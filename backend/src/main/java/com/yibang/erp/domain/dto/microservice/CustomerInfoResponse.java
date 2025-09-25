package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户信息响应DTO
 * 用于HSF微服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class CustomerInfoResponse {
    
    /**
     * 客户ID
     */
    private Long id;
    
    /**
     * 客户编码
     */
    private String customerCode;
    
    /**
     * 客户名称
     */
    private String name;
    
    /**
     * 所属销售公司ID
     */
    private Long companyId;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 联系邮箱
     */
    private String contactEmail;
    
    /**
     * 客户地址
     */
    private String address;
    
    /**
     * 客户类型
     */
    private String customerType;
    
    /**
     * 客户等级
     */
    private String customerLevel;
    
    /**
     * 信用额度
     */
    private BigDecimal creditLimit;
    
    /**
     * 付款条件
     */
    private String paymentTerms;
    
    /**
     * 税号
     */
    private String taxNumber;
    
    /**
     * 银行账户
     */
    private String bankAccount;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 微信open_id
     */
    private String openId;
    
    /**
     * 微信union_id
     */
    private String unionId;
    
    /**
     * 省份
     */
    private String province;
    
    /**
     * 城市
     */
    private String city;
    
    /**
     * 性别
     */
    private String gender;
    
    /**
     * 国家
     */
    private String country;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
