# 网上书店系统开发步骤

> 本文件记录开发进度，每完成一项打勾

---

## 项目信息

- 技术栈：Java 17 + Spring Boot 3.2.5 + MyBatis-Plus 3.5.6 + Vue 3 + MySQL 8.0
- 数据库：bookstore（8张表）
- 后端端口：8081
- 开发周期：2026/5/18 - 2026/5/30

---

## 开发阶段

### 阶段一：基础框架 ✅

- [x] Common 层
  - [x] BaseEntity.java（实体基类）
  - [x] Constants.java（常量定义）
  - [x] Result.java（统一响应）
  - [x] PageResult.java（分页结果）

- [x] Exception 层
  - [x] BusinessException.java（业务异常）
  - [x] GlobalExceptionHandler.java（全局异常处理）

- [x] Config 层
  - [x] CorsConfig.java（跨域配置）
  - [x] MybatisPlusConfig.java（分页配置）

- [x] Util 层
  - [x] SecurityUtils.java（密码加密）
  - [x] OrderNoGenerator.java（订单号生成）

- [x] 清理废弃文件 org/example/Main.java

---

### 阶段二：Entity 实体类 ✅

- [x] User.java（用户实体）
- [x] Category.java（分类实体）
- [x] Book.java（书籍实体）
- [x] Address.java（收货地址实体）
- [x] Cart.java（购物车实体）
- [x] Order.java（订单实体）
- [x] OrderItem.java（订单明细实体）
- [x] Review.java（评论实体）

---

### 阶段三：Mapper 数据访问层 ✅

- [x] UserMapper.java
- [x] CategoryMapper.java
- [x] BookMapper.java
- [x] AddressMapper.java
- [x] CartMapper.java
- [x] OrderMapper.java
- [x] OrderItemMapper.java
- [x] ReviewMapper.java

---

### 阶段四：DTO / VO 数据传输对象 ✅

#### DTO（请求对象）

- [x] LoginDTO.java
- [x] RegisterDTO.java
- [x] PasswordUpdateDTO.java
- [x] BookQueryDTO.java
- [x] CartAddDTO.java
- [x] CartUpdateDTO.java
- [x] OrderCreateDTO.java
- [x] OrderQueryDTO.java

#### VO（响应对象）

- [x] UserVO.java
- [x] BookVO.java
- [x] BookDetailVO.java
- [x] CartVO.java
- [x] OrderVO.java
- [x] OrderItemVO.java
- [x] ReviewVO.java
- [x] CategoryVO.java

---

### 阶段五：Service 业务层 ✅

#### Service 接口 + 实现类

- [x] UserService + UserServiceImpl
  - [x] 注册
  - [x] 登录
  - [x] 获取当前用户信息
  - [x] 修改密码
  - [x] 修改个人信息

- [x] BookService + BookServiceImpl
  - [x] 分页查询书籍
  - [x] 按分类查询
  - [x] 搜索书籍
  - [x] 获取书籍详情

- [x] CategoryService + CategoryServiceImpl
  - [x] 获取全部分类
  - [x] 按父级分类查询
  - [x] 分类增删改查

- [x] CartService + CartServiceImpl
  - [x] 加入购物车
  - [x] 修改数量
  - [x] 删除购物车商品
  - [x] 获取购物车列表
  - [x] 清空购物车

- [x] OrderService + OrderServiceImpl
  - [x] 创建订单
  - [x] 支付订单
  - [x] 取消订单
  - [x] 确认收货
  - [x] 获取订单列表
  - [x] 获取订单详情

- [x] ReviewService + ReviewServiceImpl
  - [x] 发表评论
  - [x] 获取书籍评论
  - [x] 删除评论

---

### 阶段六：Controller 控制层 ✅

#### 业务 Controller

- [x] UserController.java
  - POST /api/user/register
  - POST /api/user/login
  - GET /api/user/info
  - PUT /api/user/password
  - PUT /api/user/profile

- [x] BookController.java
  - GET /api/book/list
  - GET /api/book/{id}
  - GET /api/book/search
  - GET /api/book/category/{categoryId}

- [x] CategoryController.java
  - GET /api/category/list
  - GET /api/category/tree

- [x] CartController.java
  - GET /api/cart/list
  - POST /api/cart/add
  - PUT /api/cart/update
  - DELETE /api/cart/{id}
  - DELETE /api/cart/clear

- [x] OrderController.java
  - POST /api/order/create
  - GET /api/order/list
  - GET /api/order/{id}
  - PUT /api/order/{id}/pay
  - PUT /api/order/{id}/cancel
  - PUT /api/order/{id}/confirm

- [x] ReviewController.java
  - POST /api/review/add
  - GET /api/review/book/{bookId}
  - DELETE /api/review/{id}

#### 管理 Controller（admin/）

- [x] admin/BookManageController.java
  - POST /admin/book/add
  - PUT /admin/book/update
  - DELETE /admin/book/{id}
  - PUT /admin/book/{id}/status

- [x] admin/CategoryManageController.java
  - POST /admin/category/add
  - PUT /admin/category/update
  - DELETE /admin/category/{id}

- [x] admin/OrderManageController.java
  - GET /admin/order/list
  - PUT /admin/order/{id}/ship
  - PUT /admin/order/{id}/deliver

- [x] admin/UserManageController.java
  - GET /admin/user/list
  - PUT /admin/user/{id}/disable
  - PUT /admin/user/{id}/enable

- [x] admin/ReviewManageController.java
  - GET /admin/review/list
  - DELETE /admin/review/{id}
  - PUT /admin/review/{id}/hide

---

### 阶段七：前端开发（Vue 3）

待定（前端独立项目）

---

## 提交记录

| 日期 | Commit | 内容 |
|------|--------|------|
| 2026/5/18 | 5fa3e83 | feat: 完成基础框架搭建 |
| 2026/5/18 | 8586ded | feat: 添加8个Entity实体类 |
| 2026/5/18 | a8ec09c | feat: 添加8个Mapper接口 |
| 2026/5/18 | dde801c | feat: 添加DTO和VO数据传输对象 |
| 2026/5/18 | da4af82 | feat: 完成Service业务层（6个Service接口+实现类） |
| 2026/5/18 | f621743 | feat: 完成Controller控制层（11个Controller） |
| 2026/5/18 | a753142 | fix: 移除BookServiceImpl中未使用的BaseMapper导入 |
