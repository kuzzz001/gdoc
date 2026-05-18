# Gdoc（吉智文档）— 多人实时协同编辑系统

Gdoc 是一款基于 **Java 17 + Spring Boot 3.2.5** 构建的多人实时协同文档编辑系统。支持富文本编辑、OT 算法冲突解决、即时通讯、好友社交、历史版本管理等功能。

---

## 功能特性

### 文档协作
- **富文本编辑器**：支持字体/字号、粗体/斜体/下划线/删除线、颜色选择、对齐方式、列表、标题、引用、链接、分割线等格式化功能
- **多人实时协同**：基于 WebSocket + STOMP 协议，支持多人同时编辑同一文档
- **OT 冲突解决**：自研 OT（Operation Transformation）算法，确保并发编辑下文档内容最终一致
- **光标同步**：实时显示协作者光标位置，不同用户分配不同颜色
- **文档分享**：生成分享链接，支持只读/可编辑权限，可设置有效期
- **协作者管理**：添加/移除协作者，支持编辑者/查看者角色切换

### 用户系统
- **自动账号分配**：注册时系统自动分配 6 位唯一账号（如 000001）
- **JWT 认证**：基于 JJWT 0.12.5，Token 自动续期
- **头像管理**：8 种默认 SVG 头像 + 自定义上传，自动裁剪压缩

### 社交功能（微信风格）
- **好友系统**：搜索用户、发送/接收好友请求、删除好友
- **即时通讯**：一对一文本消息、图片消息、文件传输，WebSocket 实时推送
- **消息状态**：已发送 → 已送达 → 已读
- **未读提示**：红色徽章呼吸动画，消息摘要预览
- **文档协作邀请**：从好友列表发送文档协作邀请，支持权限选择

### 版本管理
- **自动快照**：每 50 次操作自动创建版本快照
- **历史查询**：查看文档历史版本
- **版本回滚**：一键回滚到指定版本

### 部署运维
- **双数据库支持**：MySQL 8.0 / PostgreSQL
- **Redis 分布式支持**：Pub/Sub 跨节点消息同步 + 分布式锁
- **Docker 容器化部署**：Docker Compose 一键启动
- **Render 云部署**：支持 Blueprint 自动部署

---

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.5 |
| 安全 | Spring Security + JWT | 0.12.5 |
| ORM | MyBatis-Plus | 3.5.6 |
| 连接池 | Druid | 1.2.22 |
| 数据库 | MySQL / PostgreSQL | 8.0 / 16 |
| 缓存 | Redis | 7 |
| 实时通信 | WebSocket + STOMP (SockJS) | - |
| 算法 | OT (Operation Transformation) | 自研 |
| API 文档 | Knife4j | 4.5.0 |
| 工具库 | Hutool | 5.8.27 |
| 构建工具 | Maven | 3.9+ |
| 容器化 | Docker + Docker Compose | - |

---

## 项目架构

```
gdoc-parent
├── gdoc-common          # 公共模块：工具类、常量、异常定义、统一响应
├── gdoc-model           # 数据模型：Entity、DTO、VO
├── gdoc-security        # 安全模块：JWT 工具、Spring Security 配置
├── gdoc-user            # 用户模块：注册、登录、用户信息管理
├── gdoc-document        # 文档模块：文档 CRUD、分享、权限管理
├── gdoc-collaboration   # 协同模块：WebSocket、OT 引擎、房间管理
├── gdoc-history         # 历史模块：版本快照、操作日志、回滚
├── gdoc-social          # 社交模块：好友、即时通讯、协作邀请
└── gdoc-server          # 启动模块：Spring Boot 入口、全局配置
```

---

## 数据库设计

系统共包含 **9 张业务表**：

| 表名 | 说明 |
|------|------|
| `gdoc_user` | 用户表（账号、密码、昵称、头像） |
| `gdoc_document` | 文档表（标题、内容、版本号、逻辑删除） |
| `gdoc_share` | 分享链接表（Token、权限、有效期） |
| `gdoc_collaborator` | 协作者表（用户-文档关联、角色） |
| `gdoc_snapshot` | 版本快照表（内容、版本号） |
| `gdoc_operation_log` | 操作日志表（操作 JSON、版本号） |
| `gdoc_friendship` | 好友关系表（状态：待确认/已接受/已拒绝/已拉黑） |
| `gdoc_message` | 消息表（文本/图片/文件/系统消息） |
| `gdoc_collab_invitation` | 协作邀请表（邀请人、被邀请人、权限、状态） |

