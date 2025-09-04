package com.yibang.erp.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * AI Excel行数据模型
 * 用于存储从Excel中提取的原始数据
 */
@Data
public class AIExcelRowData {

    /**
     * 行号
     */
    private Integer rowNumber;

    /**
     * 原始行数据（所有列的值）
     */
    private List<String> rawValues;

    /**
     * 列标题映射
     */
    private Map<String, String> columnHeaders;

    /**
     * 识别出的字段数据
     */
    private RecognizedFields recognizedFields;

    /**
     * 置信度评分
     */
    private Double confidence;

    /**
     * 识别出的字段内部类
     */
    @Data
    public static class RecognizedFields {
        // 客户信息
        private String customerCode;
        private String customerName;
        private String contactPerson;
        private String contactPhone;
        private String deliveryAddress;
        private String expectedDeliveryDate;
        private String provinceName;

        private String cityName;
        private String districtName;

        /**
         * 置信度评分
         */
        private Double aiConfidence;

        // 商品信息
        private String productSku;
        private String productName;
        private String productSpecification;
        private Integer quantity;
        private Double unitPrice;
        private String unit;
        private Long companyId;



        // 订单信息
        private String orderType;
        private String specialRequirements;
        private String remarks;
        //卖家留言
        private String salesNote;
        //买家留言
        private String buyerNote;

        private String sourceOrderId;

        // 扩展字段
        private Map<String, Object> extendedFields;
    }

    /**
     * 获取列值
     */
    public String getColumnValue(String columnName) {
        if (columnHeaders == null || rawValues == null) {
            return null;
        }
        
        Integer columnIndex = null;
        for (Map.Entry<String, String> entry : columnHeaders.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(columnName)) {
                // 找到列标题，获取对应的索引
                for (int i = 0; i < rawValues.size(); i++) {
                    if (i < rawValues.size()) {
                        columnIndex = i;
                        break;
                    }
                }
                break;
            }
        }
        
        if (columnIndex != null && columnIndex < rawValues.size()) {
            return rawValues.get(columnIndex);
        }
        
        return null;
    }

    /**
     * 设置列值
     */
    public void setColumnValue(String columnName, String value) {
        if (columnHeaders == null || rawValues == null) {
            return;
        }
        
        for (Map.Entry<String, String> entry : columnHeaders.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(columnName)) {
                Integer columnIndex = null;
                for (int i = 0; i < rawValues.size(); i++) {
                    if (i < rawValues.size()) {
                        columnIndex = i;
                        break;
                    }
                }
                
                if (columnIndex != null && columnIndex < rawValues.size()) {
                    rawValues.set(columnIndex, value);
                }
                break;
            }
        }
    }
}
