package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.OrderBatchProcessRequest;
import com.yibang.erp.domain.dto.OrderBatchProcessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 订单批量处理控制器
 */
@RestController
@RequestMapping("/api/orders/batch")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
public class OrderBatchController {

    @Autowired
    private com.yibang.erp.domain.service.OrderBatchService orderBatchService;

    /**
     * 批量导入Excel订单
     */
    @PostMapping("/import-excel")
    public ResponseEntity<OrderBatchProcessResponse> importExcelOrders(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "templateId", required = false) Long templateId) {
        OrderBatchProcessResponse response = orderBatchService.importExcelOrders(file, templateId);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量更新订单状态
     */
    @PostMapping("/update-status")
    public ResponseEntity<OrderBatchProcessResponse> batchUpdateOrderStatus(
            @RequestBody OrderBatchProcessRequest request) {
        OrderBatchProcessResponse response = orderBatchService.batchUpdateOrderStatus(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量删除订单
     */
    @PostMapping("/delete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OrderBatchProcessResponse> batchDeleteOrders(
            @RequestBody OrderBatchProcessRequest request) {
        OrderBatchProcessResponse response = orderBatchService.batchDeleteOrders(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量分配供应商
     */
    @PostMapping("/assign-supplier")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<OrderBatchProcessResponse> batchAssignSupplier(
            @RequestBody OrderBatchProcessRequest request) {
        OrderBatchProcessResponse response = orderBatchService.batchAssignSupplier(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取批量处理进度
     */
    @GetMapping("/progress/{batchId}")
    public ResponseEntity<OrderBatchProcessResponse> getBatchProgress(@PathVariable String batchId) {
        OrderBatchProcessResponse response = orderBatchService.getBatchProgress(batchId);
        return ResponseEntity.ok(response);
    }

    /**
     * 取消批量处理
     */
    @PostMapping("/cancel/{batchId}")
    public ResponseEntity<Boolean> cancelBatchProcess(@PathVariable String batchId) {
        boolean result = orderBatchService.cancelBatchProcess(batchId);
        return ResponseEntity.ok(result);
    }
}
