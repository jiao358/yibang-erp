package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.ProductPriceTierConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品价格分层配置Repository
 */
@Mapper
public interface ProductPriceTierConfigRepository extends BaseMapper<ProductPriceTierConfig> {

}
