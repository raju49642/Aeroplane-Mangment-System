package com.ams.dto;

import jakarta.validation.constraints.*;

public class FlightRequest {

    @NotNull(message = "carrierId is required")
    private Integer carrierId;

    @NotBlank(message = "flightNumber is required")
    @Pattern(regexp = "^[A-Z]{2}\\d{2,3}$", message = "flightNumber must be 2 uppercase letters followed by 2 or 3 digits")
    private String flightNumber;

    @NotBlank(message = "origin is required")
    private String origin;

    @NotBlank(message = "destination is required")
    private String destination;

    @NotNull(message = "baseFare is required")
    @DecimalMin(value = "3000.0", message = "baseFare must be between 3,000 and 50,000")
    @DecimalMax(value = "50000.0", message = "baseFare must be between 3,000 and 50,000")
    private Double baseFare;

    @NotNull(message = "seatCapacityBusinessClass is required")
    @Min(value = 30, message = "seatCapacityBusinessClass must be between 30 and 120")
    @Max(value = 120, message = "seatCapacityBusinessClass must be between 30 and 120")
    private Integer seatCapacityBusinessClass;

    @NotNull(message = "seatCapacityEconomyClass is required")
    @Min(value = 100, message = "seatCapacityEconomyClass must be between 100 and 500")
    @Max(value = 500, message = "seatCapacityEconomyClass must be between 100 and 500")
    private Integer seatCapacityEconomyClass;

    @NotNull(message = "seatCapacityExecutiveClass is required")
    @Min(value = 25, message = "seatCapacityExecutiveClass must be between 25 and 75")
    @Max(value = 75, message = "seatCapacityExecutiveClass must be between 25 and 75")
    private Integer seatCapacityExecutiveClass;

    public Integer getCarrierId() { return carrierId; }
    public void setCarrierId(Integer carrierId) { this.carrierId = carrierId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Double getBaseFare() { return baseFare; }
    public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }
    public Integer getSeatCapacityBusinessClass() { return seatCapacityBusinessClass; }
    public void setSeatCapacityBusinessClass(Integer v) { this.seatCapacityBusinessClass = v; }
    public Integer getSeatCapacityEconomyClass() { return seatCapacityEconomyClass; }
    public void setSeatCapacityEconomyClass(Integer v) { this.seatCapacityEconomyClass = v; }
    public Integer getSeatCapacityExecutiveClass() { return seatCapacityExecutiveClass; }
    public void setSeatCapacityExecutiveClass(Integer v) { this.seatCapacityExecutiveClass = v; }
}
