# AGENTS.md

## 快速命令

```bash
mvn spring-boot:run            # 启动后端（端口 8081）
cd bookstore-frontend; npm install; npm run dev  # 启动前端（端口 5173，代理 /api /admin 到 8081）
mysql -u root -p < sql/init.sql  # 初始化数据库（创建 bookstore 库、8 张表、测试数据）
```

构建与测试：
```bash
mvn clean package              # 完整构建
mvn compile                    # 仅快速编译检查
# pom.xml 中有 spring-boot-starter-test，但 src/test/ 下只有 BCrypt 的 main()，没有真实测试
```

## 关键：雪花 ID 精度丢失

MyBatis-Plus 的 `ASSIGN_ID` 生成 19 位 Long，超出 JS 的 `Number.MAX_SAFE_INTEGER`。**每个从 Java 传到前端的 Long 类型 ID 都必须序列化为字符串**，否则会静默丢失精度。

| 层级 | 做法 | 示例 |
|-------|---------|---------|
| **VO**（响应） | 每个 Long ID 字段加 `@JsonSerialize(using = ToStringSerializer.class)` | `BookVO.bookId`，所有 9 个 VO |
| **Entity**（响应 + admin 的 `@RequestBody`） | `BaseEntity.id`、`Book.categoryId`、`Category.parentId` 加 `@JsonFormat(shape = JsonFormat.Shape.STRING)` | 双向转换 |
| **DTO**（请求体） | ID 字段用 `String` 而非 `Long` | `OrderCreateDTO.cartItemIds: List<String>`、`CartUpdateDTO.id: String` |
| **Service** | `Long.parseLong(dto.getId())` 转回 Long | `AddressServiceImpl.update()` |

**VO 不继承 BaseEntity**——每个 VO 在 `convertToVO()` 中手工复制字段。新增 VO 时每个 Long ID 字段都要加注解。

## 架构

- **Spring Boot 3.2.5** + **MyBatis-Plus 3.5.6**（`classpath:mapper/*.xml` 已配置但项目中没用 XML 映射文件）
- **Controller → Service → Mapper**，DTO 入 / VO 出，统一包装为 `Result<T>(code,message,data)`
- **分页**：书籍查询用 `PageResult<T>(total,records)`；后台管理用 MyBatis-Plus 的 `Page<Entity>`；由 `PaginationInnerInterceptor` 支持
- **认证**：`AuthInterceptor`（位于 `interceptor/` 包）拦截 `/api/user/*`、`/api/address/**`、`/api/cart/**`、`/api/order/**`、`/api/review/add`、`/admin/**`——验证 JWT（`Authorization: Bearer <token>`），用户信息存到 `AuthContext`（ThreadLocal）
- **管理员**：`AdminInterceptor` 检查角色是否为 `admin`
- **密码**：BCrypt（仅 `spring-security-crypto`，没有完整的 Spring Security）；**JWT**：HMAC-SHA384（jjwt 0.12.5），24 小时过期，密钥在 `application.yml` 的 `jwt.secret`
- **订单状态**：`pending → paid → shipped → delivered → completed`；`pending → cancelled`；`paid/shipped → refunded`（**Constants 中定义了，但服务层没有实现退款逻辑**）
- **拦截器**放在 `interceptor/` 包下，不在 `config/`
- `@MapperScan("com.example.bookstore.mapper")` 在 `BookstoreApplication.java` 中

## 关键约束

- SQL 中表名 `` `order` `` 必须加反引号（MySQL 保留字）
- 默认管理员：`admin` / `123456`（BCrypt 加密，在种子数据中）
- 数据库密码：`application-local.yml`（已 gitignore，值为 `${DB_PASSWORD:-}`）；复制 `application-local.yml.example` 使用
- `BaseEntity` 提供 `id`（雪花算法）、`createTime`、`updateTime`（MyBatis-Plus 自动填充）
- **封面图片**：`pictures/` 目录存放封面 JPG，通过 `/pictures/*` 访问（后端静态资源映射 + Vite 代理）

## 前端

- **两个 axios 实例**：`src/api/index.js`（用户端，base `/api`，错误时显示 ElMessage，401 自动退出登录）+ `src/api/admin.js`（管理端，base `/api`，原始响应，拦截器较少）。两个都从 `localStorage` 取 token 加 `Bearer`。
- 管理端 API 路径使用 `/admin/` 前缀（例如 `GET /admin/book/list`）
- 路径别名 `@` → `src/`（例如 `import api from '@/api'`）
- `OrderItemVO` 字段名为 `bookTitle`/`bookAuthor`，**不是** `title`/`author`
- `OrderVO` 是扁平字段 `receiverName`/`receiverPhone`/`receiverAddress`，**不是** 嵌套的 `address.receiver`
- 购物车更新接口传 `{id, quantity}`（购物车记录 ID），**不是** `{bookId, quantity}`
- 创建订单接口传 `{cartItemIds: [购物车记录ID], addressId}`，**不是** `{items: [{bookId, quantity}]}`
