package com.ams.dto;
import java.time.LocalDate; import java.time.LocalDateTime; import java.time.LocalTime;
public class TicketResponse {
 public Integer ticketId, bookingId, userId, passengerId, flightId, carrierId, paymentId; public String ticketNumber, passengerName, carrierName, carrierCode, flightNumber, origin, destination, seatCategory, paymentStatus, ticketStatus; public Integer passengerAge; public LocalDate travelDate; public LocalTime departureTime, arrivalTime; public Integer numberOfSeats; public Double baseFare, discountAmount, paidAmount, refundAmount; public LocalDateTime issuedAt, cancelledAt;
}
