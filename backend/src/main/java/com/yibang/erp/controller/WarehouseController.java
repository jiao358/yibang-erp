package com.yibang.erp.controller;

import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.dto.WarehouseRequest;
import com.yibang.erp.domain.entity.Warehouse;
import com.yibang.erp.domain.service.WarehouseService;
import com.yibang.erp.common.response.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仓库管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
public class WarehouseController {

    private final WarehouseService warehouseService;

    private final JwtUtil jwtUtil;

    /**
     * 创建仓库
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createWarehouse(@RequestBody WarehouseRequest request,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            // 从JWT中提取用户ID
            Long operatorId = extractUserIdFromJWT(authHeader);
            Warehouse warehouse = warehouseService.createWarehouse(request, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", warehouse);
            response.put("message", "仓库创建成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建仓库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建仓库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新仓库
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateWarehouse(@PathVariable Long id,
                                                             @RequestBody WarehouseRequest request,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            Warehouse warehouse = warehouseService.updateWarehouse(id, request, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", warehouse);
            response.put("message", "仓库更新成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新仓库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新仓库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除仓库
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteWarehouse(@PathVariable Long id,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = warehouseService.deleteWarehouse(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "仓库删除成功" : "仓库删除失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除仓库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除仓库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID查询仓库
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWarehouseById(@PathVariable Long id) {
        try {
            Warehouse warehouse = warehouseService.getWarehouseById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", warehouse);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询仓库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询仓库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 分页查询仓库列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWarehousePage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) String warehouseType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long companyId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 自动获取当前用户公司ID，确保数据隔离
            Long currentUserCompanyId = extractCompanyIdFromJWT(authHeader);
            if (companyId == null) {
                companyId = currentUserCompanyId;
            } else if (!companyId.equals(currentUserCompanyId) && !hasSystemAdminRole(authHeader)) {
                // 非系统管理员不能查看其他公司的仓库
                companyId = currentUserCompanyId;
            }
            
            var pageResult = warehouseService.getWarehousePage(page, size, warehouseCode, warehouseName, 
                                                             warehouseType, status, companyId);
            
            PageResult<Warehouse> result = new PageResult<>();
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
            log.error("查询仓库列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询仓库列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据公司ID查询仓库列表
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<Map<String, Object>> getWarehousesByCompanyId(@PathVariable Long companyId) {
        try {
            List<Warehouse> warehouses = warehouseService.getWarehousesByCompanyId(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", warehouses);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询公司仓库列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询公司仓库列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 启用仓库
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> activateWarehouse(@PathVariable Long id,
                                                               @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = warehouseService.activateWarehouse(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "仓库启用成功" : "仓库启用失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("启用仓库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "启用仓库失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 停用仓库
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateWarehouse(@PathVariable Long id,
                                                                 @RequestHeader("Authorization") String authHeader) {
        try {
            Long operatorId = extractUserIdFromJWT(authHeader);
            boolean success = warehouseService.deactivateWarehouse(id, operatorId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "仓库停用成功" : "仓库停用失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("停用仓库失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "停用仓库失败: " + e.getMessage());
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
    
    /**
     * 从JWT中提取公司ID的辅助方法
     */
    private Long extractCompanyIdFromJWT(String authHeader) {
        // TODO: 实现从JWT中提取公司ID的逻辑
        // 这里需要根据你的JWT实现来完善

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的JWT令牌");
        }

        String token = authHeader.substring(7);
        Long belongCompayId = jwtUtil.getCompanyIdFromToken(token);

        return belongCompayId;
    }
    
    /**
     * 检查用户是否有系统管理员角色
     */
    private boolean hasSystemAdminRole(String authHeader) {
        // TODO: 实现从JWT中解析角色的逻辑
        // 这里需要根据你的JWT实现来完善
        boolean isAdmin=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SYSTEM_ADMIN"));

        return isAdmin; // 临时返回true，实际应该从JWT中解析角色
    }
}
