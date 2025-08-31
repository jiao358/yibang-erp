package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.InventoryCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存盘点数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface InventoryCheckRepository extends BaseMapper<InventoryCheck> {

    /**
     * 根据状态查询盘点单
     */
    @Select("SELECT * FROM inventory_checks WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryCheck> findByStatus(@Param("status") String status);

    /**
     * 根据仓库ID查询盘点单
     */
    @Select("SELECT * FROM inventory_checks WHERE warehouse_id = #{warehouseId} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryCheck> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 根据盘点类型查询盘点单
     */
    @Select("SELECT * FROM inventory_checks WHERE check_type = #{checkType} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryCheck> findByCheckType(@Param("checkType") String checkType);

    /**
     * 根据时间范围查询盘点单
     */
    @Select("SELECT * FROM inventory_checks WHERE planned_start_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY planned_start_time DESC")
    List<InventoryCheck> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待审核的盘点单
     */
    @Select("SELECT * FROM inventory_checks WHERE review_status = 'PENDING' AND deleted = 0 ORDER BY created_at ASC")
    List<InventoryCheck> findPendingReview();
}
