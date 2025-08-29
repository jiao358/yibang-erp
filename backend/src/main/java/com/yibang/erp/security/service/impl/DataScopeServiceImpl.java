package com.yibang.erp.security.service.impl;

import com.yibang.erp.security.annotation.DataScope;
import com.yibang.erp.security.service.DataScopeService;
import com.yibang.erp.common.util.SecurityUtils;
import com.yibang.erp.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据范围服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataScopeServiceImpl implements DataScopeService {
    
    private static final ThreadLocal<DataScope> DATA_SCOPE_THREAD_LOCAL = new ThreadLocal<>();
    
    @Override
    public void setDataScope(DataScope dataScope) {
        DATA_SCOPE_THREAD_LOCAL.set(dataScope);
        log.debug("设置数据范围: {}", dataScope);
    }
    
    @Override
    public DataScope getCurrentDataScope() {
        return DATA_SCOPE_THREAD_LOCAL.get();
    }
    
    @Override
    public String buildDataScopeCondition(String tableAlias) {
        DataScope dataScope = getCurrentDataScope();
        if (dataScope == null) {
            return "";
        }
        
        String alias = tableAlias == null ? "" : tableAlias + ".";
        
        switch (dataScope.type()) {
            case ALL:
                return "";
            case COMPANY:
                return buildCompanyCondition(alias, dataScope);
            case DEPARTMENT:
                return buildDepartmentCondition(alias, dataScope);
            case PERSONAL:
                return buildPersonalCondition(alias, dataScope);
            case CUSTOM:
                return buildCustomCondition(alias, dataScope);
            default:
                return buildCompanyCondition(alias, dataScope);
        }
    }
    
    @Override
    public DataScopeCondition buildDataScopeConditionWithParams(String tableAlias) {
        DataScope dataScope = getCurrentDataScope();
        if (dataScope == null) {
            return new DataScopeCondition("", new HashMap<>());
        }
        
        String alias = tableAlias == null ? "" : tableAlias + ".";
        Map<String, Object> parameters = new HashMap<>();
        
        switch (dataScope.type()) {
            case ALL:
                return new DataScopeCondition("", parameters);
            case COMPANY:
                return buildCompanyConditionWithParams(alias, dataScope, parameters);
            case DEPARTMENT:
                return buildDepartmentConditionWithParams(alias, dataScope, parameters);
            case PERSONAL:
                return buildPersonalConditionWithParams(alias, dataScope, parameters);
            case CUSTOM:
                return buildCustomConditionWithParams(alias, dataScope, parameters);
            default:
                return buildCompanyConditionWithParams(alias, dataScope, parameters);
        }
    }
    
    @Override
    public boolean canAccessData(Long userId, Long companyId, Long deptId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        // 超级管理员可以访问所有数据
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return true;
        }
        
        // 检查公司权限
        if (companyId != null && !companyId.equals(currentUser.getCompanyId())) {
            return false;
        }
        
        // 检查部门权限
        if (deptId != null && !deptId.equals(currentUser.getDeptId())) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public java.util.List<Long> getUserAccessibleCompanyIds(Long userId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return java.util.Collections.emptyList();
        }
        
        // 超级管理员可以访问所有公司
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return java.util.Collections.singletonList(currentUser.getCompanyId());
        }
        
        // 普通用户只能访问自己的公司
        return java.util.Collections.singletonList(currentUser.getCompanyId());
    }
    
    @Override
    public java.util.List<Long> getUserAccessibleDeptIds(Long userId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return java.util.Collections.emptyList();
        }
        
        // 超级管理员可以访问所有部门
        if (SecurityUtils.hasRole("SUPER_ADMIN")) {
            return java.util.Collections.singletonList(currentUser.getDeptId());
        }
        
        // 普通用户只能访问自己的部门
        return java.util.Collections.singletonList(currentUser.getDeptId());
    }
    
    @Override
    public void clearDataScope() {
        DATA_SCOPE_THREAD_LOCAL.remove();
        log.debug("清除数据范围");
    }
    
    /**
     * 构建公司条件
     */
    private String buildCompanyCondition(String alias, DataScope dataScope) {
        return String.format(" AND %s%s = %d", 
            alias, dataScope.companyField(), SecurityUtils.getCurrentUser().getCompanyId());
    }
    
    /**
     * 构建公司条件（带参数）
     */
    private DataScopeCondition buildCompanyConditionWithParams(String alias, DataScope dataScope, Map<String, Object> parameters) {
        String condition = String.format(" AND %s%s = :companyId", alias, dataScope.companyField());
        parameters.put("companyId", SecurityUtils.getCurrentUser().getCompanyId());
        return new DataScopeCondition(condition, parameters);
    }
    
    /**
     * 构建部门条件
     */
    private String buildDepartmentCondition(String alias, DataScope dataScope) {
        return String.format(" AND %s%s = %d", 
            alias, dataScope.deptField(), SecurityUtils.getCurrentUser().getDeptId());
    }
    
    /**
     * 构建部门条件（带参数）
     */
    private DataScopeCondition buildDepartmentConditionWithParams(String alias, DataScope dataScope, Map<String, Object> parameters) {
        String condition = String.format(" AND %s%s = :deptId", alias, dataScope.deptField());
        parameters.put("deptId", SecurityUtils.getCurrentUser().getDeptId());
        return new DataScopeCondition(condition, parameters);
    }
    
    /**
     * 构建个人条件
     */
    private String buildPersonalCondition(String alias, DataScope dataScope) {
        return String.format(" AND %s%s = %d", 
            alias, dataScope.userField(), SecurityUtils.getCurrentUserId());
    }
    
    /**
     * 构建个人条件（带参数）
     */
    private DataScopeCondition buildPersonalConditionWithParams(String alias, DataScope dataScope, Map<String, Object> parameters) {
        String condition = String.format(" AND %s%s = :userId", alias, dataScope.userField());
        parameters.put("userId", SecurityUtils.getCurrentUserId());
        return new DataScopeCondition(condition, parameters);
    }
    
    /**
     * 构建自定义条件
     */
    private String buildCustomCondition(String alias, DataScope dataScope) {
        if (dataScope.customCondition() == null || dataScope.customCondition().trim().isEmpty()) {
            return "";
        }
        return " AND " + dataScope.customCondition();
    }
    
    /**
     * 构建自定义条件（带参数）
     */
    private DataScopeCondition buildCustomConditionWithParams(String alias, DataScope dataScope, Map<String, Object> parameters) {
        if (dataScope.customCondition() == null || dataScope.customCondition().trim().isEmpty()) {
            return new DataScopeCondition("", parameters);
        }
        return new DataScopeCondition(" AND " + dataScope.customCondition(), parameters);
    }
}
