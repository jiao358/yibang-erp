package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yibang.erp.domain.dto.ProductPriceTierConfigRequest;
import com.yibang.erp.domain.dto.ProductPriceTierConfigResponse;
import com.yibang.erp.domain.entity.ProductPriceTierConfig;
import com.yibang.erp.domain.entity.PriceTier;
import com.yibang.erp.domain.service.ProductPriceTierConfigService;
import com.yibang.erp.infrastructure.repository.ProductPriceTierConfigRepository;
import com.yibang.erp.infrastructure.repository.PriceTierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品价格分层配置服务实现类
 */
@Service
@Transactional
public class ProductPriceTierConfigServiceImpl implements ProductPriceTierConfigService {

    @Autowired
    private ProductPriceTierConfigRepository configRepository;

    @Autowired
    private PriceTierRepository priceTierRepository;

    @Override
    public ProductPriceTierConfigResponse createConfig(ProductPriceTierConfigRequest request) {
        ProductPriceTierConfig config = new ProductPriceTierConfig();
        config.setProductId(request.getProductId());
        config.setPriceTierId(request.getPriceTierId());
        config.setDropshippingPrice(request.getDropshippingPrice());
        config.setRetailLimitPrice(request.getRetailLimitPrice());
        config.setIsActive(request.getIsActive());
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setCreatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : 1L);
        config.setUpdatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : 1L);

        configRepository.insert(config);
        return convertToResponse(config);
    }

    @Override
    public ProductPriceTierConfigResponse updateConfig(Long id, ProductPriceTierConfigRequest request) {
        ProductPriceTierConfig config = configRepository.selectById(id);
        if (config == null) {
            throw new RuntimeException("价格分层配置不存在");
        }

        config.setPriceTierId(request.getPriceTierId());
        config.setDropshippingPrice(request.getDropshippingPrice());
        config.setRetailLimitPrice(request.getRetailLimitPrice());
        config.setIsActive(request.getIsActive());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(request.getCreatedBy() != null ? request.getCreatedBy() : 1L);

        configRepository.updateById(config);
        return convertToResponse(config);
    }

    @Override
    public boolean deleteConfig(Long id) {
        return configRepository.deleteById(id) > 0;
    }

    @Override
    public ProductPriceTierConfigResponse getConfigById(Long id) {
        ProductPriceTierConfig config = configRepository.selectById(id);
        return config != null ? convertToResponse(config) : null;
    }

    @Override
    public List<ProductPriceTierConfigResponse> getConfigsByProductId(Long productId) {
        LambdaQueryWrapper<ProductPriceTierConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPriceTierConfig::getProductId, productId)
                   .eq(ProductPriceTierConfig::getIsActive, true)
                   .orderByAsc(ProductPriceTierConfig::getId);

        List<ProductPriceTierConfig> configs = configRepository.selectList(queryWrapper);
        return enrichWithPriceTierInfo(configs);
    }

    @Override
    public boolean batchSaveConfigs(Long productId, List<ProductPriceTierConfigRequest> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return true;
        }

        // 先删除该商品的所有配置
        LambdaQueryWrapper<ProductPriceTierConfig> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProductPriceTierConfig::getProductId, productId);
        configRepository.delete(deleteWrapper);

        // 批量插入新配置
        for (ProductPriceTierConfigRequest request : configs) {
            request.setProductId(productId);
            createConfig(request);
        }

        return true;
    }

    @Override
    public List<ProductPriceTierConfigResponse> getConfigsByPriceTierId(Long priceTierId) {
        LambdaQueryWrapper<ProductPriceTierConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductPriceTierConfig::getPriceTierId, priceTierId)
                   .eq(ProductPriceTierConfig::getIsActive, true);

        List<ProductPriceTierConfig> configs = configRepository.selectList(queryWrapper);
        return enrichWithPriceTierInfo(configs);
    }

    /**
     * 转换为响应DTO
     */
    private ProductPriceTierConfigResponse convertToResponse(ProductPriceTierConfig config) {
        ProductPriceTierConfigResponse response = new ProductPriceTierConfigResponse();
        response.setId(config.getId());
        response.setProductId(config.getProductId());
        response.setPriceTierId(config.getPriceTierId());
        response.setDropshippingPrice(config.getDropshippingPrice());
        response.setRetailLimitPrice(config.getRetailLimitPrice());
        response.setIsActive(config.getIsActive());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        response.setCreatedBy(config.getCreatedBy());
        response.setUpdatedBy(config.getUpdatedBy());

        // 获取价格分层信息
        PriceTier priceTier = priceTierRepository.selectById(config.getPriceTierId());
        if (priceTier != null) {
            response.setPriceTierName(priceTier.getTierName());
            response.setPriceTierType(priceTier.getTierType());
        }

        return response;
    }

    /**
     * 丰富价格分层信息
     */
    private List<ProductPriceTierConfigResponse> enrichWithPriceTierInfo(List<ProductPriceTierConfig> configs) {
        if (CollectionUtils.isEmpty(configs)) {
            return List.of();
        }

        // 获取所有价格分层ID
        List<Long> priceTierIds = configs.stream()
                .map(ProductPriceTierConfig::getPriceTierId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询价格分层信息
        List<PriceTier> priceTiers = priceTierRepository.selectBatchIds(priceTierIds);
        Map<Long, PriceTier> priceTierMap = priceTiers.stream()
                .collect(Collectors.toMap(PriceTier::getId, pt -> pt));

        // 转换为响应DTO并填充价格分层信息
        return configs.stream()
                .map(config -> {
                    ProductPriceTierConfigResponse response = convertToResponse(config);
                    PriceTier priceTier = priceTierMap.get(config.getPriceTierId());
                    if (priceTier != null) {
                        response.setPriceTierName(priceTier.getTierName());
                        response.setPriceTierType(priceTier.getTierType());
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductPriceTierConfigResponse> getConfigsByProductIdsAndPriceTierId(List<Long> productIds, Long priceTierId) {
        if (CollectionUtils.isEmpty(productIds) || priceTierId == null) {
            return List.of();
        }

        LambdaQueryWrapper<ProductPriceTierConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ProductPriceTierConfig::getProductId, productIds)
                   .eq(ProductPriceTierConfig::getPriceTierId, priceTierId)
                   .eq(ProductPriceTierConfig::getIsActive, true)
                   .eq(ProductPriceTierConfig::getIsDeleted, Boolean.FALSE)
                   .orderByAsc(ProductPriceTierConfig::getProductId)
                   .orderByAsc(ProductPriceTierConfig::getId);

        List<ProductPriceTierConfig> configs = configRepository.selectList(queryWrapper);
        return enrichWithPriceTierInfo(configs);
    }
}
