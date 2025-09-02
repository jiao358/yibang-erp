import request from '@/utils/request'
import type { ProductImageResponse } from '@/types/product'

export const productImageApi = {
  // 上传单个商品图片
  uploadImage(file: File, productId: number, isPrimary: boolean = false, sortOrder: number = 0): Promise<ProductImageResponse> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('productId', productId.toString())
    formData.append('isPrimary', isPrimary.toString())
    formData.append('sortOrder', sortOrder.toString())
    
    return request.post('/api/product-images/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 批量上传商品图片
  uploadImages(files: File[], productId: number, isPrimary: boolean = false, sortOrder: number = 0): Promise<ProductImageResponse[]> {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    formData.append('productId', productId.toString())
    formData.append('isPrimary', isPrimary.toString())
    formData.append('sortOrder', sortOrder.toString())
    
    return request.post('/api/product-images/upload/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取商品图片列表
  getProductImages(productId: number): Promise<ProductImageResponse[]> {
    return request.get(`/api/product-images/product/${productId}`)
  },

  // 获取商品主图
  getPrimaryImage(productId: number): Promise<ProductImageResponse> {
    return request.get(`/api/product-images/product/${productId}/primary`)
  },

  // 设置主图
  setPrimaryImage(imageId: number): Promise<ProductImageResponse> {
    return request.put(`/api/product-images/${imageId}/primary`)
  },

  // 删除图片
  deleteImage(imageId: number): Promise<void> {
    return request.delete(`/api/product-images/${imageId}`)
  },

  // 删除商品的所有图片
  deleteProductImages(productId: number): Promise<void> {
    return request.delete(`/api/product-images/product/${productId}`)
  },

  // 更新图片排序
  updateImageSort(imageIds: number[]): Promise<void> {
    return request.put('/api/product-images/sort', imageIds)
  }
}
