package com.yibang.erp.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface ProductService {

    /**
     * 分页查询商品列表
     */
    Page<Product> getProductList(Page<Product> page, String keyword, Long categoryId, Long brandId, String status);

    /**
     * 根据ID获取商品
     */
    Product getProductById(Long id);

    /**
     * 根据SKU获取商品
     */
    Product getProductBySku(String sku);

    /**
     * 搜索商品
     */
    List<Product> searchProducts(String keyword, Integer limit);
}
