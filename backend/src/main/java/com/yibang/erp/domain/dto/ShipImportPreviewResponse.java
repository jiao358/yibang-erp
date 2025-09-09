package com.yibang.erp.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 发货导入预览响应DTO
 */
@Data
public class ShipImportPreviewResponse {

    /**
     * 预览数据列表
     */
    private List<ShipImportPreviewItem> data;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 有效数量
     */
    private Integer validCount;

    /**
     * 无效数量
     */
    private Integer invalidCount;

    /**
     * 发货导入预览项
     */
    @Data
    public static class ShipImportPreviewItem {
        private String platformOrderNo;
        private String trackingNumber;
        private String carrier;
        private String shippingMethod;
        private String shippingNotes;
        private Long warehouseId;
        private String warehouseCode;
        private String status; // valid/invalid
        private String errorMessage;
    }
}
