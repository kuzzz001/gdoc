# Gdoc 开发 TODO

> 参考腾讯文档、Google Docs 等成熟协同文档产品的功能，规划 Gdoc 的后续优化方向。

---

## 优先级说明

- **P0** — 核心体验缺陷，影响正常使用
- **P1** — 重要体验优化，显著提升产品质量
- **P2** — 锦上添花，丰富产品能力
- **P3** — 长期规划，面向企业级场景

---

## 一、前端工程化（P0）✅ v1.2.x 已完成

### 1.1 迁移至 SPA 框架 ✅ v1.2.1 已完成
- [x] 引入 Vue 3 + Vite 构建工具链，替代 CDN 引入方式
- [x] 使用 vue-router 实现前端路由（登录页 / 文档列表 / 编辑器 / 社交页）
- [x] 使用 Pinia 替代全局 Vue 对象进行状态管理
- [x] 使用 TypeScript 重写前端逻辑

### 1.2 组件化改造 ✅ v1.2.1 已完成
- [x] 提取通用组件：头像 Avatar、弹窗 Modal、分页 Pagination
- [x] 提取通用组件：编辑器：格式工具栏 Toolbar、图片预览 ImagePreview
- [x] 提取通用组件：社交：好友卡片 FriendCard、聊天列表 ChatList、邀请卡片 InvitationCard
- [x] 建立统一的 UI 组件库规范（CSS 变量系统）

### 1.3 后续优化 ✅ v1.2.2 已完成
- [x] 补充消息气泡 MessageBubble 组件（支持文本/图片/文件/系统消息，消息状态指示）
- [x] 补充代码块 CodeBlock 组件（语法高亮、复制、行号、折叠展开）
- [x] 建立组件文档 `/components` 页面（4 大分类，实时预览 + 代码示例）
- [x] 暗黑模式支持（亮色/暗色/跟随系统三态切换，CSS 变量系统，ThemeToggle 组件）

---

## 二、编辑器引擎升级（P0）✅ v1.5.0 已完成

### 2.1 替换 execCommand ✅
- [x] 迁移至 ProseMirror / TipTap 作为编辑器内核 (TipTap 2.x + Vue 3)
- [x] 创建 TipTapEditor 组件替代 contenteditable
- [x] Schema 定义（doc → paragraph / heading / blockquote / list / codeBlock / image / table / horizontalRule）
- [x] Node 类型：标题（h1-h3）、段落、引用、代码块（含语法高亮）、有序/无序列表
- [x] Mark 类型：粗体、斜体、下划线、删除线、行内代码、链接、高亮
- [x] 自定义 Node：图片、分割线、表格（含 Table/TableRow/TableCell/TableHeader）
- [x] 选区与光标管理（TipTap 内置）
- [x] 快捷键系统（Ctrl+B 等，TipTap 内置）
- [x] 输入规则（Markdown 快捷输入：`# `→ 标题、`> `→ 引用、`- `→ 列表）— Typography 扩展

### 2.2 协同 OT 引擎升级 ✅ v1.5.0 已完成
- [x] 当前：纯文本 OT（INSERT / DELETE / RETAIN 三种操作）
- [x] 升级为富文本 OT：支持结构化操作（Delta 类 + attributes）
- [x] 定义富文本操作类型（DeltaOp: INSERT/DELETE/RETAIN + attributes）
- [x] 实现富文本 transform 算法（Delta.transform + priority 参数）
- [x] 实现 Composition（Delta.compose 合并连续操作）
- [x] 实现 Invert（OTEngine.invert 反转操作）
- [x] 实现 Selection 同步（SelectionState + OTServer 光标同步升级）
- [x] OTServer 文档房间模型（DocumentRoom + 版本历史 + 在线用户管理）

### 2.3 撤销/重做
- [x] TipTap 内置 Undo/Redo（History Extension）
- [ ] 支持协同场景下的 Undo（拒绝已 transform 的操作）

