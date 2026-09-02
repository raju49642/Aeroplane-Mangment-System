package com.ams.dto;

import com.ams.validation.NotPastDate;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class FlightScheduleRequest {

    @NotNull(message = "travelDate is required")
    @NotPastDate(message = "travelDate cannot be in the past")
    private LocalDate travelDate;

    @NotNull(message = "departureTime is required")
    private LocalTime departureTime;

    @NotNull(message = "arrivalTime is required")
    private LocalTime arrivalTime;

    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
}
