package com.yibang.erp.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yibang.erp.domain.dto.AIExcelRowData;
import com.yibang.erp.domain.dto.DeepSeekChatRequest;
import com.yibang.erp.domain.dto.DeepSeekChatResponse;
import com.yibang.erp.domain.service.AIExcelFieldRecognitionService;
import com.yibang.erp.infrastructure.client.DeepSeekClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Excel字段识别服务实现类
 * 使用DeepSeek AI模型进行智能字段识别
 */
@Slf4j
@Service
public class AIExcelFieldRecognitionServiceImpl implements AIExcelFieldRecognitionService {

    @Autowired
    private DeepSeekClient deepSeekClient;

    @Autowired
    private ObjectMapper objectMapper;

    // 预定义的字段映射规则（作为AI识别的备选方案）
    private static final Map<String, List<String>> FIELD_KEYWORDS = new HashMap<>();
    
    static {
        // 客户相关字段
        FIELD_KEYWORDS.put("customerName", Arrays.asList("客户名称", "客户名", "客户", "姓名", "名称"));
        FIELD_KEYWORDS.put("customerCode", Arrays.asList("客户编码", "客户编号", "编码", "编号", "代码"));
        FIELD_KEYWORDS.put("contactPerson", Arrays.asList("联系人", "联系人姓名", "联系人名", "联系"));
        FIELD_KEYWORDS.put("contactPhone", Arrays.asList("联系电话", "电话", "手机", "手机号", "联系方式"));
        FIELD_KEYWORDS.put("deliveryAddress", Arrays.asList("地址", "收货地址", "配送地址", "送货地址", "地址信息"));
        FIELD_KEYWORDS.put("expectedDeliveryDate", Arrays.asList("交货日期", "预计交货", "交货时间", "日期", "时间"));
        
        // 商品相关字段
        FIELD_KEYWORDS.put("productSku", Arrays.asList("SKU", "69码", "商品编码", "产品编码", "编码", "条码"));
        FIELD_KEYWORDS.put("productName", Arrays.asList("商品名称", "产品名称", "商品名", "产品名", "名称", "品名"));
        FIELD_KEYWORDS.put("productSpecification", Arrays.asList("规格", "商品规格", "产品规格", "规格型号", "型号"));
        FIELD_KEYWORDS.put("quantity", Arrays.asList("数量", "件数", "个数", "数量", "订购数量"));
        FIELD_KEYWORDS.put("unitPrice", Arrays.asList("单价", "价格", "单价", "单位价格", "售价"));
        FIELD_KEYWORDS.put("unit", Arrays.asList("单位", "计量单位", "单位", "件", "个", "套"));
        
        // 订单相关字段
        FIELD_KEYWORDS.put("orderType", Arrays.asList("订单类型", "类型", "订单种类", "种类"));
        FIELD_KEYWORDS.put("specialRequirements", Arrays.asList("特殊要求", "要求", "备注", "说明", "特殊说明"));
        FIELD_KEYWORDS.put("remarks", Arrays.asList("备注", "说明", "注释", "备注信息", "其他"));
    }

    @Override
    public FieldMappingResult recognizeColumnHeaders(List<String> columnHeaders) {
        try {
            log.info("开始识别Excel列标题，共{}列", columnHeaders.size());
            
            // 构建AI提示词
            String prompt = buildColumnRecognitionPrompt(columnHeaders);
            
            // 调用AI模型进行识别
            FieldMappingResult aiResult = callAIForColumnRecognition(prompt, columnHeaders);
            
            // 如果AI识别失败，使用规则识别作为备选
            if (aiResult == null || aiResult.getConfidence() < 0.5) {
                log.warn("AI识别置信度过低，使用规则识别作为备选");
                return fallbackRuleBasedRecognition(columnHeaders);
            }
            
            return aiResult;
            
        } catch (Exception e) {
            log.error("AI列标题识别失败，使用规则识别", e);
            return fallbackRuleBasedRecognition(columnHeaders);
        }
    }

