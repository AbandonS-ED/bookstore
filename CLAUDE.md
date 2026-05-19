# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 **Spring Boot 3 + MyBatis-Plus** 的网上书店系统（网上书店系统），作为计算机专业的课程设计项目。

**技术栈：**
- Java 17 / Spring Boot 3.2.5
- MyBatis-Plus 3.5.6（ORM框架）
- MySQL 8.0（数据库）
- Druid 1.2.23（连接池）
- Vue 3 + Vite（前端框架）

## 常用命令

```bash
# 后端构建
mvn clean package

# 后端运行
mvn spring-boot:run

# 后端编译（不运行）
mvn clean compile -DskipTests

# 运行测试
mvn test

# 运行单个测试类
mvn test -Dtest=类名

# 跳过测试打包
mvn clean package -DskipTests
```

## 前端命令

```bash
cd bookstore-frontend

# 安装依赖
npm install

# 开发模式运行（前端端口 5173）
npm run dev

# 生产构建
npm run build
```

## 数据库初始化

运行应用前需先初始化数据库：

```bash
mysql -u root -p < sql/init.sql
```

将创建包含8张表的数据库 `bookstore`：`user`、`category`、`book`、`address`、`cart`、`order`、`order_item`、`review`。

**默认管理员账号：** 用户名 `admin`，密码 `123456`（BCrypt加密）

## 项目架构

### 包结构

遵循标准Spring Boot三层架构：

```
com.example.bookstore/
├── config/          # 配置类（CORS、MyBatis-Plus、WebMvc）
├── controller/     # REST控制器 + admin/下的管理控制器
├── service/        # 业务层接口 + impl/实现类
├── mapper/         # MyBatis-Plus Mapper接口
├── entity/         # 实体类
├── dto/            # 数据传输对象（请求/响应）
├── vo/             # 视图对象（格式化API响应）
├── common/         # Result统一响应、Constants常量、BaseEntity基类
├── exception/      # BusinessException、GlobalExceptionHandler全局异常处理
└── util/           # SecurityUtils、OrderNoGenerator、StringUtils、DateUtils
```

### 核心设计模式

- **Controller → Service → Mapper** 三层架构
- **DTO输入、VO输出** 分离
- **统一响应格式**：`Result<T>` 封装 `code`、`message`、`data`
- **全局异常处理** 通过 `GlobalExceptionHandler`
- **MyBatis-Plus** 简化CRUD，XML映射文件位于 `resources/mapper/`

### 配置文件

主配置位于 `src/main/resources/application.yml`：
- 后端服务端口：**8081**（API服务）
- 数据库：**localhost:3306/bookstore**
- MyBatis-Plus XML映射：`classpath:mapper/*.xml`
- CORS已配置允许前端开发服务器（如 localhost:5173）访问
- JWT配置：`jwt.secret`（密钥）和 `jwt.expiration`（有效期24小时）

### 认证与授权

- `AuthInterceptor` 拦截 `/api/` 请求验证 JWT Token
- `AdminInterceptor` 拦截 `/admin/` 请求验证管理员角色
- `AuthContext` 使用 ThreadLocal 存储当前用户信息
- 密码使用 `BCryptPasswordEncoder` 加密，不可逆

### 数据库表结构

| 表名 | 用途 |
|------|------|
| `user` | 用户表，包含角色（user/admin） |
| `category` | 分类表（支持层级，通过parent_id） |
| `book` | 书籍表，含库存、价格、封面 |
| `address` | 用户收货地址表 |
| `cart` | 购物车表（user_id + book_id唯一约束） |
| `order` | 订单主表，含状态、支付状态 |
| `order_item` | 订单明细表（冗余书籍信息） |
| `review` | 书籍评论表，评分1-5 |

## 前端结构

- **Vue 3 + Vite** 项目位于 `bookstore-frontend/`
- 组件化开发，Vue Router 路由管理
- **API 层采用双 axios 实例**：
  - `src/api/index.js` - 用户端API，`baseURL: '/api'`，经 vite 代理转发到 `http://localhost:8081`
  - `src/api/admin.js` - 管理端API，`baseURL: '/api'`，请求 `/api/admin/xxx` 经代理转发到后端 `/admin/xxx`
- vite 代理配置：同时代理 `/api` 和 `/admin` 到后端 `http://localhost:8081`
- 后台接口路径：`/admin/xxx`（不是 `/api/admin/xxx`）

## 数据库表

| 表名 | 用途 |
|------|------|
| `user` | 用户表，包含角色（user/admin） |
| `category` | 分类表（支持层级，通过parent_id） |
| `book` | 书籍表，含库存、价格、封面 |
| `address` | 用户收货地址表 |
| `cart` | 购物车表（user_id + book_id唯一约束） |
| `order` | 订单主表，含状态、支付状态 |
| `order_item` | 订单明细表（冗余书籍信息） |
| `review` | 书籍评论表，评分1-5 |

## 订单状态流转

```
pending(待付款) → paid(已付款) → shipped(已发货) → delivered(已收货) → completed(已完成)
    ↓                ↓
cancelled(已取消)   refunded(已退款)
```

## 开发注意事项

- `BookstoreApplication` 使用 `@MapperScan("com.example.bookstore.mapper")` 自动扫描Mapper
- SQL中 `order` 表名需用反引号包裹（保留字）
- 密码使用BCrypt加密（见 `SecurityUtils`），密钥从 `application.yml` 的 `jwt.secret` 读取
- 订单号通过 `OrderNoGenerator` 工具类生成
- 实体类时间字段（createTime/updateTime）通过 `MyMetaObjectHandler` 自动填充

## 文档位置

分析文档位于 `docs/analysis/`：
- 01_需求分析.md - 需求分析
- 02_数据库设计.md - 数据库设计
- 03_技术方案.md - 技术方案
- 04_源码结构.md - 源码结构
