package com.ams.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Integer bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private FlightSchedule flightSchedule;

    @Column(name = "no_of_seats", nullable = false)
    private Integer noOfSeats;

    @Column(name = "seat_category", nullable = false)
    private String seatCategory;

    // Server-generated timestamp of when the booking was made (distinct from travelDate).
    @Column(name = "booking_date_time", nullable = false)
    private LocalDateTime bookingDateTime;

    @Column(name = "booking_status", nullable = false)
    private String bookingStatus;

    @Column(name = "booking_amount", nullable = false)
    private Double bookingAmount;

    @Column(name = "ticket_number", unique = true)
    private String ticketNumber;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = false)
    @OrderBy("passengerId ASC")
    private List<Passenger> passengers = new ArrayList<>();

    public Booking() {
    }

    public Integer getBookingId() { return bookingId; }
    public void setBookingId(Integer bookingId) { this.bookingId = bookingId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public FlightSchedule getFlightSchedule() { return flightSchedule; }
    public void setFlightSchedule(FlightSchedule flightSchedule) { this.flightSchedule = flightSchedule; }

    public Integer getNoOfSeats() { return noOfSeats; }
    public void setNoOfSeats(Integer noOfSeats) { this.noOfSeats = noOfSeats; }

    public String getSeatCategory() { return seatCategory; }
    public void setSeatCategory(String seatCategory) { this.seatCategory = seatCategory; }

    public LocalDateTime getBookingDateTime() { return bookingDateTime; }
    public void setBookingDateTime(LocalDateTime bookingDateTime) { this.bookingDateTime = bookingDateTime; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public Double getBookingAmount() { return bookingAmount; }
    public void setBookingAmount(Double bookingAmount) { this.bookingAmount = bookingAmount; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public List<Passenger> getPassengers() { return passengers; }
    public void setPassengers(List<Passenger> passengers) { this.passengers = passengers; }
}
