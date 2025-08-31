package com.yibang.erp.domain.service;

/**
 * 订单号生成服务接口
 * 采用Redis分布式锁方式，确保订单号唯一性
 */
public interface OrderNumberGeneratorService {
    
    /**
     * 生成平台订单号
     * 格式：登录ID+订单渠道+日期+序号
     * 例如：000001MANUAL202412010001
     * 
     * @param userName 登录用户ID
     * @param orderSource 订单渠道（MANUAL, EXCEL_IMPORT, API, WEBSITE）
     * @return 生成的订单号
     */
    String generatePlatformOrderNo(String userName, String orderSource);
    
    /**
     * 批量预生成订单号
     * 用于批处理场景，提高性能
     * 
     * @param userName 登录用户ID
     * @param orderSource 订单渠道
     * @param count 需要生成的订单号数量
     * @return 订单号列表
     */
    java.util.List<String> preGenerateOrderNumbers(String userName, String orderSource, int count);
    
    /**
     * 验证订单号格式是否正确
     * 
     * @param orderNo 订单号
     * @return 是否有效
     */
    boolean validateOrderNoFormat(String orderNo);
}
