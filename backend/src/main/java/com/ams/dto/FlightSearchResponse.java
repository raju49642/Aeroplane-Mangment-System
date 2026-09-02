package com.ams.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class FlightSearchResponse {
    private Integer scheduleId;
    private Integer flightId;
    private String flightNumber;
    private String carrierName;
    private String origin;
    private String destination;
    private LocalDate travelDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Double baseFare;
    private Double economyFare;
    private Double businessFare;
    private Double executiveFare;
    private Integer availableBusinessSeats;
    private Integer availableEconomySeats;
    private Integer availableExecutiveSeats;

    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }
    public Integer getFlightId() { return flightId; }
    public void setFlightId(Integer flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }
    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }
    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public Double getBaseFare() { return baseFare; }
    public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }
    public Double getEconomyFare() { return economyFare; }
    public void setEconomyFare(Double economyFare) { this.economyFare = economyFare; }
    public Double getBusinessFare() { return businessFare; }
    public void setBusinessFare(Double businessFare) { this.businessFare = businessFare; }
    public Double getExecutiveFare() { return executiveFare; }
    public void setExecutiveFare(Double executiveFare) { this.executiveFare = executiveFare; }
    public Integer getAvailableBusinessSeats() { return availableBusinessSeats; }
    public void setAvailableBusinessSeats(Integer v) { this.availableBusinessSeats = v; }
    public Integer getAvailableEconomySeats() { return availableEconomySeats; }
    public void setAvailableEconomySeats(Integer v) { this.availableEconomySeats = v; }
    public Integer getAvailableExecutiveSeats() { return availableExecutiveSeats; }
    public void setAvailableExecutiveSeats(Integer v) { this.availableExecutiveSeats = v; }
}
