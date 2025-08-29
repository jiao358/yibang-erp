package com.yibang.erp.common.exception;

/**
 * 权限拒绝异常
 * 当用户没有足够权限访问资源时抛出
 */
public class PermissionDeniedException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 权限标识符
     */
    private String permission;
    
    /**
     * 资源标识符
     */
    private String resource;
    
    public PermissionDeniedException(String message) {
        super(message);
    }
    
    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PermissionDeniedException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PermissionDeniedException(String errorCode, String message, String permission) {
        super(message);
        this.errorCode = errorCode;
        this.permission = permission;
    }
    
    public PermissionDeniedException(String errorCode, String message, String permission, String resource) {
        super(message);
        this.errorCode = errorCode;
        this.permission = permission;
        this.resource = resource;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public String getResource() {
        return resource;
    }
}
