package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 供应商拒绝订单请求DTO
 */
@Data
public class SupplierRejectRequest {

    /**
     * 拒绝原因
     */
    @NotBlank(message = "拒绝原因不能为空")
    private String rejectReason;

    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作人角色
     */
    private String operatorRole;
}
