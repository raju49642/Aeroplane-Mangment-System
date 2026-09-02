package com.ams.dto;

public class FlightResponse {
    private Integer flightId;
    private String flightNumber;
    private String carrierName;
    private Integer carrierId;
    private String origin;
    private String destination;
    private Double baseFare;
    private Integer seatCapacityBusinessClass;
    private Integer seatCapacityEconomyClass;
    private Integer seatCapacityExecutiveClass;

    // Constructors
    public FlightResponse() {}

    public FlightResponse(Integer flightId, String flightNumber, String carrierName,
                         Integer carrierId, String origin, String destination,
                         Double baseFare, Integer seatCapacityBusinessClass,
                         Integer seatCapacityEconomyClass, Integer seatCapacityExecutiveClass) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.carrierName = carrierName;
        this.carrierId = carrierId;
        this.origin = origin;
        this.destination = destination;
        this.baseFare = baseFare;
        this.seatCapacityBusinessClass = seatCapacityBusinessClass;
        this.seatCapacityEconomyClass = seatCapacityEconomyClass;
        this.seatCapacityExecutiveClass = seatCapacityExecutiveClass;
    }

    // Getters and Setters
    public Integer getFlightId() { return flightId; }
    public void setFlightId(Integer flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }

    public Integer getCarrierId() { return carrierId; }
    public void setCarrierId(Integer carrierId) { this.carrierId = carrierId; }

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
