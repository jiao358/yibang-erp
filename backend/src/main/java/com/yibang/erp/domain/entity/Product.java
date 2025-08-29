package com.yibang.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("products")
public class Product {

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品编码
     */
    @TableField("sku")
    private String sku;

    /**
     * 商品名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 品牌ID
     */
    @TableField("brand_id")
    private Long brandId;

    /**
     * 商品描述
     */
    @TableField("description")
    private String description;

    /**
     * 商品简介
     */
    @TableField("short_description")
    private String shortDescription;

    /**
     * 商品图片JSON数组
     */
    @TableField("images")
    private String images;

    /**
     * 商品规格JSON
     */
    @TableField("specifications")
    private String specifications;

    /**
     * 单位
     */
    @TableField("unit")
    private String unit;

    /**
     * 成本价
     */
    @TableField("cost_price")
    private BigDecimal costPrice;

    /**
     * 销售价
     */
    @TableField("selling_price")
    private BigDecimal sellingPrice;

    /**
     * 市场价
     */
    @TableField("market_price")
    private BigDecimal marketPrice;

    /**
     * 重量(kg)
     */
    @TableField("weight")
    private BigDecimal weight;

    /**
     * 尺寸信息JSON
     */
    @TableField("dimensions")
    private String dimensions;

    /**
     * 条形码
     */
    @TableField("barcode")
    private String barcode;

    /**
     * 海关编码
     */
    @TableField("hs_code")
    private String hsCode;

    /**
     * 原产国
     */
    @TableField("origin_country")
    private String originCountry;

    /**
     * 材质
     */
    @TableField("material")
    private String material;

    /**
     * 颜色
     */
    @TableField("color")
    private String color;

    /**
     * 尺寸
     */
    @TableField("size")
    private String size;

    /**
     * 商品标签JSON数组
     */
    @TableField("tags")
    private String tags;

    /**
     * 商品状态
     */
    @TableField("status")
    private ProductStatus status;

    /**
     * 审核状态
     */
    @TableField("approval_status")
    private ProductApprovalStatus approvalStatus;

    /**
     * 审核意见
     */
    @TableField("approval_comment")
    private String approvalComment;

    /**
     * 审核时间
     */
    @TableField("approval_at")
    private LocalDateTime approvalAt;

    /**
     * 审核人ID
     */
    @TableField("approval_by")
    private Long approvalBy;

    /**
     * 所属公司ID
     */
    @TableField("company_id")
    private Long companyId;

    /**
     * 是否推荐
     */
    @TableField("is_featured")
    private Boolean isFeatured;

    /**
     * 是否热销
     */
    @TableField("is_hot")
    private Boolean isHot;

    /**
     * 是否新品
     */
    @TableField("is_new")
    private Boolean isNew;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Long viewCount;

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
     * 检查商品是否可以编辑
     */
    public boolean isEditable() {
        return status != null && status.isEditable();
    }

    /**
     * 检查商品是否可以销售
     */
    public boolean isSellable() {
        return status != null && status.isSellable();
    }

    /**
     * 检查商品是否已审核
     */
    public boolean isApproved() {
        return approvalStatus != null && approvalStatus.isApproved();
    }

    /**
     * 检查商品是否被拒绝
     */
    public boolean isRejected() {
        return approvalStatus != null && approvalStatus.isRejected();
    }

    /**
     * 检查商品是否需要审核
     */
    public boolean needsApproval() {
        return approvalStatus == null || approvalStatus == ProductApprovalStatus.PENDING;
    }
}
