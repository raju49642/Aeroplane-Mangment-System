package com.ams.repository;

import com.ams.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Integer> {

    List<Flight> findByCarrierCarrierName(String carrierName);
    List<Flight> findByCarrierCarrierId(Integer carrierId);
    boolean existsByCarrierCarrierId(Integer carrierId);

    boolean existsByFlightNumberIgnoreCase(String flightNumber);

    Optional<Flight> findByFlightNumberIgnoreCase(String flightNumber);
}
