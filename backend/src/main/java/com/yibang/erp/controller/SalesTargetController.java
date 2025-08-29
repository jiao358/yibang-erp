package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.SalesTargetRequest;
import com.yibang.erp.domain.dto.SalesTargetResponse;
import com.yibang.erp.domain.dto.SalesTargetQueryRequest;
import com.yibang.erp.domain.service.SalesTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 销售目标Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/sales-targets")
@RequiredArgsConstructor
public class SalesTargetController {

    private final SalesTargetService salesTargetService;

    /**
     * 创建销售目标
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<SalesTargetResponse> createSalesTarget(@Valid @RequestBody SalesTargetRequest request) {
        log.info("创建销售目标: {}", request);
        SalesTargetResponse response = salesTargetService.createSalesTarget(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新销售目标
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<SalesTargetResponse> updateSalesTarget(
            @PathVariable Long id,
            @Valid @RequestBody SalesTargetRequest request) {
        log.info("更新销售目标: id={}, request={}", id, request);
        SalesTargetResponse response = salesTargetService.updateSalesTarget(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除销售目标
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> deleteSalesTarget(@PathVariable Long id) {
        log.info("删除销售目标: id={}", id);
        boolean success = salesTargetService.deleteSalesTarget(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 根据ID获取销售目标
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<SalesTargetResponse> getSalesTargetById(@PathVariable Long id) {
        log.info("获取销售目标: id={}", id);
        SalesTargetResponse response = salesTargetService.getSalesTargetById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询销售目标
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<PageResult<SalesTargetResponse>> getSalesTargetPage(SalesTargetQueryRequest request) {
        log.info("分页查询销售目标: {}", request);
        PageResult<SalesTargetResponse> result = salesTargetService.getSalesTargetPage(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询销售目标列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<SalesTargetResponse>> getSalesTargetList(SalesTargetQueryRequest request) {
        log.info("查询销售目标列表: {}", request);
        List<SalesTargetResponse> result = salesTargetService.getSalesTargetList(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用销售目标
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> toggleSalesTargetStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        log.info("切换销售目标状态: id={}, isActive={}", id, isActive);
        boolean success = salesTargetService.toggleSalesTargetStatus(id, isActive);
        return ResponseEntity.ok(success);
    }

    /**
     * 批量启用/禁用销售目标
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> batchToggleSalesTargetStatus(
            @RequestParam List<Long> ids,
            @RequestParam Boolean isActive) {
        log.info("批量切换销售目标状态: ids={}, isActive={}", ids, isActive);
        boolean success = salesTargetService.batchToggleSalesTargetStatus(ids, isActive);
        return ResponseEntity.ok(success);
    }

    /**
     * 更新目标进度
     */
    @PutMapping("/{id}/progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> updateTargetProgress(
            @PathVariable Long id,
            @RequestParam BigDecimal currentValue) {
        log.info("更新目标进度: id={}, currentValue={}", id, currentValue);
        boolean success = salesTargetService.updateTargetProgress(id, currentValue);
        return ResponseEntity.ok(success);
    }

    /**
     * 批量更新目标进度
     */
    @PutMapping("/batch-progress")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> batchUpdateTargetProgress(
            @RequestParam List<Long> ids,
            @RequestParam List<BigDecimal> currentValues) {
        log.info("批量更新目标进度: ids={}, currentValues={}", ids, currentValues);
        boolean success = salesTargetService.batchUpdateTargetProgress(ids, currentValues);
        return ResponseEntity.ok(success);
    }

    /**
     * 激活销售目标
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> activateSalesTarget(@PathVariable Long id) {
        log.info("激活销售目标: id={}", id);
        boolean success = salesTargetService.activateSalesTarget(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 完成销售目标
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> completeSalesTarget(@PathVariable Long id) {
        log.info("完成销售目标: id={}", id);
        boolean success = salesTargetService.completeSalesTarget(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 取消销售目标
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> cancelSalesTarget(@PathVariable Long id) {
        log.info("取消销售目标: id={}", id);
        boolean success = salesTargetService.cancelSalesTarget(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 重置销售目标
     */
    @PutMapping("/{id}/reset")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> resetSalesTarget(@PathVariable Long id) {
        log.info("重置销售目标: id={}", id);
        boolean success = salesTargetService.resetSalesTarget(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 获取适用的销售目标
     */
    @GetMapping("/applicable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<SalesTargetResponse>> getApplicableSalesTargets(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String customerType,
            @RequestParam Long companyId) {
        log.info("获取适用销售目标: categoryId={}, customerType={}, companyId={}", categoryId, customerType, companyId);
        List<SalesTargetResponse> result = salesTargetService.getApplicableSalesTargets(categoryId, customerType, companyId);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取目标完成统计信息
     */
    @GetMapping("/completion-statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Object> getTargetCompletionStatistics(@RequestParam Long companyId) {
        log.info("获取目标完成统计信息: companyId={}", companyId);
        Object statistics = salesTargetService.getTargetCompletionStatistics(companyId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取目标类型统计信息
     */
    @GetMapping("/type-statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Object> getTargetTypeStatistics(@RequestParam Long companyId) {
        log.info("获取目标类型统计信息: companyId={}", companyId);
        Object statistics = salesTargetService.getTargetTypeStatistics(companyId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 验证销售目标配置
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Boolean> validateSalesTargetConfig(@Valid @RequestBody SalesTargetRequest request) {
        log.info("验证销售目标配置: {}", request);
        boolean isValid = salesTargetService.validateSalesTargetConfig(request);
        return ResponseEntity.ok(isValid);
    }
}
