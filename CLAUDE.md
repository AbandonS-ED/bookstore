# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

基于 **Spring Boot 3 + MyBatis-Plus** 的网上书店系统（课程设计项目）。

**技术栈：**
- Java 17 / Spring Boot 3.2.5
- MyBatis-Plus 3.5.6
- MySQL 8.0 / Druid 1.2.23
- Vue 3 + Vite

## 常用命令

```bash
# 后端
mvn spring-boot:run          # 启动后端（端口 8081）
mvn test                     # 运行测试
mvn clean package -DskipTests # 打包

# 前端
cd bookstore-frontend
npm install                  # 安装依赖
npm run dev                  # 开发模式（端口 5173）
npm run build                # 生产构建
```

## 快速启动

1. 初始化数据库：`mysql -u root -p < sql/init.sql`
2. 启动后端：`mvn spring-boot:run`
3. 启动前端：`cd bookstore-frontend && npm run dev`

**默认管理员账号：** admin / 123456（BCrypt加密）

## 架构要点

### 后端结构

```
com.example.bookstore/
├── config/          # CORS、MyBatis-Plus、WebMvc配置
├── controller/      # REST控制器 + admin/管理控制器
├── service/        # 业务层接口 + impl/实现类
├── mapper/         # MyBatis-Plus Mapper接口
├── entity/         # 实体类（BaseEntity提供id/createTime/updateTime）
├── dto/            # 数据传输对象（接收前端参数）
├── vo/             # 视图对象（API响应，格式化输出）
├── common/         # Result统一响应、Constants常量
├── exception/      # BusinessException、GlobalExceptionHandler
├── interceptor/    # AuthInterceptor（JWT验证）、AdminInterceptor（管理员验证）
└── util/           # SecurityUtils、JwtUtils、OrderNoGenerator、AuthContext
```

### 前端结构

```
src/views/user/     # 用户端页面（Home/Books/BookDetail/Cart/Order*/Login/Register等）
src/views/admin/     # 管理后台（AdminHome/AdminBooks/AdminOrders/AdminUsers/AdminReviews）
src/components/     # 公共组件
src/api/            # axios封装（index.js用户端、admin.js管理端）
src/stores/         # Pinia状态管理
src/composables/     # Vue组合式函数
```

### 静态资源

- `pictures/` 目录存放书籍封面图片，后端通过 `/pictures/**` 提供静态资源服务
- Vite 代理 `/pictures` 到后端 `localhost:8081/pictures`
- 书籍封面占位符使用 `getCoverStyle(bookId)` 生成渐变背景

### Vite 代理规则（`vite.config.js`）

| 前端路径 | 后端目标 | 说明 |
|----------|---------|------|
| `/api` | `localhost:8081/api` | 用户 API 透传 |
| `/admin-api` | `localhost:8081/admin` | **路径重写** `/admin-api` → `/admin` |
| `/pictures` | `localhost:8081/pictures` | 封面图静态资源 |

### 认证与授权

- `AuthInterceptor` 拦截特定路径（`/api/user/info`、`/api/user/password`、`/api/user/profile`、`/api/address/**`、`/api/cart/**`、`/api/order/**`、`/api/review/add`、`/admin/**`）验证 JWT Token，设 `AuthContext` ThreadLocal
- `AdminInterceptor` 拦截 `/admin/**` 验证管理员角色
- `AuthContext` 使用 ThreadLocal 存储当前用户

## BigInt 精度问题（重要）

MyBatis-Plus 雪花算法生成 **19位 Long 类型 ID**，超出 JS `Number.MAX_SAFE_INTEGER`（约16位），直接序列化会丢失精度。

**修复方案：**
- VO 类：Long ID 字段加 `@JsonSerialize(using = ToStringSerializer.class)`
- Entity 基类：`id` 字段加 `@JsonFormat(shape = JsonFormat.Shape.STRING)`
- DTO 类：接收 String，前Service层用 `Long.parseLong()` 转回

**涉及文件**：10个VO（PaymentVO/BookVO/OrderVO等）、DTO、ServiceImpl、Entity

## 数据库表（共10张）

| 表名 | 说明 |
|------|------|
| `user` | 用户表（角色：user/admin） |
| `category` | 分类表（支持层级，parent_id） |
| `book` | 书籍表（含库存、价格、封面、orig_price划线原价） |
| `favorite` | 收藏表（user_id + book_id唯一约束，favorited_price收藏时价格） |
| `address` | 收货地址表 |
| `cart` | 购物车表（user_id + book_id唯一约束） |
| `order` | 订单主表（含支付ID、物流单号） |
| `order_item` | 订单明细表（冗余书籍信息） |
| `payment` | 支付记录表（支付流水号、方式、金额、状态） |
| `review` | 评论表（评分1-5） |

## 订单状态流转

完整状态定义见 `Constants.java`：
```
created(已创建) → paying(支付中) → paid(已付款) → shipped(已发货) → delivered(已收货) → completed(已完成)
   ↓ (过期)        ↓ (取消)
expired          cancelled
                           paid/shipped → refunded(已退款)
```

## 关键约束

- SQL 中 `order` 表名必须加反引号（MySQL保留字）
- `BaseEntity` 提供 id（雪花算法，`@JsonFormat(Shape.STRING)` 字符串化）、createTime、updateTime
- 密码使用 BCrypt 加密，密钥从 `application.yml` 的 `jwt.secret` 读取
- 订单号通过 `OrderNoGenerator` 生成
- `.gitignore` 排除了 `application.yml` + `application-local.yml`，数据库密码放 `application-local.yml`（从 `.example` 复制）
- Admin 管理控制器（BookManageController、OrderManageController、UserManageController、ReviewManageController）**直接注入 Mapper**，不走 Service 层，仅 `CategoryManageController` 走 Service

## 排行榜 API（getRanking）

- `type`: `sales`（畅销）、`rating`（评分）、`new`（新书）
- `period`: `all`、`week`、`month`、`quarter`、`year`（**仅 new 类型真正使用**，其他类型 period 参数暂未生效）
- 建议返回时加 `limit 20` 限制数量

## 收藏 API

`/api/favorite/*` 接口集（需登录）：

- `POST /add` — 收藏书籍（自动保存 favorited_price）
- `DELETE /{bookId}` — 取消收藏
- `GET /list` — 当前用户收藏列表（含降价提示：hover滑出"比收藏时便宜了¥X"）
- `GET /check/{bookId}` — 检查是否已收藏
- `GET /ids` — 获取已收藏书籍ID集合

## 文档

分析文档位于 `docs/analysis/`：需求分析、数据库设计、技术方案、源码结构
