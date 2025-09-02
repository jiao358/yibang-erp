package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.dto.CustomerMatchRequest;
import com.yibang.erp.domain.dto.CustomerMatchResult;
import com.yibang.erp.domain.entity.Customer;
import com.yibang.erp.domain.service.CustomerMatchingService;
import com.yibang.erp.infrastructure.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 客户匹配服务实现类
 * 实现智能客户匹配算法
 */
@Slf4j
@Service
public class CustomerMatchingServiceImpl implements CustomerMatchingService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public CustomerMatchResult matchByCode(String customerCode) {
        if (!StringUtils.hasText(customerCode)) {
            return createNoMatchResult("客户编码为空");
        }

        try {
            log.info("根据客户编码匹配客户: {}", customerCode);
            
            // 精确匹配客户编码
            Customer customer = customerRepository.selectOne(
                new QueryWrapper<Customer>()
                    .eq("customer_code", customerCode.trim())
                    .eq("deleted", false)
            );

            if (customer != null) {
                return createMatchResult(customer, 1.0, "CODE_EXACT", "客户编码完全匹配");
            }

            // 模糊匹配客户编码（包含关系）
            List<Customer> fuzzyMatches = customerRepository.selectList(
                new QueryWrapper<Customer>()
                    .like("customer_code", customerCode.trim())
                    .eq("deleted", false)
                    .last("LIMIT 5")
            );

            if (!fuzzyMatches.isEmpty()) {
                // 选择最匹配的结果
                Customer bestMatch = findBestCodeMatch(customerCode, fuzzyMatches);
                double confidence = calculateCodeConfidence(customerCode, bestMatch.getCustomerCode());
                return createMatchResult(bestMatch, confidence, "CODE_FUZZY", "客户编码模糊匹配");
            }

            return createNoMatchResult("未找到匹配的客户编码");

        } catch (Exception e) {
            log.error("客户编码匹配失败: {}", customerCode, e);
            return createNoMatchResult("客户编码匹配异常: " + e.getMessage());
        }
    }

    @Override
    public List<CustomerMatchResult> matchByName(String customerName) {
        if (!StringUtils.hasText(customerName)) {
            return Collections.emptyList();
        }

        try {
            log.info("根据客户名称匹配客户: {}", customerName);
            
            String normalizedName = customerName.trim().toLowerCase();
            
            // 精确匹配客户名称
            List<Customer> exactMatches = customerRepository.selectList(
                new QueryWrapper<Customer>()
                    .eq("name", customerName.trim())
                    .eq("deleted", false)
            );

            List<CustomerMatchResult> results = new ArrayList<>();
            
            // 添加精确匹配结果
            for (Customer customer : exactMatches) {
                results.add(createMatchResult(customer, 1.0, "NAME_EXACT", "客户名称完全匹配"));
            }

            // 模糊匹配客户名称
            List<Customer> fuzzyMatches = customerRepository.selectList(
                new QueryWrapper<Customer>()
                    .like("name", customerName.trim())
                    .eq("deleted", false)
                    .last("LIMIT 10")
            );

            // 添加模糊匹配结果
            for (Customer customer : fuzzyMatches) {
                if (!exactMatches.contains(customer)) {
                    double confidence = calculateNameConfidence(normalizedName, customer.getName().toLowerCase());
                    if (confidence >= 0.3) { // 设置最低置信度阈值
                        results.add(createMatchResult(customer, confidence, "NAME_FUZZY", "客户名称模糊匹配"));
                    }
                }
            }

            // 按置信度排序
            results.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
            
            return results;

        } catch (Exception e) {
            log.error("客户名称匹配失败: {}", customerName, e);
            return Collections.emptyList();
        }
    }

    @Override
    public CustomerMatchResult matchByPhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            return createNoMatchResult("联系电话为空");
        }

        try {
            log.info("根据联系电话匹配客户: {}", phone);
            
            String normalizedPhone = normalizePhone(phone);
            
            // 精确匹配联系电话
            Customer customer = customerRepository.selectOne(
                new QueryWrapper<Customer>()
                    .eq("contact_phone", normalizedPhone)
                    .eq("deleted", false)
            );

            if (customer != null) {
                return createMatchResult(customer, 1.0, "PHONE_EXACT", "联系电话完全匹配");
            }

            // 模糊匹配联系电话（包含关系）
            List<Customer> fuzzyMatches = customerRepository.selectList(
                new QueryWrapper<Customer>()
                    .like("contact_phone", normalizedPhone)
                    .eq("deleted", false)
                    .last("LIMIT 5")
            );

            if (!fuzzyMatches.isEmpty()) {
                // 选择最匹配的结果
                Customer bestMatch = findBestPhoneMatch(normalizedPhone, fuzzyMatches);
                double confidence = calculatePhoneConfidence(normalizedPhone, bestMatch.getContactPhone());
                return createMatchResult(bestMatch, confidence, "PHONE_FUZZY", "联系电话模糊匹配");
            }

            return createNoMatchResult("未找到匹配的联系电话");

        } catch (Exception e) {
            log.error("联系电话匹配失败: {}", phone, e);
            return createNoMatchResult("联系电话匹配异常: " + e.getMessage());
        }
    }

    @Override
    public CustomerMatchResult smartMatch(String customerCode, String customerName, String phone) {
        log.info("智能客户匹配 - 编码: {}, 名称: {}, 电话: {}", customerCode, customerName, phone);
        
        // 优先使用客户编码匹配
        if (StringUtils.hasText(customerCode)) {
            CustomerMatchResult codeResult = matchByCode(customerCode);
            if (codeResult.isMatched() && codeResult.getConfidence() >= 0.8) {
                log.info("客户编码匹配成功，置信度: {}", codeResult.getConfidence());
                return codeResult;
            }
        }

        // 客户编码匹配失败或置信度过低，使用名称匹配
        if (StringUtils.hasText(customerName)) {
            List<CustomerMatchResult> nameResults = matchByName(customerName);
            if (!nameResults.isEmpty()) {
                CustomerMatchResult bestNameResult = nameResults.get(0);
                if (bestNameResult.getConfidence() >= 0.6) {
                    log.info("客户名称匹配成功，置信度: {}", bestNameResult.getConfidence());
                    return bestNameResult;
                }
            }
        }

        // 名称匹配失败，使用电话匹配
        if (StringUtils.hasText(phone)) {
            CustomerMatchResult phoneResult = matchByPhone(phone);
            if (phoneResult.isMatched() && phoneResult.getConfidence() >= 0.7) {
                log.info("联系电话匹配成功，置信度: {}", phoneResult.getConfidence());
                return phoneResult;
            }
        }

        // 如果多个字段都有，尝试组合匹配
        if (StringUtils.hasText(customerCode) && StringUtils.hasText(customerName)) {
            CustomerMatchResult combinedResult = tryCombinedMatch(customerCode, customerName, phone);
            if (combinedResult.isMatched()) {
                log.info("组合匹配成功，置信度: {}", combinedResult.getConfidence());
                return combinedResult;
            }
        }

        // 所有匹配都失败
        log.warn("智能客户匹配失败 - 编码: {}, 名称: {}, 电话: {}", customerCode, customerName, phone);
        return createNoMatchResult("客户编码、名称和电话都无法匹配到有效客户");
    }

    @Override
    public List<CustomerMatchResult> batchMatch(List<CustomerMatchRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("批量客户匹配，请求数量: {}", requests.size());
        
        return requests.parallelStream()
                .map(request -> {
                    switch (request.getMatchType()) {
                        case "CODE_ONLY":
                            return matchByCode(request.getCustomerCode());
                        case "NAME_ONLY":
                            List<CustomerMatchResult> nameResults = matchByName(request.getCustomerName());
                            return nameResults.isEmpty() ? createNoMatchResult("名称匹配失败") : nameResults.get(0);
                        case "PHONE_ONLY":
                            return matchByPhone(request.getPhone());
                        case "SMART_MATCH":
                        default:
                            return smartMatch(request.getCustomerCode(), request.getCustomerName(), request.getPhone());
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerMatchSuggestion> getMatchSuggestions(String customerInfo) {
        if (!StringUtils.hasText(customerInfo)) {
            return Collections.emptyList();
        }

        try {
            log.info("获取客户匹配建议: {}", customerInfo);
            
            String normalizedInfo = customerInfo.trim().toLowerCase();
            List<CustomerMatchSuggestion> suggestions = new ArrayList<>();

            // 基于客户编码的建议
            if (normalizedInfo.length() >= 3) {
                List<Customer> codeSuggestions = customerRepository.selectList(
                    new QueryWrapper<Customer>()
                        .like("customer_code", normalizedInfo)
                        .eq("deleted", false)
                        .last("LIMIT 3")
                );

                for (Customer customer : codeSuggestions) {
                    CustomerMatchSuggestion suggestion = new CustomerMatchSuggestion();
                    suggestion.setCustomerId(customer.getId());
                    suggestion.setCustomerName(customer.getName());
                    suggestion.setCustomerCode(customer.getCustomerCode());
                    suggestion.setContactPerson(customer.getContactPerson());
                    suggestion.setContactPhone(customer.getContactPhone());
                    suggestion.setConfidence(0.7);
                    suggestion.setReason("客户编码部分匹配");
                    suggestion.setSuggestion("请检查客户编码是否完整");
                    suggestions.add(suggestion);
                }
            }

            // 基于客户名称的建议
            if (normalizedInfo.length() >= 2) {
                List<Customer> nameSuggestions = customerRepository.selectList(
                    new QueryWrapper<Customer>()
                        .like("name", normalizedInfo)
                        .eq("deleted", false)
                        .last("LIMIT 3")
                );

                for (Customer customer : nameSuggestions) {
                    CustomerMatchSuggestion suggestion = new CustomerMatchSuggestion();
                    suggestion.setCustomerId(customer.getId());
                    suggestion.setCustomerName(customer.getName());
                    suggestion.setCustomerCode(customer.getCustomerCode());
                    suggestion.setContactPerson(customer.getContactPerson());
                    suggestion.setContactPhone(customer.getContactPhone());
                    suggestion.setConfidence(0.6);
                    suggestion.setReason("客户名称部分匹配");
                    suggestion.setSuggestion("请检查客户名称是否准确");
                    suggestions.add(suggestion);
                }
            }

            // 去重并按置信度排序
            suggestions = suggestions.stream()
                    .collect(Collectors.toMap(
                        CustomerMatchSuggestion::getCustomerId,
                        suggestion -> suggestion,
                        (existing, replacement) -> existing.getConfidence() > replacement.getConfidence() ? existing : replacement
                    ))
                    .values()
                    .stream()
                    .sorted((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()))
                    .collect(Collectors.toList());

            return suggestions;

        } catch (Exception e) {
            log.error("获取客户匹配建议失败: {}", customerInfo, e);
            return Collections.emptyList();
        }
    }

    /**
     * 尝试组合匹配（编码 + 名称 + 电话）
     */
    private CustomerMatchResult tryCombinedMatch(String customerCode, String customerName, String phone) {
        try {
            // 查找同时满足多个条件的客户
            QueryWrapper<Customer> queryWrapper = new QueryWrapper<Customer>()
                .eq("deleted", false);

            if (StringUtils.hasText(customerCode)) {
                queryWrapper.like("customer_code", customerCode.trim());
            }
            if (StringUtils.hasText(customerName)) {
                queryWrapper.like("name", customerName.trim());
            }
            if (StringUtils.hasText(phone)) {
                queryWrapper.like("contact_phone", normalizePhone(phone));
            }

            List<Customer> combinedMatches = customerRepository.selectList(queryWrapper);

            if (!combinedMatches.isEmpty()) {
                Customer bestMatch = findBestCombinedMatch(customerCode, customerName, phone, combinedMatches);
                double confidence = calculateCombinedConfidence(customerCode, customerName, phone, bestMatch);
                return createMatchResult(bestMatch, confidence, "COMBINED_MATCH", "多字段组合匹配");
            }

            return createNoMatchResult("组合匹配失败");

        } catch (Exception e) {
            log.error("组合匹配失败", e);
            return createNoMatchResult("组合匹配异常: " + e.getMessage());
        }
    }

    /**
     * 查找最佳编码匹配
     */
    private Customer findBestCodeMatch(String targetCode, List<Customer> candidates) {
        return candidates.stream()
                .max(Comparator.comparingDouble(customer -> calculateCodeConfidence(targetCode, customer.getCustomerCode())))
                .orElse(candidates.get(0));
    }

    /**
     * 查找最佳电话匹配
     */
    private Customer findBestPhoneMatch(String targetPhone, List<Customer> candidates) {
        return candidates.stream()
                .max(Comparator.comparingDouble(customer -> calculatePhoneConfidence(targetPhone, customer.getContactPhone())))
                .orElse(candidates.get(0));
    }

    /**
     * 查找最佳组合匹配
     */
    private Customer findBestCombinedMatch(String customerCode, String customerName, String phone, List<Customer> candidates) {
        return candidates.stream()
                .max(Comparator.comparingDouble(customer -> 
                    calculateCombinedConfidence(customerCode, customerName, phone, customer)))
                .orElse(candidates.get(0));
    }

    /**
     * 计算编码匹配置信度
     */
    private double calculateCodeConfidence(String targetCode, String customerCode) {
        if (targetCode.equals(customerCode)) {
            return 1.0;
        }
        
        if (customerCode.contains(targetCode)) {
            return 0.8;
        }
        
        if (targetCode.contains(customerCode)) {
            return 0.7;
        }
        
        // 计算编辑距离
        int distance = calculateLevenshteinDistance(targetCode, customerCode);
        int maxLength = Math.max(targetCode.length(), customerCode.length());
        
        return Math.max(0.1, 1.0 - (double) distance / maxLength);
    }

    /**
     * 计算名称匹配置信度
     */
    private double calculateNameConfidence(String targetName, String customerName) {
        if (targetName.equals(customerName)) {
            return 1.0;
        }
        
        if (customerName.contains(targetName)) {
            return 0.9;
        }
        
        if (targetName.contains(customerName)) {
            return 0.8;
        }
        
        // 计算编辑距离
        int distance = calculateLevenshteinDistance(targetName, customerName);
        int maxLength = Math.max(targetName.length(), customerName.length());
        
        return Math.max(0.1, 1.0 - (double) distance / maxLength);
    }

    /**
     * 计算电话匹配置信度
     */
    private double calculatePhoneConfidence(String targetPhone, String customerPhone) {
        if (targetPhone.equals(customerPhone)) {
            return 1.0;
        }
        
        if (customerPhone.contains(targetPhone)) {
            return 0.8;
        }
        
        if (targetPhone.contains(customerPhone)) {
            return 0.7;
        }
        
        // 计算编辑距离
        int distance = calculateLevenshteinDistance(targetPhone, customerPhone);
        int maxLength = Math.max(targetPhone.length(), customerPhone.length());
        
        return Math.max(0.1, 1.0 - (double) distance / maxLength);
    }

    /**
     * 计算组合匹配置信度
     */
    private double calculateCombinedConfidence(String customerCode, String customerName, String phone, Customer customer) {
        double codeConfidence = 0.0;
        double nameConfidence = 0.0;
        double phoneConfidence = 0.0;
        
        if (StringUtils.hasText(customerCode)) {
            codeConfidence = calculateCodeConfidence(customerCode, customer.getCustomerCode());
        }
        if (StringUtils.hasText(customerName)) {
            nameConfidence = calculateNameConfidence(customerName.toLowerCase(), customer.getName().toLowerCase());
        }
        if (StringUtils.hasText(phone)) {
            phoneConfidence = calculatePhoneConfidence(normalizePhone(phone), customer.getContactPhone());
        }
        
        // 加权平均，编码权重最高，名称次之，电话最低
        int validFields = 0;
        double totalConfidence = 0.0;
        
        if (codeConfidence > 0) {
            totalConfidence += codeConfidence * 0.5;
            validFields++;
        }
        if (nameConfidence > 0) {
            totalConfidence += nameConfidence * 0.3;
            validFields++;
        }
        if (phoneConfidence > 0) {
            totalConfidence += phoneConfidence * 0.2;
            validFields++;
        }
        
        return validFields > 0 ? totalConfidence : 0.0;
    }

    /**
     * 标准化电话号码（去除空格、横线等）
     */
    private String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[\\s\\-\\+\\(\\)]", "");
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
    private CustomerMatchResult createMatchResult(Customer customer, double confidence, String matchType, String matchReason) {
        CustomerMatchResult result = new CustomerMatchResult();
        result.setMatched(true);
        result.setCustomerId(customer.getId());
        result.setCustomerName(customer.getName());
        result.setCustomerCode(customer.getCustomerCode());
        result.setContactPerson(customer.getContactPerson());
        result.setContactPhone(customer.getContactPhone());
        result.setAddress(customer.getAddress());
        result.setCustomerType(customer.getCustomerType());
        result.setConfidence(confidence);
        result.setMatchType(matchType);
        result.setMatchReason(matchReason);
        result.setCustomerStatus(customer.getStatus());
        
        return result;
    }

    /**
     * 创建匹配失败的结果
     */
    private CustomerMatchResult createNoMatchResult(String reason) {
        CustomerMatchResult result = new CustomerMatchResult();
        result.setMatched(false);
        result.setConfidence(0.0);
        result.setMatchReason(reason);
        result.setSuggestion("请检查客户信息是否正确，或联系管理员添加新客户");
        return result;
    }
}
