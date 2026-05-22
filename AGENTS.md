# AGENTS.md

## 快速命令

```bash
mvn spring-boot:run                         # 后端（端口 8081）
cd bookstore-frontend; npm run dev           # 前端（端口 5173）
mysql -u root -p < sql/init.sql             # 初始化 bookstore 库（10 张表 + 种子数据）
mvn test                                    # 纯 JUnit 5 + Mockito，无需 Spring 上下文
mvn clean package -DskipTests               # 完整构建
```

无 linter/formatter，构建仅 `mvn test` + `npm run build`。

## 雪花 ID 精度丢失（重点）

MyBatis-Plus `ASSIGN_ID` 生成 19 位 Long，超出 JS `Number.MAX_SAFE_INTEGER`。**必须字符串化**：

| 层级 | 做法 |
|------|------|
| **VO**（响应） | `@JsonSerialize(using = ToStringSerializer.class)` 在全部 11 个 VO 的 Long ID 字段 |
| **Entity** | `@JsonFormat(shape = JsonFormat.Shape.STRING)` 在 `BaseEntity.id`、`Book.categoryId`、`Category.parentId` |
| **DTO**（请求体） | ID 字段声明为 `String`（`OrderCreateDTO.addressId`/`cartItemIds`、`CartUpdateDTO.id`、`AddressUpdateDTO.id`） |
| **Service** | `Long.parseLong(dto.getId())` 转回 |

VO 独立于 Entity，字段在 `convertToVO()` 中手工复制。`FavoriteController.add()` 须用 `Map<String, String>` + `Long.parseLong()` 接收前端字符串 ID（此前 `Map<String, Long>` 会导致 null）。

## Vite 代理规则（`vite.config.js`）

| 前端路径 | 后端目标 | 说明 |
|----------|---------|------|
| `/api` | `localhost:8081/api` | 用户 API 透传 |
| `/admin-api` | `localhost:8081/admin` | **路径重写** `/admin-api` → `/admin` |
| `/pictures` | `localhost:8081/pictures` | 封面图静态资源 |

## 两个 axios 实例

| 文件 | baseURL | 响应处理 | 401 处理 |
|------|---------|----------|----------|
| `src/api/index.js` | `/api` | 非 200 弹 `ElMessage.error`，返回 `res`（解包 `response.data`） | 清除 token，`router.push('/login')` |
| `src/api/admin.js` | `/admin-api` | 返回 `response.data`（直接透传，无 `res.code` 判断） | 清除 token，`window.location.href = '/login'` |

管理端方法传相对路径（如 `/book/list`），拼接后为 `GET /admin/book/list`。

## 架构要点

- **Spring Boot 3.2.5** + **MyBatis-Plus 3.5.6**（全注解查询，XML 映射配置未使用）
- **Controller → Service → Mapper**，DTO 入 / VO 出，统一 `Result<T>(code,message,data)`
- **Admin 控制器直接注入 Mapper**（BookManageController、OrderManageController、UserManageController、ReviewManageController），仅 `CategoryManageController` 走 Service
- **拦截器**：`AuthInterceptor`（用户路径 + `/admin/**`，设 `AuthContext` ThreadLocal），`AdminInterceptor`（校验角色 admin）
- **密码**：仅 `spring-security-crypto`（BCrypt），非完整 Spring Security
- **CORS**：仅允许 `http://localhost:*`
- **默认管理员**：`admin` / `123456`
- **封面图**：`pictures/` 目录，`WebMvcConfig` 映射为 `/pictures/**` 静态资源
- **前端**：Vue 3 + Vite + Element Plus + Pinia（6 stores：cart/category/favorite/order/review/user）
- **应用入口**：`BookstoreApplication.java`，`@MapperScan("com.example.bookstore.mapper")`

## 订单状态

```
created → paying → paid → shipped → delivered → completed
   ↓ (过期)    ↓ (取消)
expired      cancelled
                    paid/shipped → refunded
```

## 前端关键约定

- `OrderItemVO` 字段名 `bookTitle`/`bookAuthor`，**不是** `title`/`author`
- `OrderVO` 扁平字段 `receiverName`/`receiverPhone`/`receiverAddress`，**不是** 嵌套 `address`
- 购物车更新传 `{id, quantity}`（购物车记录 ID），**不是** `{bookId, quantity}`
- 创建订单传 `{cartItemIds: [购物车记录ID], addressId}`，**不是** `{items: [{bookId, quantity}]}`
- **"立即购买"走购物车**：`BookDetail.vue` → `/order/confirm?bookId=X&quantity=Y` → `OrderConfirm.vue` 先调 `cartStore.addToCart` 再取 cart ID 下单

## 运行约束

- SQL 中表名 `` `order` `` 必须加反引号（MySQL 保留字）
- 数据库密码在 `application-local.yml`（从 `.example` 复制），`.gitignore` 排除了 `application.yml` + `application-local.yml`
- 测试为纯 Mockito 单元测试（`@ExtendWith(MockitoExtension.class)`），无需数据库
- E2E 冒烟测试：`python test_smoke.py`（Playwright，需前后端运行）

## 易错点

- **ID 生成矛盾**：SQL DDL 声明 `AUTO_INCREMENT`，但 MyBatis-Plus `@TableId` 默认 `ASSIGN_ID`（雪花算法），实际插入使用 19 位 Long ID。DDL 的 `AUTO_INCREMENT` 仅为 fallback。
- **无 MetaObjectHandler**：`BaseEntity` 标注 `@TableField(fill = FieldFill.INSERT/UPDATE)` 但无实现类，`createTime`/`updateTime` 的自动填充依赖 MySQL `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP`。
- **排行榜 API**（`BookController.getRanking`）：`type` 参数（`sales`/`rating`/`new`）生效，`period` 参数（`all`/`week`/`month`/`quarter`/`year`）**仅 `new` 类型实际使用**，其余类型暂忽略 period。
- **SQL init 脚本中的 ALTER TABLE**（`orig_price` / `favorited_price` 列）仅用于已有数据库迁移，`CREATE TABLE` 已包含这些列。

## 分页响应

```json
{ "code": 200, "message": "操作成功", "data": { "total": 100, "records": [...] } }
```

`PageResult<T>` 含 `total`（Long）+ `records`（List\<T\>）。MyBatis-Plus `Page` 对象直接序列化含大量无关字段，仅 `total`/`records` 由 `PageResult` 包装。
