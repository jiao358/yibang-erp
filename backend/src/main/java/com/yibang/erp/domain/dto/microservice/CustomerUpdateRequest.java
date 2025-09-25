package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 客户更新请求DTO
 * 用于HSF微服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class CustomerUpdateRequest {
    
    /**
     * 客户ID
     */
    @NotNull(message = "客户ID不能为空")
    private Long id;
    
    /**
     * 客户名称
     */
    private String name;
    
    /**
     * 手机号码
     */
    private String phone;
    
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
     * 联系邮箱
     */
    private String email;
    
    /**
     * 客户地址
     */
    private String address;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 客户类型
     */
    private String customerType;
    
    /**
     * 客户等级
     */
    private String customerLevel;
    
    /**
     * 状态
     */
    private String status;
}
