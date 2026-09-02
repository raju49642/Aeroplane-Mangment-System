package com.ams.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "carriers")
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "carrier_id")
    private Integer carrierId;

    @Column(name = "carrier_name", nullable = false, unique = true)
    private String carrierName;

    // Nullable for safe upgrade of existing deployments; new/updated carriers require it.
    @Column(name = "carrier_code", unique = true, length = 2)
    private String carrierCode;

    @Column(name = "discount_30_days_advance")
    private Double discount30DaysAdvance;

    @Column(name = "discount_60_days_advance")
    private Double discount60DaysAdvance;

    @Column(name = "discount_90_days_advance")
    private Double discount90DaysAdvance;

    @Column(name = "bulk_booking_discount")
    private Double bulkBookingDiscount;

    @Column(name = "silver_user_discount")
    private Double silverUserDiscount;

    @Column(name = "gold_user_discount")
    private Double goldUserDiscount;

    @Column(name = "platinum_user_discount")
    private Double platinumUserDiscount;

    @Column(name = "refund_2_days_before")
    private Double refund2DaysBefore;

    @Column(name = "refund_10_days_before")
    private Double refund10DaysBefore;

    @Column(name = "refund_20_days_or_more")
    private Double refund20DaysOrMore;

    @Column(name = "business_class_multiplier", nullable = false)
    private Double businessClassMultiplier = 1.0;

    @Column(name = "executive_class_multiplier", nullable = false)
    private Double executiveClassMultiplier = 1.0;

    public Carrier() {
    }

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
