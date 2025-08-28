package com.yibang.erp.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.Role;
import com.yibang.erp.domain.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRolePage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status) {
        
        try {
            Page<Role> pageParam = new Page<>(page, size);
            IPage<Role> rolePage = roleService.getRolePage(pageParam, name, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", rolePage);
            response.put("message", "获取角色列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取角色列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID获取角色
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRoleById(@PathVariable Long id) {
        try {
            Role role = roleService.getRoleById(id);
            if (role == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "角色不存在");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", role);
            response.put("message", "获取角色信息成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取角色信息失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建角色
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        try {
            Role createdRole = roleService.createRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdRole);
            response.put("message", "创建角色成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建角色失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        try {
            role.setId(id);
            Role updatedRole = roleService.updateRole(role);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedRole);
            response.put("message", "更新角色成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新角色失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRole(@PathVariable Long id) {
        try {
            boolean success = roleService.deleteRole(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除角色成功" : "删除角色失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除角色失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteRoles(@RequestBody List<Long> ids) {
        try {
            int deletedCount = roleService.batchDeleteRoles(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", deletedCount);
            response.put("message", "批量删除角色成功，共删除 " + deletedCount + " 个角色");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量删除角色失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新角色状态
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateRoleStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        try {
            boolean success = roleService.updateRoleStatus(id, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "更新角色状态成功" : "更新角色状态失败");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新角色状态失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有角色列表
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllRoles() {
        try {
            List<Role> roles = roleService.getAllRoles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roles);
            response.put("message", "获取所有角色成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取所有角色失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取激活状态的角色列表
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveRoles() {
        try {
            List<Role> roles = roleService.getActiveRoles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", roles);
            response.put("message", "获取激活角色列表成功");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取激活角色列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
