package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 物流信息实体 - 基于现有数据库表结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("logistics_info")
public class LogisticsInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 物流单号
     */
    @TableField("tracking_number")
    private String trackingNumber;

    /**
     * 物流公司
     */
    @TableField("carrier")
    private String carrier;

    /**
     * 物流公司代码
     */
    @TableField("carrier_code")
    private String carrierCode;

    /**
     * 运输方式
     */
    @TableField("shipping_method")
    private String shippingMethod;

    /**
     * 发货时间
     */
    @TableField("shipping_date")
    private LocalDateTime shippingDate;

    /**
     * 预计到达日期
     */
    @TableField("estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    /**
     * 实际到达日期
     */
    @TableField("actual_delivery_date")
    private LocalDateTime actualDeliveryDate;

    /**
     * 发货地址
     */
    @TableField("shipping_address")
    private String shippingAddress;

    /**
     * 收货地址
     */
    @TableField("delivery_address")
    private String deliveryAddress;

    /**
     * 发货联系人
     */
    @TableField("shipping_contact")
    private String shippingContact;

    /**
     * 收货联系人
     */
    @TableField("delivery_contact")
    private String deliveryContact;

    /**
     * 发货联系电话
     */
    @TableField("shipping_phone")
    private String shippingPhone;

    /**
     * 收货联系电话
     */
    @TableField("delivery_phone")
    private String deliveryPhone;

    /**
     * 发货备注
     */
    @TableField("shipping_notes")
    private String shippingNotes;

    /**
     * 收货备注
     */
    @TableField("delivery_notes")
    private String deliveryNotes;

    /**
     * 物流状态
     */
    @TableField("status")
    private String status; // PENDING, SHIPPED, IN_TRANSIT, DELIVERED, FAILED

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String platformOrderId; // 平台订单号，用于前端显示
}
