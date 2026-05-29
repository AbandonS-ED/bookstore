# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 **Spring Boot 3 + MyBatis-Plus** 的网上书店系统（课程设计项目）。

**技术栈：**
- Java 17 / Spring Boot 3.2.5 / MyBatis-Plus 3.5.6
- MySQL 8.0 / Druid 1.2.23
- Vue 3 + Vite + Element Plus + Pinia + Vue Router

## 常用命令

```bash
# 后端
mvn spring-boot:run                    # 启动后端（端口 8081）
mvn test                                # 运行测试（纯 JUnit 5 + Mockito，无需数据库）
mvn clean package -DskipTests           # 打包

# 前端
cd bookstore-frontend
npm install                             # 安装依赖
npm run dev                             # 开发模式（端口 5173）
npm run build                           # 生产构建
```

## 快速启动

1. 初始化数据库：`D:/AppServ/MySQL/bin/mysql.exe -u root bookstore < sql/init.sql`（16 张表 + 种子数据）
2. 配置密码：复制 `application-local.yml.example` 为 `application-local.yml`，填入 MySQL 密码（无密码则留空）
3. 启动后端：`mvn spring-boot:run`
4. 启动前端：`cd bookstore-frontend && npm run dev`

**默认管理员账号：** admin / 123456（BCrypt加密）

## 架构要点

### 后端结构

```
com.example.bookstore/
├── config/          # CORS、MyBatis-Plus 分页、WebMvc 配置
├── interceptor/     # AuthInterceptor（JWT）、AdminInterceptor（管理员校验）
├── controller/      # REST 控制器（AIController、CommunityPostController 等）
│   └── admin/       # 管理后台控制器（直接注入 Mapper 而非 Service）
├── service/impl/    # 业务层实现
├── mapper/          # MyBatis-Plus Mapper（全注解）
├── entity/          # 实体类（继承 BaseEntity 或独立）
├── dto/             # 数据传输对象（接收前端参数）
├── vo/              # 视图对象（API 响应，ID 字符串化）
├── common/          # Result / BaseEntity / Constants / PageResult
├── exception/       # BusinessException / GlobalExceptionHandler
└── util/            # JwtUtils / SecurityUtils(BCrypt) / AuthContext(ThreadLocal) / OrderNoGenerator
```

### 前端结构

```
src/views/user/      # 用户端页面（Home/Books/BookDetail/Cart/Order*/Login/Register/AIAssistant/Explore）
src/views/admin/     # 管理后台（Admin/AdminDashboard/AdminBooks/AdminCategories/AdminOrders/AdminRefund/AdminInventory/AdminUsers/AdminReviews/AdminCommunity）
src/components/      # 公共组件（AppHeader/AppFooter/BookCard/PaginationBar/ModalDialog/ToastContainer）
src/api/             # axios 封装（index.js 用户端、admin.js 管理端）+ ai.js/community.js
src/stores/          # Pinia 状态管理（user/cart/category/order/favorite/review）
src/composables/     # Vue 组合式函数（useToast）
src/utils/           # auth.js / format.js / cover.js
```

### Vite 代理规则（`vite.config.js`）

| 前端路径 | 后端目标 | 说明 |
|----------|---------|------|
| `/api` | `localhost:8081/api` | 用户 API 透传 |
| `/admin-api` | `localhost:8081/admin` | **路径重写** `/admin-api` → `/admin` |
| `/pictures` | `localhost:8081/pictures` | 封面图静态资源 |

### 两个 axios 实例

| 文件 | baseURL | 响应处理 | 401 处理 |
|------|---------|----------|----------|
| `src/api/index.js` | `/api` | `res.code !== 200` → `ElMessage.error`，返回 `res`（`response.data`） | 清除 token，`router.push('/login')` |
| `src/api/admin.js` | `/admin-api` | `res.code !== 200` → `ElMessage.error`，返回 `res`（`response.data`） | 清除 token，`window.location.href = '/login'` |

管理端方法传相对路径（如 `/book/list`），拼接后为 `GET /admin/book/list`。

### 拦截路径

- **AuthInterceptor**：`/api/user/info`、`/api/user/password`、`/api/user/profile`、`/api/address/**`、`/api/cart/**`、`/api/favorite/**`、`/api/order/**`、`/api/review/add`、`/api/ai/**`、`/admin/**`
- **AdminInterceptor**：`/admin/**`（校验 admin 角色）

## AI 书友 (`/ai-assistant`)

| File | Purpose |
|------|---------|
| `AIController.java` | POST `/api/ai/chat` + `/api/ai/chat/stream`，MiniMax 代理 + function calling |
| `src/api/ai.js` | 前端 API，`chatStream()` 支持 SSE 流式输出 |
| `AIAssistant.vue` | 聊天 UI，含欢迎页、书籍卡片、流式打字效果 |

