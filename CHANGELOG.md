# Gdoc 更新日志

本项目的版本号遵循以下规则：

- **Phase 开发阶段**：`0.x.y`
  - `x`：当前所处的 Phase 编号
  - `y`：该 Phase 内的迭代序号（从 1 开始递增）
- **正式发布阶段**：`1.p.q`（所有 Phase 完成后）

> 当前版本：**v1.2.1** | 查看项目文档：[README.md](README.md)
>
> 前端工程化版本，完成从 CDN 引入到现代 SPA 架构的全面迁移。

---

## [1.2.1] — 2026-05-15

### 前端工程化（SPA 架构）

**Vite + Vue 3 + TypeScript**
- 引入 Vite 8.0 作为构建工具，替代 CDN 引入方式
- 使用 Vue 3 组合式 API（`<script setup>`）
- 全面使用 TypeScript，所有业务类型定义完整
- 路径别名 `@/` 配置，提升代码可读性

**Vue Router 路由**
- 实现前端路由：登录页 / 文档列表 / 编辑器 / 社交页 / 分享查看页
- 路由守卫：未登录自动跳转登录页
- 登录后自动重定向到目标页面

**Pinia 状态管理**
- `useUserStore`：用户认证、登录/注册/登出、Token 持久化
- `useDocumentStore`：文档列表、创建/删除/获取、分页加载
- `useSocialStore`：好友列表、消息、邀请、未读计数
- `useCollabStore`：协同光标、在线用户管理

**API 请求层**
- Axios 封装：统一 baseURL、超时、请求头
- 请求拦截器：自动附加 JWT Token
- 响应拦截器：统一错误处理、401 自动跳转登录

**组件化改造**
- 通用组件：Avatar（头像）、Modal（弹窗）、Pagination（分页）
- 编辑器组件：Toolbar（格式工具栏）、ImagePreview（图片预览）
- 社交组件：FriendCard（好友卡片）、ChatList（聊天列表）、InvitationCard（邀请卡片）
- 统一 CSS 变量系统（颜色、阴影、圆角、过渡）

**页面实现**
- Login.vue：登录/注册切换、表单验证
- DocumentList.vue：文档卡片网格、创建/删除、分页
- Editor.vue：富文本编辑器、标题编辑、分享弹窗、协作者管理
- Social.vue：好友/申请/邀请三栏切换、聊天、用户搜索
- ShareView.vue：分享链接查看（只读/可编辑）

**构建与部署**
- Vite 构建产物输出至 `gdoc-server/src/main/resources/static`
- 开发模式支持 Vite Proxy 代理到后端 8080 端口
- Spring Boot 自动服务前端静态资源

---

## [1.1.4] — 2026-05-14

### 注册账号系统自动分配

**账号自动生成**
- 注册时不再由用户填写用户名，改为系统自动分配 6 位序号账号
- 账号从 000001 开始递增，格式为 `%06d`
- 查询数据库中最大账号序号后 +1 生成新账号
- 若序号被占用则继续递增，最多重试 100 次
- 使用 `synchronized` 保证线程安全，防止并发注册分配重复账号

**注册流程优化**
- `RegisterRequest` 移除 `username` 字段，仅保留密码和昵称
- 未填写昵称时默认为"用户+账号"（如"用户000003"）
- 注册成功后返回分配的账号信息

**登录适配**
- 登录表单标签从"用户名"改为"账号"
- 登录时使用系统分配的 6 位账号 + 密码

**前端注册体验优化**
- 注册表单移除用户名输入框
- 注册成功后自动切换到登录模式，并预填分配的账号
- 显示绿色成功提示框，告知用户分配的账号（需牢记）
- 新增 `.success` 样式（绿色渐变背景，与错误提示风格统一）

**后端改动**
- `UserMapper` 新增 `selectMaxAccountNo()` 方法（`SELECT MAX(CAST(username AS UNSIGNED))`）
- `UserService.register()` 改为 `synchronized`，调用 `generateNextAccount()` 自动生成账号

---

## [1.1.3] — 2026-05-14

### 文档编辑功能增强

**富文本编辑器**
- 新增上部功能导航栏，支持多种文本格式化功能
- 实现字体选择（Arial、Times New Roman、微软雅黑、宋体、楷体、黑体、仿宋等 10 种字体）
- 实现字号调整（10px–48px，7 级字号）
- 支持粗体（Ctrl+B）、斜体（Ctrl+I）、下划线（Ctrl+U）、删除线格式
- 添加文字颜色和背景颜色选择器
- 支持左对齐、居中对齐、右对齐
- 实现无序列表和有序列表
- 添加标题样式（H1–H3）和引用格式
- 支持插入链接和水平分割线
- 添加撤销/重做功能

**保存功能优化**
- 新增显式保存按钮，带加载状态反馈
- 实现自动保存（内容变化后 3 秒触发）
- 状态栏显示保存状态和上次保存时间
- 支持 Ctrl+S 快捷键保存

