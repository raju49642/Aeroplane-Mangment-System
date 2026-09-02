package com.ams.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "flight_schedules", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"flight_id", "travel_date", "departure_time"})
})
public class FlightSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Integer scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_template_id")
    private FlightScheduleTemplate scheduleTemplate;

    @Column(name = "travel_date", nullable = false)
    private LocalDate travelDate;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalTime arrivalTime;

    @Column(name = "booked_business_seats", nullable = false)
    private Integer bookedBusinessSeats = 0;

    @Column(name = "booked_economy_seats", nullable = false)
    private Integer bookedEconomySeats = 0;

    @Column(name = "booked_executive_seats", nullable = false)
    private Integer bookedExecutiveSeats = 0;

    // SCHEDULED | CANCELLED | COMPLETED
    @Column(name = "status", nullable = false)
    private String status = "SCHEDULED";

    // Optimistic lock: prevents two concurrent bookings from both reading the
    // same "available seats" snapshot and both succeeding when only one should.
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public FlightSchedule() {
    }

    public Integer getScheduleId() { return scheduleId; }
    public void setScheduleId(Integer scheduleId) { this.scheduleId = scheduleId; }

    public Flight getFlight() { return flight; }
    public void setFlight(Flight flight) { this.flight = flight; }
    public FlightScheduleTemplate getScheduleTemplate() { return scheduleTemplate; }
    public void setScheduleTemplate(FlightScheduleTemplate scheduleTemplate) { this.scheduleTemplate = scheduleTemplate; }

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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
