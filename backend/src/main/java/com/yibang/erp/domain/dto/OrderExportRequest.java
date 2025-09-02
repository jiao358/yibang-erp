package com.yibang.erp.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 订单导出请求DTO
 */
@Data
public class OrderExportRequest {

    /**
     * 订单ID列表
     */
    private List<Long> orderIds;

    /**
     * 导出类型（用于区分不同供应商的导出格式）
     */
    private String exportType;

    /**
     * 供应商ID（用于确定导出格式）
     */
    private Long supplierId;
}
