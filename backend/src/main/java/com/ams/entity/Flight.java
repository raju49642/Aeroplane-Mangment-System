package com.ams.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id")
    private Integer flightId;

    @Column(name = "flight_number", nullable = false, unique = true)
    private String flightNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    @Column(name = "origin", nullable = false)
    private String origin;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "base_fare", nullable = false)
    private Double baseFare;

    @Column(name = "seat_capacity_business_class")
    private Integer seatCapacityBusinessClass;

    @Column(name = "seat_capacity_economy_class")
    private Integer seatCapacityEconomyClass;

    @Column(name = "seat_capacity_executive_class")
    private Integer seatCapacityExecutiveClass;

    public Flight() {
    }

    public Integer getFlightId() { return flightId; }
    public void setFlightId(Integer flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public Carrier getCarrier() { return carrier; }
    public void setCarrier(Carrier carrier) { this.carrier = carrier; }

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
