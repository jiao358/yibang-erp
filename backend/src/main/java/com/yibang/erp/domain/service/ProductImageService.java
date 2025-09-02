package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.ProductImageResponse;
import com.yibang.erp.domain.dto.ProductImageUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商品图片服务接口
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
public interface ProductImageService {

    /**
     * 上传商品图片
     */
    ProductImageResponse uploadProductImage(MultipartFile file, ProductImageUploadRequest request);

    /**
     * 批量上传商品图片
     */
    List<ProductImageResponse> uploadProductImages(List<MultipartFile> files, ProductImageUploadRequest request);

    /**
     * 根据商品ID获取图片列表
     */
    List<ProductImageResponse> getProductImages(Long productId);

    /**
     * 根据商品ID获取主图
     */
    ProductImageResponse getPrimaryImage(Long productId);

    /**
     * 设置主图
     */
    ProductImageResponse setPrimaryImage(Long imageId);

    /**
     * 删除图片
     */
    void deleteImage(Long imageId);

    /**
     * 删除商品的所有图片
     */
    void deleteProductImages(Long productId);

    /**
     * 更新图片排序
     */
    void updateImageSort(List<Long> imageIds);
}
