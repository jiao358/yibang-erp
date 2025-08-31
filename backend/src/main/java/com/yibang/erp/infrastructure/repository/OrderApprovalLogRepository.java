package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.OrderApprovalLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单审核日志Repository接口
 */
@Mapper
public interface OrderApprovalLogRepository extends BaseMapper<OrderApprovalLog> {

    /**
     * 根据订单ID查询审核日志列表
     */
    List<OrderApprovalLog> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据审核人ID查询审核日志列表
     */
    List<OrderApprovalLog> selectByApproverId(@Param("approverId") Long approverId);

    /**
     * 根据审核状态查询审核日志列表
     */
    List<OrderApprovalLog> selectByStatus(@Param("status") String status);

    /**
     * 根据操作类型查询审核日志列表
     */
    List<OrderApprovalLog> selectByAction(@Param("action") String action);
}
