package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.dto.WarehouseRequest;
import com.yibang.erp.domain.entity.Warehouse;
import com.yibang.erp.infrastructure.repository.WarehouseRepository;
import com.yibang.erp.domain.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 仓库管理服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public Warehouse createWarehouse(WarehouseRequest request, Long operatorId) {
        // 检查仓库编码是否已存在
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("warehouse_code", request.getWarehouseCode())
                   .eq("company_id", request.getCompanyId())
                   .eq("deleted", false);
        
        if (warehouseRepository.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("仓库编码已存在");
        }

        Warehouse warehouse = new Warehouse();
        BeanUtils.copyProperties(request, warehouse);
        warehouse.setCreatedBy(operatorId);
        warehouse.setUpdatedBy(operatorId);
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setUpdatedAt(LocalDateTime.now());
        warehouse.setDeleted(false);

        warehouseRepository.insert(warehouse);
        log.info("仓库创建成功，ID: {}, 编码: {}", warehouse.getId(), warehouse.getWarehouseCode());
        
        return warehouse;
    }

    @Override
    @Transactional
    public Warehouse updateWarehouse(Long id, WarehouseRequest request, Long operatorId) {
        Warehouse existingWarehouse = warehouseRepository.selectById(id);
        if (existingWarehouse == null || existingWarehouse.getDeleted()) {
            throw new RuntimeException("仓库不存在");
        }

        // 检查仓库编码是否已被其他仓库使用
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("warehouse_code", request.getWarehouseCode())
                   .eq("company_id", request.getCompanyId())
                   .ne("id", id)
                   .eq("deleted", false);
        
        if (warehouseRepository.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("仓库编码已被其他仓库使用");
        }

        BeanUtils.copyProperties(request, existingWarehouse);
        existingWarehouse.setUpdatedBy(operatorId);
        existingWarehouse.setUpdatedAt(LocalDateTime.now());

        warehouseRepository.updateById(existingWarehouse);
        log.info("仓库更新成功，ID: {}", id);
        
        return existingWarehouse;
    }

    @Override
    @Transactional
    public boolean deleteWarehouse(Long id, Long operatorId) {
        Warehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null || warehouse.getDeleted()) {
            return false;
        }

        // 逻辑删除
        warehouse.setDeleted(true);
        warehouse.setUpdatedBy(operatorId);
        warehouse.setUpdatedAt(LocalDateTime.now());

        warehouseRepository.updateById(warehouse);
        log.info("仓库删除成功，ID: {}", id);
        
        return true;
    }

    @Override
    public Warehouse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null || warehouse.getDeleted()) {
            throw new RuntimeException("仓库不存在");
        }
        return warehouse;
    }

    @Override
    public Page<Warehouse> getWarehousePage(int page, int size, String warehouseCode, String warehouseName,
                                           String warehouseType, String status, Long companyId) {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);

        if (StringUtils.hasText(warehouseCode)) {
            queryWrapper.like("warehouse_code", warehouseCode);
        }
        if (StringUtils.hasText(warehouseName)) {
            queryWrapper.like("warehouse_name", warehouseName);
        }
        if (StringUtils.hasText(warehouseType)) {
            queryWrapper.eq("warehouse_type", warehouseType);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (companyId != null) {
            queryWrapper.eq("company_id", companyId);
        }

        queryWrapper.orderByDesc("created_at");

        Page<Warehouse> pageParam = new Page<>(page, size);
        return warehouseRepository.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<Warehouse> getWarehousesByCompanyId(Long companyId) {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("company_id", companyId)
                   .eq("deleted", false)
                   .eq("status", "ACTIVE")
                   .orderByAsc("warehouse_name");
        
        return warehouseRepository.selectList(queryWrapper);
    }

    @Override
    public List<Warehouse> getWarehousesByStatus(String status) {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", status)
                   .eq("deleted", false)
                   .orderByAsc("warehouse_name");
        
        return warehouseRepository.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public boolean activateWarehouse(Long id, Long operatorId) {
        Warehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null || warehouse.getDeleted()) {
            return false;
        }

        warehouse.setStatus("ACTIVE");
        warehouse.setUpdatedBy(operatorId);
        warehouse.setUpdatedAt(LocalDateTime.now());

        warehouseRepository.updateById(warehouse);
        log.info("仓库启用成功，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean deactivateWarehouse(Long id, Long operatorId) {
        Warehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null || warehouse.getDeleted()) {
            return false;
        }

        warehouse.setStatus("INACTIVE");
        warehouse.setUpdatedBy(operatorId);
        warehouse.setUpdatedAt(LocalDateTime.now());

        warehouseRepository.updateById(warehouse);
        log.info("仓库停用成功，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean maintainWarehouse(Long id, Long operatorId) {
        Warehouse warehouse = warehouseRepository.selectById(id);
        if (warehouse == null || warehouse.getDeleted()) {
            return false;
        }

        warehouse.setStatus("MAINTENANCE");
        warehouse.setUpdatedBy(operatorId);
        warehouse.setUpdatedAt(LocalDateTime.now());

        warehouseRepository.updateById(warehouse);
        log.info("仓库设置为维护状态，ID: {}", id);
        
        return true;
    }
}
