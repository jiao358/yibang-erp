package com.yibang.erp.domain.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 订单导出数据DTO
 */
@Data
public class OrderExportData {

    @ExcelProperty("平台订单号")
    private String platformOrderNo;

    @ExcelProperty("店铺编码")
    private String shopCode;

    @ExcelProperty("订单类型")
    private String orderType;

    @ExcelProperty("配送方式")
    private String deliveryType;

    @ExcelProperty("买家留言")
    private String buyerNote;

    @ExcelProperty("卖家留言")
    private String salesNote;

    @ExcelProperty("平台创建时间")
    private String orderCreateTime;

    @ExcelProperty("付款时间")
    private String payTime;



    @ExcelProperty("客户名称")
    private String customerName;

    @ExcelProperty("运费")
    private String shippingAmount;

    @ExcelProperty("平台订单行号")
    private String platformOrderId;

    @ExcelProperty("商品编码")
    private String productCode;

    @ExcelProperty("商品名称")
    private String productName;

    @ExcelProperty("数量")
    private Integer quantity;

    @ExcelProperty("收货人省份")
    private String deliveryProvince;

    @ExcelProperty("收货人城市")
    private String deliveryCity;

    @ExcelProperty("收货人区/县")
    private String deliveryDistrict;

    @ExcelProperty("收货人详细地址")
    private String deliveryAddress;

    @ExcelProperty("收货人手机号")
    private String deliveryPhone;

    @ExcelProperty("收货人电话")
    private String deliveryTel;

    @ExcelProperty("收货人姓名")
    private String deliveryContact;

    @ExcelProperty("收货人邮编")
    private String deliveryPostcode;

    @ExcelProperty("指定承运商")
    private String carrier;

    @ExcelProperty("平台属性")
    private String platformAttribute;
}
