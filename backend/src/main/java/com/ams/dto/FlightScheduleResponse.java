package com.ams.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class FlightScheduleResponse {
    private Integer scheduleId;
    private Integer flightId;
    private String flightNumber;
    private String carrierName;
    private LocalDate travelDate;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Integer bookedBusinessSeats;
    private Integer bookedEconomySeats;
    private Integer bookedExecutiveSeats;
    private Integer totalBusinessSeats;
    private Integer totalEconomySeats;
    private Integer totalExecutiveSeats;
    private String status;
    private Long version;

    // Constructors
    public FlightScheduleResponse() {}

    public FlightScheduleResponse(Integer scheduleId, Integer flightId, String flightNumber,
                                  String carrierName, LocalDate travelDate, LocalTime departureTime,
                                  LocalTime arrivalTime, Integer bookedBusinessSeats,
                                  Integer bookedEconomySeats, Integer bookedExecutiveSeats,
                                  Integer totalBusinessSeats, Integer totalEconomySeats,
                                  Integer totalExecutiveSeats, String status, Long version) {
        this.scheduleId = scheduleId;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.carrierName = carrierName;
        this.travelDate = travelDate;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.bookedBusinessSeats = bookedBusinessSeats;
        this.bookedEconomySeats = bookedEconomySeats;
        this.bookedExecutiveSeats = bookedExecutiveSeats;
        this.totalBusinessSeats = totalBusinessSeats;
        this.totalEconomySeats = totalEconomySeats;
        this.totalExecutiveSeats = totalExecutiveSeats;
        this.status = status;
        this.version = version;
    }

    // Getters and Setters
    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public Integer getFlightId() { return flightId; }
    public void setFlightId(Integer flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }

    public LocalDate getTravelDate() { return travelDate; }
    public void setTravelDate(LocalDate travelDate) { this.travelDate = travelDate; }

    public LocalTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalTime departureTime) { this.departureTime = departureTime; }

    public LocalTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public Integer getBookedBusinessSeats() { return bookedBusinessSeats; }
    public void setBookedBusinessSeats(Integer v) { this.bookedBusinessSeats = v; }

    public Integer getBookedEconomySeats() { return bookedEconomySeats; }
    public void setBookedEconomySeats(Integer v) { this.bookedEconomySeats = v; }

    public Integer getBookedExecutiveSeats() { return bookedExecutiveSeats; }
    public void setBookedExecutiveSeats(Integer v) { this.bookedExecutiveSeats = v; }

    public Integer getTotalBusinessSeats() { return totalBusinessSeats; }
    public void setTotalBusinessSeats(Integer v) { this.totalBusinessSeats = v; }

    public Integer getTotalEconomySeats() { return totalEconomySeats; }
    public void setTotalEconomySeats(Integer v) { this.totalEconomySeats = v; }

    public Integer getTotalExecutiveSeats() { return totalExecutiveSeats; }
    public void setTotalExecutiveSeats(Integer v) { this.totalExecutiveSeats = v; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
