package com.yibang.erp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yibang.erp.domain.dto.ErrorOrderInfo;
import com.yibang.erp.domain.service.AIExcelErrorOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI Excel错误订单控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-excel-error-orders")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES_ADMIN', 'SALES')")
public class AIExcelErrorOrderController {

    @Autowired
    private AIExcelErrorOrderService errorOrderService;

    /**
     * 根据任务ID获取错误订单列表
     */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getErrorOrdersByTaskId(@PathVariable String taskId) {
        try {
            log.info("获取任务错误订单列表: taskId={}", taskId);
            
            // 获取错误订单列表
            var errorOrders = errorOrderService.getErrorOrdersByTaskId(taskId);
            
            // 获取错误统计信息
            var statistics = errorOrderService.getErrorOrderStatistics(taskId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", Map.of(
                    "errorOrders", errorOrders,
                    "statistics", statistics
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取任务错误订单列表失败: taskId={}", taskId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取错误订单列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 分页获取用户的错误订单列表
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserErrorOrders(
            @RequestParam Long userId,
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            log.info("获取用户错误订单列表: userId={}, companyId={}, page={}, size={}", 
                    userId, companyId, page, size);
            
            IPage<ErrorOrderInfo> errorOrders = errorOrderService.getUserErrorOrders(userId, companyId, page, size);
            
            // 获取用户错误统计信息
            var statistics = errorOrderService.getUserErrorOrderStatistics(userId, companyId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", Map.of(
                    "errorOrders", errorOrders,
                    "statistics", statistics
                )
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户错误订单列表失败: userId={}, companyId={}", userId, companyId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取错误订单列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据错误类型获取错误订单
     */
    @GetMapping("/type/{errorType}")
    public ResponseEntity<Map<String, Object>> getErrorOrdersByType(@PathVariable String errorType) {
        try {
            log.info("根据错误类型获取错误订单: errorType={}", errorType);
            
            var errorOrders = errorOrderService.getErrorOrdersByType(errorType);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", errorOrders
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("根据错误类型获取错误订单失败: errorType={}", errorType, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取错误订单失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 根据状态获取错误订单
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getErrorOrdersByStatus(@PathVariable String status) {
        try {
            log.info("根据状态获取错误订单: status={}", status);
            
            var errorOrders = errorOrderService.getErrorOrdersByStatus(status);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", errorOrders
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("根据状态获取错误订单失败: status={}", status, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取错误订单失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 标记错误订单为已处理
     */
    @PostMapping("/{errorOrderId}/process")
    public ResponseEntity<Map<String, Object>> markAsProcessed(
            @PathVariable Long errorOrderId,
            @RequestParam Long operatorId) {
        try {
            log.info("标记错误订单为已处理: errorOrderId={}, operatorId={}", errorOrderId, operatorId);
            
            errorOrderService.markErrorOrderAsProcessed(errorOrderId, operatorId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "错误订单已标记为已处理"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("标记错误订单为已处理失败: errorOrderId={}", errorOrderId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "标记失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 标记错误订单为已忽略
     */
    @PostMapping("/{errorOrderId}/ignore")
    public ResponseEntity<Map<String, Object>> markAsIgnored(
            @PathVariable Long errorOrderId,
            @RequestParam Long operatorId) {
        try {
            log.info("标记错误订单为已忽略: errorOrderId={}, operatorId={}", errorOrderId, operatorId);
            
            errorOrderService.markErrorOrderAsIgnored(errorOrderId, operatorId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "错误订单已标记为已忽略"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("标记错误订单为已忽略失败: errorOrderId={}", errorOrderId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "标记失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取错误订单统计信息
     */
    @GetMapping("/statistics/task/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskErrorStatistics(@PathVariable String taskId) {
        try {
            log.info("获取任务错误统计信息: taskId={}", taskId);
            
            var statistics = errorOrderService.getErrorOrderStatistics(taskId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", statistics
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取任务错误统计信息失败: taskId={}", taskId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户错误订单统计信息
     */
    @GetMapping("/statistics/user")
    public ResponseEntity<Map<String, Object>> getUserErrorStatistics(
            @RequestParam Long userId,
            @RequestParam Long companyId) {
        try {
            log.info("获取用户错误统计信息: userId={}, companyId={}", userId, companyId);
            
            var statistics = errorOrderService.getUserErrorOrderStatistics(userId, companyId);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "data", statistics
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户错误统计信息失败: userId={}, companyId={}", userId, companyId, e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "获取统计信息失败: " + e.getMessage()
            ));
        }
    }
}
