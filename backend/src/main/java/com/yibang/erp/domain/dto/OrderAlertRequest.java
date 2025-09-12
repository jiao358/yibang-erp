package com.yibang.erp.domain.dto;

import com.yibang.erp.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单预警查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderAlertRequest extends PageRequest {
    
    /**
     * 处理状态：PENDING, PROCESSING, COMPLETED, REJECTED
     */
    private String status;
    
    /**
     * 处理类型：ORDER_CLOSE, ADDRESS_CHANGE, REFUND, CANCEL
     */
    private String processingType;
    
    /**
     * 开始日期（创建时间）
     */
    private String startDate;
    
    /**
     * 结束日期（创建时间）
     */
    private String endDate;
    
    /**
     * 排序字段：created_at, priority
     */
    private String sortField;
    
    /**
     * 排序方向：asc, desc
     */
    private String sortOrder;
    
    /**
     * 订单编号（模糊查询）
     */
    private String orderNumber;
    
    /**
     * 源订单ID（模糊查询）
     */
    private String sourceOrderId;
}
