package com.yibang.erp.controller;

import com.yibang.erp.domain.entity.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 客户管理控制器
 */
@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
public class CustomerController {

    @Autowired
    private com.yibang.erp.infrastructure.repository.CustomerRepository customerRepository;

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
     * 获取所有客户
     */
    @GetMapping
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
