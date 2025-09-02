package com.yibang.erp.domain.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yibang.erp.domain.dto.ErrorOrderInfo;

import java.util.List;
import java.util.Map;

/**
 * AI Excel错误订单服务接口
 */
public interface AIExcelErrorOrderService {

    /**
     * 保存错误订单
     */
    void saveErrorOrder(String taskId, int rowNumber, Map<String, Object> rawData, 
                       String errorType, String errorMessage, String suggestedAction);

    /**
     * 批量保存错误订单
     */
    void saveErrorOrders(List<Map<String, Object>> errorOrders);

    /**
     * 根据任务ID获取错误订单列表
     */
    List<ErrorOrderInfo> getErrorOrdersByTaskId(String taskId);

    /**
     * 分页获取用户的错误订单列表
     */
    IPage<ErrorOrderInfo> getUserErrorOrders(Long userId, Long companyId, int page, int size);

    /**
     * 根据错误类型获取错误订单
     */
    List<ErrorOrderInfo> getErrorOrdersByType(String errorType);

    /**
     * 根据状态获取错误订单
     */
    List<ErrorOrderInfo> getErrorOrdersByStatus(String status);

    /**
     * 标记错误订单为已处理
     */
    void markErrorOrderAsProcessed(Long errorOrderId, Long operatorId);

    /**
     * 标记错误订单为已忽略
     */
    void markErrorOrderAsIgnored(Long errorOrderId, Long operatorId);

    /**
     * 获取错误订单统计信息
     */
    Map<String, Object> getErrorOrderStatistics(String taskId);

    /**
     * 获取用户错误订单统计信息
     */
    Map<String, Object> getUserErrorOrderStatistics(Long userId, Long companyId);
}
