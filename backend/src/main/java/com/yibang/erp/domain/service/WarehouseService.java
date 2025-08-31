package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.WarehouseRequest;
import com.yibang.erp.domain.entity.Warehouse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 仓库管理服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface WarehouseService {

    /**
     * 创建仓库
     */
    Warehouse createWarehouse(WarehouseRequest request, Long operatorId);

    /**
     * 更新仓库
     */
    Warehouse updateWarehouse(Long id, WarehouseRequest request, Long operatorId);

    /**
     * 删除仓库
     */
    boolean deleteWarehouse(Long id, Long operatorId);

    /**
     * 根据ID查询仓库
     */
    Warehouse getWarehouseById(Long id);

    /**
     * 分页查询仓库列表
     */
    Page<Warehouse> getWarehousePage(int page, int size, String warehouseCode, String warehouseName, 
                                    String warehouseType, String status, Long companyId);

    /**
     * 根据公司ID查询仓库列表
     */
    List<Warehouse> getWarehousesByCompanyId(Long companyId);

    /**
     * 根据状态查询仓库列表
     */
    List<Warehouse> getWarehousesByStatus(String status);

    /**
     * 启用仓库
     */
    boolean activateWarehouse(Long id, Long operatorId);

    /**
     * 停用仓库
     */
    boolean deactivateWarehouse(Long id, Long operatorId);

    /**
     * 维护仓库
     */
    boolean maintainWarehouse(Long id, Long operatorId);
}
