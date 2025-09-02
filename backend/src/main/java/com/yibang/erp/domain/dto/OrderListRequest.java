package com.yibang.erp.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单列表请求DTO
 */
@Data
public class OrderListRequest {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 平台订单号
     */
    private String platformOrderNo;

    /**
     * 销售订单ID
     */
    private String salesOrderId;

    /**
     * 来源订单号
     */
    private String sourceOrderNo;

    /**
     * 销售人ID
     */
    private Long salesUserId;

    /**
     * 销售公司ID
     */
    private Long salesCompanyId;

    /**
     * 客户ID
     */
    private Long customerId;

    private String orderStatus;

    private String orderSource;


    /**
     * 客户姓名
     */
    private String customerName;

    /**
     * 客户电话
     */
    private String customerPhone;

    /**
     * 供应商公司ID
     */
    private Long supplierCompanyId;

    /**
     * 订单状态
     */
    private String status;

    /**
     * 订单来源
     */
    private String source;

    /**
     * 模板版本
     */
    private String templateVersion;

    /**
     * 创建开始时间
     */
    private LocalDateTime createdAtStart;

    /**
     * 创建结束时间
     */
    private LocalDateTime createdAtEnd;

    /**
     * 更新开始时间
     */
    private LocalDateTime updatedAtStart;

    /**
     * 更新结束时间
     */
    private LocalDateTime updatedAtEnd;

    /**
     * 排序字段
     */
    private String sortField = "createdAt";

    /**
     * 排序方向（asc, desc）
     */
    private String sortOrder = "desc";

    /**
     * 关键词搜索（模糊匹配平台订单号、销售订单ID、客户姓名、客户电话）
     */
    private String keyword;
}
