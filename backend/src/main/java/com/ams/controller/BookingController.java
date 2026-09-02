package com.ams.controller;

import com.ams.dto.BookingRequest;
import com.ams.dto.BookingResponse;
import com.ams.dto.CancellationPreviewResponse;
import com.ams.dto.BookingPaymentRequest;
import com.ams.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> bookFlight(@Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.bookFlight(request));
    }

    @PostMapping("/{bookingId}/pay")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingResponse> payForBooking(@PathVariable Integer bookingId, @Valid @RequestBody BookingPaymentRequest request) {
        return ResponseEntity.ok(bookingService.payForBooking(bookingId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @GetMapping("/{bookingId}/cancellation-preview")
    public ResponseEntity<CancellationPreviewResponse> previewCancellation(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.previewCancellation(bookingId));
    }

    @PutMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Integer bookingId) {
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId));
    }
}
