package com.yibang.erp.infrastructure.repository;

import com.yibang.erp.domain.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

/**
 * 客户Redis缓存Repository
 * 基于Redis对CustomerRepository进行缓存封装，防止数据库击穿
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Repository
public class CustomerRedisRepository {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    // Redis键前缀
    private static final String CUSTOMER_KEY_PREFIX = "customer:";
    private static final String CUSTOMER_BY_CODE_KEY_PREFIX = "customer:code:";
    private static final String CUSTOMER_BY_NAME_KEY_PREFIX = "customer:name:";
    private static final String CUSTOMER_BY_COMPANY_KEY_PREFIX = "customer:company:";
    private static final String CUSTOMER_BY_TYPE_KEY_PREFIX = "customer:type:";
    private static final String CUSTOMER_BY_LEVEL_KEY_PREFIX = "customer:level:";
    private static final String CUSTOMER_BY_STATUS_KEY_PREFIX = "customer:status:";
    private static final String CUSTOMER_BY_PHONE_KEY_PREFIX = "customer:phone:";
    private static final String CUSTOMER_BY_EMAIL_KEY_PREFIX = "customer:email:";
    
    // 缓存过期时间：5分钟
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(5);

    /**
     * 根据ID查询客户（带缓存）
     */
    public Customer selectById(Long id) {
        if (id == null) {
            return null;
        }
        
        String cacheKey = CUSTOMER_KEY_PREFIX + id;
        
        try {
            // 先从缓存获取
            Customer cachedCustomer = (Customer) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomer != null) {
                log.debug("从缓存获取客户: id={}", id);
                return cachedCustomer;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: id={}", id);
            Customer customer = customerRepository.selectById(id);
            
            if (customer != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customer, CACHE_EXPIRE_TIME);
                log.debug("客户数据已缓存: id={}", id);
            }
            
            return customer;
            
        } catch (Exception e) {
            log.error("查询客户失败: id={}", id, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectById(id);
        }
    }

    /**
     * 根据客户编码查询客户（带缓存）
     */
    public Customer findByCustomerCode(String customerCode) {
        if (customerCode == null || customerCode.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = CUSTOMER_BY_CODE_KEY_PREFIX + customerCode;
        
        try {
            // 先从缓存获取
            Customer cachedCustomer = (Customer) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomer != null) {
                log.debug("从缓存获取客户: customerCode={}", customerCode);
                return cachedCustomer;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: customerCode={}", customerCode);
            Customer customer = customerRepository.selectByCustomerCode(customerCode);
            
            if (customer != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customer, CACHE_EXPIRE_TIME);
                // 同时缓存客户ID映射
                redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                log.debug("客户数据已缓存: customerCode={}, id={}", customerCode, customer.getId());
            }
            
            return customer;
            
        } catch (Exception e) {
            log.error("查询客户失败: customerCode={}", customerCode, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectByCustomerCode(customerCode);
        }
    }

    /**
     * 根据客户名称查询客户（带缓存）
     */
    public Customer findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = CUSTOMER_BY_NAME_KEY_PREFIX + name;
        
        try {
            // 先从缓存获取
            Customer cachedCustomer = (Customer) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomer != null) {
                log.debug("从缓存获取客户: name={}", name);
                return cachedCustomer;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: name={}", name);
            Customer customer = customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getName, name)
                    .eq(Customer::getDeleted, false)
            );
            
            if (customer != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customer, CACHE_EXPIRE_TIME);
                // 同时缓存客户ID映射
                redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                log.debug("客户数据已缓存: name={}, id={}", name, customer.getId());
            }
            
            return customer;
            
        } catch (Exception e) {
            log.error("查询客户失败: name={}", name, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getName, name)
                    .eq(Customer::getDeleted, false)
            );
        }
    }

    /**
     * 根据手机号查询客户（带缓存）
     */
    public Customer findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = CUSTOMER_BY_PHONE_KEY_PREFIX + phone;
        
        try {
            // 先从缓存获取
            Customer cachedCustomer = (Customer) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomer != null) {
                log.debug("从缓存获取客户: phone={}", phone);
                return cachedCustomer;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: phone={}", phone);
            Customer customer = customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getContactPhone, phone)
                    .eq(Customer::getDeleted, false)
            );
            
            if (customer != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customer, CACHE_EXPIRE_TIME);
                // 同时缓存客户ID映射
                redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                log.debug("客户数据已缓存: phone={}, id={}", phone, customer.getId());
            }
            
            return customer;
            
        } catch (Exception e) {
            log.error("查询客户失败: phone={}", phone, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getContactPhone, phone)
                    .eq(Customer::getDeleted, false)
            );
        }
    }

    /**
     * 根据邮箱查询客户（带缓存）
     */
    public Customer findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = CUSTOMER_BY_EMAIL_KEY_PREFIX + email;
        
        try {
            // 先从缓存获取
            Customer cachedCustomer = (Customer) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomer != null) {
                log.debug("从缓存获取客户: email={}", email);
                return cachedCustomer;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: email={}", email);
            Customer customer = customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getContactEmail, email)
                    .eq(Customer::getDeleted, false)
            );
            
            if (customer != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customer, CACHE_EXPIRE_TIME);
                // 同时缓存客户ID映射
                redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                log.debug("客户数据已缓存: email={}, id={}", email, customer.getId());
            }
            
            return customer;
            
        } catch (Exception e) {
            log.error("查询客户失败: email={}", email, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Customer>()
                    .eq(Customer::getContactEmail, email)
                    .eq(Customer::getDeleted, false)
            );
        }
    }

    /**
     * 根据公司ID查询客户列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Customer> findByCompanyId(Long companyId) {
        if (companyId == null) {
            return List.of();
        }
        
        String cacheKey = CUSTOMER_BY_COMPANY_KEY_PREFIX + companyId;
        
        try {
            // 先从缓存获取
            List<Customer> cachedCustomers = (List<Customer>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomers != null) {
                log.debug("从缓存获取客户列表: companyId={}, count={}", companyId, cachedCustomers.size());
                return cachedCustomers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: companyId={}", companyId);
            List<Customer> customers = customerRepository.selectByCompanyId(companyId);
            
            if (customers != null && !customers.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customers, CACHE_EXPIRE_TIME);
                // 同时缓存每个客户的详细信息
                for (Customer customer : customers) {
                    redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                }
                log.debug("客户列表数据已缓存: companyId={}, count={}", companyId, customers.size());
            }
            
            return customers != null ? customers : List.of();
            
        } catch (Exception e) {
            log.error("查询客户列表失败: companyId={}", companyId, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectByCompanyId(companyId);
        }
    }

    /**
     * 根据客户类型查询客户列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Customer> findByCustomerType(String customerType) {
        if (customerType == null || customerType.trim().isEmpty()) {
            return List.of();
        }
        
        String cacheKey = CUSTOMER_BY_TYPE_KEY_PREFIX + customerType;
        
        try {
            // 先从缓存获取
            List<Customer> cachedCustomers = (List<Customer>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomers != null) {
                log.debug("从缓存获取客户列表: customerType={}, count={}", customerType, cachedCustomers.size());
                return cachedCustomers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: customerType={}", customerType);
            List<Customer> customers = customerRepository.selectByCustomerType(customerType);
            
            if (customers != null && !customers.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customers, CACHE_EXPIRE_TIME);
                // 同时缓存每个客户的详细信息
                for (Customer customer : customers) {
                    redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                }
                log.debug("客户列表数据已缓存: customerType={}, count={}", customerType, customers.size());
            }
            
            return customers != null ? customers : List.of();
            
        } catch (Exception e) {
            log.error("查询客户列表失败: customerType={}", customerType, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectByCustomerType(customerType);
        }
    }

    /**
     * 根据客户等级查询客户列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Customer> findByCustomerLevel(String customerLevel) {
        if (customerLevel == null || customerLevel.trim().isEmpty()) {
            return List.of();
        }
        
        String cacheKey = CUSTOMER_BY_LEVEL_KEY_PREFIX + customerLevel;
        
        try {
            // 先从缓存获取
            List<Customer> cachedCustomers = (List<Customer>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomers != null) {
                log.debug("从缓存获取客户列表: customerLevel={}, count={}", customerLevel, cachedCustomers.size());
                return cachedCustomers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: customerLevel={}", customerLevel);
            List<Customer> customers = customerRepository.selectByCustomerLevel(customerLevel);
            
            if (customers != null && !customers.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customers, CACHE_EXPIRE_TIME);
                // 同时缓存每个客户的详细信息
                for (Customer customer : customers) {
                    redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                }
                log.debug("客户列表数据已缓存: customerLevel={}, count={}", customerLevel, customers.size());
            }
            
            return customers != null ? customers : List.of();
            
        } catch (Exception e) {
            log.error("查询客户列表失败: customerLevel={}", customerLevel, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectByCustomerLevel(customerLevel);
        }
    }

    /**
     * 根据状态查询客户列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Customer> findByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return List.of();
        }
        
        String cacheKey = CUSTOMER_BY_STATUS_KEY_PREFIX + status;
        
        try {
            // 先从缓存获取
            List<Customer> cachedCustomers = (List<Customer>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomers != null) {
                log.debug("从缓存获取客户列表: status={}, count={}", status, cachedCustomers.size());
                return cachedCustomers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: status={}", status);
            List<Customer> customers = customerRepository.selectByStatus(status);
            
            if (customers != null && !customers.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customers, CACHE_EXPIRE_TIME);
                // 同时缓存每个客户的详细信息
                for (Customer customer : customers) {
                    redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                }
                log.debug("客户列表数据已缓存: status={}, count={}", status, customers.size());
            }
            
            return customers != null ? customers : List.of();
            
        } catch (Exception e) {
            log.error("查询客户列表失败: status={}", status, e);
            // 缓存异常时直接查询数据库
            return customerRepository.selectByStatus(status);
        }
    }

    /**
     * 根据关键词搜索客户（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<Customer> searchByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        
        String cacheKey = "customer:search:" + keyword;
        
        try {
            // 先从缓存获取
            List<Customer> cachedCustomers = (List<Customer>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedCustomers != null) {
                log.debug("从缓存获取搜索结果: keyword={}, count={}", keyword, cachedCustomers.size());
                return cachedCustomers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: keyword={}", keyword);
            List<Customer> customers = customerRepository.searchByKeyword(keyword);
            
            if (customers != null && !customers.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, customers, CACHE_EXPIRE_TIME);
                // 同时缓存每个客户的详细信息
                for (Customer customer : customers) {
                    redisTemplate.opsForValue().set(CUSTOMER_KEY_PREFIX + customer.getId(), customer, CACHE_EXPIRE_TIME);
                }
                log.debug("搜索结果已缓存: keyword={}, count={}", keyword, customers.size());
            }
            
            return customers != null ? customers : List.of();
            
        } catch (Exception e) {
            log.error("搜索客户失败: keyword={}", keyword, e);
            // 缓存异常时直接查询数据库
            return customerRepository.searchByKeyword(keyword);
        }
    }

    /**
     * 插入客户（同时更新缓存）
     */
    public int insert(Customer customer) {
        try {
            int result = customerRepository.insert(customer);
            if (result > 0 && customer.getId() != null) {
                // 清除相关缓存
                clearCustomerCache(customer);
                log.debug("客户插入成功，已清除相关缓存: id={}", customer.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("插入客户失败", e);
            throw e;
        }
    }

    /**
     * 更新客户（同时更新缓存）
     */
    public int updateById(Customer customer) {
        try {
            int result = customerRepository.updateById(customer);
            if (result > 0) {
                // 清除相关缓存
                clearCustomerCache(customer);
                log.debug("客户更新成功，已清除相关缓存: id={}", customer.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("更新客户失败: id={}", customer.getId(), e);
            throw e;
        }
    }

    /**
     * 删除客户（同时清除缓存）
     */
    public int deleteById(Long id) {
        try {
            // 先获取客户信息，用于清除缓存
            Customer customer = customerRepository.selectById(id);
            int result = customerRepository.deleteById(id);
            if (result > 0) {
                // 清除相关缓存
                if (customer != null) {
                    clearCustomerCache(customer);
                }
                log.debug("客户删除成功，已清除相关缓存: id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除客户失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 清除客户相关缓存
     */
    private void clearCustomerCache(Customer customer) {
        try {
            if (customer == null) {
                return;
            }
            
            // 清除各种键的缓存
            redisTemplate.delete(CUSTOMER_KEY_PREFIX + customer.getId());
            
            if (customer.getCustomerCode() != null) {
                redisTemplate.delete(CUSTOMER_BY_CODE_KEY_PREFIX + customer.getCustomerCode());
            }
            if (customer.getName() != null) {
                redisTemplate.delete(CUSTOMER_BY_NAME_KEY_PREFIX + customer.getName());
            }
            if (customer.getContactPhone() != null) {
                redisTemplate.delete(CUSTOMER_BY_PHONE_KEY_PREFIX + customer.getContactPhone());
            }
            if (customer.getContactEmail() != null) {
                redisTemplate.delete(CUSTOMER_BY_EMAIL_KEY_PREFIX + customer.getContactEmail());
            }
            if (customer.getCompanyId() != null) {
                redisTemplate.delete(CUSTOMER_BY_COMPANY_KEY_PREFIX + customer.getCompanyId());
            }
            if (customer.getCustomerType() != null) {
                redisTemplate.delete(CUSTOMER_BY_TYPE_KEY_PREFIX + customer.getCustomerType());
            }
            if (customer.getCustomerLevel() != null) {
                redisTemplate.delete(CUSTOMER_BY_LEVEL_KEY_PREFIX + customer.getCustomerLevel());
            }
            if (customer.getStatus() != null) {
                redisTemplate.delete(CUSTOMER_BY_STATUS_KEY_PREFIX + customer.getStatus());
            }
            
            log.debug("客户缓存已清除: id={}", customer.getId());
            
        } catch (Exception e) {
            log.error("清除客户缓存失败: id={}", customer.getId(), e);
        }
    }

    /**
     * 手动刷新客户缓存
     */
    public void refreshCustomerCache(Long customerId) {
        try {
            Customer customer = customerRepository.selectById(customerId);
            if (customer != null && customer.getId() != null) {
                // 清除旧缓存
                clearCustomerCache(customer);
                // 重新缓存
                String cacheKey = CUSTOMER_KEY_PREFIX + customer.getId();
                redisTemplate.opsForValue().set(cacheKey, customer, CACHE_EXPIRE_TIME);
                log.info("客户缓存已刷新: id={}", customerId);
            }
        } catch (Exception e) {
            log.error("刷新客户缓存失败: id={}", customerId, e);
        }
    }

    /**
     * 清除所有客户缓存
     */
    public void clearAllCustomerCache() {
        try {
            // 获取所有客户相关的键
            String pattern = CUSTOMER_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_CODE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_NAME_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_PHONE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_EMAIL_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_COMPANY_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_TYPE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_LEVEL_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = CUSTOMER_BY_STATUS_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = "customer:search:*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            log.info("所有客户缓存已清除");
        } catch (Exception e) {
            log.error("清除所有客户缓存失败", e);
        }
    }
}
