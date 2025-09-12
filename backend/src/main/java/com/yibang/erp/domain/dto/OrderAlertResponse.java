package com.yibang.erp.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单预警响应
 */
@Data
public class OrderAlertResponse {
    
    /**
     * 人工处理记录ID
     */
    private Long id;
    
    /**
     * 订单ID
     */
    private Long orderId;
    
    /**
     * 源订单ID（外部系统订单号）
     */
    private String sourceOrderId;
    
    /**
     * 处理类型
     */
    private String processingType;
    
    /**
     * 处理类型描述
     */
    private String processingTypeDesc;
    
    /**
     * 处理原因
     */
    private String processingReason;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 优先级
     */
    private String priority;
    
    /**
     * 优先级描述
     */
    private String priorityDesc;
    
    /**
     * 分配给谁处理（用户ID）
     */
    private Long assignedTo;
    
    /**
     * 分配给谁处理（用户名）
     */
    private String assignedToName;
    
    /**
     * 实际处理人（用户ID）
     */
    private Long processedBy;
    
    /**
     * 实际处理人（用户名）
     */
    private String processedByName;
    
    /**
     * 处理完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;
    
    /**
     * 处理备注
     */
    private String processingNotes;
    
    /**
     * 拒绝原因
     */
    private String rejectionReason;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 创建人
     */
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    private String createdByName;
    
    /**
     * 订单信息
     */
    private OrderBasicInfo orderInfo;
    
    /**
     * 订单基本信息
     */
    @Data
    public static class OrderBasicInfo {
        private String orderNumber;
        private String customerName;
        private String orderStatus;
        private String orderStatusDesc;
        private String totalAmount;
        private String deliveryAddress;
        private String buyerNote;
    }
}
