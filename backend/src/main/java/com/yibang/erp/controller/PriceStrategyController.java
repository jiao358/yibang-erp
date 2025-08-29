package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.PriceStrategyRequest;
import com.yibang.erp.domain.dto.PriceStrategyResponse;
import com.yibang.erp.domain.dto.PriceStrategyQueryRequest;
import com.yibang.erp.domain.service.PriceStrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 价格策略Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/price-strategies")
@RequiredArgsConstructor
public class PriceStrategyController {

    private final PriceStrategyService priceStrategyService;

    /**
     * 创建价格策略
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<PriceStrategyResponse> createPriceStrategy(@Valid @RequestBody PriceStrategyRequest request) {
        log.info("创建价格策略: {}", request);
        PriceStrategyResponse response = priceStrategyService.createPriceStrategy(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新价格策略
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<PriceStrategyResponse> updatePriceStrategy(
            @PathVariable Long id,
            @Valid @RequestBody PriceStrategyRequest request) {
        log.info("更新价格策略: id={}, request={}", id, request);
        PriceStrategyResponse response = priceStrategyService.updatePriceStrategy(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除价格策略
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> deletePriceStrategy(@PathVariable Long id) {
        log.info("删除价格策略: id={}", id);
        boolean success = priceStrategyService.deletePriceStrategy(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 根据ID获取价格策略
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<PriceStrategyResponse> getPriceStrategyById(@PathVariable Long id) {
        log.info("获取价格策略: id={}", id);
        PriceStrategyResponse response = priceStrategyService.getPriceStrategyById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询价格策略
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<PageResult<PriceStrategyResponse>> getPriceStrategyPage(PriceStrategyQueryRequest request) {
        log.info("分页查询价格策略: {}", request);
        PageResult<PriceStrategyResponse> result = priceStrategyService.getPriceStrategyPage(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询价格策略列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<PriceStrategyResponse>> getPriceStrategyList(PriceStrategyQueryRequest request) {
        log.info("查询价格策略列表: {}", request);
        List<PriceStrategyResponse> result = priceStrategyService.getPriceStrategyList(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用价格策略
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> togglePriceStrategyStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        log.info("切换价格策略状态: id={}, isActive={}", id, isActive);
        boolean success = priceStrategyService.togglePriceStrategyStatus(id, isActive);
        return ResponseEntity.ok(success);
    }

    /**
     * 批量启用/禁用价格策略
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> batchTogglePriceStrategyStatus(
            @RequestParam List<Long> ids,
            @RequestParam Boolean isActive) {
        log.info("批量切换价格策略状态: ids={}, isActive={}", ids, isActive);
        boolean success = priceStrategyService.batchTogglePriceStrategyStatus(ids, isActive);
        return ResponseEntity.ok(success);
    }

    /**
     * 获取适用的价格策略
     */
    @GetMapping("/applicable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<PriceStrategyResponse>> getApplicablePriceStrategies(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String customerType,
            @RequestParam Long companyId) {
        log.info("获取适用价格策略: categoryId={}, customerType={}, companyId={}", categoryId, customerType, companyId);
        List<PriceStrategyResponse> result = priceStrategyService.getApplicablePriceStrategies(categoryId, customerType, companyId);
        return ResponseEntity.ok(result);
    }

    /**
     * 应用价格策略计算最终价格
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<BigDecimal> applyPriceStrategy(
            @RequestParam Long strategyId,
            @RequestParam BigDecimal originalPrice) {
        log.info("应用价格策略: strategyId={}, originalPrice={}", strategyId, originalPrice);
        BigDecimal finalPrice = priceStrategyService.applyPriceStrategy(strategyId, originalPrice);
        return ResponseEntity.ok(finalPrice);
    }

    /**
     * 批量应用价格策略
     */
    @PostMapping("/batch-apply")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<BigDecimal>> batchApplyPriceStrategies(
            @RequestParam List<Long> strategyIds,
            @RequestParam List<BigDecimal> originalPrices) {
        log.info("批量应用价格策略: strategyIds={}, originalPrices={}", strategyIds, originalPrices);
        List<BigDecimal> finalPrices = priceStrategyService.batchApplyPriceStrategies(strategyIds, originalPrices);
        return ResponseEntity.ok(finalPrices);
    }

    /**
     * 验证价格策略配置
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> validatePriceStrategyConfig(@Valid @RequestBody PriceStrategyRequest request) {
        log.info("验证价格策略配置: {}", request);
        boolean isValid = priceStrategyService.validatePriceStrategyConfig(request);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 获取策略类型统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<Object> getStrategyTypeStatistics(@RequestParam Long companyId) {
        log.info("获取策略类型统计信息: companyId={}", companyId);
        Object statistics = priceStrategyService.getStrategyTypeStatistics(companyId);
        return ResponseEntity.ok(statistics);
    }
}
