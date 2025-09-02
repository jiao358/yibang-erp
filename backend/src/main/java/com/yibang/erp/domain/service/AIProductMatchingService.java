package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.ProductMatchResult;
import com.yibang.erp.domain.entity.Product;

import java.util.List;

/**
 * AI商品匹配服务接口
 * 使用AI模型进行智能商品名称匹配
 */
public interface AIProductMatchingService {

    /**
     * 使用AI模型匹配商品名称
     * @param targetProductName 目标商品名称
     * @param candidateProducts 候选商品列表
     * @return 匹配结果列表，按置信度排序
     */
    List<ProductMatchResult> matchByNameWithAI(String targetProductName, List<Product> candidateProducts);

    /**
     * 使用AI模型计算商品名称相似度
     * @param name1 商品名称1
     * @param name2 商品名称2
     * @return 相似度分数 (0.0 - 1.0)
     */
    double calculateNameSimilarityWithAI(String name1, String name2);

    /**
     * 使用AI模型进行批量商品名称匹配
     * @param targetNames 目标商品名称列表
     * @param candidateProducts 候选商品列表
     * @return 匹配结果映射
     */
    java.util.Map<String, List<ProductMatchResult>> batchMatchNamesWithAI(
        List<String> targetNames, List<Product> candidateProducts);
}
