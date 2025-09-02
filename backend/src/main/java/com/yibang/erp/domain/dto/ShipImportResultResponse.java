package com.yibang.erp.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 发货导入结果响应DTO
 */
@Data
public class ShipImportResultResponse {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 错误详情列表
     */
    private List<ShipImportError> errors;

    /**
     * 发货导入错误详情
     */
    @Data
    public static class ShipImportError {
        private String platformOrderNo;
        private String errorMessage;
        private String errorCode;
    }
}
