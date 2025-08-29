package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.PriceStrategy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格策略Repository接口
 */
@Mapper
public interface PriceStrategyRepository extends BaseMapper<PriceStrategy> {
}
