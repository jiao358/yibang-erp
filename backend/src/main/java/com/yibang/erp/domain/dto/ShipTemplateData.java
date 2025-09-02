package com.yibang.erp.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 发货模板数据DTO
 */
@Data
public class ShipTemplateData {

    @ExcelProperty("平台订单号")
    private String platformOrderNo;

    @ExcelProperty("物流单号")
    private String trackingNumber;

    @ExcelProperty("物流公司")
    private String carrier;

    @ExcelProperty("发货方式")
    private String shippingMethod;

    @ExcelProperty("发货备注")
    private String shippingNotes;

    @ExcelProperty("仓库ID")
    private Long warehouseId;
}
