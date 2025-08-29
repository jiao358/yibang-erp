package com.yibang.erp.domain.dto;

import com.yibang.erp.common.request.PageRequest;

/**
 * 用户查询参数类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public class UserQueryRequest extends PageRequest {
    
    /**
     * 用户名（模糊查询）
     */
    private String username;
    
    /**
     * 真实姓名（模糊查询）
     */
    private String realName;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 部门
     */
    private String department;
    
    /**
     * 邮箱（模糊查询）
     */
    private String email;
    
    /**
     * 手机号（模糊查询）
     */
    private String phone;
    
    public UserQueryRequest() {}
    
    public UserQueryRequest(Integer current, Integer size) {
        super(current, size);
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    /**
     * 检查是否有查询条件
     */
    public boolean hasQueryConditions() {
        return username != null && !username.trim().isEmpty() ||
               realName != null && !realName.trim().isEmpty() ||
               status != null && !status.trim().isEmpty() ||
               department != null && !department.trim().isEmpty() ||
               email != null && !email.trim().isEmpty() ||
               phone != null && !phone.trim().isEmpty();
    }
}
