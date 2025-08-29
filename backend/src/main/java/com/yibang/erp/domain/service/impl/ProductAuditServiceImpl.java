package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.dto.ProductAuditRequest;
import com.yibang.erp.domain.dto.ProductAuditResult;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.enums.ProductApprovalStatus;
import com.yibang.erp.domain.enums.ProductStatus;
import com.yibang.erp.domain.service.ProductAuditService;
import com.yibang.erp.domain.service.ProductStateService;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import com.yibang.erp.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品审核服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductAuditServiceImpl implements ProductAuditService {

    private final ProductStateService productStateService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ProductAuditResult auditProduct(ProductAuditRequest request) {
        ProductAuditResult result = new ProductAuditResult();
        result.setProductId(request.getProductId());
        result.setApprovalStatus(request.getApprovalStatus());
        result.setApprovalComment(request.getApprovalComment());
        result.setApproverId(request.getApproverId());
        result.setApprovalAt(LocalDateTime.now());

        try {
            // 检查审核权限
            if (!hasApprovalPermission(request.getApproverId(), getProductCompanyId(request.getProductId()))) {
                result.setSuccess(false);
                result.setMessage("用户没有审核权限");
                log.warn("用户没有审核权限，用户ID: {}, 商品ID: {}", request.getApproverId(), request.getProductId());
                return result;
            }

            // 执行审核
            boolean auditSuccess = productStateService.approveProduct(
                    request.getProductId(),
                    request.getApprovalStatus(),
                    request.getApprovalComment(),
                    request.getApproverId()
            );

            if (auditSuccess) {
                // 获取审核后的商品信息
                Product product = productRepository.selectById(request.getProductId());
                if (product != null) {
                    result.setProductName(product.getName());
                    result.setSku(product.getSku());
                    result.setProductStatus(product.getStatus());
                    
                    // 获取审核人姓名
                    User approver = userRepository.selectById(request.getApproverId());
                    if (approver != null) {
                        result.setApproverName(approver.getRealName());
                    }
                }

                result.setSuccess(true);
                result.setMessage("审核成功");
                log.info("商品审核成功，商品ID: {}, 审核状态: {}, 审核人ID: {}", 
                        request.getProductId(), request.getApprovalStatus(), request.getApproverId());
            } else {
                result.setSuccess(false);
                result.setMessage("审核失败");
                log.error("商品审核失败，商品ID: {}", request.getProductId());
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("审核异常: " + e.getMessage());
            log.error("商品审核异常，商品ID: {}, 审核人ID: {}", request.getProductId(), request.getApproverId(), e);
        }

        return result;
    }

    @Override
    @Transactional
    public List<ProductAuditResult> batchAuditProducts(List<ProductAuditRequest> requests) {
        List<ProductAuditResult> results = new ArrayList<>();
        
        for (ProductAuditRequest request : requests) {
            ProductAuditResult result = auditProduct(request);
            results.add(result);
        }
        
        return results;
    }

    @Override
    public List<Long> getPendingApprovalProducts(Long companyId, int page, int size) {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_id", companyId)
                       .eq("status", ProductStatus.PENDING.getCode())
                       .eq("approval_status", ProductApprovalStatus.PENDING.getCode())
                       .orderByDesc("created_at")
                       .last("LIMIT " + (page - 1) * size + ", " + size);

            List<Product> products = productRepository.selectList(queryWrapper);
            return products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取待审核商品列表异常，公司ID: {}", companyId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public long getPendingApprovalCount(Long companyId) {
        try {
            QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("company_id", companyId)
                       .eq("status", ProductStatus.PENDING.getCode())
                       .eq("approval_status", ProductApprovalStatus.PENDING.getCode());

            return productRepository.selectCount(queryWrapper);
        } catch (Exception e) {
            log.error("获取待审核商品总数异常，公司ID: {}", companyId, e);
            return 0;
        }
    }

    @Override
    public boolean hasApprovalPermission(Long userId, Long companyId) {
        try {
            // 检查用户是否存在
            User user = userRepository.selectById(userId);
            if (user == null) {
                return false;
            }

            // 检查用户是否属于指定公司
            if (!companyId.equals(user.getCompanyId())) {
                return false;
            }

            // 检查用户角色是否有审核权限
            // 这里可以根据实际需求扩展权限检查逻辑
            // 暂时简单判断：供应链管理员和系统管理员有审核权限
            String roleName = getUserRoleName(user.getRoleId());
            return "SUPPLY_CHAIN_ADMIN".equals(roleName) || "SYSTEM_ADMIN".equals(roleName);
        } catch (Exception e) {
            log.error("检查审核权限异常，用户ID: {}, 公司ID: {}", userId, companyId, e);
            return false;
        }
    }

    @Override
    public List<ProductAuditResult> getAuditHistory(Long productId, int page, int size) {
        // 这里需要实现审核历史记录的查询
        // 暂时返回空列表，后续可以扩展
        log.info("获取审核历史记录，商品ID: {}, 页码: {}, 每页大小: {}", productId, page, size);
        return new ArrayList<>();
    }

    @Override
    public Object getAuditStatistics(Long companyId, String startDate, String endDate) {
        // 这里需要实现审核统计信息的查询
        // 暂时返回null，后续可以扩展
        log.info("获取审核统计信息，公司ID: {}, 开始日期: {}, 结束日期: {}", companyId, startDate, endDate);
        return null;
    }

    /**
     * 获取商品所属公司ID
     */
    private Long getProductCompanyId(Long productId) {
        try {
            Product product = productRepository.selectById(productId);
            return product != null ? product.getCompanyId() : null;
        } catch (Exception e) {
            log.error("获取商品公司ID异常，商品ID: {}", productId, e);
            return null;
        }
    }

    /**
     * 获取用户角色名称
     */
    private String getUserRoleName(Long roleId) {
        try {
            // 这里需要根据实际的角色表结构来实现
            // 暂时返回默认值
            return "USER";
        } catch (Exception e) {
            log.error("获取用户角色名称异常，角色ID: {}", roleId, e);
            return "USER";
        }
    }
}
