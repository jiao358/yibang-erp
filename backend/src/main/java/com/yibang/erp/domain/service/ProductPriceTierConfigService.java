package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.ProductPriceTierConfigRequest;
import com.yibang.erp.domain.dto.ProductPriceTierConfigResponse;

import java.util.List;

/**
 * 商品价格分层配置服务接口
 */
public interface ProductPriceTierConfigService {

    /**
     * 创建价格分层配置
     */
    ProductPriceTierConfigResponse createConfig(ProductPriceTierConfigRequest request);

    /**
     * 更新价格分层配置
     */
    ProductPriceTierConfigResponse updateConfig(Long id, ProductPriceTierConfigRequest request);

    /**
     * 删除价格分层配置
     */
    boolean deleteConfig(Long id);

    /**
     * 根据ID获取价格分层配置
     */
    ProductPriceTierConfigResponse getConfigById(Long id);

    /**
     * 根据商品ID获取所有价格分层配置
     */
    List<ProductPriceTierConfigResponse> getConfigsByProductId(Long productId);

    /**
     * 批量保存价格分层配置
     */
    boolean batchSaveConfigs(Long productId, List<ProductPriceTierConfigRequest> configs);

    /**
     * 根据价格分层ID获取配置
     */
    List<ProductPriceTierConfigResponse> getConfigsByPriceTierId(Long priceTierId);

    /**
     * 根据产品ID列表和价格分层ID查询价格配置
     * 
     * @param productIds 产品ID列表
     * @param priceTierId 价格分层ID
     * @return 价格配置列表
     */
    List<ProductPriceTierConfigResponse> getConfigsByProductIdsAndPriceTierId(List<Long> productIds, Long priceTierId);
}
