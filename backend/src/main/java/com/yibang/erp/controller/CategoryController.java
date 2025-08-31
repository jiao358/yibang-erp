package com.yibang.erp.controller;

import com.yibang.erp.domain.entity.Category;
import com.yibang.erp.domain.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分类管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取所有活跃分类
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllActiveCategories() {
        try {
            List<Category> categories = categoryService.getAllActiveCategories();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            response.put("message", "获取分类列表成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID获取分类
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            if (category == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "分类不存在");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", category);
            response.put("message", "获取分类信息成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类信息失败，分类ID: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类信息失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建分类
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> createCategory(@RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdCategory);
            response.put("message", "创建分类成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建分类失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建分类失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        try {
            category.setId(id);
            Category updatedCategory = categoryService.updateCategory(category);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedCategory);
            response.put("message", "更新分类成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新分类失败，分类ID: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新分类失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        try {
            boolean success = categoryService.deleteCategory(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除分类成功" : "删除分类失败");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除分类失败，分类ID: {}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除分类失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新分类状态
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCategoryStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            boolean success = categoryService.updateCategoryStatus(id, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "更新分类状态成功" : "更新分类状态失败");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新分类状态失败，分类ID: {}, 状态: {}", id, status, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新分类状态失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取分类树结构
     */
    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        try {
            List<Category> categoryTree = categoryService.getCategoryTree();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categoryTree);
            response.put("message", "获取分类树成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类树失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类树失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据父分类ID获取子分类
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<Map<String, Object>> getCategoriesByParentId(@PathVariable Long parentId) {
        try {
            List<Category> categories = categoryService.getCategoriesByParentId(parentId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            response.put("message", "获取子分类成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取子分类失败，父分类ID: {}", parentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取子分类失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据层级获取分类
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<Map<String, Object>> getCategoriesByLevel(@PathVariable Integer level) {
        try {
            List<Category> categories = categoryService.getCategoriesByLevel(level);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", categories);
            response.put("message", "获取分类成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取分类失败，层级: {}", level, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分类失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
