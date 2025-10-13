package com.yibang.erp.controller.microservice;

public class OrderDeliveryRequest {
    private String sourceOrderId;

    private boolean orderApproved;

    public String getSourceOrderId() {
        return sourceOrderId;
    }

    public void setSourceOrderId(String sourceOrderId) {
        this.sourceOrderId = sourceOrderId;
    }

    public boolean isOrderApproved() {
        return orderApproved;
    }

    public void setOrderApproved(boolean orderApproved) {
        this.orderApproved = orderApproved;
    }

}
