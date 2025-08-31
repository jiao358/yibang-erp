package com.yibang.erp.domain.enums;

/**
 * 订单来源枚举
 */
public enum OrderSource {
    
    /**
     * 人工填写
     */
    MANUAL("MANUAL", "人工填写", "用户通过界面手动填写订单信息"),
    
    /**
     * Excel导入
     */
    EXCEL("EXCEL", "Excel导入", "通过Excel文件批量导入订单"),
    
    /**
     * 图片识别
     */
    IMAGE("IMAGE", "图片识别", "通过AI识别图片中的订单信息"),
    
    /**
     * API接口
     */
    API("API", "API接口", "通过外部API接口创建订单"),
    
    /**
     * PDF解析
     */
    PDF("PDF", "PDF解析", "通过AI解析PDF文档中的订单信息"),
    
    /**
     * Word文档
     */
    WORD("WORD", "Word文档", "通过AI解析Word文档中的订单信息"),
    
    /**
     * 邮件解析
     */
    EMAIL("EMAIL", "邮件解析", "通过AI解析邮件中的订单信息"),
    
    /**
     * 语音识别
     */
    VOICE("VOICE", "语音识别", "通过AI识别语音中的订单信息");

    private final String code;
    private final String name;
    private final String description;

    OrderSource(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取来源枚举
     */
    public static OrderSource fromCode(String code) {
        for (OrderSource source : values()) {
            if (source.code.equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("Unknown order source code: " + code);
    }

    /**
     * 检查是否需要AI处理
     */
    public boolean requiresAIProcessing() {
        return this == IMAGE || this == PDF || this == WORD || this == EMAIL || this == VOICE;
    }

    /**
     * 检查是否支持批量处理
     */
    public boolean supportsBatchProcessing() {
        return this == EXCEL || this == API;
    }

    /**
     * 获取处理优先级（数字越小优先级越高）
     */
    public int getPriority() {
        switch (this) {
            case API:
                return 1; // API接口优先级最高
            case MANUAL:
                return 2; // 人工填写次之
            case EXCEL:
                return 3; // Excel导入
            case IMAGE:
                return 4; // 图片识别
            case PDF:
                return 5; // PDF解析
            case WORD:
                return 6; // Word解析
            case EMAIL:
                return 7; // 邮件解析
            case VOICE:
                return 8; // 语音识别优先级最低
            default:
                return 9;
        }
    }
}
