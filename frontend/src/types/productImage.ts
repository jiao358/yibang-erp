/**
 * 商品图片相关类型定义
 */

export interface ProductImageResponse {
  id: number
  productId: number
  imageName: string
  imageUrl: string
  imageSize: number
  imageType: string
  imageWidth: number
  imageHeight: number
  isPrimary: boolean
  sortOrder: number
  createdAt: string
}

export interface ProductImageUploadRequest {
  productId: number
  isPrimary?: boolean
  sortOrder?: number
}

export interface ProductImageUploadResult {
  success: boolean
  data?: ProductImageResponse
  error?: string
}
