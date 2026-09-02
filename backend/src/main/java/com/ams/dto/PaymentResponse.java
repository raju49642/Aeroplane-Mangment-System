package com.ams.dto;
import java.time.LocalDateTime;
public record PaymentResponse(Integer paymentId, String customerCategory, Double amount, String paymentMethod, String maskedCardNumber, String upiTransactionRef, String status, LocalDateTime paidAt, String qrCodePayload) { }
