package com.yibang.erp.domain.service;

import com.yibang.erp.domain.dto.CustomerMatchRequest;
import com.yibang.erp.domain.dto.CustomerMatchResult;

import java.util.List;

/**
 * 客户匹配服务接口
 * 负责根据客户信息智能匹配系统中的客户
 */
public interface CustomerMatchingService {

    /**
     * 根据客户编码精确匹配客户
     * @param customerCode 客户编码
     * @return 客户匹配结果
     */
    CustomerMatchResult matchByCode(String customerCode);

    /**
     * 根据客户名称模糊匹配客户
     * @param customerName 客户名称
     * @return 客户匹配结果列表（按置信度排序）
     */
    List<CustomerMatchResult> matchByName(String customerName);

    /**
     * 根据联系电话匹配客户
     * @param phone 联系电话
     * @return 客户匹配结果
     */
    CustomerMatchResult matchByPhone(String phone);

    /**
     * 智能客户匹配（优先编码，其次名称和电话）
     * @param customerCode 客户编码
     * @param customerName 客户名称
     * @param phone 联系电话
     * @return 最佳匹配结果
     */
    CustomerMatchResult smartMatch(String customerCode, String customerName, String phone);

    /**
     * 批量客户匹配
     * @param requests 批量匹配请求
     * @return 批量匹配结果
     */
    List<CustomerMatchResult> batchMatch(List<CustomerMatchRequest> requests);

    /**
     * 获取客户匹配建议
     * @param customerInfo 客户信息
     * @return 匹配建议列表
     */
    List<CustomerMatchSuggestion> getMatchSuggestions(String customerInfo);

    /**
     * 客户匹配建议
     */
    class CustomerMatchSuggestion {
        private Long customerId;
        private String customerName;
        private String customerCode;
        private String contactPerson;
        private String contactPhone;
        private double confidence;
        private String reason;
        private String suggestion;

        // Getters and setters
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerCode() { return customerCode; }
        public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        public String getContactPhone() { return contactPhone; }
        public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
}
