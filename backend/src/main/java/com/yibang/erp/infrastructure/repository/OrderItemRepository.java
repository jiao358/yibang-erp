package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单商品Repository接口
 */
@Mapper
public interface OrderItemRepository extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单商品列表
     */
    @Select("SELECT * FROM order_items WHERE order_id = #{orderId} ORDER BY id ASC")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据平台订单号查询订单商品列表
     */
    @Select("SELECT oi.* FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.platform_order_id = #{platformOrderNo} " +
            "ORDER BY oi.id ASC")
    List<OrderItem> selectByPlatformOrderNo(@Param("platformOrderNo") String platformOrderNo);

    /**
     * 根据商品ID查询订单商品列表
     */
    @Select("SELECT * FROM order_items WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<OrderItem> selectByProductId(@Param("productId") Long productId);

    /**
     * 批量插入订单商品
     */
    @Insert("<script>" +
            "INSERT INTO order_items (order_id, product_id, sku, product_name, product_specifications, " +
            "unit_price, quantity, unit, discount_rate, discount_amount, tax_rate, tax_amount, subtotal, " +
            "ai_mapped_product_id, ai_confidence, ai_processing_status, ai_processing_result, created_at, updated_at) " +
            "VALUES " +
            "<foreach collection='items' item='item' separator=','>" +
            "(#{item.orderId}, #{item.productId}, #{item.sku}, #{item.productName}, #{item.productSpecifications}, " +
            "#{item.unitPrice}, #{item.quantity}, #{item.unit}, #{item.discountRate}, #{item.discountAmount}, " +
            "#{item.taxRate}, #{item.taxAmount}, #{item.subtotal}, #{item.aiMappedProductId}, #{item.aiConfidence}, " +
            "#{item.aiProcessingStatus}, #{item.aiProcessingResult}, #{item.createdAt}, #{item.updatedAt})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 根据订单ID删除订单商品
     */
    @Delete("DELETE FROM order_items WHERE order_id = #{orderId}")
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据平台订单号删除订单商品
     */
    @Delete("DELETE oi FROM order_items oi " +
            "JOIN orders o ON oi.order_id = o.id " +
            "WHERE o.platform_order_id = #{platformOrderNo}")
    int deleteByPlatformOrderNo(@Param("platformOrderNo") String platformOrderNo);
}
