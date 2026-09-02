package com.ams.controller;

import com.ams.dto.CarrierRequest;
import com.ams.dto.CarrierResponse;
import com.ams.entity.Carrier;
import com.ams.service.CarrierService;
import com.ams.service.FlightService;
import com.ams.dto.SuggestedFlightNumberResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
public class CarrierController {

    private final CarrierService carrierService;
    private final FlightService flightService;

    public CarrierController(CarrierService carrierService, FlightService flightService) {
        this.carrierService = carrierService;
        this.flightService = flightService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CarrierResponse> createCarrier(@Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carrierService.createCarrierResponse(request));
    }

    @GetMapping
    public ResponseEntity<List<CarrierResponse>> getAllCarriers() {
        return ResponseEntity.ok(carrierService.getAllCarriersAsResponse());
    }

    @GetMapping("/{carrierId}")
    public ResponseEntity<CarrierResponse> getCarrierById(@PathVariable Integer carrierId) {
        return ResponseEntity.ok(carrierService.getCarrierByIdAsResponse(carrierId));
    }

    @PutMapping("/{carrierId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<CarrierResponse> updateCarrier(@PathVariable Integer carrierId, @Valid @RequestBody CarrierRequest request) {
        return ResponseEntity.ok(carrierService.updateCarrierResponse(carrierId, request));
    }

    @DeleteMapping("/{carrierId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCarrier(@PathVariable Integer carrierId) {
        carrierService.deleteCarrier(carrierId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{carrierId}/suggested-flight-number")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<SuggestedFlightNumberResponse> suggestedFlightNumber(@PathVariable Integer carrierId) {
        return ResponseEntity.ok(flightService.suggestFlightNumber(carrierId));
    }
}
