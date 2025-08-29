package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.PriceTier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 价格分层Repository接口
 */
@Mapper
public interface PriceTierRepository extends BaseMapper<PriceTier> {
}