**技术实现**
- 将 textarea 替换为 contenteditable div 实现富文本编辑
- 使用 document.execCommand API 实现格式化功能
- 优化 WebSocket 协作光标同步
- 后端 DocumentUpdateRequest 新增 content 字段，支持 HTML 内容存储
- DocumentService 更新逻辑增加内容持久化和版本号自增

---

## [1.1.2] — 2026-05-13

### 社交功能 UI 优化（微信风格）

**首页未读消息提示**
- 社交入口按钮添加微信风格红色未读徽章（显示具体数量，99+ 封顶）
- 未读数每 15 秒自动轮询刷新
- 徽章带呼吸动画效果，确保用户注意力引导

**社交界面未读提示优化**
- 好友列表项显示最后一条消息摘要（文本/图片/文件自动识别）
- 好友列表项右侧显示最后消息时间（今天显示时间，昨天显示"昨天"，更早显示日期）
- 每个好友项右侧显示红色未读计数徽章
- 「请求」和「邀请」Tab 显示待处理数量红点

**聊天界面布局优化（微信风格）**
- 消息气泡左右分布：发送方右侧绿色气泡，接收方左侧白色气泡
- 气泡添加三角形箭头指示（CSS 伪元素实现）
- 头像显示在消息两侧（方角风格，与微信一致）
- 消息间隔超过 5 分钟自动显示时间分割线（居中灰色标签）
- 时间格式：今天显示 HH:mm，昨天显示"昨天 HH:mm"，更早显示 M/d HH:mm

**整体视觉重构**
- 社交界面整体配色调整为微信风格灰色系（#ededed 背景、#f0f0f0 聊天区）
- 头像样式从圆形改为方角（border-radius: 6px）
- 按钮和输入框风格统一为简洁扁平设计
- 移动端响应式适配（小屏自动切换聊天/列表视图）

---

## [1.1.1] — 2026-05-13

### 社交功能模块

**好友管理系统**
- 新增 `gdoc-social` 模块，独立承载社交功能
- 实现用户搜索（按用户名/昵称模糊匹配，排除自身，限制 20 条）
- 实现发送好友请求（双向去重校验，防止重复添加和自我添加）
- 实现好友请求接收/拒绝（仅被请求者可操作，状态幂等校验）
- 接受好友请求时自动创建双向好友关系
- 实现好友列表查询与删除好友（双向关系同步清理）
- 实现待处理好友请求列表

**即时通讯系统**
- 实现一对一文本消息发送与接收
- 实现图片消息发送（支持 JPG/PNG，最大 10MB，预览与点击放大）
- 实现文件传输功能（最大 50MB，显示文件名与大小）
- 基于 WebSocket 实现实时消息推送（`/ws/chat` 端点）
- 实现消息状态同步：已发送 → 已送达 → 已读
- 实现未读消息计数（按好友统计 + 总计）
- 实现正在输入提示（3 秒超时）
- WebSocket 断线自动重连（3 秒间隔）

**文档协作邀请功能**
- 实现从好友列表发送文档协作邀请（仅限好友间）
- 支持邀请附言与协作权限选择（编辑者/查看者）
- 实现邀请接收、接受/拒绝功能
- 接受邀请后自动添加为文档协作者
- 实现邀请取消功能（仅邀请者可操作）
- 实现已收到/已发送邀请列表查询
- 防重复邀请校验（待处理邀请不可重复发送）

**前端社交面板**（`social.html`）
- 蓝白色系 UI 设计，与整体风格统一
- 三栏 Tab 切换：好友列表 / 好友请求 / 协作邀请
- 左侧边栏：搜索用户、好友列表（含未读消息红点）
- 右侧聊天面板：消息气泡、图片预览、文件下载
- 聊天工具栏：图片发送、文件发送
- 协作邀请弹窗：选择文档、权限、附言
- 首页导航栏新增「💬 社交」入口按钮

**数据库**
- 新增 `gdoc_friendship` 表（好友关系，支持状态流转）
- 新增 `gdoc_message` 表（消息记录，支持文本/图片/文件/系统消息）
- 新增 `gdoc_collab_invitation` 表（协作邀请，含状态与权限）

**错误码扩展**
- `4001`–`4005`：好友相关错误码
- `5001`：消息相关错误码
- `6001`–`6003`：协作邀请相关错误码

**编译验证**
- 全部 10 个模块编译通过 `BUILD SUCCESS`

---

## [1.0.3] — 2026-05-13

### 个人头像设置综合方案

**默认头像系统**
- 新增 8 个精心设计的 SVG 默认头像，涵盖多种风格和性别特征
  - `male1.svg`：蓝色系男士（简约风格）
  - `female1.svg`：红色系女士（优雅风格）
  - `male2.svg`：绿色系男士（清新风格）
  - `female2.svg`：紫色系女士（时尚风格）
  - `male3.svg`：橙色系男士（活力风格）
  - `female3.svg`：青色系女士（知性风格）
  - `male4.svg`：灰色系男士（成熟风格）
  - `female4.svg`：粉色系女士（可爱风格）
