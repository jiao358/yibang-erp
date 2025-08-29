package com.yibang.erp.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yibang.erp.config.AIConfig;
import com.yibang.erp.domain.dto.DeepSeekChatRequest;
import com.yibang.erp.domain.dto.DeepSeekChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * DeepSeek API客户端
 * 负责与DeepSeek AI模型进行通信
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {
    
    private final AIConfig aiConfig;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    
    /**
     * 发送聊天请求到DeepSeek
     * @param request 聊天请求
     * @return 聊天响应
     */
    public Mono<DeepSeekChatResponse> chat(DeepSeekChatRequest request) {
        if (!aiConfig.getEnabled()) {
            return Mono.error(new RuntimeException("AI功能未启用"));
        }
        
        WebClient webClient = webClientBuilder
            .baseUrl(aiConfig.getBaseUrl())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.getApiKey())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
        
        return webClient.post()
            .uri("/v1/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(DeepSeekChatResponse.class)
            .timeout(Duration.ofMillis(aiConfig.getTimeout()))
            .doOnError(error -> log.error("DeepSeek API调用失败: {}", error.getMessage()))
            .doOnSuccess(response -> log.info("DeepSeek API调用成功，模型: {}", response.getModel()));
    }
    
    /**
     * 测试DeepSeek连接
     * @return 连接测试结果
     */
    public Mono<Boolean> testConnection() {
        DeepSeekChatRequest testRequest = new DeepSeekChatRequest();
        testRequest.setModel(aiConfig.getDefaultModel());
        testRequest.setMessages(new String[]{"Hello, this is a test message."});
        testRequest.setMaxTokens(10);
        testRequest.setTemperature(0.1);
        
        return chat(testRequest)
            .map(response -> true)
            .onErrorReturn(false);
    }
    
    /**
     * 处理订单相关的AI请求
     * @param orderContext 订单上下文信息
     * @param processType 处理类型
     * @return AI处理结果
     */
    public Mono<String> processOrder(String orderContext, String processType) {
        String prompt = buildOrderPrompt(orderContext, processType);
        
        DeepSeekChatRequest request = new DeepSeekChatRequest();
        request.setModel(aiConfig.getDefaultModel());
        request.setMessages(new String[]{prompt});
        request.setMaxTokens(aiConfig.getMaxTokens());
        request.setTemperature(aiConfig.getTemperature());
        
        return chat(request)
            .map(response -> response.getChoices()[0].getMessage().getContent())
            .onErrorReturn("AI处理失败，请检查配置或稍后重试");
    }
    
    /**
     * 构建订单处理提示词
     * @param orderContext 订单上下文
     * @param processType 处理类型
     * @return 提示词
     */
    private String buildOrderPrompt(String orderContext, String processType) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的ERP系统AI助手，专门处理订单相关业务。\n\n");
        prompt.append("订单上下文信息：\n").append(orderContext).append("\n\n");
        prompt.append("处理类型：").append(processType).append("\n\n");
        prompt.append("请根据订单信息和处理类型，提供专业的分析和建议。\n");
        prompt.append("要求：\n");
        prompt.append("1. 分析要准确、专业\n");
        prompt.append("2. 建议要具体、可操作\n");
        prompt.append("3. 语言要简洁、清晰\n");
        prompt.append("4. 考虑业务实际情况\n\n");
        prompt.append("请开始分析：");
        
        return prompt.toString();
    }
}
