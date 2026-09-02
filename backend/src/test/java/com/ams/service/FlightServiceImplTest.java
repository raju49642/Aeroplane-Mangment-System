package com.ams.service;

import com.ams.dto.*;
import com.ams.entity.Carrier;
import com.ams.entity.Flight;
import com.ams.entity.FlightSchedule;
import com.ams.exception.DuplicateResourceException;
import com.ams.exception.InvalidBookingException;
import com.ams.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FlightServiceImplTest {

    @Autowired private FlightService flightService;
    @Autowired private CarrierService carrierService;

    private Carrier carrier;

    @BeforeEach
    void setUp() {
        CarrierRequest cr = new CarrierRequest();
        cr.setCarrierName("Test Airlines " + System.nanoTime());
        cr.setCarrierCode("AI");
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
        carrier = carrierService.createCarrier(cr);
    }

    private FlightRequest buildFlightRequest(String flightNumber, String origin, String dest) {
        FlightRequest r = new FlightRequest();
        r.setCarrierId(carrier.getCarrierId());
        r.setFlightNumber(flightNumber);
        r.setOrigin(origin);
        r.setDestination(dest);
        r.setBaseFare(7500.0);
        r.setSeatCapacityBusinessClass(30);
        r.setSeatCapacityEconomyClass(100);
        r.setSeatCapacityExecutiveClass(25);
        return r;
    }

    @Test
    void createFlight_success() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI101", "Chennai", "Delhi"));
        assertNotNull(flight.getFlightId());
        assertEquals("AI101", flight.getFlightNumber());
    }

    @Test
    void createFlight_duplicateFlightNumber_throwsException() {
        flightService.createFlight(buildFlightRequest("AI203", "Chennai", "Delhi"));
        assertThrows(DuplicateResourceException.class,
                () -> flightService.createFlight(buildFlightRequest("AI203", "Mumbai", "Pune")));
    }

    @Test
    void createFlight_sameOriginAndDestination_throwsException() {
        assertThrows(InvalidBookingException.class,
                () -> flightService.createFlight(buildFlightRequest("AI811", "Chennai", "Chennai")));
    }

    @Test
    void createSchedule_futureDate_success() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI102", "Chennai", "Delhi"));
        FlightScheduleRequest sr = new FlightScheduleRequest();
        sr.setTravelDate(LocalDate.now().plusDays(10));
        sr.setDepartureTime(LocalTime.of(6, 30));
        sr.setArrivalTime(LocalTime.of(9, 15));

        FlightSchedule schedule = flightService.createSchedule(flight.getFlightId(), sr);
        assertNotNull(schedule.getScheduleId());
        assertEquals("SCHEDULED", schedule.getStatus());
    }

    @Test
    void createSchedule_invalidDepartureAfterArrival_throwsException() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI103", "Chennai", "Delhi"));
        FlightScheduleRequest sr = new FlightScheduleRequest();
        sr.setTravelDate(LocalDate.now().plusDays(10));
        sr.setDepartureTime(LocalTime.of(10, 0));
        sr.setArrivalTime(LocalTime.of(9, 0));

        assertThrows(InvalidBookingException.class, () -> flightService.createSchedule(flight.getFlightId(), sr));
    }

    @Test
    void createSchedule_durationAtOrBelowTwelveHours_isAccepted() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI106", "Chennai", "Delhi"));

        assertDoesNotThrow(() -> flightService.createSchedule(flight.getFlightId(), scheduleRequest(LocalTime.of(6, 0), LocalTime.of(8, 0))));
        FlightScheduleRequest twelveHourRequest = scheduleRequest(LocalTime.of(6, 0), LocalTime.of(18, 0));
        twelveHourRequest.setTravelDate(LocalDate.now().plusDays(11));
        assertDoesNotThrow(() -> flightService.createSchedule(flight.getFlightId(), twelveHourRequest));
    }

    @Test
    void createSchedule_durationOverTwelveHours_isRejected() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI107", "Chennai", "Delhi"));
        InvalidBookingException exception = assertThrows(InvalidBookingException.class,
                () -> flightService.createSchedule(flight.getFlightId(), scheduleRequest(LocalTime.of(6, 0), LocalTime.of(18, 1))));
        assertEquals("Flight duration cannot exceed 12 hours.", exception.getMessage());
    }

    @Test
    void createSchedule_equalOrReverseTimes_areRejected() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI108", "Chennai", "Delhi"));
        assertThrows(InvalidBookingException.class, () -> flightService.createSchedule(flight.getFlightId(), scheduleRequest(LocalTime.of(10, 0), LocalTime.of(10, 0))));
        assertThrows(InvalidBookingException.class, () -> flightService.createSchedule(flight.getFlightId(), scheduleRequest(LocalTime.of(12, 0), LocalTime.of(10, 0))));
    }

    @Test
    void createSchedule_todayPastDeparture_isRejected() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI109", "Chennai", "Delhi"));
        FlightScheduleRequest request = scheduleRequest(LocalTime.now().minusMinutes(1), LocalTime.now().plusHours(1));
        request.setTravelDate(LocalDate.now());
        assertThrows(InvalidBookingException.class,
                () -> flightService.createSchedule(flight.getFlightId(), request));
    }

    private FlightScheduleRequest scheduleRequest(LocalTime departure, LocalTime arrival) {
        FlightScheduleRequest request = new FlightScheduleRequest();
        request.setTravelDate(LocalDate.now().plusDays(10));
        request.setDepartureTime(departure);
        request.setArrivalTime(arrival);
        return request;
    }

    @Test
    void createSchedule_duplicateSchedule_throwsException() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI104", "Chennai", "Delhi"));
        FlightScheduleRequest sr = new FlightScheduleRequest();
        sr.setTravelDate(LocalDate.now().plusDays(10));
        sr.setDepartureTime(LocalTime.of(6, 30));
        sr.setArrivalTime(LocalTime.of(9, 15));

        flightService.createSchedule(flight.getFlightId(), sr);
        assertThrows(DuplicateResourceException.class, () -> flightService.createSchedule(flight.getFlightId(), sr));
    }

    @Test
    void search_returnsMatchingScheduledFlights_onlyFutureAndAvailable() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI105", "Chennai", "Delhi"));
        LocalDate travelDate = LocalDate.now().plusDays(15);

        FlightScheduleRequest sr = new FlightScheduleRequest();
        sr.setTravelDate(travelDate);
        sr.setDepartureTime(LocalTime.of(6, 30));
        sr.setArrivalTime(LocalTime.of(9, 15));
        flightService.createSchedule(flight.getFlightId(), sr);

        List<FlightSearchResponse> results = flightService.search("Chennai", "Delhi", travelDate);

        assertEquals(1, results.size());
        assertEquals("AI105", results.get(0).getFlightNumber());
        assertEquals(100, results.get(0).getAvailableEconomySeats());
    }

    @Test
    void search_noMatchingRoute_returnsEmpty() {
        List<FlightSearchResponse> results = flightService.search("Mumbai", "Kolkata", LocalDate.now().plusDays(5));
        assertTrue(results.isEmpty());
    }

    @Test
    void recurringMondaySchedule_isReturnedOnlyForMatchingMondays() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI110", "Hyderabad", "Bengaluru"));
        LocalDate firstMonday = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        if (firstMonday.equals(LocalDate.now()) && LocalTime.now().isAfter(LocalTime.of(6, 0))) {
            firstMonday = firstMonday.plusWeeks(1);
        }

        FlightScheduleTemplateRequest template = new FlightScheduleTemplateRequest();
        template.setOperatingDays(Set.of(DayOfWeek.MONDAY));
        template.setEffectiveFrom(firstMonday);
        template.setDepartureTime(LocalTime.of(6, 0));
        template.setArrivalTime(LocalTime.of(7, 30));
        flightService.createScheduleTemplate(flight.getFlightId(), template);

        assertEquals(1, flightService.search("Hyderabad", "Bengaluru", firstMonday).size());
        assertEquals(1, flightService.search("Hyderabad", "Bengaluru", firstMonday.plusWeeks(1)).size());
        assertTrue(flightService.search("Hyderabad", "Bengaluru", firstMonday.plusDays(1)).isEmpty());
        assertTrue(flightService.search("Mumbai", "Delhi", firstMonday).isEmpty());
    }

    @Test
    void search_sameOriginDestination_throwsException() {
        assertThrows(InvalidBookingException.class,
                () -> flightService.search("Chennai", "Chennai", LocalDate.now().plusDays(5)));
    }

    @Test
    void createScheduleTemplate_durationAtOrBelowTwelveHours_isAccepted() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI111", "Hyderabad", "Bengaluru"));

        assertDoesNotThrow(() -> flightService.createScheduleTemplate(flight.getFlightId(), scheduleTemplateRequest(LocalTime.of(8, 0), LocalTime.of(20, 0))));
    }

    @Test
    void createScheduleTemplate_invalidOrOverTwelveHourDuration_isRejected() {
        Flight flight = flightService.createFlight(buildFlightRequest("AI112", "Hyderabad", "Bengaluru"));

        assertEquals("Departure time must be before arrival time.", assertThrows(InvalidBookingException.class,
                () -> flightService.createScheduleTemplate(flight.getFlightId(), scheduleTemplateRequest(LocalTime.of(8, 0), LocalTime.of(8, 0)))).getMessage());
        assertEquals("Departure time must be before arrival time.", assertThrows(InvalidBookingException.class,
                () -> flightService.createScheduleTemplate(flight.getFlightId(), scheduleTemplateRequest(LocalTime.of(23, 0), LocalTime.of(2, 0)))).getMessage());
        assertEquals("Flight duration cannot exceed 12 hours.", assertThrows(InvalidBookingException.class,
                () -> flightService.createScheduleTemplate(flight.getFlightId(), scheduleTemplateRequest(LocalTime.of(8, 0), LocalTime.of(20, 1)))).getMessage());
    }

    private FlightScheduleTemplateRequest scheduleTemplateRequest(LocalTime departure, LocalTime arrival) {
        FlightScheduleTemplateRequest request = new FlightScheduleTemplateRequest();
        request.setOperatingDays(Set.of(DayOfWeek.MONDAY));
        request.setEffectiveFrom(LocalDate.now().plusDays(1));
        request.setDepartureTime(departure);
        request.setArrivalTime(arrival);
        return request;
    }
}
