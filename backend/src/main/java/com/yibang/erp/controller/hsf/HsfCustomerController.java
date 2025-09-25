package com.yibang.erp.controller.hsf;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.microservice.CustomerInfoResponse;
import com.yibang.erp.domain.dto.microservice.CustomerRegisterRequest;
import com.yibang.erp.domain.dto.microservice.CustomerUpdateRequest;
import com.yibang.erp.domain.dto.microservice.Result;
import com.yibang.erp.domain.entity.Customer;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.CustomerService;
import com.yibang.erp.infrastructure.repository.CustomerRepository;
import com.yibang.erp.infrastructure.repository.UserRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HSF客户服务控制器
 * 提供给yibang-mall系统调用
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/hsf/customer")
public class HsfCustomerController {

    @Autowired
    private CustomerService customerService;
    
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRedisRepository userRedisRepository;

    /**
     * 客户注册
     */
    @PostMapping("/register")
    public Result<CustomerInfoResponse> registerCustomer(@RequestBody CustomerRegisterRequest request) {
        try {
            log.info("HSF API调用: 客户注册 - name={}, phone={}, openId={}", 
                    request.getName(), request.getPhone(), request.getOpenId());
            
            // 检查是否已存在相同openId的客户
            Customer existingCustomer = customerService.getCustomerByOpenId(request.getOpenId());
            if (existingCustomer != null) {
                return Result.error("该微信用户已注册");
            }
            
            // 检查是否已存在相同手机号的客户
            Customer existingByPhone = customerService.getCustomerByPhone(request.getPhone());
            if (existingByPhone != null) {
                return Result.error("该手机号已注册");
            }
            
            // 创建客户
            Customer customer = new Customer();
            customer.setName(request.getName());
            customer.setContactPhone(request.getPhone());
            customer.setOpenId(request.getOpenId());
            customer.setUnionId(request.getUnionId());
            customer.setProvince(request.getProvince());
            customer.setCity(request.getCity());
            customer.setGender(request.getGender());
            customer.setCountry(request.getCountry());
            customer.setContactEmail(request.getEmail());
            customer.setAddress(request.getAddress());

            User estela= userRedisRepository.findByUsername("estela");

            customer.setCompanyId(estela.getCompanyId());
            customer.setCustomerType("INDIVIDUAL"); // 默认为个人客户
            customer.setCustomerLevel("REGULAR"); // 默认为普通客户
            customer.setStatus("ACTIVE");
            customer.setCreatedAt(LocalDateTime.now());
            customer.setUpdatedAt(LocalDateTime.now());
            customer.setCreatedBy(0L); // 系统创建
            customer.setUpdatedBy(0L);
            customer.setDeleted(false);
            customer.setCustomerCode(request.getOpenId());

            
            Customer createdCustomer = customerService.createCustomer(customer);
            CustomerInfoResponse response = convertToCustomerInfoResponse(createdCustomer);
            
            return Result.success("客户注册成功", response);
        } catch (Exception e) {
            log.error("HSF API调用失败: 客户注册", e);
            return Result.error("客户注册失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取客户详情
     */
    @GetMapping("/{id}")
    public Result<CustomerInfoResponse> getCustomerById(@PathVariable Long id) {
        try {
            log.info("HSF API调用: 根据ID获取客户详情 - {}", id);
            
            Customer customer = customerService.getCustomerById(id);
            if (customer == null) {
                return Result.error("客户不存在");
            }

            CustomerInfoResponse response = convertToCustomerInfoResponse(customer);
            return Result.success("获取客户详情成功", response);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据ID获取客户详情", e);
            return Result.error("获取客户详情失败: " + e.getMessage());
        }
    }

    /**
     * 根据微信open_id获取客户信息
     */
    @GetMapping("/openid/{openId}")
    public Result<CustomerInfoResponse> getCustomerByOpenId(@PathVariable String openId) {
        try {
            log.info("HSF API调用: 根据微信open_id获取客户信息 - {}", openId);
            
            Customer customer = customerService.getCustomerByOpenId(openId);
            if (customer == null) {
                return Result.error("客户不存在");
            }

            CustomerInfoResponse response = convertToCustomerInfoResponse(customer);
            return Result.success("获取客户信息成功", response);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据微信open_id获取客户信息", e);
            return Result.error("获取客户信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据手机号获取客户信息
     */
    @GetMapping("/phone/{phone}")
    public Result<CustomerInfoResponse> getCustomerByPhone(@PathVariable String phone) {
        try {
            log.info("HSF API调用: 根据手机号获取客户信息 - {}", phone);
            
            Customer customer = customerService.getCustomerByPhone(phone);
            if (customer == null) {
                return Result.error("客户不存在");
            }

            CustomerInfoResponse response = convertToCustomerInfoResponse(customer);
            return Result.success("获取客户信息成功", response);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据手机号获取客户信息", e);
            return Result.error("获取客户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户列表
     */
    @GetMapping("/list")
    public Result<PageResult<CustomerInfoResponse>> getCustomerList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String customerType,
            @RequestParam(required = false) String customerLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long companyId) {
        try {
            log.info("HSF API调用: 获取客户列表 - page={}, size={}, keyword={}", page, size, keyword);
            
            // 构建查询条件
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
            
            // 先查询总数
            long total = customerRepository.selectCount(queryWrapper);
            
            if (total == 0) {
                PageResult<CustomerInfoResponse> emptyResult = PageResult.empty(page, size);
                return Result.success("获取客户列表成功", emptyResult);
            }
            
            // 添加分页条件
            int offset = (page - 1) * size;
            queryWrapper.last(String.format("LIMIT %d, %d", offset, size));
            
            // 查询数据
            List<Customer> customers = customerRepository.selectList(queryWrapper);
            
            // 转换为响应格式
            List<CustomerInfoResponse> customerList = customers.stream()
                    .map(this::convertToCustomerInfoResponse)
                    .collect(Collectors.toList());
            
            PageResult<CustomerInfoResponse> pageResult = PageResult.of(customerList, total, page, size);
            
            return Result.success("获取客户列表成功", pageResult);
        } catch (Exception e) {
            log.error("HSF API调用失败: 获取客户列表", e);
            return Result.error("获取客户列表失败: " + e.getMessage());
        }
    }

    /**
     * 更新客户信息
     */
    @PutMapping("/update")
    public Result<CustomerInfoResponse> updateCustomer(@RequestBody CustomerUpdateRequest request) {
        try {
            log.info("HSF API调用: 更新客户信息 - id={}", request.getId());
            
            Customer existingCustomer = customerService.getCustomerById(request.getId());
            if (existingCustomer == null) {
                return Result.error("客户不存在");
            }
            
            // 更新客户信息
            if (request.getName() != null) {
                existingCustomer.setName(request.getName());
            }
            if (request.getPhone() != null) {
                existingCustomer.setContactPhone(request.getPhone());
            }
            if (request.getOpenId() != null) {
                existingCustomer.setOpenId(request.getOpenId());
            }
            if (request.getUnionId() != null) {
                existingCustomer.setUnionId(request.getUnionId());
            }
            if (request.getProvince() != null) {
                existingCustomer.setProvince(request.getProvince());
            }
            if (request.getCity() != null) {
                existingCustomer.setCity(request.getCity());
            }
            if (request.getGender() != null) {
                existingCustomer.setGender(request.getGender());
            }
            if (request.getCountry() != null) {
                existingCustomer.setCountry(request.getCountry());
            }
            if (request.getEmail() != null) {
                existingCustomer.setContactEmail(request.getEmail());
            }
            if (request.getAddress() != null) {
                existingCustomer.setAddress(request.getAddress());
            }
            if (request.getContactPerson() != null) {
                existingCustomer.setContactPerson(request.getContactPerson());
            }
            if (request.getCustomerType() != null) {
                existingCustomer.setCustomerType(request.getCustomerType());
            }
            if (request.getCustomerLevel() != null) {
                existingCustomer.setCustomerLevel(request.getCustomerLevel());
            }
            if (request.getStatus() != null) {
                existingCustomer.setStatus(request.getStatus());
            }
            
            Customer updatedCustomer = customerService.updateCustomer(existingCustomer);
            CustomerInfoResponse response = convertToCustomerInfoResponse(updatedCustomer);
            
            return Result.success("更新客户信息成功", response);
        } catch (Exception e) {
            log.error("HSF API调用失败: 更新客户信息", e);
            return Result.error("更新客户信息失败: " + e.getMessage());
        }
    }

    /**
     * 搜索客户
     */
    @GetMapping("/search")
    public Result<List<CustomerInfoResponse>> searchCustomers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("HSF API调用: 搜索客户 - keyword={}, limit={}", keyword, limit);
            
            List<Customer> customers = customerService.searchCustomers(keyword, limit);
            List<CustomerInfoResponse> customerList = customers.stream()
                    .map(this::convertToCustomerInfoResponse)
                    .collect(Collectors.toList());
            
            return Result.success("搜索客户成功", customerList);
        } catch (Exception e) {
            log.error("HSF API调用失败: 搜索客户", e);
            return Result.error("搜索客户失败: " + e.getMessage());
        }
    }

    /**
     * 转换Customer为CustomerInfoResponse
     */
    private CustomerInfoResponse convertToCustomerInfoResponse(Customer customer) {
        CustomerInfoResponse response = new CustomerInfoResponse();
        response.setId(customer.getId());
        response.setCustomerCode(customer.getCustomerCode());
        response.setName(customer.getName());
        response.setCompanyId(customer.getCompanyId());
        response.setContactPerson(customer.getContactPerson());
        response.setContactPhone(customer.getContactPhone());
        response.setContactEmail(customer.getContactEmail());
        response.setAddress(customer.getAddress());
        response.setCustomerType(customer.getCustomerType());
        response.setCustomerLevel(customer.getCustomerLevel());
        response.setCreditLimit(customer.getCreditLimit());
        response.setPaymentTerms(customer.getPaymentTerms());
        response.setTaxNumber(customer.getTaxNumber());
        response.setBankAccount(customer.getBankAccount());
        response.setStatus(customer.getStatus());
        response.setOpenId(customer.getOpenId());
        response.setUnionId(customer.getUnionId());
        response.setProvince(customer.getProvince());
        response.setCity(customer.getCity());
        response.setGender(customer.getGender());
        response.setCountry(customer.getCountry());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());
        return response;
    }
}
