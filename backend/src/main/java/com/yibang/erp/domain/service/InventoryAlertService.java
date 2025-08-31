package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.InventoryAlertRequest;
import com.yibang.erp.domain.entity.InventoryAlert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 库存预警服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface InventoryAlertService {

    /**
     * 创建库存预警
     */
    InventoryAlert createAlert(InventoryAlertRequest request, Long operatorId);

    /**
     * 更新库存预警
     */
    InventoryAlert updateAlert(Long id, InventoryAlertRequest request, Long operatorId);

    /**
     * 删除库存预警
     */
    boolean deleteAlert(Long id, Long operatorId);

    /**
     * 根据ID查询库存预警
     */
    InventoryAlert getAlertById(Long id);

    /**
     * 分页查询库存预警列表
     */
    Page<InventoryAlert> getAlertPage(int page, int size, String alertType, String alertLevel, 
                                     String status, Long productId, Long warehouseId);

    /**
     * 查询待处理的预警
     */
    List<InventoryAlert> getPendingAlerts();

    /**
     * 查询高优先级预警
     */
    List<InventoryAlert> getHighPriorityAlerts();

    /**
     * 处理库存预警
     */
    boolean handleAlert(Long id, String handlingResult, String handlingRemark, Long handlerId);

    /**
     * 忽略库存预警
     */
    boolean ignoreAlert(Long id, String reason, Long operatorId);

    /**
     * 自动检测并创建库存预警
     */
    void autoDetectAndCreateAlerts();

    /**
     * 根据预警类型查询预警
     */
    List<InventoryAlert> getAlertsByType(String alertType);

    /**
     * 根据预警级别查询预警
     */
    List<InventoryAlert> getAlertsByLevel(String alertLevel);
}