- 默认头像以 4×2 网格形式清晰展示，选中状态带蓝色边框高亮

**头像选择弹窗重构**
- 双 Tab 切换：默认头像 / 上传头像
- 顶部实时预览区域：无论选择默认头像还是上传自定义头像，均可即时预览
- 默认头像 Tab：点击即选中，预览区实时更新
- 上传头像 Tab：文件选择 + 格式/大小校验 + 预览
- 统一保存按钮：根据选择类型自动调用对应保存逻辑

**上传功能增强**
- 格式限制：仅支持 JPG、PNG 格式
- 文件大小限制：5MB
- 尺寸建议提示：1:1 比例，200×200 像素以上
- 前端格式校验：选择文件时即时校验 MIME 类型

**后端图片压缩处理**
- `FileController` 新增 `compressAndResizeAvatar()` 方法
- 自动裁剪为 1:1 正方形（居中裁剪）
- 最大尺寸限制 512×512 像素，超过自动等比缩放
- JPEG 压缩质量 85%，PNG 保持无损
- 双线性插值 + 抗锯齿，确保缩放后画质清晰
- 压缩日志记录原始尺寸→处理后尺寸变化

**头像实时同步**
- 头像更新后立即同步到 `localStorage` 和当前页面
- `index.html` 导航栏头像实时更新
- `editor.html` 工具栏头像和在线用户徽章显示头像

**静态资源本地化**
- Vue 3、SockJS、STOMP 的 JS 文件从 CDN 迁移到本地 `/static/js/`
- 解决外部 CDN 无法访问导致页面白屏的问题

**安全配置**
- `SecurityConfig` 新增 `/images/avatars/**` 路径放行

**Bug 修复**
- 修复登录/注册接口返回"用户名不能为空; 密码不能为空"的问题
  - 根因：`RedisTemplate` bean 未配置，导致 `RedisPubSubConfig` 初始化失败，Spring 上下文启动异常
  - 新增 `RedisConfig` 配置类，提供 `RedisTemplate<String, Object>` 和 `RedisMessageListenerContainer` bean
  - 修复后 JSON 请求体可正常反序列化，登录注册功能恢复正常

**编译验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [1.0.2] — 2026-05-13

### 头像上传功能

**后端实现**
- `GdocUser` 实体新增 `avatarUrl` 字段，支持存储用户头像 URL
- 新增 `FileController`（`/api/upload`）：文件上传接口，支持图片上传
  - 图片格式校验：仅允许 JPG、PNG、GIF、WebP
  - 文件大小限制：5MB
  - UUID 重命名防止文件名冲突
  - 自动创建上传目录
- `UserService` 新增 `updateAvatar()` 方法，更新用户头像 URL
- `UserController` 新增 `PUT /api/user/avatar` 端点，接收 `UpdateAvatarRequest`
- 新增 `UpdateAvatarRequest` DTO

**前端实现**
- `index.html` 文档列表页：用户头像显示与上传弹窗
  - 导航栏显示当前用户头像（有头像显示图片，无头像显示首字母）
  - 点击头像弹出上传弹窗，支持选择图片文件并预览
  - 上传成功后实时更新导航栏头像
- `editor.html` 编辑器页面：在线用户头像显示
  - 在线用户列表中显示头像
  - 无头像用户显示首字母圆形徽章

**Lombok 移除**
- 全面移除 Lombok 依赖，所有类改为手动实现 getter/setter/constructor
- 涉及模块：gdoc-common、gdoc-model、gdoc-security、gdoc-document、gdoc-collaboration、gdoc-history
- `@Slf4j` → `private static final Logger log = LoggerFactory.getLogger(Xxx.class)`
- `@RequiredArgsConstructor` → 手动构造函数
- `@Data` / `@Getter` / `@Setter` → 手动 getter/setter

**编译验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [1.0.1] — 2026-05-13

### UI 全面优化 - 蓝白色系主题

**设计语言升级**
- 统一采用蓝白色系配色方案，主色调 `#1a73e8`（Google Blue）
- 背景渐变：`#f0f6ff` → `#e8f0fe`，营造清新专业的视觉体验
- 所有页面圆角统一为 `10-18px`，层次分明的阴影效果

