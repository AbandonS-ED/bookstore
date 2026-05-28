# 网上书店系统

基于 Spring Boot 3 + MyBatis-Plus 的网上书店系统，支持用户浏览、购物车、订单管理等核心功能。提供用户端和管理后台两套界面。

## 技术栈

| 分类 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 3.2.5, MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0, Druid 1.2.23 |
| 前端 | Vue 3 + Vite + Element Plus + Pinia + Vue Router |
| 构建 | Maven |

## 功能模块

- **用户模块** — 注册、登录、个人中心、收货地址管理
- **书籍模块** — 书籍浏览、搜索、详情、分类查看、新品上架、排行榜（畅销/评分/新书/收藏）
- **收藏模块** — 收藏书籍（含降价提示）、收藏列表管理
- **购物车模块** — 加入购物车、修改数量、删除
- **订单模块** — 创建订单、订单列表、订单详情、取消、确认收货、售后申请
- **评论模块** — 书籍评论、1-5 星评分
- **后台管理** — 仪表盘、书籍管理、分类管理、库存管理、订单管理、退款售后、用户管理、评论管理

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- Node.js 18+

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 配置数据库密码

复制 `application-local.yml.example` 为 `application-local.yml`，填入 MySQL 密码。

### 3. 启动后端

```bash
mvn spring-boot:run
```

后端运行在 `http://localhost:8081`。

### 4. 启动前端

```bash
cd bookstore-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，自动代理 `/api`、`/admin-api`（重写为 `/admin`）、`/pictures` 到后端。

### 管理员账号

用户名 `admin`，密码 `123456`（BCrypt 加密）

## 项目结构

```
bookstore/                          # 后端 Spring Boot 项目
├── src/main/java/com/example/bookstore/
│   ├── config/                     # CORS、MyBatis-Plus 分页、WebMvc 配置
│   ├── controller/                 # 用户端 REST 控制器
│   │   └── admin/                  # 管理后台控制器
│   ├── service/                    # 业务层接口 + impl/ 实现类
│   ├── mapper/                     # MyBatis-Plus Mapper（全注解）
│   ├── entity/                     # 实体类（继承 BaseEntity）
│   ├── dto/                        # 数据传输对象（接收前端参数）
│   ├── vo/                         # 视图对象（API 响应，ID 字符串化）
│   ├── common/                     # Result 统一响应、BaseEntity、Constants、PageResult
│   ├── interceptor/                # AuthInterceptor（JWT）、AdminInterceptor（角色校验）
│   ├── exception/                  # BusinessException、GlobalExceptionHandler
│   └── util/                       # JwtUtils、SecurityUtils、OrderNoGenerator、AuthContext
├── pictures/                       # 书籍封面图片
├── bookstore-frontend/             # 前端 Vue 3 项目
│   ├── src/
│   │   ├── views/user/             # 用户端页面（20 个）
│   │   ├── views/admin/            # 管理后台页面（9 个：Dashboard/Books/Categories/Inventory/Orders/Refund/Users/Reviews/Admin）
│   │   ├── components/             # 公共组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态管理（6 个 store）
│   │   ├── api/                    # axios 封装（index.js 用户端、admin.js 管理端）
│   │   └── utils/                  # 工具函数
│   └── vite.config.js              # Vite 配置（@ 别名、代理规则）
└── sql/
    └── init.sql                    # 数据库初始化脚本（12 张表 + 种子数据）
```

## 数据库表

| 表名 | 说明 |
|------|------|
| `user` | 用户表（角色 user/admin） |
| `category` | 分类表（支持层级，parent_id） |
| `book` | 书籍表（含库存、价格、划线原价、封面、收藏数、作者 ID） |
| `author` | 作者表（bio/国籍/生卒年/奖项） |
| `book_chapter` | 目录表（章节 + 页码） |
| `favorite` | 收藏表（唯一约束 user_id+book_id，含收藏时价格） |
| `address` | 收货地址表 |
| `cart` | 购物车表 |
| `order` | 订单主表（含支付 ID、物流单号） |
| `order_item` | 订单明细表（冗余书籍信息） |
| `payment` | 支付记录表 |
| `review` | 评论表（评分 1-5） |

## 书籍详情页

详情页包含 **4 个标签页**：

| 标签 | 内容 |
|------|------|
| 简介 | 书籍 description 字段，多段落描述 |
| 作者 | 作者 bio/国籍/生卒年/奖项标签 |
| 目录 | 书籍章节列表（含页码点线装饰） |
| 评论 | 读者评分和评论列表 |

评分均值（`avgRating`）在查询时从 `review` 表实时计算，非数据库持久化字段。

## 技术方案

详见 [docs/analysis/01_需求分析.md](docs/analysis/01_需求分析.md)
