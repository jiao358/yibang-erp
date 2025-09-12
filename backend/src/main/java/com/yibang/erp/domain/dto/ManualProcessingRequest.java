package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 人工处理请求
 */
@Data
public class ManualProcessingRequest {
    
    /**
     * 处理备注
     */
    private String processingNotes;
    
    /**
     * 拒绝原因（当状态为REJECTED时使用）
     */
    private String rejectionReason;
    
    /**
     * 分配给谁处理（用户ID）
     */
    private Long assignedTo;
}
