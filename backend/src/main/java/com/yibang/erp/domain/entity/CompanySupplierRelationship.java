package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应链公司与销售公司关系实体类
 *
 * @author yibang-erp
 * @since 2025-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("company_supplier_relationships")
public class CompanySupplierRelationship {

    /**
     * 关系ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 供应链公司ID
     */
    @TableField("supplier_company_id")
    private Long supplierCompanyId;

    /**
     * 销售公司ID
     */
    @TableField("sales_company_id")
    private Long salesCompanyId;

    /**
     * 关系类型：DIRECT-直营，PARTNER-合作伙伴，FRANCHISE-加盟
     */
    @TableField("relationship_type")
    private String relationshipType;

    /**
     * 关系状态
     */
    @TableField("status")
    private String status;

    /**
     * 佣金比例
     */
    @TableField("commission_rate")
    private BigDecimal commissionRate;

    /**
     * 合作开始日期
     */
    @TableField("start_date")
    private LocalDate startDate;

    /**
     * 合作结束日期
     */
    @TableField("end_date")
    private LocalDate endDate;

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

    /**
     * 供应链公司信息（非数据库字段）
     */
    @TableField(exist = false)
    private Company supplierCompany;

    /**
     * 销售公司信息（非数据库字段）
     */
    @TableField(exist = false)
    private Company salesCompany;
}
