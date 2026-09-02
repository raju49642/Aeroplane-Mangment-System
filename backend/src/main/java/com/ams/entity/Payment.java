package com.ams.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private User user;
    @Column(nullable = false) private String customerCategory;
    @Column(nullable = false) private Double amount;
    @Column(nullable = false) private String paymentMethod;
    private String maskedCardNumber;
    private String upiTransactionRef;
    @Column(nullable = false) private String status;
    private LocalDateTime paidAt;
    @OneToOne @JoinColumn(name = "booking_id", unique = true)
    private Booking booking;
    public Integer getPaymentId() { return paymentId; }
    public User getUser() { return user; } public void setUser(User v) { user = v; }
    public String getCustomerCategory() { return customerCategory; } public void setCustomerCategory(String v) { customerCategory = v; }
    public Double getAmount() { return amount; } public void setAmount(Double v) { amount = v; }
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String v) { paymentMethod = v; }
    public String getMaskedCardNumber() { return maskedCardNumber; } public void setMaskedCardNumber(String v) { maskedCardNumber = v; }
    public String getUpiTransactionRef() { return upiTransactionRef; } public void setUpiTransactionRef(String v) { upiTransactionRef = v; }
    public String getStatus() { return status; } public void setStatus(String v) { status = v; }
    public LocalDateTime getPaidAt() { return paidAt; } public void setPaidAt(LocalDateTime v) { paidAt = v; }
    public Booking getBooking() { return booking; } public void setBooking(Booking v) { booking = v; }
}
