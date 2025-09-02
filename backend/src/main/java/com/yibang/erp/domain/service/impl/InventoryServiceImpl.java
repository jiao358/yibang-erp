package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.StockOperationRequest;
import com.yibang.erp.domain.dto.InventoryListDTO;
import com.yibang.erp.domain.dto.StockAdjustmentRequest;
import com.yibang.erp.domain.entity.InventoryOperation;
import com.yibang.erp.domain.entity.ProductInventory;
import com.yibang.erp.domain.entity.Warehouse;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.service.InventoryService;
import com.yibang.erp.infrastructure.repository.InventoryOperationRepository;
import com.yibang.erp.infrastructure.repository.ProductInventoryRepository;
import com.yibang.erp.infrastructure.repository.WarehouseRepository;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 库存管理服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductInventoryRepository productInventoryRepository;
    private final InventoryOperationRepository inventoryOperationRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public InventoryOperation stockIn(StockOperationRequest request) {
        // 获取或创建库存记录
        ProductInventory inventory = getOrCreateInventory(request.getProductId(), request.getWarehouseId());
        
        // 记录操作前的库存状态
        Integer beforeAvailable = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        Integer beforeReserved = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        Integer beforeDamaged = inventory.getDamagedQuantity() != null ? inventory.getDamagedQuantity() : 0;

        // 更新库存
        inventory.setAvailableQuantity(beforeAvailable + request.getQuantity());
        inventory.setLastStockIn(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        productInventoryRepository.updateById(inventory);

        // 创建操作记录
        InventoryOperation operation = createOperationRecord(request, beforeAvailable, beforeReserved, beforeDamaged,
                inventory.getAvailableQuantity(), beforeReserved, beforeDamaged);

        log.info("商品入库成功，商品ID: {}, 仓库ID: {}, 数量: {}", 
                request.getProductId(), request.getWarehouseId(), request.getQuantity());
        
        return operation;
    }

    @Override
    @Transactional
    public InventoryOperation stockOut(StockOperationRequest request) {
        ProductInventory inventory = productInventoryRepository.findByProductIdAndWarehouseId(
                request.getProductId(), request.getWarehouseId());
        
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }

        Integer availableQuantity = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        if (availableQuantity < request.getQuantity()) {
            throw new RuntimeException("库存不足，可用库存: " + availableQuantity + "，需要数量: " + request.getQuantity());
        }

        // 记录操作前的库存状态
        Integer beforeAvailable = availableQuantity;
        Integer beforeReserved = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        Integer beforeDamaged = inventory.getDamagedQuantity() != null ? inventory.getDamagedQuantity() : 0;

        // 更新库存
        inventory.setAvailableQuantity(beforeAvailable - request.getQuantity());
        inventory.setLastStockOut(LocalDateTime.now());
        inventory.setUpdatedAt(LocalDateTime.now());

        productInventoryRepository.updateById(inventory);

        // 创建操作记录
        InventoryOperation operation = createOperationRecord(request, beforeAvailable, beforeReserved, beforeDamaged,
                inventory.getAvailableQuantity(), beforeReserved, beforeDamaged);

        log.info("商品出库成功，商品ID: {}, 仓库ID: {}, 数量: {}", 
                request.getProductId(), request.getWarehouseId(), request.getQuantity());
        
        return operation;
    }

    @Override
    @Transactional
    public InventoryOperation adjustStock(StockOperationRequest request) {
        ProductInventory inventory = productInventoryRepository.findByProductIdAndWarehouseId(
                request.getProductId(), request.getWarehouseId());
        
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }

        // 记录操作前的库存状态
        Integer beforeAvailable = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        Integer beforeReserved = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        Integer beforeDamaged = inventory.getDamagedQuantity() != null ? inventory.getDamagedQuantity() : 0;

        // 更新库存（调整操作直接设置数量）
        inventory.setAvailableQuantity(request.getQuantity());
        inventory.setUpdatedAt(LocalDateTime.now());

        productInventoryRepository.updateById(inventory);

        // 创建操作记录
        InventoryOperation operation = createOperationRecord(request, beforeAvailable, beforeReserved, beforeDamaged,
                inventory.getAvailableQuantity(), beforeReserved, beforeDamaged);

        log.info("库存调整成功，商品ID: {}, 仓库ID: {}, 调整后数量: {}", 
                request.getProductId(), request.getWarehouseId(), request.getQuantity());
        
        return operation;
    }

    @Override
    @Transactional
    public InventoryOperation adjustStockComplete(StockAdjustmentRequest request) {
        // 获取库存记录
        ProductInventory inventory = productInventoryRepository.findByProductIdAndWarehouseId(
                request.getProductId(), request.getWarehouseId());
        
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }

        // 记录操作前的库存状态
        Integer beforeAvailable = inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0;
        Integer beforeReserved = inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0;
        Integer beforeDamaged = inventory.getDamagedQuantity() != null ? inventory.getDamagedQuantity() : 0;
        Integer beforeMinStockLevel = inventory.getMinStockLevel();

        // 更新库存
        if (request.getNewAvailableQuantity() != null) {
            inventory.setAvailableQuantity(request.getNewAvailableQuantity());
        }
        if (request.getNewReservedQuantity() != null) {
            inventory.setReservedQuantity(request.getNewReservedQuantity());
        }
        if (request.getNewMinStockLevel() != null) {
            inventory.setMinStockLevel(request.getNewMinStockLevel());
        }
        inventory.setUpdatedAt(LocalDateTime.now());

        productInventoryRepository.updateById(inventory);

        // 创建操作记录
        InventoryOperation operation = new InventoryOperation();
        operation.setOperationNo(generateOperationNo());
        operation.setOperationType("ADJUSTMENT");
        operation.setProductId(request.getProductId());
        operation.setWarehouseId(request.getWarehouseId());
        operation.setQuantity(0); // 调整操作的数量为0
        operation.setBeforeAvailableQuantity(beforeAvailable);
        operation.setAfterAvailableQuantity(inventory.getAvailableQuantity());
        operation.setBeforeReservedQuantity(beforeReserved);
        operation.setAfterReservedQuantity(inventory.getReservedQuantity());
        operation.setBeforeDamagedQuantity(beforeDamaged);
        operation.setAfterDamagedQuantity(beforeDamaged); // 损坏库存不变
        operation.setReason(request.getReason());
        operation.setRemark(request.getRemark());
        operation.setOperatorId(request.getOperatorId());
        operation.setOperationTime(LocalDateTime.now());
        operation.setCreatedAt(LocalDateTime.now());
        operation.setUpdatedAt(LocalDateTime.now());
        operation.setCreatedBy(request.getOperatorId());
        operation.setUpdatedBy(request.getOperatorId());
        operation.setDeleted(false);

        inventoryOperationRepository.insert(operation);

        log.info("库存调整完成，商品ID: {}, 仓库ID: {}, 可用库存: {}->{}, 预留库存: {}->{}, 最低库存: {}->{}", 
                request.getProductId(), request.getWarehouseId(), 
                beforeAvailable, inventory.getAvailableQuantity(),
                beforeReserved, inventory.getReservedQuantity(),
                beforeMinStockLevel, inventory.getMinStockLevel());
        
        return operation;
    }

    @Override
    @Transactional
    public InventoryOperation transferStock(StockOperationRequest request) {
        // 从源仓库出库
        StockOperationRequest outRequest = new StockOperationRequest();
        outRequest.setOperationType("STOCK_OUT");
        outRequest.setProductId(request.getProductId());
        outRequest.setWarehouseId(request.getWarehouseId());
        outRequest.setQuantity(request.getQuantity());
        outRequest.setReason("库存调拨出库");
        outRequest.setOperatorId(request.getOperatorId());
        
        stockOut(outRequest);

        // 到目标仓库入库
        StockOperationRequest inRequest = new StockOperationRequest();
        inRequest.setOperationType("STOCK_IN");
        inRequest.setProductId(request.getProductId());
        inRequest.setWarehouseId(request.getWarehouseId());
        inRequest.setQuantity(request.getQuantity());
        inRequest.setReason("库存调拨入库");
        inRequest.setOperatorId(request.getOperatorId());
        
        InventoryOperation operation = stockIn(inRequest);
        operation.setOperationType("TRANSFER");
        operation.setReason(request.getReason());
        
        log.info("库存调拨成功，商品ID: {}, 数量: {}", request.getProductId(), request.getQuantity());
        
        return operation;
    }

    @Override
    public List<ProductInventory> getInventoryByProductId(Long productId) {
        return productInventoryRepository.findByProductId(productId);
    }

    @Override
    public List<ProductInventory> getInventoryByWarehouseId(Long warehouseId) {
        return productInventoryRepository.findByWarehouseId(warehouseId);
    }

    @Override
    public ProductInventory getInventoryByProductAndWarehouse(Long productId, Long warehouseId) {
        return productInventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    @Override
    public PageResult<ProductInventory> getInventoryPage(int page, int size, Long productId, Long warehouseId,
                                                         String productName, String warehouseName, Long companyId) {

        // 构建基础查询条件
        QueryWrapper<ProductInventory> queryWrapper = new QueryWrapper<>();
        
        if (productId != null) {
            queryWrapper.eq("product_id", productId);
        }
        if (warehouseId != null) {
            queryWrapper.eq("warehouse_id", warehouseId);
        }

        // 实现公司级别的数据隔离
        if (companyId != null) {
            // 先查询该公司的仓库ID列表
            List<Long> companyWarehouseIds = getCompanyWarehouseIds(companyId);
            if (companyWarehouseIds.isEmpty()) {
                // 如果公司没有仓库，返回空结果
                return PageResult.of(new ArrayList<>(), 0L, page, size);
            }
            // 只查询该公司仓库的库存
            queryWrapper.in("warehouse_id", companyWarehouseIds);
        }
        
        queryWrapper.orderByDesc("updated_at");

        // 获取总记录数
        long total = productInventoryRepository.selectCount(queryWrapper);
        
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0L, page, size);
        }
        
        // 添加分页条件（手动实现分页）
        int offset = (page - 1) * size;
        queryWrapper.last(String.format("LIMIT %d, %d", offset, size));
        
        // 查询库存数据
        List<ProductInventory> inventories = productInventoryRepository.selectList(queryWrapper);
        //todo 1.0 根据result查询出对应的商品信息和仓库信息，进行数据封装返回

        PageResult<ProductInventory> pageResult = PageResult.of(inventories, total, page, size);
        return pageResult;
    }

    @Override
    public PageResult<InventoryListDTO> getInventoryListPage(int page, int size, Long productId, Long warehouseId,
                                                           String productName, String productSku, String warehouseName, Long companyId) {
        // 构建基础查询条件
        QueryWrapper<ProductInventory> queryWrapper = new QueryWrapper<>();
        
        if (productId != null) {
            queryWrapper.eq("product_id", productId);
        }
        if (warehouseId != null) {
            queryWrapper.eq("warehouse_id", warehouseId);
        }

        // 如果提供了商品SKU，先查询对应的商品ID
        if (StringUtils.hasText(productSku)) {
            QueryWrapper<Product> productQueryWrapper = new QueryWrapper<>();
            productQueryWrapper.like("sku", productSku.trim());
            if (companyId != null) {
                productQueryWrapper.eq("company_id", companyId);
            }
            List<Product> products = productRepository.selectList(productQueryWrapper);
            if (products.isEmpty()) {
                // 如果没有找到匹配的商品，返回空结果
                return PageResult.of(new ArrayList<>(), 0L, page, size);
            }
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            queryWrapper.in("product_id", productIds);
        }

        // 实现公司级别的数据隔离
        if (companyId != null) {
            // 先查询该公司的仓库ID列表
            List<Long> companyWarehouseIds = getCompanyWarehouseIds(companyId);
            if (companyWarehouseIds.isEmpty()) {
                // 如果公司没有仓库，返回空结果
                return PageResult.of(new ArrayList<>(), 0L, page, size);
            }
            // 只查询该公司仓库的库存
            queryWrapper.in("warehouse_id", companyWarehouseIds);
        }
        
        queryWrapper.orderByDesc("updated_at");

        // 获取总记录数
        long total = productInventoryRepository.selectCount(queryWrapper);
        
        if (total == 0) {
            return PageResult.of(new ArrayList<>(), 0L, page, size);
        }
        
        // 添加分页条件（手动实现分页）
        int offset = (page - 1) * size;
        queryWrapper.last(String.format("LIMIT %d, %d", offset, size));
        
        // 查询库存数据
        List<ProductInventory> inventories = productInventoryRepository.selectList(queryWrapper);
        
        // 转换为DTO并填充商品和仓库信息
        List<InventoryListDTO> dtoList = inventories.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        return PageResult.of(dtoList, total, page, size);
    }
    
    /**
     * 获取指定公司的仓库ID列表
     */
    private List<Long> getCompanyWarehouseIds(Long companyId) {
        try {
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id");
            queryWrapper.eq("company_id", companyId);
            queryWrapper.eq("status", "ACTIVE");
            queryWrapper.eq("deleted", false);
            
            List<Warehouse> warehouses = warehouseRepository.selectList(queryWrapper);
            return warehouses.stream()
                .map(Warehouse::getId)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询公司仓库失败，公司ID: {}", companyId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ProductInventory> getLowStockProducts() {
        return productInventoryRepository.findLowStockProducts();
    }

    @Override
    public List<ProductInventory> getProductsNeedingReorder() {
        return productInventoryRepository.findProductsNeedingReorder();
    }

    @Override
    @Transactional
    public boolean setStockAlertLevel(Long productId, Long warehouseId, Integer minStockLevel,
                                    Integer maxStockLevel, Integer reorderPoint, Long operatorId) {
        ProductInventory inventory = productInventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        if (inventory == null) {
            return false;
        }

        inventory.setMinStockLevel(minStockLevel);
        inventory.setMaxStockLevel(maxStockLevel);
        inventory.setReorderPoint(reorderPoint);
        inventory.setUpdatedAt(LocalDateTime.now());

        productInventoryRepository.updateById(inventory);
        log.info("设置库存预警线成功，商品ID: {}, 仓库ID: {}", productId, warehouseId);
        
        return true;
    }

    @Override
    public PageResult<InventoryOperation> getOperationHistory(int page, int size, Long productId, Long warehouseId,
                                                       String operationType, String startTime, String endTime) {
        QueryWrapper<InventoryOperation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);

        if (productId != null) {
            queryWrapper.eq("product_id", productId);
        }
        if (warehouseId != null) {
            queryWrapper.eq("warehouse_id", warehouseId);
        }
        if (StringUtils.hasText(operationType)) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            queryWrapper.between("operation_time", startTime, endTime);
        }

        queryWrapper.orderByDesc("operation_time");

        Page<InventoryOperation> pageParam = new Page<>(page, size);
        Page<InventoryOperation> list =inventoryOperationRepository.selectPage(pageParam, queryWrapper);

        return PageResult.of(list.getRecords(), list.getTotal(), page, size);
    }

    /**
     * 获取或创建库存记录
     */
    private ProductInventory getOrCreateInventory(Long productId, Long warehouseId) {
        ProductInventory inventory = productInventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
        
        if (inventory == null) {
            inventory = new ProductInventory();
            inventory.setProductId(productId);
            inventory.setWarehouseId(warehouseId);
            inventory.setAvailableQuantity(0);
            inventory.setReservedQuantity(0);
            inventory.setDamagedQuantity(0);
            inventory.setCreatedAt(LocalDateTime.now());
            inventory.setUpdatedAt(LocalDateTime.now());
            
            productInventoryRepository.insert(inventory);
        }
        
        return inventory;
    }

    /**
     * 创建操作记录
     */
    private InventoryOperation createOperationRecord(StockOperationRequest request, 
                                                   Integer beforeAvailable, Integer beforeReserved, Integer beforeDamaged,
                                                   Integer afterAvailable, Integer afterReserved, Integer afterDamaged) {
        InventoryOperation operation = new InventoryOperation();
        operation.setOperationNo(generateOperationNo());
        operation.setOperationType(request.getOperationType());
        operation.setProductId(request.getProductId());
        operation.setWarehouseId(request.getWarehouseId());
        operation.setQuantity(request.getQuantity());
        operation.setBeforeAvailableQuantity(beforeAvailable);
        operation.setAfterAvailableQuantity(afterAvailable);
        operation.setBeforeReservedQuantity(beforeReserved);
        operation.setAfterReservedQuantity(afterReserved);
        operation.setBeforeDamagedQuantity(beforeDamaged);
        operation.setAfterDamagedQuantity(afterDamaged);
        operation.setUnitPrice(request.getUnitPrice());
        operation.setTotalAmount(request.getUnitPrice() != null ? 
                request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())) : null);
        operation.setOrderId(request.getOrderId());
        operation.setOrderItemId(request.getOrderItemId());
        operation.setReason(request.getReason());
        operation.setRemark(request.getRemark());
        operation.setOperatorId(request.getOperatorId());
        operation.setOperationTime(LocalDateTime.now());
        operation.setCreatedAt(LocalDateTime.now());
        operation.setUpdatedAt(LocalDateTime.now());
        operation.setCreatedBy(request.getOperatorId());
        operation.setUpdatedBy(request.getOperatorId());
        operation.setDeleted(false);

        inventoryOperationRepository.insert(operation);
        return operation;
    }

    /**
     * 生成操作单号
     */
    private String generateOperationNo() {
        return "OP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 将ProductInventory转换为InventoryListDTO
     */
    private InventoryListDTO convertToDTO(ProductInventory inventory) {
        InventoryListDTO dto = new InventoryListDTO();
        
        // 复制库存基本信息
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setWarehouseId(inventory.getWarehouseId());
        dto.setAvailableQuantity(inventory.getAvailableQuantity());
        dto.setReservedQuantity(inventory.getReservedQuantity());
        dto.setDamagedQuantity(inventory.getDamagedQuantity());
        dto.setMinStockLevel(inventory.getMinStockLevel());
        dto.setMaxStockLevel(inventory.getMaxStockLevel());
        dto.setReorderPoint(inventory.getReorderPoint());
        dto.setLastStockIn(inventory.getLastStockIn());
        dto.setLastStockOut(inventory.getLastStockOut());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        
        // 查询并填充商品信息
        try {
            Product product = productRepository.selectById(inventory.getProductId());
            if (product != null) {
                dto.setProductSku(product.getSku());
                dto.setProductName(product.getName());
                dto.setProductUnit(product.getUnit());
            }
        } catch (Exception e) {
            log.warn("查询商品信息失败，商品ID: {}", inventory.getProductId(), e);
        }
        
        // 查询并填充仓库信息
        try {
            Warehouse warehouse = warehouseRepository.selectById(inventory.getWarehouseId());
            if (warehouse != null) {
                dto.setWarehouseCode(warehouse.getWarehouseCode());
                dto.setWarehouseName(warehouse.getWarehouseName());
            }
        } catch (Exception e) {
            log.warn("查询仓库信息失败，仓库ID: {}", inventory.getWarehouseId(), e);
        }
        
        return dto;
    }
}
