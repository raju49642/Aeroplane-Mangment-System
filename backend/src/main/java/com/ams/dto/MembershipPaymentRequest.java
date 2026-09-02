package com.ams.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public class MembershipPaymentRequest {
 @NotBlank @Pattern(regexp="SILVER|GOLD|PLATINUM") private String customerCategory;
 @NotBlank @Pattern(regexp="CREDIT_CARD|UPI") private String paymentMethod;
 @Pattern(regexp="^\\d{13,19}$", message="cardNumber must contain 13-19 digits") private String cardNumber;
 private String cardHolderName; private Integer expiryMonth; private Integer expiryYear;
 @Pattern(regexp="^\\d{3,4}$", message="cvv must contain 3-4 digits") private String cvv;
 public String getCustomerCategory(){return customerCategory;} public void setCustomerCategory(String v){customerCategory=v;}
 public String getPaymentMethod(){return paymentMethod;} public void setPaymentMethod(String v){paymentMethod=v;}
 public String getCardNumber(){return cardNumber;} public void setCardNumber(String v){cardNumber=v;}
 public String getCardHolderName(){return cardHolderName;} public void setCardHolderName(String v){cardHolderName=v;}
 public Integer getExpiryMonth(){return expiryMonth;} public void setExpiryMonth(Integer v){expiryMonth=v;}
 public Integer getExpiryYear(){return expiryYear;} public void setExpiryYear(Integer v){expiryYear=v;}
 public String getCvv(){return cvv;} public void setCvv(String v){cvv=v;}
}
