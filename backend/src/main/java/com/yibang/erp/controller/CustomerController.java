package com.yibang.erp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.entity.Customer;
import com.yibang.erp.domain.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
public class CustomerController {

    @Autowired
    private com.yibang.erp.infrastructure.repository.CustomerRepository customerRepository;
    
    @Autowired
    private CustomerService customerService;

    /**
     * 创建客户
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        customerRepository.insert(customer);
        return ResponseEntity.ok(customer);
    }

    /**
     * 更新客户
     */
    @PutMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SALES')")
    public ResponseEntity<Customer> updateCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody Customer customer) {
        customer.setId(customerId);
        customerRepository.updateById(customer);
        return ResponseEntity.ok(customer);
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Boolean> deleteCustomer(@PathVariable Long customerId) {
        boolean result = customerRepository.deleteById(customerId) > 0;
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取客户
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long customerId) {
        Customer customer = customerRepository.selectById(customerId);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 根据客户编码获取客户
     */
    @GetMapping("/code/{customerCode}")
    public ResponseEntity<Customer> getCustomerByCode(@PathVariable String customerCode) {
        Customer customer = customerRepository.selectByCustomerCode(customerCode);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 分页查询客户列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCustomerList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String customerCode,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String customerLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId) {
        try {
            // 构建分页对象
            Page<Customer> page = new Page<>(current, size);
            
            // 确定搜索关键词：优先使用keyword，其次使用name或customerCode
            String searchKeyword = null;
            if (StringUtils.hasText(keyword)) {
                searchKeyword = keyword;
            } else if (StringUtils.hasText(name)) {
                searchKeyword = name;
            } else if (StringUtils.hasText(customerCode)) {
                searchKeyword = customerCode;
            }
            
            // 调用Service进行分页查询
            Page<Customer> customerPage = customerService.getCustomerList(
                    page, 
                    searchKeyword, 
                    customerType, 
                    customerLevel, 
                    status, 
                    companyId
            );
            
            // 构建分页结果
            PageResult<Customer> pageResult = PageResult.of(
                    customerPage.getRecords(), 
                    customerPage.getTotal(), 
                    customerPage.getCurrent(), 
                    customerPage.getSize()
            );
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pageResult);
            response.put("message", "获取客户列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "获取客户列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取所有客户（保留此接口用于其他需要完整列表的场景）
     */
    @GetMapping("/all")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.selectList(null);
        return ResponseEntity.ok(customers);
    }

    /**
     * 根据公司ID获取客户列表
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<Customer>> getCustomersByCompanyId(@PathVariable Long companyId) {
        List<Customer> customers = customerRepository.selectByCompanyId(companyId);
        return ResponseEntity.ok(customers);
    }

    /**
     * 根据客户类型获取客户列表
     */
    @GetMapping("/type/{customerType}")
    public ResponseEntity<List<Customer>> getCustomersByType(@PathVariable String customerType) {
        List<Customer> customers = customerRepository.selectByCustomerType(customerType);
        return ResponseEntity.ok(customers);
    }

    /**
     * 根据客户等级获取客户列表
     */
    @GetMapping("/level/{customerLevel}")
    public ResponseEntity<List<Customer>> getCustomersByLevel(@PathVariable String customerLevel) {
        List<Customer> customers = customerRepository.selectByCustomerLevel(customerLevel);
        return ResponseEntity.ok(customers);
    }

    /**
     * 根据状态获取客户列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Customer>> getCustomersByStatus(@PathVariable String status) {
        List<Customer> customers = customerRepository.selectByStatus(status);
        return ResponseEntity.ok(customers);
    }

    /**
     * 根据关键词搜索客户
     */
    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String keyword) {
        List<Customer> customers = customerRepository.searchByKeyword(keyword);
        return ResponseEntity.ok(customers);
    }
}
