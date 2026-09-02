package com.ams.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import java.util.List;

public class BookingRequest {

    // Booking is made against a specific scheduled flight instance, never a bare flightId,
    // since the same flight can operate on many dates/times.
    @NotNull(message = "scheduleId is required")
    private Integer scheduleId;

    @NotNull(message = "noOfSeats is required")
    @Min(value = 1, message = "noOfSeats must be at least 1")
    @Max(value = 6, message = "noOfSeats must not exceed 6 per booking")
    private Integer noOfSeats;

    @NotNull(message = "seatCategory is required")
    @Pattern(regexp = "BUSINESS|ECONOMY|EXECUTIVE", message = "seatCategory must be one of BUSINESS, ECONOMY, EXECUTIVE")
    private String seatCategory;

    // Retained for compatibility with older clients; payment details are ignored and
    // must be submitted to the dedicated booking payment endpoint.
    private String paymentMethod;


    @Valid
    private List<PassengerRequest> passengers;

    // NOTE: userId is intentionally NOT part of this DTO. The booking owner is always
    // derived from the authenticated principal (Spring Security context), never from
    // client-supplied input, to prevent booking-on-behalf-of-another-user attacks.

    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }
    public Integer getNoOfSeats() { return noOfSeats; }
    public void setNoOfSeats(Integer noOfSeats) { this.noOfSeats = noOfSeats; }
    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public List<PassengerRequest> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerRequest> passengers) { this.passengers = passengers; }
}
