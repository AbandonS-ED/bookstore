# 网上书店系统开发步骤

> 本文件记录开发进度，每完成一项打勾

---

## 项目信息

- 技术栈：Java 17 + Spring Boot 3.2.5 + MyBatis-Plus 3.5.6 + Vue 3 + MySQL 8.0
- 数据库：bookstore（16张表）
- 后端端口：8081
- 开发周期：2026/5/18 - 2026/5/30

---

## 开发阶段

### 阶段一：基础框架 ✅

- [x] Common 层
  - [x] BaseEntity.java（实体基类，id雪花算法+JsonFormat字符串化）
  - [x] Constants.java（常量定义）
  - [x] Result.java（统一响应）
  - [x] PageResult.java（分页结果）

- [x] Exception 层
  - [x] BusinessException.java（业务异常）
  - [x] GlobalExceptionHandler.java（全局异常处理）

- [x] Config 层
  - [x] CorsConfig.java（跨域配置）
  - [x] MybatisPlusConfig.java（分页配置）
  - [x] WebMvcConfig.java（拦截器注册+静态资源映射）

- [x] Interceptor 层
  - [x] AuthInterceptor.java（JWT认证）
  - [x] AdminInterceptor.java（管理员校验）

- [x] Util 层
  - [x] SecurityUtils.java（BCrypt密码加密）
  - [x] JwtUtils.java（JJWT生成/解析）
  - [x] AuthContext.java（ThreadLocal认证上下文）
  - [x] OrderNoGenerator.java（订单号生成）

- [x] 清理废弃文件 org/example/Main.java

---

### 阶段二：Entity 实体类 ✅

- [x] BaseEntity.java（抽象基类）
- [x] User.java（用户实体）
- [x] Category.java（分类实体）
- [x] Book.java（书籍实体，含favoritedCount/authorId/origPrice）
- [x] Author.java（作者实体，bio/国籍/生卒年/奖项）
- [x] BookChapter.java（目录实体，不继承BaseEntity）
- [x] Address.java（收货地址实体）
- [x] Cart.java（购物车实体）
- [x] Order.java（订单实体）
- [x] OrderItem.java（订单明细实体，@TableId AUTO）
- [x] Review.java（评论实体）
- [x] Favorite.java（收藏实体）
- [x] Payment.java（支付记录实体）

---

### 阶段三：Mapper 数据访问层 ✅

- [x] UserMapper.java
- [x] CategoryMapper.java
- [x] BookMapper.java（含原子SQL：decreaseStock/increaseSales/increaseFavoritedCount/decreaseFavoritedCount）
- [x] AuthorMapper.java
- [x] BookChapterMapper.java
- [x] AddressMapper.java
- [x] CartMapper.java
- [x] OrderMapper.java
- [x] OrderItemMapper.java
- [x] ReviewMapper.java
- [x] FavoriteMapper.java
- [x] PaymentMapper.java

---

### 阶段四：DTO / VO 数据传输对象 ✅

#### DTO（请求对象，15个）

- [x] LoginDTO.java
- [x] RegisterDTO.java
- [x] PasswordUpdateDTO.java
- [x] ProfileUpdateDTO.java
- [x] BookQueryDTO.java
- [x] CartAddDTO.java（bookId为String）
- [x] CartUpdateDTO.java（id为String）
- [x] OrderCreateDTO.java（addressId/cartItemIds为String）
- [x] OrderQueryDTO.java
- [x] PayApplyDTO.java
- [x] PayCallbackDTO.java
- [x] ShipDTO.java
- [x] AddressAddDTO.java
- [x] AddressUpdateDTO.java（id为String）
- [x] ReviewAddDTO.java

#### VO（响应对象，13个，ID均@JsonSerialize ToStringSerializer）

- [x] UserVO.java
- [x] BookVO.java
- [x] BookDetailVO.java
- [x] AuthorVO.java
- [x] BookChapterVO.java
- [x] CartVO.java
- [x] FavoriteVO.java
- [x] OrderVO.java
- [x] OrderItemVO.java
- [x] PaymentVO.java（amount为String手动转换）
- [x] AddressVO.java
- [x] ReviewVO.java
- [x] CategoryVO.java

---

### 阶段五：Service 业务层 ✅

#### Service 接口 + 实现类（9个）

- [x] UserService + UserServiceImpl
  - [x] 注册
  - [x] 登录（返回JWT）
  - [x] 获取当前用户信息
  - [x] 修改密码
  - [x] 修改个人信息

- [x] BookService + BookServiceImpl
  - [x] 分页查询书籍（标签过滤/关键字搜索/排序）
  - [x] 按分类查询（价格范围/评分/时间筛选）
  - [x] 搜索书籍
  - [x] 获取书籍详情（含作者/章节/评分/收藏状态）
  - [x] 排行榜（sales/rating/new/collection + period）
  - [x] 即将上架

