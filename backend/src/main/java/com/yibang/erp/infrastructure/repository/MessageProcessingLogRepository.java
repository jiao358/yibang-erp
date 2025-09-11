package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.MessageProcessingLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息处理日志Repository
 */
@Mapper
public interface MessageProcessingLogRepository extends BaseMapper<MessageProcessingLog> {

    /**
     * 根据消息ID查询处理记录
     */
    @Select("SELECT * FROM message_processing_log WHERE message_id = #{messageId} AND deleted = 0")
    MessageProcessingLog selectByMessageId(@Param("messageId") String messageId);

    /**
     * 根据源订单号和销售订单ID查询处理记录
     */
    @Select("SELECT * FROM message_processing_log WHERE source_order_id = #{sourceOrderId} AND sales_order_id = #{salesOrderId} AND deleted = 0")
    MessageProcessingLog selectByOrderIds(@Param("sourceOrderId") String sourceOrderId, 
                                         @Param("salesOrderId") String salesOrderId);

    /**
     * 根据幂等性键查询处理记录
     */
    @Select("SELECT * FROM message_processing_log WHERE idempotency_key = #{idempotencyKey} AND deleted = 0")
    MessageProcessingLog selectByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
}
