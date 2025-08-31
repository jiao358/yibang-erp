package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.dto.InventoryAlertRequest;
import com.yibang.erp.domain.entity.InventoryAlert;
import com.yibang.erp.infrastructure.repository.InventoryAlertRepository;
import com.yibang.erp.domain.service.InventoryAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 库存预警服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryAlertServiceImpl implements InventoryAlertService {

    private final InventoryAlertRepository inventoryAlertRepository;

    @Override
    @Transactional
    public InventoryAlert createAlert(InventoryAlertRequest request, Long operatorId) {
        // 检查是否已存在相同类型的预警
        QueryWrapper<InventoryAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", request.getProductId())
                   .eq("warehouse_id", request.getWarehouseId())
                   .eq("alert_type", request.getAlertType())
                   .eq("status", "PENDING")
                   .eq("deleted", false);
        
        if (inventoryAlertRepository.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("该商品已存在相同类型的待处理预警");
        }

        InventoryAlert alert = new InventoryAlert();
        BeanUtils.copyProperties(request, alert);
        alert.setAlertNo(generateAlertNo());
        alert.setCreatedBy(operatorId);
        alert.setUpdatedBy(operatorId);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        alert.setDeleted(false);

        inventoryAlertRepository.insert(alert);
        log.info("库存预警创建成功，ID: {}, 类型: {}", alert.getId(), alert.getAlertType());
        
        return alert;
    }

    @Override
    @Transactional
    public InventoryAlert updateAlert(Long id, InventoryAlertRequest request, Long operatorId) {
        InventoryAlert existingAlert = inventoryAlertRepository.selectById(id);
        if (existingAlert == null || existingAlert.getDeleted()) {
            throw new RuntimeException("库存预警不存在");
        }

        BeanUtils.copyProperties(request, existingAlert);
        existingAlert.setUpdatedBy(operatorId);
        existingAlert.setUpdatedAt(LocalDateTime.now());

        inventoryAlertRepository.updateById(existingAlert);
        log.info("库存预警更新成功，ID: {}", id);
        
        return existingAlert;
    }

    @Override
    @Transactional
    public boolean deleteAlert(Long id, Long operatorId) {
        InventoryAlert alert = inventoryAlertRepository.selectById(id);
        if (alert == null || alert.getDeleted()) {
            return false;
        }

        alert.setDeleted(true);
        alert.setUpdatedBy(operatorId);
        alert.setUpdatedAt(LocalDateTime.now());

        inventoryAlertRepository.updateById(alert);
        log.info("库存预警删除成功，ID: {}", id);
        
        return true;
    }

    @Override
    public InventoryAlert getAlertById(Long id) {
        InventoryAlert alert = inventoryAlertRepository.selectById(id);
        if (alert == null || alert.getDeleted()) {
            throw new RuntimeException("库存预警不存在");
        }
        return alert;
    }

    @Override
    public Page<InventoryAlert> getAlertPage(int page, int size, String alertType, String alertLevel,
                                            String status, Long productId, Long warehouseId) {
        QueryWrapper<InventoryAlert> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);

        if (StringUtils.hasText(alertType)) {
            queryWrapper.eq("alert_type", alertType);
        }
        if (StringUtils.hasText(alertLevel)) {
            queryWrapper.eq("alert_level", alertLevel);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (productId != null) {
            queryWrapper.eq("product_id", productId);
        }
        if (warehouseId != null) {
            queryWrapper.eq("warehouse_id", warehouseId);
        }

        queryWrapper.orderByDesc("alert_level", "created_at");

        Page<InventoryAlert> pageParam = new Page<>(page, size);
        return inventoryAlertRepository.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<InventoryAlert> getPendingAlerts() {
        return inventoryAlertRepository.findPendingAlerts();
    }

    @Override
    public List<InventoryAlert> getHighPriorityAlerts() {
        return inventoryAlertRepository.findHighPriorityAlerts();
    }

    @Override
    @Transactional
    public boolean handleAlert(Long id, String handlingResult, String handlingRemark, Long handlerId) {
        InventoryAlert alert = inventoryAlertRepository.selectById(id);
        if (alert == null || alert.getDeleted()) {
            return false;
        }

        alert.setStatus("RESOLVED");
        alert.setHandlerId(handlerId);
        alert.setHandledAt(LocalDateTime.now());
        alert.setHandlingResult(handlingResult);
        alert.setHandlingRemark(handlingRemark);
        alert.setUpdatedBy(handlerId);
        alert.setUpdatedAt(LocalDateTime.now());

        inventoryAlertRepository.updateById(alert);
        log.info("库存预警处理成功，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean ignoreAlert(Long id, String reason, Long operatorId) {
        InventoryAlert alert = inventoryAlertRepository.selectById(id);
        if (alert == null || alert.getDeleted()) {
            return false;
        }

        alert.setStatus("IGNORED");
        alert.setHandlerId(operatorId);
        alert.setHandledAt(LocalDateTime.now());
        alert.setHandlingResult("已忽略");
        alert.setHandlingRemark(reason);
        alert.setUpdatedBy(operatorId);
        alert.setUpdatedAt(LocalDateTime.now());

        inventoryAlertRepository.updateById(alert);
        log.info("库存预警已忽略，ID: {}, 原因: {}", id, reason);
        
        return true;
    }

    @Override
    @Transactional
    public void autoDetectAndCreateAlerts() {
        // TODO: 实现自动检测库存预警的逻辑
        // 1. 检测库存不足的商品
        // 2. 检测库存过高的商品
        // 3. 检测即将过期的商品
        // 4. 检测损坏库存
        log.info("自动检测库存预警功能待实现");
    }

    @Override
    public List<InventoryAlert> getAlertsByType(String alertType) {
        return inventoryAlertRepository.findByAlertType(alertType);
    }

    @Override
    public List<InventoryAlert> getAlertsByLevel(String alertLevel) {
        return inventoryAlertRepository.findByAlertLevel(alertLevel);
    }

    /**
     * 生成预警编号
     */
    private String generateAlertNo() {
        return "ALT" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
