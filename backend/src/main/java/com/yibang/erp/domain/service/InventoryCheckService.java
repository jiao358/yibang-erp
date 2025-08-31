package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.InventoryCheckRequest;
import com.yibang.erp.domain.entity.InventoryCheck;
import com.yibang.erp.domain.entity.InventoryCheckItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * 库存盘点服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface InventoryCheckService {

    /**
     * 创建库存盘点
     */
    InventoryCheck createCheck(InventoryCheckRequest request, Long operatorId);

    /**
     * 更新库存盘点
     */
    InventoryCheck updateCheck(Long id, InventoryCheckRequest request, Long operatorId);

    /**
     * 删除库存盘点
     */
    boolean deleteCheck(Long id, Long operatorId);

    /**
     * 根据ID查询库存盘点
     */
    InventoryCheck getCheckById(Long id);

    /**
     * 分页查询库存盘点列表
     */
    Page<InventoryCheck> getCheckPage(int page, int size, String checkType, String status,
                                     Long warehouseId, String startTime, String endTime);

    /**
     * 根据状态查询库存盘点
     */
    List<InventoryCheck> getChecksByStatus(String status);

    /**
     * 根据仓库ID查询库存盘点
     */
    List<InventoryCheck> getChecksByWarehouseId(Long warehouseId);

    /**
     * 根据盘点类型查询库存盘点
     */
    List<InventoryCheck> getChecksByType(String checkType);

    /**
     * 查询待审核的库存盘点
     */
    List<InventoryCheck> getPendingReview();

    /**
     * 开始盘点
     */
    boolean startCheck(Long id, Long operatorId);

    /**
     * 完成盘点
     */
    boolean completeCheck(Long id, Long operatorId);

    /**
     * 取消盘点
     */
    boolean cancelCheck(Long id, Long operatorId);

    /**
     * 审核盘点
     */
    boolean reviewCheck(Long id, String reviewStatus, String reviewComment, Long reviewerId);

    /**
     * 获取盘点明细
     */
    List<InventoryCheckItem> getCheckItems(Long checkId);

    /**
     * 获取有差异的盘点明细
     */
    List<InventoryCheckItem> getItemsWithDifference();

    /**
     * 添加盘点明细
     */
    boolean addCheckItem(Long checkId, InventoryCheckItem item, Long operatorId);

    /**
     * 更新盘点明细
     */
    boolean updateCheckItem(Long itemId, InventoryCheckItem item, Long operatorId);

    /**
     * 验证盘点明细
     */
    boolean verifyCheckItem(Long itemId, Long verifierId);
}
