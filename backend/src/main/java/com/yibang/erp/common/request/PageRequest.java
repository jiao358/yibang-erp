package com.yibang.erp.common.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 分页查询参数类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public class PageRequest {
    
    /**
     * 当前页码，从1开始
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer current = 1;
    
    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 1000, message = "每页大小不能超过1000")
    private Integer size = 20;
    
    /**
     * 排序字段
     */
    private String sortField;
    
    /**
     * 排序方向：asc, desc
     */
    private String sortOrder = "desc";
    
    public PageRequest() {}
    
    public PageRequest(Integer current, Integer size) {
        this.current = current;
        this.size = size;
    }
    
    /**
     * 获取偏移量（用于SQL的LIMIT）
     */
    public int getOffset() {
        return (current - 1) * size;
    }
    
    /**
     * 获取限制数量（用于SQL的LIMIT）
     */
    public int getLimit() {
        return size;
    }
    
    /**
     * 获取排序SQL片段
     */
    public String getOrderByClause() {
        if (sortField == null || sortField.trim().isEmpty()) {
            return "";
        }
        
        String order = "desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        return String.format("ORDER BY %s %s", sortField, order);
    }
    
    // Getters and Setters
    public Integer getCurrent() {
        return current;
    }
    
    public void setCurrent(Integer current) {
        this.current = current;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public String getSortField() {
        return sortField;
    }
    
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    
    public String getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
