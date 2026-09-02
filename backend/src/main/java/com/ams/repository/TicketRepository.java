package com.ams.repository;
import com.ams.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TicketRepository extends JpaRepository<Ticket, Integer> { Optional<Ticket> findByTicketNumber(String ticketNumber); List<Ticket> findByBookingBookingId(Integer bookingId); List<Ticket> findByUserUserId(Integer userId); boolean existsByTicketNumber(String ticketNumber); }
