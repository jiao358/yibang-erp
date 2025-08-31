package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.OrderTemplateRequest;
import com.yibang.erp.domain.dto.OrderTemplateResponse;
import com.yibang.erp.domain.entity.OrderTemplate;
import com.yibang.erp.domain.service.OrderTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 订单模板管理控制器
 */
@RestController
@RequestMapping("/api/order-templates")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class OrderTemplateController {

    @Autowired
    private OrderTemplateService orderTemplateService;

    /**
     * 创建订单模板
     */
    @PostMapping
    public ResponseEntity<OrderTemplateResponse> createTemplate(@Valid @RequestBody OrderTemplateRequest request) {
        OrderTemplateResponse response = orderTemplateService.createTemplate(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新订单模板
     */
    @PutMapping("/{templateId}")
    public ResponseEntity<OrderTemplateResponse> updateTemplate(
            @PathVariable Long templateId,
            @Valid @RequestBody OrderTemplateRequest request) {
        OrderTemplateResponse response = orderTemplateService.updateTemplate(templateId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除订单模板
     */
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Boolean> deleteTemplate(@PathVariable Long templateId) {
        boolean result = orderTemplateService.deleteTemplate(templateId);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据ID获取订单模板
     */
    @GetMapping("/{templateId}")
    public ResponseEntity<OrderTemplateResponse> getTemplateById(@PathVariable Long templateId) {
        OrderTemplateResponse response = orderTemplateService.getTemplateById(templateId);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 获取所有订单模板
     */
    @GetMapping
    public ResponseEntity<List<OrderTemplateResponse>> getAllTemplates() {
        List<OrderTemplateResponse> response = orderTemplateService.getAllTemplates();
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前活跃的订单模板
     */
    @GetMapping("/active")
    public ResponseEntity<OrderTemplateResponse> getActiveTemplate() {
        OrderTemplateResponse response = orderTemplateService.getActiveTemplate();
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 激活指定版本的订单模板
     */
    @PostMapping("/{templateId}/activate")
    public ResponseEntity<OrderTemplateResponse> activateTemplate(@PathVariable Long templateId) {
        OrderTemplateResponse response = orderTemplateService.activateTemplate(templateId);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取模板版本历史
     */
    @GetMapping("/{templateId}/version-history")
    public ResponseEntity<List<OrderTemplateResponse>> getTemplateVersionHistory(@PathVariable Long templateId) {
        List<OrderTemplateResponse> response = orderTemplateService.getTemplateVersionHistory(templateId);
        return ResponseEntity.ok(response);
    }

    /**
     * 验证模板配置
     */
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateTemplate(@Valid @RequestBody OrderTemplateRequest request) {
        boolean result = orderTemplateService.validateTemplate(request);
        return ResponseEntity.ok(result);
    }
}
