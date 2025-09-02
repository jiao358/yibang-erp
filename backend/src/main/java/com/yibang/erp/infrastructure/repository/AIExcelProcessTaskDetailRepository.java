package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.AIExcelProcessTaskDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI Excel处理任务详情Repository接口
 */
@Mapper
public interface AIExcelProcessTaskDetailRepository extends BaseMapper<AIExcelProcessTaskDetail> {
}
