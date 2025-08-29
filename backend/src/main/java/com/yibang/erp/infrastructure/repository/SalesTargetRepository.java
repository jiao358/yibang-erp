package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.SalesTarget;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售目标Repository接口
 */
@Mapper
public interface SalesTargetRepository extends BaseMapper<SalesTarget> {
}
