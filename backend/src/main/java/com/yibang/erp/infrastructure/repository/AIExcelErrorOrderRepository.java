package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibang.erp.domain.entity.AIExcelErrorOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * AI Excel错误订单Repository接口
 */
@Mapper
public interface AIExcelErrorOrderRepository extends BaseMapper<AIExcelErrorOrder> {

    /**
     * 根据任务ID查询错误订单列表
     */
    @Select("SELECT * FROM ai_excel_error_orders WHERE task_id = #{taskId} ORDER BY excel_row_number ASC")
    List<AIExcelErrorOrder> selectByTaskId(@Param("taskId") String taskId);

    /**
     * 根据用户和公司分页查询错误订单
     */
    @Select("SELECT * FROM ai_excel_error_orders WHERE user_id = #{userId} AND company_id = #{companyId} ORDER BY created_at DESC")
    IPage<AIExcelErrorOrder> selectByUserAndCompany(Page<AIExcelErrorOrder> page, 
                                                   @Param("userId") Long userId, 
                                                   @Param("companyId") Long companyId);

    /**
     * 根据错误类型查询错误订单
     */
    @Select("SELECT * FROM ai_excel_error_orders WHERE error_type = #{errorType} ORDER BY created_at DESC")
    List<AIExcelErrorOrder> selectByErrorType(@Param("errorType") String errorType);

    /**
     * 根据状态查询错误订单
     */
    @Select("SELECT * FROM ai_excel_error_orders WHERE status = #{status} ORDER BY created_at DESC")
    List<AIExcelErrorOrder> selectByStatus(@Param("status") String status);

    /**
     * 统计任务错误订单数量
     */
    @Select("SELECT COUNT(*) FROM ai_excel_error_orders WHERE task_id = #{taskId}")
    Long countByTaskId(@Param("taskId") String taskId);

    /**
     * 统计用户错误订单数量
     */
    @Select("SELECT COUNT(*) FROM ai_excel_error_orders WHERE user_id = #{userId} AND company_id = #{companyId}")
    Long countByUser(@Param("userId") Long userId, @Param("companyId") Long companyId);
}
