package com.yibang.erp.controller;

import com.yibang.erp.domain.dto.ProductImageResponse;
import com.yibang.erp.domain.dto.ProductImageUploadRequest;
import com.yibang.erp.domain.service.ProductImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 商品图片管理控制器
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/product-images")
@PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'SUPPLIER_ADMIN', 'SALES')")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    /**
     * 上传单个商品图片
     */
    @PostMapping("/upload")
    public ResponseEntity<ProductImageResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("productId") Long productId,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary,
            @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder) {
        
        try {
            log.info("上传商品图片请求，商品ID: {}, 文件名: {}", productId, file.getOriginalFilename());
            
            ProductImageUploadRequest request = new ProductImageUploadRequest();
            request.setProductId(productId);
            request.setIsPrimary(isPrimary);
            request.setSortOrder(sortOrder);
            
            ProductImageResponse response = productImageService.uploadProductImage(file, request);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("上传商品图片失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量上传商品图片
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<List<ProductImageResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("productId") Long productId,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary,
            @RequestParam(value = "sortOrder", defaultValue = "0") Integer sortOrder) {
        
        try {
            log.info("批量上传商品图片请求，商品ID: {}, 文件数量: {}", productId, files.size());
            
            ProductImageUploadRequest request = new ProductImageUploadRequest();
            request.setProductId(productId);
            request.setIsPrimary(isPrimary);
            request.setSortOrder(sortOrder);
            
            List<ProductImageResponse> responses = productImageService.uploadProductImages(files, request);
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            log.error("批量上传商品图片失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取商品图片列表
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(@PathVariable Long productId) {
        try {
            log.info("获取商品图片列表请求，商品ID: {}", productId);
            
            List<ProductImageResponse> images = productImageService.getProductImages(productId);
            
            return ResponseEntity.ok(images);
            
        } catch (Exception e) {
            log.error("获取商品图片列表失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取商品主图
     */
    @GetMapping("/product/{productId}/primary")
    public ResponseEntity<ProductImageResponse> getPrimaryImage(@PathVariable Long productId) {
        try {
            log.info("获取商品主图请求，商品ID: {}", productId);
            
            ProductImageResponse image = productImageService.getPrimaryImage(productId);
            
            return ResponseEntity.ok(image);
            
        } catch (Exception e) {
            log.error("获取商品主图失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 设置主图
     */
    @PutMapping("/{imageId}/primary")
    public ResponseEntity<ProductImageResponse> setPrimaryImage(@PathVariable Long imageId) {
        try {
            log.info("设置主图请求，图片ID: {}", imageId);
            
            ProductImageResponse response = productImageService.setPrimaryImage(imageId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置主图失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除图片
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        try {
            log.info("删除图片请求，图片ID: {}", imageId);
            
            productImageService.deleteImage(imageId);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("删除图片失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除商品的所有图片
     */
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteProductImages(@PathVariable Long productId) {
        try {
            log.info("删除商品所有图片请求，商品ID: {}", productId);
            
            productImageService.deleteProductImages(productId);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("删除商品所有图片失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新图片排序
     */
    @PutMapping("/sort")
    public ResponseEntity<Void> updateImageSort(@RequestBody List<Long> imageIds) {
        try {
            log.info("更新图片排序请求，图片ID列表: {}", imageIds);
            
            productImageService.updateImageSort(imageIds);
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            log.error("更新图片排序失败", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
