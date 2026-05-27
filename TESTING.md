# Gdoc 测试文档

> 文档版本：v1.5.0 | 最后更新：2026-05-27

---

## 目录

1. [测试策略](#1-测试策略)
2. [测试层次](#2-测试层次)
3. [后端单元测试](#3-后端单元测试)
4. [OT 引擎测试](#4-ot-引擎测试)
5. [前端组件测试](#5-前端组件测试)
6. [集成测试](#6-集成测试)
7. [端到端测试](#7-端到端测试)
8. [测试用例清单](#8-测试用例清单)

---

## 1. 测试策略

### 1.1 测试金字塔

```
          ╱╲
         ╱  ╲          E2E 测试（手动）
        ╱    ╲         ───────────────
       ╱      ╲        关键业务流程
      ╱────────╲
     ╱          ╲      集成测试（Spring Boot Test）
    ╱            ╲     ───────────────────
   ╱              ╲    API 接口、WebSocket、数据库
  ╱────────────────╲
 ╱                  ╲   单元测试（JUnit 5）
╱                    ╲  ──────────────
╱                      ╲ OT 引擎、Service 逻辑、工具类
```

### 1.2 测试覆盖目标

| 层次 | 覆盖目标 | 工具 |
|------|----------|------|
| 单元测试 | OT 引擎 100%、核心 Service >80%、工具类 >90% | JUnit 5, Mockito (planned) |
| 集成测试 | 所有 REST API、WebSocket 端点 | Spring Boot Test, MockMvc |
| 前端测试 | 核心组件渲染与交互 | Vitest, Vue Test Utils (planned) |
| E2E 测试 | 用户注册→登录→创建文档→编辑→分享→社交 | 手动测试 |

### 1.3 当前状态

| 模块 | 单元测试 | 集成测试 | E2E |
|------|---------|---------|-----|
| OTEngine | ✅ 17 tests | — | — |
| OperationBuffer | ✅ (included) | — | — |
| UserService | — | — | ✅ 手动 |
| DocumentService | — | — | ✅ 手动 |
| 前端组件 | — | — | ✅ 手动 |
| WebSocket | — | — | ✅ 手动 |

---

## 2. 测试层次

### 2.1 单元测试层

**范围**：独立的类和方法，不依赖外部服务（数据库、Redis、WebSocket）

**被测对象**：
- OT 引擎核心算法（OTEngine、OTTransform、OperationBuffer）
- 工具类（JwtUtils）
- 异常处理（GlobalExceptionHandler）

**隔离策略**：
- OT 引擎：纯逻辑，无外部依赖，直接实例化测试
- Service 层：计划使用 Mockito 模拟 Mapper 层

### 2.2 集成测试层

**范围**：跨模块交互，涉及数据库、Redis、WebSocket

**被测对象**：
- REST API 端点（Controller）
- Service + Mapper 数据库读写
- WebSocket 消息收发

**策略**：
- 使用 `@SpringBootTest` 加载完整上下文
- 使用 H2 内存数据库或 Testcontainers
- 使用 MockMvc 测试 REST API
- 使用 StompClient 测试 WebSocket

### 2.3 端到端测试层

**范围**：完整业务流程，真实浏览器环境

**策略**：
- 手动执行测试用例
- 覆盖关键用户旅程

---

## 3. 后端单元测试

### 3.1 运行测试

```bash
# 运行所有测试
mvn test

# 运行指定模块测试
mvn test -pl gdoc-collaboration

# 运行指定测试类
mvn test -pl gdoc-collaboration -Dtest=OTEngineTest

# 跳过测试（构建用）
mvn package -DskipTests
```

### 3.2 测试指南

**命名规范**：
- 测试类：`{被测类名}Test`
- 测试方法：`test{方法名}{场景}`（如 `testApplyInsert`）
- 断言：使用 JUnit 5 `assertEquals`、`assertTrue`、`assertFalse`、`assertThrows`

**Given-When-Then 风格**：

```java
@Test
void testApplyInsertAndDelete() {
    // Given
    OperationBuffer ops = new OperationBuffer();
    ops.add(Operation.insert("XX"));
    ops.add(Operation.delete(5));

    // When
    String result = OTEngine.apply("helloworld", ops);

    // Then
    assertEquals("XXworld", result);
}
```

**使用 `opsToString` 辅助方法提升可读性**：

```java
private String opsToString(OperationBuffer buffer) {
    StringBuilder sb = new StringBuilder();
    for (Operation op : buffer.getOperations()) {
        if (sb.length() > 0) sb.append("+");
        switch (op.getType()) {
            case INSERT -> sb.append(op.getText());
            case DELETE -> sb.append("delete(").append(op.getCount()).append(")");
            case RETAIN -> sb.append("retain(").append(op.getCount()).append(")");
        }
    }
    return sb.toString();
}
```

---

## 4. OT 引擎测试

### 4.1 OTEngine.apply 测试

验证操作序列在文档上的正确应用。

| 测试用例 | 输入文档 | 操作 | 预期结果 |
|----------|---------|------|---------|
| 纯插入 | `"world"` | `insert("hello")` | `"helloworld"` |
| 纯删除 | `"helloworld"` | `delete(5)` | `"world"` |
| 纯跳过 | `"hello"` | `retain(5)` | `"hello"` |
| 插入+删除 | `"helloworld"` | `insert("XX") + delete(5)` | `"XXworld"` |

### 4.2 OTTransform.transform 测试

验证两个并发操作的 transform 结果。

| 测试用例 | 左操作 | 右操作 | 预期 left' | 预期 right' |
|----------|-------|--------|-----------|------------|
| INSERT / INSERT | `insert("A")` | `insert("B")` | `"A"` | `"B"` |
| INSERT / RETAIN | `insert("ABC")` | `retain(3)` | `"ABC"` | `retain(3)` |
| RETAIN / DELETE | `retain(5)` | `delete(3) + retain(2)` | `retain(5)` | `delete(3) + retain(2)` |
| DELETE / INSERT | `delete(3)` | `insert("X")` | `delete(3)` | `"X"` |

### 4.3 OTEngine.compose 测试

验证连续操作的合并正确性。

| 测试用例 | ops1 | ops2 | 预期 compose 结果 |
|----------|------|------|-------------------|
| 空操作 | 空 | 空 | 空 |
| 插入+插入 | `insert("AB")` | `retain(2) + insert("CD")` | `"ABCD"` |
| 跳过+插入 | `retain(3)` | `retain(3) + insert("X")` | `"X"` |

### 4.4 OTEngine.invert 测试

验证操作反转的正确性（用于撤销）。

| 测试用例 | 操作 | 文档 | 预期 invert 结果 |
|----------|------|------|-----------------|
| 插入反转 | `insert("hello")` | `""` | `delete(5)` |
| 删除反转 | `delete(5)` | `"hello"` | `"hello"` |
| 跳过反转 | `retain(5)` | `"hello"` | 空操作 |

### 4.5 OperationBuffer 测试

| 测试用例 | 操作 | 预期 |
|----------|------|------|
| 克隆 | 构造 → clone | length 相等，对象不同 |
| 合并 | insert("hello") + insert("hello") | 合并为 1 个操作 |
| 不合并 | insert("hello") + insert("world") | 保持 2 个操作 |

### 4.6 现有测试统计

```
OTEngineTest
├── testApplyInsert            ✓
├── testApplyDelete            ✓
├── testApplyRetain            ✓
├── testApplyInsertAndDelete   ✓
├── testTransformInsertInsert  ✓
├── testTransformInsertRetain  ✓
├── testTransformRetainDelete  ✓
├── testTransformDeleteInsert  ✓
├── testComposeEmpty           ✓
├── testComposeInsertInsert    ✓
├── testComposeRetainInsert    ✓
├── testInvertInsert           ✓
├── testInvertDelete           ✓
├── testInvertRetain           ✓
├── testOperationBufferClone   ✓
├── testOperationBufferMerge   ✓
├── testOperationBufferNoMergeDifferentText ✓
└── Total: 17 tests
```

### 4.7 计划新增的 OT 测试用例

| 优先级 | 测试用例 | 说明 |
|--------|----------|------|
| P1 | 复杂组合操作 | INSERT + RETAIN + DELETE 混合序列 |
| P1 | Transform 嵌套 | 多层 transform 连续应用 |
| P1 | Compose + Apply 一致性 | compose(op1, op2).apply(doc) == op2.apply(op1.apply(doc)) |
| P2 | 并发冲突模拟 | 多用户同时编辑同一位置 |
| P2 | 大文档性能 | 10 万+字符文档的 apply 性能 |
| P2 | 边界值 | 空文档、单字符文档、全量替换 |

---

## 5. 前端组件测试

### 5.1 测试环境

当前阶段：**手动测试**
计划工具：**Vitest + @vue/test-utils**

### 5.2 组件测试清单

#### 通用组件

| 组件 | 测试项 | 状态 |
|------|--------|------|
| Avatar | 文字头像渲染 | ✅ 手动 |
| Avatar | 图片头像渲染 | ✅ 手动 |
| Avatar | 不同尺寸（sm/md/lg） | ✅ 手动 |
| Modal | 显示/隐藏切换 | ✅ 手动 |
| Modal | 标题渲染 | ✅ 手动 |
| Modal | 插槽内容渲染 | ✅ 手动 |
| Pagination | 页码渲染 | ✅ 手动 |
| Pagination | 点击切换页码 | ✅ 手动 |
| Pagination | 首尾页禁用状态 | ✅ 手动 |
| MessageBubble | 文本消息渲染 | ✅ 手动 |
| MessageBubble | 图片消息渲染 | ✅ 手动 |
| MessageBubble | 文件消息渲染 | ✅ 手动 |
| MessageBubble | 系统消息渲染 | ✅ 手动 |
| MessageBubble | 自己/他人布局 | ✅ 手动 |
| MessageBubble | 消息状态显示 | ✅ 手动 |
| ThemeToggle | 三态切换 | ✅ 手动 |
| ThemeToggle | 图标切换 | ✅ 手动 |

#### 编辑器组件

| 组件 | 测试项 | 状态 |
|------|--------|------|
| Toolbar | 按钮渲染 | ✅ 手动 |
| Toolbar | 命令发射 | ✅ 手动 |
| CodeBlock | 代码渲染 | ✅ 手动 |
| CodeBlock | 复制功能 | ✅ 手动 |
| CodeBlock | 展开/收起 | ✅ 手动 |
| CodeBlock | 行号显示 | ✅ 手动 |
| ImagePreview | 显示/隐藏 | ✅ 手动 |
| ImagePreview | 图片渲染 | ✅ 手动 |

#### 社交组件

| 组件 | 测试项 | 状态 |
|------|--------|------|
| FriendCard | 好友信息渲染 | ✅ 手动 |
| FriendCard | 未读徽章 | ✅ 手动 |
| ChatList | 好友列表渲染 | ✅ 手动 |
| ChatList | 在线状态 | ✅ 手动 |
| InvitationCard | 邀请信息渲染 | ✅ 手动 |
| InvitationCard | 接受/拒绝操作 | ✅ 手动 |

### 5.3 页面测试清单

| 页面 | 测试项 | 状态 |
|------|--------|------|
| Login | 登录表单 | ✅ 手动 |
| Login | 注册表单 | ✅ 手动 |
| Login | 表单验证 | ✅ 手动 |
| Login | 登录成功跳转 | ✅ 手动 |
| DocumentList | 文档列表加载 | ✅ 手动 |
| DocumentList | 创建文档 | ✅ 手动 |
| DocumentList | 删除文档 | ✅ 手动 |
| DocumentList | 分页 | ✅ 手动 |
| Editor | 富文本编辑 | ✅ 手动 |
| Editor | 保存内容 | ✅ 手动 |
| Editor | 保存标题 | ✅ 手动 |
| Editor | 分享弹窗 | ✅ 手动 |
| Editor | 协作者管理 | ✅ 手动 |
| Social | 好友列表 | ✅ 手动 |
| Social | 聊天功能 | ✅ 手动 |
| Social | 好友请求 | ✅ 手动 |
| Social | 协作邀请 | ✅ 手动 |
| ShareView | 分享文档查看 | ✅ 手动 |
| Components | 组件文档展示 | ✅ 手动 |

### 5.4 暗黑模式测试

| 测试项 | 预期 | 状态 |
|--------|------|------|
| 亮色主题 | 浅色背景，深色文字 | ✅ |
| 暗色主题 | 深色背景，浅色文字 | ✅ |
| 跟随系统 | 自动切换 | ✅ |
| 持久化 | 刷新后保持 | ✅ |
| 所有组件 | 组件正常显示 | ✅ |

---

## 6. 集成测试

### 6.1 REST API 集成测试（计划）

使用 `@WebMvcTest` + MockMvc 测试 Controller 层：

```java
// 示例：用户注册接口测试
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setPassword("password123");
        request.setNickname("测试用户");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
```

### 6.2 计划中的集成测试

| 测试用例 | 接口 | 场景 |
|----------|------|------|
| 注册成功 | POST /api/auth/register | 正常注册流程 |
| 登录成功 | POST /api/auth/login | 正常登录流程 |
| 登录失败 | POST /api/auth/login | 密码错误 |
| 文档创建 | POST /api/docs | 创建文档并校验返回 |
| 文档列表 | GET /api/docs | 分页查询 |
| 文档更新 | PUT /api/docs/{id} | 标题+内容更新 |
| 文档删除 | DELETE /api/docs/{id} | 拥有者删除 |
| 文档删除无权限 | DELETE /api/docs/{id} | 非拥有者删除 |
| 好友搜索 | GET /api/social/users/search | 模糊查询 |
| 发送好友请求 | POST /api/social/friends/request | 正常流程 |
| 拒绝重复请求 | POST /api/social/friends/request | 幂等校验 |

### 6.3 WebSocket 集成测试（计划）

```java
// 示例：WebSocket 协同测试
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class CollaborationWebSocketTest {

    @Test
    void testJoinAndSync() throws Exception {
        // 1. 建立 STOMP 连接
        // 2. 发送 /app/doc/1/join
        // 3. 验证收到 /queue/sync 消息
        // 4. 发送操作
        // 5. 验证收到 /topic/doc/1 广播
    }
}
```

---

## 7. 端到端测试

### 7.1 核心用户旅程

#### 旅程 1：用户注册 → 登录 → 创建文档 → 编辑

```
步骤：
1. 访问 http://localhost:5173
2. 点击"注册"Tab
3. 输入密码和昵称，点击注册
4. ✅ 系统分配账号，显示成功提示
5. 输入分配的账号和密码登录
6. ✅ 跳转到文档列表页
7. 点击"新建文档"
8. ✅ 进入编辑器页面
9. 输入标题，编辑内容
10. ✅ 保存成功，显示"已保存"
```

#### 旅程 2：文档分享 → 查看

```
步骤：
1. 在编辑器点击"分享"
2. 选择权限（只读），生成分享链接
3. ✅ 显示分享链接
4. 新开浏览器无痕窗口访问链接
5. ✅ 显示文档内容（只读模式）
```

#### 旅程 3：好友 → 聊天 → 协作邀请

```
步骤：
1. 用户 A 登录，进入社交页
2. 搜索用户 B 的账号
3. ✅ 找到用户 B
4. 发送好友请求
5. ✅ 请求发送成功
6. 用户 B 登录，进入社交页
7. ✅ 看到好友请求
8. 接受请求
9. ✅ 好友添加成功
10. 用户 A 打开与 B 的聊天
11. ✅ 发送消息，B 收到
12. 用户 A 向 B 发送协作邀请
13. 用户 B 接受邀请
14. ✅ B 成为文档协作者
```

#### 旅程 4：暗黑模式

```
步骤：
1. 登录进入首页
2. 点击 ThemeToggle 按钮
3. ✅ 页面切换为暗色主题
4. 刷新页面
5. ✅ 保持暗色主题
6. 再次点击切换为系统跟随
7. ✅ 自动跟随系统主题
```

### 7.2 回归测试清单

#### 每次发布前必测

| 编号 | 测试项 | 优先级 |
|------|--------|--------|
| R01 | 用户注册 | P0 |
| R02 | 用户登录/登出 | P0 |
| R03 | Token 过期自动跳转登录 | P0 |
| R04 | 创建文档 | P0 |
| R05 | 文档列表加载 | P0 |
| R06 | 打开编辑器 | P0 |
| R07 | 编辑并保存文档 | P0 |
| R08 | 编辑并保存标题 | P0 |
| R09 | 删除文档 | P0 |
| R10 | 好友搜索/添加/删除 | P1 |
| R11 | 发送/接收聊天消息 | P1 |
| R12 | 发送/接受协作邀请 | P1 |
| R13 | 分享文档/查看分享 | P1 |
| R14 | 暗黑模式切换 | P1 |
| R15 | 组件文档页面渲染 | P2 |

### 7.3 浏览器兼容性测试

| 浏览器 | 状态 |
|--------|------|
| Chrome 120+ | ✅ 通过 |
| Firefox 120+ | ✅ 通过 |
| Edge 120+ | ✅ 通过 |
| Safari 17+ | ⚠️ 部分功能可能受限（execCommand 差异） |

---

## 8. 测试用例清单

### 8.1 OT 引擎测试用例（17 个现存 + 6 个计划）

```
现存:
[OT-001] testApplyInsert            — insert("hello") on "world" → "helloworld"
[OT-002] testApplyDelete            — delete(5) on "helloworld" → "world"
[OT-003] testApplyRetain            — retain(5) on "hello" → "hello"
[OT-004] testApplyInsertAndDelete    — insert("XX") + delete(5) → "XXworld"
[OT-005] testTransformInsertInsert   — 并发插入，各自保留
[OT-006] testTransformInsertRetain   — 插入 vs 跳过
[OT-007] testTransformRetainDelete   — 跳过 vs 删除
[OT-008] testTransformDeleteInsert   — 删除 vs 插入
[OT-009] testComposeEmpty            — 空操作组合
[OT-010] testComposeInsertInsert     — 插入 + 插入
[OT-011] testComposeRetainInsert     — 跳过 + 插入
[OT-012] testInvertInsert            — 插入反转 → 删除
[OT-013] testInvertDelete            — 删除反转 → 插入
[OT-014] testInvertRetain            — 跳过反转 → 空
[OT-015] testOperationBufferClone    — 克隆独立性
[OT-016] testOperationBufferMerge    — 相同操作合并
[OT-017] testOperationBufferNoMergeDifferentText — 不同操作不合并

计划:
[OT-018] 复杂组合操作 — INSERT + RETAIN + DELETE 混合
[OT-019] 多层 Transform — 连续 transform 应用
[OT-020] Compose+Apply 一致性 — compose(op1,op2).apply == op2.apply(op1.apply)
[OT-021] 并发冲突模拟 — 多用户同位置编辑
[OT-022] 大文档性能 — 10 万+字符
[OT-023] 边界值 — 空/单字符/全量替换
```

### 8.2 API 集成测试用例（计划）

```
[API-001] 注册成功
[API-002] 注册密码为空
[API-003] 登录成功
[API-004] 登录密码错误
[API-005] 登录账号不存在
[API-006] 未认证访问受保护接口
[API-007] Token 无效
[API-008] 创建文档成功
[API-009] 文档列表分页
[API-010] 文档详情（拥有者）
[API-011] 文档详情（协作者）
[API-012] 文档更新（拥有者）
[API-013] 文档更新（编辑者）
[API-014] 文档更新（查看者 → 403）
[API-015] 文档删除（拥有者）
[API-016] 文档删除（非拥有者 → 403）
[API-017] 创建分享链接
[API-018] 访问有效分享链接
[API-019] 访问过期分享链接
[API-020] 撤销分享链接
[API-021] 好友搜索
[API-022] 发送好友请求
[API-023] 处理好友请求（接受）
[API-024] 处理好友请求（拒绝）
[API-025] 删除好友
[API-026] 发送协作邀请
[API-027] 处理协作邀请
```

### 8.4 新增功能 API 测试用例（v1.3.0）

**文件夹系统：**
```
[API-F01] 创建文件夹       POST /api/folders { name: "工作", parentId: 0 }
[API-F02] 创建空名称文件夹  POST /api/folders { name: "" }            (预期 400)
[API-F03] 创建子文件夹      POST /api/folders { name: "子目录", parentId: X }
[API-F04] 获取文件夹树      GET  /api/folders                        (预期树形结构)
[API-F05] 重命名文件夹      PUT  /api/folders/{id}/rename?name=xxx
[API-F06] 删除文件夹        DELETE /api/folders/{id}                 (子文件夹移至根目录)
[API-F07] 删除不存在的文件夹 DELETE /api/folders/99999             (预期 400 或 404)
[API-F08] 其他用户文件夹隔离 GET  /api/folders                       (预期只看到自己的)
```

**评论系统：**
```
[API-C01] 创建评论          POST /api/documents/{id}/comments { content: "...", rangeStart: 0, rangeEnd: 10 }
[API-C02] 创建无范围评论    POST /api/documents/{id}/comments { content: "整体意见" }
[API-C03] 获取评论列表      GET  /api/documents/{id}/comments
[API-C04] 回复评论          POST /api/documents/{id}/comments/{cid}/reply?content=...
[API-C05] 标记已解决        PUT  /api/documents/{id}/comments/{cid}/resolve?resolved=1
[API-C06] 重新打开评论      PUT  /api/documents/{id}/comments/{cid}/resolve?resolved=0
[API-C07] 删除评论          DELETE /api/documents/{id}/comments/{cid}
[API-C08] 评论用户昵称显示 GET  /api/documents/{id}/comments        (预期返回 username/avatarUrl)
```

**通知系统：**
```
[API-N01] 获取通知列表      GET  /api/notifications
[API-N02] 标记已读          PUT  /api/notifications/{id}/read
[API-N03] 全部已读          PUT  /api/notifications/read-all
[API-N04] 未读计数          GET  /api/notifications/unread-count
[API-N05] 空通知列表        GET  /api/notifications                  (预期空数组)
[API-N06] 通知按时间倒序    GET  /api/notifications                  (预期最新通知在前)
```

### 8.5 前端编辑器测试用例（v1.3.0）

```
[EDIT-001] TipTap 编辑器渲染           — 组件挂载后显示占位符
[EDIT-002] 标题切换                    — 点击 H1/H2/H3 按钮切换标题
[EDIT-003] 粗体/斜体/下划线/删除线     — 选择文本 + 点击格式按钮
[EDIT-004] 有序/无序列表               — 点击列表按钮创建列表项
[EDIT-005] 引用块                      — 点击引用按钮创建引用块
[EDIT-006] 代码块语法高亮              — 插入代码块，指定语言，验证高亮
[EDIT-007] 表格插入                    — 插入 3x3 表格，添加行/列
[EDIT-008] 链接插入                    — 选择文本，插入链接
[EDIT-009] 图片插入                    — 粘贴图片 URL
[EDIT-010] 分割线插入                  — 点击分割线按钮
[EDIT-011] 撤销/重做                   — Ctrl+Z / Ctrl+Y 或按钮操作
[EDIT-012] 输入同步                    — 输入内容 → onUpdate 触发 → emit('change', html)
[EDIT-013] setContent 加载             — 从后端获取内容，编辑器显示
[EDIT-014] getHTML 获取                — 编辑后获取 HTML，内容一致
```

### 8.6 前端组件测试用例（计划用 Vitest）

```
[UI-001] Avatar 文字头像渲染
[UI-002] Avatar 图片头像渲染
[UI-003] Modal 显示/隐藏
[UI-004] Pagination 页码点击
[UI-005] MessageBubble 文本消息
[UI-006] MessageBubble 图片消息
[UI-007] MessageBubble 文件消息
[UI-008] MessageBubble 系统消息
[UI-009] MessageBubble isSelf 布局
[UI-010] CodeBlock 代码渲染
[UI-011] CodeBlock 复制功能
[UI-012] CodeBlock 展开/收起
[UI-013] ThemeToggle 切换主题
[UI-014] FriendCard 好友信息
[UI-015] ChatList 列表渲染
[UI-016] InvitationCard 邀请操作
```

---

## 附录

### A. 测试环境配置

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:gdoc_test;MODE=MySQL
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  redis:
    host: localhost
    port: 6379
```

### B. 测试规范建议

**单元测试规范**：

1. 每个测试方法只测试一个行为，一个断言或一组相关断言
2. 测试方法名清晰表明被测场景
3. 使用 `// Given` / `// When` / `// Then` 三段落注释
4. 避免测试间依赖，每个测试独立可运行
5. 不测试私有方法（通过公有方法间接测试）

**集成测试规范**：

1. 使用随机端口（`webEnvironment = RANDOM_PORT`）
2. 测试数据在 `@BeforeEach` 中准备，在 `@AfterEach` 中清理
3. 优先使用 Mock 隔离外部依赖
4. WebSocket 测试考虑连接超时

**前端测试规范**：

1. 每个组件一个测试文件（`{ComponentName}.spec.ts`）
2. 测试组件渲染（snapshot）、props 响应、事件发射
3. 使用 `shallowMount` 隔离子组件
4. 模拟外部依赖（Pinia store、axios）

### C. 常见问题排查

| 问题 | 排查方向 |
|------|----------|
| 测试运行失败 | 检查数据库连接、Redis 是否启动 |
| OT 测试断言失败 | 验证操作序列构造、transform 结果 |
| 前端测试组件不渲染 | 检查 props 是否正确传递 |
| WebSocket 测试超时 | 检查端口配置、STOMP 端点路径 |
| MockMvc 返回 401 | 检查测试中是否设置了认证 Token |