---

## 三、文档功能增强（P1）✅ v1.5.0 已完成

### 3.1 文档导入导出 ✅ v1.4.0 已完成
- [x] **导出 PDF**：使用 Flying Saucer / iText 将 HTML 内容渲染为 PDF
- [x] **导出 Word**：使用 POI-TL / Apache POI 生成 .docx
- [x] **导出 Markdown**：HTML → Markdown 转换
- [x] **导入 Markdown**：解析 .md 为文档内容
- [ ] **导入 Word**：解析 .docx 为 HTML/Content JSON

### 3.2 文档模板 ✅ v1.5.0 已完成
- [x] 内置模板库（会议纪要、项目计划、周报、产品需求文档等）
- [x] 用户自定义模板（将文档另存为模板）
- [x] 模板变量替换（`{{date}}`、`{{name}}` 等自动填充）
- [x] 前端 TemplatePicker 组件（分类筛选、模板预览、选择创建）

### 3.3 文档组织管理 ✅ v1.4.0 已完成
- [x] **文件夹系统**：支持多级文件夹组织文档
- [x] **文件夹树 UI**：DocumentList 侧边文件夹树 + FolderTree 组件
- [x] **标签系统**：支持文档打标签（TagService + TagController + 标签筛选 UI）
- [x] **搜索优化**：MySQL 全文索引实现全文搜索
- [x] **回收站**：删除文档进入回收站，30天内可恢复（RecycleBinService + UI）

### 3.4 富文本高级功能 ✅ v1.5.0 部分完成
- [x] **表格编辑器**：TipTap Table 扩展（插入/删除行列、表头支持）
- [x] **代码块**：TipTap CodeBlockLowlight + lowlight 语法高亮（15+ 语言）
- [x] **数学公式**：支持 LaTeX 公式编辑（TipTap Mathematics + KaTeX）
- [ ] **图片增强**：拖拽上传、粘贴上传、图片裁剪/缩放/对齐、Alt 文本
- [ ] **目录生成**：根据标题结构自动生成文档目录（Table of Contents）
- [ ] **页眉页脚**：文档页眉页脚、页码设置
- [ ] **分页预览**：打印分页预览模式

### 3.5 版本对比 ✅ v1.5.0 已完成
- [x] 版本 Diff 可视化（VersionDiff 组件，左侧旧版本 / 右侧联排对比）
- [x] 版本命名与备注（VersionService + versionName 字段）
- [x] 版本列表、创建、重命名 API（VersionController）

---

## 四、协同功能深化（P1）✅ v1.5.0 已完成

### 4.1 评论系统 ✅ v1.4.0/v1.5.0 已完成
- [x] **行内评论**：选中文本/段落添加评论
- [x] **评论回复**：支持评论下多轮回复对话
- [x] **评论面板 UI**：Editor 侧边评论面板 + CommentPanel 组件
- [x] **已解决/重新打开**：评论状态管理（resolved 状态切换）
- [x] **@提及**：在评论中 @ 协作成员（系统通知）

### 4.2 批注模式
- [ ] **建议修改**（Suggesting mode）：类似 Google Docs 的"建议"模式
- [ ] **接受/拒绝建议**：逐条或批量接受/拒绝
- [ ] **建议通知**：有新建议时通知文档协作者

### 4.3 实时协作增强 ✅ v1.5.0 部分完成
- [x] **协作者头像游标**：显示每个协作者的姓名标签
- [x] **用户在线状态**：编辑器中显示当前在线协作者列表
- [ ] **编辑历史追溯**：查看指定版本的每位协作者的编辑内容
- [ ] **文档锁**：手动锁定文档，禁止他人编辑

### 4.4 通知系统 ✅ v1.4.0/v1.5.0 已完成
- [x] **系统通知**：被添加为协作者、收到评论、文档被修改等场景推送通知
- [x] **通知中心 UI**：NotificationCenter 组件 + 未读红点
- [ ] **多渠道通知**：站内通知 + 可扩展邮件通知

