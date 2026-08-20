# auth-server-community

Auth 管理后台 **Community Edition** 后端仓库。基于 Spring Boot 3.5、Spring Cloud 2025、Spring Cloud Alibaba
构建，提供鉴权、网关限流与系统业务能力。

配套前端：[auth-web-community](https://github.com/kiven-z/auth-web-community)。

## Community Edition 说明

本仓库为开源社区版，与闭源完整版共享同一套数据库 schema 与核心代码结构，便于问题修复双向合并。部分高级能力将在后续版本中从对外入口层裁剪；表结构与底层服务代码现阶段保持对齐。

## 技术栈

| 类别      | 选型                                                                      |
|---------|-------------------------------------------------------------------------|
| 运行时     | JDK 17                                                                  |
| 框架      | Spring Boot 3.5.x、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0 |
| 构建      | Apache Maven 3.9.x（CI-friendly `${revision}`）                           |
| 持久层     | MyBatis-Plus 3.5、HikariCP、dynamic-datasource                            |
| 数据库     | MySQL 9.x                                                               |
| 缓存/会话   | Redis（验证码、限流）                                                           |
| 注册/配置中心 | Nacos                                                                   |
| 对象存储    | AWS SDK v2（S3 协议），本地推荐 MinIO                                            |
| 鉴权      | JJWT 0.12、自研 Security 二次封装                                              |
| 文档      | springdoc-openapi 2.8                                                   |
| 工具      | Hutool、MapStruct、EasyExcel、Quartz、ip2region、yauaa                       |

## 模块结构

``` 
auth-server-community
├── commons/                 # 公共基础库
│   ├── common-core
│   ├── common-data
│   ├── common-mapstruct
│   └── common-starter-*     # cache / ip / jwt / restTemplate / web / webdoc
├── modules/                 # 可复用业务模块
│   ├── module-file / module-file-api
│   ├── module-message-api
│   ├── module-platform-persistence
│   └── module-security-*    # core / data-permission / web-starter / contract / autoconfigure
├── services/                # 可独立部署的业务服务
│   ├── service-auth         # 鉴权 / JWT 签发
│   ├── service-system       # 系统业务（用户、部门、消息、文件、调度等）
│   └── service-example      # 示例工程
└── auth-gateway/            # 网关 / 限流
```

`service-system` 按 `admin / authorization / bootstrap / common / file / message / schedule` 二级拆分。

## 环境要求

- JDK 17+
- Maven 3.9+（或使用仓库内 `./mvnw`）
- MySQL 9.x、Redis、Nacos（本地开发可用 Docker Compose 拉起中间件）
- 可选：MinIO、ip2region xdb 文件（见 `docker/compose.yml` 注释）

## 快速开始

```bash
# 编译（默认 dev profile）
./mvnw -T 1C clean install -DskipTests

# 单服务运行（以 service-system 为例）
cd services/service-system/service-system-bootstrap
../../mvnw spring-boot:run

# 切换 profile
./mvnw -P prod clean package
```

默认服务端口：

| 服务             | 端口    |
|----------------|-------|
| auth-gateway   | 8080  |
| service-auth   | 20001 |
| service-system | 20002 |

## 容器化部署

`docker/` 提供运行时 compose 与本地构建 override：

```bash
cd docker
./dockerctl.sh dev build-up    # 本地构建并启动

# 使用远程镜像仓库（需先 docker login）
export IMAGE_PREFIX=ghcr.io/<your-org>
export IMAGE_TAG=latest
./dockerctl.sh prod up
```

变量与挂载说明见 `docker/compose.yml` 顶部注释。

## 代码规范

- 遵循《阿里巴巴 Java 开发手册（泰山版）》与 SonarQube Quality Gate。
- 代码格式：`./spring-format.sh`（spring-javaformat + Spotless）。

## 许可证

Copyright 2024-2026 Bunny。

本仓库基于 [Apache License 2.0](./LICENSE) 发布。
