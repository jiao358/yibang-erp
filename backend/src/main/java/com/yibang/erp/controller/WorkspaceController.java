package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.entity.Order;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.entity.ProductInventory;
import com.yibang.erp.domain.service.WorkspaceService;
import com.yibang.erp.infrastructure.repository.OrderRepository;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import com.yibang.erp.infrastructure.repository.ProductInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/workspace")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;
    private final JwtUtil jwtUtil;

    /**
     * 获取供应链管理员统计数据
     */
    @GetMapping("/supplier-stats")
    @PreAuthorize("hasRole('SUPPLIER_ADMIN') or hasRole('SUPPLIER_OPERATOR')")
    public ResponseEntity<Map<String, Object>> getSupplierStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            Map<String, Object> stats = workspaceService.getSupplierStats(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取供应链统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取系统管理员统计数据
     */
    @GetMapping("/system-stats")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Map<String, Object> stats = workspaceService.getSystemStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取系统统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取销售统计数据
     */
    @GetMapping("/sales-stats")
    @PreAuthorize("hasRole('SALES_ADMIN') or hasRole('SALES')")
    public ResponseEntity<Map<String, Object>> getSalesStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            Map<String, Object> stats = workspaceService.getSalesStats(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取销售统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取基础统计数据
     */
    @GetMapping("/basic-stats")
    public ResponseEntity<Map<String, Object>> getBasicStats(@RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            Map<String, Object> stats = workspaceService.getBasicStats(companyId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取基础统计数据失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取最近订单
     */
    @GetMapping("/recent-orders")
    public ResponseEntity<Map<String, Object>> getRecentOrders(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> orders = workspaceService.getRecentOrders(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最近订单失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取最近商品
     */
    @GetMapping("/recent-products")
    public ResponseEntity<Map<String, Object>> getRecentProducts(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> products = workspaceService.getRecentProducts(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", products);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近商品失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最近商品失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取库存预警商品
     */
    @GetMapping("/low-stock-items")
    public ResponseEntity<Map<String, Object>> getLowStockItems(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> items = workspaceService.getLowStockItems(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", items);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取库存预警商品失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取库存预警商品失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待审核订单
     */
    @GetMapping("/pending-orders")
    public ResponseEntity<Map<String, Object>> getPendingOrders(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> orders = workspaceService.getPendingOrders(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取待审核订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取待审核订单失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取待发货订单
     */
    @GetMapping("/pending-shipments")
    public ResponseEntity<Map<String, Object>> getPendingShipments(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> orders = workspaceService.getPendingShipments(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取待发货订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取待发货订单失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取我的订单（销售用户）
     */
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('SALES_ADMIN') or hasRole('SALES')")
    public ResponseEntity<Map<String, Object>> getMyOrders(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            Long userId = extractUserIdFromJWT(authHeader);
            List<Map<String, Object>> orders = workspaceService.getMyOrders(companyId, userId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取我的订单失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取我的订单失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取最近客户
     */
    @GetMapping("/recent-customers")
    public ResponseEntity<Map<String, Object>> getRecentCustomers(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> customers = workspaceService.getRecentCustomers(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", customers);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近客户失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最近客户失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取AI导入历史
     */
    @GetMapping("/recent-ai-tasks")
    public ResponseEntity<Map<String, Object>> getRecentAITasks(
            @RequestParam(defaultValue = "5") int limit,
            @RequestHeader("Authorization") String authHeader) {
        try {
            Long companyId = extractCompanyIdFromJWT(authHeader);
            List<Map<String, Object>> tasks = workspaceService.getRecentAITasks(companyId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", tasks);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取AI导入历史失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取AI导入历史失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从JWT中提取公司ID
     */
    private Long extractCompanyIdFromJWT(String authHeader) {
        try {
            // 从Authorization header中提取Bearer token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // 移除"Bearer "前缀
                return jwtUtil.getCompanyIdFromToken(token);
            } else {
                log.warn("Authorization header格式不正确: {}", authHeader);
                return 1L; // 返回默认公司ID
            }
        } catch (Exception e) {
            log.error("解析JWT失败", e);
            return 1L; // 返回默认公司ID
        }
    }

    /**
     * 从JWT中提取用户ID
     */
    private Long extractUserIdFromJWT(String authHeader) {
        try {
            // 从Authorization header中提取Bearer token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7); // 移除"Bearer "前缀
                return jwtUtil.getUserIdFromToken(token);
            } else {
                log.warn("Authorization header格式不正确: {}", authHeader);
                return 1L; // 返回默认用户ID
            }
        } catch (Exception e) {
            log.error("解析JWT失败", e);
            return 1L; // 返回默认用户ID
        }
    }
}
