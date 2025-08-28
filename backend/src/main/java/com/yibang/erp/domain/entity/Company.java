package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 公司实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("companies")
public class Company {

    /**
     * 公司ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公司名称
     */
    @TableField("name")
    private String name;

    /**
     * 业务类型：SUPPLIER-供应商, SALES-销售商
     */
    @TableField("type")
    private String type;

    /**
     * 状态：ACTIVE-激活, INACTIVE-未激活, SUSPENDED-暂停
     */
    @TableField("status")
    private String status;

    /**
     * 营业执照号
     */
    @TableField("business_license")
    private String businessLicense;

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
     * 公司地址
     */
    @TableField("address")
    private String address;

    /**
     * 公司描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
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
     * 逻辑删除标记
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
