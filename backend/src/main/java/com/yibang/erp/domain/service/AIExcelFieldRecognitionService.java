package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.AIExcelRowData;

import java.util.List;

/**
 * AI Excel字段识别服务接口
 * 负责使用AI模型识别Excel中的字段含义
 */
public interface AIExcelFieldRecognitionService {

    /**
     * 识别Excel列标题的字段含义
     * @param columnHeaders 列标题列表
     * @return 字段映射结果
     */
    FieldMappingResult recognizeColumnHeaders(List<String> columnHeaders);

    /**
     * 识别单行数据的字段值
     * @param rowData 行数据
     * @return 识别后的字段数据
     */
    AIExcelRowData.RecognizedFields recognizeRowFields(AIExcelRowData rowData);

    /**
     * 批量识别多行数据
     * @param rowDataList 行数据列表
     * @return 识别后的字段数据列表
     */
    List<AIExcelRowData.RecognizedFields> batchRecognizeRowFields(List<AIExcelRowData> rowDataList);

    /**
     * 字段映射结果
     */
    class FieldMappingResult {
        private List<ColumnMapping> columnMappings;
        private double confidence;
        private String reasoning;

        // Getters and setters
        public List<ColumnMapping> getColumnMappings() { return columnMappings; }
        public void setColumnMappings(List<ColumnMapping> columnMappings) { this.columnMappings = columnMappings; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getReasoning() { return reasoning; }
        public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    }

    /**
     * 列映射信息
     */
    class ColumnMapping {
        private int columnIndex;
        private String originalHeader;
        private String recognizedField;
        private String fieldType;
        private double confidence;
        private String description;

        // Getters and setters
        public int getColumnIndex() { return columnIndex; }
        public void setColumnIndex(int columnIndex) { this.columnIndex = columnIndex; }
        public String getOriginalHeader() { return originalHeader; }
        public void setOriginalHeader(String originalHeader) { this.originalHeader = originalHeader; }
        public String getRecognizedField() { return recognizedField; }
        public void setRecognizedField(String recognizedField) { this.recognizedField = recognizedField; }
        public String getFieldType() { return fieldType; }
        public void setFieldType(String fieldType) { this.fieldType = fieldType; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
