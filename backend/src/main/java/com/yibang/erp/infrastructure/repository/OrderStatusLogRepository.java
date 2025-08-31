package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.OrderStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单状态日志Repository接口
 */
@Mapper
public interface OrderStatusLogRepository extends BaseMapper<OrderStatusLog> {

    /**
     * 根据订单ID查询状态日志列表
     */
    List<OrderStatusLog> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据平台订单号查询状态日志列表
     */
    List<OrderStatusLog> selectByPlatformOrderNo(@Param("platformOrderNo") String platformOrderNo);

    /**
     * 根据订单ID和状态查询状态日志
     */
    List<OrderStatusLog> selectByOrderIdAndStatus(@Param("orderId") Long orderId, @Param("status") String status);

    /**
     * 根据操作人ID查询状态日志列表
     */
    List<OrderStatusLog> selectByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 根据时间范围查询状态日志列表
     */
    List<OrderStatusLog> selectByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
