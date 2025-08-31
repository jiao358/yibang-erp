package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.InventoryAlertRequest;
import com.yibang.erp.domain.entity.InventoryAlert;
import com.yibang.erp.domain.service.InventoryAlertService;
import com.yibang.erp.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存预警控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory-alerts")
@RequiredArgsConstructor
public class InventoryAlertController {

    private final InventoryAlertService inventoryAlertService;

    /**
     * 创建库存预警
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> createAlert(@RequestBody InventoryAlertRequest request,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            InventoryAlert alert = inventoryAlertService.createAlert(request, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", alert);
            response.put("message", "库存预警创建成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建库存预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新库存预警
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateAlert(@PathVariable Long id,
                                                          @RequestBody InventoryAlertRequest request,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            InventoryAlert alert = inventoryAlertService.updateAlert(id, request, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", alert);
            response.put("message", "库存预警更新成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新库存预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除库存预警
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteAlert(@PathVariable Long id,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryAlertService.deleteAlert(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "库存预警删除成功" : "库存预警删除失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除库存预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID查询库存预警
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAlertById(@PathVariable Long id) {
        try {
            InventoryAlert alert = inventoryAlertService.getAlertById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", alert);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询库存预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 分页查询库存预警列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAlertPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String alertType,
            @RequestParam(required = false) String alertLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Long warehouseId) {
        try {
            var pageResult = inventoryAlertService.getAlertPage(page, size, alertType, alertLevel, 
                                                              status, productId, warehouseId);
            
            PageResult<InventoryAlert> result = new PageResult<>();
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
            log.error("查询库存预警列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询库存预警列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询待处理的预警
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingAlerts() {
        try {
            List<InventoryAlert> pendingAlerts = inventoryAlertService.getPendingAlerts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pendingAlerts);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询待处理预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询待处理预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 查询高优先级预警
     */
    @GetMapping("/high-priority")
    public ResponseEntity<Map<String, Object>> getHighPriorityAlerts() {
        try {
            List<InventoryAlert> highPriorityAlerts = inventoryAlertService.getHighPriorityAlerts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", highPriorityAlerts);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询高优先级预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询高优先级预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 处理库存预警
     */
    @PostMapping("/{id}/handle")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> handleAlert(@PathVariable Long id,
                                                          @RequestParam String handlingResult,
                                                          @RequestParam(required = false) String handlingRemark,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long handlerId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryAlertService.handleAlert(id, handlingResult, handlingRemark, handlerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "库存预警处理成功" : "库存预警处理失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("处理库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理库存预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 忽略库存预警
     */
    @PostMapping("/{id}/ignore")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> ignoreAlert(@PathVariable Long id,
                                                          @RequestParam String reason,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryAlertService.ignoreAlert(id, reason, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "库存预警已忽略" : "忽略库存预警失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("忽略库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "忽略库存预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 自动检测并创建库存预警
     */
    @PostMapping("/auto-detect")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> autoDetectAndCreateAlerts() {
        try {
            inventoryAlertService.autoDetectAndCreateAlerts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "自动检测库存预警完成");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("自动检测库存预警失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "自动检测库存预警失败: " + e.getMessage());
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
        return 1L; // 临时返回默认值
    }
}
