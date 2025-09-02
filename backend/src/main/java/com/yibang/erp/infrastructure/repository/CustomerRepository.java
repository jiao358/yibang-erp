package com.yibang.erp.infrastructure.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibang.erp.domain.entity.Customer;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 客户Repository接口
 */
@Mapper
public interface CustomerRepository extends BaseMapper<Customer> {

    /**
     * 根据客户编码查询客户
     */
    @Select("SELECT * FROM customers WHERE customer_code = #{customerCode} AND deleted = 0")
    Customer selectByCustomerCode(@Param("customerCode") String customerCode);

    /**
     * 根据公司ID查询客户列表
     */
    @Select("SELECT * FROM customers WHERE company_id = #{companyId} AND deleted = 0 ORDER BY created_at DESC")
    List<Customer> selectByCompanyId(@Param("companyId") Long companyId);

    /**
     * 根据客户类型查询客户列表
     */
    @Select("SELECT * FROM customers WHERE customer_type = #{customerType} AND deleted = 0 ORDER BY created_at DESC")
    List<Customer> selectByCustomerType(@Param("customerType") String customerType);

    /**
     * 根据客户等级查询客户列表
     */
    @Select("SELECT * FROM customers WHERE customer_level = #{customerLevel} AND deleted = 0 ORDER BY created_at DESC")
    List<Customer> selectByCustomerLevel(@Param("customerLevel") String customerLevel);

    /**
     * 根据状态查询客户列表
     */
    @Select("SELECT * FROM customers WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<Customer> selectByStatus(@Param("status") String status);

    /**
     * 根据关键词搜索客户
     */
    @Select("SELECT * FROM customers WHERE (customer_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR customer_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR contact_person LIKE CONCAT('%', #{keyword}, '%') " +
            "OR phone LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND deleted = 0 ORDER BY created_at DESC")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);
}
