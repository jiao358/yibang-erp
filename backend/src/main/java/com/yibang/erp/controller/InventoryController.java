package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.dto.StockOperationRequest;
import com.yibang.erp.domain.dto.InventoryListDTO;
import com.yibang.erp.domain.dto.StockAdjustmentRequest;
import com.yibang.erp.domain.entity.InventoryOperation;
import com.yibang.erp.domain.entity.ProductInventory;
import com.yibang.erp.domain.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;
    private final JwtUtil jwtUtil;

    /**
     * 商品入库
     */
    @PostMapping("/stock-in")
    public ResponseEntity<Map<String, Object>> stockIn(@RequestBody StockOperationRequest request,
                                                      @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            request.setOperatorId(operatorId);
            
            InventoryOperation operation = inventoryService.stockIn(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", operation);
            response.put("message", "商品入库成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("商品入库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "商品入库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 商品出库
     */
    @PostMapping("/stock-out")
    public ResponseEntity<Map<String, Object>> stockOut(@RequestBody StockOperationRequest request,
                                                       @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            request.setOperatorId(operatorId);
            
            InventoryOperation operation = inventoryService.stockOut(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", operation);
            response.put("message", "商品出库成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("商品出库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "商品出库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 库存调整
     */
    @PostMapping("/adjust")
    public ResponseEntity<Map<String, Object>> adjustStock(@RequestBody StockOperationRequest request,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            request.setOperatorId(operatorId);
            
            InventoryOperation operation = inventoryService.adjustStock(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", operation);
            response.put("message", "库存调整成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("库存调整失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "库存调整失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 库存调整（完整版）
     */
    @PostMapping("/adjust-complete")
    public ResponseEntity<Map<String, Object>> adjustStockComplete(@RequestBody StockAdjustmentRequest request,
                                                                 @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            request.setOperatorId(operatorId);
            
            InventoryOperation operation = inventoryService.adjustStockComplete(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", operation);
            response.put("message", "库存调整成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("库存调整失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "库存调整失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 库存调拨
     */
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transferStock(@RequestBody StockOperationRequest request,
                                                            @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            request.setOperatorId(operatorId);
            
            InventoryOperation operation = inventoryService.transferStock(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", operation);
            response.put("message", "库存调拨成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("库存调拨失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "库存调拨失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据商品ID查询库存
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getInventoryByProductId(@PathVariable Long productId) {
        try {
            List<ProductInventory> inventoryList = inventoryService.getInventoryByProductId(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", inventoryList);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询商品库存失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询商品库存失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据仓库ID查询库存
     */
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<Map<String, Object>> getInventoryByWarehouseId(@PathVariable Long warehouseId) {
        try {
            List<ProductInventory> inventoryList = inventoryService.getInventoryByWarehouseId(warehouseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", inventoryList);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询仓库库存失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询仓库库存失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 分页查询库存列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getInventoryPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productSku,
            @RequestParam(required = false) String warehouseName,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 自动获取当前用户公司ID，确保数据隔离
            Long currentUserCompanyId = extractCompanyIdFromJWT(authHeader);
            
            PageResult<InventoryListDTO> pageResult = inventoryService.getInventoryListPage(page, size, productId, warehouseId,
                                                             productName, productSku, warehouseName, currentUserCompanyId);
            
            PageResult<InventoryListDTO> result = new PageResult<>();
            result.setRecords(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setCurrent(pageResult.getCurrent());
            result.setSize(pageResult.getSize());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询库存列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询库存列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询库存不足的商品
     */
    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts() {
        try {
            List<ProductInventory> lowStockProducts = inventoryService.getLowStockProducts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", lowStockProducts);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询库存不足商品失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询库存不足商品失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询需要补货的商品
     */
    @GetMapping("/need-reorder")
    public ResponseEntity<Map<String, Object>> getProductsNeedingReorder() {
        try {
            List<ProductInventory> needReorderProducts = inventoryService.getProductsNeedingReorder();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", needReorderProducts);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询需要补货商品失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询需要补货商品失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 设置库存预警线
     */
    @PostMapping("/alert-level")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> setStockAlertLevel(
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam Integer minStockLevel,
            @RequestParam Integer maxStockLevel,
            @RequestParam Integer reorderPoint,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryService.setStockAlertLevel(productId, warehouseId, minStockLevel, 
                                                               maxStockLevel, reorderPoint, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "设置库存预警线成功" : "设置库存预警线失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("设置库存预警线失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "设置库存预警线失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询库存操作记录
     */
    @GetMapping("/operations")
    public ResponseEntity<Map<String, Object>> getOperationHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            var pageResult = inventoryService.getOperationHistory(page, size, productId, warehouseId, 
                                                                operationType, startTime, endTime);
            
            PageResult<InventoryOperation> result = new PageResult<>();
            result.setRecords(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setCurrent(pageResult.getCurrent());
            result.setSize(pageResult.getSize());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询操作记录失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询操作记录失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从JWT中提取用户ID的辅助方法
     */
    private Long extractUserIdFromJWT(String authHeader) {
        // TODO: 实现从JWT中提取用户ID的逻辑
        // 这里需要根据你的JWT实现来完善
        String token = authHeader.substring(7);
        Long userIdFromToken=jwtUtil.getUserIdFromToken(token);


        return userIdFromToken; // 临时返回默认值
    }
    
    /**
     * 从JWT中提取公司ID的辅助方法
     */
    private Long extractCompanyIdFromJWT(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("无效的Authorization头");
            }
            
            String token = authHeader.substring(7);
            Long companyId=jwtUtil.getCompanyIdFromToken(token);

            return companyId;

        } catch (Exception e) {
            log.error("从JWT中提取公司ID失败", e);
            throw new RuntimeException("从JWT中提取公司ID失败: " + e.getMessage());
        }
    }
}
