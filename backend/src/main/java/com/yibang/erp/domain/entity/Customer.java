package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("customers")
public class Customer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户编码
     */
    @TableField("customer_code")
    private String customerCode;

    /**
     * 客户名称
     */
    @TableField("name")
    private String name;

    /**
     * 所属销售公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 客户地址
     */
    @TableField("address")
    private String address;

    /**
     * 客户类型
     */
    @TableField("customer_type")
    private String customerType; // INDIVIDUAL, ENTERPRISE, WHOLESALE

    /**
     * 客户等级
     */
    @TableField("customer_level")
    private String customerLevel; // REGULAR, VIP, PREMIUM

    /**
     * 信用额度
     */
    @TableField("credit_limit")
    private BigDecimal creditLimit;

    /**
     * 付款条件
     */
    @TableField("payment_terms")
    private String paymentTerms;

    /**
     * 税号
     */
    @TableField("tax_number")
    private String taxNumber;

    /**
     * 银行账户
     */
    @TableField("bank_account")
    private String bankAccount;

    /**
     * 状态
     */
    @TableField("status")
    private String status; // ACTIVE, INACTIVE, BLOCKED

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField("updated_by")
    private Long updatedBy;

    /**
     * 微信open_id
     */
    @TableField("open_id")
    private String openId;

    /**
     * 微信union_id
     */
    @TableField("union_id")
    private String unionId;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 国家
     */
    @TableField("country")
    private String country;

    /**
     * 是否删除
     */
    @TableField("deleted")
    @TableLogic
    private Boolean deleted;
}
