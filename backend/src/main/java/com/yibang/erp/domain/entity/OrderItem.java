package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 订单商品实体 - 基于现有数据库表结构
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("order_items")
public class OrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 商品SKU
     */
    @TableField("sku")
    private String sku;

    /**
     * 商品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 商品规格JSON
     */
    @TableField("product_specifications")
    private String productSpecifications;

    @TableField( "sales_note")//卖家备注
    private String salesNote;
    @TableField( "buyer_note") //买家备注
    private String buyerNote;


    /**
     * 商品单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 商品数量
     */
    @TableField("quantity")
    private Integer quantity;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 折扣率
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 折扣金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 税率
     */
    @TableField("tax_rate")
    private BigDecimal taxRate;

    /**
     * 税额
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;

    /**
     * 小计金额
     */
    @TableField("subtotal")
    private BigDecimal subtotal;

    /**
     * AI映射后的商品ID
     */
    @TableField("ai_mapped_product_id")
    private Long aiMappedProductId;

    /**
     * AI映射置信度
     */
    @TableField("ai_confidence")
    private BigDecimal aiConfidence;

    /**
     * AI处理状态
     */
    @TableField("ai_processing_status")
    private String aiProcessingStatus; // PENDING, PROCESSING, COMPLETED, FAILED

    /**
     * AI处理结果JSON
     */
    @TableField("ai_processing_result")
    private String aiProcessingResult;

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

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private Map<String, Object> extendedFields;

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String platformOrderId; // 平台订单号，用于前端显示

    // 扩展字段（用于存储动态字段，不映射到数据库）
    @TableField(exist = false)
    private String remarks; // 商品备注，用于前端显示

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

    /**
     * 计算商品总价
     */
    public void calculateTotalPrice() {
        if (quantity != null && unitPrice != null) {
            this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
