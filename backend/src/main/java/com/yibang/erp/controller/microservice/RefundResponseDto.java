package com.yibang.erp.controller.microservice;

public class RefundResponseDto {

    private boolean refundSuccess;

    private String refundReason;

    public boolean isRefundSuccess() {
        return refundSuccess;
    }

    public void setRefundSuccess(boolean refundSuccess) {
        this.refundSuccess = refundSuccess;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
}
