package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.OrderManualProcessing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单人工处理表Repository
 */
@Mapper
public interface OrderManualProcessingRepository extends BaseMapper<OrderManualProcessing> {

    /**
     * 根据订单ID查询人工处理记录
     */
    @Select("SELECT * FROM order_manual_processing WHERE order_id = #{orderId} AND deleted = 0 ORDER BY created_at DESC")
    List<OrderManualProcessing> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据源订单ID查询人工处理记录
     */
    @Select("SELECT * FROM order_manual_processing WHERE source_order_id = #{sourceOrderId} AND deleted = 0 ORDER BY created_at DESC")
    List<OrderManualProcessing> selectBySourceOrderId(@Param("sourceOrderId") String sourceOrderId);

    /**
     * 根据处理类型和状态查询记录
     */
    @Select("SELECT * FROM order_manual_processing WHERE processing_type = #{processingType} AND status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<OrderManualProcessing> selectByTypeAndStatus(@Param("processingType") String processingType, @Param("status") String status);

    /**
     * 根据分配人查询待处理记录
     */
    @Select("SELECT * FROM order_manual_processing WHERE assigned_to = #{assignedTo} AND status IN ('PENDING', 'PROCESSING') AND deleted = 0 ORDER BY priority DESC, created_at ASC")
    List<OrderManualProcessing> selectPendingByAssignedTo(@Param("assignedTo") Long assignedTo);
}
