package com.yibang.erp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yibang.erp.domain.dto.*;
import com.yibang.erp.domain.entity.OrderStatusLog;
import com.yibang.erp.domain.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 订单管理控制器
 */
@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     */
    @PostMapping

    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新订单
     */
    @PutMapping("/{orderId}")

    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateRequest request) {
        OrderResponse response = orderService.updateOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{orderId}")

    public ResponseEntity<Boolean> deleteOrder(@PathVariable Long orderId) {
        boolean result = orderService.deleteOrder(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取订单
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        OrderResponse response = orderService.getOrderById(orderId);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据平台订单号获取订单
     */
    @GetMapping("/platform-order/{platformOrderNo}")
    public ResponseEntity<OrderResponse> getOrderByPlatformOrderNo(@PathVariable String platformOrderNo) {
        OrderResponse response = orderService.getOrderByPlatformOrderNo(platformOrderNo);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 分页查询订单列表
     */
    @GetMapping
    public ResponseEntity<IPage<OrderResponse>> getOrderList(OrderListRequest request) {
        IPage<OrderResponse> response = orderService.getOrderList(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 提交订单
     */
    @PostMapping("/{orderId}/submit")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<OrderResponse> submitOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.submitOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 供应商确认订单
     */
    @PostMapping("/{orderId}/supplier-confirm")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<OrderResponse> supplierConfirmOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.supplierConfirmOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 供应商发货
     */
    @PostMapping("/{orderId}/supplier-ship")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<OrderResponse> supplierShipOrder(@PathVariable Long orderId) {
        OrderResponse response = orderService.supplierShipOrder(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 解决订单冲突
     */
    @PostMapping("/{orderId}/resolve-conflict")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OrderResponse> resolveOrderConflict(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderConflictResolutionRequest request) {
        OrderResponse response = orderService.resolveOrderConflict(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量处理订单
     */
    @PostMapping("/batch-process")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<OrderBatchProcessResponse> batchProcessOrders(
            @Valid @RequestBody OrderBatchProcessRequest request) {
        OrderBatchProcessResponse response = orderService.batchProcessOrders(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据销售用户ID获取订单列表
     */
    @GetMapping("/sales-user/{salesUserId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<List<OrderResponse>> getOrdersBySalesUserId(@PathVariable Long salesUserId) {
        List<OrderResponse> response = orderService.getOrdersBySalesUserId(salesUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据供应商公司ID获取订单列表
     */
    @GetMapping("/supplier-company/{supplierCompanyId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<List<OrderResponse>> getOrdersBySupplierCompanyId(@PathVariable Long supplierCompanyId) {
        List<OrderResponse> response = orderService.getOrdersBySupplierCompanyId(supplierCompanyId);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据客户ID获取订单列表
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderResponse> response = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    /**
     * 检查订单状态转换是否有效
     */
    @GetMapping("/{orderId}/status-transition-valid")
    public ResponseEntity<Boolean> isValidStatusTransition(
            @PathVariable Long orderId,
            @RequestParam String targetStatus) {
        boolean result = orderService.isValidStatusTransition(String.valueOf(orderId), targetStatus);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取订单状态历史
     */
    @GetMapping("/{orderId}/status-history")
    public ResponseEntity<List<OrderStatusLog>> getOrderStatusHistory(@PathVariable Long orderId) {
        List<OrderStatusLog> response = orderService.getOrderStatusHistory(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 生成平台订单号
     */
    @GetMapping("/generate-order-no")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<String> generatePlatformOrderNo() {
        String orderNo = orderService.generatePlatformOrderNo();
        return ResponseEntity.ok(orderNo);
    }

    /**
     * 生成平台订单号（指定登录用户和渠道）
     */
    @PostMapping("/generate-order-no")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<String> generatePlatformOrderNoWithParams(
            @RequestParam Long accountId,
            @RequestParam String orderSource) {
        String orderNo = orderService.generatePlatformOrderNo(accountId, orderSource);
        return ResponseEntity.ok(orderNo);
    }

    /**
     * 批量预生成订单号
     */
    @PostMapping("/pre-generate-order-numbers")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<List<String>> preGenerateOrderNumbers(
            @RequestParam Long accountId,
            @RequestParam String orderSource,
            @RequestParam int count) {
        List<String> orderNumbers = orderService.preGenerateOrderNumbers(accountId, orderSource, count);
        return ResponseEntity.ok(orderNumbers);
    }

    /**
     * 验证订单数据
     */
    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<Boolean> validateOrderData(@Valid @RequestBody OrderCreateRequest request) {
        boolean result = orderService.validateOrderData(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 处理多渠道订单冲突
     */
    @PostMapping("/resolve-multi-source-conflict")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OrderResponse> handleMultiSourceConflict(
            @RequestParam String salesOrderId,
            @RequestParam Long salesUserId,
            @RequestBody List<OrderCreateRequest> conflictingOrders) {
        OrderResponse response = orderService.handleMultiSourceConflict(salesOrderId, salesUserId, conflictingOrders);
        return ResponseEntity.ok(response);
    }
}
