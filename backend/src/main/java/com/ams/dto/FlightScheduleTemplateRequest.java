package com.ams.dto;
import com.ams.validation.NotPastDate;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.Set;
public class FlightScheduleTemplateRequest {
 @NotNull private LocalTime departureTime; @NotNull private LocalTime arrivalTime;
 @NotEmpty(message="operatingDays must contain at least one day") private Set<DayOfWeek> operatingDays;
 @NotNull @NotPastDate(message="effectiveFrom cannot be in the past") private LocalDate effectiveFrom;
 private LocalDate effectiveTo;
 private Boolean active = true;
 public LocalTime getDepartureTime(){return departureTime;} public void setDepartureTime(LocalTime v){departureTime=v;}
 public LocalTime getArrivalTime(){return arrivalTime;} public void setArrivalTime(LocalTime v){arrivalTime=v;}
 public Set<DayOfWeek> getOperatingDays(){return operatingDays;} public void setOperatingDays(Set<DayOfWeek> v){operatingDays=v;}
 public LocalDate getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(LocalDate v){effectiveFrom=v;}
 public LocalDate getEffectiveTo(){return effectiveTo;} public void setEffectiveTo(LocalDate v){effectiveTo=v;}
 public Boolean getActive(){return active;} public void setActive(Boolean v){active=v;}
}
