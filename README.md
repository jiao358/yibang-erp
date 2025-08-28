# 智能供应链协同平台 (Smart Supply Chain Collaboration Platform)

## 项目简介

智能供应链协同平台是一个基于AI技术的现代化ERP系统，旨在打通供应链与销售之间的业务流与信息流，实现商品高效上架、订单智能处理与全程可视化跟踪，最终提升整体运营效率。

## 技术架构

### 前端技术栈
- **Vue 3** + TypeScript - 现代化前端框架
- **Element Plus** - 企业级UI组件库
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP客户端
- **ECharts** - 数据可视化图表
- **WebSocket** - 实时通信

### 后端技术栈
- **Spring Boot 3.x** - 企业级Java框架
- **Spring Security 6.x** - 安全框架
- **MyBatis Plus 3.x** - ORM框架
- **MySQL 8.0** - 主数据库
- **Redis 7.x** - 缓存和会话管理
- **JWT** - 身份认证
- **WebSocket + STOMP** - 实时通信

### 架构原则
- **DDD领域驱动设计** - 清晰的业务边界和领域模型
- **多租户架构** - 支持多公司数据隔离
- **微服务设计** - 模块化、可扩展的系统架构
- **最小化修改原则** - 确保代码质量和系统稳定性

## 核心功能模块

### 1. 用户管理
- 多角色权限管理（系统管理员、供应链管理员、供应链操作员、销售）
- 多租户数据隔离
- JWT身份认证

### 2. 商品管理
- 商品信息维护
- 库存管理
- 商品审核流程

### 3. 订单管理
- AI智能订单处理
- Excel文件智能解析
- 订单状态全程跟踪

### 4. 定价管理
- 多层级价格策略
- 销售目标管理
- 绩效分析

### 5. 站内消息系统
- 实时消息推送
- 消息模板管理
- 多渠道通知

### 6. 系统监控
- 系统健康监控
- 业务指标监控
- 异常告警系统

### 7. 数据分析
- GMV实时分析
- 数字大屏展示
- 智能预测分析

## 项目结构

```
yibang-erp/
├── frontend/                 # Vue 3 前端项目
│   ├── src/
│   │   ├── components/      # 通用组件
│   │   ├── views/          # 页面视图
│   │   ├── router/         # 路由配置
│   │   ├── store/          # 状态管理
│   │   ├── api/            # API接口
│   │   └── utils/          # 工具函数
│   ├── public/             # 静态资源
│   └── package.json        # 依赖配置
├── backend/                 # Spring Boot 后端项目
│   ├── src/main/java/
│   │   └── com/yibang/erp/
│   │       ├── controller/ # 控制器层
│   │       ├── service/    # 业务逻辑层
│   │       ├── repository/ # 数据访问层
│   │       ├── entity/     # 实体类
│   │       ├── config/     # 配置类
│   │       ├── security/   # 安全配置
│   │       ├── websocket/  # WebSocket配置
│   │       └── monitor/    # 监控模块
│   └── pom.xml             # Maven依赖
├── database/                # 数据库脚本
│   ├── init/               # 初始化脚本
│   ├── migration/          # 迁移脚本
│   └── config/             # 数据库配置
├── docker/                  # Docker配置文件
├── docs/                    # 项目文档
└── .tasks/                  # 任务管理
```

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.8+

### 开发环境搭建
1. 克隆项目
```bash
git clone [项目地址]
cd yibang-erp
```

2. 后端启动
```bash
cd backend
mvn spring-boot:run
```

3. 前端启动
```bash
cd frontend
npm install
npm run dev
```

4. 数据库初始化
```bash
cd database/init
# 执行初始化脚本
```

## 开发规范

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用统一的命名规范
- 添加完整的注释和文档

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 代码重构
- test: 测试相关
- chore: 构建过程或辅助工具的变动

## 部署说明

### Docker部署
```bash
docker-compose up -d
```

### 生产环境
- 支持多环境配置
- 自动化部署流程
- 监控和告警

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

[许可证信息]

## 联系方式

- 项目负责人：[姓名]
- 邮箱：[邮箱地址]
- 项目地址：[项目地址]

## 更新日志

### V1.3 (2024-01-14)
- 新增站内消息系统模块
- 新增系统监控模块
- 完善GMV分析和数字大屏功能

### V1.2 (2024-01-14)
- 新增数字大屏模块
- 新增GMV分析模块
- 新增价格分层管理模块

### V1.1 (2024-01-14)
- 新增AI管理模块
- 完善权限体系

### V1.0 (2023-10-27)
- 基础功能实现
- 用户认证和权限管理
- 商品和订单管理
