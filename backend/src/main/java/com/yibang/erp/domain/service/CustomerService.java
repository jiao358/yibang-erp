package com.yibang.erp.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Customer;

import java.util.List;

/**
 * 客户服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface CustomerService {
    
    /**
     * 根据ID获取客户
     */
    Customer getCustomerById(Long id);
    
    /**
     * 根据客户编码获取客户
     */
    Customer getCustomerByCode(String customerCode);
    
    /**
     * 根据微信open_id获取客户
     */
    Customer getCustomerByOpenId(String openId);
    
    /**
     * 根据微信union_id获取客户
     */
    Customer getCustomerByUnionId(String unionId);
    
    /**
     * 根据手机号获取客户
     */
    Customer getCustomerByPhone(String phone);
    
    /**
     * 创建客户
     */
    Customer createCustomer(Customer customer);
    
    /**
     * 更新客户
     */
    Customer updateCustomer(Customer customer);
    
    /**
     * 分页查询客户列表
     */
    Page<Customer> getCustomerList(Page<Customer> page, String keyword, String customerType, 
                                   String customerLevel, String status, Long companyId);
    
    /**
     * 根据公司ID查询客户列表
     */
    List<Customer> getCustomersByCompanyId(Long companyId);
    
    /**
     * 搜索客户
     */
    List<Customer> searchCustomers(String keyword, Integer limit);
}
