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

### BigInt 精度问题（重要）

MyBatis-Plus 雪花算法生成 **19位 Long 类型 ID**，直接序列化到 JSON 时 JavaScript 会丢失精度（JS Number 最大安全整数约 16 位）。

**修复方案：**
- VO 类：所有 Long 类型 ID 字段添加 `@JsonSerialize(using = ToStringSerializer.class)`
- Entity 基类：`id` 字段添加 `@JsonFormat(shape = JsonFormat.Shape.STRING)` 实现双向转换
- DTO 类：接收前端字符串 ID，Service 层用 `Long.parseLong()` 转换

**涉及文件（9个VO、3个DTO、3个ServiceImpl、2个Entity）**

### 前端 API 结构（关键）

**双 axios 实例设计**：
- `src/api/index.js` → 用户端 API，`baseURL: '/api'`
- `src/api/admin.js` → 管理端 API，`baseURL: '/api'`

**代理规则**（vite.config.js）：
- `/api/xxx` → 后端 `http://localhost:8081/api/xxx`
- `/admin/xxx` → 后端 `http://localhost:8081/admin/xxx`（注意路径直接是 `/admin` 不是 `/api/admin`）

前端调用示例：
```js
// 用户端
api.get('/user/info')

// 管理端（admin.js自动加前缀 /admin）
admin.get('/book/list')
```

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