---

## 五、社交功能增强（P1）✅ v1.5.0 已完成

### 5.1 群组协作 ✅ v1.5.0 已完成
- [x] **团队/组织**：支持创建团队，团队内文档共享
  - 数据库：`gdoc_team` + `gdoc_team_member` 表
  - 后端：TeamService + TeamController（创建/列表/成员管理/删除）
- [x] **团队文档库**：团队统一管理文档
- [x] **团队聊天**：群聊功能
  - 数据库：`gdoc_group_chat` + `gdoc_group_member` + `gdoc_group_message` 表
  - 后端：GroupChatService + GroupChatController
  - 前端：GroupChatPanel 组件

### 5.2 消息增强
- [ ] **消息搜索**：历史消息全文检索
- [ ] **消息引用**：引用聊天中的某条消息回复
- [ ] **消息撤回**：2 分钟内可撤回
- [ ] **已读成员**：群聊场景下查看消息已读/未读列表
- [ ] **文件管理**：独立的聊天文件浏览界面

---

## 六、用户体验优化（P2）✅ v1.5.0 部分完成

### 6.1 主题与布局
- [x] **暗黑模式**：支持深色主题，跟随系统或手动切换（v1.2.2）
- [ ] **响应式布局**：适配平板和手机屏幕
- [x] **阅读模式**：ReadingMode 组件（字体大小调节、内容宽度切换、沉浸式阅读）

### 6.2 编辑器体验 ✅ v1.5.0 部分完成
- [x] **字数统计**：WordCount 组件（字数/字符数/段落数实时统计）
- [ ] **打字机模式**：打字时自动跟随
- [ ] **专注模式**：高亮当前行/段落
- [ ] **全屏编辑**：隐藏工具栏，全屏书写
- [ ] **快捷键提示**：按 Ctrl+/ 显示快捷键列表

### 6.3 移动端适配 ✅ v1.5.0 部分完成
- [ ] **移动端编辑器**：触屏友好的工具栏布局
- [x] **PWA 支持**：manifest.json 配置，可安装到桌面
- [ ] **触摸手势**：手势选中、滑动删除、长按菜单

---

## 七、后端架构升级（P2）✅ v1.5.0 已完成

### 7.1 可观测性 ✅ v1.5.0 已完成
- [x] **操作日志（审计）**：LoggingAspect 记录 Controller 入参/出参/耗时
- [x] **API 访问日志**：请求耗时统计（LoggingAspect @Around）
- [x] **健康检查**：Spring Boot Actuator 端点（HealthCheck + spring-boot-starter-actuator）
- [ ] **APM 集成**：接入 SkyWalking / Prometheus + Grafana

### 7.2 性能优化
- [x] **统一分页响应**：PageResult<T> 统一包装（records/total/page/size/pages）
- [ ] **OT 操作合并**：高频操作合并发送，减少 WebSocket 消息数量
- [ ] **内容缓存**：热点文档内容缓存在 Redis，减少 DB 查询
- [ ] **大文档优化**：分片加载、懒加载（>1MB 的文档分块传输）

### 7.3 安全加固 ✅ v1.4.0/v1.5.0 已完成
- [x] **接口限流**：RateLimitFilter 实现 API 限流
- [x] **XSS 过滤**：XssFilter 富文本内容 XSS 过滤
- [x] **CSRF 防护**：Spring Security CSRF 配置
- [x] **敏感操作二次确认**：删除文档、移除协作者等操作需再次确认

---

## 八、测试与质量保障（P2）✅ v1.5.0 部分完成

