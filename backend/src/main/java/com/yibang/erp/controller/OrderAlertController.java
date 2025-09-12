package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.dto.ManualProcessingRequest;
import com.yibang.erp.domain.dto.OrderAlertRequest;
import com.yibang.erp.domain.dto.OrderAlertResponse;
import com.yibang.erp.domain.service.OrderAlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单预警管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/order-alerts")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
public class OrderAlertController {

    @Autowired
    private OrderAlertService orderAlertService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询订单预警列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getOrderAlerts(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String processingType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String orderNumber,
            @RequestParam(required = false) String sourceOrderId,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // 构建查询请求
            OrderAlertRequest queryRequest = new OrderAlertRequest();
            queryRequest.setCurrent(current);
            queryRequest.setSize(size);
            queryRequest.setStatus(status);
            queryRequest.setProcessingType(processingType);
            queryRequest.setStartDate(startDate);
            queryRequest.setEndDate(endDate);
            queryRequest.setOrderNumber(orderNumber);
            queryRequest.setSourceOrderId(sourceOrderId);
            queryRequest.setSortField(sortField);
            queryRequest.setSortOrder(sortOrder);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少有效的Authorization头"));
            }

            String token = authHeader.substring(7);
            Long companyId = jwtUtil.getCompanyIdFromToken(token);
            Long userId = jwtUtil.getUserIdFromToken(token);

            // 查询预警列表
            PageResult<OrderAlertResponse> alertPage = orderAlertService.getOrderAlerts(queryRequest, companyId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", alertPage);
            response.put("message", "获取订单预警列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取订单预警列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取订单预警列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 分配预警给用户
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<Map<String, Object>> assignAlert(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7);
            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            
            orderAlertService.assignAlert(id, userId, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "分配预警成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("分配预警失败: alertId={}, userId={}", id, userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "分配预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 开始处理预警
     */
    @PostMapping("/{id}/process")
    public ResponseEntity<Map<String, Object>> processAlert(
            @PathVariable Long id,
            @RequestBody ManualProcessingRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7);
            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            
            orderAlertService.processAlert(id, request, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "开始处理预警成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("开始处理预警失败: alertId={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "开始处理预警失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 完成预警处理
     */
    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeAlert(
            @PathVariable Long id,
            @RequestBody ManualProcessingRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7);
            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            
            orderAlertService.completeAlert(id, request, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "完成预警处理成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("完成预警处理失败: alertId={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "完成预警处理失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 拒绝预警处理
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectAlert(
            @PathVariable Long id,
            @RequestBody ManualProcessingRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7);
            Long currentUserId = jwtUtil.getUserIdFromToken(token);
            
            orderAlertService.rejectAlert(id, request, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "拒绝预警处理成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("拒绝预警处理失败: alertId={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "拒绝预警处理失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
