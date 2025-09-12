package com.yibang.erp.domain.dto;

public class OrderCloseMessageResponse {

    public boolean canClose;

    public String message;

    public boolean isCanClose() {
        return canClose;
    }

    public void setCanClose(boolean canClose) {
        this.canClose = canClose;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
