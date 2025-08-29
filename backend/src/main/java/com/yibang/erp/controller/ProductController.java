package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.ProductAuditRequest;
import com.yibang.erp.domain.dto.ProductAuditResult;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;
import com.yibang.erp.domain.service.ProductAuditService;
import com.yibang.erp.domain.service.ProductStateService;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductStateService productStateService;
    private final ProductAuditService productAuditService;

    /**
     * 获取商品列表
     */
    @GetMapping
    public PageResult<Product> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long companyId) {
        
        log.info("获取商品列表，页码: {}, 每页大小: {}, 状态: {}, 公司ID: {}", page, size, status, companyId);
        
        // 这里实现分页查询逻辑
        // 暂时返回空结果，后续完善
        return new PageResult<>();
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        log.info("获取商品详情，商品ID: {}", id);
        return productRepository.selectById(id);
    }

    /**
     * 创建商品
     */
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        log.info("创建商品，商品名称: {}", product.getName());
        
        // 设置初始状态
        product.setStatus(ProductStatus.DRAFT);
        product.setApprovalStatus(ProductApprovalStatus.PENDING);
        
        productRepository.insert(product);
        return product;
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        log.info("更新商品，商品ID: {}", id);
        
        product.setId(id);
        productRepository.updateById(product);
        return product;
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public boolean deleteProduct(@PathVariable Long id) {
        log.info("删除商品，商品ID: {}", id);
        return productRepository.deleteById(id) > 0;
    }

    /**
     * 提交商品审核
     */
    @PostMapping("/{id}/submit")
    public boolean submitForApproval(@PathVariable Long id, @RequestParam Long submitterId) {
        log.info("提交商品审核，商品ID: {}, 提交人ID: {}", id, submitterId);
        return productStateService.submitForApproval(id, submitterId);
    }

    /**
     * 审核商品
     */
    @PostMapping("/{id}/audit")
    public ProductAuditResult auditProduct(@PathVariable Long id, @RequestBody ProductAuditRequest request) {
        log.info("审核商品，商品ID: {}, 审核状态: {}, 审核人ID: {}", 
                id, request.getApprovalStatus(), request.getApproverId());
        
        request.setProductId(id);
        return productAuditService.auditProduct(request);
    }

    /**
     * 上架商品
     */
    @PostMapping("/{id}/activate")
    public boolean activateProduct(@PathVariable Long id, @RequestParam Long operatorId) {
        log.info("上架商品，商品ID: {}, 操作人ID: {}", id, operatorId);
        return productStateService.activateProduct(id, operatorId);
    }

    /**
     * 下架商品
     */
    @PostMapping("/{id}/deactivate")
    public boolean deactivateProduct(@PathVariable Long id, 
                                   @RequestParam Long operatorId,
                                   @RequestParam(required = false) String reason) {
        log.info("下架商品，商品ID: {}, 操作人ID: {}, 原因: {}", id, operatorId, reason);
        return productStateService.deactivateProduct(id, operatorId, reason);
    }

    /**
     * 停售商品
     */
    @PostMapping("/{id}/discontinue")
    public boolean discontinueProduct(@PathVariable Long id, 
                                    @RequestParam Long operatorId,
                                    @RequestParam(required = false) String reason) {
        log.info("停售商品，商品ID: {}, 操作人ID: {}, 原因: {}", id, operatorId, reason);
        return productStateService.discontinueProduct(id, operatorId, reason);
    }

    /**
     * 重置商品为草稿状态
     */
    @PostMapping("/{id}/reset")
    public boolean resetToDraft(@PathVariable Long id, @RequestParam Long operatorId) {
        log.info("重置商品为草稿状态，商品ID: {}, 操作人ID: {}", id, operatorId);
        return productStateService.resetToDraft(id, operatorId);
    }

    /**
     * 获取待审核商品列表
     */
    @GetMapping("/pending")
    public List<Long> getPendingApprovalProducts(
            @RequestParam Long companyId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("获取待审核商品列表，公司ID: {}, 页码: {}, 每页大小: {}", companyId, page, size);
        return productAuditService.getPendingApprovalProducts(companyId, page, size);
    }

    /**
     * 获取待审核商品总数
     */
    @GetMapping("/pending/count")
    public long getPendingApprovalCount(@RequestParam Long companyId) {
        log.info("获取待审核商品总数，公司ID: {}", companyId);
        return productAuditService.getPendingApprovalCount(companyId);
    }

    /**
     * 批量审核商品
     */
    @PostMapping("/batch-audit")
    public List<ProductAuditResult> batchAuditProducts(@RequestBody List<ProductAuditRequest> requests) {
        log.info("批量审核商品，数量: {}", requests.size());
        return productAuditService.batchAuditProducts(requests);
    }

    /**
     * 获取商品状态
     */
    @GetMapping("/{id}/status")
    public ProductStatus getProductStatus(@PathVariable Long id) {
        log.info("获取商品状态，商品ID: {}", id);
        return productStateService.getCurrentStatus(id);
    }

    /**
     * 获取商品审核状态
     */
    @GetMapping("/{id}/approval-status")
    public ProductApprovalStatus getProductApprovalStatus(@PathVariable Long id) {
        log.info("获取商品审核状态，商品ID: {}", id);
        return productStateService.getApprovalStatus(id);
    }

    /**
     * 检查状态流转是否合法
     */
    @PostMapping("/{id}/validate-transition")
    public boolean validateStatusTransition(@PathVariable Long id, @RequestParam ProductStatus targetStatus) {
        log.info("检查状态流转是否合法，商品ID: {}, 目标状态: {}", id, targetStatus);
        
        ProductStatus currentStatus = productStateService.getCurrentStatus(id);
        return productStateService.isStatusTransitionValid(currentStatus, targetStatus);
    }
}
