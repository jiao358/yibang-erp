package com.yibang.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.dto.ProductAuditRequest;
import com.yibang.erp.domain.dto.ProductAuditResult;
import com.yibang.erp.domain.dto.ProductPriceTierConfigResponse;
import com.yibang.erp.domain.entity.*;
import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;
import com.yibang.erp.domain.service.ProductAuditService;
import com.yibang.erp.domain.service.ProductPriceTierConfigService;
import com.yibang.erp.domain.service.ProductStateService;
import com.yibang.erp.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductStateService productStateService;
    private final ProductAuditService productAuditService;
    private final CompanyRepository companyRepository;
    private final ProductPriceTierConfigService productPriceTierConfigService;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BrandRepository brandRepository;
    private PriceTierRepository priceTierRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductPriceTierConfigRepository productPriceTierConfigRepository;
    @Autowired
    private ProductImageRepository productImageRepository;

    /**
     * 获取商品列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String name,


            @RequestHeader("Authorization") String authHeader) {

        log.info("获取商品列表，页码: {}, 每页大小: {}, 状态: {}, 公司ID: {}", page, size, status, companyId);

        try {
            // 构建查询条件
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new IllegalArgumentException("无效的Authorization头");
            }

            String token = authHeader.substring(7);

            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                    stream().anyMatch(x -> x.getAuthority().equals("ROLE_SYSTEM_ADMIN"));


            // 添加状态过滤
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status);
            }

            // 添加公司过滤
            if (companyId != null && !isAdmin) {
                queryWrapper.eq("company_id", companyId);
            }

            // 只查询未删除的商品
            queryWrapper.eq("deleted", false);

            // 按创建时间倒序
            queryWrapper.orderByDesc("created_at");


            // 获取总记录数
            long total = productRepository.selectCount(queryWrapper);
            
            if (total == 0) {
                PageResult<Product> emptyResult = new PageResult<>();
                emptyResult.setRecords(List.of());
                emptyResult.setTotal(0);
                emptyResult.setCurrent(page);
                emptyResult.setSize(size);
                emptyResult.setPages(0);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", emptyResult);
                response.put("message", "获取商品列表成功");
                return ResponseEntity.ok(response);
            }
            
            // 添加分页条件（手动实现分页）
            int offset = (page - 1) * size;
            queryWrapper.last(String.format("LIMIT %d, %d", offset, size));
            
            // 查询商品数据
            List<Product> products = productRepository.selectList(queryWrapper);
            
            // 批量获取商品主图信息
            if (!products.isEmpty()) {
                List<Long> productIds = products.stream().map(Product::getId).toList();
                List<ProductImage> primaryImages = productImageRepository.selectPrimaryByProductIds(productIds);
                
                // 创建主图映射
                Map<Long, String> imageMap = primaryImages.stream()
                    .collect(Collectors.toMap(ProductImage::getProductId, ProductImage::getImageUrl));
                
                // 设置商品主图
                products.forEach(product -> {
                    String imageUrl = imageMap.get(product.getId());
                    if (imageUrl != null) {
                        product.setImages(imageUrl);
                    }
                });
            }
            
            // 构建返回结果
            PageResult<Product> pageResult = new PageResult<>();
            pageResult.setRecords(products);
            pageResult.setTotal(total);
            pageResult.setCurrent(page);
            pageResult.setSize(size);
            pageResult.setPages((total + size - 1) / size);

            log.info("商品列表查询成功，总数: {}, 当前页: {}, 返回数据: {}", total, page, products.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pageResult);
            response.put("message", "获取商品列表成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取商品列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取商品列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
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
    public Product createProduct(@RequestBody Product product, @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的Authorization头");
        }

        String token = authHeader.substring(7);
        Long belongCompayId = jwtUtil.getCompanyIdFromToken(token);

        Long userId = jwtUtil.getUserIdFromToken(token);

        log.info("创建商品，商品名称: {}, SKU: {}, 分类ID: {}, 公司ID: {}",
                product.getName(), product.getSku(), product.getCategoryId(), product.getCompanyId());

        // 验证必填字段
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("商品编码(SKU)不能为空");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (product.getCategoryId() == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
        if (product.getUnit() == null || product.getUnit().trim().isEmpty()) {
            throw new IllegalArgumentException("商品单位不能为空");
        }
        if (product.getCostPrice() == null) {
            throw new IllegalArgumentException("成本价不能为空");
        }
        if (product.getSellingPrice() == null) {
            throw new IllegalArgumentException("销售价不能为空");
        }
        if (product.getCompanyId() == null) {
            throw new IllegalArgumentException("所属公司不能为空");
        }

        product.setCreatedBy(userId);
        product.setUpdatedBy(userId);

        // 设置初始状态
        product.setStatus(ProductStatus.DRAFT);
        product.setApprovalStatus(ProductApprovalStatus.APPROVED);

        // 设置默认值
        if (product.getViewCount() == null) {
            product.setViewCount(0L);
        }
        if (product.getIsFeatured() == null) {
            product.setIsFeatured(false);
        }
        if (product.getIsHot() == null) {
            product.setIsHot(false);
        }
        if (product.getIsNew() == null) {
            product.setIsNew(false);
        }

        try {
            int result = productRepository.insert(product);
            if (result <= 0) {
                throw new RuntimeException("商品插入失败");
            }
            log.info("商品创建成功，ID: {}", product.getId());
            return product;
        } catch (Exception e) {
            log.error("创建商品失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        log.info("更新商品，商品ID: {}", id);

        product.setId(id);
        product.setApprovalStatus(ProductApprovalStatus.APPROVED);
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


    private Long getCurrentUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("无效的Authorization头");
        }

        String token = authHeader.substring(7);
        return jwtUtil.getUserIdFromToken(token);
    }

    /**
     * 获取销售用户商品列表（包含价格分层信息）
     */
    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES_USER')")
    public ResponseEntity<Map<String, Object>> getSalesProductList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long supplierCompanyId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestHeader("Authorization") String authHeader) {

        log.info("获取销售商品列表，页码: {}, 每页大小: {}, 商品名称: {}, 品牌: {}, 分类ID: {}, 供应商公司ID: {}",
                page, size, name, brand, categoryId, supplierCompanyId);

        try {
            // 构建查询条件
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();

            // 只查询已上架的商品
            queryWrapper.eq("status", "ACTIVE");
            queryWrapper.eq("approval_status", "APPROVED");
            queryWrapper.eq("deleted", false);

            // 添加名称过滤
            if (name != null && !name.trim().isEmpty()) {
                queryWrapper.like("name", name.trim());
            }

            // 添加品牌过滤
            if (brand != null && !brand.trim().isEmpty()) {
                queryWrapper.like("brand", brand.trim());
            }

            // 添加分类过滤
            if (categoryId != null) {
                queryWrapper.eq("category_id", categoryId);
            }

            // 添加供应商公司过滤
            if (supplierCompanyId != null) {
                queryWrapper.eq("company_id", supplierCompanyId);
            }

            // 添加价格范围过滤
            if (minPrice != null) {
                queryWrapper.ge("selling_price", minPrice);
            }
            if (maxPrice != null) {
                queryWrapper.le("selling_price", maxPrice);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc("created_at");

            // 执行分页查询
            Page<Product> pageParam = new Page<>(page, size);
            Page<Product> result = productRepository.selectPage(pageParam, queryWrapper);
            long countData= productRepository.selectCount(queryWrapper);
            if (CollectionUtils.isEmpty(result.getRecords())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", PageResult.of(null, result.getTotal(), page, size));
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }

            //先查询品牌内容
            Set<Long> bandIds = result.getRecords().stream().map(x -> {
                return x.getBrandId();
            }).collect(Collectors.toSet());

            List<Brand> brands = brandRepository.selectByIds(bandIds);

            Map<Long, String> brandsMap = new HashMap<>();
            brands.stream().forEach(x -> {
                Long brandId = x.getId();
                String brandName = x.getName();
                brandsMap.put(brandId, brandName);

            });

            //查看这个用户是否具备销售价格分层逻辑
            Long userId = getCurrentUserId(authHeader);

            User user = userRepository.selectById(userId);
            Long priceTierId = user.getPriceTierId();
            List<ProductPriceTierConfigResponse> productPriceTierConfigs = Lists.newArrayList();
            if (priceTierId != null) {
                //如果具备价格分层，则对查询出来的商品进行查询价格
                List<Long> productIds = result.getRecords().stream().map(x -> {
                    return x.getId();

                }).toList();


                productPriceTierConfigs = productPriceTierConfigService.getConfigsByProductIdsAndPriceTierId(productIds, priceTierId);
                if (productPriceTierConfigs == null) {
                    productPriceTierConfigs = List.of();
                }


            }
            final Map<Long, ProductPriceTierConfigResponse> productPriceTierConfigMap = productPriceTierConfigs.stream().collect(Collectors.toMap(ProductPriceTierConfigResponse::getProductId, x -> x));
            ;

            // 为每个商品添加供应商公司名称
            List<Map<String, Object>> productsWithCompany = result.getRecords().stream()
                    .map(product -> {
                        Map<String, Object> productMap = new HashMap<>();
                        productMap.put("id", product.getId());
                        productMap.put("sku", product.getSku());
                        productMap.put("name", product.getName());
                        productMap.put("brand", brandsMap.get(product.getBrandId().longValue()) != null ? brandsMap.get(product.getBrandId().longValue()) : "--");
                        productMap.put("specifications", product.getSpecifications());

                        if (MapUtils.isNotEmpty(productPriceTierConfigMap)) {
                            ProductPriceTierConfigResponse priceConfig = productPriceTierConfigMap.get(product.getId());
                            if (priceConfig != null) {
                                productMap.put("sellingPrice", priceConfig.getDropshippingPrice());
                                productMap.put("marketPrice", priceConfig.getRetailLimitPrice());
                            }

                        }


                        if(productMap.containsKey("sellingPrice") == false){
                            productMap.put("sellingPrice", product.getSellingPrice());
                        }
                        if(productMap.containsKey("marketPrice") == false){
                            productMap.put("marketPrice", product.getMarketPrice());
                        }


                        productMap.put("unit", product.getUnit());
                        productMap.put("status", product.getStatus());
                        productMap.put("companyId", product.getCompanyId());
                        productMap.put("approvalStatus", product.getApprovalStatus());

                        try {
                            // 查询供应商公司信息
                            Company company = companyRepository.selectById(product.getCompanyId());
                            if (company != null) {
                                productMap.put("supplierCompanyName", company.getName());
                            } else {
                                productMap.put("supplierCompanyName", "--");
                            }
                        } catch (Exception e) {
                            log.warn("查询商品供应商公司信息失败，商品ID: {}", product.getId(), e);
                            productMap.put("supplierCompanyName", "--");
                        }

                        return productMap;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", PageResult.of(productsWithCompany, countData, page, size));
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取销售商品列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取销售商品列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
