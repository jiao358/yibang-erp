package com.yibang.erp.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yibang.erp.domain.dto.DeepSeekChatRequest;
import com.yibang.erp.domain.dto.DeepSeekChatResponse;
import com.yibang.erp.domain.dto.ProductMatchResult;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.service.AIProductMatchingService;
import com.yibang.erp.infrastructure.client.DeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * AI商品匹配服务实现类
 * 使用DeepSeek AI模型进行智能商品名称匹配
 */
@Slf4j
@Service
public class AIProductMatchingServiceImpl implements AIProductMatchingService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<ProductMatchResult> matchByNameWithAI(String targetProductName, List<Product> candidateProducts) {
        if (!StringUtils.hasText(targetProductName) || candidateProducts == null || candidateProducts.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            log.info("使用AI模型匹配商品名称: {}, 候选商品数量: {}", targetProductName, candidateProducts.size());

            // 构建AI提示词
            String prompt = buildProductMatchingPrompt(targetProductName, candidateProducts);

            // 调用AI模型进行匹配
            List<ProductMatchResult> aiResults = callAIForProductMatching(prompt, targetProductName, candidateProducts);

            // 如果AI匹配失败，使用传统方法作为备选
            if (aiResults == null || aiResults.isEmpty()) {
                log.warn("AI商品匹配失败，名称不用传统");
//                return fallbackTraditionalMatching(targetProductName, candidateProducts);
                return Collections.emptyList();
            }

            // 按置信度排序
            aiResults.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

            log.info("AI商品匹配完成，找到 {} 个匹配结果", aiResults.size());
            return aiResults;

        } catch (Exception e) {
            log.error("AI商品匹配失败: {}", targetProductName, e);
//            return fallbackTraditionalMatching(targetProductName, candidateProducts);
            return Collections.emptyList();

        }
    }

    @Override
    public double calculateNameSimilarityWithAI(String name1, String name2) {
        if (!StringUtils.hasText(name1) || !StringUtils.hasText(name2)) {
            return 0.0;
        }

        try {
            log.debug("使用AI模型计算商品名称相似度: {} vs {}", name1, name2);

            // 构建AI提示词
            String prompt = buildSimilarityPrompt(name1, name2);

            // 调用AI模型计算相似度
            Double similarity = callAIForSimilarity(prompt);

            if (similarity != null) {
                log.debug("AI计算的相似度: {} vs {} = {}", name1, name2, similarity);
                return similarity;
            } else {
                log.warn("AI相似度计算失败，使用传统方法");
                return calculateTraditionalSimilarity(name1, name2);
            }

        } catch (Exception e) {
            log.error("AI相似度计算失败: {} vs {}", name1, name2, e);
            return calculateTraditionalSimilarity(name1, name2);
        }
    }

    @Override
    public Map<String, List<ProductMatchResult>> batchMatchNamesWithAI(
            List<String> targetNames, List<Product> candidateProducts) {
        
        if (targetNames == null || targetNames.isEmpty() || candidateProducts == null || candidateProducts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, List<ProductMatchResult>> results = new HashMap<>();

        try {
            log.info("开始批量AI商品名称匹配，目标名称数量: {}, 候选商品数量: {}", 
                    targetNames.size(), candidateProducts.size());

            // 构建批量匹配的AI提示词
            String prompt = buildBatchMatchingPrompt(targetNames, candidateProducts);

            // 调用AI模型进行批量匹配
            Map<String, List<ProductMatchResult>> aiResults = callAIForBatchMatching(prompt, targetNames, candidateProducts);

            if (aiResults != null && !aiResults.isEmpty()) {
                results.putAll(aiResults);
                log.info("批量AI匹配完成，成功匹配 {} 个商品名称", results.size());
            } else {
                log.warn("批量AI匹配失败，使用传统方法");
                // 逐个使用传统方法匹配
                for (String targetName : targetNames) {
                    results.put(targetName, fallbackTraditionalMatching(targetName, candidateProducts));
                }
            }

        } catch (Exception e) {
            log.error("批量AI商品匹配失败", e);
            // 使用传统方法作为备选
            for (String targetName : targetNames) {
                results.put(targetName, fallbackTraditionalMatching(targetName, candidateProducts));
            }
        }

        return results;
    }

    /**
     * 构建商品匹配的AI提示词
     */
    private String buildProductMatchingPrompt(String targetProductName, List<Product> candidateProducts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个商品匹配专家。请分析目标商品名称与候选商品列表的匹配程度。\n\n");
        prompt.append("目标商品名称: ").append(targetProductName).append("\n\n");
        prompt.append("候选商品列表:\n");
        
        for (int i = 0; i < candidateProducts.size(); i++) {
            Product product = candidateProducts.get(i);
            prompt.append(i + 1).append(". 商品名称: ").append(product.getName());
            if (StringUtils.hasText(product.getSku())) {
                prompt.append(", SKU: ").append(product.getSku());
            }
            if (StringUtils.hasText(product.getSpecifications())) {
                prompt.append(", 规格: ").append(product.getSpecifications());
            }
            prompt.append("\n");
        }
        
        prompt.append("\n请分析每个候选商品与目标商品的匹配程度，考虑以下因素：\n");
        prompt.append("1. 商品名称的语义相似性\n");
        prompt.append("2. 商品类别和功能的相关性\n");
        prompt.append("3. 规格和型号的匹配度\n");
        prompt.append("4. 品牌和系列的关联性\n\n");
        
        prompt.append("请返回JSON格式的匹配结果，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"matches\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"productIndex\": 0,\n");
        prompt.append("      \"confidence\": 0.95,\n");
        prompt.append("      \"matchReason\": \"商品名称高度相似，规格匹配\",\n");
        prompt.append("      \"matchType\": \"NAME_SEMANTIC\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n\n");
        prompt.append("置信度说明：\n");
        prompt.append("- 0.9-1.0: 完全匹配或高度相似\n");
        prompt.append("- 0.7-0.89: 高度相关\n");
        prompt.append("- 0.5-0.69: 中等相关\n");
        prompt.append("- 0.3-0.49: 低度相关\n");
        prompt.append("- 0.0-0.29: 不相关\n");
        
        return prompt.toString();
    }

    /**
     * 构建相似度计算的AI提示词
     */
    private String buildSimilarityPrompt(String name1, String name2) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个商品名称相似度分析专家。请分析两个商品名称的相似程度。\n\n");
        prompt.append("商品名称1: ").append(name1).append("\n");
        prompt.append("商品名称2: ").append(name2).append("\n\n");
        
        prompt.append("请考虑以下因素：\n");
        prompt.append("1. 语义相似性（功能、用途、类别）\n");
        prompt.append("2. 词汇相似性（关键词、描述词）\n");
        prompt.append("3. 品牌和系列关联性\n");
        prompt.append("4. 规格和型号的匹配度\n\n");
        
        prompt.append("请返回一个0.0到1.0之间的相似度分数，其中：\n");
        prompt.append("- 1.0: 完全相同或完全同义\n");
        prompt.append("- 0.9: 高度相似（如：\"iPhone 13\" vs \"iPhone 13 Pro\"）\n");
        prompt.append("- 0.7: 相关产品（如：\"苹果手机\" vs \"iPhone\"）\n");
        prompt.append("- 0.5: 同类产品（如：\"智能手机\" vs \"iPhone\"）\n");
        prompt.append("- 0.3: 低度相关（如：\"手机\" vs \"平板电脑\"）\n");
        prompt.append("- 0.0: 完全不相关\n\n");
        
        prompt.append("请只返回数字，例如：0.85");
        
        return prompt.toString();
    }

    /**
     * 构建批量匹配的AI提示词
     */
    private String buildBatchMatchingPrompt(List<String> targetNames, List<Product> candidateProducts) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个商品批量匹配专家。请分析多个目标商品名称与候选商品列表的匹配关系。\n\n");
        
        prompt.append("目标商品名称列表:\n");
        for (int i = 0; i < targetNames.size(); i++) {
            prompt.append(i + 1).append(". ").append(targetNames.get(i)).append("\n");
        }
        
        prompt.append("\n候选商品列表:\n");
        for (int i = 0; i < candidateProducts.size(); i++) {
            Product product = candidateProducts.get(i);
            prompt.append(i + 1).append(". 商品名称: ").append(product.getName());
            if (StringUtils.hasText(product.getSku())) {
                prompt.append(", SKU: ").append(product.getSku());
            }
            prompt.append("\n");
        }
        
        prompt.append("\n请为每个目标商品名称找到最匹配的候选商品，返回JSON格式：\n");
        prompt.append("{\n");
        prompt.append("  \"targetProductName1\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"productIndex\": 0,\n");
        prompt.append("      \"confidence\": 0.95,\n");
        prompt.append("      \"matchReason\": \"匹配原因\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"targetProductName2\": [...]\n");
        prompt.append("}\n");
        
        return prompt.toString();
    }

    /**
     * 调用AI模型进行商品匹配
     */
    private List<ProductMatchResult> callAIForProductMatching(String prompt, String targetProductName, List<Product> candidateProducts) {
        try {
            // 构建DeepSeek请求
            DeepSeekChatRequest request = new DeepSeekChatRequest();
            request.setModel("deepseek-chat");
            request.setMaxTokens(30000);
            request.setTemperature(0.1);

            // 构建消息
            DeepSeekChatRequest.DeepSeekMessage message = new DeepSeekChatRequest.DeepSeekMessage();
            message.setRole("user");
            message.setContent(prompt);
            request.setMessages(new DeepSeekChatRequest.DeepSeekMessage[]{message});

            // 调用AI模型
            DeepSeekChatResponse response = deepSeekClient.chat(request).block();

            if (response != null && response.getChoices() != null && response.getChoices().length > 0) {
                String aiResponse = response.getChoices()[0].getMessage().getContent();
                log.debug("AI商品匹配响应: {}", aiResponse);

                // 解析AI响应
                return parseProductMatchingResponse(aiResponse, targetProductName, candidateProducts);
            } else {
                log.warn("AI商品匹配响应为空或格式不正确");
                return null;
            }

        } catch (Exception e) {
            log.error("调用AI进行商品匹配失败", e);
            return null;
        }
    }

    /**
     * 调用AI模型计算相似度
     */
    private Double callAIForSimilarity(String prompt) {
        try {
            // 构建DeepSeek请求
            DeepSeekChatRequest request = new DeepSeekChatRequest();
            request.setModel("deepseek-chat");
            request.setMaxTokens(1000);
            request.setTemperature(0.1);

            // 构建消息
            DeepSeekChatRequest.DeepSeekMessage message = new DeepSeekChatRequest.DeepSeekMessage();
            message.setRole("user");
            message.setContent(prompt);
            request.setMessages(new DeepSeekChatRequest.DeepSeekMessage[]{message});

            // 调用AI模型
            DeepSeekChatResponse response = deepSeekClient.chat(request).block();

            if (response != null && response.getChoices() != null && response.getChoices().length > 0) {
                String aiResponse = response.getChoices()[0].getMessage().getContent();
                log.debug("AI相似度响应: {}", aiResponse);

                // 解析AI响应中的数字
                return parseSimilarityResponse(aiResponse);
            } else {
                log.warn("AI相似度响应为空或格式不正确");
                return null;
            }

        } catch (Exception e) {
            log.error("调用AI计算相似度失败", e);
            return null;
        }
    }

    /**
     * 调用AI模型进行批量匹配
     */
    private Map<String, List<ProductMatchResult>> callAIForBatchMatching(String prompt, List<String> targetNames, List<Product> candidateProducts) {
        try {
            // 构建DeepSeek请求
            DeepSeekChatRequest request = new DeepSeekChatRequest();
            request.setModel("deepseek-chat");
            request.setMaxTokens(5000);
            request.setTemperature(0.1);

            // 构建消息
            DeepSeekChatRequest.DeepSeekMessage message = new DeepSeekChatRequest.DeepSeekMessage();
            message.setRole("user");
            message.setContent(prompt);
            request.setMessages(new DeepSeekChatRequest.DeepSeekMessage[]{message});

            // 调用AI模型
            DeepSeekChatResponse response = deepSeekClient.chat(request).block();

            if (response != null && response.getChoices() != null && response.getChoices().length > 0) {
                String aiResponse = response.getChoices()[0].getMessage().getContent();
                log.debug("AI批量匹配响应: {}", aiResponse);

                // 解析AI响应
                return parseBatchMatchingResponse(aiResponse, targetNames, candidateProducts);
            } else {
                log.warn("AI批量匹配响应为空或格式不正确");
                return null;
            }

        } catch (Exception e) {
            log.error("调用AI进行批量匹配失败", e);
            return null;
        }
    }

    /**
     * 解析商品匹配的AI响应
     */
    private List<ProductMatchResult> parseProductMatchingResponse(String aiResponse, String targetProductName, List<Product> candidateProducts) {
        try {
            // 尝试从AI响应中提取JSON
            String jsonContent = extractJsonFromResponse(aiResponse);
            if (jsonContent == null) {
                log.warn("无法从AI响应中提取JSON内容");
                return null;
            }

            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(jsonContent, Map.class);
            List<ProductMatchResult> results = new ArrayList<>();

            if (responseMap.containsKey("matches")) {
                List<Map<String, Object>> matches = (List<Map<String, Object>>) responseMap.get("matches");

                for (Map<String, Object> match : matches) {
                    Integer productIndex = (Integer) match.get("productIndex");
                    Double confidence = ((Number) match.get("confidence")).doubleValue();
                    String matchReason = (String) match.get("matchReason");
                    String matchType = (String) match.get("matchType");

                    if (productIndex != null && productIndex >= 0 && productIndex < candidateProducts.size()) {
                        Product product = candidateProducts.get(productIndex);
                        ProductMatchResult result = createMatchResult(product, confidence, matchType, matchReason);
                        results.add(result);
                    }
                }
            }

            return results;

        } catch (Exception e) {
            log.error("解析AI商品匹配响应失败", e);
            return null;
        }
    }

    /**
     * 解析相似度的AI响应
     */
    private Double parseSimilarityResponse(String aiResponse) {
        try {
            // 提取数字
            String[] parts = aiResponse.trim().split("\\s+");
            for (String part : parts) {
                try {
                    double value = Double.parseDouble(part);
                    if (value >= 0.0 && value <= 1.0) {
                        return value;
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略非数字部分
                }
            }
            return null;
        } catch (Exception e) {
            log.error("解析AI相似度响应失败", e);
            return null;
        }
    }

    /**
     * 解析批量匹配的AI响应
     */
    private Map<String, List<ProductMatchResult>> parseBatchMatchingResponse(String aiResponse, List<String> targetNames, List<Product> candidateProducts) {
        try {
            // 尝试从AI响应中提取JSON
            String jsonContent = extractJsonFromResponse(aiResponse);
            if (jsonContent == null) {
                log.warn("无法从AI响应中提取JSON内容");
                return null;
            }

            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(jsonContent, Map.class);
            Map<String, List<ProductMatchResult>> results = new HashMap<>();

            for (String targetName : targetNames) {
                if (responseMap.containsKey(targetName)) {
                    List<Map<String, Object>> matches = (List<Map<String, Object>>) responseMap.get(targetName);
                    List<ProductMatchResult> targetResults = new ArrayList<>();

                    for (Map<String, Object> match : matches) {
                        Integer productIndex = (Integer) match.get("productIndex");
                        Double confidence = ((Number) match.get("confidence")).doubleValue();
                        String matchReason = (String) match.get("matchReason");

                        if (productIndex != null && productIndex >= 0 && productIndex < candidateProducts.size()) {
                            Product product = candidateProducts.get(productIndex);
                            ProductMatchResult result = createMatchResult(product, confidence, "AI_BATCH", matchReason);
                            targetResults.add(result);
                        }
                    }

                    results.put(targetName, targetResults);
                }
            }

            return results;

        } catch (Exception e) {
            log.error("解析AI批量匹配响应失败", e);
            return null;
        }
    }

    /**
     * 从AI响应中提取JSON内容
     */
    private String extractJsonFromResponse(String aiResponse) {
        if (aiResponse == null) {
            return null;
        }

        // 查找JSON开始和结束位置
        int startIndex = aiResponse.indexOf('{');
        int endIndex = aiResponse.lastIndexOf('}');

        if (startIndex >= 0 && endIndex > startIndex) {
            return aiResponse.substring(startIndex, endIndex + 1);
        }

        return null;
    }

    /**
     * 传统匹配方法作为备选
     */
    private List<ProductMatchResult> fallbackTraditionalMatching(String targetProductName, List<Product> candidateProducts) {
        log.info("使用传统方法匹配商品名称: {}", targetProductName);
        
        List<ProductMatchResult> results = new ArrayList<>();
        String normalizedName = targetProductName.trim().toLowerCase();

        for (Product product : candidateProducts) {
            double confidence = calculateTraditionalSimilarity(normalizedName, product.getName().toLowerCase());
            if (confidence >= 0.3) { // 设置最低置信度阈值
                String matchType = confidence >= 0.8 ? "NAME_TRADITIONAL_HIGH" : "NAME_TRADITIONAL_LOW";
                String matchReason = String.format("传统方法匹配，相似度: %.2f", confidence);
                results.add(createMatchResult(product, confidence, matchType, matchReason));
            }
        }

        // 按置信度排序
        results.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
        return results;
    }

    /**
     * 传统相似度计算方法
     */
    private double calculateTraditionalSimilarity(String name1, String name2) {
        if (name1.equals(name2)) {
            return 1.0;
        }

        if (name1.contains(name2) || name2.contains(name1)) {
            return 0.8;
        }

        // 计算编辑距离
        int distance = calculateLevenshteinDistance(name1, name2);
        int maxLength = Math.max(name1.length(), name2.length());

        return Math.max(0.1, 1.0 - (double) distance / maxLength);
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
     * 创建匹配结果
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
        result.setSupplierCompanyId(product.getCompanyId());

        // 设置供应商信息（如果有的话）
        if (product.getCompanyId() != null) {
            result.setSupplierCompanyId(product.getCompanyId());
            // TODO: 获取供应商公司名称
            result.setSupplierCompanyName("供应商公司");
        }

        return result;
    }
}
