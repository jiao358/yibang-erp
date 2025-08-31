package com.yibang.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.entity.Company;
import com.yibang.erp.domain.service.CompanyService;
import com.yibang.erp.infrastructure.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公司管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN','SUPPLIER_ADMIN')")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyRepository companyRepository;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询公司列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCompanyPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        
        try {
            Page<Company> pageParam = new Page<>(page, size);
            IPage<Company> companyPage = companyService.getCompanyPage(pageParam, name, type, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", companyPage);
            response.put("message", "获取公司列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取公司列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID获取公司
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCompanyById(@PathVariable Long id) {
        try {
            Company company = companyService.getCompanyById(id);
            if (company == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "公司不存在");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", company);
            response.put("message", "获取公司信息成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取公司信息失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建公司
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCompany(@RequestBody Company company) {
        try {
            Company createdCompany = companyService.createCompany(company);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdCompany);
            response.put("message", "创建公司成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建公司失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新公司
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        try {
            company.setId(id);
            Company updatedCompany = companyService.updateCompany(company);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedCompany);
            response.put("message", "更新公司成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新公司失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除公司
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCompany(@PathVariable Long id) {
        try {
            boolean success = companyService.deleteCompany(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除公司成功" : "删除公司失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除公司失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量删除公司
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteCompanies(@RequestBody List<Long> ids) {
        try {
            int deletedCount = companyService.batchDeleteCompanies(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", deletedCount);
            response.put("message", "批量删除公司成功，共删除 " + deletedCount + " 个公司");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量删除公司失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新公司状态
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateCompanyStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            boolean success = companyService.updateCompanyStatus(id, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "更新公司状态成功" : "更新公司状态失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新公司状态失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有公司列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllCompanies() {
        try {
            List<Company> companies = companyService.getAllCompanies();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", companies);
            response.put("message", "获取所有公司成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取所有公司失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据业务类型获取公司列表
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getCompaniesByType(@PathVariable String type) {
        try {
            List<Company> companies = companyService.getCompaniesByType(type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", companies);
            response.put("message", "获取" + type + "类型公司列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取公司列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取激活状态的公司列表
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveCompanies() {
        try {
            List<Company> companies = companyService.getActiveCompanies();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", companies);
            response.put("message", "获取激活公司列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取激活公司列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取供应商公司列表（供销售用户使用）
     */
    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES_USER')")
    public ResponseEntity<Map<String, Object>> getSupplierCompanies(
            @RequestHeader("Authorization") String authHeader) {
        try {
            // 获取当前用户公司ID
            Long currentUserCompanyId = extractCompanyIdFromJWT(authHeader);
            
            // 查询所有供应商类型的公司
            QueryWrapper<Company> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("type", "SUPPLIER");
            queryWrapper.eq("status", "ACTIVE");
            queryWrapper.eq("deleted", false);
            
            // 如果不是系统管理员，只显示与当前用户公司有关系的供应商
            if (currentUserCompanyId != null && !hasSystemAdminRole(authHeader)) {
                // TODO: 这里可以根据业务关系表查询，暂时返回所有供应商
            }
            
            List<Company> suppliers = companyRepository.selectList(queryWrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", suppliers);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取供应商公司列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取供应商公司列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 从JWT中提取公司ID
     */
    private Long extractCompanyIdFromJWT(String authHeader) {
        try {
            String token = authHeader.substring(7);
            Long companyId = jwtUtil.getCompanyIdFromToken(token);
            return companyId;
        } catch (Exception e) {
            log.warn("从JWT提取公司ID失败", e);
            return null;
        }
    }

    /**
     * 检查是否有系统管理员角色
     */
    private boolean hasSystemAdminRole(String authHeader) {
        try {
            String token = authHeader.substring(7);
            List<String> roles = jwtUtil.getRolesFromToken(token);
            return roles != null && roles.contains("SYSTEM_ADMIN");
        } catch (Exception e) {
            log.warn("检查系统管理员角色失败", e);
            return false;
        }
    }
}
