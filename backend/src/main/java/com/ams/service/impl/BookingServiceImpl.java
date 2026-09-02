package com.ams.service.impl;

import com.ams.dto.BookingRequest;
import com.ams.dto.BookingPaymentRequest;
import com.ams.dto.BookingResponse;
import com.ams.dto.PassengerRequest;
import com.ams.dto.PassengerResponse;
import com.ams.entity.*;
import com.ams.exception.InsufficientSeatsException;
import com.ams.exception.InvalidBookingException;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.BookingRepository;
import com.ams.repository.FlightScheduleRepository;
import com.ams.repository.UserRepository;
import com.ams.repository.PaymentRepository;
import com.ams.repository.TicketRepository;
import com.ams.service.BookingService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;
import com.ams.dto.CancellationPreviewResponse;

/**
 * Discount strategy (additive percentage, capped at 100%) and refund-tier logic are
 * unchanged in spirit from the original implementation — see class-level docs there.
 * What changed for this revision:
 *   - The booking owner is ALWAYS the authenticated principal (from SecurityContext),
 *     never a client-supplied userId. This closes the "book/cancel on behalf of
 *     someone else" hole entirely — there is no code path that reads a userId from
 *     client input anywhere in this class.
 *   - Booking now happens against a specific FlightSchedule (scheduleId), acquired
 *     with a pessimistic write lock, so two concurrent booking requests against the
 *     same schedule cannot both succeed against the same "last remaining seat".
 *   - bookingDateTime is always LocalDateTime.now() (server-generated), separate from
 *     the schedule's travelDate.
 */
@Service
public class BookingServiceImpl implements BookingService {