    @Override
    public AIExcelRowData.RecognizedFields recognizeRowFields(AIExcelRowData rowData) {
        try {
            log.info("开始识别行数据字段，行号: {}", rowData.getRowNumber());
            
            // 构建AI提示词
            String prompt = buildRowFieldRecognitionPrompt(rowData);
            
            // 调用AI模型进行识别
            AIExcelRowData.RecognizedFields aiResult = callAIForRowFieldRecognition(prompt, rowData);
            
            // 如果AI识别失败，使用规则识别作为备选
            if (aiResult == null) {
                log.warn("AI行字段识别失败，使用规则识别作为备选");
                return fallbackRuleBasedRowRecognition(rowData);
            }
            
            return aiResult;
            
        } catch (Exception e) {
            log.error("AI行字段识别失败，使用规则识别", e);
            return fallbackRuleBasedRowRecognition(rowData);
        }
    }

    @Override
    public List<AIExcelRowData.RecognizedFields> batchRecognizeRowFields(List<AIExcelRowData> rowDataList) {
        log.info("开始批量识别行数据字段，共{}行", rowDataList.size());
        
        return rowDataList.parallelStream()
                .map(this::recognizeRowFields)
                .collect(Collectors.toList());
    }

    /**
     * 构建列标题识别的AI提示词
     */
    private String buildColumnRecognitionPrompt(List<String> columnHeaders) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个Excel数据处理专家。请分析以下Excel列标题，识别每个列对应的业务字段含义。\n\n");
        prompt.append("列标题列表：\n");
        
        for (int i = 0; i < columnHeaders.size(); i++) {
            prompt.append(i + 1).append(". ").append(columnHeaders.get(i)).append("\n");
        }
        
        prompt.append("\n请返回JSON格式的识别结果，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"columnMappings\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"columnIndex\": 0,\n");
        prompt.append("      \"originalHeader\": \"客户名称\",\n");
        prompt.append("      \"recognizedField\": \"customerName\",\n");
        prompt.append("      \"fieldType\": \"string\",\n");
        prompt.append("      \"confidence\": 0.95,\n");
        prompt.append("      \"description\": \"客户名称字段\"\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"confidence\": 0.9,\n");
        prompt.append("  \"reasoning\": \"识别依据说明\"\n");
        prompt.append("}\n\n");
        prompt.append("字段类型说明：\n");
        prompt.append("- customerName: 客户名称\n");
        prompt.append("- customerCode: 客户编码\n");
        prompt.append("- contactPerson: 联系人\n");
        prompt.append("- contactPhone: 联系电话\n");
        prompt.append("- deliveryAddress: 收货地址\n");
        prompt.append("- expectedDeliveryDate: 预计交货日期\n");
        prompt.append("- productSku: 商品SKU/69码\n");
        prompt.append("- productName: 商品名称\n");
        prompt.append("- productSpecification: 商品规格\n");
        prompt.append("- quantity: 数量\n");
        prompt.append("- unitPrice: 单价\n");
        prompt.append("- unit: 单位\n");
        prompt.append("- orderType: 订单类型\n");
        prompt.append("- specialRequirements: 特殊要求\n");
        prompt.append("- remarks: 备注\n");
        
