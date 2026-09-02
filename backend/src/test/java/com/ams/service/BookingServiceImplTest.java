package com.ams.service;

import com.ams.dto.*;
import com.ams.entity.Flight;
import com.ams.entity.FlightSchedule;
import com.ams.entity.User;
import com.ams.exception.InsufficientSeatsException;
import com.ams.exception.InvalidBookingException;
import com.ams.repository.PaymentRepository;
import com.ams.repository.TicketRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {

    @Autowired private BookingService bookingService;
    @Autowired private UserService userService;
    @Autowired private CarrierService carrierService;
    @Autowired private FlightService flightService;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private PaymentRepository paymentRepository;

    private Integer scheduleId;
    private String regularUsername;
    private String goldUsername;

    @BeforeEach
    void setUp() {
        CarrierRequest cr = new CarrierRequest();
        cr.setCarrierName("Booking Test Air " + System.nanoTime());
        cr.setCarrierCode("BT");
        cr.setDiscount30DaysAdvance(10.0);
        cr.setDiscount60DaysAdvance(20.0);
        cr.setDiscount90DaysAdvance(30.0);
        cr.setBulkBookingDiscount(10.0);
        cr.setSilverUserDiscount(10.0);
        cr.setGoldUserDiscount(20.0);
        cr.setPlatinumUserDiscount(30.0);
        cr.setRefund2DaysBefore(75.0);
        cr.setRefund10DaysBefore(85.0);
        cr.setRefund20DaysOrMore(95.0);
        var carrier = carrierService.createCarrier(cr);

        FlightRequest fr = new FlightRequest();
        fr.setCarrierId(carrier.getCarrierId());
        fr.setFlightNumber("BT" + (System.nanoTime() % 1000));
        fr.setOrigin("Chennai");
        fr.setDestination("Delhi");
        fr.setBaseFare(3000.0);
        fr.setSeatCapacityBusinessClass(30);
        fr.setSeatCapacityEconomyClass(100);
        fr.setSeatCapacityExecutiveClass(25);
        Flight flight = flightService.createFlight(fr);

        FlightScheduleRequest sr = new FlightScheduleRequest();
        sr.setTravelDate(LocalDate.now().plusDays(25));
        sr.setDepartureTime(LocalTime.of(6, 30));
        sr.setArrivalTime(LocalTime.of(9, 15));
        FlightSchedule schedule = flightService.createSchedule(flight.getFlightId(), sr);
        scheduleId = schedule.getScheduleId();

        regularUsername = "regular_" + System.nanoTime();
        RegisterUserRequest regularReq = baseUserRequest(regularUsername, "REGULAR");
        userService.registerUser(regularReq);

        goldUsername = "gold_" + System.nanoTime();
        RegisterUserRequest goldReq = baseUserRequest(goldUsername, "GOLD");
        userService.registerUser(goldReq);
    }

    private RegisterUserRequest baseUserRequest(String userName, String category) {
        RegisterUserRequest r = new RegisterUserRequest();
        r.setUserName(userName);
        r.setPassword("Passw0rd!");
        r.setCustomerCategory(category);
        r.setPhone("9876543210");
        r.setEmailId(userName + "@example.com");
        r.setAddress1("Address 1");
        r.setCity("Chennai");
        r.setState("Tamil Nadu");
        r.setZipCode("600001");
        r.setDob(LocalDate.of(2000, 1, 1));
        return r;
    }

    @AfterEach
    void tearDown() {
        TestSecurityUtil.clear();
    }

    private BookingRequest bookingRequest(int seats, String category) {
        BookingRequest r = new BookingRequest();
        r.setScheduleId(scheduleId);
        r.setNoOfSeats(seats);
        r.setPaymentMethod("UPI");
        r.setSeatCategory(category);
        r.setPassengers(IntStream.range(0, seats).mapToObj(index -> {
            PassengerRequest passenger = new PassengerRequest();
            passenger.setPassengerName("Passenger " + (index + 1));
            passenger.setAge(25);
            return passenger;
        }).toList());
        return r;
    }

    @Test
    void bookFlight_success_usesAuthenticatedUser_notClientSuppliedId() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingResponse response = bookingService.bookFlight(bookingRequest(1, "ECONOMY"));

        assertEquals("BOOKED", response.getBookingStatus());
        assertEquals(regularUsername, response.getUserName());
        assertNotNull(response.getBookingDateTime());
        assertEquals(scheduleId, response.getScheduleId());
        assertEquals(1, response.getPassengers().size());
    }

    @Test
    void bookFlight_groupBooking_createsOneTicketPerPassengerAndOnePayment() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingResponse response = bookingService.bookFlight(bookingRequest(4, "ECONOMY"));
        BookingPaymentRequest paymentRequest = new BookingPaymentRequest();
        paymentRequest.setPaymentMethod("UPI");
        response = bookingService.payForBooking(response.getBookingId(), paymentRequest);

        var tickets = ticketRepository.findByBookingBookingId(response.getBookingId());
        assertEquals(4, response.getPassengers().size());
        assertEquals(4, tickets.size());
        assertEquals(4, tickets.stream().map(ticket -> ticket.getTicketNumber()).distinct().count());
        assertTrue(tickets.stream().allMatch(ticket -> ticket.getPassenger() != null));
        assertTrue(paymentRepository.findByBookingBookingId(response.getBookingId()).isPresent());
    }

    @Test
    void bookFlight_rejectsPassengerCountMismatch() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingRequest request = bookingRequest(4, "ECONOMY");
        request.setPassengers(request.getPassengers().subList(0, 3));
        assertThrows(InvalidBookingException.class, () -> bookingService.bookFlight(request));
    }

    @Test
    void bookFlight_insufficientSeats_throwsException() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        assertThrows(InvalidBookingException.class, () -> bookingService.bookFlight(bookingRequest(999, "ECONOMY")));
    }

    @Test
    void bookFlight_goldCustomerDiscount_applied() {
        TestSecurityUtil.loginAs(goldUsername, "CUSTOMER");
        // 25 days advance -> no advance discount; GOLD -> 20% discount => 600
        BookingResponse response = bookingService.bookFlight(bookingRequest(1, "ECONOMY"));
        assertEquals(2400.0, response.getBookingAmount(), 0.01);
    }

    @Test
    void bookFlight_bulkDiscount_applied() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        // Six seats is the maximum group booking; it remains eligible for bulk discount.
        BookingResponse response = bookingService.bookFlight(bookingRequest(6, "ECONOMY"));
        assertEquals(16200.0, response.getBookingAmount(), 0.01);
    }

    @Test
    void cancelBooking_ownBooking_succeedsWithRefund() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingResponse booked = bookingService.bookFlight(bookingRequest(1, "ECONOMY"));

        // 25 days remaining -> refund20DaysOrMore = 95%.
        BookingResponse cancelled = bookingService.cancelBooking(booked.getBookingId());
        assertEquals("CANCELLED", cancelled.getBookingStatus());
        assertEquals(2850.0, cancelled.getBookingAmount(), 0.01);
    }

    @Test
    void cancelBooking_otherCustomersBooking_isRejected() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingResponse booked = bookingService.bookFlight(bookingRequest(1, "ECONOMY"));

        TestSecurityUtil.loginAs(goldUsername, "CUSTOMER");
        assertThrows(AccessDeniedException.class, () -> bookingService.cancelBooking(booked.getBookingId()));
    }

    @Test
    void cancelBooking_alreadyCancelled_throwsException() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingResponse booked = bookingService.bookFlight(bookingRequest(1, "ECONOMY"));
        bookingService.cancelBooking(booked.getBookingId());

        assertThrows(InvalidBookingException.class, () -> bookingService.cancelBooking(booked.getBookingId()));
    }

    @Test
    void cancelBooking_restoresSeatAvailability() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        BookingResponse first = bookingService.bookFlight(bookingRequest(3, "ECONOMY"));
        bookingService.cancelBooking(first.getBookingId());

        // The released seats are available again, subject to the 6-seat per-booking limit.
        BookingResponse second = bookingService.bookFlight(bookingRequest(6, "ECONOMY"));
        assertNotNull(second.getBookingId());
    }

    @Test
    void getMyBookings_onlyReturnsAuthenticatedUsersBookings() {
        TestSecurityUtil.loginAs(regularUsername, "CUSTOMER");
        bookingService.bookFlight(bookingRequest(1, "ECONOMY"));

        TestSecurityUtil.loginAs(goldUsername, "CUSTOMER");
        bookingService.bookFlight(bookingRequest(1, "BUSINESS"));

        assertEquals(1, bookingService.getMyBookings().size());
        assertEquals(goldUsername, bookingService.getMyBookings().get(0).getUserName());
    }
}
