package com.ams.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class BookingPaymentRequest {
    @NotBlank @Pattern(regexp = "UPI|CREDIT_CARD", message = "paymentMethod must be UPI or CREDIT_CARD")
    private String paymentMethod;
    private String cardHolderName;
    private String cardNumber;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String cvv;
    public String getPaymentMethod() { return paymentMethod; } public void setPaymentMethod(String v) { paymentMethod = v; }
    public String getCardHolderName() { return cardHolderName; } public void setCardHolderName(String v) { cardHolderName = v; }
    public String getCardNumber() { return cardNumber; } public void setCardNumber(String v) { cardNumber = v; }
    public Integer getExpiryMonth() { return expiryMonth; } public void setExpiryMonth(Integer v) { expiryMonth = v; }
    public Integer getExpiryYear() { return expiryYear; } public void setExpiryYear(Integer v) { expiryYear = v; }
    public String getCvv() { return cvv; } public void setCvv(String v) { cvv = v; }
}
