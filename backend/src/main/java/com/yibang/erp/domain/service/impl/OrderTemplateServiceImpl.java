package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yibang.erp.domain.dto.OrderTemplateRequest;
import com.yibang.erp.domain.dto.OrderTemplateResponse;
import com.yibang.erp.domain.entity.OrderTemplate;
import com.yibang.erp.infrastructure.repository.OrderTemplateRepository;
import com.yibang.erp.domain.service.OrderTemplateService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单模板服务实现类
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderTemplateServiceImpl extends ServiceImpl<OrderTemplateRepository, OrderTemplate> implements OrderTemplateService {

    @Autowired
    private OrderTemplateRepository orderTemplateRepository;

    @Override
    public OrderTemplateResponse createTemplate(OrderTemplateRequest request) {
        // 验证模板名称唯一性
        validateTemplateNameUnique(request.getTemplateName(), null);
        
        // 创建模板实体
        OrderTemplate template = new OrderTemplate();
        BeanUtils.copyProperties(request, template);
        
        // 设置默认值
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setCreatedBy(getCurrentUserId());
        template.setUpdatedBy(getCurrentUserId());
        
        // 如果是第一个模板，自动设置为活跃状态
        if (getAllTemplates().isEmpty()) {
            template.setIsActive(true);
        }
        
        // 保存模板
        orderTemplateRepository.insert(template);
        
        return buildTemplateResponse(template);
    }

    @Override
    public OrderTemplateResponse updateTemplate(Long templateId, OrderTemplateRequest request) {
        // 检查模板是否存在
        OrderTemplate existingTemplate = getTemplateEntityById(templateId);
        
        // 验证模板名称唯一性（排除当前模板）
        validateTemplateNameUnique(request.getTemplateName(), templateId);
        
        // 更新模板
        BeanUtils.copyProperties(request, existingTemplate);
        existingTemplate.setUpdatedAt(LocalDateTime.now());
        existingTemplate.setUpdatedBy(getCurrentUserId());
        
        orderTemplateRepository.updateById(existingTemplate);
        
        return buildTemplateResponse(existingTemplate);
    }

    @Override
    public boolean deleteTemplate(Long templateId) {
        // 检查模板是否存在
        OrderTemplate template = getTemplateEntityById(templateId);
        
        // 检查是否为活跃模板
        if (template.getIsActive()) {
            throw new IllegalStateException("无法删除活跃模板，请先激活其他模板");
        }
        
        // 删除模板
        return orderTemplateRepository.deleteById(templateId) > 0;
    }

    @Override
    public OrderTemplateResponse getTemplateById(Long templateId) {
        OrderTemplate template = getTemplateEntityById(templateId);
        return buildTemplateResponse(template);
    }

    @Override
    public List<OrderTemplateResponse> getAllTemplates() {
        QueryWrapper<OrderTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_at");
        
        List<OrderTemplate> templates = orderTemplateRepository.selectList(queryWrapper);
        return templates.stream()
                .map(this::buildTemplateResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderTemplateResponse getActiveTemplate() {
        QueryWrapper<OrderTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true);
        
        OrderTemplate activeTemplate = orderTemplateRepository.selectOne(queryWrapper);
        if (activeTemplate == null) {
            throw new IllegalStateException("没有找到活跃的订单模板");
        }
        
        return buildTemplateResponse(activeTemplate);
    }

    @Override
    @Transactional
    public OrderTemplateResponse activateTemplate(Long templateId) {
        // 检查模板是否存在
        OrderTemplate template = getTemplateEntityById(templateId);
        
        // 先取消所有模板的活跃状态
        UpdateWrapper<OrderTemplate> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("is_active", false)
                   .set("updated_at", LocalDateTime.now())
                   .set("updated_by", getCurrentUserId());
        orderTemplateRepository.update(null, updateWrapper);
        
        // 激活指定模板
        template.setIsActive(true);
        template.setUpdatedAt(LocalDateTime.now());
        template.setUpdatedBy(getCurrentUserId());
        orderTemplateRepository.updateById(template);
        
        return buildTemplateResponse(template);
    }

    @Override
    public List<OrderTemplateResponse> getTemplateVersionHistory(Long templateId) {
        // 检查模板是否存在
        getTemplateEntityById(templateId);
        
        // 获取版本历史（这里简化处理，实际可能需要版本表）
        QueryWrapper<OrderTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_name", 
                orderTemplateRepository.selectById(templateId).getTemplateName())
                   .orderByDesc("version")
                   .orderByDesc("created_at");
        
        List<OrderTemplate> templates = orderTemplateRepository.selectList(queryWrapper);
        return templates.stream()
                .map(this::buildTemplateResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean validateTemplate(OrderTemplateRequest request) {
        // 验证必填字段
        if (!StringUtils.hasText(request.getTemplateName())) {
            return false;
        }
        
        if (!StringUtils.hasText(request.getVersion())) {
            return false;
        }
        
        if (request.getIsActive() == null) {
            return false;
        }
        
        // 验证字段配置
        if (request.getCoreFieldsConfig() != null && request.getCoreFieldsConfig().isEmpty()) {
            return false;
        }
        
        // 验证业务规则配置
        if (request.getBusinessRulesConfig() != null && request.getBusinessRulesConfig().isEmpty()) {
            return false;
        }
        
        return true;
    }

    /**
     * 根据ID获取模板实体
     */
    private OrderTemplate getTemplateEntityById(Long templateId) {
        OrderTemplate template = orderTemplateRepository.selectById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }
        return template;
    }

    /**
     * 验证模板名称唯一性
     */
    private void validateTemplateNameUnique(String templateName, Long excludeId) {
        QueryWrapper<OrderTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("template_name", templateName);
        
        if (excludeId != null) {
            queryWrapper.ne("id", excludeId);
        }
        
        if (orderTemplateRepository.selectCount(queryWrapper) > 0) {
            throw new IllegalArgumentException("模板名称已存在: " + templateName);
        }
    }

    /**
     * 构建模板响应对象
     */
    private OrderTemplateResponse buildTemplateResponse(OrderTemplate template) {
        OrderTemplateResponse response = new OrderTemplateResponse();
        BeanUtils.copyProperties(template, response);
        
        // 设置创建人和更新人姓名（这里简化处理，实际需要查询用户表）
        response.setCreatedByName("系统用户");
        response.setUpdatedByName("系统用户");
        
        return response;
    }

    /**
     * 获取当前用户ID（这里简化处理，实际需要从安全上下文获取）
     */
    private Long getCurrentUserId() {
        // TODO: 从Spring Security上下文获取当前用户ID
        return 1L;
    }
}
