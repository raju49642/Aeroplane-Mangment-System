package com.ams.repository;
import com.ams.entity.FlightScheduleTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FlightScheduleTemplateRepository extends JpaRepository<FlightScheduleTemplate, Integer> {
    List<FlightScheduleTemplate> findByFlightFlightId(Integer flightId);
    boolean existsByFlightFlightId(Integer flightId);
}