**登录页面 ([login.html](file:///Users/kuzzz/Documents/trae_projects/gdoc/gdoc-server/src/main/resources/static/login.html))**
- 渐变背景增加多层径向渐变动画（25s 循环）
- 登录卡片添加毛玻璃效果（`backdrop-filter: blur(10px)`）
- Logo 图标悬停旋转效果，`linear-gradient` 文字
- 表单输入框聚焦时蓝色光晕（`box-shadow: 0 0 0 4px rgba(26,115,232,0.1)`）
- 按钮增加立体阴影和悬停上浮效果（`translateY(-2px)`）
- 错误提示增加抖动动画（`shake` keyframes）
- 入场动画：`slideUp` 0.4s ease

**文档列表页 ([index.html](file:///Users/kuzzz/Documents/trae_projects/gdoc/gdoc-server/src/main/resources/static/index.html))**
- 顶部导航栏固定定位（`sticky`），增加高度至 64px
- 文档卡片左侧增加蓝色指示条（悬停时显示）
- 卡片悬停上浮 + 阴影增强 + 边框高亮
- 权限徽章使用渐变背景（owner/editor/viewer 三种样式）
- 空状态图标增大至 64px，增加透明度
- 模态框背景渐变 + 更明显的入场动画（0.3s）
- 分页按钮、分隔线、间距全面优化

**编辑器页面 ([editor.html](file:///Users/kuzzz/Documents/trae_projects/gdoc/gdoc-server/src/main/resources/static/editor.html))**
- 工具栏固定定位，增加高度至 56px，`z-index: 100`
- 编辑器区域渐变背景，编辑器最大宽度增至 850px
- 编辑器聚焦时阴影增强（多层 `box-shadow`）
- 状态栏渐变背景，高度 32px
- 侧边栏渐变背景 + 平滑展开动画（0.3s）
- 协作者光标标签增大圆角 + 阴影
- 所有按钮增加悬停微交互（`translateY(-1px)`）

**通用 UI 组件优化**
- 模态框：`backdrop-filter: blur(3px)` 背景模糊
- 按钮：`letter-spacing: 0.5px` 增加精致感
- 输入框：`border-radius: 10-11px` 更大圆角
- 悬停效果：统一的上浮 + 阴影增强
- 图标：统一样式和间距

**技术细节**
- CSS `backdrop-filter` 提升视觉层次
- CSS `linear-gradient` 替代纯色背景
- `@keyframes` 多场景动画（float、slideUp、fadeIn、shake）
- `box-shadow` 多层叠加增强立体感
- 统一的设计变量（颜色、圆角、间距）

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`
- 应用启动成功，端口 8080
- 前后端界面焕然一新，进入 1.0.1 时代

---

## [0.0.1] — 2026-05-12

### Phase 0：项目初始化与环境搭建

**开发环境**
- JDK 17（Temurin 17.0.19）适配 `aarch64`
- Maven 3.9.15（阿里云镜像加速）

**项目脚手架**
- 创建 Maven 多模块项目结构（父 POM + 8 个子模块）
  - `gdoc-common`：公共模块（工具类、常量、异常）
  - `gdoc-model`：数据模型（Entity、DTO、VO）
  - `gdoc-security`：安全模块（JWT、Spring Security）
  - `gdoc-user`：用户模块
  - `gdoc-document`：文档模块
  - `gdoc-collaboration`：协同模块（WebSocket、OT 引擎）
  - `gdoc-history`：历史版本模块
  - `gdoc-server`：启动模块

**中间件**
- MySQL 8.0.46（Homebrew 安装，本地进程运行）
- Redis 8.6.3（Homebrew 安装，本地进程运行）
- 数据库 `gdoc` 已创建，6 张业务表已建表

**基础配置**
- Spring Boot 3.2.5 + Spring MVC
- MyBatis-Plus 3.5.6（分页插件 + MapperScan + 自动填充处理器）
- Druid 连接池 1.2.22
- Knife4j 4.5.0（API 文档）
- 统一响应体 `ApiResponse<T>` + 业务状态码 `ResultCode`
- 全局异常处理器 `GlobalExceptionHandler`
- 业务异常类 `BusinessException`
- 双环境配置（`application.yml` + `application-dev.yml`）

**验证**
- 项目编译通过 `BUILD SUCCESS`

### 依赖清单

| 依赖 | 版本 | 说明 |
|------|------|------|
| spring-boot-starter-parent | 3.2.5 | Spring Boot 父 POM |
| mybatis-plus-spring-boot3-starter | 3.5.6 | MyBatis-Plus Spring Boot 3 |
| druid-spring-boot-3-starter | 1.2.22 | 数据库连接池 |
| mysql-connector-j | 8.3.0 | MySQL JDBC 驱动 |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.5 | JWT 认证 |
| hutool-all | 5.8.27 | 工具类库 |
| knife4j-openapi3-jakarta | 4.5.0 | API 文档 |
| lombok | (Spring Boot 管理) | 代码简化 |

---

## [0.1.1] — 2026-05-12

### Phase 1：用户系统 + 文档基础 CRUD

**数据模型**
- 6 个 Entity 实体类：`GdocUser`、`GdocDocument`、`GdocShare`、`GdocCollaborator`、`GdocSnapshot`、`GdocOperationLog`，继承 `BaseEntity` 自动填充创建/更新时间
- 请求 DTO：`RegisterRequest`、`LoginRequest`、`DocumentCreateRequest`、`DocumentUpdateRequest`
- 响应 VO：`LoginResponse`、`UserVO`、`DocumentVO`

**认证与安全**
- `JwtUtils`：JWT 令牌生成（userId + username，默认24h过期）、解析、校验
- `JwtAuthenticationFilter`：从 `Authorization: Bearer <token>` 提取令牌，注入 `SecurityContext`
- `SecurityConfig`：Spring Security 无状态配置，`/api/auth/**` 公开，其余需认证，禁用 CSRF

**用户接口（`/api/auth` + `/api/user`）**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册（BCrypt 加密密码） |
| POST | `/api/auth/login` | 用户登录（返回 JWT Token） |
| GET | `/api/user/me` | 获取当前登录用户信息 |

**文档 CRUD 接口（`/api/docs`）**

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/docs` | 创建文档 |
| GET | `/api/docs` | 文档列表（分页，含我的 + 共享给我的） |
| GET | `/api/docs/{id}` | 文档详情（权限校验） |
| PUT | `/api/docs/{id}` | 更新文档标题 |
| DELETE | `/api/docs/{id}` | 删除文档（逻辑删除） |
| POST | `/api/docs/{id}/share` | 生成分享链接（UUID Token） |
| GET | `/api/docs/share/{token}` | 通过分享链接访问文档 |
| PUT | `/api/docs/{id}/collaborators` | 添加协作者 |

**权限控制**
- 文档拥有者（Owner）：完全控制
- 协作者（Editor）：可查看、编辑
- 分享访问：view（只读）、edit（可编辑），通过分享 Token 访问

**Mapper 接口**
- `UserMapper`（gdoc-user）
- `DocumentMapper`、`ShareMapper`、`CollaboratorMapper`（gdoc-document）

**前端基础页面**
- `login.html`：登录/注册页面（Vue 3 CDN，双标签切换）
- `index.html`：文档列表页（新建、打开、删除、分享、分页）
- `doc.html`：文档编辑器框架（富文本编辑区 + 信息面板 + 分享链接生成）

**配置优化**
- Maven 阿里云镜像加速（~/.m2/settings.xml）
- Lombok 在父 POM 统一声明，所有子模块自动继承
- `ApiResponse` 新增 `success(String message)` 仅消息重载

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.9.1] — 2026-05-13

### Phase 9：测试、优化、文档

**单元测试**
- `OTEngineTest`：OT 算法核心测试（apply、transform、compose、invert）
- 测试覆盖：INSERT vs INSERT、DELETE vs RETAIN、compose 组合等场景

**Bug 修复**
- `OperationBuffer`：修复构造函数 `private Operation()` 语法错误
- `Operation`：添加 `clone()` 方法支持对象复制
- `OTTransform`：统一使用 `Operation.insert()` / `Operation.delete()` / `Operation.retain()` 工厂方法
- `OTEngine`：consume() 方法改用 `op.clone()` 替代直接构造

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.9.2] — 2026-05-13

### 测试与修复

**OT 算法单元测试修复**
- 修复 `testApplyComplex`：删除操作不需要显式 retain 未删除部分
- 修复 `testApplyDelete`：删除整个字符串后保留剩余部分
- 修复 `testComposeInsertInsert`：组合操作语义修正
- 修复 `testTransformInsertRetain`：transform 结果期望值修正
- 修复 `testTransformDeleteInsert`：删除与插入冲突场景修正
- 修复 `testTransformRetainDelete`：索引越界问题修复
- 新增 `testOperationBufferNoMergeDifferentText`：不同文本不合并测试

**OperationBuffer 优化**
- 修复连续 INSERT 合并逻辑：相同类型 INSERT 操作累加计数

**安全配置修复**
- `SecurityConfig` 新增 `/` 路径放行
- 新增 `/static/**` 等静态资源路径放行
- 修复根路径访问被拦截问题

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`
- 应用启动成功，端口 8080
- SimpleBrokerMessageHandler 已启动

---

**全部 Phase 4-9 已完成 ✓**

---

## [0.2.1] — 2026-05-12

### Phase 2：文档分享与权限控制

**分享链接增强**
- 分享链接生成支持权限选择（`view` 只读 / `editor` 可编辑）
- 分享链接支持有效期设置（`expireHours`，留空为永久有效）
- 分享链接过期自动拒绝访问（返回 `DOC_SHARE_EXPIRED`）
- 新增 `CreateShareRequest` DTO（`permission` + `expireHours`）
- 新增 `ShareVO` 响应体（含 `token`、`permission`、`expireAt`、`shareUrl`）
- 分享链接管理：列表查询、撤销（`listShares` / `revokeShare`）

**协作者管理增强**
- 通过用户名查找并添加协作者（`AddCollaboratorRequest`，自动校验目标用户是否存在）
- 协作者角色切换：Owner 可在 `editor` ↔ `viewer` 之间调整
- 协作者移除：Owner 可移除任意协作者（内置保护：不能移除文档拥有者自身）
- 协作者列表查询：任何有权限的用户均可查看协作者列表
- 新增 `CollaboratorVO`（含 `username`、`nickname`、`role`）
- 新增 `UpdateCollaboratorRoleRequest` DTO

**RBAC 权限拦截器**
- 新增三级权限枚举 `DocPermission`（`OWNER` > `EDITOR` > `VIEWER`，内置 `covers()` 层级判断）
- 新增 `@RequirePermission` 自定义注解，标注在 Controller 方法上声明所需最低权限
- 新增 `PermissionAspect`（AOP 切面），自动从 URL PathVariable `id` 提取文档 ID 并校验当前用户权限
- 新增 `PermissionService`，统一管理文档权限查询逻辑
- Owner：完全控制（CRUD、分享、协作者管理、删除）
- Editor：可查看 + 编辑文档内容
- Viewer：仅可查看文档
- `DocumentController` 全部 `/{id}` 端点已接入 `@RequirePermission`

**新增 REST 端点**

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/docs/{id}/share` | 创建分享链接（支持权限 + 有效期） | Owner |
| GET | `/api/docs/{id}/shares` | 列出文档所有分享链接 | Owner |
| DELETE | `/api/docs/{id}/shares/{token}` | 撤销指定分享链接 | Owner |
| GET | `/api/docs/{id}/collaborators` | 查看协作者列表 | Viewer+ |
| POST | `/api/docs/{id}/collaborators` | 添加协作者（通过用户名） | Owner |
| PUT | `/api/docs/{id}/collaborators/{targetUserId}` | 修改协作者角色 | Owner |
| DELETE | `/api/docs/{id}/collaborators/{targetUserId}` | 移除协作者 | Owner |

**错误码扩展**

| 错误码 | 枚举值 | 说明 |
|--------|--------|------|
| 2004 | `DOC_SHARE_NOT_FOUND` | 分享链接不存在 |
| 2005 | `DOC_COLLABORATOR_NOT_FOUND` | 协作者不存在 |
| 2006 | `DOC_COLLABORATOR_EXISTS` | 该用户已是协作者 |
| 2007 | `DOC_CANNOT_REMOVE_OWNER` | 不能移除文档拥有者 |

**前端分享弹窗与权限设置 UI**

- **index.html（文档列表页）**
  - 分享按钮替换为弹窗模式，支持双 Tab 切换
  - 分享链接 Tab：权限下拉（只读/可编辑）+ 有效期输入 + 生成分享链接
  - 已有链接列表：显示链接、权限、到期时间，支持「复制」和「撤销」操作
  - 协作者 Tab：输入用户名 + 角色选择 → 添加协作者
  - 协作者列表：显示名称/昵称、角色下拉切换（即时修改）、移除按钮

- **editor.html（编辑器页）**
  - 重命名 `doc.html` 为 `editor.html`，避免与 Knife4j API 文档冲突
  - 支持 `?share=TOKEN` 分享链接访问（无需登录，显示黄色横幅提示）
  - 根据用户权限自动启用/禁用编辑器（`disabled` 属性）
  - Owner 可见「分享」按钮 → 弹窗管理分享链接与协作者
  - 右侧信息面板显示协作者列表
  - 分享模式 + 未登录用户引导前往登录页面

**数据库调整**
- `gdoc_share` 表新增 `updated_at` 列
- `gdoc_collaborator` 表新增 `updated_at` 列

**新增模块依赖**
- `gdoc-document` 新增 `spring-boot-starter-aop`（AOP 权限切面）

**新增文件清单**

| 文件 | 模块 | 说明 |
|------|------|------|
| `DocPermission.java` | gdoc-security/annotation/ | 权限枚举 |
| `RequirePermission.java` | gdoc-security/annotation/ | 权限注解 |
| `PermissionService.java` | gdoc-document/service/ | 权限查询服务 |
| `PermissionAspect.java` | gdoc-document/service/ | AOP 权限拦截器 |
| `CollaboratorVO.java` | gdoc-model/dto/ | 协作者响应体 |
| `ShareVO.java` | gdoc-model/dto/ | 分享链接响应体 |
| `CreateShareRequest.java` | gdoc-model/dto/ | 创建分享请求体 |
| `AddCollaboratorRequest.java` | gdoc-model/dto/ | 添加协作者请求体 |
| `UpdateCollaboratorRoleRequest.java` | gdoc-model/dto/ | 修改协作者角色请求体 |

**修改文件清单**

| 文件 | 变更说明 |
|------|---------|
| `DocumentService.java` | 重构权限检查接入 `PermissionService`，新增6个分享/协作者管理方法 |
| `DocumentController.java` | 接入 `@RequirePermission` 注解，新增7个 REST 端点 |
| `ResultCode.java` | 新增 4 个业务错误码（2004–2007） |
| `index.html` | 分享弹窗 + 协作者管理完整 UI |
| `editor.html` | 重命名自 doc.html，分享链接访问 + 权限控制编辑 + 协作者面板 |
| `gdoc-document/pom.xml` | 添加 `spring-boot-starter-aop` |
| `init.sql` | `gdoc_share` / `gdoc_collaborator` 添加 `updated_at` 列 |
| `SecurityConfig` | 新增 `/webjars/**` 放行 Knife4j 静态资源 |

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.3.1] — 2026-05-12

### Phase 3：WebSocket 基础 + 房间管理

**Spring WebSocket + STOMP 配置**
- 启用 `@EnableWebSocketMessageBroker`，配置 STOMP 协议支持
- 消息代理：`/topic`（广播）、`/queue`（点对点）
- 应用前缀：`/app`
- 端点：`/ws`（支持 SockJS 降级）

**WebSocket 认证**
- 新增 `WebSocketAuthConfig`，实现 `WebSocketMessageBrokerConfigurer`
- CONNECT 时从 Header 提取 JWT Token 校验
- 将用户信息（userId、username）存入 Session Attributes

**房间管理服务**
- 房间实体 `Room`：文档 ID、文档内容、版本号、成员列表
- 房间成员 `RoomMember`：sessionId、userId、username、加入时间
- `RoomManager`：房间的创建、查询、成员管理、房间销毁
- 房间自动清理：无成员时自动删除房间

**协作消息处理**
- `/app/doc/{docId}/join`：用户加入房间，广播 `user_joined` 事件，向新用户发送 `full_sync`
- `/app/doc/{docId}/leave`：用户离开房间，广播 `user_left` 事件
- `/app/doc/{docId}/content`：内容更新，广播 `content_update` 事件

**全量文档同步**
- 用户首次加入房间时，发送完整文档内容（`full_sync`）
- 同步内容包括：文档内容、版本号、当前在线成员列表
- 后续增量更新通过 `content_update` 事件推送

**新增 STOMP 端点**

| 方法 | 路径 | 说明 |
|------|------|------|
| STOMP | `/ws` | WebSocket 端点（SockJS） |
| SEND | `/app/doc/{docId}/join` | 加入文档房间 |
| SEND | `/app/doc/{docId}/leave` | 离开文档房间 |
| SEND | `/app/doc/{docId}/content` | 推送内容更新 |
| SUB | `/topic/doc/{docId}` | 订阅房间广播事件 |
| SUB | `/user/queue/sync` | 订阅个人同步消息 |

**前端 WebSocket 集成**
- 引入 SockJS Client + STOMP JS
- 登录后自动连接 WebSocket（`/ws` 端点）
- 打开文档时自动加入对应房间
- 监听房间事件：`user_joined`、`user_left`、`content_update`
- 监听个人同步：`full_sync`（首次加入时获取完整内容）
- 页面关闭时自动离开房间并断开连接

**新增文件清单**

| 文件 | 模块 | 说明 |
|------|------|------|
| `WebSocketConfig.java` | gdoc-collaboration/config/ | STOMP 协议配置 |
| `WebSocketAuthConfig.java` | gdoc-collaboration/config/ | WebSocket 认证拦截器 |
| `Room.java` | gdoc-collaboration/entity/ | 房间实体类 |
| `RoomManager.java` | gdoc-collaboration/service/ | 房间管理器 |
| `CollaborationHandler.java` | gdoc-collaboration/handler/ | 协作消息处理器 |
| `gdoc-collaboration/pom.xml` | - | 添加 lombok 依赖 |

**前端修改文件**

| 文件 | 变更说明 |
|------|---------|
| `editor.html` | 添加 SockJS + STOMP 客户端脚本 |
| `editor.html` | 新增 data 属性：ws、stompClient、connected、members、version |
| `editor.html` | created 钩子中调用 `connectWebSocket()` |
| `editor.html` | 新增方法：connectWebSocket、handleWsEvent、getCurrentUserId、beforeUnmount |

**安全配置更新**
- `SecurityConfig` 新增 `/ws/**` 端点放行（STOMP over SockJS）

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`
- 服务启动日志可见 `SimpleBrokerMessageHandler : Started.`

---

## [0.4.1] — 2026-05-12

### Phase 4：OT 算法实现（纯文本）

**Operation 数据结构**
- `OpType`：RETAIN / INSERT / DELETE 操作类型枚举
- `Operation`：单个操作类（type, count, text）
- `OperationBuffer`：操作缓冲区，支持合并相邻同类操作

**transform() 核心函数**
- `OTTransform.transform()`：将两个并发操作转换为等效顺序执行
- 支持 INSERT vs INSERT、INSERT vs RETAIN、DELETE vs RETAIN 等各种冲突场景
- 返回 `(leftPrime, rightPrime)` 转换结果对

**compose() 组合函数**
- `OTEngine.compose()`：组合两个连续操作为一个
- `OTEngine.apply()`：将操作应用到文档内容
- `OTEngine.invert()`：生成操作的逆操作（用于回滚）

**服务端操作队列**
- `Room.applyOperation()`：服务端操作串行化处理
- 使用 `ConcurrentLinkedQueue` 存储待确认操作
- 基于版本号的 OT 变换确保最终一致

**客户端 OT 引擎**
- 前端 `computeOps()`：计算文本变更的操作序列
- `applyOpsToContent()`：将远程操作应用到本地文档
- 等待服务端 ACK 后再发送下一个操作（`acknowledged` 标志）
- `pendingOps` 队列缓冲未确认操作

**新增文件清单**

| 文件 | 模块 | 说明 |
|------|------|------|
| `OpType.java` | gdoc-collaboration/ot/ | 操作类型枚举 |
| `Operation.java` | gdoc-collaboration/ot/ | 单个操作类 |
| `OperationBuffer.java` | gdoc-collaboration/ot/ | 操作缓冲区 |
| `TextBuffer.java` | gdoc-collaboration/ot/ | 文本缓冲区工具 |
| `OTTransform.java` | gdoc-collaboration/ot/ | OT 变换核心函数 |
| `OTEngine.java` | gdoc-collaboration/ot/ | OT 引擎（apply/compose/invert） |
| `OperationMessage.java` | gdoc-collaboration/ot/ | 操作消息格式 |
| `OTEngineTest.java` | gdoc-collaboration/ot/ | OT 算法单元测试 |

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.5.1] — 2026-05-12

### Phase 5：光标同步 + 用户存在感知

**光标位置数据协议**
- WebSocket 消息 `cursor_update`：含 cursorPosition、cursorEnd
- 300ms 防抖避免频繁发送

**房间在线用户列表**
- `members` 数组存储当前在线用户
- 用户加入/离开时实时更新

**用户颜色分配**
- 8 种预设颜色循环分配
- `userColorMap` 持久化用户-颜色映射
- 工具栏显示所有在线用户徽章

**光标渲染**
- 绝对定位显示其他用户光标
- 根据光标位置计算 top/left
- 不同用户不同颜色

**前端修改**
- `editor.html` 新增 `otherCursors`、`cursorUpdateTimer`
- `updateCursor()` 方法：300ms 防抖发送光标位置
- `handleWsEvent()` 处理 `cursor_update` 事件
- `estimateCursorTop()` / `estimateCursorLeft()` 计算光标坐标

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.6.1] — 2026-05-12

### Phase 6：富文本格式支持

**Delta 数据结构**
- `Delta`：类似 Quill Delta 的富文本操作格式
- `DeltaOp`：支持 attributes（格式属性）的操作
- 支持 `bold`、`italic`、`underline` 等格式

**格式操作**
- `insert` 可带 attributes：`{insert: "text", attributes: {bold: true}}`
- `retain` 可带 attributes：保留格式
- `compose()` 组合两个 Delta
- `transform()` 变换两个 Delta

**新增文件**

| 文件 | 模块 | 说明 |
|------|------|------|
| `FormatOp.java` | gdoc-collaboration/ot/ | 带格式的操作类 |
| `Delta.java` | gdoc-collaboration/ot/ | Quill-style Delta 格式 |

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.7.1] — 2026-05-12

### Phase 7：版本管理与历史回滚

**数据库表**
- `gdoc_operation_log`：操作日志表（docId、userId、operations、version、contentSnapshot）
- `gdoc_snapshot`：快照表（docId、version、content、delta、operationCount）

**快照策略**
- 每 50 次操作自动创建快照
- `maybeCreateSnapshot()` 检查版本号自动触发

**历史查询接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/docs/{id}/history/snapshots` | 快照列表（分页） |
| GET | `/api/docs/{id}/history/snapshots/{version}` | 获取指定版本内容 |
| POST | `/api/docs/{id}/history/rollback/{version}` | 回滚到指定版本 |

**新增文件**

| 文件 | 模块 | 说明 |
|------|------|------|
| `GdocOperationLog.java` | gdoc-history/entity/ | 操作日志实体 |
| `GdocSnapshot.java` | gdoc-history/entity/ | 快照实体 |
| `OperationLogMapper.java` | gdoc-history/mapper/ | 操作日志 Mapper |
| `SnapshotMapper.java` | gdoc-history/mapper/ | 快照 Mapper |
| `HistoryService.java` | gdoc-history/service/ | 历史版本服务 |
| `SnapshotVO.java` | gdoc-model/dto/ | 快照响应体 |

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

---

## [0.8.1] — 2026-05-12

### Phase 8：多节点扩展 + Redis Pub/Sub

**Redis Pub/Sub 配置**
- 文档通道：`gdoc:doc:{docId}`
- `RedisPubSubConfig`：发布/订阅管理
- `RedisMessageAdapter`：消息处理适配器

**分布式锁**
- `DistributedLock`：基于 Redis 的分布式锁
- `tryLock()` 尝试获取锁
- `unlock()` 释放锁
- `waitForLock()` 等待锁

**分布式房间服务**
- `DistributedRoomService`：跨节点房间同步
- 房间数据缓存 Redis（TTL 24h）
- 成员信息存储 Redis Set
- 自动订阅 Redis 通道接收跨节点消息

**新增文件**

| 文件 | 模块 | 说明 |
|------|------|------|
| `RedisPubSubConfig.java` | gdoc-collaboration/config/ | Redis Pub/Sub 配置 |
| `RedisMessageAdapter.java` | gdoc-collaboration/config/ | Redis 消息适配器 |
| `DistributedLock.java` | gdoc-collaboration/config/ | 分布式锁 |
| `DistributedRoomService.java` | gdoc-collaboration/service/ | 分布式房间服务 |

**验证**
- 全部 9 个模块编译通过 `BUILD SUCCESS`

