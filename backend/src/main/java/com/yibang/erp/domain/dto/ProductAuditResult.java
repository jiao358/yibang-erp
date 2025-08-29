package com.yibang.erp.domain.dto;

import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品审核结果DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class ProductAuditResult {

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
    private String sku;

    /**
     * 审核状态
     */
    private ProductApprovalStatus approvalStatus;

    /**
     * 商品状态
     */
    private ProductStatus productStatus;

    /**
     * 审核意见
     */
    private String approvalComment;

    /**
     * 审核人ID
     */
    private Long approverId;

    /**
     * 审核人姓名
     */
    private String approverName;

    /**
     * 审核时间
     */
    private LocalDateTime approvalAt;

    /**
     * 审核结果消息
     */
    private String message;

    /**
     * 是否成功
     */
    private boolean success;
}
