package com.ams.dto;
public class CancellationPreviewResponse {
    private double refundPercentage; private double refundAmount; private long daysRemaining; private boolean eligible; private String reason;
    public double getRefundPercentage() { return refundPercentage; } public void setRefundPercentage(double v) { refundPercentage = v; }
    public double getRefundAmount() { return refundAmount; } public void setRefundAmount(double v) { refundAmount = v; }
    public long getDaysRemaining() { return daysRemaining; } public void setDaysRemaining(long v) { daysRemaining = v; }
    public boolean isEligible() { return eligible; } public void setEligible(boolean v) { eligible = v; }
    public String getReason() { return reason; } public void setReason(String v) { reason = v; }
}
