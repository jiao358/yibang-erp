package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.common.response.PageResult;
import com.yibang.erp.common.util.PageUtils;
import com.yibang.erp.domain.dto.PriceTierRequest;
import com.yibang.erp.domain.dto.PriceTierResponse;
import com.yibang.erp.domain.dto.PriceTierQueryRequest;
import com.yibang.erp.domain.entity.PriceTier;
import com.yibang.erp.domain.enums.PriceTierType;
import com.yibang.erp.domain.service.PriceTierService;
import com.yibang.erp.infrastructure.repository.PriceTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 价格分层Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceTierServiceImpl implements PriceTierService {

    private final PriceTierRepository priceTierRepository;

    @Override
    @Transactional
    public PriceTierResponse createPriceTier(PriceTierRequest request) {
        // 验证配置
        if (!validatePriceTierConfig(request)) {
            throw new IllegalArgumentException("价格分层配置无效");
        }

        // 检查分层代码是否已存在
        if (isTierCodeExists(request.getTierCode(), null, request.getCompanyId())) {
            throw new IllegalArgumentException("分层代码已存在");
        }

        PriceTier priceTier = new PriceTier();
        BeanUtils.copyProperties(request, priceTier);
        
        // 设置默认值
        priceTier.setCreatedAt(LocalDateTime.now());
        priceTier.setUpdatedAt(LocalDateTime.now());
        priceTier.setIsDeleted(false);

        priceTierRepository.insert(priceTier);
        
        return getPriceTierById(priceTier.getId());
    }

    @Override
    @Transactional
    public PriceTierResponse updatePriceTier(Long id, PriceTierRequest request) {
        PriceTier existingTier = priceTierRepository.selectById(id);
        if (existingTier == null) {
            throw new IllegalArgumentException("价格分层不存在");
        }

        // 验证配置
        if (!validatePriceTierConfig(request)) {
            throw new IllegalArgumentException("价格分层配置无效");
        }

        // 检查分层代码是否已存在（排除当前记录）
        if (isTierCodeExists(request.getTierCode(), id, request.getCompanyId())) {
            throw new IllegalArgumentException("分层代码已存在");
        }

        BeanUtils.copyProperties(request, existingTier);
        existingTier.setUpdatedAt(LocalDateTime.now());
        
        priceTierRepository.updateById(existingTier);
        
        return getPriceTierById(id);
    }

    @Override
    @Transactional
    public boolean deletePriceTier(Long id) {
        PriceTier priceTier = priceTierRepository.selectById(id);
        if (priceTier == null) {
            return false;
        }
        
        return priceTierRepository.deleteById(id) > 0;
    }

    @Override
    public PriceTierResponse getPriceTierById(Long id) {
        PriceTier priceTier = priceTierRepository.selectById(id);
        if (priceTier == null) {
            return null;
        }
        
        return convertToResponse(priceTier);
    }

    @Override
    public PageResult<PriceTierResponse> getPriceTierPage(PriceTierQueryRequest request) {
        Page<PriceTier> page = new Page<>(request.getPage(), request.getSize());
        
        LambdaQueryWrapper<PriceTier> wrapper = buildQueryWrapper(request);
        IPage<PriceTier> result = priceTierRepository.selectPage(page, wrapper);
        
        List<PriceTierResponse> records = result.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<PriceTierResponse> getPriceTierList(PriceTierQueryRequest request) {
        LambdaQueryWrapper<PriceTier> wrapper = buildQueryWrapper(request);
        List<PriceTier> priceTiers = priceTierRepository.selectList(wrapper);
        
        return priceTiers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean togglePriceTierStatus(Long id, Boolean isActive) {
        PriceTier priceTier = priceTierRepository.selectById(id);
        if (priceTier == null) {
            return false;
        }
        
        priceTier.setIsActive(isActive);
        priceTier.setUpdatedAt(LocalDateTime.now());
        
        return priceTierRepository.updateById(priceTier) > 0;
    }

    @Override
    @Transactional
    public boolean batchTogglePriceTierStatus(List<Long> ids, Boolean isActive) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        
        List<PriceTier> priceTiers = priceTierRepository.selectBatchIds(ids);
        if (priceTiers.isEmpty()) {
            return false;
        }
        
        priceTiers.forEach(tier -> {
            tier.setIsActive(isActive);
            tier.setUpdatedAt(LocalDateTime.now());
        });
        
        // 逐个更新
        int updateCount = 0;
        for (PriceTier tier : priceTiers) {
            if (priceTierRepository.updateById(tier) > 0) {
                updateCount++;
            }
        }
        return updateCount == priceTiers.size();
    }

    @Override
    public List<PriceTierResponse> getApplicablePriceTiers(Long categoryId, String customerType, Long companyId) {
        LambdaQueryWrapper<PriceTier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceTier::getCompanyId, companyId)
                .eq(PriceTier::getIsActive, true)
                .and(w -> w.eq(PriceTier::getCategoryId, categoryId).or().isNull(PriceTier::getCategoryId))
                .and(w -> w.eq(PriceTier::getCustomerType, customerType).or().isNull(PriceTier::getCustomerType))
                .orderByAsc(PriceTier::getPriority);
        
        List<PriceTier> priceTiers = priceTierRepository.selectList(wrapper);
        
        return priceTiers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal calculateFinalPrice(Long priceTierId, BigDecimal originalPrice) {
        PriceTier priceTier = priceTierRepository.selectById(priceTierId);
        if (priceTier == null || !priceTier.getIsActive()) {
            return originalPrice;
        }
        
        BigDecimal finalPrice = originalPrice;
        
        // 应用折扣率
        if (priceTier.getDiscountRate() != null) {
            finalPrice = finalPrice.multiply(BigDecimal.ONE.subtract(priceTier.getDiscountRate()));
        }
        
        // 应用固定折扣
        if (priceTier.getFixedDiscount() != null) {
            finalPrice = finalPrice.subtract(priceTier.getFixedDiscount());
        }
        
        // 确保价格不低于最小值
        if (priceTier.getMinPrice() != null && finalPrice.compareTo(priceTier.getMinPrice()) < 0) {
            finalPrice = priceTier.getMinPrice();
        }
        
        // 确保价格不超过最大值
        if (priceTier.getMaxPrice() != null && finalPrice.compareTo(priceTier.getMaxPrice()) > 0) {
            finalPrice = priceTier.getMaxPrice();
        }
        
        return finalPrice.max(BigDecimal.ZERO);
    }

    @Override
    public boolean validatePriceTierConfig(PriceTierRequest request) {
        // 验证价格范围
        if (request.getMinPrice() != null && request.getMaxPrice() != null) {
            if (request.getMinPrice().compareTo(request.getMaxPrice()) >= 0) {
                return false;
            }
        }
        
        // 验证折扣率
        if (request.getDiscountRate() != null) {
            if (request.getDiscountRate().compareTo(BigDecimal.ZERO) < 0 || 
                request.getDiscountRate().compareTo(BigDecimal.ONE) > 0) {
                return false;
            }
        }
        
        // 验证固定折扣
        if (request.getFixedDiscount() != null && request.getFixedDiscount().compareTo(BigDecimal.ZERO) < 0) {
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

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<PriceTier> buildQueryWrapper(PriceTierQueryRequest request) {
        LambdaQueryWrapper<PriceTier> wrapper = new LambdaQueryWrapper<>();
        
        wrapper.eq(PriceTier::getCompanyId, request.getCompanyId())
                .eq(PriceTier::getIsDeleted, false);
        
        if (StringUtils.hasText(request.getTierName())) {
            wrapper.like(PriceTier::getTierName, request.getTierName());
        }
        
        if (StringUtils.hasText(request.getTierCode())) {
            wrapper.eq(PriceTier::getTierCode, request.getTierCode());
        }
        
        if (StringUtils.hasText(request.getTierType())) {
            wrapper.eq(PriceTier::getTierType, request.getTierType());
        }
        
        if (request.getIsActive() != null) {
            wrapper.eq(PriceTier::getIsActive, request.getIsActive());
        }
        
        if (request.getCategoryId() != null) {
            wrapper.eq(PriceTier::getCategoryId, request.getCategoryId());
        }
        
        if (StringUtils.hasText(request.getCustomerType())) {
            wrapper.eq(PriceTier::getCustomerType, request.getCustomerType());
        }
        
        // 价格范围查询
        if (request.getMinPriceFrom() != null) {
            wrapper.ge(PriceTier::getMinPrice, request.getMinPriceFrom());
        }
        if (request.getMinPriceTo() != null) {
            wrapper.le(PriceTier::getMinPrice, request.getMinPriceTo());
        }
        
        if (request.getMaxPriceFrom() != null) {
            wrapper.ge(PriceTier::getMaxPrice, request.getMaxPriceFrom());
        }
        if (request.getMaxPriceTo() != null) {
            wrapper.le(PriceTier::getMaxPrice, request.getMaxPriceTo());
        }
        
        // 折扣率范围查询
        if (request.getDiscountRateFrom() != null) {
            wrapper.ge(PriceTier::getDiscountRate, request.getDiscountRateFrom());
        }
        if (request.getDiscountRateTo() != null) {
            wrapper.le(PriceTier::getDiscountRate, request.getDiscountRateTo());
        }
        
        // 优先级范围查询
        if (request.getPriorityFrom() != null) {
            wrapper.ge(PriceTier::getPriority, request.getPriorityFrom());
        }
        if (request.getPriorityTo() != null) {
            wrapper.le(PriceTier::getPriority, request.getPriorityTo());
        }
        
        // 时间范围查询
        if (request.getEffectiveStartFrom() != null) {
            wrapper.ge(PriceTier::getEffectiveStart, request.getEffectiveStartFrom());
        }
        if (request.getEffectiveStartTo() != null) {
            wrapper.le(PriceTier::getEffectiveStart, request.getEffectiveStartTo());
        }
        
        if (request.getCreatedAtFrom() != null) {
            wrapper.ge(PriceTier::getCreatedAt, request.getCreatedAtFrom());
        }
        if (request.getCreatedAtTo() != null) {
            wrapper.le(PriceTier::getCreatedAt, request.getCreatedAtTo());
        }
        
        wrapper.orderByAsc(PriceTier::getPriority)
                .orderByDesc(PriceTier::getCreatedAt);
        
        return wrapper;
    }

    /**
     * 检查分层代码是否已存在
     */
    private boolean isTierCodeExists(String tierCode, Long excludeId, Long companyId) {
        LambdaQueryWrapper<PriceTier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PriceTier::getTierCode, tierCode)
                .eq(PriceTier::getCompanyId, companyId)
                .eq(PriceTier::getIsDeleted, false);
        
        if (excludeId != null) {
            wrapper.ne(PriceTier::getId, excludeId);
        }
        
        return priceTierRepository.selectCount(wrapper) > 0;
    }

    /**
     * 转换为响应DTO
     */
    private PriceTierResponse convertToResponse(PriceTier priceTier) {
        PriceTierResponse response = new PriceTierResponse();
        BeanUtils.copyProperties(priceTier, response);
        
        // 设置分层类型描述
        try {
            PriceTierType tierType = PriceTierType.fromCode(priceTier.getTierType());
            response.setTierTypeDescription(tierType.getDescription());
        } catch (Exception e) {
            response.setTierTypeDescription("未知类型");
        }
        
        return response;
    }
}
