package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.LogisticsInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 物流信息Repository接口
 */
@Mapper
public interface LogisticsInfoRepository extends BaseMapper<LogisticsInfo> {

    /**
     * 根据订单ID查询物流信息
     */
    @Select("SELECT * FROM logistics_info WHERE order_id = #{orderId} ORDER BY created_at DESC LIMIT 1")
    LogisticsInfo selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据物流单号查询物流信息
     */
    @Select("SELECT * FROM logistics_info WHERE tracking_number = #{trackingNumber} ORDER BY created_at DESC LIMIT 1")
    LogisticsInfo selectByTrackingNumber(@Param("trackingNumber") String trackingNumber);

    /**
     * 根据物流公司查询物流信息列表
     */
    @Select("SELECT * FROM logistics_info WHERE carrier = #{carrier} ORDER BY created_at DESC")
    List<LogisticsInfo> selectByCarrier(@Param("carrier") String carrier);

    /**
     * 根据物流状态查询物流信息列表
     */
    @Select("SELECT * FROM logistics_info WHERE status = #{status} ORDER BY created_at DESC")
    List<LogisticsInfo> selectByStatus(@Param("status") String status);

    /**
     * 根据时间范围查询物流信息列表
     */
    @Select("SELECT * FROM logistics_info WHERE created_at BETWEEN #{startTime} AND #{endTime} ORDER BY created_at DESC")
    List<LogisticsInfo> selectByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
