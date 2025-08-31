package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * 密码哈希
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 所属公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 所属供应链公司ID（可为空，表示个人销售）
     */
    @TableField("supplier_company_id")
    private Long supplierCompanyId;

    /**
     * 所属销售公司ID（可为空，表示个人销售）
     */
    @TableField("sales_company_id")
    private Long salesCompanyId;

    /**
     * 销售类型：COMPANY_SALES-公司销售，INDIVIDUAL_SALES-个人销售
     */
    @TableField("sales_type")
    private String salesType;

    /**
     * 价格分层等级ID
     */
    @TableField("price_tier_id")
    private Long priceTierId;

    /**
     * 部门
     */
    @TableField("department")
    private String department;
    


    /**
     * 职位
     */
    @TableField("position")
    private String position;

    /**
     * 状态：ACTIVE-激活, INACTIVE-未激活, LOCKED-锁定, PENDING-待审核
     */
    @TableField("status")
    private String status;

    /**
     * 最后登录时间
     */
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 登录尝试次数
     */
    @TableField("login_attempts")
    private Integer loginAttempts;

    /**
     * 锁定截止时间
     */
    @TableField("locked_until")
    private LocalDateTime lockedUntil;

    /**
     * 密码修改时间
     */
    @TableField("password_changed_at")
    private LocalDateTime passwordChangedAt;

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
     * 用户角色列表
     */
    @TableField(exist = false)
    private Set<Role> roles;
    
    /**
     * 用户权限列表
     */
    @TableField(exist = false)
    private Set<Permission> permissions;

    @TableField(exist = false)
    private String password;
}
