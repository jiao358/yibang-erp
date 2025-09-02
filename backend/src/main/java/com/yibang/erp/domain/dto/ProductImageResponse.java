package com.yibang.erp.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品图片响应DTO
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
public class ProductImageResponse {

    /**
     * 图片ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 图片文件名
     */
    private String imageName;

    /**
     * 图片访问URL
     */
    private String imageUrl;

    /**
     * 图片文件大小（字节）
     */
    private Long imageSize;

    /**
     * 图片类型
     */
    private String imageType;

    /**
     * 图片宽度
     */
    private Integer imageWidth;

    /**
     * 图片高度
     */
    private Integer imageHeight;

    /**
     * 是否为主图
     */
    private Boolean isPrimary;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
