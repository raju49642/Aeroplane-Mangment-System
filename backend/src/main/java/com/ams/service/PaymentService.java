package com.ams.service;
import com.ams.dto.*; import java.util.List;
public interface PaymentService { PaymentResponse pay(MembershipPaymentRequest request); PaymentResponse confirmUpi(Integer paymentId); List<PaymentResponse> getMyPayments(); }
