package com.yibang.erp.controller;

import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.JwtUtil;
import com.yibang.erp.domain.dto.PasswordResetRequest;
import com.yibang.erp.domain.dto.UserListResponse;
import com.yibang.erp.domain.dto.UserQueryRequest;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.CompanyService;
import com.yibang.erp.domain.service.RoleService;
import com.yibang.erp.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private CompanyService companyService;
    @Autowired
    private  JwtUtil jwtUtil;

    /**
     * 分页查询用户列表（带角色和公司信息联查）
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String personalCompanyName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestHeader("Authorization") String authHeader

    ) {
        
        try {
            // 构建查询请求
            UserQueryRequest queryRequest = new UserQueryRequest(current, size);
            queryRequest.setUsername(username);
            queryRequest.setRealName(personalCompanyName); // 个人公司名称映射到real_name字段
            queryRequest.setEmail(email);
            queryRequest.setPhone(phone);
            queryRequest.setStatus(status);
            queryRequest.setCompanyId(companyId);
            queryRequest.setSortField(sortField);
            queryRequest.setSortOrder(sortOrder);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of("error", "缺少有效的Authorization头"));
            }

            String token = authHeader.substring(7);
            Long belongCompayId = jwtUtil.getCompanyIdFromToken(token);


            // 查询用户列表（带角色和公司信息）
            PageResult<UserListResponse> userPage = userService.getUserPageWithRoleAndCompany(queryRequest,belongCompayId);




            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", userPage);
            response.put("message", "获取用户列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "用户不存在");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", user);
            response.put("message", "获取用户信息成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户信息失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user,@RequestHeader("Authorization") String authHeader) {
        try {

            String token = authHeader.substring(7);

            Long companyIdFromToken = jwtUtil.getCompanyIdFromToken(token);
            user.setSupplierCompanyId(companyIdFromToken);

            // 权限检查：供应链管理员只能创建销售角色用户
            if (!hasFullUserPermission()) {
                // 检查角色是否为销售角色
                if (user.getRoleId() != null) {
                    String roleName = roleService.getRoleById(user.getRoleId()).getName();
                    if (!"SALES".equals(roleName)) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("message", "权限不足：只能创建销售角色用户");
                        response.put("timestamp", System.currentTimeMillis());
                        return ResponseEntity.status(403).body(response);
                    }
                }
            }
            
            User createdUser = userService.createUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdUser);
            response.put("message", "创建用户成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建用户失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedUser);
            response.put("message", "更新用户成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新用户失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除用户成功" : "删除用户失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除用户失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable Long id, @RequestBody PasswordResetRequest request) {
        try {
            // 验证密码确认
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "两次输入的密码不一致");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean success = userService.resetPassword(id, request.getNewPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "重置密码成功" : "重置密码失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "重置密码失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取可用的角色列表（根据权限过滤）
     */
    @GetMapping("/roles/available")
    public ResponseEntity<Map<String, Object>> getAvailableRoles() {
        try {
            List<Map<String, Object>> roles;
            
            if (hasFullUserPermission()) {
                // 系统管理员：所有角色
                roles = roleService.getAllActiveRoles();
            } else {
                // 供应链管理员：只能看到销售角色
                roles = roleService.getRolesByName("SALES");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roles);
            response.put("message", "获取可用角色成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取可用角色失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取可用的公司列表（根据权限过滤）
     */
    @GetMapping("/companies/available")
    public ResponseEntity<Map<String, Object>> getAvailableCompanies() {
        try {
            List<Map<String, Object>> companies;
            
            if (hasFullUserPermission()) {
                // 系统管理员：所有公司
                companies = companyService.getAllActiveCompanies();
            } else {
                // 供应链管理员：只能看到销售公司
                companies = companyService.getCompaniesByTypeMap("SALES");
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", companies);
            response.put("message", "获取可用公司成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取可用公司失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查是否有完整的用户管理权限
     */
    private boolean hasFullUserPermission() {
        // 这里应该从SecurityContext获取当前用户角色
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object auths= authentication.getAuthorities();
        // 暂时返回true，实际应该根据JWT token中的角色判断
        return true;
    }
}
