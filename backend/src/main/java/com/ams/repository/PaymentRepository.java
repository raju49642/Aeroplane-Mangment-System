package com.ams.repository;
import com.ams.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, Integer> { List<Payment> findByUserUserIdOrderByPaidAtDesc(Integer userId); Optional<Payment> findByBookingBookingId(Integer bookingId); }
