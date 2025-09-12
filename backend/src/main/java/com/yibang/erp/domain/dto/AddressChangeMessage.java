package com.yibang.erp.domain.dto;

public class AddressChangeMessage {
    /** 订单ID */
    private String orderId;

    /** 省份 */
    private String provinceName;

    /** 城市 */
    private String cityName;

    /** 区域 */
    private String districtName;

    /** 详细地址 */
    private String deliveryAddress;

    /** 买家留言 */
    private String buyerNote;
    /** 幂等性键（来自电商平台的Idempotency-Key） */
    private String idempotencyKey;

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getBuyerNote() {
        return buyerNote;
    }

    public void setBuyerNote(String buyerNote) {
        this.buyerNote = buyerNote;
    }
}
