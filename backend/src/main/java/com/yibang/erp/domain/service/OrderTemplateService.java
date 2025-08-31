package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.OrderTemplateRequest;
import com.yibang.erp.domain.dto.OrderTemplateResponse;

import java.util.List;

/**
 * 订单模板服务接口
 */
public interface OrderTemplateService {

    /**
     * 创建订单模板
     */
    OrderTemplateResponse createTemplate(OrderTemplateRequest request);

    /**
     * 更新订单模板
     */
    OrderTemplateResponse updateTemplate(Long templateId, OrderTemplateRequest request);

    /**
     * 删除订单模板
     */
    boolean deleteTemplate(Long templateId);

    /**
     * 根据ID获取订单模板
     */
    OrderTemplateResponse getTemplateById(Long templateId);

    /**
     * 获取所有订单模板
     */
    List<OrderTemplateResponse> getAllTemplates();

    /**
     * 获取当前活跃的订单模板
     */
    OrderTemplateResponse getActiveTemplate();

    /**
     * 激活指定版本的订单模板
     */
    OrderTemplateResponse activateTemplate(Long templateId);

    /**
     * 获取模板版本历史
     */
    List<OrderTemplateResponse> getTemplateVersionHistory(Long templateId);

    /**
     * 验证模板配置
     */
    boolean validateTemplate(OrderTemplateRequest request);
}