### Tools exposed to MiniMax
- `searchBooks(keyword)` — 按书名/作者搜索
- `getBookDetail(bookId)` — 完整书籍信息
- `addToCart(bookId, quantity)` — 加入购物车（需登录）
- `getCartItems()` — 购物车内容
- `listAddresses()` — 收货地址列表

## 书斋社区 (`/community`)

`CommunityPostController` 处理社区帖子，支持发帖、点赞、列表查询。

## BigInt 精度问题（重要）

MyBatis-Plus 雪花算法生成 **19 位 Long 类型 ID**，超出 JS `Number.MAX_SAFE_INTEGER`（约 16 位）。**必须字符串化**：

- **VO**：所有 Long ID 字段加 `@JsonSerialize(using = ToStringSerializer.class)`
- **Entity**：`BaseEntity.id`、`Book.authorId`/`categoryId`、`Category.parentId` 加 `@JsonFormat(shape = JsonFormat.Shape.STRING)`
- **DTO**：ID 字段声明为 `String`（如 `OrderCreateDTO.addressId`/`cartItemIds`），Service 层 `Long.parseLong()` 转回

**注意**：`OrderItem.id` 使用 `@TableId(type = IdType.AUTO)`（自增），不使用雪花算法。

## 数据库表（共 16 张）

| 表名 | 说明 | 特殊字段 |
|------|------|----------|
| `user` | 用户表（角色 user/admin） | — |
| `category` | 分类表（层级 parent_id） | `status`（1启用 0禁用） |
| `book` | 书籍表 | `favorited_count`（原子更新）、`orig_price`、`author_id`、`expected_shelf_date`（预售） |
| `author` | 作者表 | `bio`/`country`/`birth_year`/`awards` |
| `book_chapter` | 目录表（章节+页码） | **不继承 BaseEntity**，独立 id |
| `favorite` | 收藏表 | `favorited_price`（收藏时价格），唯一约束 `user_id+book_id` |
| `address` | 收货地址表 | `Entity` 中 `@TableField("user_id")` 显式列映射 |
| `cart` | 购物车表 | 唯一约束 `user_id+book_id` |
| `order` | 订单主表（`order` 必须反引号） | `status` / `pay_status` / `express_no` / `expire_time` |
| `order_item` | 订单明细表 | 冗余 `book_title`/`book_author`/`cover_url` |
| `payment` | 支付记录表 | `payment_no`（流水号） |
| `review` | 评论表 | 评分 1-5、`status`（显示/隐藏）、`order_item_id`（订单项关联） |
| `community_post` | 社区帖子表 | 雪花算法 ID，`likes`/`liked`/`book_id` |
| `community_like` | 社区点赞表 | `user_id`/`post_id`，AUTO_INCREMENT |
| `book_quality_review` | 书籍品评表 | 雪花算法 ID，关联 `book_id` |
| `book_excerpt` | 书籍精彩文段表 | `content`/`sort_order`，AUTO_INCREMENT |

## 订单状态流转

```
created → paying → paid → shipped → delivered → completed
   ↓ (过期)    ↓ (取消)
expired      cancelled
                    paid/shipped → refunded
                    delivered/completed → after_sale
```

- `book.sales` 仅在 `pay()` 中原子递增（`UPDATE SET sales = sales + #{quantity}`）
- 取消订单回滚库存但**不回滚** sales
- 申请售后接受 `delivered`（已收货）和 `completed`（已完成）

## 原子 SQL 更新（BookMapper）

- `decreaseStock` — `stock = stock - #{quantity}`，带 `WHERE stock >= #{quantity}` 防超卖
- `increaseSales` — `sales = sales + #{quantity}`，仅在支付时调用
- `increaseFavoritedCount` / `decreaseFavoritedCount` — `favorited_count +/- 1`

## 关键约束

- **无 MetaObjectHandler**：`BaseEntity` 标注 `@TableField(fill = ...)` 但无实现，依赖 MySQL `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP`
- `DDL AUTO_INCREMENT` 仅为 fallback，实际使用雪花算法 19 位 Long
- 管理员控制器（BookManageController、OrderManageController、UserManageController、ReviewManageController）**直接注入 Mapper** 返回原始 `Page<Entity>`，不转 VO
- `PaymentVO.amount` 为 `String`（手写 `.toString()` 转换非 `@JsonSerialize`）

## 排行榜 API（getRanking）

- `type`: `sales`（畅销）、`rating`（评分）、`new`（新书）、`collection`（收藏）
- `period`: `all`、`week`、`month`、`quarter`、`year`（**仅 `new` 类型实际使用**）
- `avgRating` 在查询时从 `review` 表实时计算，非 DB 列

## 文档

分析文档位于 `docs/analysis/`：需求分析、数据库设计、技术方案、源码结构