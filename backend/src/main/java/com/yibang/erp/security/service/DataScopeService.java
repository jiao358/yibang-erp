package com.yibang.erp.security.service;

import com.yibang.erp.security.annotation.DataScope;

/**
 * 数据范围服务接口
 * 负责数据访问控制
 */
public interface DataScopeService {
    
    /**
     * 设置数据范围
     *
     * @param dataScope 数据范围注解
     */
    void setDataScope(DataScope dataScope);
    
    /**
     * 获取当前数据范围
     *
     * @return 数据范围注解
     */
    DataScope getCurrentDataScope();
    
    /**
     * 构建数据范围SQL条件
     *
     * @param tableAlias 表别名
     * @return SQL条件字符串
     */
    String buildDataScopeCondition(String tableAlias);
    
    /**
     * 构建数据范围SQL条件（带参数）
     *
     * @param tableAlias 表别名
     * @return 数据范围条件对象
     */
    DataScopeCondition buildDataScopeConditionWithParams(String tableAlias);
    
    /**
     * 检查用户是否有权限访问指定数据
     *
     * @param userId 用户ID
     * @param companyId 公司ID
     * @param deptId 部门ID
     * @return 是否有权限
     */
    boolean canAccessData(Long userId, Long companyId, Long deptId);
    
    /**
     * 获取用户可访问的公司ID列表
     *
     * @param userId 用户ID
     * @return 公司ID列表
     */
    java.util.List<Long> getUserAccessibleCompanyIds(Long userId);
    
    /**
     * 获取用户可访问的部门ID列表
     *
     * @param userId 用户ID
     * @return 部门ID列表
     */
    java.util.List<Long> getUserAccessibleDeptIds(Long userId);
    
    /**
     * 清除当前数据范围
     */
    void clearDataScope();
    
    /**
     * 数据范围条件对象
     */
    class DataScopeCondition {
        private String sqlCondition;
        private java.util.Map<String, Object> parameters;
        
        public DataScopeCondition(String sqlCondition, java.util.Map<String, Object> parameters) {
            this.sqlCondition = sqlCondition;
            this.parameters = parameters;
        }
        
        public String getSqlCondition() {
            return sqlCondition;
        }
        
        public java.util.Map<String, Object> getParameters() {
            return parameters;
        }
    }
}
