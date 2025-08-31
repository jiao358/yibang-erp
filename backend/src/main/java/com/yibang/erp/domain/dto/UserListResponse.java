package com.yibang.erp.domain.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户列表响应DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class UserListResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 个人公司名称（映射到real_name字段）
     */
    private String personalCompanyName;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 角色名
     */
    private String roleName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 所属公司ID（可以为空，表示个人用户）
     */
    private Long companyId;
    
    /**
     * 所属公司名称
     */
    private String companyName;
    
    /**
     * 所属公司类型
     */
    private String companyType;
    
    /**
     * 所属供应链公司ID
     */
    private Long supplierCompanyId;
    
    /**
     * 所属销售公司ID
     */
    private Long salesCompanyId;
    
    /**
     * 销售类型
     */
    private String salesType;
    
    /**
     * 价格分层等级ID
     */
    private Long priceTierId;
    
    /**
     * 价格分层名称
     */
    private String priceTierName;
    
    /**
     * 价格分层类型
     */
    private String priceTierType;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
