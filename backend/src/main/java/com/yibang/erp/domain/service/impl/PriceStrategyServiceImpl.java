package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.domain.dto.PriceStrategyRequest;
import com.yibang.erp.domain.dto.PriceStrategyResponse;
import com.yibang.erp.domain.dto.PriceStrategyQueryRequest;
import com.yibang.erp.domain.entity.PriceStrategy;
import com.yibang.erp.domain.enums.PriceStrategyType;
import com.yibang.erp.domain.service.PriceStrategyService;
import com.yibang.erp.infrastructure.repository.PriceStrategyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 价格策略Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceStrategyServiceImpl implements PriceStrategyService {

    private final PriceStrategyRepository priceStrategyRepository;

    @Override
    @Transactional
    public PriceStrategyResponse createPriceStrategy(PriceStrategyRequest request) {
        // 验证配置
        if (!validatePriceStrategyConfig(request)) {
            throw new IllegalArgumentException("价格策略配置无效");
        }

        // 检查策略代码是否已存在
        if (isStrategyCodeExists(request.getStrategyCode(), null, request.getCompanyId())) {
            throw new IllegalArgumentException("策略代码已存在");
        }

        PriceStrategy priceStrategy = new PriceStrategy();
        BeanUtils.copyProperties(request, priceStrategy);
        
        // 设置默认值
        priceStrategy.setCreatedAt(LocalDateTime.now());
        priceStrategy.setUpdatedAt(LocalDateTime.now());
        priceStrategy.setIsDeleted(false);

        priceStrategyRepository.insert(priceStrategy);
        
        return getPriceStrategyById(priceStrategy.getId());
    }

    @Override
    @Transactional
    public PriceStrategyResponse updatePriceStrategy(Long id, PriceStrategyRequest request) {
        PriceStrategy existingStrategy = priceStrategyRepository.selectById(id);
        if (existingStrategy == null) {
            throw new IllegalArgumentException("价格策略不存在");
        }

        // 验证配置
        if (!validatePriceStrategyConfig(request)) {
            throw new IllegalArgumentException("价格策略配置无效");
        }

        // 检查策略代码是否已存在（排除当前记录）
        if (isStrategyCodeExists(request.getStrategyCode(), id, request.getCompanyId())) {
            throw new IllegalArgumentException("策略代码已存在");
        }

        BeanUtils.copyProperties(request, existingStrategy);
        existingStrategy.setUpdatedAt(LocalDateTime.now());
        
        priceStrategyRepository.updateById(existingStrategy);
        
        return getPriceStrategyById(id);
    }

    @Override
    @Transactional
    public boolean deletePriceStrategy(Long id) {
        PriceStrategy priceStrategy = priceStrategyRepository.selectById(id);
        if (priceStrategy == null) {
            return false;
        }
        
        return priceStrategyRepository.deleteById(id) > 0;
    }

    @Override
    public PriceStrategyResponse getPriceStrategyById(Long id) {
        PriceStrategy priceStrategy = priceStrategyRepository.selectById(id);
        if (priceStrategy == null) {
            return null;
        }
        
        return convertToResponse(priceStrategy);
    }

    @Override
    public PageResult<PriceStrategyResponse> getPriceStrategyPage(PriceStrategyQueryRequest request) {
        Page<PriceStrategy> page = new Page<>(request.getPage(), request.getSize());
        
        LambdaQueryWrapper<PriceStrategy> wrapper = buildQueryWrapper(request);
        IPage<PriceStrategy> result = priceStrategyRepository.selectPage(page, wrapper);
        
        List<PriceStrategyResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<PriceStrategyResponse> getPriceStrategyList(PriceStrategyQueryRequest request) {
        LambdaQueryWrapper<PriceStrategy> wrapper = buildQueryWrapper(request);
        List<PriceStrategy> priceStrategies = priceStrategyRepository.selectList(wrapper);
        
        return priceStrategies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean togglePriceStrategyStatus(Long id, Boolean isActive) {
        PriceStrategy priceStrategy = priceStrategyRepository.selectById(id);
        if (priceStrategy == null) {
            return false;
        }
        
        priceStrategy.setIsActive(isActive);
        priceStrategy.setUpdatedAt(LocalDateTime.now());
        
        return priceStrategyRepository.updateById(priceStrategy) > 0;
    }

    @Override
    @Transactional
    public boolean batchTogglePriceStrategyStatus(List<Long> ids, Boolean isActive) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<PriceStrategy> priceStrategies = priceStrategyRepository.selectBatchIds(ids);
        if (priceStrategies.isEmpty()) {
            return false;
        }
        
        priceStrategies.forEach(strategy -> {
            strategy.setIsActive(isActive);
            strategy.setUpdatedAt(LocalDateTime.now());
        });
        
        // 逐个更新
        int updateCount = 0;
        for (PriceStrategy strategy : priceStrategies) {
            if (priceStrategyRepository.updateById(strategy) > 0) {
                updateCount++;
            }
        }
        return updateCount == priceStrategies.size();
    }

    @Override
    public List<PriceStrategyResponse> getApplicablePriceStrategies(Long categoryId, String customerType, Long companyId) {
        LambdaQueryWrapper<PriceStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceStrategy::getCompanyId, companyId)
                .eq(PriceStrategy::getIsActive, true)
                .and(w -> w.eq(PriceStrategy::getCategoryId, categoryId).or().isNull(PriceStrategy::getCategoryId))
                .and(w -> w.eq(PriceStrategy::getCustomerType, customerType).or().isNull(PriceStrategy::getCustomerType))
                .orderByAsc(PriceStrategy::getPriority);
        
        List<PriceStrategy> priceStrategies = priceStrategyRepository.selectList(wrapper);
        
        return priceStrategies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal applyPriceStrategy(Long strategyId, BigDecimal originalPrice) {
        PriceStrategy priceStrategy = priceStrategyRepository.selectById(strategyId);
        if (priceStrategy == null || !priceStrategy.getIsActive()) {
            return originalPrice;
        }
        
        BigDecimal finalPrice = originalPrice;
        
        switch (priceStrategy.getStrategyType()) {
            case "DISCOUNT":
                if (priceStrategy.getAdjustmentRate() != null) {
                    finalPrice = finalPrice.multiply(BigDecimal.ONE.subtract(priceStrategy.getAdjustmentRate()));
                }
                break;
            case "MARKUP":
                if (priceStrategy.getAdjustmentRate() != null) {
                    finalPrice = finalPrice.multiply(BigDecimal.ONE.add(priceStrategy.getAdjustmentRate()));
                }
                break;
            case "FIXED":
                if (priceStrategy.getBasePrice() != null) {
                    finalPrice = priceStrategy.getBasePrice();
                }
                break;
            case "DYNAMIC":
                if (priceStrategy.getAdjustmentAmount() != null) {
                    finalPrice = finalPrice.add(priceStrategy.getAdjustmentAmount());
                }
                break;
        }
        
        // 应用价格限制
        if (priceStrategy.getMinPrice() != null && finalPrice.compareTo(priceStrategy.getMinPrice()) < 0) {
            finalPrice = priceStrategy.getMinPrice();
        }
        
        if (priceStrategy.getMaxPrice() != null && finalPrice.compareTo(priceStrategy.getMaxPrice()) > 0) {
            finalPrice = priceStrategy.getMaxPrice();
        }
        
        return finalPrice.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<BigDecimal> batchApplyPriceStrategies(List<Long> strategyIds, List<BigDecimal> originalPrices) {
        if (strategyIds.size() != originalPrices.size()) {
            throw new IllegalArgumentException("策略ID列表和原始价格列表长度不匹配");
        }
        
        List<BigDecimal> finalPrices = new ArrayList<>();
        for (int i = 0; i < strategyIds.size(); i++) {
            BigDecimal finalPrice = applyPriceStrategy(strategyIds.get(i), originalPrices.get(i));
            finalPrices.add(finalPrice);
        }
        
        return finalPrices;
    }

    @Override
    public boolean validatePriceStrategyConfig(PriceStrategyRequest request) {
        // 验证价格范围
        if (request.getMinPrice() != null && request.getMaxPrice() != null) {
            if (request.getMinPrice().compareTo(request.getMaxPrice()) >= 0) {
                return false;
            }
        }
        
        // 验证调整比例
        if (request.getAdjustmentRate() != null) {
            if (request.getAdjustmentRate().compareTo(BigDecimal.ZERO) < 0) {
                return false;
            }
        }
        
        // 验证调整金额
        if (request.getAdjustmentAmount() != null && request.getAdjustmentAmount().compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        // 验证生效时间
        if (request.getEffectiveStart() != null && request.getEffectiveEnd() != null) {
            if (request.getEffectiveStart().isAfter(request.getEffectiveEnd())) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public Object getStrategyTypeStatistics(Long companyId) {
        LambdaQueryWrapper<PriceStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceStrategy::getCompanyId, companyId)
                .eq(PriceStrategy::getIsDeleted, false);
        
        List<PriceStrategy> strategies = priceStrategyRepository.selectList(wrapper);
        
        Map<String, Long> typeCount = strategies.stream()
                .collect(Collectors.groupingBy(
                        PriceStrategy::getStrategyType,
                        Collectors.counting()
                ));
        
        Map<String, Long> statusCount = strategies.stream()
                .collect(Collectors.groupingBy(
                        strategy -> strategy.getIsActive() ? "ACTIVE" : "INACTIVE",
                        Collectors.counting()
                ));
        
        return Map.of(
                "totalCount", (long) strategies.size(),
                "typeDistribution", typeCount,
                "statusDistribution", statusCount
        );
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<PriceStrategy> buildQueryWrapper(PriceStrategyQueryRequest request) {
        LambdaQueryWrapper<PriceStrategy> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(PriceStrategy::getCompanyId, request.getCompanyId())
                .eq(PriceStrategy::getIsDeleted, false);
        
        if (StringUtils.hasText(request.getStrategyName())) {
            wrapper.like(PriceStrategy::getStrategyName, request.getStrategyName());
        }
        
        if (StringUtils.hasText(request.getStrategyCode())) {
            wrapper.eq(PriceStrategy::getStrategyCode, request.getStrategyCode());
        }
        
        if (StringUtils.hasText(request.getStrategyType())) {
            wrapper.eq(PriceStrategy::getStrategyType, request.getStrategyType());
        }
        
        if (request.getIsActive() != null) {
            wrapper.eq(PriceStrategy::getIsActive, request.getIsActive());
        }
        
        if (request.getCategoryId() != null) {
            wrapper.eq(PriceStrategy::getCategoryId, request.getCategoryId());
        }
        
        if (StringUtils.hasText(request.getCustomerType())) {
            wrapper.eq(PriceStrategy::getCustomerType, request.getCustomerType());
        }
        
        // 基础价格范围查询
        if (request.getBasePriceFrom() != null) {
            wrapper.ge(PriceStrategy::getBasePrice, request.getBasePriceFrom());
        }
        if (request.getBasePriceTo() != null) {
            wrapper.le(PriceStrategy::getBasePrice, request.getBasePriceTo());
        }
        
        // 调整比例范围查询
        if (request.getAdjustmentRateFrom() != null) {
            wrapper.ge(PriceStrategy::getAdjustmentRate, request.getAdjustmentRateFrom());
        }
        if (request.getAdjustmentRateTo() != null) {
            wrapper.le(PriceStrategy::getAdjustmentRate, request.getAdjustmentRateTo());
        }
        
        // 优先级范围查询
        if (request.getPriorityFrom() != null) {
            wrapper.ge(PriceStrategy::getPriority, request.getPriorityFrom());
        }
        if (request.getPriorityTo() != null) {
            wrapper.le(PriceStrategy::getPriority, request.getPriorityTo());
        }
        
        // 时间范围查询
        if (request.getEffectiveStartFrom() != null) {
            wrapper.ge(PriceStrategy::getEffectiveStart, request.getEffectiveStartFrom());
        }
        if (request.getEffectiveStartTo() != null) {
            wrapper.le(PriceStrategy::getEffectiveStart, request.getEffectiveStartTo());
        }
        
        if (request.getCreatedAtFrom() != null) {
            wrapper.ge(PriceStrategy::getCreatedAt, request.getCreatedAtFrom());
        }
        if (request.getCreatedAtTo() != null) {
            wrapper.le(PriceStrategy::getCreatedAt, request.getCreatedAtTo());
        }
        
        wrapper.orderByAsc(PriceStrategy::getPriority)
                .orderByDesc(PriceStrategy::getCreatedAt);
        
        return wrapper;
    }

    /**
     * 检查策略代码是否已存在
     */
    private boolean isStrategyCodeExists(String strategyCode, Long excludeId, Long companyId) {
        LambdaQueryWrapper<PriceStrategy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceStrategy::getStrategyCode, strategyCode)
                .eq(PriceStrategy::getCompanyId, companyId)
                .eq(PriceStrategy::getIsDeleted, false);
        
        if (excludeId != null) {
            wrapper.ne(PriceStrategy::getId, excludeId);
        }
        
        return priceStrategyRepository.selectCount(wrapper) > 0;
    }

    /**
     * 转换为响应DTO
     */
    private PriceStrategyResponse convertToResponse(PriceStrategy priceStrategy) {
        PriceStrategyResponse response = new PriceStrategyResponse();
        BeanUtils.copyProperties(priceStrategy, response);
        
        // 设置策略类型描述
        try {
            PriceStrategyType strategyType = PriceStrategyType.fromCode(priceStrategy.getStrategyType());
            response.setStrategyTypeDescription(strategyType.getDescription());
        } catch (Exception e) {
            response.setStrategyTypeDescription("未知类型");
        }
        
        return response;
    }
}