    private static final int BULK_BOOKING_THRESHOLD = 6;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final PaymentRepository paymentRepository;
    private final TicketRepository ticketRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                               UserRepository userRepository,
                               FlightScheduleRepository flightScheduleRepository, PaymentRepository paymentRepository, TicketRepository ticketRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.flightScheduleRepository = flightScheduleRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional
    public BookingResponse bookFlight(BookingRequest request) {
        User user = currentUser();
        validatePassengers(request);

        // Pessimistic write lock on the schedule row: serializes concurrent bookings
        // against the same schedule so seat counts can never be oversold.
        FlightSchedule schedule = flightScheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight schedule with ID " + request.getScheduleId() + " not found"));

        if (!"SCHEDULED".equals(schedule.getStatus())) {
            throw new InvalidBookingException("This flight schedule is no longer open for booking (status: " + schedule.getStatus() + ")");
        }
        if (schedule.getTravelDate().isBefore(LocalDate.now())) {
            throw new InvalidBookingException("Cannot book a flight schedule whose travel date has already passed");
        }

        Flight flight = schedule.getFlight();
        String seatCategory = request.getSeatCategory();
        int requestedSeats = request.getNoOfSeats();

        int capacity = getCapacityForCategory(flight, seatCategory);
        int alreadyBooked = getBookedSeatsForCategory(schedule, seatCategory);
        int available = capacity - alreadyBooked;

        if (requestedSeats > available) {
            throw new InsufficientSeatsException(
                    "Only " + available + " " + seatCategory + " seat(s) available on this flight for the selected date");
        }

        Carrier carrier = flight.getCarrier();

        double baseAmount = flight.getBaseFare() * multiplierForCategory(carrier, seatCategory) * requestedSeats;
        double advanceDiscount = calculateAdvanceDiscount(carrier, schedule.getTravelDate());
        double categoryDiscount = calculateCategoryDiscount(carrier, user.getCustomerCategory());
        double bulkDiscount = requestedSeats >= BULK_BOOKING_THRESHOLD
                ? nonNull(carrier.getBulkBookingDiscount())
                : 0.0;

        double totalDiscountPercentage = Math.min(100.0, advanceDiscount + categoryDiscount + bulkDiscount);
        double discountAmount = baseAmount * totalDiscountPercentage / 100.0;
        double finalAmount = Math.max(0.0, baseAmount - discountAmount);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setFlightSchedule(schedule);
        booking.setNoOfSeats(requestedSeats);
        booking.setSeatCategory(seatCategory);
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setBookingStatus("BOOKED");
        booking.setBookingAmount(finalAmount);
        booking = bookingRepository.save(booking);

        for (PassengerRequest passengerRequest : request.getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setBooking(booking);
            passenger.setPassengerName(passengerRequest.getPassengerName().trim());
            passenger.setAge(passengerRequest.getAge());
            booking.getPassengers().add(passenger);
        }
        booking = bookingRepository.save(booking);

        return toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse payForBooking(Integer bookingId, BookingPaymentRequest request) {
        Booking booking = findBooking(bookingId);
        if (!isOwner(booking)) throw new AccessDeniedException("You may only pay for your own bookings");
        Payment existing = paymentRepository.findByBookingBookingId(bookingId).orElse(null);
        if (existing != null && "SUCCESS".equals(existing.getStatus())) return toResponse(booking);
        if (!"BOOKED".equals(booking.getBookingStatus())) throw new InvalidBookingException("This booking is not payable");
        validatePaymentRequest(request);
        FlightSchedule schedule = flightScheduleRepository.findByIdForUpdate(booking.getFlightSchedule().getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight schedule not found"));
        if (!"SCHEDULED".equals(schedule.getStatus()) || schedule.getTravelDate().isBefore(LocalDate.now())) throw new InvalidBookingException("This flight schedule is no longer payable");
        int available = getCapacityForCategory(schedule.getFlight(), booking.getSeatCategory()) - getBookedSeatsForCategory(schedule, booking.getSeatCategory());
        if (booking.getNoOfSeats() > available) throw new InsufficientSeatsException("Selected seats are no longer available");
        Payment payment = new Payment(); payment.setUser(booking.getUser()); payment.setBooking(booking); payment.setAmount(booking.getBookingAmount());
        payment.setCustomerCategory(booking.getUser().getCustomerCategory() == null ? "REGULAR" : booking.getUser().getCustomerCategory());
        payment.setPaymentMethod(request.getPaymentMethod()); payment.setStatus("SUCCESS"); payment.setPaidAt(LocalDateTime.now());
        if ("UPI".equals(request.getPaymentMethod())) payment.setUpiTransactionRef("AMS-UPI-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        else payment.setMaskedCardNumber("**** **** **** " + digits(request.getCardNumber()).substring(digits(request.getCardNumber()).length() - 4));
        paymentRepository.save(payment);
        booking.setTicketNumber(generateTicketNumber(schedule.getFlight().getCarrier().getCarrierCode(), booking.getBookingId()));
        bookingRepository.save(booking);
        double perSeat = booking.getBookingAmount() / booking.getNoOfSeats();
        issueTickets(booking, payment, perSeat, 0d);
        setBookedSeatsForCategory(schedule, booking.getSeatCategory(), getBookedSeatsForCategory(schedule, booking.getSeatCategory()) + booking.getNoOfSeats());
        flightScheduleRepository.save(schedule);
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        requireAdminOrSuperAdmin();
        return bookingRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = findBooking(bookingId);
        if (!isAdminOrSuperAdmin() && !isOwner(booking)) {
            throw new AccessDeniedException("You may only view your own bookings");
        }
        return toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        User user = currentUser();
        return bookingRepository.findByUserUserId(user.getUserId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Integer bookingId) {
        Booking booking = findBooking(bookingId);

        if (!isAdminOrSuperAdmin() && !isOwner(booking)) {
            throw new AccessDeniedException("You may only cancel your own bookings");
        }
        if (!"BOOKED".equals(booking.getBookingStatus())) {
            throw new InvalidBookingException("Booking with ID " + bookingId + " is not in BOOKED status and cannot be cancelled");
        }

        // Re-acquire the schedule with a lock before mutating its seat counts, for the
        // same concurrency-safety reason as in bookFlight().
        FlightSchedule schedule = flightScheduleRepository.findByIdForUpdate(booking.getFlightSchedule().getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight schedule not found"));

        CancellationPreviewResponse preview = cancellationPreview(booking);
        if (!preview.isEligible()) {
            throw new InvalidBookingException("Booking cannot be cancelled within 1 day of travel");
        }
        double refundAmount = preview.getRefundAmount();

        booking.setBookingStatus("CANCELLED");
        bookingRepository.save(booking);
        ticketRepository.findByBookingBookingId(bookingId).forEach(ticket -> {
            ticket.setTicketStatus("CANCELLED");
            ticket.setRefundAmount(refundAmount / booking.getNoOfSeats());
            ticket.setCancelledAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        });

        int current = getBookedSeatsForCategory(schedule, booking.getSeatCategory());
        int updated = Math.max(0, current - booking.getNoOfSeats());
        setBookedSeatsForCategory(schedule, booking.getSeatCategory(), updated);
        flightScheduleRepository.save(schedule);

        BookingResponse response = toResponse(booking);
        response.setBookingAmount(refundAmount);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CancellationPreviewResponse previewCancellation(Integer bookingId) {
        Booking booking = findBooking(bookingId);
        if (!isAdminOrSuperAdmin() && !isOwner(booking)) throw new AccessDeniedException("You may only preview cancellation of your own bookings");
        return cancellationPreview(booking);
    }

    // ---------- helpers ----------

    private Booking findBooking(Integer bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + bookingId + " not found"));
    }

    private boolean isOwner(Booking booking) {
        return currentUsernameMatches(booking.getUser().getUserName());
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        return userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }

    private void requireAdminOrSuperAdmin() {
        if (!isAdminOrSuperAdmin()) {
            throw new AccessDeniedException("Only ADMIN or SUPER_ADMIN may view all bookings");
        }
    }

    private boolean isAdminOrSuperAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority()) || "ROLE_SUPER_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private boolean currentUsernameMatches(String userName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getName() != null && authentication.getName().equals(userName);
    }

    private int getCapacityForCategory(Flight flight, String seatCategory) {
        return switch (seatCategory) {
            case "BUSINESS" -> nonNullInt(flight.getSeatCapacityBusinessClass());
            case "ECONOMY" -> nonNullInt(flight.getSeatCapacityEconomyClass());
            case "EXECUTIVE" -> nonNullInt(flight.getSeatCapacityExecutiveClass());
            default -> throw new InvalidBookingException("Invalid seat category: " + seatCategory);
        };
    }

    private int getBookedSeatsForCategory(FlightSchedule schedule, String seatCategory) {
        return switch (seatCategory) {
            case "BUSINESS" -> nonNullInt(schedule.getBookedBusinessSeats());
            case "ECONOMY" -> nonNullInt(schedule.getBookedEconomySeats());
            case "EXECUTIVE" -> nonNullInt(schedule.getBookedExecutiveSeats());
            default -> throw new InvalidBookingException("Invalid seat category: " + seatCategory);
        };
    }

    private void setBookedSeatsForCategory(FlightSchedule schedule, String seatCategory, int value) {
        switch (seatCategory) {
            case "BUSINESS" -> schedule.setBookedBusinessSeats(value);
            case "ECONOMY" -> schedule.setBookedEconomySeats(value);
            case "EXECUTIVE" -> schedule.setBookedExecutiveSeats(value);
            default -> throw new InvalidBookingException("Invalid seat category: " + seatCategory);
        }
    }

    private double calculateAdvanceDiscount(Carrier carrier, LocalDate travelDate) {
        long daysInAdvance = ChronoUnit.DAYS.between(LocalDate.now(), travelDate);
        if (daysInAdvance >= 90) return nonNull(carrier.getDiscount90DaysAdvance());
        if (daysInAdvance >= 60) return nonNull(carrier.getDiscount60DaysAdvance());
        if (daysInAdvance >= 30) return nonNull(carrier.getDiscount30DaysAdvance());
        return 0.0;
    }

    private double calculateCategoryDiscount(Carrier carrier, String customerCategory) {
        if (customerCategory == null) return 0.0;
        return switch (customerCategory) {
            case "SILVER" -> nonNull(carrier.getSilverUserDiscount());
            case "GOLD" -> nonNull(carrier.getGoldUserDiscount());
            case "PLATINUM" -> nonNull(carrier.getPlatinumUserDiscount());
            default -> 0.0;
        };
    }

    private double multiplierForCategory(Carrier carrier, String seatCategory) {
        return switch (seatCategory) {
            case "ECONOMY" -> 1.0;
            case "BUSINESS" -> nonNull(carrier.getBusinessClassMultiplier());
            case "EXECUTIVE" -> nonNull(carrier.getExecutiveClassMultiplier());
            default -> throw new InvalidBookingException("Invalid seat category: " + seatCategory);
        };
    }

    private double nonNull(Double value) { return value == null ? 0.0 : value; }
    private int nonNullInt(Integer value) { return value == null ? 0 : value; }

    private BookingResponse toResponse(Booking booking) {
        FlightSchedule schedule = booking.getFlightSchedule();
        Flight flight = schedule.getFlight();

        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getBookingId());
        response.setScheduleId(schedule.getScheduleId());
        response.setFlightId(flight.getFlightId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setOrigin(flight.getOrigin());
        response.setDestination(flight.getDestination());
        response.setCarrierName(flight.getCarrier().getCarrierName());
        response.setTravelDate(schedule.getTravelDate());
        response.setDepartureTime(schedule.getDepartureTime());
        response.setArrivalTime(schedule.getArrivalTime());
        response.setUserId(booking.getUser().getUserId());
        response.setUserName(booking.getUser().getUserName());
        response.setNoOfSeats(booking.getNoOfSeats());
        response.setSeatCategory(booking.getSeatCategory());
        response.setBookingDateTime(booking.getBookingDateTime());
        response.setBookingStatus(booking.getBookingStatus());
        response.setBookingAmount(booking.getBookingAmount());
        response.setTicketNumber(booking.getTicketNumber());
        response.setPassengers(booking.getPassengers().stream()
                .map(passenger -> new PassengerResponse(passenger.getPassengerId(), passenger.getPassengerName(), passenger.getAge()))
                .collect(Collectors.toList()));
        return response;
    }

    private void validatePassengers(BookingRequest request) {
        if (request.getNoOfSeats() == null || request.getNoOfSeats() < 1 || request.getNoOfSeats() > 6) {
            throw new InvalidBookingException("noOfSeats must be between 1 and 6 per booking.");
        }
        List<PassengerRequest> passengers = request.getPassengers();
        if (passengers == null || passengers.size() != request.getNoOfSeats()) {
            throw new InvalidBookingException("Number of passengers must exactly match the number of booked seats.");
        }
        for (PassengerRequest passenger : passengers) {
            if (passenger == null || passenger.getPassengerName() == null || passenger.getPassengerName().trim().length() < 2
                    || passenger.getPassengerName().trim().length() > 100 || passenger.getAge() == null
                    || passenger.getAge() < 1 || passenger.getAge() > 120) {
                throw new InvalidBookingException("Each passenger must have a name between 2 and 100 characters and an age between 1 and 120.");
            }
        }
        Set<String> distinctPassengers = passengers.stream()
                .map(passenger -> passenger.getPassengerName().trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        if (distinctPassengers.size() != passengers.size()) {
            throw new InvalidBookingException("Each ticket must be for a different passenger.");
        }
    }

    private CancellationPreviewResponse cancellationPreview(Booking booking) {
        long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), booking.getFlightSchedule().getTravelDate());
        Carrier carrier = booking.getFlightSchedule().getFlight().getCarrier();
        CancellationPreviewResponse response = new CancellationPreviewResponse();
        response.setDaysRemaining(daysRemaining);
        if (daysRemaining < 2) {
            response.setEligible(false); response.setRefundAmount(0); response.setRefundPercentage(0);
            response.setReason("This booking is within 1 day of travel and cannot be cancelled.");
            return response;
        }
        double percentage = daysRemaining >= 20 ? nonNull(carrier.getRefund20DaysOrMore()) :
                daysRemaining >= 10 ? nonNull(carrier.getRefund10DaysBefore()) : nonNull(carrier.getRefund2DaysBefore());
        response.setEligible(true); response.setRefundPercentage(percentage);
        response.setRefundAmount(booking.getBookingAmount() * percentage / 100.0);
        return response;
    }

    private void validatePaymentRequest(BookingPaymentRequest request) {
        if ("UPI".equals(request.getPaymentMethod())) return;
        String card = digits(request.getCardNumber());
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().length() < 2 || request.getCardHolderName().trim().length() > 100) throw new InvalidBookingException("Cardholder name is required.");
        if (!card.matches("\\d{13,19}") || !passesLuhn(card)) throw new InvalidBookingException("Invalid card number.");
        if (request.getCvv() == null || !request.getCvv().matches("\\d{3,4}")) throw new InvalidBookingException("Invalid CVV.");
        if (request.getExpiryMonth() == null || request.getExpiryMonth() < 1 || request.getExpiryMonth() > 12) throw new InvalidBookingException("Invalid expiry month.");
        if (request.getExpiryYear() == null || YearMonth.of(request.getExpiryYear(), request.getExpiryMonth()).isBefore(YearMonth.now())) throw new InvalidBookingException("Card has expired.");
    }
    private String digits(String value) { return value == null ? "" : value.replaceAll("\\s", ""); }
    private boolean passesLuhn(String value) { int sum = 0; boolean alternate = false; for (int i = value.length() - 1; i >= 0; i--) { int n = value.charAt(i) - '0'; if (alternate && (n *= 2) > 9) n -= 9; sum += n; alternate = !alternate; } return sum % 10 == 0; }

