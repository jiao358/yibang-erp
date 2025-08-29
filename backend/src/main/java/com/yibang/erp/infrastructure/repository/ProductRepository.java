package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Repository接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface ProductRepository extends BaseMapper<Product> {
}
