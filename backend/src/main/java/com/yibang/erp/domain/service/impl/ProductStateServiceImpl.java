package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;
import com.yibang.erp.domain.service.ProductStateService;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import com.yibang.erp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 商品状态管理服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStateServiceImpl implements ProductStateService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public boolean submitForApproval(Long productId, Long submitterId) {
        try {
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品不存在，ID: {}", productId);
                return false;
            }

            // 检查商品当前状态是否允许提交审核
            if (product.getStatus() != ProductStatus.DRAFT) {
                log.warn("商品状态不允许提交审核，当前状态: {}, 商品ID: {}", product.getStatus(), productId);
                return false;
            }

            // 更新商品状态为待审核
            product.setStatus(ProductStatus.PENDING);
            product.setApprovalStatus(ProductApprovalStatus.PENDING);
            product.setUpdatedBy(submitterId);
            product.setUpdatedAt(LocalDateTime.now());

            int result = productRepository.updateById(product);
            if (result > 0) {
                log.info("商品提交审核成功，商品ID: {}, 提交人ID: {}", productId, submitterId);
                return true;
            } else {
                log.error("商品提交审核失败，商品ID: {}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("商品提交审核异常，商品ID: {}, 提交人ID: {}", productId, submitterId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean approveProduct(Long productId, ProductApprovalStatus approvalStatus, 
                                 String approvalComment, Long approverId) {
        try {
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品不存在，ID: {}", productId);
                return false;
            }

            // 检查商品当前状态是否允许审核
            if (product.getStatus() != ProductStatus.PENDING) {
                log.warn("商品状态不允许审核，当前状态: {}, 商品ID: {}", product.getStatus(), productId);
                return false;
            }

            // 更新审核状态
            product.setApprovalStatus(approvalStatus);
            product.setApprovalComment(approvalComment);
            product.setApprovalBy(approverId);
            product.setApprovalAt(LocalDateTime.now());
            product.setUpdatedBy(approverId);
            product.setUpdatedAt(LocalDateTime.now());

            // 根据审核结果更新商品状态
            if (approvalStatus == ProductApprovalStatus.APPROVED) {
                product.setStatus(ProductStatus.ACTIVE);
            } else if (approvalStatus == ProductApprovalStatus.REJECTED) {
                product.setStatus(ProductStatus.DRAFT); // 拒绝后回到草稿状态
            }

            int result = productRepository.updateById(product);
            if (result > 0) {
                log.info("商品审核完成，商品ID: {}, 审核状态: {}, 审核人ID: {}", 
                        productId, approvalStatus, approverId);
                return true;
            } else {
                log.error("商品审核失败，商品ID: {}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("商品审核异常，商品ID: {}, 审核人ID: {}", productId, approverId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean activateProduct(Long productId, Long operatorId) {
        try {
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品不存在，ID: {}", productId);
                return false;
            }

            // 检查商品是否已审核通过
            if (product.getApprovalStatus() != ProductApprovalStatus.APPROVED) {
                log.warn("商品未审核通过，无法上架，商品ID: {}", productId);
                return false;
            }

            // 更新商品状态为已上架
            product.setStatus(ProductStatus.ACTIVE);
            product.setUpdatedBy(operatorId);
            product.setUpdatedAt(LocalDateTime.now());

            int result = productRepository.updateById(product);
            if (result > 0) {
                log.info("商品上架成功，商品ID: {}, 操作人ID: {}", productId, operatorId);
                return true;
            } else {
                log.error("商品上架失败，商品ID: {}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("商品上架异常，商品ID: {}, 操作人ID: {}", productId, operatorId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean deactivateProduct(Long productId, Long operatorId, String reason) {
        try {
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品不存在，ID: {}", productId);
                return false;
            }

            // 检查商品当前状态是否允许下架
            if (product.getStatus() != ProductStatus.ACTIVE) {
                log.warn("商品状态不允许下架，当前状态: {}, 商品ID: {}", product.getStatus(), productId);
                return false;
            }

            // 更新商品状态为已下架
            product.setStatus(ProductStatus.INACTIVE);
            product.setUpdatedBy(operatorId);
            product.setUpdatedAt(LocalDateTime.now());

            int result = productRepository.updateById(product);
            if (result > 0) {
                log.info("商品下架成功，商品ID: {}, 操作人ID: {}, 原因: {}", productId, operatorId, reason);
                return true;
            } else {
                log.error("商品下架失败，商品ID: {}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("商品下架异常，商品ID: {}, 操作人ID: {}", productId, operatorId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean discontinueProduct(Long productId, Long operatorId, String reason) {
        try {
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品不存在，ID: {}", productId);
                return false;
            }

            // 更新商品状态为已停售
            product.setStatus(ProductStatus.DISCONTINUED);
            product.setUpdatedBy(operatorId);
            product.setUpdatedAt(LocalDateTime.now());

            int result = productRepository.updateById(product);
            if (result > 0) {
                log.info("商品停售成功，商品ID: {}, 操作人ID: {}, 原因: {}", productId, operatorId, reason);
                return true;
            } else {
                log.error("商品停售失败，商品ID: {}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("商品停售异常，商品ID: {}, 操作人ID: {}", productId, operatorId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public boolean resetToDraft(Long productId, Long operatorId) {
        try {
            Product product = productRepository.selectById(productId);
            if (product == null) {
                log.warn("商品不存在，ID: {}", productId);
                return false;
            }

            // 检查商品当前状态是否允许重置
            if (product.getStatus() == ProductStatus.ACTIVE) {
                log.warn("已上架商品不允许重置为草稿，商品ID: {}", productId);
                return false;
            }

            // 重置商品状态为草稿
            product.setStatus(ProductStatus.DRAFT);
            product.setApprovalStatus(ProductApprovalStatus.PENDING);
            product.setApprovalComment(null);
            product.setApprovalAt(null);
            product.setApprovalBy(null);
            product.setUpdatedBy(operatorId);
            product.setUpdatedAt(LocalDateTime.now());

            int result = productRepository.updateById(product);
            if (result > 0) {
                log.info("商品重置为草稿成功，商品ID: {}, 操作人ID: {}", productId, operatorId);
                return true;
            } else {
                log.error("商品重置为草稿失败，商品ID: {}", productId);
                return false;
            }
        } catch (Exception e) {
            log.error("商品重置为草稿异常，商品ID: {}, 操作人ID: {}", productId, operatorId, e);
            return false;
        }
    }

    @Override
    public boolean isStatusTransitionValid(ProductStatus currentStatus, ProductStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }

        // 定义合法的状态流转规则
        switch (currentStatus) {
            case DRAFT:
                return targetStatus == ProductStatus.PENDING || targetStatus == ProductStatus.DISCONTINUED;
            case PENDING:
                return targetStatus == ProductStatus.DRAFT || targetStatus == ProductStatus.DISCONTINUED;
            case ACTIVE:
                return targetStatus == ProductStatus.INACTIVE || targetStatus == ProductStatus.DISCONTINUED;
            case INACTIVE:
                return targetStatus == ProductStatus.ACTIVE || targetStatus == ProductStatus.DISCONTINUED;
            case DISCONTINUED:
                return false; // 已停售状态不允许再流转
            default:
                return false;
        }
    }

    @Override
    public ProductStatus getCurrentStatus(Long productId) {
        try {
            Product product = productRepository.selectById(productId);
            return product != null ? product.getStatus() : null;
        } catch (Exception e) {
            log.error("获取商品状态异常，商品ID: {}", productId, e);
            return null;
        }
    }

    @Override
    public ProductApprovalStatus getApprovalStatus(Long productId) {
        try {
            Product product = productRepository.selectById(productId);
            return product != null ? product.getApprovalStatus() : null;
        } catch (Exception e) {
            log.error("获取商品审核状态异常，商品ID: {}", productId, e);
            return null;
        }
    }
}
