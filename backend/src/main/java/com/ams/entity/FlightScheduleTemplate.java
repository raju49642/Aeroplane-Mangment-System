package com.ams.entity;
import jakarta.persistence.*;
import java.time.*;
import java.util.*;
@Entity @Table(name = "flight_schedule_templates")
public class FlightScheduleTemplate {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer scheduleTemplateId;
 @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "flight_id", nullable = false) private Flight flight;
 @Column(nullable = false) private LocalTime departureTime;
 @Column(nullable = false) private LocalTime arrivalTime;
 @ElementCollection(targetClass = DayOfWeek.class) @CollectionTable(name = "flight_schedule_template_days", joinColumns = @JoinColumn(name = "schedule_template_id")) @Enumerated(EnumType.STRING) @Column(name = "operating_day", nullable = false)
 private Set<DayOfWeek> operatingDays = EnumSet.noneOf(DayOfWeek.class);
 @Column(nullable = false) private LocalDate effectiveFrom;
 private LocalDate effectiveTo;
 @Column(nullable = false) private boolean active = true;
 public Integer getScheduleTemplateId(){return scheduleTemplateId;} public void setScheduleTemplateId(Integer v){scheduleTemplateId=v;}
 public Flight getFlight(){return flight;} public void setFlight(Flight v){flight=v;}
 public LocalTime getDepartureTime(){return departureTime;} public void setDepartureTime(LocalTime v){departureTime=v;}
 public LocalTime getArrivalTime(){return arrivalTime;} public void setArrivalTime(LocalTime v){arrivalTime=v;}
 public Set<DayOfWeek> getOperatingDays(){return operatingDays;} public void setOperatingDays(Set<DayOfWeek> v){operatingDays=v;}
 public LocalDate getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(LocalDate v){effectiveFrom=v;}
 public LocalDate getEffectiveTo(){return effectiveTo;} public void setEffectiveTo(LocalDate v){effectiveTo=v;}
 public boolean isActive(){return active;} public void setActive(boolean v){active=v;}
}
