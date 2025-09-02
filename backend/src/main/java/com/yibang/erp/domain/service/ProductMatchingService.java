package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.ProductMatchRequest;
import com.yibang.erp.domain.dto.ProductMatchResult;

import java.util.List;

/**
 * 商品匹配服务接口
 * 负责根据SKU或商品名称智能匹配系统中的商品
 */
public interface ProductMatchingService {

    /**
     * 根据SKU精确匹配商品
     * @param sku 商品SKU/69码
     * @return 商品匹配结果
     */
    ProductMatchResult matchBySku(String sku);

    /**
     * 根据商品名称模糊匹配商品
     * @param productName 商品名称
     * @return 商品匹配结果列表（按置信度排序）
     */
    List<ProductMatchResult> matchByName(String productName);

    /**
     * 智能商品匹配（优先SKU，其次名称）
     * @param sku 商品SKU
     * @param productName 商品名称
     * @return 最佳匹配结果
     */
    ProductMatchResult smartMatch(String sku, String productName);

    /**
     * 批量商品匹配
     * @param requests 批量匹配请求
     * @return 批量匹配结果
     */
    List<ProductMatchResult> batchMatch(List<ProductMatchRequest> requests);

    /**
     * 获取商品匹配建议
     * @param productInfo 商品信息
     * @return 匹配建议列表
     */
    List<ProductMatchSuggestion> getMatchSuggestions(String productInfo);

    /**
     * 商品匹配建议
     */
    class ProductMatchSuggestion {
        private Long productId;
        private String productName;
        private String sku;
        private String specification;
        private double confidence;
        private String reason;
        private String suggestion;

        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getSpecification() { return specification; }
        public void setSpecification(String specification) { this.specification = specification; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