        return prompt.toString();
    }

    /**
     * 构建行字段识别的AI提示词
     */
    private String buildRowFieldRecognitionPrompt(AIExcelRowData rowData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个Excel数据处理专家。请分析以下Excel行数据，识别并提取各个字段的值。\n\n");
        prompt.append("行号: ").append(rowData.getRowNumber()).append("\n");
        prompt.append("列标题: ").append(rowData.getColumnHeaders().values()).append("\n");
        prompt.append("行数据: ").append(rowData.getRawValues()).append("\n\n");
        
        prompt.append("请返回JSON格式的识别结果，格式如下：\n");
        prompt.append("{\n");
        prompt.append("  \"customerName\": \"客户名称值\",\n");
        prompt.append("  \"customerCode\": \"客户编码值\",\n");
        prompt.append("  \"contactPerson\": \"联系人值\",\n");
        prompt.append("  \"contactPhone\": \"联系电话值\",\n");
        prompt.append("  \"deliveryAddress\": \"收货地址值\",\n");
        prompt.append("  \"expectedDeliveryDate\": \"预计交货日期值\",\n");
        prompt.append("  \"productSku\": \"商品SKU值\",\n");
        prompt.append("  \"productName\": \"商品名称值\",\n");
        prompt.append("  \"productSpecification\": \"商品规格值\",\n");
        prompt.append("  \"quantity\": 数量值,\n");
        prompt.append("  \"unitPrice\": 单价值,\n");
        prompt.append("  \"unit\": \"单位值\",\n");
        prompt.append("  \"orderType\": \"订单类型值\",\n");
        prompt.append("  \"specialRequirements\": \"特殊要求值\",\n");
        prompt.append("  \"remarks\": \"备注值\"\n");
        prompt.append("  \"provinceName\": \"省份\"\n");
        prompt.append("  \"cityName\": \"城市\"\n");
        prompt.append("  \"districtName\": \"区\"\n");
        prompt.append("  \"sourceOrderId\": \"订单Id\"\n");
        prompt.append("  \"confidence\": \"置信度\"\n");
        prompt.append("}\n\n");
        prompt.append("注意：\n");
        prompt.append("1. 如果某个字段没有值，请设置为null\n");
        prompt.append("2. 数量字段应该是整数\n");
        prompt.append("3. 单价字段应该是数字\n");
        prompt.append("4. 日期字段格式为YYYY-MM-DD\n");
        prompt.append("5. confidence为你对本次服务转换的可信度评分0~1的区间\n");
        prompt.append("5.1 置信度说明：\n");
        prompt.append("- 0.9-1.0: 完全匹配或高度相似\n");
        prompt.append("- 0.7-0.89: 高度相关\n");
        prompt.append("- 0.5-0.69: 中等相关\n");
        prompt.append("- 0.3-0.49: 低度相关\n");
        prompt.append("- 0.0-0.29: 不相关\n");

        return prompt.toString();
    }

    /**
     * 调用AI进行列标题识别
     */
    private FieldMappingResult callAIForColumnRecognition(String prompt, List<String> columnHeaders) {
        try {
            log.info("调用AI进行列标题识别，列数: {}", columnHeaders.size());
            
            // 构建DeepSeek请求
            DeepSeekChatRequest request = new DeepSeekChatRequest();
            request.setModel("deepseek-chat");
            request.setMaxTokens(2000);
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
                log.info("AI响应: {}", aiResponse);
                
                // 解析AI响应
                return parseAIResponse(aiResponse, columnHeaders);
            } else {
                log.warn("AI响应为空或格式不正确");
                return null;
            }
            
        } catch (Exception e) {
            log.error("调用AI进行列标题识别失败", e);
            return null;
        }
    }

    /**
     * 调用AI进行行字段识别
     */
    private AIExcelRowData.RecognizedFields callAIForRowFieldRecognition(String prompt, AIExcelRowData rowData) {
        try {
            log.info("调用AI进行行字段识别，行号: {}", rowData.getRowNumber());
            
            // 构建DeepSeek请求
            DeepSeekChatRequest request = new DeepSeekChatRequest();
            request.setModel("deepseek-chat");
            request.setMaxTokens(2000);
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
                log.info("AI响应: {}", aiResponse);
                
                // 解析AI响应
                return parseAIRowResponse(aiResponse, rowData);
            } else {
                log.warn("AI响应为空或格式不正确");
                return null;
            }
            
        } catch (Exception e) {
            log.error("调用AI进行行字段识别失败", e);
            return null;
        }
    }

    /**
     * 解析AI列标题识别响应
     */
    private FieldMappingResult parseAIResponse(String aiResponse, List<String> columnHeaders) {
        try {
            // 尝试从AI响应中提取JSON
            String jsonContent = extractJsonFromResponse(aiResponse);
            if (jsonContent == null) {
                log.warn("无法从AI响应中提取JSON内容");
                return null;
            }
            
            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(jsonContent, Map.class);
            
            FieldMappingResult result = new FieldMappingResult();
            List<ColumnMapping> mappings = new ArrayList<>();
            
            // 解析列映射
            if (responseMap.containsKey("columnMappings")) {
                List<Map<String, Object>> columnMappings = (List<Map<String, Object>>) responseMap.get("columnMappings");
                
                for (Map<String, Object> mapping : columnMappings) {
                    ColumnMapping columnMapping = new ColumnMapping();
                    
                    if (mapping.containsKey("columnIndex")) {
                        columnMapping.setColumnIndex(((Number) mapping.get("columnIndex")).intValue());
                    }
                    if (mapping.containsKey("originalHeader")) {
                        columnMapping.setOriginalHeader((String) mapping.get("originalHeader"));
                    }
                    if (mapping.containsKey("recognizedField")) {
                        columnMapping.setRecognizedField((String) mapping.get("recognizedField"));
                    }
                    if (mapping.containsKey("fieldType")) {
                        columnMapping.setFieldType((String) mapping.get("fieldType"));
                    }
                    if (mapping.containsKey("confidence")) {
                        columnMapping.setConfidence(((Number) mapping.get("confidence")).doubleValue());
                    }
                    if (mapping.containsKey("description")) {
                        columnMapping.setDescription((String) mapping.get("description"));
                    }
                    
                    mappings.add(columnMapping);
                }
            }
            
            result.setColumnMappings(mappings);
            
            // 设置置信度和推理
            if (responseMap.containsKey("confidence")) {
                result.setConfidence(((Number) responseMap.get("confidence")).doubleValue());
            }
            if (responseMap.containsKey("reasoning")) {
                result.setReasoning((String) responseMap.get("reasoning"));
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("解析AI列标题识别响应失败", e);
            return null;
        }
    }

    /**
     * 解析AI行字段识别响应
     */
    private AIExcelRowData.RecognizedFields parseAIRowResponse(String aiResponse, AIExcelRowData rowData) {
        try {
            // 尝试从AI响应中提取JSON
            String jsonContent = extractJsonFromResponse(aiResponse);
            if (jsonContent == null) {
                log.warn("无法从AI响应中提取JSON内容");
                return null;
            }
            
            // 解析JSON响应
            Map<String, Object> responseMap = objectMapper.readValue(jsonContent, Map.class);
            
            AIExcelRowData.RecognizedFields fields = new AIExcelRowData.RecognizedFields();
            
            // 设置客户信息
            if (responseMap.containsKey("customerName")) {
                fields.setCustomerName((String) responseMap.get("customerName"));
            }
            if (responseMap.containsKey("customerCode")) {
                fields.setCustomerCode((String) responseMap.get("customerCode"));
            }
            if (responseMap.containsKey("contactPerson")) {
                fields.setContactPerson((String) responseMap.get("contactPerson"));
            }
            if (responseMap.containsKey("contactPhone")) {
                fields.setContactPhone((String) responseMap.get("contactPhone"));
            }
            if (responseMap.containsKey("deliveryAddress")) {
                fields.setDeliveryAddress((String) responseMap.get("deliveryAddress"));
            }
            if (responseMap.containsKey("expectedDeliveryDate")) {
                fields.setExpectedDeliveryDate((String) responseMap.get("expectedDeliveryDate"));
            }
            if(responseMap.containsKey("sourceOrderId")){
                fields.setSourceOrderId((String) responseMap.get("sourceOrderId"));
            }

            if(responseMap.containsKey("provinceName")) {
                fields.setProvinceName((String) responseMap.get("provinceName"));
            }
            if(responseMap.containsKey("cityName")){
                fields.setCityName((String) responseMap.get("cityName"));
            }
            if(responseMap.containsKey("districtName")){
                fields.setDistrictName((String) responseMap.get("districtName"));
            }

            if(responseMap.containsKey("confidence")){
                fields.setAiConfidence(((Number) responseMap.get("confidence")).doubleValue());
            }
            
            // 设置商品信息
            if (responseMap.containsKey("productSku")) {
                fields.setProductSku((String) responseMap.get("productSku"));
            }
            if (responseMap.containsKey("productName")) {
                fields.setProductName((String) responseMap.get("productName"));
            }
            if (responseMap.containsKey("productSpecification")) {
                fields.setProductSpecification((String) responseMap.get("productSpecification"));
            }
            if (responseMap.containsKey("quantity")) {
                Object quantity = responseMap.get("quantity");
                if (quantity instanceof Number) {
                    fields.setQuantity(((Number) quantity).intValue());
                }
            }
            if (responseMap.containsKey("unitPrice")) {
                Object unitPrice = responseMap.get("unitPrice");
                if (unitPrice instanceof Number) {
                    fields.setUnitPrice(((Number) unitPrice).doubleValue());
                }
            }
            if (responseMap.containsKey("unit")) {
                fields.setUnit((String) responseMap.get("unit"));
            }
            
            // 设置订单信息
            if (responseMap.containsKey("orderType")) {
                fields.setOrderType((String) responseMap.get("orderType"));
            }
            if (responseMap.containsKey("specialRequirements")) {
                fields.setSpecialRequirements((String) responseMap.get("specialRequirements"));
            }
            if (responseMap.containsKey("remarks")) {
                fields.setRemarks((String) responseMap.get("remarks"));
            }
            
            return fields;
            
        } catch (Exception e) {
            log.error("解析AI行字段识别响应失败", e);
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
     * 规则识别的备选方案
     */
    private FieldMappingResult fallbackRuleBasedRecognition(List<String> columnHeaders) {
        log.info("使用规则识别作为备选方案");
        
        List<ColumnMapping> mappings = new ArrayList<>();
        double totalConfidence = 0.0;
        
        for (int i = 0; i < columnHeaders.size(); i++) {
            String header = columnHeaders.get(i);
            ColumnMapping mapping = findBestFieldMatch(header);
            
            if (mapping != null) {
                mapping.setColumnIndex(i);
                mapping.setOriginalHeader(header);
                mappings.add(mapping);
                totalConfidence += mapping.getConfidence();
            }
        }
        
        FieldMappingResult result = new FieldMappingResult();
        result.setColumnMappings(mappings);
        result.setConfidence(mappings.isEmpty() ? 0.0 : totalConfidence / mappings.size());
        result.setReasoning("使用预定义规则进行字段匹配");
        
        return result;
    }

    /**
     * 规则识别的行字段备选方案
     */
    private AIExcelRowData.RecognizedFields fallbackRuleBasedRowRecognition(AIExcelRowData rowData) {
        log.info("使用规则识别作为行字段备选方案");
        
        AIExcelRowData.RecognizedFields fields = new AIExcelRowData.RecognizedFields();
        
        // 使用预定义规则进行字段识别
        for (Map.Entry<String, String> entry : rowData.getColumnHeaders().entrySet()) {
            String columnIndex = entry.getKey();
            String columnHeader = entry.getValue();
            String columnValue = rowData.getColumnValue(columnHeader);
            
            if (columnValue == null) continue;
            
            // 基于列标题进行字段识别
            if (isCustomerNameColumn(columnHeader)) {
                fields.setDistrictName(columnValue);
            } else if (isProductSkuColumn(columnHeader)) {
                fields.setProductSku(columnValue);
            } else if (isProductNameColumn(columnHeader)) {
                fields.setProductName(columnValue);
            } else if (isQuantityColumn(columnHeader)) {
                try {
                    fields.setQuantity(Integer.parseInt(columnValue.trim()));
                } catch (NumberFormatException e) {
                    log.warn("数量字段转换失败: {}", columnValue);
                }
            } else if (isUnitPriceColumn(columnHeader)) {
                try {
                    fields.setUnitPrice(Double.parseDouble(columnValue.trim()));
                } catch (NumberFormatException e) {
                    log.warn("单价字段转换失败: {}", columnValue);
                }
            } else if (isDeliveryAddressColumn(columnHeader)) {
                fields.setDeliveryAddress(columnValue);
            } else if (isExpectedDeliveryDateColumn(columnHeader)) {
                fields.setExpectedDeliveryDate(columnValue);
            }
        }
        
        return fields;
    }

    /**
     * 查找最佳字段匹配
     */
    private ColumnMapping findBestFieldMatch(String header) {
        if (header == null || header.trim().isEmpty()) {
            return null;
        }
        
        String normalizedHeader = header.trim().toLowerCase();
        double bestConfidence = 0.0;
        String bestField = null;
        String bestFieldType = null;
        
        for (Map.Entry<String, List<String>> entry : FIELD_KEYWORDS.entrySet()) {
            String fieldName = entry.getKey();
            List<String> keywords = entry.getValue();
            
            for (String keyword : keywords) {
                if (normalizedHeader.contains(keyword.toLowerCase())) {
                    double confidence = calculateKeywordConfidence(normalizedHeader, keyword);
                    if (confidence > bestConfidence) {
                        bestConfidence = confidence;
                        bestField = fieldName;
                        bestFieldType = getFieldType(fieldName);
                    }
                }
            }
        }
        
        if (bestField != null) {
            ColumnMapping mapping = new ColumnMapping();
            mapping.setRecognizedField(bestField);
            mapping.setFieldType(bestFieldType);
            mapping.setConfidence(bestConfidence);
            mapping.setDescription("基于关键词匹配识别");
            return mapping;
        }
        
        return null;
    }

    /**
     * 计算关键词匹配置信度
     */
    private double calculateKeywordConfidence(String header, String keyword) {
        if (header.equals(keyword)) {
            return 1.0; // 完全匹配
        }
        
        if (header.contains(keyword)) {
            double keywordRatio = (double) keyword.length() / header.length();
            return Math.min(0.9, 0.5 + keywordRatio * 0.4); // 0.5-0.9之间
        }
        
        return 0.0;
    }

    /**
     * 获取字段类型
     */
    private String getFieldType(String fieldName) {
        if (fieldName.contains("quantity")) {
            return "integer";
        } else if (fieldName.contains("unitPrice")) {
            return "decimal";
        } else if (fieldName.contains("expectedDeliveryDate")) {
            return "date";
        } else {
            return "string";
        }
    }

    // 字段识别规则方法
    private boolean isCustomerNameColumn(String header) {
        return header != null && (header.contains("客户") || header.contains("客户名称") || header.contains("客户名") || header.contains("收件") || header.contains("收件人"));
    }

    private boolean isProductSkuColumn(String header) {
        return header != null && (header.contains("SKU") || header.contains("69码") || header.contains("商品编码"));
    }

    private boolean isProductNameColumn(String header) {
        return header != null && (header.contains("商品") || header.contains("产品") || header.contains("名称"));
    }

    private boolean isQuantityColumn(String header) {
        return header != null && (header.contains("数量") || header.contains("件数"));
    }

    private boolean isUnitPriceColumn(String header) {
        return header != null && (header.contains("单价") || header.contains("价格"));
    }

    private boolean isDeliveryAddressColumn(String header) {
        return header != null && (header.contains("地址") || header.contains("收货地址"));
    }

    private boolean isExpectedDeliveryDateColumn(String header) {
        return header != null && (header.contains("日期") || header.contains("交货日期"));
    }
}