### 8.1 测试覆盖 ✅ v1.5.0 部分完成
- [x] **测试文档**：TESTING.md 完整测试策略
- [x] **单元测试**：DocumentServiceTest + DeltaTest（Service 层核心逻辑）
- [x] **OT 引擎测试**：OTTransform、OTEngine、Delta 单元测试
- [ ] **WebSocket 集成测试**：多用户协同场景模拟
- [ ] **API 接口测试**：MockMvc / RestAssured 覆盖所有 REST 接口

### 8.2 CI/CD ✅ v1.5.0 已完成
- [x] **CI 流水线**：GitHub Actions 配置（后端 Maven + 前端 npm + Docker 构建）
- [ ] **CD 流水线**：自动部署到 Render / 服务器

---

## 九、企业级功能（P3）✅ v1.5.0 部分完成

### 9.1 权限系统升级 ✅ v1.5.0 部分完成
- [x] **RBAC 模型**：基于角色的访问控制（admin/editor/viewer 三级权限）
- [ ] **文档级权限**：继承+覆盖（团队权限 → 文档权限）
- [ ] **IP 白名单**：限制文档访问来源 IP
- [x] **水印**：Watermark 组件 + WatermarkFilter（在线浏览水印）

### 9.2 高级功能 ✅ v1.5.0 部分完成
- [ ] **文档历史统计**：查看文档访问量、编辑热力图
- [ ] **数据导出**：支持批量导出文档（ZIP 打包）
- [ ] **第三方集成**：Webhook 通知、API Key 开放平台
- [x] **SSO 登录**：SsoFilter（OAuth2 Header-based SSO）
- [ ] **内容合规**：敏感词过滤、内容审核

### 9.3 AI 智能化（参考腾讯文档 AI）
- [ ] **AI 续写**：基于大模型接续写作
- [ ] **AI 润色**：优化语句表达、修正语法
- [ ] **AI 摘要**：自动生成文档摘要
- [ ] **智能排版**：一键美化文档排版
- [ ] **内容翻译**：文档全文/选段翻译

---

## 十、当前待修复/优化项 ✅ v1.5.0 已修复

### 10.1 已知问题
- [x] OT 引擎仅支持纯文本 → 已升级为富文本 OT（Delta + attributes）
- [x] `document.execCommand` 已废弃 → 已替换为 TipTap 编辑器
- [x] 社交页面 CSS 颜色值分散 → 添加 CSS 变量 `--text-on-primary`，统一颜色管理

### 10.2 代码质量
- [x] 前端 CSS 颜色值统一为 CSS 变量（`--text-on-primary` 等）
- [x] 后端添加统一的日志切面 LoggingAspect（Controller 入参/出参/耗时日志）
- [x] 统一分页响应模型 PageResult<T>（records/total/page/size/pages）
- [x] 后端 Lombok 已移除，确保所有模块无 Lombok 依赖

---

## 版本规划建议（v1.5.0 更新）

| 版本 | 聚焦 | 完成内容 |
|------|------|----------|
| ~~v1.2.x~~ | ~~前端工程化~~ | ~~Vue 3 + Vite + TypeScript、组件化、暗黑模式、组件文档~~ ✅ |
| ~~v1.3.0~~ | ~~核心升级~~ | ~~TipTap 编辑器迁移、文件夹/评论/通知系统 API~~ ✅ |
| ~~v1.4.0~~ | ~~文档管理~~ | ~~文件夹树 UI、评论面板 UI、通知中心 UI、搜索、回收站、导入导出、安全加固~~ ✅ |
| v1.5.0 | 全面升级 | 富文本 OT 引擎、文档模板、数学公式、版本对比、团队/群聊、阅读模式、字数统计、RBAC、水印、SSO、CI/CD、PWA |
| v1.6.x | 体验打磨 | 图片增强、目录生成、批注模式、响应式布局、移动端适配 |
| v2.0.x | AI 智能化 | AI 续写、润色、翻译 |

> 注：v1.5.0 完成了 TODO 中绝大部分功能，剩余项主要为体验打磨（图片增强/目录/响应式）和 AI 智能化（长期规划）。
