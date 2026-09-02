package com.ams.controller;

import com.ams.dto.FlightRequest;
import com.ams.dto.FlightScheduleRequest;
import com.ams.dto.FlightSearchResponse;
import com.ams.dto.FlightResponse;
import com.ams.dto.FlightScheduleResponse;
import com.ams.dto.FlightScheduleTemplateRequest;
import com.ams.dto.FlightScheduleTemplateResponse;
import com.ams.entity.Flight;
import com.ams.entity.FlightSchedule;
import com.ams.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> createFlight(@Valid @RequestBody FlightRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createFlightResponse(request));
    }

    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        return ResponseEntity.ok(flightService.getAllFlightsAsResponse());
    }

    @GetMapping("/{flightId:\\d+}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Integer flightId) {
        return ResponseEntity.ok(flightService.getFlightByIdAsResponse(flightId));
    }

    @GetMapping("/carrier/{carrierName}")
    public ResponseEntity<List<FlightResponse>> getFlightsByCarrierName(@PathVariable String carrierName) {
        return ResponseEntity.ok(flightService.getFlightsByCarrierNameAsResponse(carrierName));
    }

    @PutMapping("/{flightId:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightResponse> updateFlight(@PathVariable Integer flightId, @Valid @RequestBody FlightRequest request) {
        return ResponseEntity.ok(flightService.updateFlightResponse(flightId, request));
    }

    @DeleteMapping("/{flightId:\\d+}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable Integer flightId) {
        flightService.deleteFlight(flightId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{flightId:\\d+}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleResponse> createSchedule(@PathVariable Integer flightId,
                                                           @Valid @RequestBody FlightScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createScheduleResponse(flightId, request));
    }
    @GetMapping("/{flightId:\\d+}/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<FlightScheduleResponse>> getSchedules(@PathVariable Integer flightId) {
        return ResponseEntity.ok(flightService.getSchedules(flightId));
    }
    @PutMapping("/schedules/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleResponse> updateSchedule(@PathVariable Integer scheduleId, @Valid @RequestBody FlightScheduleRequest request) {
        return ResponseEntity.ok(flightService.updateSchedule(scheduleId, request));
    }

    @PostMapping("/{flightId:\\d+}/schedule-templates")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleTemplateResponse> createScheduleTemplate(@PathVariable Integer flightId, @Valid @RequestBody FlightScheduleTemplateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(flightService.createScheduleTemplate(flightId, request));
    }
    @GetMapping("/{flightId:\\d+}/schedule-templates")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<FlightScheduleTemplateResponse>> getScheduleTemplates(@PathVariable Integer flightId) { return ResponseEntity.ok(flightService.getScheduleTemplates(flightId)); }
    @PutMapping("/schedule-templates/{templateId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleTemplateResponse> updateScheduleTemplate(@PathVariable Integer templateId, @Valid @RequestBody FlightScheduleTemplateRequest request) { return ResponseEntity.ok(flightService.updateScheduleTemplate(templateId, request)); }
    @PutMapping("/schedule-templates/{templateId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<FlightScheduleTemplateResponse> deactivateScheduleTemplate(@PathVariable Integer templateId) { return ResponseEntity.ok(flightService.deactivateScheduleTemplate(templateId)); }

    /**
     * Real, database-backed flight search. This is the primary customer-facing
     * booking entry point — customers search by route + date, never by flightId.
     */
    @GetMapping("/search")
    public ResponseEntity<List<FlightSearchResponse>> search(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate) {
        return ResponseEntity.ok(flightService.search(origin, destination, travelDate));
    }

    @GetMapping("/scheduled")
    public ResponseEntity<List<FlightSearchResponse>> scheduled(@RequestParam String origin, @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(flightService.scheduled(origin, destination, fromDate, toDate));
    }
}
