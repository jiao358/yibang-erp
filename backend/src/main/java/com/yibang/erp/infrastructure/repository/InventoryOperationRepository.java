package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.InventoryOperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库存操作记录数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface InventoryOperationRepository extends BaseMapper<InventoryOperation> {

    /**
     * 根据商品ID查询操作记录
     */
    @Select("SELECT * FROM inventory_operations WHERE product_id = #{productId} AND deleted = 0 ORDER BY operation_time DESC")
    List<InventoryOperation> findByProductId(@Param("productId") Long productId);

    /**
     * 根据仓库ID查询操作记录
     */
    @Select("SELECT * FROM inventory_operations WHERE warehouse_id = #{warehouseId} AND deleted = 0 ORDER BY operation_time DESC")
    List<InventoryOperation> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 根据操作类型查询操作记录
     */
    @Select("SELECT * FROM inventory_operations WHERE operation_type = #{operationType} AND deleted = 0 ORDER BY operation_time DESC")
    List<InventoryOperation> findByOperationType(@Param("operationType") String operationType);

    /**
     * 根据时间范围查询操作记录
     */
    @Select("SELECT * FROM inventory_operations WHERE operation_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY operation_time DESC")
    List<InventoryOperation> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据订单ID查询操作记录
     */
    @Select("SELECT * FROM inventory_operations WHERE order_id = #{orderId} AND deleted = 0 ORDER BY operation_time DESC")
    List<InventoryOperation> findByOrderId(@Param("orderId") Long orderId);
}
