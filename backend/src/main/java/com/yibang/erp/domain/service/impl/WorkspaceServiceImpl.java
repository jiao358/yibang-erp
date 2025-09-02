package com.yibang.erp.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yibang.erp.domain.entity.Order;
import com.yibang.erp.domain.entity.Product;
import com.yibang.erp.domain.entity.ProductInventory;
import com.yibang.erp.domain.service.WorkspaceService;
import com.yibang.erp.infrastructure.repository.OrderRepository;
import com.yibang.erp.infrastructure.repository.ProductInventoryRepository;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductInventoryRepository productInventoryRepository;

    @Override
    public Map<String, Object> getSupplierStats(Long companyId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 待审核订单数量
            QueryWrapper<Order> pendingReviewWrapper = new QueryWrapper<>();
            pendingReviewWrapper.eq("order_status", "SUBMITTED");
            pendingReviewWrapper.eq("supplier_company_id", companyId);
            pendingReviewWrapper.eq("deleted", false);
            Long pendingOrders = orderRepository.selectCount(pendingReviewWrapper);
            
            // 待发货订单数量
            QueryWrapper<Order> pendingShipmentWrapper = new QueryWrapper<>();
            pendingShipmentWrapper.eq("approval_status", "APPROVED");
            pendingShipmentWrapper.eq("supplier_company_id", companyId);
            pendingShipmentWrapper.eq("deleted", false);
            Long pendingShipments = orderRepository.selectCount(pendingShipmentWrapper);
            
            // 商品总数
            QueryWrapper<Product> productWrapper = new QueryWrapper<>();
            productWrapper.eq("company_id", companyId);
            productWrapper.eq("deleted", false);
            Long totalProducts = productRepository.selectCount(productWrapper);
            
            // 库存预警商品数量
            QueryWrapper<ProductInventory> lowStockWrapper = new QueryWrapper<>();
            // 这里需要根据公司ID查询库存，暂时使用简单查询
            Long lowStockItems = productInventoryRepository.selectCount(lowStockWrapper);
            
            stats.put("pendingOrders", pendingOrders);
            stats.put("pendingShipments", pendingShipments);
            stats.put("totalProducts", totalProducts);
            stats.put("lowStockItems", lowStockItems);
            stats.put("pendingProducts", 0L);
            stats.put("activeProducts", totalProducts);
            stats.put("totalInventory", 0L);
            stats.put("totalWarehouses", 0L);
            stats.put("inventoryChecks", 0L);
            stats.put("inventoryValue", 0L);
            
        } catch (Exception e) {
            log.error("获取供应链统计数据失败", e);
            // 返回默认值
            stats.put("pendingOrders", 0L);
            stats.put("pendingShipments", 0L);
            stats.put("totalProducts", 0L);
            stats.put("lowStockItems", 0L);
            stats.put("pendingProducts", 0L);
            stats.put("activeProducts", 0L);
            stats.put("totalInventory", 0L);
            stats.put("totalWarehouses", 0L);
            stats.put("inventoryChecks", 0L);
            stats.put("inventoryValue", 0L);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 系统管理员统计数据
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("deleted", false);
            Long totalOrders = orderRepository.selectCount(orderWrapper);
            
            QueryWrapper<Product> productWrapper = new QueryWrapper<>();
            productWrapper.eq("deleted", false);
            Long totalProducts = productRepository.selectCount(productWrapper);
            
            stats.put("totalOrders", totalOrders);
            stats.put("totalProducts", totalProducts);
            stats.put("totalUsers", 0L);
            stats.put("totalCompanies", 0L);
            stats.put("pendingOrders", 0L);
            stats.put("activeProducts", totalProducts);
            
        } catch (Exception e) {
            log.error("获取系统统计数据失败", e);
            stats.put("totalOrders", 0L);
            stats.put("totalProducts", 0L);
            stats.put("totalUsers", 0L);
            stats.put("totalCompanies", 0L);
            stats.put("pendingOrders", 0L);
            stats.put("activeProducts", 0L);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getSalesStats(Long companyId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 销售统计数据
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("supplier_company_id", companyId);
            orderWrapper.eq("deleted", false);
            Long totalOrders = orderRepository.selectCount(orderWrapper);
            
            QueryWrapper<Order> pendingWrapper = new QueryWrapper<>();
            pendingWrapper.eq("approval_status", "PENDING");
            pendingWrapper.eq("supplier_company_id", companyId);
            pendingWrapper.eq("deleted", false);
            Long pendingOrders = orderRepository.selectCount(pendingWrapper);
            
            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("completedOrders", 0L);
            stats.put("totalRevenue", 0L);
            stats.put("monthlyTarget", 0L);
            stats.put("targetProgress", 0.0);
            
        } catch (Exception e) {
            log.error("获取销售统计数据失败", e);
            stats.put("totalOrders", 0L);
            stats.put("pendingOrders", 0L);
            stats.put("completedOrders", 0L);
            stats.put("totalRevenue", 0L);
            stats.put("monthlyTarget", 0L);
            stats.put("targetProgress", 0.0);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getBasicStats(Long companyId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 基础统计数据
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("supplier_company_id", companyId);
            orderWrapper.eq("deleted", false);
            Long totalOrders = orderRepository.selectCount(orderWrapper);
            
            QueryWrapper<Product> productWrapper = new QueryWrapper<>();
            productWrapper.eq("company_id", companyId);
            productWrapper.eq("deleted", false);
            Long totalProducts = productRepository.selectCount(productWrapper);
            
            stats.put("totalOrders", totalOrders);
            stats.put("totalProducts", totalProducts);
            stats.put("pendingOrders", 0L);
            stats.put("activeProducts", totalProducts);
            
        } catch (Exception e) {
            log.error("获取基础统计数据失败", e);
            stats.put("totalOrders", 0L);
            stats.put("totalProducts", 0L);
            stats.put("pendingOrders", 0L);
            stats.put("activeProducts", 0L);
        }
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getRecentOrders(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("supplier_company_id", companyId);
            wrapper.eq("deleted", false);
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT " + limit);
            
            List<Order> orders = orderRepository.selectList(wrapper);
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getPlatformOrderId());
                orderMap.put("status", order.getOrderStatus());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("createdAt", order.getCreatedAt());
                result.add(orderMap);
            }
            
        } catch (Exception e) {
            log.error("获取最近订单失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentProducts(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            QueryWrapper<Product> wrapper = new QueryWrapper<>();
            wrapper.eq("company_id", companyId);
            wrapper.eq("deleted", false);
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT " + limit);
            
            List<Product> products = productRepository.selectList(wrapper);
            
            for (Product product : products) {
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("id", product.getId());
                productMap.put("sku", product.getSku());
                productMap.put("name", product.getName());
                productMap.put("status", product.getStatus());
                productMap.put("createdAt", product.getCreatedAt());
                result.add(productMap);
            }
            
        } catch (Exception e) {
            log.error("获取最近商品失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getLowStockItems(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            QueryWrapper<ProductInventory> wrapper = new QueryWrapper<>();
            wrapper.last("LIMIT " + limit);
            
            List<ProductInventory> inventories = productInventoryRepository.selectList(wrapper);
            
            for (ProductInventory inventory : inventories) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("id", inventory.getId());
                itemMap.put("productId", inventory.getProductId());
                itemMap.put("warehouseId", inventory.getWarehouseId());
                itemMap.put("availableQuantity", inventory.getAvailableQuantity());
                itemMap.put("minStockLevel", inventory.getMinStockLevel());
                itemMap.put("updatedAt", inventory.getUpdatedAt());
                result.add(itemMap);
            }
            
        } catch (Exception e) {
            log.error("获取库存预警商品失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getPendingOrders(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("approval_status", "PENDING");
            wrapper.eq("supplier_company_id", companyId);
            wrapper.eq("deleted", false);
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT " + limit);
            
            List<Order> orders = orderRepository.selectList(wrapper);
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getPlatformOrderId());
                orderMap.put("status", order.getOrderStatus());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("createdAt", order.getCreatedAt());
                result.add(orderMap);
            }
            
        } catch (Exception e) {
            log.error("获取待审核订单失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getPendingShipments(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("approval_status", "APPROVED");
            wrapper.eq("supplier_company_id", companyId);
            wrapper.eq("deleted", false);
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT " + limit);
            
            List<Order> orders = orderRepository.selectList(wrapper);
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getPlatformOrderId());
                orderMap.put("status", order.getOrderStatus());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("createdAt", order.getCreatedAt());
                result.add(orderMap);
            }
            
        } catch (Exception e) {
            log.error("获取待发货订单失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getMyOrders(Long companyId, Long userId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            QueryWrapper<Order> wrapper = new QueryWrapper<>();
            wrapper.eq("sales_user_id", userId);
            wrapper.eq("sales_company_id", companyId);
            wrapper.eq("deleted", false);
            wrapper.orderByDesc("created_at");
            wrapper.last("LIMIT " + limit);
            
            List<Order> orders = orderRepository.selectList(wrapper);
            
            for (Order order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderNo", order.getPlatformOrderId());
                orderMap.put("status", order.getOrderStatus());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("createdAt", order.getCreatedAt());
                result.add(orderMap);
            }
            
        } catch (Exception e) {
            log.error("获取我的订单失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentCustomers(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 这里需要CustomerRepository，暂时返回空列表
            // TODO: 实现客户查询逻辑
            log.info("获取最近客户功能暂未实现");
            
        } catch (Exception e) {
            log.error("获取最近客户失败", e);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentAITasks(Long companyId, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 这里需要AI相关Repository，暂时返回空列表
            // TODO: 实现AI任务查询逻辑
            log.info("获取AI导入历史功能暂未实现");
            
        } catch (Exception e) {
            log.error("获取AI导入历史失败", e);
        }
        
        return result;
    }
}
