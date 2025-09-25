package com.yibang.erp.controller.hsf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.ProductPriceTierConfigResponse;
import com.yibang.erp.domain.dto.microservice.ProductInfoResponse;
import com.yibang.erp.domain.dto.microservice.Result;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.entity.ProductInventory;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.ProductPriceTierConfigService;
import com.yibang.erp.domain.service.ProductService;
import com.yibang.erp.infrastructure.repository.ProductInventoryRepository;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import com.yibang.erp.infrastructure.repository.UserRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HSF商品服务控制器
 * 提供给yibang-mall系统调用
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/hsf/product")
public class HsfProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductInventoryRepository productInventoryRepository;
    
    @Autowired
    private ProductPriceTierConfigService productPriceTierConfigService;
    
    @Autowired
    private UserRedisRepository userRedisRepository;

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<PageResult<ProductInfoResponse>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status) {
        try {
            log.info("HSF API调用: 获取商品列表 - page={}, size={}, keyword={}", page, size, keyword);
            
            // 构建查询条件
            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(keyword)) {
                queryWrapper.and(wrapper -> wrapper
                    .like(Product::getName, keyword)
                    .or()
                    .like(Product::getSku, keyword)
                    .or()
                    .like(Product::getDescription, keyword)
                );
            }
            
            if (categoryId != null) {
                queryWrapper.eq(Product::getCategoryId, categoryId);
            }
            
            if (brandId != null) {
                queryWrapper.eq(Product::getBrandId, brandId);
            }
            
            if (StringUtils.hasText(status)) {
                queryWrapper.eq(Product::getStatus, status);
            }
            
            queryWrapper.orderByDesc(Product::getCreatedAt);
            
            // 先查询总数
            long total = productRepository.selectCount(queryWrapper);
            
            if (total == 0) {
                PageResult<ProductInfoResponse> emptyResult = PageResult.empty(page, size);
                return Result.success("获取商品列表成功", emptyResult);
            }
            
            // 添加分页条件
            int offset = (page - 1) * size;
            queryWrapper.last(String.format("LIMIT %d, %d", offset, size));
            
            // 查询数据
            List<Product> products = productRepository.selectList(queryWrapper);
            
            // 获取用户价格分层信息
            User estela = userRedisRepository.findByUsername("estela");
            Long priceTierId = estela != null ? estela.getPriceTierId() : null;
            
            // 查询价格分层配置
            Map<Long, ProductPriceTierConfigResponse> priceConfigMap = Map.of();
            if (priceTierId != null && !products.isEmpty()) {
                List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
                List<ProductPriceTierConfigResponse> priceConfigs = productPriceTierConfigService
                        .getConfigsByProductIdsAndPriceTierId(productIds, priceTierId);
                priceConfigMap = priceConfigs.stream()
                        .collect(Collectors.toMap(ProductPriceTierConfigResponse::getProductId, config -> config));
            }
            
            // 转换为响应格式，应用价格分层
            final Map<Long, ProductPriceTierConfigResponse> finalPriceConfigMap = priceConfigMap;
            List<ProductInfoResponse> productList = products.stream()
                    .map(product -> convertToProductInfoResponse(product, finalPriceConfigMap))
                    .collect(Collectors.toList());
            
            PageResult<ProductInfoResponse> pageResult = PageResult.of(productList, total, page, size);
            
            return Result.success("获取商品列表成功", pageResult);
        } catch (Exception e) {
            log.error("HSF API调用失败: 获取商品列表", e);
            return Result.error("获取商品列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductInfoResponse> getProductById(@PathVariable Long id) {
        try {
            log.info("HSF API调用: 根据ID获取商品详情 - {}", id);
            
            Product product = productService.getProductById(id);
            if (product == null) {
                return Result.error("商品不存在");
            }

            // 获取用户价格分层信息
            User estela = userRedisRepository.findByUsername("estela");
            Long priceTierId = estela != null ? estela.getPriceTierId() : null;
            
            // 查询价格分层配置
            Map<Long, ProductPriceTierConfigResponse> priceConfigMap = Map.of();
            if (priceTierId != null) {
                List<ProductPriceTierConfigResponse> priceConfigs = productPriceTierConfigService
                        .getConfigsByProductIdsAndPriceTierId(List.of(id), priceTierId);
                priceConfigMap = priceConfigs.stream()
                        .collect(Collectors.toMap(ProductPriceTierConfigResponse::getProductId, config -> config));
            }

            ProductInfoResponse productInfo = convertToProductInfoResponse(product, priceConfigMap);
            return Result.success("获取商品详情成功", productInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据ID获取商品详情", e);
            return Result.error("获取商品详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据SKU获取商品信息
     */
    @GetMapping("/sku/{sku}")
    public Result<ProductInfoResponse> getProductBySku( @PathVariable String sku, @RequestParam Long companyId) {
        try {
            log.info("HSF API调用: 根据SKU获取商品信息 - {}", sku);

            LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Product::getSku, sku);
            queryWrapper.eq(Product::getCompanyId,companyId);
            Product product = productRepository.selectOne(queryWrapper);

//            Product product = productService.getProductBySku(sku);
            if (product == null) {
                return Result.error("商品不存在");
            }

            // 获取用户价格分层信息
            User estela = userRedisRepository.findByUsername("estela");
            Long priceTierId = estela != null ? estela.getPriceTierId() : null;
            
            // 查询价格分层配置
            Map<Long, ProductPriceTierConfigResponse> priceConfigMap = Map.of();
            if (priceTierId != null) {
                List<ProductPriceTierConfigResponse> priceConfigs = productPriceTierConfigService
                        .getConfigsByProductIdsAndPriceTierId(List.of(product.getId()), priceTierId);
                priceConfigMap = priceConfigs.stream()
                        .collect(Collectors.toMap(ProductPriceTierConfigResponse::getProductId, config -> config));
            }

            ProductInfoResponse productInfo = convertToProductInfoResponse(product, priceConfigMap);
            return Result.success("获取商品信息成功", productInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据SKU获取商品信息", e);
            return Result.error("获取商品信息失败: " + e.getMessage());
        }
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public Result<List<ProductInfoResponse>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("HSF API调用: 搜索商品 - keyword={}, limit={}", keyword, limit);
            
            List<Product> products = productService.searchProducts(keyword, limit);
            
            // 获取用户价格分层信息
            User estela = userRedisRepository.findByUsername("estela");
            Long priceTierId = estela != null ? estela.getPriceTierId() : null;
            
            // 查询价格分层配置
            Map<Long, ProductPriceTierConfigResponse> priceConfigMap = Map.of();
            if (priceTierId != null && !products.isEmpty()) {
                List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
                List<ProductPriceTierConfigResponse> priceConfigs = productPriceTierConfigService
                        .getConfigsByProductIdsAndPriceTierId(productIds, priceTierId);
                priceConfigMap = priceConfigs.stream()
                        .collect(Collectors.toMap(ProductPriceTierConfigResponse::getProductId, config -> config));
            }
            
            final Map<Long, ProductPriceTierConfigResponse> finalPriceConfigMap = priceConfigMap;
            List<ProductInfoResponse> productList = products.stream()
                    .map(product -> convertToProductInfoResponse(product, finalPriceConfigMap))
                    .collect(Collectors.toList());
            
            return Result.success("搜索商品成功", productList);
        } catch (Exception e) {
            log.error("HSF API调用失败: 搜索商品", e);
            return Result.error("搜索商品失败: " + e.getMessage());
        }
    }

    /**
     * 转换Product为ProductInfoResponse
     */
    private ProductInfoResponse convertToProductInfoResponse(Product product) {
        return convertToProductInfoResponse(product, Map.of());
    }
    
    /**
     * 转换Product为ProductInfoResponse（带价格分层）
     */
    private ProductInfoResponse convertToProductInfoResponse(Product product, Map<Long, ProductPriceTierConfigResponse> priceConfigMap) {
        ProductInfoResponse response = new ProductInfoResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setCategoryId(product.getCategoryId());
        response.setBrandId(product.getBrandId());
        response.setDescription(product.getDescription());
        response.setShortDescription(product.getShortDescription());
        response.setSpecifications(product.getSpecifications());
        response.setUnit(product.getUnit());
        response.setCostPrice(product.getCostPrice());
        response.setWeight(product.getWeight());
        response.setDimensions(product.getDimensions());
        response.setColor(product.getColor());
        response.setMaterial(product.getMaterial());
        response.setOriginCountry(product.getOriginCountry());
        response.setStatus(product.getStatus().toString());
        response.setApprovalStatus(product.getApprovalStatus().toString());
        response.setIsHot(product.getIsHot());
        response.setIsNew(product.getIsNew());
        response.setIsFeatured(product.getIsFeatured());
        response.setCompanyId(product.getCompanyId());

        
        // 查询商品库存数量
        Integer stockQuantity = getProductStockQuantity(product.getId());
        response.setStockQuantity(stockQuantity);
        
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        
        // 应用价格分层
        ProductPriceTierConfigResponse priceConfig = priceConfigMap.get(product.getId());
        if (priceConfig != null) {
            // 使用价格分层配置的价格
            response.setSellingPrice(priceConfig.getDropshippingPrice());
            response.setRetailLimitPrice(priceConfig.getRetailLimitPrice());
            response.setMarketPrice(priceConfig.getRetailLimitPrice()); // 市场价使用零售限价
        } else {
            // 使用商品默认价格
            response.setSellingPrice(product.getSellingPrice());
            response.setMarketPrice(product.getMarketPrice());
            response.setRetailLimitPrice(product.getRetailLimitPrice());
        }
        
        return response;
    }
    
    /**
     * 获取商品库存数量
     */
    private Integer getProductStockQuantity(Long productId) {
        try {
            // 查询商品在所有仓库的库存
            LambdaQueryWrapper<ProductInventory> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProductInventory::getProductId, productId);
            
            List<ProductInventory> inventories = productInventoryRepository.selectList(queryWrapper);
            
            // 计算总可用库存
            return inventories.stream()
                    .mapToInt(inventory -> inventory.getAvailableQuantity() != null ? inventory.getAvailableQuantity() : 0)
                    .sum();
        } catch (Exception e) {
            log.warn("查询商品库存失败，商品ID: {}, 错误: {}", productId, e.getMessage());
            return 0;
        }
    }
}
