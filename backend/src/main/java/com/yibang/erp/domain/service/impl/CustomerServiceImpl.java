package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Customer;
import com.yibang.erp.domain.service.CustomerService;
import com.yibang.erp.infrastructure.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Customer getCustomerById(Long id) {
        return customerRepository.selectById(id);
    }

    @Override
    public Customer getCustomerByCode(String customerCode) {
        return customerRepository.selectByCustomerCode(customerCode);
    }

    @Override
    public Customer getCustomerByOpenId(String openId) {
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getOpenId, openId);
        return customerRepository.selectOne(queryWrapper);
    }

    @Override
    public Customer getCustomerByUnionId(String unionId) {
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getUnionId, unionId);
        return customerRepository.selectOne(queryWrapper);
    }

    @Override
    public Customer getCustomerByPhone(String phone) {
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Customer::getContactPhone, phone);
        return customerRepository.selectOne(queryWrapper);
    }

    @Override
    public Customer createCustomer(Customer customer) {
        // 生成客户编码
        if (!StringUtils.hasText(customer.getCustomerCode())) {
            customer.setCustomerCode(generateCustomerCode());
        }
        
        // 设置默认值
        if (!StringUtils.hasText(customer.getCustomerType())) {
            customer.setCustomerType("INDIVIDUAL");
        }
        if (!StringUtils.hasText(customer.getCustomerLevel())) {
            customer.setCustomerLevel("REGULAR");
        }
        if (!StringUtils.hasText(customer.getStatus())) {
            customer.setStatus("ACTIVE");
        }
        if (customer.getCompanyId() == null) {
            customer.setCompanyId(1L); // 默认公司ID，实际应该从配置获取
        }
        
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        
        customerRepository.insert(customer);
        return customer;
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.updateById(customer);
        return customer;
    }

    @Override
    public Page<Customer> getCustomerList(Page<Customer> page, String keyword, String customerType, 
                                          String customerLevel, String status, Long companyId) {
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(Customer::getName, keyword)
                .or()
                .like(Customer::getCustomerCode, keyword)
                .or()
                .like(Customer::getContactPhone, keyword)
                .or()
                .like(Customer::getContactEmail, keyword)
            );
        }
        
        if (StringUtils.hasText(customerType)) {
            queryWrapper.eq(Customer::getCustomerType, customerType);
        }
        
        if (StringUtils.hasText(customerLevel)) {
            queryWrapper.eq(Customer::getCustomerLevel, customerLevel);
        }
        
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Customer::getStatus, status);
        }
        
        if (companyId != null) {
            queryWrapper.eq(Customer::getCompanyId, companyId);
        }
        
        queryWrapper.orderByDesc(Customer::getCreatedAt);
        
        return customerRepository.selectPage(page, queryWrapper);
    }

    @Override
    public List<Customer> getCustomersByCompanyId(Long companyId) {
        return customerRepository.selectByCompanyId(companyId);
    }

    @Override
    public List<Customer> searchCustomers(String keyword, Integer limit) {
        LambdaQueryWrapper<Customer> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                .like(Customer::getName, keyword)
                .or()
                .like(Customer::getCustomerCode, keyword)
                .or()
                .like(Customer::getContactPhone, keyword)
                .or()
                .like(Customer::getContactEmail, keyword)
            );
        }
        
        queryWrapper.orderByDesc(Customer::getCreatedAt);
        
        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }
        
        return customerRepository.selectList(queryWrapper);
    }

    /**
     * 生成客户编码
     */
    private String generateCustomerCode() {
        // 简单的客户编码生成逻辑，实际应该更复杂
        return "CUST" + System.currentTimeMillis();
    }
}
