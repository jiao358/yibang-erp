package com.yibang.erp.domain.service;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.ManualProcessingRequest;
import com.yibang.erp.domain.dto.OrderAlertRequest;
import com.yibang.erp.domain.dto.OrderAlertResponse;

/**
 * 订单预警服务接口
 */
public interface OrderAlertService {
    
    /**
     * 分页查询订单预警列表
     */
    PageResult<OrderAlertResponse> getOrderAlerts(OrderAlertRequest request, Long companyId, Long userId);
    
    /**
     * 分配预警给用户
     */
    void assignAlert(Long alertId, Long userId, Long currentUserId);
    
    /**
     * 开始处理预警
     */
    void processAlert(Long alertId, ManualProcessingRequest request, Long currentUserId);
    
    /**
     * 完成预警处理
     */
    void completeAlert(Long alertId, ManualProcessingRequest request, Long currentUserId);
    
    /**
     * 拒绝预警处理
     */
    void rejectAlert(Long alertId, ManualProcessingRequest request, Long currentUserId);
}
