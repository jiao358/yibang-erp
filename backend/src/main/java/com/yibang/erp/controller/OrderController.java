package com.yibang.erp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.dto.*;
import com.yibang.erp.domain.entity.OrderStatusLog;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.OrderService;
import com.yibang.erp.infrastructure.repository.UserRedisRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 订单管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/orders")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private  JwtUtil jwtUtil;
    @Autowired
    private UserRedisRepository userRedisRepository;
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
    public ResponseEntity<IPage<OrderResponse>> getOrderList(OrderListRequest request,@RequestHeader("Authorization") String authorization) {
        //这里面就要做后端逻辑了，销售只能看到自己的订单
        //供应商只能看到推送到自己的订单
        //1. 获取当前用户信息
        Long userId = getUserId(authorization);
        User user = userRedisRepository.selectById(userId);

        if(user.getCompanyId()==7 || user.getCompanyId()==3){
            request.setYibang(true);
        }

        boolean isAdmin=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        if(isAdmin){
            IPage<OrderResponse> response = orderService.getOrderList(request);
            return ResponseEntity.ok(response);
        }
        //如果是销售 只能看到自己的订单

        boolean isSales=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SALES"));

        if(isSales){
            request.setSalesUserId(userId);
            if(request.isYibang()){
                request.setSalesCompanyId(user.getCompanyId());
            }
            IPage<OrderResponse> response  = orderService.getOrderSalesList(request);
            return ResponseEntity.ok(response);
        }

        boolean isSupplier=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPPLIER_ADMIN")) || SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPPLIER_OPERATOR"));

        if(isSupplier){
            Long companyId= getCompanyId(authorization);
            request.setSupplierCompanyId(companyId);
            IPage<OrderResponse> response  = orderService.getOrderSuplierList(request);
            return ResponseEntity.ok(response);

        }







        return null;
    }


    private Long getCompanyId(String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long companyId= jwtUtil.getCompanyIdFromToken(token);

            return companyId;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从JWT中提取公司ID
     */
    private Long getUserId(String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long userId= jwtUtil.getUserIdFromToken(token);

            return userId;
        } catch (Exception e) {
            return null;
        }
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
    public ResponseEntity<OrderResponse> supplierShipOrder(
            @PathVariable Long orderId,
            @RequestBody SupplierShipRequest request) {
        OrderResponse response = orderService.supplierShipOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 供应商拒绝订单
     */
    @PostMapping("/{orderId}/supplier-reject")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<OrderResponse> supplierRejectOrder(
            @PathVariable Long orderId,
            @RequestBody SupplierRejectRequest request) {
        OrderResponse response = orderService.supplierRejectOrder(orderId, request);
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
     * 批量供应商确认订单
     */
    @PostMapping("/batch-supplier-confirm")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<OrderBatchConfirmResponse> batchSupplierConfirmOrders(
            @Valid @RequestBody OrderBatchConfirmRequest request) {
        OrderBatchConfirmResponse response = orderService.batchSupplierConfirmOrders(request);
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
     * 导出订单
     */
    @PostMapping("/export")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER')")
    public ResponseEntity<byte[]> exportOrders(@RequestBody OrderExportRequest request) {
        try {
            log.info("导出订单请求，订单ID列表: {}", request.getOrderIds());
            
            byte[] excelData = orderService.exportOrders(request.getOrderIds());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            // 使用英文文件名避免编码问题
            String filename = "orders_export.xlsx";
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
                
        } catch (Exception e) {
            log.error("导出订单失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 下载发货模板
     */
    @GetMapping("/ship-template")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER')")
    public ResponseEntity<byte[]> downloadShipTemplate() {
        try {
            log.info("下载发货模板请求");
            
            byte[] templateData = orderService.downloadShipTemplate();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            // 使用英文文件名避免编码问题
            String filename = "shipping_template.xlsx";
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(templateData);
                
        } catch (Exception e) {
            log.error("下载发货模板失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 预览发货导入数据
     */
    @PostMapping("/ship-import/preview")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER')")
    public ResponseEntity<ShipImportPreviewResponse> previewShipImport(@RequestParam("file") MultipartFile file) {
        try {
            log.info("预览发货导入数据请求，文件名: {}", file.getOriginalFilename());
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            byte[] fileData = file.getBytes();
            ShipImportPreviewResponse response = orderService.previewShipImport(fileData);
            
            return ResponseEntity.ok(response);
                
        } catch (Exception e) {
            log.error("预览发货导入数据失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 导入发货数据
     */
    @PostMapping("/ship-import")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER')")
    public ResponseEntity<ShipImportResultResponse> importShipData(@RequestParam("file") MultipartFile file) {
        try {
            log.info("导入发货数据请求，文件名: {}", file.getOriginalFilename());
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            byte[] fileData = file.getBytes();
            ShipImportResultResponse response = orderService.importShipData(fileData);
            
            return ResponseEntity.ok(response);
                
        } catch (Exception e) {
            log.error("导入发货数据失败", e);
            return ResponseEntity.badRequest().build();
        }
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
            @RequestParam String userName,
            @RequestParam String orderSource) {
        String orderNo = orderService.generatePlatformOrderNo(userName, orderSource);
        return ResponseEntity.ok(orderNo);
    }

    /**
     * 批量预生成订单号
     */
    @PostMapping("/pre-generate-order-numbers")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<List<String>> preGenerateOrderNumbers(
            @RequestParam String userName,
            @RequestParam String orderSource,
            @RequestParam int count) {
        List<String> orderNumbers = orderService.preGenerateOrderNumbers(userName, orderSource, count);
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
