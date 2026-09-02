package com.ams.service;

import com.ams.dto.BookingRequest;
import com.ams.dto.BookingResponse;
import com.ams.dto.CancellationPreviewResponse;
import com.ams.dto.BookingPaymentRequest;

import java.util.List;

public interface BookingService {

    BookingResponse bookFlight(BookingRequest request);
    BookingResponse payForBooking(Integer bookingId, BookingPaymentRequest request);

    List<BookingResponse> getAllBookings();

    BookingResponse getBookingById(Integer bookingId);

    List<BookingResponse> getMyBookings();

    BookingResponse cancelBooking(Integer bookingId);
    CancellationPreviewResponse previewCancellation(Integer bookingId);
}
