package com.yibang.erp.domain.dto;

import lombok.Data;

/**
 * 商品图片上传请求DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class ProductImageUploadRequest {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 是否设为主图
     */
    private Boolean isPrimary = false;

    /**
     * 排序顺序
     */
    private Integer sortOrder = 0;
}
