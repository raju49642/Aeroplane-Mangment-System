package com.ams.dto;

public class CarrierResponse {
    private Integer carrierId;
    private String carrierName;
    private String carrierCode;
    private Double discount30DaysAdvance;
    private Double discount60DaysAdvance;
    private Double discount90DaysAdvance;
    private Double bulkBookingDiscount;
    private Double silverUserDiscount;
    private Double goldUserDiscount;
    private Double platinumUserDiscount;
    private Double refund2DaysBefore;
    private Double refund10DaysBefore;
    private Double refund20DaysOrMore;
    private Double businessClassMultiplier;
    private Double executiveClassMultiplier;

    // Constructors
    public CarrierResponse() {}

    public CarrierResponse(Integer carrierId, String carrierName, String carrierCode,
                          Double discount30DaysAdvance, Double discount60DaysAdvance,
                          Double discount90DaysAdvance, Double bulkBookingDiscount,
                          Double silverUserDiscount, Double goldUserDiscount,
                          Double platinumUserDiscount, Double refund2DaysBefore,
                          Double refund10DaysBefore, Double refund20DaysOrMore,
                          Double businessClassMultiplier, Double executiveClassMultiplier) {
        this.carrierId = carrierId;
        this.carrierName = carrierName;
        this.carrierCode = carrierCode;
        this.discount30DaysAdvance = discount30DaysAdvance;
        this.discount60DaysAdvance = discount60DaysAdvance;
        this.discount90DaysAdvance = discount90DaysAdvance;
        this.bulkBookingDiscount = bulkBookingDiscount;
        this.silverUserDiscount = silverUserDiscount;
        this.goldUserDiscount = goldUserDiscount;
        this.platinumUserDiscount = platinumUserDiscount;
        this.refund2DaysBefore = refund2DaysBefore;
        this.refund10DaysBefore = refund10DaysBefore;
        this.refund20DaysOrMore = refund20DaysOrMore;
        this.businessClassMultiplier = businessClassMultiplier;
        this.executiveClassMultiplier = executiveClassMultiplier;
    }

    // Getters and Setters
    public Integer getCarrierId() { return carrierId; }
    public void setCarrierId(Integer carrierId) { this.carrierId = carrierId; }

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
