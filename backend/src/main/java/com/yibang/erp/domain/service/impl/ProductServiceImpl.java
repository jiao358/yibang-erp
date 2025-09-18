package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.service.ProductService;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 商品服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Page<Product> getProductList(Page<Product> page, String keyword, Long categoryId, Long brandId, String status) {
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
        
        return productRepository.selectPage(page, queryWrapper);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.selectById(id);
    }

    @Override
    public Product getProductBySku(String sku) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getSku, sku);
        return productRepository.selectOne(queryWrapper);
    }

    @Override
    public List<Product> searchProducts(String keyword, Integer limit) {
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
        
        queryWrapper.orderByDesc(Product::getCreatedAt);
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        return productRepository.selectList(queryWrapper);
    }
}
