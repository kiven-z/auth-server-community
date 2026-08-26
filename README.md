# auth-server-community

Auth 管理后台 **Community Edition** 后端仓库。基于 Spring Boot 3.5、Spring Cloud 2025、Spring Cloud Alibaba
构建，提供鉴权与系统业务能力。

配套前端：[auth-web-community](https://github.com/kiven-z/auth-web-community)

配套文档：https://github.com/kiven-z/auth-docs

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
- Maven 3.9+
- MySQL 9.x、Redis、Nacos
- MinIO/Aliyun OSS,
- （可选）ip2region xdb 文件

## 快速开始

| 服务             | 端口    |
|----------------|-------|
| auth-gateway   | 8080  |
| service-auth   | 20001 |
| service-system | 20002 |

## 代码规范

- 遵循 SonarQube Quality Gate。
- 代码格式：`./spring-format.sh`（spring-javaformat + Spotless）。

```bash
# 开始项目前执行
git config core.hooksPath .githooks
```

## 赞助

如果这个项目对你有帮助，欢迎打赏支持。

<table>
  <tr>
    <td align="center">
      <img src="./donate/WeChatPay.jpg" width="220" alt="微信" /><br/>
      微信
    </td>
    <td align="center">
      <img src="./donate/AliPay.jpg" width="220" alt="支付宝" /><br/>
      支付宝
    </td>
  </tr>
</table>

## 许可证

Copyright 2024-2026 Bunny。

本仓库基于 [Apache License 2.0](./LICENSE) 发布。
