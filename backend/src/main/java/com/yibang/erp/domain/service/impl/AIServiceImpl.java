package com.yibang.erp.domain.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.yibang.erp.config.AIConfig;
import com.yibang.erp.domain.dto.AIConfigRequest;
import com.yibang.erp.domain.dto.AIConfigResponse;
import com.yibang.erp.domain.dto.AIOrderProcessRequest;
import com.yibang.erp.domain.dto.AIOrderProcessResult;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.AIService;
import com.yibang.erp.infrastructure.client.DeepSeekClient;
import com.yibang.erp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {
    
    private final AIConfig aiConfig;
    private final DeepSeekClient deepSeekClient;
    private final UserRepository userRepository;
    
    private static final AtomicLong PROCESS_ID_COUNTER = new AtomicLong(1);
    
    @Override
    @Transactional
    public AIOrderProcessResult processAIOrder(AIOrderProcessRequest request) {
        long startTime = System.currentTimeMillis();
        LocalDateTime processStartTime = LocalDateTime.now();
        
        AIOrderProcessResult result = new AIOrderProcessResult();
        result.setProcessId(PROCESS_ID_COUNTER.getAndIncrement());
        result.setOrderId(request.getOrderId());
//        result.setCompanyId(request.getCompanyId());
        result.setProcessType(request.getProcessType());
        result.setDescription(request.getDescription());
        result.setUserId(request.getUserId());
        result.setPriority(request.getPriority());
        result.setCustomParams(request.getCustomParams());
        result.setStartTime(processStartTime);
        result.setCreatedAt(processStartTime);
        
        try {
            // 获取用户信息
            User user = userRepository.selectById(request.getUserId());
            if (user != null) {
                result.setUserName(user.getUsername());
            }
            
            // 构建订单上下文
            String orderContext = buildOrderContext(request);
            
            // 调用AI处理
            String aiResult = deepSeekClient.processOrder(orderContext, request.getProcessType())
                .block(); // 同步等待结果
            
            if (aiResult != null && !aiResult.contains("AI处理失败")) {
                result.setStatus("SUCCESS");
                result.setResult(aiResult);
                result.setSuccess(true);
                result.setModelName(aiConfig.getDefaultModel());
            } else {
                result.setStatus("FAILED");
                result.setResult("AI处理失败");
                result.setSuccess(false);
                result.setErrorMessage(aiResult);
            }
            
        } catch (Exception e) {
            log.error("AI订单处理失败", e);
            result.setStatus("ERROR");
            result.setResult("处理异常");
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            LocalDateTime processEndTime = LocalDateTime.now();
            
            result.setEndTime(processEndTime);
            result.setProcessingTime(endTime - startTime);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public List<AIOrderProcessResult> batchProcessAIOrders(List<AIOrderProcessRequest> requests) {
        List<AIOrderProcessResult> results = new ArrayList<>();
        //todo 核心处理的逻辑 AI的逻辑 怎么映射到自己的order表和 orderItem表信息

        for (AIOrderProcessRequest request : requests) {
            try {
                AIOrderProcessResult result = processAIOrder(request);
                results.add(result);
            } catch (Exception e) {
                log.error("批量处理订单失败: {}", e.getMessage());
                AIOrderProcessResult errorResult = createErrorResult(request, e.getMessage());
                results.add(errorResult);
            }
        }
        
        return results;
    }
    
    @Override
    @Transactional
    public List<AIOrderProcessResult> processOrdersFromExcel(MultipartFile file, Long saleUserId) {
        List<AIOrderProcessRequest> requests = new ArrayList<>();
        
        try {
            EasyExcel.read(file.getInputStream(), new ReadListener<AIOrderProcessRequest>() {
                @Override
                public void invoke(AIOrderProcessRequest request, AnalysisContext context) {
                    request.setSalesId(saleUserId);
                    requests.add(request);
                }
                
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 所有数据解析完成
                    log.info("estela excel success ok!");


                }
            }).sheet().doRead();
            
            return batchProcessAIOrders(requests);
            
        } catch (IOException e) {
            log.error("Excel文件读取失败", e);
            throw new RuntimeException("Excel文件读取失败: " + e.getMessage());
        }
    }
    
    @Override
    public AIConfigResponse getAIConfig() {
        AIConfigResponse response = new AIConfigResponse();
        response.setBaseUrl(aiConfig.getBaseUrl());
        response.setApiKeyMasked(maskApiKey(aiConfig.getApiKey()));
        response.setDefaultModel(aiConfig.getDefaultModel());
        response.setMaxTokens(aiConfig.getMaxTokens());
        response.setTemperature(aiConfig.getTemperature());
        response.setEnabled(aiConfig.getEnabled());
        response.setTimeout(aiConfig.getTimeout());
        response.setConfigStatus(aiConfig.getEnabled() ? "ENABLED" : "DISABLED");
        response.setConfigured(aiConfig.getApiKey() != null && !aiConfig.getApiKey().isEmpty());
        
        return response;
    }
    
    @Override
    @Transactional
    public AIConfigResponse updateAIConfig(AIConfigRequest request) {
        // 验证用户权限（只有系统管理员可以配置）
        User user = userRepository.selectById(request.getConfigUserId());
        if (user == null || !hasAdminRole(user)) {
            throw new RuntimeException("只有系统管理员可以配置AI参数");
        }
        
        // 更新配置
        if (request.getBaseUrl() != null) {
            aiConfig.setBaseUrl(request.getBaseUrl());
        }
        if (request.getApiKey() != null) {
            aiConfig.setApiKey(request.getApiKey());
        }
        if (request.getDefaultModel() != null) {
            aiConfig.setDefaultModel(request.getDefaultModel());
        }
        if (request.getMaxTokens() != null) {
            aiConfig.setMaxTokens(request.getMaxTokens());
        }
        if (request.getTemperature() != null) {
            aiConfig.setTemperature(request.getTemperature());
        }
        if (request.getEnabled() != null) {
            aiConfig.setEnabled(request.getEnabled());
        }
        if (request.getTimeout() != null) {
            aiConfig.setTimeout(request.getTimeout());
        }
        
        AIConfigResponse response = getAIConfig();
        response.setConfigUserId(request.getConfigUserId());
        response.setConfigUserName(user.getUsername());
        response.setConfigTime(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        
        return response;
    }
    
    @Override
    public boolean testAIConnection() {
        try {
            return deepSeekClient.testConnection().block();
        } catch (Exception e) {
            log.error("AI连接测试失败", e);
            return false;
        }
    }
    
    @Override
    public Object getAIProcessStatistics(Long companyId) {
        // TODO: 实现统计信息查询
        return new Object();
    }
    
    @Override
    public List<AIOrderProcessResult> getAIProcessHistory(Long companyId, int page, int size) {
        // TODO: 实现历史记录查询
        return new ArrayList<>();
    }
    
    /**
     * 构建订单上下文信息
     */
    private String buildOrderContext(AIOrderProcessRequest request) {
        StringBuilder context = new StringBuilder();
        context.append("订单ID: ").append(request.getOrderId()).append("\n");
//        context.append("公司ID: ").append(request.getCompanyId()).append("\n");
        context.append("处理类型: ").append(request.getProcessType()).append("\n");
        context.append("描述: ").append(request.getDescription()).append("\n");
        context.append("优先级: ").append(request.getPriority()).append("\n");
        if (request.getCustomParams() != null) {
            context.append("自定义参数: ").append(request.getCustomParams()).append("\n");
        }
        return context.toString();
    }
    
    /**
     * 创建错误结果
     */
    private AIOrderProcessResult createErrorResult(AIOrderProcessRequest request, String errorMessage) {
        AIOrderProcessResult result = new AIOrderProcessResult();
        result.setProcessId(PROCESS_ID_COUNTER.getAndIncrement());
        result.setOrderId(request.getOrderId());
//        result.setCompanyId(request.getCompanyId());
        result.setProcessType(request.getProcessType());
        result.setStatus("ERROR");
        result.setResult("处理失败");
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        result.setCreatedAt(LocalDateTime.now());
        return result;
    }
    
    /**
     * 脱敏API密钥
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * 检查用户是否有管理员角色
     */
    private boolean hasAdminRole(User user) {
        // TODO: 实现角色检查逻辑
        return "admin".equalsIgnoreCase(user.getUsername());
    }
}
