package com.yibang.erp.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单响应DTO
 */
@Data
public class OrderResponse {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 配送地址
     */
    private String deliveryAddress;

    /**
     * 币种
     */
    private String currency;
    /**
     * 平台订单号（系统内部唯一标识）
     */
    private String platformOrderNo;

    private String buyerNote;

    private String salesNote;

    /**
     * 订单类型 (NORMAL, WHOLESALE, RUSH, SAMPLE)
     */
    private String orderType;

    /**
     * 销售订单ID（客户提供的订单号，仅作参考）
     */
    private String salesOrderId;

    /**
     * 销售人ID
     */
    private Long salesUserId;

    /**
     * 销售人姓名
     */
    private String salesUserName;

    /**
     * 销售公司ID
     */
    private Long salesCompanyId;

    /**
     * 销售公司名称
     */
    private String salesCompanyName;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 客户电话
     */
    private String customerPhone;

    /**
     * 客户地址
     */
    private String customerAddress;

    private String sourceOrderId;

    /**
     * 供应商公司ID
     */
    private Long supplierCompanyId;

    /**
     * 供应商公司名称
     */
    private String supplierCompanyName;

    /**
     * 订单状态
     */
    private String status;
    /**
     * 订单状态名称
     */
    private String orderStatus;

    /**
     * 订单状态名称
     */
    private String statusName;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单数量
     */
    private Integer totalQuantity;

    /**
     * 订单来源
     */
    private String source;

    /**
     * 订单来源名称
     */
    private String sourceName;

    /**
     * 来源订单平台id
     */
    private String sourceOrderNo;

    /**
     * 配送联系人
     */
    private String deliveryContact;

    /**
     * 配送电话
     */
    private String deliveryPhone;
    /**
     * 配送地址
     */
    private String address;

    /**
     * 模板版本
     */
    private String templateVersion;

    /**
     * 扩展字段（动态字段）
     */
    private Map<String, Object> extendedFields;

    /**
     * 订单备注
     */
    private String remarks;

    /**
     * 审核意见
     */
    private String approvalComment;

    /**
     * 审核时间
     */
    private LocalDateTime approvalAt;

    /**
     * 审核人ID
     */
    private Long approvalBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * user loginName
     */
    private String createUserName;
    /**
     * 创建人姓名
     */
    private String createdByName;

    /**
     * 预计发货时间
     */
    private LocalDate expectedDeliveryDate;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 更新人姓名
     */
    private String updatedByName;

    /**
     * 是否使用AI处理
     */
    private boolean aiProcessed;

    /**
     * AI处理置信度
     */
    private String aiConfidence;


    /**
     * 省份名称
     */
    private String provinceName;


    /**
     * 城市名称
     */
    private String cityName;

    /**
     * 区域名称
     */
    private String districtName;

    /**
     * 订单商品列表
     */
    private List<OrderItemResponse> orderItems;

    /**
     * 物流信息
     */
    private LogisticsInfoDTO logisticsInfo;

    /**
     * 订单商品响应DTO
     */
    @Data
    public static class OrderItemResponse {
        
        /**
         * 订单商品ID
         */
        private Long id;

        private String unit;
        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品SKU
         */
        private String productSku;

        /**
         * 商品图片
         */
        private String productImage;

        /**
         * 商品数量
         */
        private Integer quantity;

        /**
         * 商品单价
         */
        private BigDecimal unitPrice;

        /**
         * 佣金
         */
        private BigDecimal commission;
        /**
         * 商品总价
         */
        private BigDecimal totalPrice;

        private BigDecimal discountAmount;

        /**
         * 商品备注
         */
        private String remarks;

        private String currency;

        /**
         * 扩展字段
         */
        private Map<String, Object> extendedFields;

        /**
         * 配送联系人
         */
        private String deliveryName;



    }
}
