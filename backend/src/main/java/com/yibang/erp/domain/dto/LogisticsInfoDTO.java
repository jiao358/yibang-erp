package com.yibang.erp.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 物流信息DTO
 */
@Data
public class LogisticsInfoDTO {
    
    /**
     * 物流公司
     */
    private String carrier;
    
    /**
     * 物流单号
     */
    private String trackingNumber;
    
    /**
     * 发货方式
     */
    private String shippingMethod;
    
    /**
     * 发货时间
     */
    private LocalDateTime shippedAt;
    
    /**
     * 物流备注
     */
    private String notes;
    
    /**
     * 物流状态
     */
    private String status;
    
    /**
     * 预计到达日期
     */
    private String estimatedDeliveryDate;
    
    /**
     * 实际到达日期
     */
    private LocalDateTime actualDeliveryDate;
    
    /**
     * 发货地址
     */
    private String shippingAddress;
    
    /**
     * 收货地址
     */
    private String deliveryAddress;
    
    /**
     * 发货联系人
     */
    private String shippingContact;
    
    /**
     * 收货联系人
     */
    private String deliveryContact;
    
    /**
     * 发货联系电话
     */
    private String shippingPhone;
    
    /**
     * 收货联系电话
     */
    private String deliveryPhone;
    
    /**
     * 发货仓库名称
     */
    private String warehouseName;
}
