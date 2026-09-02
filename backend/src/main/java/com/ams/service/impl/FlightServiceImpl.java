package com.ams.service.impl;

import com.ams.dto.FlightRequest;
import com.ams.dto.FlightScheduleRequest;
import com.ams.dto.FlightSearchResponse;
import com.ams.dto.FlightResponse;
import com.ams.dto.FlightScheduleResponse;
import com.ams.dto.SuggestedFlightNumberResponse;
import com.ams.dto.FlightScheduleTemplateRequest;
import com.ams.dto.FlightScheduleTemplateResponse;
import com.ams.entity.Carrier;
import com.ams.entity.Flight;
import com.ams.entity.FlightSchedule;
import com.ams.entity.FlightScheduleTemplate;
import com.ams.exception.DuplicateResourceException;
import com.ams.exception.InvalidBookingException;
import com.ams.exception.ResourceNotFoundException;
import com.ams.repository.CarrierRepository;
import com.ams.repository.FlightRepository;
import com.ams.repository.FlightScheduleRepository;
import com.ams.repository.FlightScheduleTemplateRepository;
import com.ams.service.FlightService;
import com.ams.util.AirportNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService {

    private final FlightRepository flightRepository;
    private final CarrierRepository carrierRepository;
    private final FlightScheduleRepository flightScheduleRepository;
    private final FlightScheduleTemplateRepository templateRepository;

    public FlightServiceImpl(FlightRepository flightRepository,
                              CarrierRepository carrierRepository,
                              FlightScheduleRepository flightScheduleRepository, FlightScheduleTemplateRepository templateRepository) {
        this.flightRepository = flightRepository;
        this.carrierRepository = carrierRepository;
        this.flightScheduleRepository = flightScheduleRepository;
        this.templateRepository = templateRepository;
    }

    @Override
    @Transactional
    public Flight createFlight(FlightRequest request) {
        Carrier carrier = carrierRepository.findById(request.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier with ID " + request.getCarrierId() + " not found"));

        if (flightRepository.existsByFlightNumberIgnoreCase(request.getFlightNumber())) {
            throw new DuplicateResourceException("Flight number '" + request.getFlightNumber() + "' already exists");
        }
        validateFlightNumberForCarrier(request.getFlightNumber(), carrier);
        validateCapacityTotal(request);

        validateRoute(request.getOrigin(), request.getDestination());

        Flight flight = new Flight();
        flight.setCarrier(carrier);
        applyRequestToFlight(flight, request);

        return flightRepository.save(flight);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Flight getFlightById(Integer flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight with ID " + flightId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Flight> getFlightsByCarrierName(String carrierName) {
        return flightRepository.findByCarrierCarrierName(carrierName);
    }

    @Override
    @Transactional
    public Flight updateFlight(Integer flightId, FlightRequest request) {
        Flight flight = getFlightById(flightId);

        Carrier carrier = carrierRepository.findById(request.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException("Carrier with ID " + request.getCarrierId() + " not found"));

        if (!flight.getFlightNumber().equalsIgnoreCase(request.getFlightNumber())
                && flightRepository.existsByFlightNumberIgnoreCase(request.getFlightNumber())) {
            throw new DuplicateResourceException("Flight number '" + request.getFlightNumber() + "' already exists");
        }
        validateFlightNumberForCarrier(request.getFlightNumber(), carrier);
        validateCapacityTotal(request);

        validateRoute(request.getOrigin(), request.getDestination());

        flight.setCarrier(carrier);
        applyRequestToFlight(flight, request);

        return flightRepository.save(flight);
    }

    @Override
    @Transactional
    public void deleteFlight(Integer flightId) {
        Flight flight = getFlightById(flightId);

        if (flightScheduleRepository.existsByFlightFlightId(flightId)
                || templateRepository.existsByFlightFlightId(flightId)) {
            throw new InvalidBookingException("Flight '" + flight.getFlightNumber()
                    + "' cannot be deleted while it has schedules or schedule templates");
        }

        flightRepository.delete(flight);
    }

    @Override
    @Transactional
    public FlightSchedule createSchedule(Integer flightId, FlightScheduleRequest request) {
        Flight flight = getFlightById(flightId);
        validateScheduleTimes(request);

        if (flightScheduleRepository.existsByFlightFlightIdAndTravelDateAndDepartureTime(
                flightId, request.getTravelDate(), request.getDepartureTime())) {
            throw new DuplicateResourceException(
                    "A schedule for flight " + flight.getFlightNumber() + " on " + request.getTravelDate()
                            + " at " + request.getDepartureTime() + " already exists");
        }

        FlightSchedule schedule = new FlightSchedule();
        schedule.setFlight(flight);
        schedule.setTravelDate(request.getTravelDate());
        schedule.setDepartureTime(request.getDepartureTime());
        schedule.setArrivalTime(request.getArrivalTime());
        schedule.setBookedBusinessSeats(0);
        schedule.setBookedEconomySeats(0);
        schedule.setBookedExecutiveSeats(0);
        schedule.setStatus("SCHEDULED");

        return flightScheduleRepository.save(schedule);
    }

    @Override @Transactional(readOnly = true)
    public List<FlightScheduleResponse> getSchedules(Integer flightId) {
        getFlightById(flightId);
        return flightScheduleRepository.findByFlightFlightIdOrderByTravelDateAscDepartureTimeAsc(flightId)
                .stream().map(this::toFlightScheduleResponse).collect(Collectors.toList());
    }

    @Override @Transactional
    public FlightScheduleResponse updateSchedule(Integer scheduleId, FlightScheduleRequest request) {
        FlightSchedule schedule = flightScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight schedule with ID " + scheduleId + " not found"));
        if (schedule.getTravelDate().isBefore(LocalDate.now()) || bookedSeats(schedule) > 0) {
            throw new InvalidBookingException("A historical or booked schedule cannot be changed");
        }
        validateScheduleTimes(request);
        if ((!schedule.getTravelDate().equals(request.getTravelDate()) || !schedule.getDepartureTime().equals(request.getDepartureTime()))
                && flightScheduleRepository.existsByFlightFlightIdAndTravelDateAndDepartureTime(schedule.getFlight().getFlightId(), request.getTravelDate(), request.getDepartureTime())) {
            throw new DuplicateResourceException("A schedule already exists for this flight, date and departure time");
        }
        schedule.setTravelDate(request.getTravelDate()); schedule.setDepartureTime(request.getDepartureTime()); schedule.setArrivalTime(request.getArrivalTime());
        return toFlightScheduleResponse(flightScheduleRepository.save(schedule));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightSearchResponse> search(String origin, String destination, LocalDate travelDate) {
        String normalizedOrigin = AirportNormalizer.normalize(origin);
        String normalizedDestination = AirportNormalizer.normalize(destination);
        if (normalizedOrigin.equalsIgnoreCase(normalizedDestination)) {
            throw new InvalidBookingException("origin and destination cannot be the same");
        }
        if (travelDate.isBefore(LocalDate.now())) {
            throw new InvalidBookingException("travelDate cannot be in the past");
        }

        return flightScheduleRepository.searchByRouteAndDate(
                        AirportNormalizer.searchTerms(normalizedOrigin),
                        AirportNormalizer.searchTerms(normalizedDestination),
                        travelDate).stream()
                .map(this::toSearchResponse)
                .filter(response ->
                        response.getAvailableBusinessSeats() > 0
                                || response.getAvailableEconomySeats() > 0
                                || response.getAvailableExecutiveSeats() > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightSearchResponse> scheduled(String origin, String destination, LocalDate fromDate, LocalDate toDate) {
        String normalizedOrigin = AirportNormalizer.normalize(origin);
        String normalizedDestination = AirportNormalizer.normalize(destination);
        if (normalizedOrigin.equalsIgnoreCase(normalizedDestination)) throw new InvalidBookingException("origin and destination cannot be the same");
        LocalDate start = fromDate == null || fromDate.isBefore(LocalDate.now()) ? LocalDate.now() : fromDate;
        LocalDate end = toDate == null ? start.plusDays(6) : toDate;
        if (end.isBefore(start) || end.isAfter(start.plusDays(6))) throw new InvalidBookingException("scheduled discovery date range must be within seven upcoming days");
        return flightScheduleRepository.searchByRouteAndDateRange(AirportNormalizer.searchTerms(normalizedOrigin), AirportNormalizer.searchTerms(normalizedDestination), start, end).stream().map(this::toSearchResponse).filter(r -> r.getAvailableBusinessSeats() > 0 || r.getAvailableEconomySeats() > 0 || r.getAvailableExecutiveSeats() > 0).collect(Collectors.toList());
    }

    private void validateRoute(String origin, String destination) {
        if (AirportNormalizer.normalize(origin).equalsIgnoreCase(AirportNormalizer.normalize(destination))) {
            throw new InvalidBookingException("origin and destination cannot be the same");
        }
    }

    private void applyRequestToFlight(Flight flight, FlightRequest request) {
        flight.setFlightNumber(request.getFlightNumber());
        flight.setOrigin(AirportNormalizer.normalize(request.getOrigin()));
        flight.setDestination(AirportNormalizer.normalize(request.getDestination()));
        flight.setBaseFare(request.getBaseFare());
        flight.setSeatCapacityBusinessClass(request.getSeatCapacityBusinessClass());
        flight.setSeatCapacityEconomyClass(request.getSeatCapacityEconomyClass());
        flight.setSeatCapacityExecutiveClass(request.getSeatCapacityExecutiveClass());
    }

    private void validateFlightNumberForCarrier(String flightNumber, Carrier carrier) {
        if (carrier.getCarrierCode() == null || carrier.getCarrierCode().isBlank()) {
            throw new InvalidBookingException("Carrier '" + carrier.getCarrierName() + "' must have a carrier code before flights can be created");
        }
        if (!flightNumber.startsWith(carrier.getCarrierCode())) {
            throw new InvalidBookingException("Flight number must start with carrier code '" + carrier.getCarrierCode() + "' for " + carrier.getCarrierName());
        }
    }

    private void validateCapacityTotal(FlightRequest request) {
        int total = request.getSeatCapacityBusinessClass() + request.getSeatCapacityEconomyClass() + request.getSeatCapacityExecutiveClass();
        if (total < 150 || total > 750) {
            throw new InvalidBookingException("Total seat capacity must be between 150 and 750");
        }
    }

    private FlightSearchResponse toSearchResponse(FlightSchedule schedule) {
        Flight flight = schedule.getFlight();

        FlightSearchResponse response = new FlightSearchResponse();
        response.setScheduleId(schedule.getScheduleId());
        response.setFlightId(flight.getFlightId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setCarrierName(flight.getCarrier().getCarrierName());
        response.setOrigin(flight.getOrigin());
        response.setDestination(flight.getDestination());
        response.setTravelDate(schedule.getTravelDate());
        response.setDepartureTime(schedule.getDepartureTime());
        response.setArrivalTime(schedule.getArrivalTime());
        response.setBaseFare(flight.getBaseFare());
        response.setEconomyFare(flight.getBaseFare());
        response.setBusinessFare(flight.getBaseFare() * multiplierOrOne(flight.getCarrier().getBusinessClassMultiplier()));
        response.setExecutiveFare(flight.getBaseFare() * multiplierOrOne(flight.getCarrier().getExecutiveClassMultiplier()));
        response.setAvailableBusinessSeats(flight.getSeatCapacityBusinessClass() - schedule.getBookedBusinessSeats());
        response.setAvailableEconomySeats(flight.getSeatCapacityEconomyClass() - schedule.getBookedEconomySeats());
        response.setAvailableExecutiveSeats(flight.getSeatCapacityExecutiveClass() - schedule.getBookedExecutiveSeats());
        return response;
    }

    // DTO conversion methods to avoid Hibernate proxy serialization issues
    private FlightResponse toFlightResponse(Flight flight) {
        return new FlightResponse(
                flight.getFlightId(),
                flight.getFlightNumber(),
                flight.getCarrier().getCarrierName(),
                flight.getCarrier().getCarrierId(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getBaseFare(),
                flight.getSeatCapacityBusinessClass(),
                flight.getSeatCapacityEconomyClass(),
                flight.getSeatCapacityExecutiveClass()
        );
    }

    private FlightScheduleResponse toFlightScheduleResponse(FlightSchedule schedule) {
        Flight flight = schedule.getFlight();
        return new FlightScheduleResponse(
                schedule.getScheduleId(),
                flight.getFlightId(),
                flight.getFlightNumber(),
                flight.getCarrier().getCarrierName(),
                schedule.getTravelDate(),
                schedule.getDepartureTime(),
                schedule.getArrivalTime(),
                schedule.getBookedBusinessSeats(),
                schedule.getBookedEconomySeats(),
                schedule.getBookedExecutiveSeats(),
                flight.getSeatCapacityBusinessClass(),
                flight.getSeatCapacityEconomyClass(),
                flight.getSeatCapacityExecutiveClass(),
                schedule.getStatus(),
                schedule.getVersion()
        );
    }

    private double multiplierOrOne(Double value) { return value == null ? 1.0 : value; }

    @Override
    @Transactional
    public FlightResponse createFlightResponse(FlightRequest request) {
        return toFlightResponse(createFlight(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getAllFlightsAsResponse() {
        return getAllFlights().stream()
                .map(this::toFlightResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FlightResponse getFlightByIdAsResponse(Integer flightId) {
        return toFlightResponse(getFlightById(flightId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightResponse> getFlightsByCarrierNameAsResponse(String carrierName) {
        return getFlightsByCarrierName(carrierName).stream()
                .map(this::toFlightResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FlightResponse updateFlightResponse(Integer flightId, FlightRequest request) {
        return toFlightResponse(updateFlight(flightId, request));
    }

    @Override
    @Transactional
    public FlightScheduleResponse createScheduleResponse(Integer flightId, FlightScheduleRequest request) {
        return toFlightScheduleResponse(createSchedule(flightId, request));
    }

    @Override
    @Transactional(readOnly = true)
    public SuggestedFlightNumberResponse suggestFlightNumber(Integer carrierId) {
        Carrier carrier = carrierRepository.findById(carrierId)
                .orElseThrow(() -> new ResourceNotFoundException("Carrier with ID " + carrierId + " not found"));
        if (carrier.getCarrierCode() == null || carrier.getCarrierCode().isBlank()) {
            throw new InvalidBookingException("Carrier must have a carrier code before a flight number can be suggested");
        }
        int highest = flightRepository.findByCarrierCarrierId(carrierId).stream()
                .map(Flight::getFlightNumber).filter(n -> n != null && n.startsWith(carrier.getCarrierCode()))
                .map(n -> n.substring(carrier.getCarrierCode().length())).filter(n -> n.matches("\\d+"))
                .mapToInt(Integer::parseInt).max().orElse(100);
        return new SuggestedFlightNumberResponse(carrier.getCarrierCode() + String.format("%02d", highest + 1));
    }

    @Override @Transactional
    public FlightScheduleTemplateResponse createScheduleTemplate(Integer flightId, FlightScheduleTemplateRequest request) {
        FlightScheduleTemplate template = new FlightScheduleTemplate(); template.setFlight(getFlightById(flightId));
        applyTemplate(template, request); template = templateRepository.save(template); generateInstances(template); return toTemplateResponse(template);
    }

    @Override @Transactional(readOnly = true)
    public List<FlightScheduleTemplateResponse> getScheduleTemplates(Integer flightId) {
        getFlightById(flightId); return templateRepository.findByFlightFlightId(flightId).stream().map(this::toTemplateResponse).collect(Collectors.toList());
    }

    @Override @Transactional
    public FlightScheduleTemplateResponse updateScheduleTemplate(Integer templateId, FlightScheduleTemplateRequest request) {
        FlightScheduleTemplate template = getTemplate(templateId);
        validateTemplateRequest(request);
        if (!Boolean.TRUE.equals(request.getActive())) {
            ensureMinimumDailySchedules(template, template.getOperatingDays());
        } else {
            Set<DayOfWeek> removedDays = template.getOperatingDays().stream()
                    .filter(day -> !request.getOperatingDays().contains(day)).collect(Collectors.toSet());
            ensureMinimumDailySchedules(template, removedDays);
        }
        cancelUnbookedFutureInstances(template);
        applyTemplate(template, request); templateRepository.save(template); generateInstances(template); return toTemplateResponse(template);
    }

    @Override @Transactional
    public FlightScheduleTemplateResponse deactivateScheduleTemplate(Integer templateId) {
        FlightScheduleTemplate template = getTemplate(templateId);
        ensureMinimumDailySchedules(template, template.getOperatingDays());
        template.setActive(false);
        cancelUnbookedFutureInstances(template);
        return toTemplateResponse(templateRepository.save(template));
    }

    private FlightScheduleTemplate getTemplate(Integer id) { return templateRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Schedule template with ID " + id + " not found")); }
    private void applyTemplate(FlightScheduleTemplate template, FlightScheduleTemplateRequest request) {
        validateTemplateRequest(request);
        template.setDepartureTime(request.getDepartureTime()); template.setArrivalTime(request.getArrivalTime()); template.setOperatingDays(Set.copyOf(request.getOperatingDays())); template.setEffectiveFrom(request.getEffectiveFrom()); template.setEffectiveTo(request.getEffectiveTo()); template.setActive(true);
        template.setActive(!Boolean.FALSE.equals(request.getActive()));
    }
    private void validateScheduleTimes(FlightScheduleRequest request) {
        if (request == null || request.getTravelDate() == null) {
            throw new InvalidBookingException("travelDate is required");
        }
        if (request.getTravelDate().isBefore(LocalDate.now())) throw new InvalidBookingException("travelDate cannot be in the past");
        validateScheduleDuration(request.getDepartureTime(), request.getArrivalTime());
        if (request.getTravelDate().isEqual(LocalDate.now()) && !request.getDepartureTime().isAfter(LocalTime.now())) {
            throw new InvalidBookingException("departureTime must be in the future when travelDate is today");
        }
    }
    private int bookedSeats(FlightSchedule schedule) { return schedule.getBookedBusinessSeats() + schedule.getBookedEconomySeats() + schedule.getBookedExecutiveSeats(); }
    private void cancelUnbookedFutureInstances(FlightScheduleTemplate template) {
        flightScheduleRepository.findByScheduleTemplateScheduleTemplateIdAndTravelDateGreaterThanEqual(template.getScheduleTemplateId(), LocalDate.now())
                .stream().filter(schedule -> bookedSeats(schedule) == 0 && "SCHEDULED".equals(schedule.getStatus()))
                .forEach(schedule -> { schedule.setStatus("CANCELLED"); flightScheduleRepository.save(schedule); });
    }
    private void validateTemplateRequest(FlightScheduleTemplateRequest request) {
        if (request.getOperatingDays() == null || request.getOperatingDays().isEmpty()) throw new InvalidBookingException("operatingDays must contain at least one day");
        if (request.getEffectiveFrom() == null || request.getEffectiveFrom().isBefore(LocalDate.now())) throw new InvalidBookingException("effectiveFrom cannot be in the past");
        validateScheduleDuration(request.getDepartureTime(), request.getArrivalTime());
        if (request.getEffectiveTo() != null && !request.getEffectiveTo().isAfter(request.getEffectiveFrom())) throw new InvalidBookingException("effectiveTo must be after effectiveFrom");
    }
    private void ensureMinimumDailySchedules(FlightScheduleTemplate template, Set<DayOfWeek> daysToRemove) {
        LocalDate start = template.getEffectiveFrom().isAfter(LocalDate.now()) ? template.getEffectiveFrom() : LocalDate.now();
        LocalDate end = template.getEffectiveTo() == null ? start.plusDays(6) : template.getEffectiveTo();
        for (LocalDate date = start; !date.isAfter(end) && !date.isAfter(start.plusDays(6)); date = date.plusDays(1)) {
            if (!daysToRemove.contains(date.getDayOfWeek())) continue;
            long count = flightScheduleRepository.countByTravelDateAndStatus(date, "SCHEDULED");
            if (count <= 5) throw new InvalidBookingException("Cannot deactivate this schedule because " + date.getDayOfWeek() + " would have only " + Math.max(0, count - 1) + " scheduled flights. At least 5 flights are required.");
        }
    }
    private void validateScheduleDuration(LocalTime departureTime, LocalTime arrivalTime) {
        if (departureTime == null || arrivalTime == null) {
            throw new InvalidBookingException("Departure time and arrival time are required.");
        }
        if (!departureTime.isBefore(arrivalTime)) throw new InvalidBookingException("Departure time must be before arrival time.");
        if (Duration.between(departureTime, arrivalTime).compareTo(Duration.ofHours(12)) > 0) throw new InvalidBookingException("Flight duration cannot exceed 12 hours.");
    }
    // Strategy A: materialize a 90-day rolling window. Existing search/booking continue to use persisted schedules and never virtual IDs.
    private void generateInstances(FlightScheduleTemplate template) {
        if (!template.isActive()) return;
        validateScheduleDuration(template.getDepartureTime(), template.getArrivalTime());
        LocalDate start = template.getEffectiveFrom().isAfter(LocalDate.now()) ? template.getEffectiveFrom() : LocalDate.now();
        if (start.isEqual(LocalDate.now()) && !template.getDepartureTime().isAfter(LocalTime.now())) start = start.plusDays(1);
        LocalDate end = LocalDate.now().plusDays(90); if (template.getEffectiveTo() != null && template.getEffectiveTo().isBefore(end)) end = template.getEffectiveTo();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            if (!template.getOperatingDays().contains(date.getDayOfWeek()) || flightScheduleRepository.existsByFlightFlightIdAndTravelDateAndDepartureTime(template.getFlight().getFlightId(), date, template.getDepartureTime())) continue;
            FlightSchedule schedule = new FlightSchedule(); schedule.setFlight(template.getFlight()); schedule.setScheduleTemplate(template); schedule.setTravelDate(date); schedule.setDepartureTime(template.getDepartureTime()); schedule.setArrivalTime(template.getArrivalTime()); schedule.setBookedBusinessSeats(0); schedule.setBookedEconomySeats(0); schedule.setBookedExecutiveSeats(0); schedule.setStatus("SCHEDULED"); flightScheduleRepository.save(schedule);
        }
    }
    private FlightScheduleTemplateResponse toTemplateResponse(FlightScheduleTemplate t) { FlightScheduleTemplateResponse r = new FlightScheduleTemplateResponse(); r.setScheduleTemplateId(t.getScheduleTemplateId()); r.setFlightId(t.getFlight().getFlightId()); r.setFlightNumber(t.getFlight().getFlightNumber()); r.setCarrierName(t.getFlight().getCarrier().getCarrierName()); r.setDepartureTime(t.getDepartureTime()); r.setArrivalTime(t.getArrivalTime()); r.setOperatingDays(t.getOperatingDays()); r.setEffectiveFrom(t.getEffectiveFrom()); r.setEffectiveTo(t.getEffectiveTo()); r.setActive(t.isActive()); return r; }
}
