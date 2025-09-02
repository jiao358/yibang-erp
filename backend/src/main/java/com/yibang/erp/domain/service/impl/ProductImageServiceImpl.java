package com.yibang.erp.domain.service.impl;

import com.yibang.erp.common.util.UserSecurityUtils;
import com.yibang.erp.domain.dto.ProductImageResponse;
import com.yibang.erp.domain.dto.ProductImageUploadRequest;
import com.yibang.erp.domain.entity.ProductImage;
import com.yibang.erp.domain.service.ProductImageService;
import com.yibang.erp.infrastructure.repository.ProductImageRepository;
import com.yibang.erp.infrastructure.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 商品图片服务实现类
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Service
public class ProductImageServiceImpl implements ProductImageService {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.image.path}")
    private String imagePath;

    @Value("${file.upload.image.max-size}")
    private String maxSize;

    @Value("${file.upload.image.allowed-types}")
    private String allowedTypes;

    @Value("${file.upload.access.base-url}")
    private String accessBaseUrl;

    @Override
    @Transactional
    public ProductImageResponse uploadProductImage(MultipartFile file, ProductImageUploadRequest request) {
        try {
            // 验证文件
            validateFile(file);

            // 验证商品是否存在
            if (productRepository.selectById(request.getProductId()) == null) {
                throw new RuntimeException("商品不存在");
            }

            // 生成文件路径
            String fileName = generateFileName(file.getOriginalFilename());
            String relativePath = generateRelativePath(request.getProductId(), fileName);
            String fullPath = uploadPath + relativePath;

            // 创建目录
            createDirectoryIfNotExists(fullPath);

            // 保存文件
            file.transferTo(new File(fullPath));

            // 获取图片尺寸
            BufferedImage image = ImageIO.read(new File(fullPath));
            int width = image.getWidth();
            int height = image.getHeight();

            // 生成访问URL
            String imageUrl = accessBaseUrl + relativePath;

            // 保存到数据库
            ProductImage productImage = new ProductImage();
            productImage.setProductId(request.getProductId());
            productImage.setImageName(file.getOriginalFilename());
            productImage.setImageUrl(imageUrl);
            productImage.setImagePath(relativePath);
            productImage.setImageSize(file.getSize());
            productImage.setImageType(getFileExtension(file.getOriginalFilename()));
            productImage.setImageWidth(width);
            productImage.setImageHeight(height);
            productImage.setIsPrimary(request.getIsPrimary());
            productImage.setSortOrder(request.getSortOrder());
            productImage.setStatus(1);
            productImage.setCreatedAt(LocalDateTime.now());
            productImage.setUpdatedAt(LocalDateTime.now());
            productImage.setCreatedBy(getCurrentUserId());
            productImage.setUpdatedBy(getCurrentUserId());

            // 如果设为主图，先清除其他主图
            if (request.getIsPrimary()) {
                productImageRepository.clearPrimaryByProductId(request.getProductId());
            }

            productImageRepository.insert(productImage);

            // 转换为响应DTO
            return convertToResponse(productImage);

        } catch (Exception e) {
            log.error("上传商品图片失败", e);
            throw new RuntimeException("上传商品图片失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public List<ProductImageResponse> uploadProductImages(List<MultipartFile> files, ProductImageUploadRequest request) {
        List<ProductImageResponse> responses = new ArrayList<>();
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            ProductImageUploadRequest uploadRequest = new ProductImageUploadRequest();
            BeanUtils.copyProperties(request, uploadRequest);
            uploadRequest.setSortOrder(request.getSortOrder() + i);
            uploadRequest.setIsPrimary(i == 0 && request.getIsPrimary()); // 只有第一张图片设为主图
            
            ProductImageResponse response = uploadProductImage(file, uploadRequest);
            responses.add(response);
        }
        
        return responses;
    }

    @Override
    public List<ProductImageResponse> getProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.selectByProductId(productId);
        return images.stream().map(this::convertToResponse).toList();
    }

    @Override
    public ProductImageResponse getPrimaryImage(Long productId) {
        ProductImage image = productImageRepository.selectPrimaryByProductId(productId);
        return image != null ? convertToResponse(image) : null;
    }

    @Override
    @Transactional
    public ProductImageResponse setPrimaryImage(Long imageId) {
        ProductImage image = productImageRepository.selectById(imageId);
        if (image == null) {
            throw new RuntimeException("图片不存在");
        }

        // 清除该商品的其他主图
        productImageRepository.clearPrimaryByProductId(image.getProductId());

        // 设置当前图片为主图
        image.setIsPrimary(true);
        image.setUpdatedAt(LocalDateTime.now());
        image.setUpdatedBy(getCurrentUserId());
        productImageRepository.updateById(image);

        return convertToResponse(image);
    }

    @Override
    @Transactional
    public void deleteImage(Long imageId) {
        ProductImage image = productImageRepository.selectById(imageId);
        if (image == null) {
            throw new RuntimeException("图片不存在");
        }

        // 软删除
        image.setStatus(0);
        image.setUpdatedAt(LocalDateTime.now());
        image.setUpdatedBy(getCurrentUserId());
        productImageRepository.updateById(image);

        // 删除物理文件
        try {
            String fullPath = uploadPath + image.getImagePath();
            Files.deleteIfExists(Paths.get(fullPath));
        } catch (IOException e) {
            log.warn("删除图片文件失败: {}", image.getImagePath(), e);
        }
    }

    @Override
    @Transactional
    public void deleteProductImages(Long productId) {
        List<ProductImage> images = productImageRepository.selectByProductId(productId);
        
        for (ProductImage image : images) {
            // 删除物理文件
            try {
                String fullPath = uploadPath + image.getImagePath();
                Files.deleteIfExists(Paths.get(fullPath));
            } catch (IOException e) {
                log.warn("删除图片文件失败: {}", image.getImagePath(), e);
            }
        }
        
        // 软删除数据库记录
        productImageRepository.deleteByProductId(productId);
    }

    @Override
    @Transactional
    public void updateImageSort(List<Long> imageIds) {
        for (int i = 0; i < imageIds.size(); i++) {
            ProductImage image = productImageRepository.selectById(imageIds.get(i));
            if (image != null) {
                image.setSortOrder(i);
                image.setUpdatedAt(LocalDateTime.now());
                image.setUpdatedBy(getCurrentUserId());
                productImageRepository.updateById(image);
            }
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 检查文件大小
        long maxSizeBytes = parseSize(maxSize);
        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("文件大小不能超过 " + maxSize);
        }

        // 检查文件类型
        String extension = getFileExtension(file.getOriginalFilename());
        if (!isAllowedType(extension)) {
            throw new RuntimeException("不支持的文件类型: " + extension);
        }
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return timestamp + "_" + uuid + "." + extension;
    }

    /**
     * 生成相对路径
     */
    private String generateRelativePath(Long productId, String fileName) {
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return imagePath + productId + "/" + datePath + "/" + fileName;
    }

    /**
     * 创建目录
     */
    private void createDirectoryIfNotExists(String fullPath) throws IOException {
        Path path = Paths.get(fullPath);
        Files.createDirectories(path.getParent());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (StringUtils.hasText(filename) && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 检查是否为允许的文件类型
     */
    private boolean isAllowedType(String extension) {
        String[] types = allowedTypes.split(",");
        for (String type : types) {
            if (type.trim().equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析文件大小
     */
    private long parseSize(String size) {
        if (size.endsWith("MB")) {
            return Long.parseLong(size.replace("MB", "")) * 1024 * 1024;
        } else if (size.endsWith("KB")) {
            return Long.parseLong(size.replace("KB", "")) * 1024;
        } else if (size.endsWith("B")) {
            return Long.parseLong(size.replace("B", ""));
        }
        return 10 * 1024 * 1024; // 默认10MB
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        try {
            return UserSecurityUtils.getCurrentUserId();
        } catch (Exception e) {
            return 1L; // 默认用户ID
        }
    }

    /**
     * 转换为响应DTO
     */
    private ProductImageResponse convertToResponse(ProductImage image) {
        ProductImageResponse response = new ProductImageResponse();
        BeanUtils.copyProperties(image, response);
        return response;
    }
}
