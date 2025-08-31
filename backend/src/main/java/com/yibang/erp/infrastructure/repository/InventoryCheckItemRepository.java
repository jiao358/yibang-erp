package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.InventoryCheckItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存盘点明细数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface InventoryCheckItemRepository extends BaseMapper<InventoryCheckItem> {

    /**
     * 根据盘点单ID查询明细
     */
    @Select("SELECT * FROM inventory_check_items WHERE check_id = #{checkId} AND deleted = 0 ORDER BY created_at ASC")
    List<InventoryCheckItem> findByCheckId(@Param("checkId") Long checkId);

    /**
     * 根据商品ID查询盘点明细
     */
    @Select("SELECT * FROM inventory_check_items WHERE product_id = #{productId} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryCheckItem> findByProductId(@Param("productId") Long productId);

    /**
     * 根据仓库ID查询盘点明细
     */
    @Select("SELECT * FROM inventory_check_items WHERE warehouse_id = #{warehouseId} AND deleted = 0 ORDER BY created_at DESC")
    List<InventoryCheckItem> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 查询有差异的盘点明细
     */
    @Select("SELECT * FROM inventory_check_items WHERE difference_quantity != 0 AND deleted = 0 ORDER BY ABS(difference_quantity) DESC")
    List<InventoryCheckItem> findItemsWithDifference();

    /**
     * 根据状态查询盘点明细
     */
    @Select("SELECT * FROM inventory_check_items WHERE status = #{status} AND deleted = 0 ORDER BY created_at ASC")
    List<InventoryCheckItem> findByStatus(@Param("status") String status);
}
