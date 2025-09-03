package com.yibang.erp.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yibang.erp.config.AIConfig;
import com.yibang.erp.domain.dto.DeepSeekChatRequest;
import com.yibang.erp.domain.dto.DeepSeekChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

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
    @Autowired
    @Lazy
    private WebClient webClient;
    @Lazy
    @Bean
    public WebClient deepSeekWebClient() {
        if (!aiConfig.getEnabled()) {
            return null;
        }

        return webClientBuilder
                .baseUrl(aiConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 发送聊天请求到DeepSeek
     * @param request 聊天请求
     * @return 聊天响应
     */
    public Mono<DeepSeekChatResponse> chat(DeepSeekChatRequest request) {
        if (!aiConfig.getEnabled()) {
            return Mono.error(new RuntimeException("AI功能未启用"));
        }


        
        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(DeepSeekChatResponse.class)
//            .timeout(Duration.ofMillis(aiConfig.getTimeout()))
                .timeout(Duration.ofMillis(30000*24L)).retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))

                        .doOnError(error ->{
                            log.error("DeepSeek API调用失败: {}", error.getMessage());
                            if (error instanceof WebClientRequestException) {
                                log.error("网络连接失败，请检查网络配置和DNS: {}", error.getMessage());
                            } else {
                                log.error("DeepSeek API调用失败: {}", error.getMessage());
                            }


                        } )
            .doOnSuccess(response -> log.info("DeepSeek API调用成功，模型: {}", response.getModel()));
    }
    
    /**
     * 测试DeepSeek连接
     * @return 连接测试结果
     */
    public Mono<Boolean> testConnection() {
        DeepSeekChatRequest testRequest = new DeepSeekChatRequest();
        testRequest.setModel(aiConfig.getDefaultModel());


//        testRequest.setMessages(new String[]{"Hello, this is a test message."});

        DeepSeekChatRequest.DeepSeekMessage message = new DeepSeekChatRequest.DeepSeekMessage();
        message.setRole("user");
        message.setContent("Hello, this is a test message.");
        testRequest.setMessages(new DeepSeekChatRequest.DeepSeekMessage[]{message});


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
//        request.setMessages(new String[]{prompt});

        // 修正消息格式
        DeepSeekChatRequest.DeepSeekMessage message = new DeepSeekChatRequest.DeepSeekMessage();
        message.setRole("user");
        message.setContent(prompt);
        request.setMessages(new DeepSeekChatRequest.DeepSeekMessage[]{message});



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
    
    /**
     * 地址解析处理
     * @param address 原始地址
     * @return 地址解析结果
     */
    public Mono<String> parseAddress(String address) {
        String prompt = buildAddressParsePrompt(address);
        
        DeepSeekChatRequest request = new DeepSeekChatRequest();
        request.setModel(aiConfig.getDefaultModel());
//        request.setMessages(new String[]{prompt});
        // 修正消息格式
        DeepSeekChatRequest.DeepSeekMessage message = new DeepSeekChatRequest.DeepSeekMessage();
        message.setRole("user");
        message.setContent(prompt);
        request.setMessages(new DeepSeekChatRequest.DeepSeekMessage[]{message});



        request.setMaxTokens(500);
        request.setTemperature(0.1);

        return chat(request)
            .map(response -> response.getChoices()[0].getMessage().getContent())
            .onErrorReturn("AI地址解析失败，请检查配置或稍后重试");
    }
    
    /**
     * 构建地址解析提示词
     * @param address 原始地址
     * @return 提示词
     */
    private String buildAddressParsePrompt(String address) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的地址解析AI助手。请将以下地址解析为结构化的信息：\n\n");
        prompt.append("原始地址：").append(address).append("\n\n");
        prompt.append("请严格按照以下JSON格式返回结果，不要包含任何其他文字：\n");
        prompt.append("{\n");
        prompt.append("  \"provinceName\": \"省份名称\",\n");
        prompt.append("  \"cityName\": \"城市名称\",\n");
        prompt.append("  \"districtName\": \"区域名称\",\n");
        prompt.append("  \"confidence\": 0.95,\n");
        prompt.append("  \"fixAddress\": \"修复后的完整地址\"\n");
        prompt.append("}\n\n");
        prompt.append("要求：\n");
        prompt.append("1. 省份名称要准确，如：广东省、北京市、上海市等\n");
        prompt.append("2. 城市名称要准确，如：深圳市、广州市、杭州市等\n");
        prompt.append("3. 区域名称要准确，如：南山区、天河区、西湖区等\n");
        prompt.append("4. 置信度范围0-1，表示解析的准确程度\n");
        prompt.append("5. 修复后的地址要完整、规范\n");
        prompt.append("6. 如果无法解析某个字段，请设为null\n\n");
        prompt.append("请开始解析：");
        
        return prompt.toString();
    }
}
