package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.ProductInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 商品库存数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface ProductInventoryRepository extends BaseMapper<ProductInventory> {

    /**
     * 根据公司ID查询该公司的商品列表（用于入库出库时选择）
     */
    @Select("SELECT p.id, p.sku, p.name, p.unit, p.status " +
            "FROM products p " +
            "WHERE p.company_id = #{companyId} AND p.status = 'ACTIVE' " +
            "ORDER BY p.name")
    List<Map<String, Object>> findProductsByCompanyId(@Param("companyId") Long companyId);

    /**
     * 根据商品ID查询所有仓库的库存
     */
    @Select("SELECT * FROM product_inventory WHERE product_id = #{productId}")
    List<ProductInventory> findByProductId(@Param("productId") Long productId);

    /**
     * 根据仓库ID查询所有商品库存
     */
    @Select("SELECT * FROM product_inventory WHERE warehouse_id = #{warehouseId}")
    List<ProductInventory> findByWarehouseId(@Param("warehouseId") Long warehouseId);

    /**
     * 根据商品ID和仓库ID查询库存
     */
    @Select("SELECT * FROM product_inventory WHERE product_id = #{productId} AND warehouse_id = #{warehouseId}")
    ProductInventory findByProductIdAndWarehouseId(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);

    /**
     * 查询库存不足的商品
     */
    @Select("SELECT * FROM product_inventory WHERE available_quantity <= min_stock_level")
    List<ProductInventory> findLowStockProducts();

    /**
     * 查询需要补货的商品
     */
    @Select("SELECT * FROM product_inventory WHERE available_quantity <= reorder_point")
    List<ProductInventory> findProductsNeedingReorder();
}
