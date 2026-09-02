package com.ams.repository;

import com.ams.entity.FlightSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface FlightScheduleRepository extends JpaRepository<FlightSchedule, Integer> {

    boolean existsByFlightFlightId(Integer flightId);

    boolean existsByFlightFlightIdAndTravelDateAndDepartureTime(Integer flightId, LocalDate travelDate, LocalTime departureTime);
    List<FlightSchedule> findByFlightFlightIdOrderByTravelDateAscDepartureTimeAsc(Integer flightId);
    List<FlightSchedule> findByScheduleTemplateScheduleTemplateIdAndTravelDateGreaterThanEqual(Integer templateId, LocalDate travelDate);
    long countByTravelDateAndStatus(LocalDate travelDate, String status);

    @Query("SELECT fs FROM FlightSchedule fs " +
           "WHERE LOWER(fs.flight.origin) IN :origins " +
           "AND LOWER(fs.flight.destination) IN :destinations " +
           "AND fs.travelDate = :travelDate " +
           "AND fs.status = 'SCHEDULED'")
    List<FlightSchedule> searchByRouteAndDate(
            @Param("origins") List<String> origins,
            @Param("destinations") List<String> destinations,
            @Param("travelDate") LocalDate travelDate);

    @Query("SELECT fs FROM FlightSchedule fs WHERE LOWER(fs.flight.origin) IN :origins AND LOWER(fs.flight.destination) IN :destinations AND fs.travelDate BETWEEN :fromDate AND :toDate AND fs.status = 'SCHEDULED' ORDER BY fs.travelDate, fs.departureTime")
    List<FlightSchedule> searchByRouteAndDateRange(@Param("origins") List<String> origins, @Param("destinations") List<String> destinations, @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // Pessimistic write lock: used when booking, so two concurrent requests for the
    // same schedule serialize on this row instead of both reading a stale seat count.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fs FROM FlightSchedule fs WHERE fs.scheduleId = :scheduleId")
    Optional<FlightSchedule> findByIdForUpdate(@Param("scheduleId") Integer scheduleId);
}
