package com.yibang.erp.domain.service;

import java.util.List;
import java.util.Map;

/**
 * 工作台服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface WorkspaceService {

    /**
     * 获取供应链管理员统计数据
     */
    Map<String, Object> getSupplierStats(Long companyId);

    /**
     * 获取系统管理员统计数据
     */
    Map<String, Object> getSystemStats();

    /**
     * 获取销售统计数据
     */
    Map<String, Object> getSalesStats(Long companyId);

    /**
     * 获取基础统计数据
     */
    Map<String, Object> getBasicStats(Long companyId);

    /**
     * 获取最近订单
     */
    List<Map<String, Object>> getRecentOrders(Long companyId, int limit);

    /**
     * 获取最近商品
     */
    List<Map<String, Object>> getRecentProducts(Long companyId, int limit);

    /**
     * 获取库存预警商品
     */
    List<Map<String, Object>> getLowStockItems(Long companyId, int limit);

    /**
     * 获取待审核订单
     */
    List<Map<String, Object>> getPendingOrders(Long companyId, int limit);

    /**
     * 获取待发货订单
     */
    List<Map<String, Object>> getPendingShipments(Long companyId, int limit);

    /**
     * 获取我的订单（销售用户）
     */
    List<Map<String, Object>> getMyOrders(Long companyId, Long userId, int limit);

    /**
     * 获取最近客户
     */
    List<Map<String, Object>> getRecentCustomers(Long companyId, int limit);

    /**
     * 获取AI导入历史
     */
    List<Map<String, Object>> getRecentAITasks(Long companyId, int limit);
}
