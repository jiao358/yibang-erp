package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品图片Repository
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface ProductImageRepository extends BaseMapper<ProductImage> {

    /**
     * 根据商品ID查询图片列表
     */
    @Select("SELECT * FROM product_images WHERE product_id = #{productId} AND status = 1 ORDER BY sort_order ASC, created_at ASC")
    List<ProductImage> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据商品ID查询主图
     */
    @Select("SELECT * FROM product_images WHERE product_id = #{productId} AND is_primary = 1 AND status = 1 LIMIT 1")
    ProductImage selectPrimaryByProductId(@Param("productId") Long productId);

    /**
     * 根据商品ID列表批量查询图片
     */
    @Select("<script>" +
            "SELECT * FROM product_images WHERE product_id IN " +
            "<foreach item='productId' collection='productIds' open='(' separator=',' close=')'>" +
            "#{productId}" +
            "</foreach>" +
            " AND status = 1 ORDER BY product_id, sort_order ASC" +
            "</script>")
    List<ProductImage> selectByProductIds(@Param("productIds") List<Long> productIds);

    /**
     * 统计商品图片数量
     */
    @Select("SELECT COUNT(*) FROM product_images WHERE product_id = #{productId} AND status = 1")
    Integer countByProductId(@Param("productId") Long productId);

    /**
     * 更新商品的所有图片为非主图
     */
    @Select("UPDATE product_images SET is_primary = 0 WHERE product_id = #{productId}")
    void clearPrimaryByProductId(@Param("productId") Long productId);

    /**
     * 软删除商品的所有图片
     */
    @Select("UPDATE product_images SET status = 0 WHERE product_id = #{productId}")
    void deleteByProductId(@Param("productId") Long productId);
}
