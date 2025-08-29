package com.yibang.erp.domain.service;

import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;

/**
 * 商品状态管理服务接口
 * 负责商品状态流转和审核流程管理
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface ProductStateService {

    /**
     * 提交商品审核
     * 将商品状态从草稿改为待审核
     *
     * @param productId 商品ID
     * @param submitterId 提交人ID
     * @return 是否成功
     */
    boolean submitForApproval(Long productId, Long submitterId);

    /**
     * 审核商品
     * 处理商品的审核结果
     *
     * @param productId 商品ID
     * @param approvalStatus 审核状态
     * @param approvalComment 审核意见
     * @param approverId 审核人ID
     * @return 审核结果
     */
    boolean approveProduct(Long productId, ProductApprovalStatus approvalStatus, 
                          String approvalComment, Long approverId);

    /**
     * 上架商品
     * 将审核通过的商品状态改为已上架
     *
     * @param productId 商品ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean activateProduct(Long productId, Long operatorId);

    /**
     * 下架商品
     * 将商品状态改为已下架
     *
     * @param productId 商品ID
     * @param operatorId 操作人ID
     * @param reason 下架原因
     * @return 是否成功
     */
    boolean deactivateProduct(Long productId, Long operatorId, String reason);

    /**
     * 停售商品
     * 将商品状态改为已停售
     *
     * @param productId 商品ID
     * @param operatorId 操作人ID
     * @param reason 停售原因
     * @return 是否成功
     */
    boolean discontinueProduct(Long productId, Long operatorId, String reason);

    /**
     * 恢复商品为草稿状态
     * 将商品状态重置为草稿，允许重新编辑
     *
     * @param productId 商品ID
     * @param operatorId 操作人ID
     * @return 是否成功
     */
    boolean resetToDraft(Long productId, Long operatorId);

    /**
     * 检查商品状态流转是否合法
     *
     * @param currentStatus 当前状态
     * @param targetStatus 目标状态
     * @return 是否合法
     */
    boolean isStatusTransitionValid(ProductStatus currentStatus, ProductStatus targetStatus);

    /**
     * 获取商品当前状态
     *
     * @param productId 商品ID
     * @return 商品状态
     */
    ProductStatus getCurrentStatus(Long productId);

    /**
     * 获取商品审核状态
     *
     * @param productId 商品ID
     * @return 审核状态
     */
    ProductApprovalStatus getApprovalStatus(Long productId);
}
