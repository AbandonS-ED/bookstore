# AGENTS.md

## 快速命令

```bash
mvn spring-boot:run                         # 启动后端（端口 8081）
cd bookstore-frontend; npm install; npm run dev  # 启动前端（端口 5173）
mysql -u root -p < sql/init.sql             # 初始化数据库（bookstore 库、9 张表、测试数据）
mvn test                                    # 运行单元测试（19 个，JUnit 5 + Mockito）
mvn compile                                 # 仅编译检查
mvn clean package -DskipTests               # 完整构建
```

## 关键：雪花 ID 精度丢失

MyBatis-Plus `ASSIGN_ID` 生成 19 位 Long，超出 JS `Number.MAX_SAFE_INTEGER`。**每个从 Java 传到前端的 Long 类型 ID 都必须序列化为字符串**，否则静默丢失精度。

| 层级 | 做法 | 示例 |
|-------|---------|---------|
| **VO**（响应） | Long ID 字段加 `@JsonSerialize(using = ToStringSerializer.class)` | 10 个 VO 中 9 个已加；**`PaymentVO.id` / `PaymentVO.orderId` 缺少——疑似 bug** |
| **Entity**（响应 + admin `@RequestBody`） | `BaseEntity.id`、`Book.categoryId`、`Category.parentId` 加 `@JsonFormat(shape = JsonFormat.Shape.STRING)` | 双向转换 |
| **DTO**（请求体） | ID 字段用 `String` 而非 `Long` | `OrderCreateDTO.cartItemIds: List<String>`、`CartUpdateDTO.id: String` |
| **Service** | `Long.parseLong(dto.getId())` 转回 Long | `AddressServiceImpl.update()` |

**VO 不继承 BaseEntity**——每个 VO 在 `convertToVO()` 中手工复制字段。新增 VO 时每个 Long ID 字段都要加注解。

## 架构要点

- **Spring Boot 3.2.5** + **MyBatis-Plus 3.5.6**（XML 映射文件路径 `classpath:mapper/*.xml` 已配置但项目中未使用）
- **Controller → Service → Mapper**，DTO 入 / VO 出，统一 `Result<T>(code,message,data)`
- **Admin 控制器直接注入 Mapper**（BookManage/OrderManage/UserManage/ReviewManage），仅 `CategoryManageController` 走 Service
- **拦截器**在 `interceptor/` 包：`AuthInterceptor`（JWT 验证 → `AuthContext` ThreadLocal）、`AdminInterceptor`（校验 role=admin）
- **JWT**：HMAC-SHA（jjwt 0.12.5），24h 过期，密钥在 `application.yml` 的 `jwt.secret`
- **订单状态**：`pending → paid → shipped → delivered → completed`；`pending → cancelled`；`paid/shipped → refunded`（退款在 `OrderManageController.refund()` 中实现）
- **分页**：用户端用 `PageResult<T>(total,records)`；管理端直接用 MyBatis-Plus `Page<Entity>`

## Vite 代理规则（易错）

| 前端路径 | 后端目标 | 说明 |
|-----------|-------|------|
| `/api/*` | `localhost:8081/api/*` | 用户 API，直接透传 |
| `/admin-api/*` | `localhost:8081/admin/*` | 管理 API，**路径重写** `/admin-api` → `/admin` |

- `src/api/index.js` 用 `baseURL: '/api'`（用户端，ElMessage 错误提示，401 自动退出）
- `src/api/admin.js` 用 `baseURL: '/admin-api'`（管理端，拦截器少，返回 `response.data`）
- 两个实例都从 `localStorage` 取 token 加 `Authorization: Bearer <token>`
- 管理端方法传相对路径（如 `/book/list`），拼接后为 `GET /admin/book/list`

## 关键约束

- SQL 中表名 `` `order` `` 必须加反引号（MySQL 保留字）
- 数据库共 9 张表（含 `payment`），`init.sql` 已包含全部 DDL + 种子数据
- 默认管理员：`admin` / `123456`（BCrypt 加密）
- 数据库密码：`application-local.yml`（gitignored）；复制 `application-local.yml.example` 使用
- `BaseEntity` 提供 `id`（雪花算法）、`createTime`、`updateTime`（MyBatis-Plus 自动填充）
- 封面图片：`pictures/` 目录，通过 `/pictures/*` 访问（后端静态资源映射 + Vite 代理）
- 路径别名 `@` → `src/`（前端）

## 前端关键约定

- `OrderItemVO` 字段名为 `bookTitle`/`bookAuthor`，**不是** `title`/`author`
- `OrderVO` 是扁平字段 `receiverName`/`receiverPhone`/`receiverAddress`，**不是** 嵌套的 `address`
- 购物车更新传 `{id, quantity}`（购物车记录 ID），**不是** `{bookId, quantity}`
- 创建订单传 `{cartItemIds: [购物车记录ID], addressId}`，**不是** `{items: [{bookId, quantity}]}`
- **"立即购买"走购物车**：`BookDetail.vue` 跳到 `/order/confirm?bookId=X&quantity=Y`，`OrderConfirm.vue` 的 `handleSubmit` 先调 `cartStore.addToCart` 再取 cart ID 下单，而非从空 cart 取数据（否则 `@NotEmpty` 拒绝）
