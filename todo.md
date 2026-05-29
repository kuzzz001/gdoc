# GDoc 项目改进 TODO

> 基于 v1.5.0 代码库全面审查生成 | 2026-05-28

---

## 🔴 CI/CD 与 Docker 环境不匹配（高优先级）

- [ ] **1. Dockerfile 中 JDK 版本不一致**：`pom.xml` 指定 Java 21，但 `Dockerfile` 构建阶段使用 `maven:3.9-eclipse-temurin-17`，运行阶段使用 `eclipse-temurin:17-jre`，编译运行均存在兼容性风险
- [ ] **2. CI workflow JDK 版本不一致**：`.github/workflows/ci.yml` 中 `setup-java` 使用 `java-version: '17'`，但 `pom.xml` 要求 Java 21
- [ ] **3. CI 引用的 Dockerfile 路径不存在**：`ci.yml` 引用 `docker/Dockerfile.backend` 和 `docker/Dockerfile.frontend`，但 `docker/` 目录下只有 `init/` 初始化 SQL，这两个文件缺失
- [ ] **4. CI 前端脚本缺失**：`ci.yml` 中执行 `npm run lint` 和 `npm run test:unit`，但 `gdoc-web/package.json` 的 `scripts` 中没有定义这两个命令（`test:unit` 通过 `|| true` 静默忽略失败）
- [ ] **5. render.yaml 与 CI/Dockerfile 不一致**：`render.yaml` 指向 `./Dockerfile`，但 CI 构建的是 `docker/Dockerfile.backend`，部署平台与 CI 未对齐

---

## 🔴 安全漏洞（高优先级）