    private String generateTicketNumber(String carrierCode, Integer bookingId) {
        if (carrierCode == null || !carrierCode.matches("[A-Z]{2}")) throw new InvalidBookingException("Carrier must have a valid carrier code to issue tickets");
        String prefix = carrierCode + String.format("%04d", bookingId) + "-";
        for (int attempt = 0; attempt < 20; attempt++) {
            String ticket = prefix + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
            if (!ticketRepository.existsByTicketNumber(ticket) && !bookingRepository.existsByTicketNumber(ticket)) return ticket;
        }
        throw new InvalidBookingException("Unable to generate a unique ticket number; please retry");
    }

    private void issueTickets(Booking booking, Payment payment, double baseFare, double discountAmount) {
        FlightSchedule schedule = booking.getFlightSchedule();
        Flight flight = schedule.getFlight();
        Carrier carrier = flight.getCarrier();
        for (int i = 0; i < booking.getPassengers().size(); i++) {
            Passenger passenger = booking.getPassengers().get(i);
            Ticket ticket = new Ticket();
            ticket.setTicketNumber(i == 0 ? booking.getTicketNumber() : generateTicketNumber(carrier.getCarrierCode(), booking.getBookingId()));
            ticket.setBooking(booking); ticket.setPassenger(passenger); ticket.setUser(booking.getUser()); ticket.setPayment(payment);
            ticket.setFlightId(flight.getFlightId()); ticket.setCarrierId(carrier.getCarrierId());
            ticket.setCarrierName(carrier.getCarrierName()); ticket.setCarrierCode(carrier.getCarrierCode()); ticket.setFlightNumber(flight.getFlightNumber());
            ticket.setOrigin(flight.getOrigin()); ticket.setDestination(flight.getDestination()); ticket.setTravelDate(schedule.getTravelDate()); ticket.setDepartureTime(schedule.getDepartureTime()); ticket.setArrivalTime(schedule.getArrivalTime());
            ticket.setSeatCategory(booking.getSeatCategory()); ticket.setNumberOfSeats(1); ticket.setBaseFare(baseFare); ticket.setDiscountAmount(discountAmount); ticket.setPaidAmount((baseFare - discountAmount));
            ticket.setTicketStatus("CONFIRMED"); ticket.setIssuedAt(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
    }
}
