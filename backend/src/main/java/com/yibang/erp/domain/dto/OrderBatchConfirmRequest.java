package com.yibang.erp.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量供应商确认订单请求DTO
 */
@Data
public class OrderBatchConfirmRequest {

    /**
     * 订单ID列表
     */
    @NotEmpty(message = "订单ID列表不能为空")
    private List<Long> orderIds;

    /**
     * 确认原因
     */
    private String reason = "批量供应商确认";

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
