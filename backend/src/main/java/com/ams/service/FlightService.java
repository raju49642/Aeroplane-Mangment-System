package com.ams.service;

import com.ams.dto.FlightRequest;
import com.ams.dto.FlightScheduleRequest;
import com.ams.dto.FlightSearchResponse;
import com.ams.dto.FlightResponse;
import com.ams.dto.FlightScheduleResponse;
import com.ams.dto.SuggestedFlightNumberResponse;
import com.ams.dto.FlightScheduleTemplateRequest;
import com.ams.dto.FlightScheduleTemplateResponse;
import com.ams.entity.Flight;
import com.ams.entity.FlightSchedule;

import java.time.LocalDate;
import java.util.List;

public interface FlightService {

    Flight createFlight(FlightRequest request);

    List<Flight> getAllFlights();

    Flight getFlightById(Integer flightId);

    List<Flight> getFlightsByCarrierName(String carrierName);

    Flight updateFlight(Integer flightId, FlightRequest request);

    void deleteFlight(Integer flightId);

    FlightSchedule createSchedule(Integer flightId, FlightScheduleRequest request);

    List<FlightSearchResponse> search(String origin, String destination, LocalDate travelDate);
    List<FlightSearchResponse> scheduled(String origin, String destination, LocalDate fromDate, LocalDate toDate);

    // DTO methods to avoid Hibernate proxy serialization issues
    FlightResponse createFlightResponse(FlightRequest request);

    List<FlightResponse> getAllFlightsAsResponse();

    FlightResponse getFlightByIdAsResponse(Integer flightId);

    List<FlightResponse> getFlightsByCarrierNameAsResponse(String carrierName);

    FlightResponse updateFlightResponse(Integer flightId, FlightRequest request);

    FlightScheduleResponse createScheduleResponse(Integer flightId, FlightScheduleRequest request);
    List<FlightScheduleResponse> getSchedules(Integer flightId);
    FlightScheduleResponse updateSchedule(Integer scheduleId, FlightScheduleRequest request);
    SuggestedFlightNumberResponse suggestFlightNumber(Integer carrierId);
    FlightScheduleTemplateResponse createScheduleTemplate(Integer flightId, FlightScheduleTemplateRequest request);
    List<FlightScheduleTemplateResponse> getScheduleTemplates(Integer flightId);
    FlightScheduleTemplateResponse updateScheduleTemplate(Integer templateId, FlightScheduleTemplateRequest request);
    FlightScheduleTemplateResponse deactivateScheduleTemplate(Integer templateId);
}
