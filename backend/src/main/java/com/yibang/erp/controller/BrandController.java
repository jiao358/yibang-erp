package com.yibang.erp.controller;

import com.yibang.erp.domain.entity.Brand;
import com.yibang.erp.domain.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 品牌管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@RestController
@RequestMapping("/api/brands")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN','SALES','SUPPLIER_OPERATOR')")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 获取所有活跃品牌
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllActiveBrands() {
        try {
            List<Brand> brands = brandService.getAllActiveBrands();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", brands);
            response.put("message", "获取品牌列表成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取品牌列表失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据ID获取品牌
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBrandById(@PathVariable Long id) {
        try {
            Brand brand = brandService.getBrandById(id);
            if (brand == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "品牌不存在");
                response.put("timestamp", System.currentTimeMillis());
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", brand);
            response.put("message", "获取品牌信息成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取品牌信息失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建品牌
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createBrand(@RequestBody Brand brand) {
        try {
            Brand createdBrand = brandService.createBrand(brand);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdBrand);
            response.put("message", "创建品牌成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建品牌失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新品牌
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBrand(@PathVariable Long id, @RequestBody Brand brand) {
        try {
            brand.setId(id);
            Brand updatedBrand = brandService.updateBrand(brand);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updatedBrand);
            response.put("message", "更新品牌成功");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新品牌失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBrand(@PathVariable Long id) {
        try {
            boolean success = brandService.deleteBrand(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除品牌成功" : "删除品牌失败");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除品牌失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新品牌状态
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateBrandStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            boolean success = brandService.updateBrandStatus(id, status);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "更新品牌状态成功" : "更新品牌状态失败");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新品牌状态失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.badRequest().body(response);
        }
    }
}
