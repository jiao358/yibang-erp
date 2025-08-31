package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 公司信息DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class CompanyInfo {
    
    /**
     * 公司ID
     */
    private Long id;
    
    /**
     * 公司名称
     */
    private String name;
    
    /**
     * 公司类型：SUPPLIER-供应链公司，SALES-销售公司
     */
    private String type;
    
    /**
     * 公司状态
     */
    private String status;
    
    /**
     * 公司描述
     */
    private String description;
}
