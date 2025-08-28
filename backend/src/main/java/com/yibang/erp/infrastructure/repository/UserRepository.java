package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Mapper
public interface UserRepository extends BaseMapper<User> {
}