- [x] CategoryService + CategoryServiceImpl
  - [x] 获取全部分类（含书籍计数）
  - [x] 按父级分类查询
  - [x] 分类增删改查

- [x] CartService + CartServiceImpl
  - [x] 加入购物车
  - [x] 修改数量
  - [x] 删除购物车商品
  - [x] 获取购物车列表
  - [x] 清空购物车

- [x] OrderService + OrderServiceImpl（@Transactional）
  - [x] 创建订单（扣库存 + 删购物车项）
  - [x] 支付订单（原子递增sales）
  - [x] 取消订单（回滚库存）
  - [x] 确认收货
  - [x] 申请退款（paid/shipped → refunding）
  - [x] 申请售后（delivered/completed → after_sale）
  - [x] 获取订单列表
  - [x] 获取订单详情

- [x] PaymentService + PaymentServiceImpl（@Transactional）
  - [x] 申请支付（创建支付记录，订单→paying）
  - [x] 支付回调（校验X-Callback-Secret，更新订单状态）

- [x] ReviewService + ReviewServiceImpl
  - [x] 发表评论
  - [x] 获取书籍评论
  - [x] 获取我的评论
  - [x] 删除评论（所有者或管理员）

- [x] AddressService + AddressServiceImpl
  - [x] 添加地址
  - [x] 更新地址
  - [x] 删除地址
  - [x] 设为默认
  - [x] 获取地址列表

- [x] FavoriteService + FavoriteServiceImpl（@Transactional）
  - [x] 添加收藏（原子递增favorited_count + 保存favorited_price）
  - [x] 取消收藏（原子递减favorited_count）
  - [x] 获取收藏列表（含降价提示）
  - [x] 检查是否已收藏
  - [x] 获取已收藏书籍ID集合（Set<String>）

---

### 阶段六：Controller 控制层 ✅

#### 业务 Controller（9个）

- [x] UserController.java
  - POST /api/user/register
  - POST /api/user/login
  - GET /api/user/info
  - PUT /api/user/password
  - PUT /api/user/profile

- [x] BookController.java
  - GET /api/book/list（分页）
  - GET /api/book/{id}（详情）
  - GET /api/book/search
  - GET /api/book/category/{categoryId}（分类浏览+筛选）
  - GET /api/book/ranking（排行榜）
  - GET /api/book/coming-soon

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
  - POST /api/order/{id}/pay-apply（申请支付）
  - PUT /api/order/{id}/pay（支付）
  - PUT /api/order/{id}/cancel
  - PUT /api/order/{id}/confirm
  - PUT /api/order/{id}/apply-refund
  - PUT /api/order/{id}/apply-after-sale

- [x] PaymentController.java
  - GET /api/payment/{orderId}
  - POST /api/payment/callback（X-Callback-Secret校验）

- [x] AddressController.java
  - GET /api/address/list
  - GET /api/address/{id}
  - POST /api/address/add
  - PUT /api/address/update
  - DELETE /api/address/{id}
  - PUT /api/address/{id}/default

- [x] ReviewController.java
  - POST /api/review/add
  - GET /api/review/book/{bookId}
  - GET /api/review/my
  - DELETE /api/review/{id}

- [x] FavoriteController.java
  - POST /api/favorite/add（Map<String, String>接收）
  - DELETE /api/favorite/{bookId}
  - GET /api/favorite/list
  - GET /api/favorite/check/{bookId}
  - GET /api/favorite/ids

#### 管理 Controller（5个，直接注入Mapper，返回Page<Entity>）

- [x] BookManageController.java
  - POST /admin/book/add
  - PUT /admin/book/update
  - DELETE /admin/book/{id}
  - PUT /admin/book/{id}/status
  - PUT /admin/book/{id}/stock（库存调整）
  - GET /admin/book/list

- [x] CategoryManageController.java（唯一走Service的管理端）
  - POST /admin/category/add
  - PUT /admin/category/update
  - DELETE /admin/category/{id}

- [x] OrderManageController.java
  - GET /admin/order/list
  - PUT /admin/order/{id}/ship
  - PUT /admin/order/{id}/deliver
  - PUT /admin/order/{id}/refund
  - PUT /admin/order/{id}/approve-refund（回滚库存）
  - PUT /admin/order/{id}/reject-refund
  - PUT /admin/order/{id}/approve-after-sale（回滚库存）
  - PUT /admin/order/{id}/reject-after-sale

- [x] UserManageController.java
  - GET /admin/user/list
  - PUT /admin/user/{id}/disable
  - PUT /admin/user/{id}/enable
  - PUT /admin/user/{id}/role
  - DELETE /admin/user/{id}

- [x] ReviewManageController.java
  - GET /admin/review/list（支持 keyword/status/rating 筛选）
  - DELETE /admin/review/{id}
  - PUT /admin/review/{id}/hide
  - PUT /admin/review/{id}/show

