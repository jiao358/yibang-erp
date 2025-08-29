package com.yibang.erp.domain.dto;

import com.yibang.erp.domain.enums.ProductApprovalStatus;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 商品审核请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class ProductAuditRequest {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 审核状态
     */
    @NotNull(message = "审核状态不能为空")
    private ProductApprovalStatus approvalStatus;

    /**
     * 审核意见
     */
    @Size(max = 1000, message = "审核意见长度不能超过1000字符")
    private String approvalComment;

    /**
     * 审核人ID
     */
    @NotNull(message = "审核人ID不能为空")
    private Long approverId;
}