---

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.9+
- Docker & Docker Compose（可选，用于启动中间件）

### 方式一：Docker Compose 一键启动（推荐）

```bash
# 1. 启动 MySQL + Redis
docker-compose up -d mysql redis

# 2. 编译项目
mvn clean package -DskipTests

# 3. 修改 application-dev.yml 配置数据库连接信息

# 4. 启动服务
java -jar gdoc-server/target/gdoc-server-1.0.0-SNAPSHOT.jar
```

### 方式二：本地开发

```bash
# 1. 启动 MySQL 和 Redis（本地安装或 Docker）

# 2. 创建数据库 gdoc，执行初始化脚本
# MySQL: docker/init/mysql/init.sql
# PostgreSQL: docker/init/postgres/init.sql

# 3. 编译并启动
mvn clean package -DskipTests -Pdev
java -jar gdoc-server/target/gdoc-server-1.0.0-SNAPSHOT.jar
```

### 方式三：前端开发模式（v1.2.1+）

```bash
# 1. 启动后端服务（方式一或方式二）

# 2. 进入前端项目
cd gdoc-web

# 3. 安装依赖
npm install

# 4. 启动开发服务器（自动代理到后端 8080 端口）
npm run dev

# 5. 访问 http://localhost:5173
```

### 访问

| 地址 | 说明 |
|------|------|
| `http://localhost:5173` | 前端开发服务器（v1.2.1+） |
| `http://localhost:8080` | 生产构建（前端静态资源） |
| `http://localhost:8080/doc.html` | Knife4j API 文档 |

---

## API 概览

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |

### 用户接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/me` | 获取当前用户信息 |
| PUT | `/api/user/avatar` | 更新头像 |

### 文档接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/docs` | 创建文档 |
| GET | `/api/docs` | 文档列表（分页） |
| GET | `/api/docs/{id}` | 文档详情 |
| PUT | `/api/docs/{id}` | 更新文档 |
| DELETE | `/api/docs/{id}` | 删除文档 |

### 分享接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/docs/{id}/share` | 创建分享链接 |
| GET | `/api/docs/{id}/shares` | 列出分享链接 |
| DELETE | `/api/docs/{id}/shares/{token}` | 撤销分享 |
| GET | `/api/docs/share/{token}` | 通过分享链接访问 |

### 协作者接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/docs/{id}/collaborators` | 查看协作者列表 |
| POST | `/api/docs/{id}/collaborators` | 添加协作者 |
| PUT | `/api/docs/{id}/collaborators/{userId}` | 修改协作者角色 |
| DELETE | `/api/docs/{id}/collaborators/{userId}` | 移除协作者 |

### WebSocket 端点

| 端点 | 说明 |
|------|------|
| `/ws` | WebSocket 连接端点（支持 SockJS） |
| `/app/doc/{docId}/join` | 加入文档房间 |
| `/app/doc/{docId}/leave` | 离开文档房间 |
| `/app/doc/{docId}/content` | 推送内容更新 |
| `/app/doc/{docId}/cursor` | 推送光标位置 |
| `/ws/chat` | 聊天 WebSocket 端点 |

### 社交接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/social/users/search` | 搜索用户 |
| POST | `/api/social/friends/request` | 发送好友请求 |
| PUT | `/api/social/friends/request/{id}` | 处理好友请求 |
| GET | `/api/social/friends` | 好友列表 |
| DELETE | `/api/social/friends/{userId}` | 删除好友 |
| GET | `/api/social/messages/{friendId}` | 聊天记录 |
| POST | `/api/social/invitations` | 发送协作邀请 |
| GET | `/api/social/invitations` | 邀请列表 |
| PUT | `/api/social/invitations/{id}` | 处理协作邀请 |

---

## 部署

### Render 部署

项目支持一键部署到 Render：

1. Fork 本仓库
2. 在 Render Dashboard 中创建 Blueprint
3. 连接仓库，Render 自动识别 `render.yaml`
4. 配置环境变量：
   - `DATABASE_URL` - PostgreSQL 连接地址
   - `DATABASE_USERNAME` - 数据库用户名
   - `DATABASE_PASSWORD` - 数据库密码
   - `REDIS_HOST` - Redis 地址
   - `REDIS_PORT` - Redis 端口
   - `JWT_SECRET` - JWT 密钥（自动生成）