---

### 阶段七：前端开发（Vue 3）✅

- [x] 用户端页面开发（Home/Books/BookDetail/Cart/Order*/Login/Register/User/Settings/Addresses/Reviews/Ranking/NewArrivals/Favorites/CategoryBrowse/About/OrderSuccess/Explore）
- [x] 管理端页面开发（Admin/AdminDashboard/AdminBooks/AdminCategories/AdminInventory/AdminOrders/AdminRefund/AdminUsers/AdminReviews）
- [x] 公共组件（AppHeader/AppFooter/BookCard/PaginationBar/ModalDialog/ToastContainer）
- [x] Pinia状态管理（user/cart/category/order/favorite/review）
- [x] 前后端联调

### 阶段八：新增功能页面 ✅

- [x] 排行榜页面（Ranking.vue）— 畅销榜/评分榜/新书榜/收藏榜 + 时间维度筛选
- [x] 新品上架页面（NewArrivals.vue）— 新书展示
- [x] 收藏页面（Favorites.vue）— 降价提示、筛选器
- [x] 分类浏览页面（CategoryBrowse.vue）— 多条件筛选
- [x] 关于我们页面（About.vue）— 品牌故事
- [x] 探索好书页面（Explore.vue）— 命运之书翻牌 + 书的漂流瓶

### 阶段九：后端完善 ✅

- [x] favortied_count 原子SQL更新（增加/删除收藏时同步book表）
- [x] book.sales 支付时原子递增（OrderServiceImpl.pay）
- [x] 售后申请支持 delivered + completed 两种状态
- [x] 管理员订单管理增加approve-refund/reject-refund/approve-after-sale/reject-after-sale
- [x] 管理员用户管理增加role/delete
- [x] BookMapper.updateStock 原子设置库存
- [x] 管理员评论管理增加 show（显示）操作
- [x] 管理员库存管理页面 AdminInventory.vue + AdminRefund.vue 退款售后页面
- [x] 管理员仪表盘 AdminDashboard.vue
- [x] 种子数据增加author/book_chapter表，review从12→30条

### 阶段十：社区功能 ✅

- [x] CommunityPost 实体（继承 BaseEntity）
- [x] CommunityLike 实体
- [x] CommunityPostMapper / CommunityLikeMapper
- [x] CommunityPostService + CommunityPostServiceImpl
- [x] CommunityPostController（/api/community/**）
- [x] CommunityManageController（/admin/community/**）
- [x] community_post / community_like 数据库表
- [x] 前端 Explore.vue 社区帖子展示
- [x] 前端 AdminCommunity.vue 管理后台
- [x] 前端 api/community.js

### 阶段十一：AI 智能助手 ✅

- [x] AIController（/api/ai/chat + /api/ai/chat/stream）
- [x] MiniMax API 集成（MiniMax-M2.7 + Function Calling）
- [x] 5 个工具：searchBooks / getBookDetail / addToCart / getCartItems / listAddresses
- [x] SSE 流式输出
- [x] 前端 AIAssistant.vue 聊天界面
- [x] 前端 api/ai.js（chat + chatStream）
- [x] application.yml 配置 minimax.api.key/url

### 阶段十二：安全与修复 ✅

- [x] 密钥从 application.yml 移至 application-local.yml（gitignore）
- [x] application.yml 使用环境变量占位符 \${DB_PASSWORD}、\${JWT_SECRET}、\${MINIMAX_API_KEY}
- [x] AI API URL 修正为 chatcompletion_v2（OpenAI 兼容格式）
- [x] 社区帖子 image_url 改为 MEDIUMTEXT（支持 base64 图片）
- [x] 请求体限制增大至 10MB
- [x] 社区发帖/点赞接口加入认证拦截器
- [x] 管理后台社区编辑功能（PUT /admin/community/update）
- [x] 管理后台社区搜索功能（communityApi.list 支持 keyword）
- [x] 导航栏图标统一为 SVG 简笔画风格
- [x] 收藏 API 401 错误静默处理

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
| 2026/5/19 | 901a185 | feat: 完善前端功能并修复多项问题 |
| 2026/5/19 | ddf5dd2 | feat: 推送更新至远程仓库 |
| 2026/5/20 | 0561e0b | fix: add @JsonSerialize to all VO id fields for BigInt precision |
| 2026/5/20 | 1e861dd | fix: change DTO id types to String for BigInt precision; fix order confirm sends cartItemIds |
| 2026/5/20 | c3636be | fix: 添加 CartUpdateDTO 缺失的 NotBlank 导入 |
| 2026/5/20 | ba81179 | fix: comprehensive BigInt precision fix for all ID fields in VOs, DTOs, entities |
| 2026/5/20 | 5ecd5b5 | fix: cart update sends wrong field bookId instead of id |
