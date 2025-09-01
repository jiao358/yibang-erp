package com.yibang.erp.domain.dto;

public class OrderAddressDTO {

    private String provinceName;

    private String cityName;
    private String districtName;

    /**
     * ai置信度
     */
    private Double confidence;

    /**
     * Ai模型修复后地址
     */
    private String fixAddress;
    
    /**
     * 省份代码
     */
    private String provinceCode;
    
    /**
     * 城市代码
     */
    private String cityCode;
    
    /**
     * 区域代码
     */
    private String districtCode;


    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getFixAddress() {
        return fixAddress;
    }

    public void setFixAddress(String fixAddress) {
        this.fixAddress = fixAddress;
    }
    
    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }
}