5. 部署完成即可访问

### Docker 部署

```bash
# 构建镜像
docker build -t gdoc-server .

# 运行
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://host:5432/gdoc \
  -e DATABASE_USERNAME=gdoc \
  -e DATABASE_PASSWORD=gdoc123 \
  -e REDIS_HOST=host \
  -e REDIS_PORT=6379 \
  -v /path/to/uploads:/app/uploads \
  gdoc-server
```

---

## 前端架构（v1.2.1+）

v1.2.1 版本完成了前端工程化改造，从 CDN 引入方式迁移至现代化的 SPA 架构：

```
gdoc-web/
├── src/
│   ├── api/              # API 请求层
│   │   ├── request.ts    # axios 实例 + 拦截器
│   │   ├── auth.ts       # 认证接口
│   │   ├── document.ts   # 文档/分享/协作者接口
│   │   └── social.ts     # 社交/消息/邀请接口
│   ├── assets/styles/    # 全局样式
│   │   └── global.scss   # CSS 变量 + 基础样式
│   ├── components/       # 组件库
│   │   ├── common/       # 通用组件
│   │   │   ├── Avatar.vue       # 头像组件
│   │   │   ├── Modal.vue        # 弹窗组件
│   │   │   └── Pagination.vue   # 分页组件
│   │   ├── editor/       # 编辑器组件
│   │   │   ├── Toolbar.vue       # 格式工具栏
│   │   │   └── ImagePreview.vue  # 图片预览
│   │   └── social/       # 社交组件
│   │       ├── FriendCard.vue      # 好友卡片
│   │       ├── ChatList.vue        # 聊天列表
│   │       └── InvitationCard.vue  # 邀请卡片
│   ├── router/           # 路由配置
│   │   ├── index.ts      # 路由守卫
│   │   └── routes.ts     # 路由定义
│   ├── stores/           # Pinia 状态管理
│   │   ├── user.ts       # 用户状态
│   │   ├── document.ts   # 文档状态
│   │   ├── social.ts     # 社交状态
│   │   └── collab.ts     # 协同状态
│   ├── types/            # TypeScript 类型定义
│   │   └── index.ts      # 所有业务类型
│   ├── views/            # 页面组件
│   │   ├── Login.vue         # 登录/注册页
│   │   ├── DocumentList.vue  # 文档列表页
│   │   ├── Editor.vue        # 文档编辑器页
│   │   ├── Social.vue        # 社交页
│   │   └── ShareView.vue     # 分享查看页
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── vite.config.ts        # Vite 配置
├── tsconfig.app.json     # TypeScript 配置
└── package.json          # 依赖管理
```

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.5+ | 组合式 API |
| TypeScript | 6.0+ | 类型安全 |
| Vite | 8.0+ | 构建工具 |
| Vue Router | 4.x | 前端路由 |
| Pinia | 3.x | 状态管理 |
| Axios | 1.x | HTTP 客户端 |
| Sass | - | CSS 预处理器 |

---

## 开发计划

| 版本 | 状态 | 内容 |
|------|------|------|
| 0.0.1 | ✅ 已完成 | 项目初始化、环境搭建、基础配置 |
| 0.1.1 | ✅ 已完成 | 用户系统、文档基础 CRUD |
| 0.2.1 | ✅ 已完成 | 分享与权限控制 |
| 0.3.1 | ✅ 已完成 | WebSocket 基础、房间管理 |
| 0.4.1 | ✅ 已完成 | OT 算法（纯文本） |
| 0.5.1 | ✅ 已完成 | 光标同步、用户存在感知 |
| 0.6.1 | ✅ 已完成 | 富文本格式支持 |
| 0.7.1 | ✅ 已完成 | 版本管理与历史回滚 |
| 0.8.1 | ✅ 已完成 | Redis Pub/Sub 多节点扩展 |
| 0.9.x | ✅ 已完成 | 测试、优化、修复 |
| 1.0.x | ✅ 已完成 | UI 优化、头像上传、Lombok 移除 |
| 1.1.x | ✅ 已完成 | 社交功能、富文本编辑器、自动账号分配 |
| **1.2.1** | ✅ **已完成** | **前端工程化：Vite + Vue 3 + TypeScript + Pinia + 组件化** |

---

## 更新日志

详见 [CHANGELOG.md](CHANGELOG.md)

---

## 许可证

MIT License