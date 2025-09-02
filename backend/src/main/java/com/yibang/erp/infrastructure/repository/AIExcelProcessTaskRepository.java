package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.AIExcelProcessTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI Excel处理任务Repository接口
 */
@Mapper
public interface AIExcelProcessTaskRepository extends BaseMapper<AIExcelProcessTask> {
}
