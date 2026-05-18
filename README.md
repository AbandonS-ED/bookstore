# 网上书店系统

基于 Spring Boot 3 + MyBatis-Plus 的网上书店系统，支持用户浏览、购物车、订单管理等核心功能。

## 技术栈

| 分类 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 3.2.5, MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0, Druid 1.2.23 |
| 前端 | Layui 2.8.x, JSP |
| 构建 | Maven |

## 功能模块

- **用户模块** - 注册、登录、个人中心、收货地址管理
- **书籍模块** - 书籍浏览、搜索、详情、分类查看
- **购物车模块** - 加入购物车、修改数量、删除
- **订单模块** - 创建订单、订单列表、订单详情
- **评论模块** - 书籍评论、评分
- **后台管理** - 书籍管理、分类管理、订单管理、用户管理、评论管理

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 数据库初始化

```bash
mysql -u root -p < sql/init.sql
```

### 运行项目

```bash
mvn spring-boot:run
```

访问 http://localhost:8080

**管理员账号：** admin / 123456

## 项目结构

```
src/main/java/com/example/bookstore/
├── config/          # 配置类
├── controller/     # 控制器
├── service/        # 业务层
├── mapper/         # 数据访问层
├── entity/        # 实体类
├── dto/           # 数据传输对象
├── vo/            # 视图对象
├── common/        # 通用类
├── exception/     # 异常处理
└── util/          # 工具类
```

## 数据库表

- `user` - 用户表
- `category` - 分类表
- `book` - 书籍表
- `address` - 收货地址表
- `cart` - 购物车表
- `order` - 订单表
- `order_item` - 订单明细表
- `review` - 评论表

## 技术方案

详见 [docs/analysis/03_技术方案.md](docs/analysis/03_技术方案.md)
