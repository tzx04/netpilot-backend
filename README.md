# NetPilot 智能网络管理平台（后端）

一个面向中小企业的智能网络运维平台，支持设备纳管、在线监测、SNMP 采集、AI 智能诊断。

## 🛠 技术栈

- **后端框架**：Spring Boot 4.x / Spring Security / JWT
- **持久层**：MyBatis-Plus / MySQL
- **缓存**：Redis
- **网络采集**：SNMP4J
- **AI 能力**：DeepSeek API（流式输出 + 智能诊断）

## ✨ 核心功能

- **用户鉴权**：JWT 无状态登录，401 自动跳转
- **设备管理**：设备增删改查 + 在线状态检测（Ping）
- **定时监测**：定时任务检测设备状态，SNMP 采集 CPU / 内存 / 带宽
- **AI 智能诊断**：结合真实设备数据生成诊断报告，自动持久化
- **数据可视化**：提供监控数据接口，支持前端 ECharts 大屏

## 🚀 快速开始

1. 创建 MySQL 数据库 `netpilot`，导入 `sql/init.sql`
2. 复制 `application.yml` 为 `application-local.yml`，填入真实数据库密码、JWT secret、DeepSeek API Key
3. 启动 Redis
4. 运行 `NetpilotBackendApplication`
5. 后端服务地址：http://localhost:8080/api

## 🔑 主要接口

| 接口 | 说明 |
|---|---|
| `POST /api/auth/login` | 用户登录 |
| `POST /api/auth/register` | 用户注册 |
| `GET /api/devices` | 设备列表 |
| `POST /api/devices` | 新增设备 |
| `GET /api/ai/diagnose/{id}` | AI 智能诊断 |
| `GET /api/ai/history/{id}` | 诊断历史 |
| `POST /api/ai/chat/stream` | 流式 AI 对话 |
| `GET /api/monitor/latest-all` | 所有设备最新监控数据 |

## 📷 项目截图

（可补充设备列表 / AI 诊断 / 监控大屏截图）

## 🔗 前端仓库

[netpilot-frontend](https://github.com/tzx04/netpilot-frontend)
