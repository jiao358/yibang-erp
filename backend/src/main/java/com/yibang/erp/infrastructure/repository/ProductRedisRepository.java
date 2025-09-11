package com.yibang.erp.infrastructure.repository;

import com.yibang.erp.domain.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

/**
 * 商品Redis缓存Repository
 * 基于Redis对ProductRepository进行缓存封装，防止数据库击穿
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Repository
public class ProductRedisRepository {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private ProductRepository productRepository;

    // Redis键前缀
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCT_BY_SKU_KEY_PREFIX = "product:sku:";
    private static final String PRODUCT_BY_NAME_KEY_PREFIX = "product:name:";
    private static final String PRODUCT_BY_CATEGORY_KEY_PREFIX = "product:category:";
    private static final String PRODUCT_BY_BRAND_KEY_PREFIX = "product:brand:";
    private static final String PRODUCT_BY_COMPANY_KEY_PREFIX = "product:company:";
    private static final String PRODUCT_BY_STATUS_KEY_PREFIX = "product:status:";
    private static final String PRODUCT_BY_APPROVAL_STATUS_KEY_PREFIX = "product:approval:";
    
    // 缓存过期时间：10分钟（商品信息相对稳定）
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(10);

    /**
     * 根据ID查询商品（带缓存）
     */
    public Product selectById(Long id) {
        if (id == null) {
            return null;
        }
        
        String cacheKey = PRODUCT_KEY_PREFIX + id;
        
        try {
            // 先从缓存获取
            Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProduct != null) {
                log.debug("从缓存获取商品: id={}", id);
                return cachedProduct;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: id={}", id);
            Product product = productRepository.selectById(id);
            
            if (product != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME);
                log.debug("商品数据已缓存: id={}", id);
            }
            
            return product;
            
        } catch (Exception e) {
            log.error("查询商品失败: id={}", id, e);
            // 缓存异常时直接查询数据库
            return productRepository.selectById(id);
        }
    }

    /**
     * 根据SKU查询商品（带缓存）
     */
    public Product findBySku(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = PRODUCT_BY_SKU_KEY_PREFIX + sku;
        
        try {
            // 先从缓存获取
            Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProduct != null) {
                log.debug("从缓存获取商品: sku={}", sku);
                return cachedProduct;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: sku={}", sku);
            Product product = productRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getSku, sku)
                    .eq(Product::getDeleted, false)
            );
            
            if (product != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME);
                // 同时缓存商品ID映射
                redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), product, CACHE_EXPIRE_TIME);
                log.debug("商品数据已缓存: sku={}, id={}", sku, product.getId());
            }
            
            return product;
            
        } catch (Exception e) {
            log.error("查询商品失败: sku={}", sku, e);
            // 缓存异常时直接查询数据库
            return productRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getSku, sku)
                    .eq(Product::getDeleted, false)
            );
        }
    }

    /**
     * 根据商品名称查询商品（带缓存）
     */
    public Product findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = PRODUCT_BY_NAME_KEY_PREFIX + name;
        
        try {
            // 先从缓存获取
            Product cachedProduct = (Product) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProduct != null) {
                log.debug("从缓存获取商品: name={}", name);
                return cachedProduct;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: name={}", name);
            Product product = productRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getName, name)
                    .eq(Product::getDeleted, false)
            );
            
            if (product != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME);
                // 同时缓存商品ID映射
                redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), product, CACHE_EXPIRE_TIME);
                log.debug("商品数据已缓存: name={}, id={}", name, product.getId());
            }
            
            return product;
            
        } catch (Exception e) {
            log.error("查询商品失败: name={}", name, e);
            // 缓存异常时直接查询数据库
            return productRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getName, name)
                    .eq(Product::getDeleted, false)
            );
        }
    }

    /**
     * 根据分类ID查询商品列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Product> findByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        
        String cacheKey = PRODUCT_BY_CATEGORY_KEY_PREFIX + categoryId;
        
        try {
            // 先从缓存获取
            List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProducts != null) {
                log.debug("从缓存获取商品列表: categoryId={}, count={}", categoryId, cachedProducts.size());
                return cachedProducts;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: categoryId={}", categoryId);
            List<Product> products = productRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, categoryId)
                    .eq(Product::getDeleted, false)
                    .orderByDesc(Product::getCreatedAt)
            );
            
            if (products != null && !products.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, products, CACHE_EXPIRE_TIME);
                // 同时缓存每个商品的详细信息
                for (Product product : products) {
                    redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), product, CACHE_EXPIRE_TIME);
                }
                log.debug("商品列表数据已缓存: categoryId={}, count={}", categoryId, products.size());
            }
            
            return products != null ? products : List.of();
            
        } catch (Exception e) {
            log.error("查询商品列表失败: categoryId={}", categoryId, e);
            // 缓存异常时直接查询数据库
            return productRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getCategoryId, categoryId)
                    .eq(Product::getDeleted, false)
                    .orderByDesc(Product::getCreatedAt)
            );
        }
    }

    /**
     * 根据品牌ID查询商品列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Product> findByBrandId(Long brandId) {
        if (brandId == null) {
            return List.of();
        }
        
        String cacheKey = PRODUCT_BY_BRAND_KEY_PREFIX + brandId;
        
        try {
            // 先从缓存获取
            List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProducts != null) {
                log.debug("从缓存获取商品列表: brandId={}, count={}", brandId, cachedProducts.size());
                return cachedProducts;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: brandId={}", brandId);
            List<Product> products = productRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getBrandId, brandId)
                    .eq(Product::getDeleted, false)
                    .orderByDesc(Product::getCreatedAt)
            );
            
            if (products != null && !products.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, products, CACHE_EXPIRE_TIME);
                // 同时缓存每个商品的详细信息
                for (Product product : products) {
                    redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), product, CACHE_EXPIRE_TIME);
                }
                log.debug("商品列表数据已缓存: brandId={}, count={}", brandId, products.size());
            }
            
            return products != null ? products : List.of();
            
        } catch (Exception e) {
            log.error("查询商品列表失败: brandId={}", brandId, e);
            // 缓存异常时直接查询数据库
            return productRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getBrandId, brandId)
                    .eq(Product::getDeleted, false)
                    .orderByDesc(Product::getCreatedAt)
            );
        }
    }

    /**
     * 根据公司ID查询商品列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Product> findByCompanyId(Long companyId) {
        if (companyId == null) {
            return List.of();
        }
        
        String cacheKey = PRODUCT_BY_COMPANY_KEY_PREFIX + companyId;
        
        try {
            // 先从缓存获取
            List<Product> cachedProducts = (List<Product>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedProducts != null) {
                log.debug("从缓存获取商品列表: companyId={}, count={}", companyId, cachedProducts.size());
                return cachedProducts;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: companyId={}", companyId);
            List<Product> products = productRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getCompanyId, companyId)
                    .eq(Product::getDeleted, false)
                    .orderByDesc(Product::getCreatedAt)
            );
            
            if (products != null && !products.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, products, CACHE_EXPIRE_TIME);
                // 同时缓存每个商品的详细信息
                for (Product product : products) {
                    redisTemplate.opsForValue().set(PRODUCT_KEY_PREFIX + product.getId(), product, CACHE_EXPIRE_TIME);
                }
                log.debug("商品列表数据已缓存: companyId={}, count={}", companyId, products.size());
            }
            
            return products != null ? products : List.of();
            
        } catch (Exception e) {
            log.error("查询商品列表失败: companyId={}", companyId, e);
            // 缓存异常时直接查询数据库
            return productRepository.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Product>()
                    .eq(Product::getCompanyId, companyId)
                    .eq(Product::getDeleted, false)
                    .orderByDesc(Product::getCreatedAt)
            );
        }
    }

    /**
     * 插入商品（同时更新缓存）
     */
    public int insert(Product product) {
        try {
            int result = productRepository.insert(product);
            if (result > 0 && product.getId() != null) {
                // 清除相关缓存
                clearProductCache(product);
                log.debug("商品插入成功，已清除相关缓存: id={}", product.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("插入商品失败", e);
            throw e;
        }
    }

    /**
     * 更新商品（同时更新缓存）
     */
    public int updateById(Product product) {
        try {
            int result = productRepository.updateById(product);
            if (result > 0) {
                // 清除相关缓存
                clearProductCache(product);
                log.debug("商品更新成功，已清除相关缓存: id={}", product.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("更新商品失败: id={}", product.getId(), e);
            throw e;
        }
    }

    /**
     * 删除商品（同时清除缓存）
     */
    public int deleteById(Long id) {
        try {
            // 先获取商品信息，用于清除缓存
            Product product = productRepository.selectById(id);
            int result = productRepository.deleteById(id);
            if (result > 0) {
                // 清除相关缓存
                if (product != null) {
                    clearProductCache(product);
                }
                log.debug("商品删除成功，已清除相关缓存: id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除商品失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 清除商品相关缓存
     */
    private void clearProductCache(Product product) {
        try {
            if (product == null) {
                return;
            }
            
            // 清除各种键的缓存
            redisTemplate.delete(PRODUCT_KEY_PREFIX + product.getId());
            
            if (product.getSku() != null) {
                redisTemplate.delete(PRODUCT_BY_SKU_KEY_PREFIX + product.getSku());
            }
            if (product.getName() != null) {
                redisTemplate.delete(PRODUCT_BY_NAME_KEY_PREFIX + product.getName());
            }
            if (product.getCategoryId() != null) {
                redisTemplate.delete(PRODUCT_BY_CATEGORY_KEY_PREFIX + product.getCategoryId());
            }
            if (product.getBrandId() != null) {
                redisTemplate.delete(PRODUCT_BY_BRAND_KEY_PREFIX + product.getBrandId());
            }
            if (product.getCompanyId() != null) {
                redisTemplate.delete(PRODUCT_BY_COMPANY_KEY_PREFIX + product.getCompanyId());
            }
            if (product.getStatus() != null) {
                redisTemplate.delete(PRODUCT_BY_STATUS_KEY_PREFIX + product.getStatus());
            }
            if (product.getApprovalStatus() != null) {
                redisTemplate.delete(PRODUCT_BY_APPROVAL_STATUS_KEY_PREFIX + product.getApprovalStatus());
            }
            
            log.debug("商品缓存已清除: id={}", product.getId());
            
        } catch (Exception e) {
            log.error("清除商品缓存失败: id={}", product.getId(), e);
        }
    }

    /**
     * 手动刷新商品缓存
     */
    public void refreshProductCache(Long productId) {
        try {
            Product product = productRepository.selectById(productId);
            if (product != null && product.getId() != null) {
                // 清除旧缓存
                clearProductCache(product);
                // 重新缓存
                String cacheKey = PRODUCT_KEY_PREFIX + product.getId();
                redisTemplate.opsForValue().set(cacheKey, product, CACHE_EXPIRE_TIME);
                log.info("商品缓存已刷新: id={}", productId);
            }
        } catch (Exception e) {
            log.error("刷新商品缓存失败: id={}", productId, e);
        }
    }

    /**
     * 清除所有商品缓存
     */
    public void clearAllProductCache() {
        try {
            // 获取所有商品相关的键
            String pattern = PRODUCT_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_SKU_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_NAME_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_CATEGORY_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_BRAND_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_COMPANY_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_STATUS_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = PRODUCT_BY_APPROVAL_STATUS_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            log.info("所有商品缓存已清除");
        } catch (Exception e) {
            log.error("清除所有商品缓存失败", e);
        }
    }
}
