package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.entity.Warehouse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

/**
 * 仓库Redis缓存Repository
 * 基于Redis对WarehouseRepository进行缓存封装，防止数据库击穿
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Repository
public class WarehouseRedisRepository {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private WarehouseRepository warehouseRepository;

    // Redis键前缀
    private static final String WAREHOUSE_KEY_PREFIX = "warehouse:";
    private static final String WAREHOUSE_BY_CODE_KEY_PREFIX = "warehouse:code:";
    private static final String WAREHOUSE_BY_COMPANY_KEY_PREFIX = "warehouse:company:";
    private static final String WAREHOUSE_BY_STATUS_KEY_PREFIX = "warehouse:status:";
    private static final String WAREHOUSE_BY_CODE_COMPANY_KEY_PREFIX = "warehouse:code:company:";
    
    // 缓存过期时间：5分钟
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(5);

    /**
     * 根据ID查询仓库（带缓存）
     */
    public Warehouse selectById(Long id) {
        if (id == null) {
            return null;
        }
        
        String cacheKey = WAREHOUSE_KEY_PREFIX + id;
        
        try {
            // 先从缓存获取
            Warehouse cachedWarehouse = (Warehouse) redisTemplate.opsForValue().get(cacheKey);
            if (cachedWarehouse != null) {
                log.debug("从缓存获取仓库: id={}", id);
                return cachedWarehouse;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: id={}", id);
            Warehouse warehouse = warehouseRepository.selectById(id);
            
            if (warehouse != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, warehouse, CACHE_EXPIRE_TIME);
                log.debug("仓库数据已缓存: id={}", id);
            }
            
            return warehouse;
            
        } catch (Exception e) {
            log.error("查询仓库失败: id={}", id, e);
            // 缓存异常时直接查询数据库
            return warehouseRepository.selectById(id);
        }
    }

    /**
     * 根据仓库编码和公司ID查询仓库（带缓存）
     * 对应原代码：QueryWrapper<Warehouse> warehouseWrapper = new QueryWrapper<>();
     * warehouseWrapper.eq("warehouse_code", warehouseCode);
     * warehouseWrapper.eq("status", "ACTIVE");
     * warehouseWrapper.eq("deleted", 0);
     * warehouseWrapper.eq("company_id", supplierCompanyId);
     */
    public Warehouse findByCodeAndCompanyAndStatus(String warehouseCode, Long companyId, String status) {
        if (warehouseCode == null || companyId == null || status == null) {
            return null;
        }
        
        String cacheKey = WAREHOUSE_BY_CODE_COMPANY_KEY_PREFIX + warehouseCode + ":" + companyId + ":" + status;
        
        try {
            // 先从缓存获取
            Warehouse cachedWarehouse = (Warehouse) redisTemplate.opsForValue().get(cacheKey);
            if (cachedWarehouse != null) {
                log.debug("从缓存获取仓库: code={}, companyId={}, status={}", warehouseCode, companyId, status);
                return cachedWarehouse;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: code={}, companyId={}, status={}", warehouseCode, companyId, status);
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("warehouse_code", warehouseCode)
                       .eq("status", status)
                       .eq("deleted", 0)
                       .eq("company_id", companyId);
            Warehouse warehouse = warehouseRepository.selectOne(queryWrapper);
            
            if (warehouse != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, warehouse, CACHE_EXPIRE_TIME);
                // 同时缓存仓库ID映射
                redisTemplate.opsForValue().set(WAREHOUSE_KEY_PREFIX + warehouse.getId(), warehouse, CACHE_EXPIRE_TIME);
                log.debug("仓库数据已缓存: code={}, companyId={}, status={}, id={}", 
                    warehouseCode, companyId, status, warehouse.getId());
            }
            
            return warehouse;
            
        } catch (Exception e) {
            log.error("查询仓库失败: code={}, companyId={}, status={}", warehouseCode, companyId, status, e);
            // 缓存异常时直接查询数据库
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("warehouse_code", warehouseCode)
                       .eq("status", status)
                       .eq("deleted", 0)
                       .eq("company_id", companyId);
            return warehouseRepository.selectOne(queryWrapper);
        }
    }

    /**
     * 根据公司ID查询仓库列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Warehouse> findByCompanyId(Long companyId) {
        if (companyId == null) {
            return List.of();
        }
        
        String cacheKey = WAREHOUSE_BY_COMPANY_KEY_PREFIX + companyId;
        
        try {
            // 先从缓存获取
            List<Warehouse> cachedWarehouses = (List<Warehouse>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedWarehouses != null) {
                log.debug("从缓存获取仓库列表: companyId={}, count={}", companyId, cachedWarehouses.size());
                return cachedWarehouses;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: companyId={}", companyId);
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_id", companyId)
                       .eq("deleted", 0);
            List<Warehouse> warehouses = warehouseRepository.selectList(queryWrapper);
            
            if (warehouses != null && !warehouses.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, warehouses, CACHE_EXPIRE_TIME);
                // 同时缓存每个仓库的详细信息
                for (Warehouse warehouse : warehouses) {
                    redisTemplate.opsForValue().set(WAREHOUSE_KEY_PREFIX + warehouse.getId(), warehouse, CACHE_EXPIRE_TIME);
                }
                log.debug("仓库列表数据已缓存: companyId={}, count={}", companyId, warehouses.size());
            }
            
            return warehouses != null ? warehouses : List.of();
            
        } catch (Exception e) {
            log.error("查询仓库列表失败: companyId={}", companyId, e);
            // 缓存异常时直接查询数据库
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_id", companyId)
                       .eq("deleted", 0);
            return warehouseRepository.selectList(queryWrapper);
        }
    }

    /**
     * 根据状态查询仓库列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Warehouse> findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return List.of();
        }
        
        String cacheKey = WAREHOUSE_BY_STATUS_KEY_PREFIX + status;
        
        try {
            // 先从缓存获取
            List<Warehouse> cachedWarehouses = (List<Warehouse>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedWarehouses != null) {
                log.debug("从缓存获取仓库列表: status={}, count={}", status, cachedWarehouses.size());
                return cachedWarehouses;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: status={}", status);
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", status)
                       .eq("deleted", 0);
            List<Warehouse> warehouses = warehouseRepository.selectList(queryWrapper);
            
            if (warehouses != null && !warehouses.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, warehouses, CACHE_EXPIRE_TIME);
                // 同时缓存每个仓库的详细信息
                for (Warehouse warehouse : warehouses) {
                    redisTemplate.opsForValue().set(WAREHOUSE_KEY_PREFIX + warehouse.getId(), warehouse, CACHE_EXPIRE_TIME);
                }
                log.debug("仓库列表数据已缓存: status={}, count={}", status, warehouses.size());
            }
            
            return warehouses != null ? warehouses : List.of();
            
        } catch (Exception e) {
            log.error("查询仓库列表失败: status={}", status, e);
            // 缓存异常时直接查询数据库
            QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", status)
                       .eq("deleted", 0);
            return warehouseRepository.selectList(queryWrapper);
        }
    }

    /**
     * 插入仓库（同时更新缓存）
     */
    public int insert(Warehouse warehouse) {
        try {
            int result = warehouseRepository.insert(warehouse);
            if (result > 0 && warehouse.getId() != null) {
                // 清除相关缓存
                clearWarehouseCache(warehouse);
                log.debug("仓库插入成功，已清除相关缓存: id={}", warehouse.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("插入仓库失败", e);
            throw e;
        }
    }

    /**
     * 更新仓库（同时更新缓存）
     */
    public int updateById(Warehouse warehouse) {
        try {
            int result = warehouseRepository.updateById(warehouse);
            if (result > 0) {
                // 清除相关缓存
                clearWarehouseCache(warehouse);
                log.debug("仓库更新成功，已清除相关缓存: id={}", warehouse.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("更新仓库失败: id={}", warehouse.getId(), e);
            throw e;
        }
    }

    /**
     * 删除仓库（同时清除缓存）
     */
    public int deleteById(Long id) {
        try {
            // 先获取仓库信息，用于清除缓存
            Warehouse warehouse = warehouseRepository.selectById(id);
            int result = warehouseRepository.deleteById(id);
            if (result > 0) {
                // 清除相关缓存
                if (warehouse != null) {
                    clearWarehouseCache(warehouse);
                }
                log.debug("仓库删除成功，已清除相关缓存: id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除仓库失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 清除仓库相关缓存
     */
    private void clearWarehouseCache(Warehouse warehouse) {
        try {
            if (warehouse == null) {
                return;
            }
            
            // 清除各种键的缓存
            redisTemplate.delete(WAREHOUSE_KEY_PREFIX + warehouse.getId());
            
            if (warehouse.getWarehouseCode() != null && warehouse.getCompanyId() != null && warehouse.getStatus() != null) {
                redisTemplate.delete(WAREHOUSE_BY_CODE_COMPANY_KEY_PREFIX + 
                    warehouse.getWarehouseCode() + ":" + warehouse.getCompanyId() + ":" + warehouse.getStatus());
            }
            if (warehouse.getCompanyId() != null) {
                redisTemplate.delete(WAREHOUSE_BY_COMPANY_KEY_PREFIX + warehouse.getCompanyId());
            }
            if (warehouse.getStatus() != null) {
                redisTemplate.delete(WAREHOUSE_BY_STATUS_KEY_PREFIX + warehouse.getStatus());
            }
            
            log.debug("仓库缓存已清除: id={}", warehouse.getId());
            
        } catch (Exception e) {
            log.error("清除仓库缓存失败: id={}", warehouse != null ? warehouse.getId() : "null", e);
        }
    }

    /**
     * 手动刷新仓库缓存
     */
    public void refreshWarehouseCache(Long warehouseId) {
        try {
            Warehouse warehouse = warehouseRepository.selectById(warehouseId);
            if (warehouse != null && warehouse.getId() != null) {
                Long id = warehouse.getId();
                // 清除旧缓存
                clearWarehouseCache(warehouse);
                // 重新缓存
                String cacheKey = WAREHOUSE_KEY_PREFIX + id;
                redisTemplate.opsForValue().set(cacheKey, warehouse, CACHE_EXPIRE_TIME);
                log.info("仓库缓存已刷新: id={}", warehouseId);
            }
        } catch (Exception e) {
            log.error("刷新仓库缓存失败: id={}", warehouseId, e);
        }
    }

    /**
     * 清除所有仓库缓存
     */
    public void clearAllWarehouseCache() {
        try {
            // 获取所有仓库相关的键
            String pattern = WAREHOUSE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = WAREHOUSE_BY_CODE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = WAREHOUSE_BY_COMPANY_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = WAREHOUSE_BY_STATUS_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = WAREHOUSE_BY_CODE_COMPANY_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            log.info("所有仓库缓存已清除");
        } catch (Exception e) {
            log.error("清除所有仓库缓存失败", e);
        }
    }
}
