package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.ProductPriceTierConfigRequest;
import com.yibang.erp.domain.dto.ProductPriceTierConfigResponse;
import com.yibang.erp.domain.service.ProductPriceTierConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 商品价格分层配置控制器
 */
@RestController
@RequestMapping("/api/product-price-configs")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
public class ProductPriceTierConfigController {

    @Autowired
    private ProductPriceTierConfigService configService;

    /**
     * 创建价格分层配置
     */
    @PostMapping
    public ResponseEntity<ProductPriceTierConfigResponse> createConfig(
            @Valid @RequestBody ProductPriceTierConfigRequest request) {
        ProductPriceTierConfigResponse response = configService.createConfig(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新价格分层配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductPriceTierConfigResponse> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody ProductPriceTierConfigRequest request) {
        ProductPriceTierConfigResponse response = configService.updateConfig(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除价格分层配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteConfig(@PathVariable Long id) {
        boolean result = configService.deleteConfig(id);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取价格分层配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductPriceTierConfigResponse> getConfigById(@PathVariable Long id) {
        ProductPriceTierConfigResponse response = configService.getConfigById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据商品ID获取所有价格分层配置
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductPriceTierConfigResponse>> getConfigsByProductId(
            @PathVariable Long productId) {
        List<ProductPriceTierConfigResponse> configs = configService.getConfigsByProductId(productId);
        return ResponseEntity.ok(configs);
    }

    /**
     * 批量保存价格分层配置
     */
    @PostMapping("/product/{productId}/batch")
    public ResponseEntity<Boolean> batchSaveConfigs(
            @PathVariable Long productId,
            @RequestBody List<ProductPriceTierConfigRequest> configs) {
        boolean result = configService.batchSaveConfigs(productId, configs);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据价格分层ID获取配置
     */
    @GetMapping("/price-tier/{priceTierId}")
    public ResponseEntity<List<ProductPriceTierConfigResponse>> getConfigsByPriceTierId(
            @PathVariable Long priceTierId) {
        List<ProductPriceTierConfigResponse> configs = configService.getConfigsByPriceTierId(priceTierId);
        return ResponseEntity.ok(configs);
    }
}
