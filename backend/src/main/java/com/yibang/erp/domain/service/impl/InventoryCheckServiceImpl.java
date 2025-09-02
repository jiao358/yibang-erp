package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.dto.InventoryCheckRequest;
import com.yibang.erp.domain.entity.InventoryCheck;
import com.yibang.erp.domain.entity.InventoryCheckItem;
import com.yibang.erp.infrastructure.repository.InventoryCheckRepository;
import com.yibang.erp.infrastructure.repository.InventoryCheckItemRepository;
import com.yibang.erp.domain.service.InventoryCheckService;
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
 * 库存盘点服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryCheckServiceImpl implements InventoryCheckService {

    private final InventoryCheckRepository inventoryCheckRepository;
    private final InventoryCheckItemRepository inventoryCheckItemRepository;

    @Override
    @Transactional
    public InventoryCheck createCheck(InventoryCheckRequest request, Long operatorId) {
        InventoryCheck check = new InventoryCheck();
        BeanUtils.copyProperties(request, check);
        check.setCheckNo(generateCheckNo());
        check.setStatus("PLANNED");
        check.setReviewStatus("PENDING");
        check.setCreatedBy(operatorId);
        check.setUpdatedBy(operatorId);
        check.setCreatedAt(LocalDateTime.now());
        check.setUpdatedAt(LocalDateTime.now());
        check.setDeleted(false);

        inventoryCheckRepository.insert(check);
        log.info("库存盘点创建成功，ID: {}, 单号: {}", check.getId(), check.getCheckNo());
        
        return check;
    }

    @Override
    @Transactional
    public InventoryCheck updateCheck(Long id, InventoryCheckRequest request, Long operatorId) {
        InventoryCheck existingCheck = inventoryCheckRepository.selectById(id);
        if (existingCheck == null || existingCheck.getDeleted()) {
            throw new RuntimeException("库存盘点不存在");
        }

        // 只有已计划的盘点可以修改
        if (!"PLANNED".equals(existingCheck.getStatus())) {
            throw new RuntimeException("只有已计划的盘点可以修改");
        }

        BeanUtils.copyProperties(request, existingCheck);
        existingCheck.setUpdatedBy(operatorId);
        existingCheck.setUpdatedAt(LocalDateTime.now());

        inventoryCheckRepository.updateById(existingCheck);
        log.info("库存盘点更新成功，ID: {}", id);
        
        return existingCheck;
    }

    @Override
    @Transactional
    public boolean deleteCheck(Long id, Long operatorId) {
        InventoryCheck check = inventoryCheckRepository.selectById(id);
        if (check == null || check.getDeleted()) {
            return false;
        }

        // 只有已计划的盘点可以删除
        if (!"PLANNED".equals(check.getStatus())) {
            throw new RuntimeException("只有已计划的盘点可以删除");
        }

        check.setDeleted(true);
        check.setUpdatedBy(operatorId);
        check.setUpdatedAt(LocalDateTime.now());

        inventoryCheckRepository.updateById(check);
        log.info("库存盘点删除成功，ID: {}", id);
        
        return true;
    }

    @Override
    public InventoryCheck getCheckById(Long id) {
        InventoryCheck check = inventoryCheckRepository.selectById(id);
        if (check == null || check.getDeleted()) {
            throw new RuntimeException("库存盘点不存在");
        }
        return check;
    }

    @Override
    public Page<InventoryCheck> getCheckPage(int page, int size, String checkType, String status,
                                            Long warehouseId, String startTime, String endTime) {
        QueryWrapper<InventoryCheck> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", false);

        if (StringUtils.hasText(checkType)) {
            queryWrapper.eq("check_type", checkType);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq("status", status);
        }
        if (warehouseId != null) {
            queryWrapper.eq("warehouse_id", warehouseId);
        }
        if (StringUtils.hasText(startTime) && StringUtils.hasText(endTime)) {
            queryWrapper.between("planned_start_time", startTime, endTime);
        }

        queryWrapper.orderByDesc("created_at");

        // 获取总记录数
        long total = inventoryCheckRepository.selectCount(queryWrapper);
        
        Page<InventoryCheck> pageParam = new Page<>(page, size);
        
        if (total == 0) {
            pageParam.setRecords(List.of());
            pageParam.setTotal(0);
            pageParam.setCurrent(page);
            pageParam.setSize(size);
            pageParam.setPages(0);
            return pageParam;
        }
        
        // 添加分页条件（手动实现分页）
        int offset = (page - 1) * size;
        queryWrapper.last(String.format("LIMIT %d, %d", offset, size));
        
        // 查询盘点数据
        List<InventoryCheck> checks = inventoryCheckRepository.selectList(queryWrapper);
        
        // 设置分页结果
        pageParam.setRecords(checks);
        pageParam.setTotal(total);
        pageParam.setCurrent(page);
        pageParam.setSize(size);
        pageParam.setPages((total + size - 1) / size);
        
        return pageParam;
    }

    @Override
    public List<InventoryCheck> getChecksByStatus(String status) {
        return inventoryCheckRepository.findByStatus(status);
    }

    @Override
    public List<InventoryCheck> getChecksByWarehouseId(Long warehouseId) {
        return inventoryCheckRepository.findByWarehouseId(warehouseId);
    }

    @Override
    public List<InventoryCheck> getChecksByType(String checkType) {
        return inventoryCheckRepository.findByCheckType(checkType);
    }

    @Override
    public List<InventoryCheck> getPendingReview() {
        return inventoryCheckRepository.findPendingReview();
    }

    @Override
    @Transactional
    public boolean startCheck(Long id, Long operatorId) {
        InventoryCheck check = inventoryCheckRepository.selectById(id);
        if (check == null || check.getDeleted()) {
            return false;
        }

        if (!"PLANNED".equals(check.getStatus())) {
            throw new RuntimeException("只有已计划的盘点可以开始");
        }

        check.setStatus("IN_PROGRESS");
        check.setActualStartTime(LocalDateTime.now());
        check.setUpdatedBy(operatorId);
        check.setUpdatedAt(LocalDateTime.now());

        inventoryCheckRepository.updateById(check);
        log.info("库存盘点开始，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean completeCheck(Long id, Long operatorId) {
        InventoryCheck check = inventoryCheckRepository.selectById(id);
        if (check == null || check.getDeleted()) {
            return false;
        }

        if (!"IN_PROGRESS".equals(check.getStatus())) {
            throw new RuntimeException("只有进行中的盘点可以完成");
        }

        check.setStatus("COMPLETED");
        check.setActualEndTime(LocalDateTime.now());
        check.setUpdatedBy(operatorId);
        check.setUpdatedAt(LocalDateTime.now());

        inventoryCheckRepository.updateById(check);
        log.info("库存盘点完成，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean cancelCheck(Long id, Long operatorId) {
        InventoryCheck check = inventoryCheckRepository.selectById(id);
        if (check == null || check.getDeleted()) {
            return false;
        }

        if ("COMPLETED".equals(check.getStatus())) {
            throw new RuntimeException("已完成的盘点不能取消");
        }

        check.setStatus("CANCELLED");
        check.setUpdatedBy(operatorId);
        check.setUpdatedAt(LocalDateTime.now());

        inventoryCheckRepository.updateById(check);
        log.info("库存盘点取消，ID: {}", id);
        
        return true;
    }

    @Override
    @Transactional
    public boolean reviewCheck(Long id, String reviewStatus, String reviewComment, Long reviewerId) {
        InventoryCheck check = inventoryCheckRepository.selectById(id);
        if (check == null || check.getDeleted()) {
            return false;
        }

        if (!"COMPLETED".equals(check.getStatus())) {
            throw new RuntimeException("只有已完成的盘点可以审核");
        }

        check.setReviewStatus(reviewStatus);
        check.setReviewComment(reviewComment);
        check.setReviewerId(reviewerId);
        check.setReviewedAt(LocalDateTime.now());
        check.setUpdatedBy(reviewerId);
        check.setUpdatedAt(LocalDateTime.now());

        inventoryCheckRepository.updateById(check);
        log.info("库存盘点审核完成，ID: {}, 状态: {}", id, reviewStatus);
        
        return true;
    }

    @Override
    public List<InventoryCheckItem> getCheckItems(Long checkId) {
        return inventoryCheckItemRepository.findByCheckId(checkId);
    }

    @Override
    public List<InventoryCheckItem> getItemsWithDifference() {
        return inventoryCheckItemRepository.findItemsWithDifference();
    }

    @Override
    @Transactional
    public boolean addCheckItem(Long checkId, InventoryCheckItem item, Long operatorId) {
        InventoryCheck check = inventoryCheckRepository.selectById(checkId);
        if (check == null || check.getDeleted()) {
            return false;
        }

        if (!"IN_PROGRESS".equals(check.getStatus())) {
            throw new RuntimeException("只有进行中的盘点可以添加明细");
        }

        item.setCheckId(checkId);
        item.setStatus("PENDING");
        item.setCreatedBy(operatorId);
        item.setUpdatedBy(operatorId);
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        item.setDeleted(false);

        inventoryCheckItemRepository.insert(item);
        log.info("盘点明细添加成功，盘点ID: {}, 商品ID: {}", checkId, item.getProductId());
        
        return true;
    }

    @Override
    @Transactional
    public boolean updateCheckItem(Long itemId, InventoryCheckItem item, Long operatorId) {
        InventoryCheckItem existingItem = inventoryCheckItemRepository.selectById(itemId);
        if (existingItem == null || existingItem.getDeleted()) {
            return false;
        }

        // 只有待盘点的明细可以修改
        if (!"PENDING".equals(existingItem.getStatus())) {
            throw new RuntimeException("只有待盘点的明细可以修改");
        }

        item.setId(itemId);
        item.setUpdatedBy(operatorId);
        item.setUpdatedAt(LocalDateTime.now());

        inventoryCheckItemRepository.updateById(item);
        log.info("盘点明细更新成功，ID: {}", itemId);
        
        return true;
    }

    @Override
    @Transactional
    public boolean verifyCheckItem(Long itemId, Long verifierId) {
        InventoryCheckItem item = inventoryCheckItemRepository.selectById(itemId);
        if (item == null || item.getDeleted()) {
            return false;
        }

        if (!"CHECKED".equals(item.getStatus())) {
            throw new RuntimeException("只有已盘点的明细可以验证");
        }

        item.setStatus("VERIFIED");
        item.setVerifierId(verifierId);
        item.setVerifiedAt(LocalDateTime.now());
        item.setUpdatedBy(verifierId);
        item.setUpdatedAt(LocalDateTime.now());

        inventoryCheckItemRepository.updateById(item);
        log.info("盘点明细验证完成，ID: {}", itemId);
        
        return true;
    }

    /**
     * 生成盘点单号
     */
    private String generateCheckNo() {
        return "CHK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
