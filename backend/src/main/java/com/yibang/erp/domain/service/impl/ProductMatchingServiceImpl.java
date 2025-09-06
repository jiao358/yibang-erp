package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.dto.ProductMatchRequest;
import com.yibang.erp.domain.dto.ProductMatchResult;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.service.ProductMatchingService;
import com.yibang.erp.domain.service.AIProductMatchingService;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品匹配服务实现类
 * 实现智能商品匹配算法
 */
@Slf4j
@Service
public class ProductMatchingServiceImpl implements ProductMatchingService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AIProductMatchingService aiProductMatchingService;

    @Override
    public ProductMatchResult matchBySku(String sku) {
        if (!StringUtils.hasText(sku)) {
            return createNoMatchResult("SKU为空");
        }

        try {
            log.info("根据SKU匹配商品: {}", sku);
            
            // 精确匹配SKU
            Product product = productRepository.selectOne(
                new QueryWrapper<Product>()
                    .eq("sku", sku.trim())
                    .eq("deleted", false)
            );

            if (product != null) {
                return createMatchResult(product, 1.0, "SKU_EXACT", "SKU完全匹配");
            }

            // 模糊匹配SKU（包含关系） SKU 暂时不支持模糊匹配
           /* List<Product> fuzzyMatches = productRepository.selectList(
                new QueryWrapper<Product>()
                    .like("sku", sku.trim())
                    .eq("deleted", false)
                    .last("LIMIT 1000")
            );

            if (!fuzzyMatches.isEmpty()) {
                // 选择最匹配的结果
                Product bestMatch = findBestSkuMatch(sku, fuzzyMatches);
                double confidence = calculateSkuConfidence(sku, bestMatch.getSku());
                return createMatchResult(bestMatch, confidence, "SKU_FUZZY", "SKU模糊匹配");
            }*/

            return createNoMatchResult("未找到匹配的SKU");

        } catch (Exception e) {
            log.error("SKU匹配失败: {}", sku, e);
            return createNoMatchResult("SKU匹配异常: " + e.getMessage());
        }
    }

    @Override
    public List<ProductMatchResult> matchByName(String productName) {
        if (!StringUtils.hasText(productName)) {
            return Collections.emptyList();
        }

        try {
            log.info("根据商品名称匹配商品: {}", productName);
            
            // 首先尝试精确匹配
            List<Product> exactMatches = productRepository.selectList(
                new QueryWrapper<Product>()
                    .eq("name", productName.trim())
                    .eq("deleted", false)
            );

            List<ProductMatchResult> results = new ArrayList<>();
            
            // 添加精确匹配结果
            for (Product product : exactMatches) {
                results.add(createMatchResult(product, 1.0, "NAME_EXACT", "商品名称完全匹配"));
                return results;
            }

            // 如果精确匹配没有结果，使用AI模型进行智能匹配
            if (exactMatches.isEmpty()) {
                log.info("精确匹配无结果，使用AI模型进行智能匹配");
                
                // 获取所有可用的商品作为候选
                List<Product> allProducts = productRepository.selectList(
                    new QueryWrapper<Product>()
                        .eq("deleted", false)
                        .last("LIMIT 400") // 限制数量避免AI处理过慢
                            .select( "sku", "name","unit","company_id","id") // 只选择必要字段
                );
                
                if (!allProducts.isEmpty()) {
                    // 使用AI模型进行智能匹配
                    List<ProductMatchResult> aiResults = aiProductMatchingService.matchByNameWithAI(productName, allProducts);
                    if (aiResults != null && !aiResults.isEmpty()) {
                        results.addAll(aiResults);
                        log.info("AI模型匹配成功，找到 {} 个匹配结果", aiResults.size());
                    } /*else {
                        log.warn("AI模型匹配失败，使用传统模糊匹配作为备选");
                        // AI匹配失败，使用传统方法作为备选
                        results.addAll(fallbackTraditionalMatching(productName));
                    }*/
                }
            }

            // 按置信度排序
            results.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
            
            log.info("商品名称匹配完成，共找到 {} 个匹配结果", results.size());
            return results;

        } catch (Exception e) {
            log.error("商品名称匹配失败: {}", productName, e);
            // 发生异常时，使用传统方法作为备选 todo 暂时不进行错误处理 直接承认错误 比较好
            return Collections.emptyList();
//            return fallbackTraditionalMatching(productName);
        }
    }

    @Override
    public ProductMatchResult smartMatch(String sku, String productName) {
        log.info("智能商品匹配 - SKU: {}, 商品名称: {}", sku, productName);
        
        // 优先使用SKU匹配
        if (StringUtils.hasText(sku)) {
            ProductMatchResult skuResult = matchBySku(sku);
            if (skuResult.isMatched() && skuResult.getConfidence() >= 0.8) {
                log.info("SKU匹配成功，置信度: {}", skuResult.getConfidence());
                return skuResult;
            }
        }

        // SKU匹配失败或置信度过低，使用名称匹配
        if (StringUtils.hasText(productName)) {
            List<ProductMatchResult> nameResults = matchByName(productName);
            if (!nameResults.isEmpty()) {
                ProductMatchResult bestNameResult = nameResults.get(0);
                if (bestNameResult.getConfidence() >= 0.95) {
                    log.info("商品名称匹配成功，置信度: {}", bestNameResult.getConfidence());
                    return bestNameResult;
                }
            }
        }

        // 如果SKU和名称都有，尝试组合匹配  不需要组合匹配，sku有就有没有就失败
       /* if (StringUtils.hasText(sku) && StringUtils.hasText(productName)) {
            ProductMatchResult combinedResult = tryCombinedMatch(sku, productName);
            if (combinedResult.isMatched()) {
                log.info("组合匹配成功，置信度: {}", combinedResult.getConfidence());
                return combinedResult;
            }
        }*/

        // 所有匹配都失败
        log.warn("智能商品匹配失败 - SKU: {}, 商品名称: {}", sku, productName);
        return createNoMatchResult("SKU和商品名称都无法匹配到有效商品");
    }

    @Override
    public List<ProductMatchResult> batchMatch(List<ProductMatchRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("批量商品匹配，请求数量: {}", requests.size());
        
        return requests.parallelStream()
                .map(request -> {
                    switch (request.getMatchType()) {
                        case "SKU_ONLY":
                            return matchBySku(request.getSku());
                        case "NAME_ONLY":
                            List<ProductMatchResult> nameResults = matchByName(request.getProductName());
                            return nameResults.isEmpty() ? createNoMatchResult("名称匹配失败") : nameResults.get(0);
                        case "SMART_MATCH":
                        default:
                            return smartMatch(request.getSku(), request.getProductName());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductMatchSuggestion> getMatchSuggestions(String productInfo) {
        if (!StringUtils.hasText(productInfo)) {
            return Collections.emptyList();
        }

        try {
            log.info("获取商品匹配建议: {}", productInfo);
            
            String normalizedInfo = productInfo.trim().toLowerCase();
            List<ProductMatchSuggestion> suggestions = new ArrayList<>();

            // 基于SKU的建议
            if (normalizedInfo.length() >= 3) {
                List<Product> skuSuggestions = productRepository.selectList(
                    new QueryWrapper<Product>()
                        .like("sku", normalizedInfo)
                        .eq("deleted", false)
                        .last("LIMIT 3")
                );

                for (Product product : skuSuggestions) {
                    ProductMatchSuggestion suggestion = new ProductMatchSuggestion();
                    suggestion.setProductId(product.getId());
                    suggestion.setProductName(product.getName());
                    suggestion.setSku(product.getSku());
                    suggestion.setSpecification(product.getSpecifications());
                    suggestion.setConfidence(0.7);
                    suggestion.setReason("SKU部分匹配");
                    suggestion.setSuggestion("请检查SKU是否完整");
                    suggestions.add(suggestion);
                }
            }

            // 基于名称的建议
            if (normalizedInfo.length() >= 2) {
                List<Product> nameSuggestions = productRepository.selectList(
                    new QueryWrapper<Product>()
                        .like("name", normalizedInfo)
                        .eq("deleted", false)
                        .last("LIMIT 3")
                );

                for (Product product : nameSuggestions) {
                    ProductMatchSuggestion suggestion = new ProductMatchSuggestion();
                    suggestion.setProductId(product.getId());
                    suggestion.setProductName(product.getName());
                    suggestion.setSku(product.getSku());
                    suggestion.setSpecification(product.getSpecifications());
                    suggestion.setConfidence(0.6);
                    suggestion.setReason("商品名称部分匹配");
                    suggestion.setSuggestion("请检查商品名称是否准确");
                    suggestions.add(suggestion);
                }
            }

            // 去重并按置信度排序
            suggestions = suggestions.stream()
                    .collect(Collectors.toMap(
                        ProductMatchSuggestion::getProductId,
                        suggestion -> suggestion,
                        (existing, replacement) -> existing.getConfidence() > replacement.getConfidence() ? existing : replacement
                    ))
                    .values()
                    .stream()
                    .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
                    .collect(Collectors.toList());

            return suggestions;

        } catch (Exception e) {
            log.error("获取商品匹配建议失败: {}", productInfo, e);
            return Collections.emptyList();
        }
    }

    /**
     * 尝试组合匹配（SKU + 商品名称）
     */
    private ProductMatchResult tryCombinedMatch(String sku, String productName) {
        try {
            // 查找同时满足SKU和名称条件的商品
            List<Product> combinedMatches = productRepository.selectList(
                new QueryWrapper<Product>()
                    .like("sku", sku.trim())
                    .like("name", productName.trim())
                    .eq("deleted", false)
            );

            if (!combinedMatches.isEmpty()) {
                Product bestMatch = findBestCombinedMatch(sku, productName, combinedMatches);
                double confidence = calculateCombinedConfidence(sku, productName, bestMatch);
                return createMatchResult(bestMatch, confidence, "COMBINED_MATCH", "SKU和名称组合匹配");
            }

            return createNoMatchResult("组合匹配失败");

        } catch (Exception e) {
            log.error("组合匹配失败", e);
            return createNoMatchResult("组合匹配异常: " + e.getMessage());
        }
    }

    /**
     * 查找最佳SKU匹配
     */
    private Product findBestSkuMatch(String targetSku, List<Product> candidates) {
        return candidates.stream()
                .max(Comparator.comparingDouble(product -> calculateSkuConfidence(targetSku, product.getSku())))
                .orElse(candidates.get(0));
    }

    /**
     * 查找最佳组合匹配
     */
    private Product findBestCombinedMatch(String targetSku, String targetName, List<Product> candidates) {
        return candidates.stream()
                .max(Comparator.comparingDouble(product -> 
                    calculateCombinedConfidence(targetSku, targetName, product)))
                .orElse(candidates.get(0));
    }

    /**
     * 计算SKU匹配置信度
     */
    private double calculateSkuConfidence(String targetSku, String productSku) {
        if (targetSku.equals(productSku)) {
            return 1.0;
        }
        
        if (productSku.contains(targetSku)) {
            return 0.8;
        }
        
        if (targetSku.contains(productSku)) {
            return 0.7;
        }
        
        // 计算编辑距离
        int distance = calculateLevenshteinDistance(targetSku, productSku);
        int maxLength = Math.max(targetSku.length(), productSku.length());
        
        return Math.max(0.1, 1.0 - (double) distance / maxLength);
    }

    /**
     * 传统模糊匹配方法（作为AI匹配的备选）
     */
    private List<ProductMatchResult> fallbackTraditionalMatching(String productName) {
        log.info("使用传统模糊匹配方法: {}", productName);
        
        try {
            String normalizedName = productName.trim().toLowerCase();
            List<ProductMatchResult> results = new ArrayList<>();
            
            // 模糊匹配商品名称
            List<Product> fuzzyMatches = productRepository.selectList(
                new QueryWrapper<Product>()
                    .like("name", productName.trim())
                    .eq("deleted", false)
                    .last("LIMIT 10")
            );

            // 添加模糊匹配结果
            for (Product product : fuzzyMatches) {
                double confidence = calculateNameConfidence(normalizedName, product.getName().toLowerCase());
                if (confidence >= 0.3) { // 设置最低置信度阈值
                    results.add(createMatchResult(product, confidence, "NAME_FUZZY", "商品名称模糊匹配"));
                }
            }

            // 按置信度排序
            results.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
            
            log.info("传统模糊匹配完成，找到 {} 个匹配结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("传统模糊匹配失败: {}", productName, e);
            return Collections.emptyList();
        }
    }

    /**
     * 计算名称匹配置信度
     */
    private double calculateNameConfidence(String targetName, String productName) {
        if (targetName.equals(productName)) {
            return 1.0;
        }
        
        if (productName.contains(targetName)) {
            return 0.9;
        }
        
        if (targetName.contains(productName)) {
            return 0.8;
        }
        
        // 计算编辑距离
        int distance = calculateLevenshteinDistance(targetName, productName);
        int maxLength = Math.max(targetName.length(), productName.length());
        
        return Math.max(0.1, 1.0 - (double) distance / maxLength);
    }

    /**
     * 计算组合匹配置信度
     */
    private double calculateCombinedConfidence(String targetSku, String targetName, Product product) {
        double skuConfidence = calculateSkuConfidence(targetSku, product.getSku());
        double nameConfidence = calculateNameConfidence(targetName, product.getName().toLowerCase());
        
        // 加权平均，SKU权重更高
        return skuConfidence * 0.6 + nameConfidence * 0.4;
    }

    /**
     * 计算编辑距离（Levenshtein距离）
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1])) + 1;
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * 创建匹配成功的结果
     */
    private ProductMatchResult createMatchResult(Product product, double confidence, String matchType, String matchReason) {
        ProductMatchResult result = new ProductMatchResult();
        result.setMatched(true);
        result.setProductId(product.getId());
        result.setProductName(product.getName());
        result.setSku(product.getSku());
        result.setSpecification(product.getSpecifications());
        result.setConfidence(confidence);
        result.setMatchType(matchType);
        result.setMatchReason(matchReason);
        result.setProductStatus(product.getStatus().name());
        result.setSupplierCompanyId(product.getCompanyId());
        
        // 设置供应商信息（如果有的话）
        if (product.getCompanyId() != null) {
            result.setSupplierCompanyId(product.getCompanyId());
            // TODO: 获取供应商公司名称
            result.setSupplierCompanyName("供应商公司");
        }
        
        return result;
    }

    /**
     * 创建匹配失败的结果
     */
    private ProductMatchResult createNoMatchResult(String reason) {
        ProductMatchResult result = new ProductMatchResult();
        result.setMatched(false);
        result.setConfidence(0.0);
        result.setMatchReason(reason);
        result.setSuggestion("请检查商品信息是否正确，或联系管理员添加新商品");
        return result;
    }
}
