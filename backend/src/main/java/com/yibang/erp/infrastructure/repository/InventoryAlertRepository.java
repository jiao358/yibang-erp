package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.InventoryAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存预警数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface InventoryAlertRepository extends BaseMapper<InventoryAlert> {

    /**
     * 查询待处理的预警
     */
    @Select("SELECT * FROM inventory_alerts WHERE status = 'PENDING' AND deleted = 0 ORDER BY alert_level DESC, created_at ASC")
    List<InventoryAlert> findPendingAlerts();

    /**
     * 根据预警类型查询预警
     */
    @Select("SELECT * FROM inventory_alerts WHERE alert_type = #{alertType} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryAlert> findByAlertType(@Param("alertType") String alertType);

    /**
     * 根据预警级别查询预警
     */
    @Select("SELECT * FROM inventory_alerts WHERE alert_level = #{alertLevel} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryAlert> findByAlertLevel(@Param("alertLevel") String alertLevel);

    /**
     * 根据商品ID查询预警
     */
    @Select("SELECT * FROM inventory_alerts WHERE product_id = #{productId} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryAlert> findByProductId(@Param("productId") Long productId);

    /**
     * 根据仓库ID查询预警
     */
    @Select("SELECT * FROM inventory_alerts WHERE warehouse_id = #{warehouseId} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryAlert> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 查询高优先级预警
     */
    @Select("SELECT * FROM inventory_alerts WHERE alert_level IN ('HIGH', 'CRITICAL') AND status = 'PENDING' AND deleted = 0 ORDER BY alert_level DESC, created_at ASC")
    List<InventoryAlert> findHighPriorityAlerts();
}
