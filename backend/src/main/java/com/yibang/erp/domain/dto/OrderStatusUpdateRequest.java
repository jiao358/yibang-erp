package com.yibang.erp.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 订单状态更新请求DTO
 */
@Data
public class OrderStatusUpdateRequest {

    /**
     * 目标状态
     */
    @NotBlank(message = "目标状态不能为空")
    private String targetStatus;

    /**
     * 状态变更原因
     */
    private String reason;

    /**
     * 状态变更备注
     */
    private String remarks;

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

    /**
     * 是否强制更新（跳过状态转换验证）
     */
    private Boolean forceUpdate = false;
}
