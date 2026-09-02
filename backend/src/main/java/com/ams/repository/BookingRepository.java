package com.ams.repository;

import com.ams.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByUserUserId(Integer userId);
    boolean existsByTicketNumber(String ticketNumber);
}
