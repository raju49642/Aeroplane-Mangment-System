package com.ams.dto;

import jakarta.validation.constraints.*;

public class CarrierRequest {

    @NotBlank(message = "carrierName is required")
    @Size(min = 4, max = 50, message = "carrierName must be between 4 and 50 characters")
    private String carrierName;

    @NotBlank(message = "carrierCode is required")
    @Pattern(regexp = "^[A-Z]{2}$", message = "carrierCode must be exactly 2 uppercase letters")
    private String carrierCode;

    @NotNull(message = "discount30DaysAdvance is required")
    @DecimalMin(value = "10.0", message = "Enter a discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter a discount between 10% and 30%.")
    private Double discount30DaysAdvance;

    @NotNull(message = "discount60DaysAdvance is required")
    @DecimalMin(value = "10.0", message = "Enter a discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter a discount between 10% and 30%.")
    private Double discount60DaysAdvance;

    @NotNull(message = "discount90DaysAdvance is required")
    @DecimalMin(value = "10.0", message = "Enter a discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter a discount between 10% and 30%.")
    private Double discount90DaysAdvance;

    @NotNull(message = "bulkBookingDiscount is required")
    @DecimalMin(value = "10.0", message = "Enter the discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter the discount between 10% and 30%.")
    private Double bulkBookingDiscount;

    @NotNull(message = "silverUserDiscount is required")
    @DecimalMin(value = "10.0", message = "Enter the discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter the discount between 10% and 30%.")
    private Double silverUserDiscount;

    @NotNull(message = "goldUserDiscount is required")
    @DecimalMin(value = "10.0", message = "Enter the discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter the discount between 10% and 30%.")
    private Double goldUserDiscount;

    @NotNull(message = "platinumUserDiscount is required")
    @DecimalMin(value = "10.0", message = "Enter the discount between 10% and 30%.")
    @DecimalMax(value = "30.0", message = "Enter the discount between 10% and 30%.")
    private Double platinumUserDiscount;

    @NotNull(message = "refund2DaysBefore is required")
    @DecimalMin(value = "75.0", message = "Enter the refund percentage between 75% and 95%.")
    @DecimalMax(value = "95.0", message = "Enter the refund percentage between 75% and 95%.")
    private Double refund2DaysBefore;

    @NotNull(message = "refund10DaysBefore is required")
    @DecimalMin(value = "75.0", message = "Enter the refund percentage between 75% and 95%.")
    @DecimalMax(value = "95.0", message = "Enter the refund percentage between 75% and 95%.")
    private Double refund10DaysBefore;

    @NotNull(message = "refund20DaysOrMore is required")
    @DecimalMin(value = "75.0", message = "Enter the refund percentage between 75% and 95%.")
    @DecimalMax(value = "95.0", message = "Enter the refund percentage between 75% and 95%.")
    private Double refund20DaysOrMore;

    @NotNull(message = "businessClassMultiplier is required")
    @DecimalMin(value = "1.0", message = "businessClassMultiplier must be at least 1.0")
    private Double businessClassMultiplier;

    @NotNull(message = "executiveClassMultiplier is required")
    @DecimalMin(value = "1.0", message = "executiveClassMultiplier must be at least 1.0")
    private Double executiveClassMultiplier;

    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public Double getDiscount30DaysAdvance() { return discount30DaysAdvance; }
    public void setDiscount30DaysAdvance(Double v) { this.discount30DaysAdvance = v; }
    public Double getDiscount60DaysAdvance() { return discount60DaysAdvance; }
    public void setDiscount60DaysAdvance(Double v) { this.discount60DaysAdvance = v; }
    public Double getDiscount90DaysAdvance() { return discount90DaysAdvance; }
    public void setDiscount90DaysAdvance(Double v) { this.discount90DaysAdvance = v; }
    public Double getBulkBookingDiscount() { return bulkBookingDiscount; }
    public void setBulkBookingDiscount(Double v) { this.bulkBookingDiscount = v; }
    public Double getSilverUserDiscount() { return silverUserDiscount; }
    public void setSilverUserDiscount(Double v) { this.silverUserDiscount = v; }
    public Double getGoldUserDiscount() { return goldUserDiscount; }
    public void setGoldUserDiscount(Double v) { this.goldUserDiscount = v; }
    public Double getPlatinumUserDiscount() { return platinumUserDiscount; }
    public void setPlatinumUserDiscount(Double v) { this.platinumUserDiscount = v; }
    public Double getRefund2DaysBefore() { return refund2DaysBefore; }
    public void setRefund2DaysBefore(Double v) { this.refund2DaysBefore = v; }
    public Double getRefund10DaysBefore() { return refund10DaysBefore; }
    public void setRefund10DaysBefore(Double v) { this.refund10DaysBefore = v; }
    public Double getRefund20DaysOrMore() { return refund20DaysOrMore; }
    public void setRefund20DaysOrMore(Double v) { this.refund20DaysOrMore = v; }
    public Double getBusinessClassMultiplier() { return businessClassMultiplier; }
    public void setBusinessClassMultiplier(Double v) { this.businessClassMultiplier = v; }
    public Double getExecutiveClassMultiplier() { return executiveClassMultiplier; }
    public void setExecutiveClassMultiplier(Double v) { this.executiveClassMultiplier = v; }
}
