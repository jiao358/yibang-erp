package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单实体 - 基于现有数据库表结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("orders")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 平台订单号（系统内部唯一标识）
     */
    @TableField("platform_order_id")
    private String platformOrderId;

    /**
     * 客户ID
     */
    @TableField("customer_id")
    private Long customerId;

    /**
     * 销售ID
     */
    @TableField("sales_id")
    private Long salesId;

    /**
     * 供应链公司ID
     */
    @TableField("supplier_company_id")
    private Long supplierCompanyId;

    /**
     * 订单类型
     */
    @TableField("order_type")
    private String orderType; // NORMAL, WHOLESALE, RUSH, SAMPLE

    /**
     * 订单来源
     */
    @TableField("order_source")
    private String orderSource; // MANUAL, EXCEL_IMPORT, API, WEBSITE

    /**
     * 订单状态
     */
    @TableField("order_status")
    private String orderStatus;

    /**
     * 审核状态
     */
    @TableField("approval_status")
    private String approvalStatus; // PENDING, APPROVED, REJECTED

    /**
     * 审核意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 审核时间
     */
    @TableField("approval_at")
    private LocalDateTime approvalAt;

    /**
     * 审核人ID
     */
    @TableField("approval_by")
    private Long approvalBy;

    /**
     * 订单总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 折扣金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 税额
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;

    @TableField("logistics_order_number")
    private String logisticsOrderNumber;

    /**
     * 物流公司
     */
    @TableField("logistics_company")
    private String logisticsCompany;

    /**
     * 运费
     */
    @TableField("shipping_amount")
    private BigDecimal shippingAmount;

    /**
     * 最终金额
     */
    @TableField("final_amount")
    private BigDecimal finalAmount;

    /**
     * 货币代码
     */
    @TableField("currency")
    private String currency;

    /**
     * 支付状态
     */
    @TableField("payment_status")
    private String paymentStatus; // UNPAID, PARTIALLY_PAID, PAID, REFUNDED

    /**
     * 支付方式
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 支付时间
     */
    @TableField("payment_at")
    private LocalDateTime paymentAt;

    /**
     * 收货地址
     */
    @TableField("delivery_address")
    private String deliveryAddress;

    /**
     * 收货联系人
     */
    @TableField("delivery_contact")
    private String deliveryContact;

    /**
     * 收货联系电话
     */
    @TableField("delivery_phone")
    private String deliveryPhone;

    /**
     * 预计交货日期
     */
    @TableField("expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    /**
     * 特殊要求
     */
    @TableField("special_requirements")
    private String specialRequirements;

    /**
     * AI处理置信度
     */
    @TableField("ai_confidence")
    private BigDecimal aiConfidence;

    /**
     * 是否经过AI处理
     */
    @TableField("ai_processed")
    private Boolean aiProcessed;



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

    @TableField("province_name")
    private String provinceName;


    @TableField("city_name")
    private String cityName;

    @TableField("district_name")
    private String  districtName;

    @TableField("source_order_id")
    private String sourceOrderId;





    /**
     * 是否删除
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private Map<String, Object> extendedFields;

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String salesOrderId; // 销售订单ID，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String templateVersion; // 模板版本，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String remarks; // 订单备注，用于前端显示

    /**
     * 获取扩展字段Map
     */
    public Map<String, Object> getExtendedFieldsMap() {
        if (extendedFields == null) {
            return Map.of();
        }
        return extendedFields;
    }

    /**
     * 设置扩展字段Map
     */
    public void setExtendedFieldsMap(Map<String, Object> fields) {
        this.extendedFields = fields;
    }
}
