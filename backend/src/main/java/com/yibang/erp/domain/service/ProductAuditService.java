package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.ProductAuditRequest;
import com.yibang.erp.domain.dto.ProductAuditResult;
import com.yibang.erp.domain.enums.ProductApprovalStatus;

import java.util.List;

/**
 * 商品审核服务接口
 * 负责商品审核流程的业务逻辑
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface ProductAuditService {

    /**
     * 审核商品
     *
     * @param request 审核请求
     * @return 审核结果
     */
    ProductAuditResult auditProduct(ProductAuditRequest request);

    /**
     * 批量审核商品
     *
     * @param requests 审核请求列表
     * @return 审核结果列表
     */
    List<ProductAuditResult> batchAuditProducts(List<ProductAuditRequest> requests);

    /**
     * 获取待审核商品列表
     *
     * @param companyId 公司ID
     * @param page 页码
     * @param size 每页大小
     * @return 待审核商品列表
     */
    List<Long> getPendingApprovalProducts(Long companyId, int page, int size);

    /**
     * 获取待审核商品总数
     *
     * @param companyId 公司ID
     * @return 待审核商品总数
     */
    long getPendingApprovalCount(Long companyId);

    /**
     * 检查用户是否有审核权限
     *
     * @param userId 用户ID
     * @param companyId 公司ID
     * @return 是否有审核权限
     */
    boolean hasApprovalPermission(Long userId, Long companyId);

    /**
     * 获取审核历史记录
     *
     * @param productId 商品ID
     * @param page 页码
     * @param size 每页大小
     * @return 审核历史记录
     */
    List<ProductAuditResult> getAuditHistory(Long productId, int page, int size);

    /**
     * 获取审核统计信息
     *
     * @param companyId 公司ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 审核统计信息
     */
    Object getAuditStatistics(Long companyId, String startDate, String endDate);
}
