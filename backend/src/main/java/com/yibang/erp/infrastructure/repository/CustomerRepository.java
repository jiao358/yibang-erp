package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 客户Repository接口
 */
@Mapper
public interface CustomerRepository extends BaseMapper<Customer> {

    /**
     * 根据客户编码查询客户
     */
    Customer selectByCustomerCode(@Param("customerCode") String customerCode);

    /**
     * 根据公司ID查询客户列表
     */
    List<Customer> selectByCompanyId(@Param("companyId") Long companyId);

    /**
     * 根据客户类型查询客户列表
     */
    List<Customer> selectByCustomerType(@Param("customerType") String customerType);

    /**
     * 根据客户等级查询客户列表
     */
    List<Customer> selectByCustomerLevel(@Param("customerLevel") String customerLevel);

    /**
     * 根据状态查询客户列表
     */
    List<Customer> selectByStatus(@Param("status") String status);

    /**
     * 根据关键词搜索客户
     */
    List<Customer> searchByKeyword(@Param("keyword") String keyword);
}
