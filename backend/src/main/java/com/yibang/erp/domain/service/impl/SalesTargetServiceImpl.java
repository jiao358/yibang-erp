package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.SalesTargetRequest;
import com.yibang.erp.domain.dto.SalesTargetResponse;
import com.yibang.erp.domain.dto.SalesTargetQueryRequest;
import com.yibang.erp.domain.entity.SalesTarget;
import com.yibang.erp.domain.enums.SalesTargetType;
import com.yibang.erp.domain.service.SalesTargetService;
import com.yibang.erp.infrastructure.repository.SalesTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 销售目标Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesTargetServiceImpl implements SalesTargetService {

    private final SalesTargetRepository salesTargetRepository;

    @Override
    @Transactional
    public SalesTargetResponse createSalesTarget(SalesTargetRequest request) {
        // 验证配置
        if (!validateSalesTargetConfig(request)) {
            throw new IllegalArgumentException("销售目标配置无效");
        }

        // 检查目标代码是否已存在
        if (isTargetCodeExists(request.getTargetCode(), null, request.getCompanyId())) {
            throw new IllegalArgumentException("目标代码已存在");
        }

        SalesTarget salesTarget = new SalesTarget();
        BeanUtils.copyProperties(request, salesTarget);
        
        // 设置默认值
        salesTarget.setCurrentValue(BigDecimal.ZERO);
        salesTarget.setCompletionRate(BigDecimal.ZERO);
        salesTarget.setCreatedAt(LocalDateTime.now());
        salesTarget.setUpdatedAt(LocalDateTime.now());
        salesTarget.setIsDeleted(false);

        salesTargetRepository.insert(salesTarget);
        
        return getSalesTargetById(salesTarget.getId());
    }

    @Override
    @Transactional
    public SalesTargetResponse updateSalesTarget(Long id, SalesTargetRequest request) {
        SalesTarget existingTarget = salesTargetRepository.selectById(id);
        if (existingTarget == null) {
            throw new IllegalArgumentException("销售目标不存在");
        }

        // 验证配置
        if (!validateSalesTargetConfig(request)) {
            throw new IllegalArgumentException("销售目标配置无效");
        }

        // 检查目标代码是否已存在（排除当前记录）
        if (isTargetCodeExists(request.getTargetCode(), id, request.getCompanyId())) {
            throw new IllegalArgumentException("目标代码已存在");
        }

        BeanUtils.copyProperties(request, existingTarget);
        existingTarget.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算完成率
        if (existingTarget.getCurrentValue() != null && existingTarget.getTargetValue() != null) {
            BigDecimal completionRate = existingTarget.getCurrentValue()
                    .divide(existingTarget.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            existingTarget.setCompletionRate(completionRate);
        }
        
        salesTargetRepository.updateById(existingTarget);
        
        return getSalesTargetById(id);
    }

    @Override
    @Transactional
    public boolean deleteSalesTarget(Long id) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        return salesTargetRepository.deleteById(id) > 0;
    }

    @Override
    public SalesTargetResponse getSalesTargetById(Long id) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return null;
        }
        
        return convertToResponse(salesTarget);
    }

    @Override
    public PageResult<SalesTargetResponse> getSalesTargetPage(SalesTargetQueryRequest request) {
        Page<SalesTarget> page = new Page<>(request.getPage(), request.getSize());
        
        LambdaQueryWrapper<SalesTarget> wrapper = buildQueryWrapper(request);
        IPage<SalesTarget> result = salesTargetRepository.selectPage(page, wrapper);
        
        List<SalesTargetResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<SalesTargetResponse> getSalesTargetList(SalesTargetQueryRequest request) {
        LambdaQueryWrapper<SalesTarget> wrapper = buildQueryWrapper(request);
        List<SalesTarget> salesTargets = salesTargetRepository.selectList(wrapper);
        
        return salesTargets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean toggleSalesTargetStatus(Long id, Boolean isActive) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        salesTarget.setIsActive(isActive);
        salesTarget.setUpdatedAt(LocalDateTime.now());
        
        return salesTargetRepository.updateById(salesTarget) > 0;
    }

    @Override
    @Transactional
    public boolean batchToggleSalesTargetStatus(List<Long> ids, Boolean isActive) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<SalesTarget> salesTargets = salesTargetRepository.selectBatchIds(ids);
        if (salesTargets.isEmpty()) {
            return false;
        }
        
        salesTargets.forEach(target -> {
            target.setIsActive(isActive);
            target.setUpdatedAt(LocalDateTime.now());
        });
        
        // 逐个更新
        int updateCount = 0;
        for (SalesTarget target : salesTargets) {
            if (salesTargetRepository.updateById(target) > 0) {
                updateCount++;
            }
        }
        return updateCount == salesTargets.size();
    }

    @Override
    @Transactional
    public boolean updateTargetProgress(Long id, BigDecimal currentValue) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        salesTarget.setCurrentValue(currentValue);
        
        // 计算完成率
        if (salesTarget.getTargetValue() != null && salesTarget.getTargetValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal completionRate = currentValue
                    .divide(salesTarget.getTargetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            salesTarget.setCompletionRate(completionRate);
            
            // 检查是否完成
            if (completionRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
                salesTarget.setStatus("COMPLETED");
            }
        }
        
        salesTarget.setUpdatedAt(LocalDateTime.now());
        
        return salesTargetRepository.updateById(salesTarget) > 0;
    }

    @Override
    @Transactional
    public boolean batchUpdateTargetProgress(List<Long> ids, List<BigDecimal> currentValues) {
        if (ids.size() != currentValues.size()) {
            throw new IllegalArgumentException("目标ID列表和当前值列表长度不匹配");
        }
        
        boolean allSuccess = true;
        for (int i = 0; i < ids.size(); i++) {
            if (!updateTargetProgress(ids.get(i), currentValues.get(i))) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }

    @Override
    @Transactional
    public boolean activateSalesTarget(Long id) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        salesTarget.setStatus("ACTIVE");
        salesTarget.setUpdatedAt(LocalDateTime.now());
        
        return salesTargetRepository.updateById(salesTarget) > 0;
    }

    @Override
    @Transactional
    public boolean completeSalesTarget(Long id) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        salesTarget.setStatus("COMPLETED");
        salesTarget.setUpdatedAt(LocalDateTime.now());
        
        return salesTargetRepository.updateById(salesTarget) > 0;
    }

    @Override
    @Transactional
    public boolean cancelSalesTarget(Long id) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        salesTarget.setStatus("CANCELLED");
        salesTarget.setUpdatedAt(LocalDateTime.now());
        
        return salesTargetRepository.updateById(salesTarget) > 0;
    }

    @Override
    @Transactional
    public boolean resetSalesTarget(Long id) {
        SalesTarget salesTarget = salesTargetRepository.selectById(id);
        if (salesTarget == null) {
            return false;
        }
        
        salesTarget.setStatus("DRAFT");
        salesTarget.setCurrentValue(BigDecimal.ZERO);
        salesTarget.setCompletionRate(BigDecimal.ZERO);
        salesTarget.setUpdatedAt(LocalDateTime.now());
        
        return salesTargetRepository.updateById(salesTarget) > 0;
    }

    @Override
    public List<SalesTargetResponse> getApplicableSalesTargets(Long categoryId, String customerType, Long companyId) {
        LambdaQueryWrapper<SalesTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesTarget::getCompanyId, companyId)
                .eq(SalesTarget::getIsActive, true)
                .eq(SalesTarget::getStatus, "ACTIVE")
                .and(w -> w.eq(SalesTarget::getCategoryId, categoryId).or().isNull(SalesTarget::getCategoryId))
                .and(w -> w.eq(SalesTarget::getCustomerType, customerType).or().isNull(SalesTarget::getCustomerType))
                .orderByAsc(SalesTarget::getPriority);
        
        List<SalesTarget> salesTargets = salesTargetRepository.selectList(wrapper);
        
        return salesTargets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Object getTargetCompletionStatistics(Long companyId) {
        LambdaQueryWrapper<SalesTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesTarget::getCompanyId, companyId)
                .eq(SalesTarget::getIsDeleted, false);
        
        List<SalesTarget> targets = salesTargetRepository.selectList(wrapper);
        
        long totalCount = targets.size();
        long completedCount = targets.stream()
                .filter(target -> "COMPLETED".equals(target.getStatus()))
                .count();
        long activeCount = targets.stream()
                .filter(target -> "ACTIVE".equals(target.getStatus()))
                .count();
        long draftCount = targets.stream()
                .filter(target -> "DRAFT".equals(target.getStatus()))
                .count();
        long cancelledCount = targets.stream()
                .filter(target -> "CANCELLED".equals(target.getStatus()))
                .count();
        
        return Map.of(
                "totalCount", totalCount,
                "completedCount", completedCount,
                "activeCount", activeCount,
                "draftCount", draftCount,
                "cancelledCount", cancelledCount,
                "completionRate", totalCount > 0 ? (double) completedCount / totalCount * 100 : 0.0
        );
    }

    @Override
    public Object getTargetTypeStatistics(Long companyId) {
        LambdaQueryWrapper<SalesTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesTarget::getCompanyId, companyId)
                .eq(SalesTarget::getIsDeleted, false);
        
        List<SalesTarget> targets = salesTargetRepository.selectList(wrapper);
        
        Map<String, Long> typeCount = targets.stream()
                .collect(Collectors.groupingBy(
                        SalesTarget::getTargetType,
                        Collectors.counting()
                ));
        
        Map<String, Long> statusCount = targets.stream()
                .collect(Collectors.groupingBy(
                        SalesTarget::getStatus,
                        Collectors.counting()
                ));
        
        return Map.of(
                "totalCount", (long) targets.size(),
                "typeDistribution", typeCount,
                "statusDistribution", statusCount
        );
    }

    @Override
    public boolean validateSalesTargetConfig(SalesTargetRequest request) {
        // 验证时间范围
        if (request.getPeriodStart() != null && request.getPeriodEnd() != null) {
            if (request.getPeriodStart().isAfter(request.getPeriodEnd())) {
                return false;
            }
        }
        
        // 验证目标值
        if (request.getTargetValue() != null && request.getTargetValue().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        return true;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<SalesTarget> buildQueryWrapper(SalesTargetQueryRequest request) {
        LambdaQueryWrapper<SalesTarget> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(SalesTarget::getCompanyId, request.getCompanyId())
                .eq(SalesTarget::getIsDeleted, false);
        
        if (StringUtils.hasText(request.getTargetName())) {
            wrapper.like(SalesTarget::getTargetName, request.getTargetName());
        }
        
        if (StringUtils.hasText(request.getTargetCode())) {
            wrapper.eq(SalesTarget::getTargetCode, request.getTargetCode());
        }
        
        if (StringUtils.hasText(request.getTargetType())) {
            wrapper.eq(SalesTarget::getTargetType, request.getTargetType());
        }
        
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(SalesTarget::getStatus, request.getStatus());
        }
        
        if (request.getIsActive() != null) {
            wrapper.eq(SalesTarget::getIsActive, request.getIsActive());
        }
        
        if (request.getCategoryId() != null) {
            wrapper.eq(SalesTarget::getCategoryId, request.getCategoryId());
        }
        
        if (StringUtils.hasText(request.getCustomerType())) {
            wrapper.eq(SalesTarget::getCustomerType, request.getCustomerType());
        }
        
        if (request.getOwnerId() != null) {
            wrapper.eq(SalesTarget::getOwnerId, request.getOwnerId());
        }
        
        // 目标值范围查询
        if (request.getTargetValueFrom() != null) {
            wrapper.ge(SalesTarget::getTargetValue, request.getTargetValueFrom());
        }
        if (request.getTargetValueTo() != null) {
            wrapper.le(SalesTarget::getTargetValue, request.getTargetValueTo());
        }
        
        // 完成率范围查询
        if (request.getCompletionRateFrom() != null) {
            wrapper.ge(SalesTarget::getCompletionRate, request.getCompletionRateFrom());
        }
        if (request.getCompletionRateTo() != null) {
            wrapper.le(SalesTarget::getCompletionRate, request.getCompletionRateTo());
        }
        
        // 优先级范围查询
        if (request.getPriorityFrom() != null) {
            wrapper.ge(SalesTarget::getPriority, request.getPriorityFrom());
        }
        if (request.getPriorityTo() != null) {
            wrapper.le(SalesTarget::getPriority, request.getPriorityTo());
        }
        
        // 时间范围查询
        if (request.getPeriodStartFrom() != null) {
            wrapper.ge(SalesTarget::getPeriodStart, request.getPeriodStartFrom());
        }
        if (request.getPeriodStartTo() != null) {
            wrapper.le(SalesTarget::getPeriodStart, request.getPeriodStartTo());
        }
        
        if (request.getPeriodEndFrom() != null) {
            wrapper.ge(SalesTarget::getPeriodEnd, request.getPeriodEndFrom());
        }
        if (request.getPeriodEndTo() != null) {
            wrapper.le(SalesTarget::getPeriodEnd, request.getPeriodEndTo());
        }
        
        if (request.getCreatedAtFrom() != null) {
            wrapper.ge(SalesTarget::getCreatedAt, request.getCreatedAtFrom());
        }
        if (request.getCreatedAtTo() != null) {
            wrapper.le(SalesTarget::getCreatedAt, request.getCreatedAtTo());
        }
        
        wrapper.orderByAsc(SalesTarget::getPriority)
                .orderByDesc(SalesTarget::getCreatedAt);
        
        return wrapper;
    }

    /**
     * 检查目标代码是否已存在
     */
    private boolean isTargetCodeExists(String targetCode, Long excludeId, Long companyId) {
        LambdaQueryWrapper<SalesTarget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SalesTarget::getTargetCode, targetCode)
                .eq(SalesTarget::getCompanyId, companyId)
                .eq(SalesTarget::getIsDeleted, false);
        
        if (excludeId != null) {
            wrapper.ne(SalesTarget::getId, excludeId);
        }
        
        return salesTargetRepository.selectCount(wrapper) > 0;
    }

    /**
     * 转换为响应DTO
     */
    private SalesTargetResponse convertToResponse(SalesTarget salesTarget) {
        SalesTargetResponse response = new SalesTargetResponse();
        BeanUtils.copyProperties(salesTarget, response);
        
        // 设置目标类型描述
        try {
            SalesTargetType targetType = SalesTargetType.fromCode(salesTarget.getTargetType());
            response.setTargetTypeDescription(targetType.getDescription());
        } catch (Exception e) {
            response.setTargetTypeDescription("未知类型");
        }
        
        // 设置周期描述
        response.setPeriodDescription(getPeriodDescription(salesTarget.getPeriod()));
        
        // 设置状态描述
        response.setStatusDescription(getStatusDescription(salesTarget.getStatus()));
        
        return response;
    }

    /**
     * 获取周期描述
     */
    private String getPeriodDescription(String period) {
        return switch (period) {
            case "DAILY" -> "日";
            case "WEEKLY" -> "周";
            case "MONTHLY" -> "月";
            case "QUARTERLY" -> "季";
            case "YEARLY" -> "年";
            default -> "未知";
        };
    }

    /**
     * 获取状态描述
     */
    private String getStatusDescription(String status) {
        return switch (status) {
            case "DRAFT" -> "草稿";
            case "ACTIVE" -> "激活";
            case "COMPLETED" -> "完成";
            case "CANCELLED" -> "取消";
            default -> "未知";
        };
    }
}