- [ ] **6. JWT Secret 硬编码默认值**：[JwtUtils.java](file:///home/kuzzz/Documents/gdoc/gdoc-security/src/main/java/com/gdoc/security/util/JwtUtils.java#L18) 中 `@Value` 注解的默认值为明文 `GdocSecretKey2026GdocSecretKey2026Gdoc`，若未配置环境变量则使用弱密钥。应移除默认值，改为启动时校验必填
- [ ] **7. SSO Filter 无签名验证**：[SsoFilter.java](file:///home/kuzzz/Documents/gdoc/gdoc-common/src/main/java/com/gdoc/common/security/SsoFilter.java#L22-L33) 仅检查 `X-SSO-User` Header 存在即信任，无 token 校验、无签名验证，攻击者可伪造任意用户身份
- [ ] **8. 登录接口无限流保护**：[SecurityConfig.java](file:///home/kuzzz/Documents/gdoc/gdoc-server/src/main/java/com/gdoc/server/config/SecurityConfig.java#L36) 中 `/api/auth/login` 为 `permitAll()`，无爆破防护，虽有全局 `RateLimitFilter` 但登录应更严格（如 5次/分钟）
- [ ] **9. 前端 Token 存储在 localStorage**：[user.ts](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/stores/user.ts#L23) 和 [request.ts](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/api/request.ts#L14) 将 JWT 存储在 `localStorage`，易受 XSS 攻击窃取，建议改用 HttpOnly Secure Cookie
- [ ] **10. 敏感配置暴露风险**：`.gitignore` 仅忽略 `application-dev.yml`，但 `application-prod.yml` 仍被追踪，且 `docker-compose.yml` 中数据库密码 `gdoc123`、Redis 密码 `redis123` 明文硬编码

---

## 🟡 代码质量（中优先级）

- [ ] **11. 删除未使用的模板代码**：`gdoc-web/src/components/HelloWorld.vue` 是 Vite 脚手架遗留的计数器 Demo，`gdoc-web/src/assets/vue.svg`、`vite.svg` 也未被使用
- [ ] **12. Lombok 使用不统一**：项目根 `pom.xml` 已引入 Lombok，但 [GdocUser.java](file:///home/kuzzz/Documents/gdoc/gdoc-model/src/main/java/com/gdoc/model/entity/GdocUser.java) 和 [ApiResponse.java](file:///home/kuzzz/Documents/gdoc/gdoc-common/src/main/java/com/gdoc/common/result/ApiResponse.java) 仍然手写 getter/setter。建议所有 Entity 统一使用 `@Data`
- [ ] **13. UserService.register() 全局 synchronized 锁**：[UserService.java](file:///home/kuzzz/Documents/gdoc/gdoc-user/src/main/java/com/gdoc/user/service/UserService.java#L33) 整个注册方法加锁，在高并发下成为性能瓶颈。建议改用数据库自增序列、分布式 ID 或优化为仅对账号生成逻辑加锁
- [ ] **14. 文档列表查询 N+1 风险**：[DocumentService.java](file:///home/kuzzz/Documents/gdoc/gdoc-document/src/main/java/com/gdoc/document/service/DocumentService.java#L72) 的 `list()` 方法中 `exists` 子查询对每条记录都会额外查询 `gdoc_collaborator` 表，大数据量下性能堪忧
- [ ] **15. Editor.vue 过于臃肿**（250+ 行）：分享弹窗、协作者管理、WebSocket 逻辑均耦合在同一个组件中，建议拆分为 `ShareModal.vue`、`CollaboratorPanel.vue` 等独立组件
- [ ] **16. 前端错误处理粗暴**：[Editor.vue](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/views/Editor.vue) 多处使用原生 `alert()` 弹窗和 `console.error()`，缺乏统一的 Toast/Notification 通知系统
- [ ] **17. 未校验分页参数上下限**：所有 Controller 接收 `page`/`size` 参数无上限约束，可传入 `size=999999` 导致一次加载全表数据造成 OOM
- [ ] **18. WatermarkFilter 实现过于简陋**：[WatermarkFilter.java](file:///home/kuzzz/Documents/gdoc/gdoc-common/src/main/java/com/gdoc/common/security/WatermarkFilter.java) 仅在 Response Header 加 `X-Watermark`，并未在页面内容/图片/PDF 上真正呈现水印，前端 [Watermark.vue](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/components/common/Watermark.vue) 组件也未在编辑器页面中挂载使用

---

## 🟡 测试覆盖（中优先级）

- [ ] **19. 后端测试严重不足**：整个项目仅 3 个测试类（`OTEngineTest`、`DeltaTest`、`DocumentServiceTest`），`gdoc-user`、`gdoc-social`、`gdoc-history` 模块零测试覆盖
- [ ] **20. DocumentServiceTest 是无效测试**：[DocumentServiceTest.java](file:///home/kuzzz/Documents/gdoc/gdoc-document/src/test/java/com/gdoc/document/service/DocumentServiceTest.java) 声明 `@InjectMocks DocumentService` 但测试中直接调用 `documentMapper`，从未真正调用 `documentService` 的任何方法，测试的是 MyBatis-Plus 而非业务逻辑
- [ ] **21. 前端零测试**：`gdoc-web` 无任何 Vitest/Jest 单元测试或组件测试，`package.json` 中无 `test` 或 `test:unit` 脚本
- [ ] **22. 无集成测试**：缺少 Spring Boot 集成测试（`@SpringBootTest`），API 契约、数据库映射、Redis/WebSocket 连通性均无自动化验证

---

## 🟢 前端工程化（低优先级）

- [ ] **23. 缺少 ESLint 配置**：`gdoc-web` 目录下无 `.eslintrc.*` 或 `eslint.config.*`，CI 中 `npm run lint` 必然失败
- [ ] **24. 前端中文字符串硬编码**：Login、Editor、DocumentList 等页面所有提示文本均为硬编码中文，未抽离 i18n 国际化配置，不利于多语言扩展
- [ ] **25. TypeScript 类型与后端 DTO 脱节**：[types/index.ts](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/types/index.ts) 手动定义前端类型，与后端 Java DTO 无自动同步机制，字段变更时容易遗漏
- [ ] **26. 构建产物直接写入后端静态资源目录**：[vite.config.ts](file:///home/kuzzz/Documents/gdoc/gdoc-web/vite.config.ts#L28-L31) 中 `outDir` 指向 `../gdoc-server/src/main/resources/static`，前后端耦合紧密。建议使用 Nginx 反向代理或独立部署
- [ ] **27. 缺少 Prettier 配置**：无 `.prettierrc`，代码风格（缩进、引号、分号）无统一自动格式化工具

---

## 🟢 架构与运维（低优先级）

- [ ] **28. 双数据库支持混乱**：`docker-compose.yml` 仅启动 MySQL，但 `gdoc-server/pom.xml` 同时引入 `mysql-connector-j` 和 `postgresql` 驱动，`docker/init/postgres/init.sql` 也存在但 `compose` 中无 PostgreSQL 服务，`render.yaml` 又配置了 PostgreSQL 环境变量
- [ ] **29. docker-compose.yml 缺少应用服务**：compose 中仅有 `mysql` 和 `redis` 两个基础服务，未定义 `gdoc-server` 应用服务，无法一键启动完整开发环境
- [ ] **30. 静态资源中包含上传文件**：[gdoc-server/uploads/](file:///home/kuzzz/Documents/gdoc/gdoc-server/uploads/abea9d993cf84bbab275ae9bccc41062.png) 目录中遗留了一张测试上传图片，应加入 `.gitignore` 并清理
- [ ] **31. 版本号未同步**：根 `pom.xml` 中版本为 `1.0.0-SNAPSHOT`，但 Git 提交信息提及 `v1.5.0`，CHANGELOG.md 中版本标记需统一
- [ ] **32. 日志策略缺失**：配置文件中无日志级别、滚动策略（`logback-spring.xml`）配置，生产环境日志可能无限增长

---

## 🔵 未完成 / 占位功能

- [ ] **33. Editor.vue 中 WebSocket 方法为空**：[Editor.vue](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/views/Editor.vue#L215-L223) 的 `connectWebSocket()` 和 `disconnectWebSocket()` 是空函数体，注释标注 "Will be implemented with STOMP/SockJS"，协作编辑的核心功能未接入
- [ ] **34. RBAC 权限完全硬编码**：[RbacService.java](file:///home/kuzzz/Documents/gdoc/gdoc-common/src/main/java/com/gdoc/common/security/RbacService.java#L12-L34) 角色和权限定义在静态 `HashMap` 中，无法动态增删改角色或调整权限，也无法持久化到数据库
- [ ] **35. SSO Filter 默认关闭**：[SsoFilter.java](file:///home/kuzzz/Documents/gdoc/gdoc-common/src/main/java/com/gdoc/common/security/SsoFilter.java#L18) 的 `enabled` 默认值为 `false`，无任何配置化开关（如 `application.yml` 中无 `gdoc.sso.*` 配置项）
- [ ] **36. 版本对比功能仅存前端骨架**：[VersionDiff.vue](file:///home/kuzzz/Documents/gdoc/gdoc-web/src/components/editor/VersionDiff.vue) 存在但 `VersionService` 中未找到 diff 计算逻辑，版本间差异对比后端未实现

---

## 📊 统计

| 优先级 | 数量 |
|--------|------|
| 🔴 高   | 10   |
| 🟡 中   | 12   |
| 🟢 低   | 10   |
| 🔵 占位 | 4    |
| **合计** | **36** |

---

> 建议修复顺序：先处理 🔴 安全与 CI/CD（前 10 项），再补齐 🟡 测试与代码质量，最后优化 🟢 工程化细节和 🔵 未完成功能。