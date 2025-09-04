package com.yibang.erp.controller;

import com.yibang.erp.common.util.UserSecurityUtils;
import com.yibang.erp.domain.dto.InventoryCheckRequest;
import com.yibang.erp.domain.entity.InventoryCheck;
import com.yibang.erp.domain.entity.InventoryCheckItem;
import com.yibang.erp.domain.service.InventoryCheckService;
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
 * 库存盘点控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory-checks")
@RequiredArgsConstructor
public class InventoryCheckController {

    private final InventoryCheckService inventoryCheckService;

    /**
     * 创建库存盘点
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> createCheck(@RequestBody InventoryCheckRequest request,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            InventoryCheck check = inventoryCheckService.createCheck(request, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", check);
            response.put("message", "库存盘点创建成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建库存盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建库存盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新库存盘点
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCheck(@PathVariable Long id,
                                                          @RequestBody InventoryCheckRequest request,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            InventoryCheck check = inventoryCheckService.updateCheck(id, request, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", check);
            response.put("message", "库存盘点更新成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新库存盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新库存盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除库存盘点
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCheck(@PathVariable Long id,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryCheckService.deleteCheck(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "库存盘点删除成功" : "库存盘点删除失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除库存盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除库存盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID查询库存盘点
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCheckById(@PathVariable Long id) {
        try {
            InventoryCheck check = inventoryCheckService.getCheckById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", check);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询库存盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询库存盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 分页查询库存盘点列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCheckPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String checkType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            var pageResult = inventoryCheckService.getCheckPage(page, size, checkType, status, 
                                                              warehouseId, startTime, endTime);
            
            PageResult<InventoryCheck> result = new PageResult<>();
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
            log.error("查询库存盘点列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询库存盘点列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 开始盘点
     */
    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> startCheck(@PathVariable Long id,
                                                         @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryCheckService.startCheck(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "盘点开始成功" : "盘点开始失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("开始盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "开始盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 完成盘点
     */
    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> completeCheck(@PathVariable Long id,
                                                            @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryCheckService.completeCheck(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "盘点完成成功" : "盘点完成失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("完成盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "完成盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 取消盘点
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> cancelCheck(@PathVariable Long id,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryCheckService.cancelCheck(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "盘点取消成功" : "盘点取消失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("取消盘点失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "取消盘点失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 审核盘点
     */
    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> reviewCheck(@PathVariable Long id,
                                                          @RequestParam String reviewStatus,
                                                          @RequestParam(required = false) String reviewComment,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Long reviewerId = extractUserIdFromJWT(authHeader);
            boolean success = inventoryCheckService.reviewCheck(id, reviewStatus, reviewComment, reviewerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "盘点审核成功" : "盘点审核失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("盘点审核失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "盘点审核失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取盘点明细
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> getCheckItems(@PathVariable Long id) {
        try {
            List<InventoryCheckItem> items = inventoryCheckService.getCheckItems(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", items);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询盘点明细失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询盘点明细失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取有差异的盘点明细
     */
    @GetMapping("/items/with-difference")
    public ResponseEntity<Map<String, Object>> getItemsWithDifference() {
        try {
            List<InventoryCheckItem> items = inventoryCheckService.getItemsWithDifference();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", items);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询有差异的盘点明细失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询有差异的盘点明细失败: " + e.getMessage());
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
        return UserSecurityUtils.getCurrentUserId();

    }
}
