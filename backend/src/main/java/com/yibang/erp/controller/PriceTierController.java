package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.PriceTierRequest;
import com.yibang.erp.domain.dto.PriceTierResponse;
import com.yibang.erp.domain.dto.PriceTierQueryRequest;
import com.yibang.erp.domain.service.PriceTierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 价格分层Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/price-tiers")
@RequiredArgsConstructor
public class PriceTierController {

    private final PriceTierService priceTierService;

    /**
     * 创建价格分层
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<PriceTierResponse> createPriceTier(@Valid @RequestBody PriceTierRequest request) {
        log.info("创建价格分层: {}", request);
        PriceTierResponse response = priceTierService.createPriceTier(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新价格分层
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<PriceTierResponse> updatePriceTier(
            @PathVariable Long id,
            @Valid @RequestBody PriceTierRequest request) {
        log.info("更新价格分层: id={}, request={}", id, request);
        PriceTierResponse response = priceTierService.updatePriceTier(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除价格分层
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> deletePriceTier(@PathVariable Long id) {
        log.info("删除价格分层: id={}", id);
        boolean success = priceTierService.deletePriceTier(id);
        return ResponseEntity.ok(success);
    }

    /**
     * 根据ID获取价格分层
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<PriceTierResponse> getPriceTierById(@PathVariable Long id) {
        log.info("获取价格分层: id={}", id);
        PriceTierResponse response = priceTierService.getPriceTierById(id);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询价格分层
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<PageResult<PriceTierResponse>> getPriceTierPage(PriceTierQueryRequest request) {
        log.info("分页查询价格分层: {}", request);
        PageResult<PriceTierResponse> result = priceTierService.getPriceTierPage(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询价格分层列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<PriceTierResponse>> getPriceTierList(PriceTierQueryRequest request) {
        log.info("查询价格分层列表: {}", request);
        List<PriceTierResponse> result = priceTierService.getPriceTierList(request);
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用价格分层
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> togglePriceTierStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        log.info("切换价格分层状态: id={}, isActive={}", id, isActive);
        boolean success = priceTierService.togglePriceTierStatus(id, isActive);
        return ResponseEntity.ok(success);
    }

    /**
     * 批量启用/禁用价格分层
     */
    @PutMapping("/batch-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> batchTogglePriceTierStatus(
            @RequestParam List<Long> ids,
            @RequestParam Boolean isActive) {
        log.info("批量切换价格分层状态: ids={}, isActive={}", ids, isActive);
        boolean success = priceTierService.batchTogglePriceTierStatus(ids, isActive);
        return ResponseEntity.ok(success);
    }

    /**
     * 获取适用的价格分层
     */
    @GetMapping("/applicable")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<List<PriceTierResponse>> getApplicablePriceTiers(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String customerType,
            @RequestParam Long companyId) {
        log.info("获取适用价格分层: categoryId={}, customerType={}, companyId={}", categoryId, customerType, companyId);
        List<PriceTierResponse> result = priceTierService.getApplicablePriceTiers(categoryId, customerType, companyId);
        return ResponseEntity.ok(result);
    }

    /**
     * 计算最终价格
     */
    @PostMapping("/calculate-price")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN') or hasRole('SALES_ADMIN')")
    public ResponseEntity<BigDecimal> calculateFinalPrice(
            @RequestParam Long priceTierId,
            @RequestParam BigDecimal originalPrice) {
        log.info("计算最终价格: priceTierId={}, originalPrice={}", priceTierId, originalPrice);
        BigDecimal finalPrice = priceTierService.calculateFinalPrice(priceTierId, originalPrice);
        return ResponseEntity.ok(finalPrice);
    }

    /**
     * 验证价格分层配置
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPPLIER_ADMIN')")
    public ResponseEntity<Boolean> validatePriceTierConfig(@Valid @RequestBody PriceTierRequest request) {
        log.info("验证价格分层配置: {}", request);
        boolean isValid = priceTierService.validatePriceTierConfig(request);
        return ResponseEntity.ok(isValid);
    }
}
