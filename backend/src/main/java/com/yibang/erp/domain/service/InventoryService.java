package com.yibang.erp.domain.service;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.StockOperationRequest;
import com.yibang.erp.domain.dto.InventoryListDTO;
import com.yibang.erp.domain.dto.StockAdjustmentRequest;
import com.yibang.erp.domain.entity.InventoryOperation;
import com.yibang.erp.domain.entity.ProductInventory;

import java.util.List;

/**
 * 库存管理服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface InventoryService {

    /**
     * 商品入库
     */
    InventoryOperation stockIn(StockOperationRequest request);

    /**
     * 商品出库
     */
    InventoryOperation stockOut(StockOperationRequest request);

    /**
     * 库存调整
     */
    InventoryOperation adjustStock(StockOperationRequest request);

    /**
     * 库存调整（完整版）
     */
    InventoryOperation adjustStockComplete(StockAdjustmentRequest request);

    /**
     * 库存调拨
     */
    InventoryOperation transferStock(StockOperationRequest request);

    /**
     * 根据商品ID查询库存
     */
    List<ProductInventory> getInventoryByProductId(Long productId);

    /**
     * 根据仓库ID查询库存
     */
    List<ProductInventory> getInventoryByWarehouseId(Long warehouseId);

    /**
     * 根据商品ID和仓库ID查询库存
     */
    ProductInventory getInventoryByProductAndWarehouse(Long productId, Long warehouseId);

    /**
     * 分页查询库存列表
     */
    PageResult<ProductInventory> getInventoryPage(int page, int size, Long productId, Long warehouseId,
                                          String productName, String warehouseName, Long companyId);

    /**
     * 分页查询库存列表（包含商品和仓库信息）
     */
    PageResult<InventoryListDTO> getInventoryListPage(int page, int size, Long productId, Long warehouseId,
                                          String productName, String warehouseName, Long companyId);

    /**
     * 查询库存不足的商品
     */
    List<ProductInventory> getLowStockProducts();

    /**
     * 查询需要补货的商品
     */
    List<ProductInventory> getProductsNeedingReorder();

    /**
     * 设置库存预警线
     */
    boolean setStockAlertLevel(Long productId, Long warehouseId, Integer minStockLevel, 
                              Integer maxStockLevel, Integer reorderPoint, Long operatorId);

    /**
     * 查询库存操作记录
     */
    PageResult<InventoryOperation> getOperationHistory(int page, int size, Long productId, Long warehouseId,
                                                       String operationType, String startTime, String endTime);
}
