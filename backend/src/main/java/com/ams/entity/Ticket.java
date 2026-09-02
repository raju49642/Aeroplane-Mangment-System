package com.ams.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tickets", indexes = {@Index(name = "idx_ticket_number", columnList = "ticket_number"), @Index(name = "idx_ticket_user", columnList = "user_id")})
public class Ticket {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer ticketId;
    @Column(name = "ticket_number", nullable = false, unique = true) private String ticketNumber;
    @ManyToOne(optional = false) @JoinColumn(name = "booking_id", nullable = false) private Booking booking;
    // Nullable solely for tickets created before passenger records existed. New tickets always set it.
    @ManyToOne @JoinColumn(name = "passenger_id") private Passenger passenger;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id", nullable = false) private User user;
    @ManyToOne(optional = false) @JoinColumn(name = "payment_id", nullable = false) private Payment payment;
    @Column(nullable = false) private Integer flightId; @Column(nullable = false) private Integer carrierId;
    @Column(nullable = false) private String carrierName; @Column(nullable = false, length = 2) private String carrierCode; @Column(nullable = false) private String flightNumber;
    @Column(nullable = false) private String origin; @Column(nullable = false) private String destination; @Column(nullable = false) private LocalDate travelDate; @Column(nullable = false) private LocalTime departureTime; @Column(nullable = false) private LocalTime arrivalTime;
    @Column(nullable = false) private String seatCategory; @Column(nullable = false) private Integer numberOfSeats; @Column(nullable = false) private Double baseFare; @Column(nullable = false) private Double discountAmount; @Column(nullable = false) private Double paidAmount;
    @Column(nullable = false) private String ticketStatus; @Column(nullable = false) private LocalDateTime issuedAt; private Double refundAmount; private LocalDateTime cancelledAt;
    public Integer getTicketId(){return ticketId;} public String getTicketNumber(){return ticketNumber;} public void setTicketNumber(String v){ticketNumber=v;} public Booking getBooking(){return booking;} public void setBooking(Booking v){booking=v;} public Passenger getPassenger(){return passenger;} public void setPassenger(Passenger v){passenger=v;} public User getUser(){return user;} public void setUser(User v){user=v;} public Payment getPayment(){return payment;} public void setPayment(Payment v){payment=v;}
    public Integer getFlightId(){return flightId;} public void setFlightId(Integer v){flightId=v;} public Integer getCarrierId(){return carrierId;} public void setCarrierId(Integer v){carrierId=v;} public String getCarrierName(){return carrierName;} public void setCarrierName(String v){carrierName=v;} public String getCarrierCode(){return carrierCode;} public void setCarrierCode(String v){carrierCode=v;} public String getFlightNumber(){return flightNumber;} public void setFlightNumber(String v){flightNumber=v;} public String getOrigin(){return origin;} public void setOrigin(String v){origin=v;} public String getDestination(){return destination;} public void setDestination(String v){destination=v;} public LocalDate getTravelDate(){return travelDate;} public void setTravelDate(LocalDate v){travelDate=v;} public LocalTime getDepartureTime(){return departureTime;} public void setDepartureTime(LocalTime v){departureTime=v;} public LocalTime getArrivalTime(){return arrivalTime;} public void setArrivalTime(LocalTime v){arrivalTime=v;} public String getSeatCategory(){return seatCategory;} public void setSeatCategory(String v){seatCategory=v;} public Integer getNumberOfSeats(){return numberOfSeats;} public void setNumberOfSeats(Integer v){numberOfSeats=v;} public Double getBaseFare(){return baseFare;} public void setBaseFare(Double v){baseFare=v;} public Double getDiscountAmount(){return discountAmount;} public void setDiscountAmount(Double v){discountAmount=v;} public Double getPaidAmount(){return paidAmount;} public void setPaidAmount(Double v){paidAmount=v;} public String getTicketStatus(){return ticketStatus;} public void setTicketStatus(String v){ticketStatus=v;} public LocalDateTime getIssuedAt(){return issuedAt;} public void setIssuedAt(LocalDateTime v){issuedAt=v;} public Double getRefundAmount(){return refundAmount;} public void setRefundAmount(Double v){refundAmount=v;} public LocalDateTime getCancelledAt(){return cancelledAt;} public void setCancelledAt(LocalDateTime v){cancelledAt=v;}
}
