package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 商品图片实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product_images")
public class ProductImage {

    /**
     * 图片ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 图片文件名
     */
    @TableField("image_name")
    private String imageName;

    /**
     * 图片访问URL
     */
    @TableField("image_url")
    private String imageUrl;

    /**
     * 图片存储路径
     */
    @TableField("image_path")
    private String imagePath;

    /**
     * 图片文件大小（字节）
     */
    @TableField("image_size")
    private Long imageSize;

    /**
     * 图片类型（jpg, png, gif等）
     */
    @TableField("image_type")
    private String imageType;

    /**
     * 图片宽度
     */
    @TableField("image_width")
    private Integer imageWidth;

    /**
     * 图片高度
     */
    @TableField("image_height")
    private Integer imageHeight;

    /**
     * 是否为主图
     */
    @TableField("is_primary")
    private Boolean isPrimary;

    /**
     * 排序顺序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：1-正常，0-删除
     */
    @TableField("status")
    private Integer status;

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
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 更新人ID
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
