package com.yibang.erp.domain.dto.microservice;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 客户注册请求DTO
 * 用于HSF微服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class CustomerRegisterRequest {
    
    /**
     * 客户名称
     */
    @NotBlank(message = "客户名称不能为空")
    private String name;
    
    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    private String phone;
    
    /**
     * 微信open_id
     */
    @NotBlank(message = "微信open_id不能为空")
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
     * 所属销售公司ID（可选，如果不提供则使用默认公司）
     */
    private Long companyId;
}
