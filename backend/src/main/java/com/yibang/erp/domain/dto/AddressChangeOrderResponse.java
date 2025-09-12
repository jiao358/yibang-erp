package com.yibang.erp.domain.dto;

public class AddressChangeOrderResponse {

    public boolean canEdit;

    public String message;

    public boolean isCanEdit() {
        return canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
