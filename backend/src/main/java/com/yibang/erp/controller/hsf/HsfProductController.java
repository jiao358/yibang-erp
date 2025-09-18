package com.yibang.erp.controller.hsf;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.dto.microservice.ProductInfoResponse;
import com.yibang.erp.domain.dto.microservice.Result;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /**
     * 获取商品列表
     */
    @GetMapping("/list")
    public Result<Page<ProductInfoResponse>> getProductList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String status) {
        try {
            log.info("HSF API调用: 获取商品列表 - page={}, size={}, keyword={}", page, size, keyword);
            
            // 构建查询条件
            Page<Product> productPage = new Page<>(page, size);
            Page<Product> result = productService.getProductList(productPage, keyword, categoryId, brandId, status);
            
            // 转换为响应格式
            Page<ProductInfoResponse> responsePage = new Page<>();
            responsePage.setCurrent(result.getCurrent());
            responsePage.setSize(result.getSize());
            responsePage.setTotal(result.getTotal());
            responsePage.setPages(result.getPages());
            
            List<ProductInfoResponse> productList = result.getRecords().stream()
                    .map(this::convertToProductInfoResponse)
                    .collect(Collectors.toList());
            responsePage.setRecords(productList);
            
            return Result.success("获取商品列表成功", responsePage);
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

            ProductInfoResponse productInfo = convertToProductInfoResponse(product);
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
    public Result<ProductInfoResponse> getProductBySku(@PathVariable String sku) {
        try {
            log.info("HSF API调用: 根据SKU获取商品信息 - {}", sku);
            
            Product product = productService.getProductBySku(sku);
            if (product == null) {
                return Result.error("商品不存在");
            }

            ProductInfoResponse productInfo = convertToProductInfoResponse(product);
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
            List<ProductInfoResponse> productList = products.stream()
                    .map(this::convertToProductInfoResponse)
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
        ProductInfoResponse response = new ProductInfoResponse();
        response.setId(product.getId());
        response.setSku(product.getSku());
        response.setName(product.getName());
        response.setCategoryId(product.getCategoryId());
        response.setBrandId(product.getBrandId());
        response.setDescription(product.getDescription());
        response.setSellingPrice(product.getSellingPrice());
        response.setStatus(product.getStatus().toString());
        response.setApprovalStatus(product.getApprovalStatus().toString());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }
}
