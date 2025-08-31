package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单商品Repository接口
 */
@Mapper
public interface OrderItemRepository extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单商品列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据平台订单号查询订单商品列表
     */
    List<OrderItem> selectByPlatformOrderNo(@Param("platformOrderNo") String platformOrderNo);

    /**
     * 根据商品ID查询订单商品列表
     */
    List<OrderItem> selectByProductId(@Param("productId") Long productId);

    /**
     * 批量插入订单商品
     */
    int batchInsert(@Param("items") List<OrderItem> items);

    /**
     * 根据订单ID删除订单商品
     */
    int deleteByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据平台订单号删除订单商品
     */
    int deleteByPlatformOrderNo(@Param("platformOrderNo") String platformOrderNo);
}
