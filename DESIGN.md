# Gdoc 设计文档

> 文档版本：v1.5.0 | 最后更新：2026-05-27

---

## 目录

1. [系统架构概述](#1-系统架构概述)
2. [后端架构设计](#2-后端架构设计)
3. [前端架构设计](#3-前端架构设计)
4. [数据库设计](#4-数据库设计)
5. [模块详细设计](#5-模块详细设计)
6. [接口设计](#6-接口设计)
7. [安全设计](#7-安全设计)
8. [部署设计](#8-部署设计)

---

## 1. 系统架构概述

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                               │
│  ┌────────────────┐  ┌────────────────┐  ┌──────────────┐  │
│  │  Vue 3 SPA     │  │  WebSocket     │  │  SockJS      │  │
│  │  (Vite + TS)   │  │  (STOMP)       │  │  (Fallback)  │  │
│  └───────┬────────┘  └───────┬────────┘  └──────┬───────┘  │
└──────────┼──────────────────┼──────────────────┼────────────┘
           │ HTTP/REST        │ WS              │ WS
           ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────┐
│                     代理层（Vite Dev Server / Nginx）          │
│          /api/* → Backend:8080    /ws/* → Backend:8080      │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                    API 网关层（Spring Boot）                    │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐   │
│  │ 认证过滤器 │ │ CORS配置  │ │ 权限注解   │ │  全局异常处理  │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────────┘   │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                    业务模块层                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │ 用户模块  │ │ 文档模块  │ │ 社交模块  │ │ 协同模块  │      │
│  │ gdoc-user│ │gdoc-docu │ │gdoc-social│ │gdoc-collab│     │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐      │
│  │ 历史模块  │ │ 安全模块  │ │ 公共模块  │ │ 模型模块  │      │
│  │gdoc-histo│ │gdoc-secu │ │gdoc-commo│ │gdoc-mode │      │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘      │
└─────────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────┐
│                     数据持久层                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  MySQL 8.0   │  │  Redis 7     │  │  本地文件系统     │  │
│  │ (业务数据)    │  │ (缓存/PubSub)│  │ (头像/文件上传)   │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 架构原则

| 原则 | 说明 |
|------|------|
| 模块化 | 9 个 Maven 模块，职责清晰，依赖单向 |
| 松耦合 | 模块间通过 API 层和模型层通信，避免循环依赖 |
| 可替换 | 数据库支持 MySQL/PostgreSQL 双适配 |
| 前后端分离 | v1.2.1 后全面采用 SPA 架构 |
| RESTful | API 设计遵循 REST 风格，统一响应格式 |

---

## 2. 后端架构设计

### 2.1 模块依赖关系

```
gdoc-common (工具类、异常、统一响应)
    │
    ├── gdoc-model (Entity、DTO、VO)
    │
    ├── gdoc-security (JWT、权限注解)
    │
    ├── gdoc-user (用户服务，依赖 common + model + security)
    │
    ├── gdoc-document (文档服务，依赖 user + common + model + security)
    │
    ├── gdoc-collaboration (协同服务，依赖 common + model)
    │
    ├── gdoc-history (历史服务，依赖 document + common + model)
    │
    ├── gdoc-social (社交服务，依赖 user + document + common + model)
    │
    └── gdoc-server (启动入口，聚合所有模块)
```

### 2.2 三层架构

每个业务模块遵循标准的三层架构：

```
Controller (REST 控制器)
    ↓ 接收请求、参数校验、调用 Service
Service (业务逻辑层)
    ↓ 核心业务逻辑、事务管理、权限校验
Mapper (数据访问层，MyBatis-Plus)
    ↓ CRUD 操作、分页查询
Database (MySQL / PostgreSQL)
```

### 2.3 统一响应模型

所有 API 响应遵循统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { }
}
```

- 成功：`code=200`，data 承载业务数据
- 失败：`code=业务错误码`，message 包含错误描述
- 业务异常通过 `BusinessException` 抛出，由 `GlobalExceptionHandler` 统一捕获

### 2.4 错误码体系

| 范围 | 模块 | 示例 |
|------|------|------|
| 200 | 通用 | 操作成功 |
| 400–500 | HTTP 标准 | 401 未认证、403 无权限 |
| 1001–1004 | 用户模块 | 1001 用户名已存在 |
| 2001–2007 | 文档模块 | 2002 无权操作文档 |
| 3001–3002 | 协同模块 | 3002 版本冲突 |
| 4001–4005 | 好友模块 | 4003 不能添加自己 |
| 5001 | 消息模块 | 5001 消息不存在 |
| 6001–6003 | 邀请模块 | 6002 邀请已处理 |

---

## 3. 前端架构设计

### 3.1 项目结构

```
gdoc-web/
├── src/
│   ├── api/                  # API 请求层（axios 封装）
│   │   ├── request.ts        #   拦截器、Token 注入
│   │   ├── auth.ts           #   认证接口
│   │   ├── document.ts       #   文档/分享/协作者接口
│   │   └── social.ts         #   社交/消息/邀请接口
│   ├── assets/styles/        # 全局样式
│   │   └── global.scss       #   CSS 变量 + 基础样式
│   ├── components/           # UI 组件库
│   │   ├── common/           #   通用组件
│   │   │   ├── Avatar.vue    #     头像（默认 SVG + 自定义上传）
│   │   │   ├── Modal.vue     #     弹窗
│   │   │   ├── Pagination.vue#     分页
│   │   │   ├── MessageBubble.vue # 消息气泡
│   │   │   └── ThemeToggle.vue    # 主题切换
│   │   ├── editor/           #   编辑器组件
│   │   │   ├── Toolbar.vue       # 格式工具栏
│   │   │   ├── CodeBlock.vue     # 代码块展示
│   │   │   └── ImagePreview.vue  # 图片预览
│   │   └── social/           #   社交组件
│   │       ├── FriendCard.vue      # 好友卡片
│   │       ├── ChatList.vue        # 聊天列表
│   │       └── InvitationCard.vue  # 邀请卡片
│   ├── router/               # 路由配置
│   │   ├── index.ts          #   路由守卫（认证检查）
│   │   └── routes.ts         #   路由定义
│   ├── stores/               # Pinia 状态管理
│   │   ├── user.ts           #   用户认证
│   │   ├── document.ts       #   文档 CRUD
│   │   ├── social.ts         #   社交功能
│   │   ├── collab.ts         #   协同编辑
│   │   └── theme.ts          #   主题切换
│   ├── types/                # TypeScript 类型定义
│   │   └── index.ts          #   所有业务类型
│   ├── views/                # 页面视图
│   │   ├── Login.vue         #   登录/注册
│   │   ├── DocumentList.vue  #   文档列表
│   │   ├── Editor.vue        #   文档编辑器
│   │   ├── Social.vue        #   社交面板
│   │   ├── ShareView.vue     #   分享查看
│   │   └── Components.vue    #   组件文档
│   ├── App.vue               # 根组件
│   ├── main.ts               # 入口文件
│   └── style.css             # 全局样式（CSS 变量 + 主题）
```

### 3.2 路由设计

| 路径 | 页面 | 需要登录 | 说明 |
|------|------|----------|------|
| `/login` | Login | 否 | 登录/注册，已登录自动跳转首页 |
| `/` | DocumentList | 是 | 文档列表首页 |
| `/editor/:id` | Editor | 是 | 文档编辑器 |
| `/social` | Social | 是 | 社交面板 |
| `/share/:token` | ShareView | 否 | 分享链接查看 |
| `/components` | Components | 否 | 组件文档 |

### 3.3 状态管理（Pinia Store）

| Store | 状态 | 方法 | 说明 |
|-------|------|------|------|
| user | user, token, isLoggedIn | login(), register(), logout(), fetchUser() | 用户认证 |
| document | documents, currentDoc, loading | fetchDocuments(), createDocument(), getDocument(), updateDocument(), deleteDocument() | 文档管理 |
| social | friends, messages, friendRequests, invitations, unreadTotal | searchUsers(), sendFriendRequest(), handleFriendRequest(), sendMessage(), fetchMessages(), fetchInvitations() | 社交功能 |
| collab | connected, onlineUsers, cursors | connect(), disconnect(), sendOperation(), sendCursor() | 协同编辑 |
| theme | theme (light/dark/system) | setTheme(), toggleTheme() | 主题切换 |

### 3.4 API 层设计

使用 axios 实例封装，提供统一的请求/响应拦截：

- **请求拦截器**：自动从 localStorage 读取 Token，注入 `Authorization: Bearer <token>` 请求头
- **响应拦截器**：统一处理响应数据解包；401 状态码自动清除 Token 并跳转登录页
- **baseURL**：`/api`，开发环境经 Vite Proxy 代理到后端 8080

### 3.5 数据流

```
页面视图 (View)
    ↕ 响应式数据
Pinia Store (State + Actions)
    ↕ Promise<Data>
API 层 (Axios)
    ↕ HTTP Request/Response
后端 Controller
    ↕ Service → Mapper → Database
```

---

## 4. 数据库设计

### 4.1 ER 图

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   gdoc_user   │       │ gdoc_document     │       │ gdoc_share   │
│──────────────│       │──────────────────│       │──────────────│
│ id (PK)      │──1:N──│ id (PK)           │──1:N──│ id (PK)      │
│ username     │       │ title             │       │ doc_id (FK)  │
│ password     │       │ content (LONGTEXT)│       │ token (UNIQUE)│
│ nickname     │       │ owner_id (FK)     │       │ permission   │
│ avatar_url   │       │ version           │       │ expire_at    │
│ created_at   │       │ deleted           │       │ created_at   │
│ updated_at   │       │ created_at        │       │ updated_at   │
└──────────────┘       │ updated_at        │       └──────────────┘
       │               └──────────────────┘
       │ 1:N                   │ 1:N
       ▼                       ▼
┌──────────────┐       ┌──────────────────┐
│gdoc_friendship│      │gdoc_collaborator  │
│──────────────│       │──────────────────│
│ id (PK)      │       │ id (PK)           │
│ user_id (FK) │       │ doc_id (FK)       │
│ friend_id (FK)       │ user_id (FK)      │
│ status (0-3)  │       │ role (editor/viewer)│
│ created_at   │       │ created_at        │
│ updated_at   │       │ updated_at        │
└──────────────┘       └──────────────────┘
       │
       ▼
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│ gdoc_message  │       │ gdoc_snapshot     │       │gdoc_operation │
│──────────────│       │──────────────────│       │_log          │
│ id (PK)      │       │ id (PK)           │       │──────────────│
│ sender_id    │       │ doc_id (FK)       │       │ id (PK)      │
│ receiver_id  │       │ version           │       │ doc_id (FK)  │
│ content      │       │ content (LONGTEXT)│       │ user_id      │
│ msg_type     │       │ operation_seq     │       │ revision     │
│ file_url     │       │ created_at        │       │ operation    │
│ file_name    │       └──────────────────┘       │ created_at   │
│ file_size    │                                   └──────────────┘
│ status (0-2) │       ┌──────────────────┐
│ created_at   │       │gdoc_collab_       │
│ updated_at   │       │invitation         │
└──────────────┘       │──────────────────│
                       │ id (PK)           │
                       │ doc_id (FK)       │
                       │ inviter_id (FK)   │
                       │ invitee_id (FK)   │
                       │ role              │
                       │ status (0-3)      │
                       │ message           │
                       │ created_at        │
                       │ updated_at        │
                       └──────────────────┘
```

### 4.2 索引策略

| 表 | 索引 | 目的 |
|-----|-------|------|
| gdoc_document | `idx_owner (owner_id)` | 按拥有者查询文档 |
| gdoc_share | `idx_token (token)` | 分享链接查询 |
| gdoc_share | `idx_doc_id (doc_id)` | 按文档查询分享列表 |
| gdoc_collaborator | `uk_doc_user (doc_id, user_id)` | 唯一约束，防重复添加 |
| gdoc_snapshot | `idx_doc_version (doc_id, version)` | 版本查询 |
| gdoc_operation_log | `idx_doc_revision (doc_id, revision)` | 操作日志查询 |
| gdoc_friendship | `uk_user_friend (user_id, friend_id)` | 唯一约束 |
| gdoc_friendship | `idx_status (status)` | 状态查询 |
| gdoc_message | `idx_sender (sender_id)` | 发件人查询 |
| gdoc_message | `idx_receiver (receiver_id)` | 收件人查询 |
| gdoc_message | `idx_created (created_at)` | 时间范围查询 |

### 4.3 字段类型规范

| 类型 | 使用场景 |
|------|----------|
| `BIGINT` | 所有主键、外键 |
| `VARCHAR(64)` | 账号、状态码等短字符串 |
| `VARCHAR(256)` | 标题、文件名等 |
| `VARCHAR(512)` | URL、JSON 等较长字符串 |
| `LONGTEXT` | 文档内容（HTML/JSON） |
| `TEXT` | 一般文本内容 |
| `TINYINT` | 枚举状态（0–3） |
| `DATETIME` | 时间戳 |

---

## 5. 模块详细设计

### 5.1 用户模块 (gdoc-user)

#### 5.1.1 账号生成算法

```
1. SELECT MAX(CAST(username AS UNSIGNED)) FROM gdoc_user
2. 若结果为空，从 1 开始；否则 maxNo + 1
3. 拼接 6 位零填充：String.format("%06d", accountNo)
4. 尝试插入，若主键冲突则重试（最多 100 次）
5. synchronized 方法保证线程安全
```

#### 5.1.2 认证流程

```
登录请求 → UserController.login()
    → UserService.authenticate()
        → UserMapper.selectByUsername()
        → BCryptPasswordEncoder.matches(password, user.password)
        → JwtUtils.generateToken(userId, username)
    ← 返回 { token, user }
```

### 5.2 文档模块 (gdoc-document)

#### 5.2.1 权限模型

```
DocPermission 枚举:
  OWNER     — 拥有者（创建者），拥有全部权限
  EDITOR    — 编辑者，可编辑文档内容
  VIEWER    — 查看者，只读访问

权限判定链：
  1. 文档 owner_id == userId → OWNER
  2. 查询 gdoc_collaborator → role → EDITOR / VIEWER
  3. 查询 gdoc_share → permission → 只读/编辑
```

#### 5.2.2 权限注解

```java
@RequirePermission(DocPermission.EDITOR)
public DocumentVO update(Long docId, DocumentUpdateRequest request, Long userId)
```

`PermissionAspect` 切面自动拦截带 `@RequirePermission` 的方法，在方法执行前完成权限校验。

### 5.3 协同模块 (gdoc-collaboration)

#### 5.3.1 房间模型

```
Room:
  - docId: Long              // 文档 ID
  - content: String          // 当前文档内容
  - version: AtomicInteger   // 当前版本号
  - members: ConcurrentHashMap<String, RoomMember>
      // key: sessionId, value: { userId, username, cursorPosition, joinedAt }

RoomManager:
  - rooms: ConcurrentHashMap<Long, Room>  // 房间缓存
  + getOrCreateRoom(docId)   // 获取或创建房间
  + addMember(docId, sessionId, userId, username)
  + removeMember(docId, sessionId)
  + getRoom(docId)
```

#### 5.3.2 WebSocket 消息流

```
客户端 A                  WebSocket                  客户端 B
  │                         │                          │
  │── join(docId) ────────►│                          │
  │                         │── full_sync(content) ──►  │
  │                         │── user_joined(A) ──────►  │
  │                         │                          │
  │── operation(ops, ver)─►│                          │
  │                         │  OTEngine.apply()        │
  │                         │  version++               │
  │                         ├── operation(ops) ──────►  │
  │◄─── ack(version) ──────┤                          │
  │                         │                          │
  │                         │                          │
  │── cursor(pos) ────────►│                          │
  │                         ├── cursor(A, pos) ──────►  │
```

#### 5.3.3 OT 算法

**操作类型**：

| 操作 | 描述 | 参数 |
|------|------|------|
| RETAIN(n) | 跳过 n 个字符 | count: int |
| INSERT(text) | 插入文本 | text: String |
| DELETE(n) | 删除 n 个字符 | count: int |

**操作表示示例**：
```
文档: "hello world"
操作: retain(6) + insert("beautiful ") + retain(5)
结果: "hello beautiful world"
```

**Transform 规则**：

| 情况 | left \ right | left' | right' |
|------|-------------|-------|--------|
| INSERT / INSERT | 保留 left | retain(left.length) + left | right + retain(left.length) |
| INSERT / RETAIN | 保留 left | retain(left.length) + right | left + right |
| RETAIN / DELETE | 裁剪 min | retain(n - min) | delete(min) + retain(n - min) |
| DELETE / INSERT | 保留 left | left | right |
| DELETE / DELETE | 裁剪 min | — | — |

**Compose（连续操作组合）**：

```
op1: insert("AB")       // 在空文档插入 "AB"
op2: retain(2) + insert("CD")  // 在 "AB" 后插入 "CD"
compose(op1, op2) = insert("ABCD")
```

**Invert（操作反转，用于撤销）**：

```
op: insert("hello")    →  invert: delete(5)
op: delete(5)          →  invert: insert("hello")
op: retain(5)          →  invert: (空操作)
```

### 5.4 社交模块 (gdoc-social)

#### 5.4.1 好友关系状态机

```
                  ┌─────────┐
        ┌────────►│ 待确认   │◄────────┐
        │         │ (0)     │         │
        │         └────┬────┘         │
        │              │              │
  发送请求         接受 │               │ 被请求方拒绝
        │              │              │
        │         ┌────▼────┐         │
        └─────────┤ 已接受   ├─────────┘
        │         │ (1)     │
        └─────────► 已拒绝   │
                  │ (2)     │
                  └─────────┘
                  ┌─────────┐
                  │ 已拉黑   │
                  │ (3)     │
                  └─────────┘
```

#### 5.4.2 消息状态流转

```
已发送 (0) ───► 已送达 (1) ───► 已读 (2)
  ↑              ↑                ↑
消息发送      WebSocket 推送   接收方打开聊天
成功         至接收方          会话或点击消息
```

#### 5.4.3 WebSocket 聊天协议

```
// 连接端点
/ws/chat

// 发送消息（客户端 → 服务端）
{
  "type": "message",
  "receiverId": 2,
  "content": "你好",
  "msgType": "text"
}

// 推送消息（服务端 → 客户端）
{
  "type": "message",
  "message": { "id": 100, "senderId": 1, "content": "你好", ... }
}

// 已送达通知（服务端 → 发送方）
{
  "type": "delivered",
  "messageId": 100
}

// 已读通知（服务端 → 发送方）
{
  "type": "read",
  "messageId": 100
}

// 正在输入
{
  "type": "typing",
  "isTyping": true/false
}
// 输入状态 3 秒超时自动清除
```

### 5.5 版本管理模块 (gdoc-history)

#### 5.5.1 快照策略

- 每 50 次操作变更自动创建快照
- 快照记录当前文档的完整内容（LONGTEXT）
- 快照关联操作序列号（operation_seq），用于精准定位
- 回滚时：将指定快照的 content 写回 document 表，版本号递增

### 5.6 主题模块 (前端)

#### 5.6.1 CSS 变量系统

```css
:root {
  /* 颜色 */
  --primary: #4f46e5;
  --success: #10b981;
  --warning: #f59e0b;
  --danger: #ef4444;

  /* 文本 */
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --text-placeholder: #9ca3af;
  --text-disabled: #d1d5db;

  /* 背景 */
  --bg-primary: #ffffff;
  --bg-secondary: #f9fafb;
  --bg-tertiary: #f3f4f6;

  /* 边框与圆角 */
  --border-color: #e5e7eb;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;

  /* 阴影 */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.07);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);

  /* 过渡 */
  --transition: 0.2s ease;
}

[data-theme='dark'] {
  --primary: #818cf8;
  --success: #34d399;
  --warning: #fbbf24;
  --danger: #f87171;

  --text-primary: #f9fafb;
  --text-secondary: #d1d5db;
  --text-placeholder: #9ca3af;
  --text-disabled: #6b7280;

  --bg-primary: #111827;
  --bg-secondary: #1f2937;
  --bg-tertiary: #374151;

  --border-color: #374151;

  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.5);
}
```

#### 5.6.2 主题切换逻辑

```
用户点击 ThemeToggle
    ↓
toggleTheme(): light → dark → system → light
    ↓
setTheme(theme):
  1. 写入 localStorage (持久化)
  2. 设置 data-theme 属性 (CSS 变量切换)
    - light: data-theme="light"
    - dark:  data-theme="dark"
    - system: 移除 data-theme，CSS prefers-color-scheme 自动适配
```

---

### 5.7 文件夹系统 (gdoc-document, v1.3.0 新增)

#### 5.7.1 文件夹树形结构算法

```
查询：SELECT * FROM gdoc_folder WHERE owner_id = ? ORDER BY sort_order
构建树：
  1. 全部节点按 parentId 分组为 Map<parentId, List<FolderVO>>
  2. 取 parentId=0 的节点作为根节点列表
  3. 递归填充子节点：fillChildren(node, parentMap)
  4. 返回根节点列表（即完整的树形结构）

删除：DELETE FROM gdoc_folder WHERE id = ?
  → 将其子文件夹 parent_id 设为 0（移至根目录）
  → 不级联删除子文件夹（柔性策略）
```

#### 5.7.2 认证集成

```
请求 → JwtAuthenticationFilter 解析 Token → Authentication.setPrincipal(userId)
    → FolderController 方法参数注入 Authentication
    → Long userId = (Long) authentication.getPrincipal()
    → 自动获得当前用户 ID
```

### 5.8 评论系统 (gdoc-document, v1.3.0 新增)

#### 5.8.1 评论数据结构

```
gdoc_comment:
  id           BIGINT PK     评论ID
  doc_id       BIGINT        关联文档ID
  user_id      BIGINT        评论者用户ID
  content      TEXT          评论内容
  range_start  INT           选区起始偏移（可选）
  range_end    INT           选区结束偏移（可选）
  resolved     TINYINT       已解决(1) / 未解决(0)
  created_at   DATETIME      创建时间
  updated_at   DATETIME      更新时间

gdoc_comment_reply:
  id           BIGINT PK     回复ID
  comment_id   BIGINT        关联评论ID
  user_id      BIGINT        回复者用户ID
  content      TEXT          回复内容
  created_at   DATETIME      创建时间
```

#### 5.8.2 评论查询优化

```
SELECT c.*, u.nickname, u.avatar_url
FROM gdoc_comment c
LEFT JOIN gdoc_user u ON c.user_id = u.id
WHERE c.doc_id = ? ORDER BY c.created_at DESC
```

### 5.9 通知系统 (gdoc-social, v1.3.0 新增)

#### 5.9.1 通知类型枚举

| 类型 | 常量值 | 触发场景 |
|------|------|------|
| collab_invite | `collab_invite` | 收到协作邀请 |
| comment | `comment` | 文档新增评论 |
| doc_shared | `doc_shared` | 文档被分享 |
| mention | `mention` | @提及 |

#### 5.9.2 通知发送流程

```
业务触发 → NotificationService.send(userId, type, content, relatedId)
    → new GdocNotification() 设置各字段
    → INSERT INTO gdoc_notification
    → 可扩展：WebSocket 实时推送未读计数
```

#### 5.9.3 未读计数 API

```
GET /api/notifications/unread-count
→ SELECT COUNT(*) FROM gdoc_notification WHERE user_id = ? AND is_read = 0
→ 返回未读数量，前端用于红点 badge 展示
```

### 5.10 TipTap 编辑器 (v1.3.0 新增)

#### 5.10.1 扩展架构

```
Editor
├── StarterKit (基础套件：bold/italic/strike/code/heading/bulletList/orderedList/blockquote/horizontalRule/history)
│   └── codeBlock: false (禁用内置 codeBlock，改用 CodeBlockLowlight)
├── Underline (下划线标记)
├── Link (链接)
├── Highlight (文本高亮)
├── Placeholder (占位符提示)
├── Image (图片处理)
├── Table + TableRow + TableCell + TableHeader (表格支持)
└── CodeBlockLowlight + lowlight (语法高亮代码块)
```

#### 5.10.2 数据流

```
编辑器输入 → TipTap onUpdate 事件
    → emit('change', editor.getHTML())  → 父组件接收 HTML
    → 自动保存（300ms debounce）
    → updateDocument({ content: html })  → 后端存储
```

---

## 6. 接口设计

### 6.1 REST API 概览

| 前缀 | 模块 | 说明 |
|------|------|------|
| `/api/auth` | 认证 | 注册、登录 |
| `/api/user` | 用户 | 信息查询、头像更新 |
| `/api/docs` | 文档 | 文档 CRUD、分享、协作者 |
| `/api/social` | 社交 | 好友、消息、邀请 |
| `/api/files` | 文件 | 上传 |
| `/ws` | WebSocket | STOMP 协同端点 |
| `/ws/chat` | WebSocket | 聊天端点 |

### 6.2 关键接口序列图

#### 文档协作流程

```
用户 A                         用户 B
  │                              │
  │  1. 打开文档 editor/:id      │
  │──────► GET /api/docs/:id ────┤
  │◄───── DocumentVO ───────────┤
  │                              │
  │  2. 建立 WebSocket 连接      │
  │──────► STOMP /ws ───────────┤
  │                              │
  │  3. 加入房间                 │
  │──────► /app/doc/:id/join ───┤
  │                              │
  │  4. 收到同步                 │
  │◄───── /queue/sync ──────────┤
  │   { type: "full_sync",      │
  │     content, version }      │
  │                              │
  │  5. 编辑内容                 │     用户 B 同时加入
  │──────► /app/doc/:id/op ─────┤          ...
  │   { operations, version }   │
  │                              │
  │  6. OT 引擎处理              │
  │                              │
  │◄───── /topic/doc/:id ───────┤
  │   { type: "operation",      │
  │     userId, version, ops }  │
```

---

## 7. 安全设计

### 7.1 认证体系

```
┌─────────────────────────────────────────────────────┐
│                    客户端                              │
│  localStorage: { token, user }                       │
│  Axios Interceptor: Authorization: Bearer <token>   │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│              JwtAuthenticationFilter                  │
│  从请求头提取 Token                                  │
│  JwtUtils.validateToken(token) → userId             │
│  UsernamePasswordAuthenticationToken → SecurityContext│
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│              SecurityConfig                           │
│  /api/auth/** — 无需认证                              │
│  /api/docs/share/** — 无需认证                        │
│  /ws/** — 无需认证 (WebSocket)                        │
│  /api/** — 需要认证                                   │
│  其余 — 静态资源放行                                  │
└─────────────────────────────────────────────────────┘
```

### 7.2 JWT Token

| 参数 | 值 |
|------|-----|
| 算法 | HMAC-SHA256 |
| 密钥 | 应用配置 `jwt.secret` |
| 有效期 | 24 小时 |
| 负载 | `sub`: userId, `username`: username, `iat`, `exp` |

### 7.3 权限控制

- **方法级注解**：`@RequirePermission(DocPermission.EDITOR)` 标注在 Controller 方法上
- **AOP 切面**：`PermissionAspect` 拦截注解，查询协作者表判定权限
- **文档分享**：分享链接含 Token（UUID 32 位无横线），不易猜测

### 7.4 密码安全

- 使用 Spring Security 的 BCryptPasswordEncoder（强度 10）
- 数据库不存储明文密码
- 登录校验：`encoder.matches(plainPassword, hashedPassword)`

### 7.5 WebSocket 认证

- WebSocket 连接时通过 `URL 参数` 或 `STOMP CONNECT 帧的 header` 传递 Token
- `WebSocketAuthConfig` 拦截器解析 Token 并注入用户信息到 session

---

## 8. 部署设计

### 8.1 部署架构

```
┌──────────────────────────────────────────┐
│            Docker Host                     │
│  ┌──────────┐  ┌──────────┐              │
│  │  MySQL   │  │  Redis   │              │
│  │  8.0     │  │  7-alpine│              │
│  └──────────┘  └──────────┘              │
│         ▲            ▲                    │
│         │            │                    │
│  ┌──────┴────────────┴──────────┐        │
│  │         gdoc-server           │        │
│  │    Spring Boot 3.2.5 JAR      │        │
│  │  ┌─────────┐  ┌──────────┐   │        │
│  │  │ 静态资源  │  │ 文件上传  │   │        │
│  │  │ (前端)   │  │ (uploads)│   │        │
│  │  └─────────┘  └──────────┘   │        │
│  └──────────────────────────────┘        │
└──────────────────────────────────────────┘
```

### 8.2 配置管理

| 环境 | 配置文件 | 用途 |
|------|----------|------|
| dev | application-dev.yml.example | 本地开发 |
| prod | application-prod.yml | 生产部署 |
| 默认 | application.yml | 公共配置 |

### 8.3 构建产物流

```
gdoc-web (npm run build)
    ↓ Vite 构建
gdoc-server/src/main/resources/static/
    ├── index.html
    ├── assets/
    │   ├── index-DZlIFkXu.js      # 主入口 JS
    │   ├── index-jhk8aEsX.css     # 主样式
    │   ├── Editor-IK3kPLSR.js     # 编辑器页面
    │   ├── Social-BcH2BkKj.js     # 社交页面
    │   ├── Components-DpvMCEJR.js # 组件文档页面
    │   └── ...                    # 其他 chunk
    └── favicon.svg

mvn package -DskipTests
    ↓
gdoc-server/target/gdoc-server-1.0.0-SNAPSHOT.jar
    ↓
java -jar gdoc-server.jar
    ↓ 自动服务静态资源
访问 http://localhost:8080
```

### 8.4 Docker 部署

```yaml
# docker-compose.yml
services:
  mysql:    # MySQL 8.0，端口 3306
  redis:    # Redis 7，端口 6379
  # gdoc-server (需单独启动或添加至 compose)
```

### 8.5 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活配置环境 | prod |
| `DATABASE_URL` | 数据库 JDBC 连接 | — |
| `DATABASE_USERNAME` | 数据库用户名 | — |
| `DATABASE_PASSWORD` | 数据库密码 | — |
| `REDIS_HOST` | Redis 地址 | localhost |
| `REDIS_PORT` | Redis 端口 | 6379 |
| `JWT_SECRET` | JWT 签名密钥 | — |

---

## 附录

### A. 技术栈版本清单

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17+ (21) | 后端语言 |
| Spring Boot | 3.2.5 | 应用框架 |
| MyBatis-Plus | 3.5.6 | ORM |
| Druid | 1.2.22 | 数据库连接池 |
| JJWT | 0.12.5 | JWT 认证 |
| Hutool | 5.8.27 | 工具类 |
| Knife4j | 4.5.0 | API 文档 |
| Vue | 3.5+ | 前端框架 |
| Vite | 8.0+ | 前端构建 |
| TypeScript | 6.0+ | 前端语言 |
| Pinia | 3.0+ | 前端状态管理 |
| Axios | 1.16+ | HTTP 客户端 |
| STOMP.js | 7.3+ | WebSocket 客户端 |
| MySQL | 8.0+ | 关系数据库 |
| Redis | 7+ | 缓存/PubSub |
| Docker | latest | 容器化 |

### B. 模块 Maven 坐标

| 模块 | artifactId | 依赖 |
|------|-----------|------|
| gdoc-common | gdoc-common | — |
| gdoc-model | gdoc-model | gdoc-common |
| gdoc-security | gdoc-security | gdoc-common |
| gdoc-user | gdoc-user | common + model + security |
| gdoc-document | gdoc-document | user + common + model + security |
| gdoc-collaboration | gdoc-collaboration | common + model |
| gdoc-history | gdoc-history | common + model |
| gdoc-social | gdoc-social | user + document + common + model |
| gdoc-server | gdoc-server | 所有模块 |