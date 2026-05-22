# 网上书店前端开发计划书

## 一、项目概述

### 1.1 项目信息
- **项目名称**：bookstore-frontend
- **技术栈**：Vue 3 + Vite + Pinia + Vue Router
- **UI框架**：原生 CSS + CSS Variables（自定义设计）
- **HTTP客户端**：Axios
- **后端接口**：http://localhost:8081

### 1.2 设计美学方向

**「书斋雅韵」— 东方文人书房美学**

灵感来源于中国传统书房：宣纸质感的背景、墨色的文字、朱红的点缀、竹简的纹理。整体氛围宁静、雅致、有书卷气。

- **配色**：
  - 主色：墨色 `#1a1a1a`（文字、标题）
  - 背景：宣纸白 `#f8f6f1`（主背景）、云白 `#fefefe`（卡片）
  - 点缀：朱砂红 `#c94043`（按钮、重要标记）
  - 辅助：竹青 `#7a9e7e`（成功状态）、古铜 `#b08d57`（价格、强调）
  - 边框：烟灰 `#e5e3df`（分割线、边框）

- **字体**：
  - 标题：`"Noto Serif SC"`（思源宋体）— 衬线体，文人风骨
  - 正文：`"Noto Sans SC"`（思源黑体）— 清晰易读
  - 英文/数字：`"Crimson Pro"` — 优雅衬线体

- **动效**：
  - 页面切换：淡入淡出 + 轻微上移（opacity 0→1, translateY 20px→0, 400ms ease-out）
  - 卡片悬停：微妙上浮 + 阴影加深（translateY -4px, 200ms）
  - 按钮点击：轻微缩放（scale 0.97, 100ms）
  - 图片加载：骨架屏占位

---

## 二、页面结构

### 2.1 用户端页面

| 页面 | 路由 | 描述 |
|------|------|------|
| 首页 | `/` | 轮播图、分类导航、畅销书籍、新书上架 |
| 登录 | `/login` | 用户登录 |
| 注册 | `/register` | 用户注册 |
| 书籍列表 | `/books` | 分类筛选、搜索、分页 |
| 书籍详情 | `/book/:id` | 书籍信息、评分、评论、加入购物车 |
| 排行榜 | `/ranking` | 畅销榜/评分榜/新书榜，支持时间维度筛选 |
| 新品上架 | `/new-arrivals` | 新书展示 |
| 分类浏览 | `/category-browse` | 多条件筛选（分类/价格/评分） |
| 关于我们 | `/about` | 团队介绍、品牌故事 |
| 我的收藏 | `/favorites` | 收藏书籍列表，含降价提示 |
| 购物车 | `/cart` | 购物车商品列表、数量修改、结算 |
| 订单确认 | `/order/confirm` | 确认订单、选择地址 |
| 订单列表 | `/orders` | 用户订单列表 |
| 订单详情 | `/order/:id` | 订单详细信息 |
| 用户中心 | `/user` | 个人信息、收货地址、修改密码 |

### 2.2 管理端页面

| 页面 | 路由 | 描述 |
|------|------|------|
| 管理首页 | `/admin` | 数据概览 |
| 书籍管理 | `/admin/books` | 书籍增删改查 |
| 分类管理 | `/admin/categories` | 分类增删改查 |
| 订单管理 | `/admin/orders` | 订单列表、发货管理 |
| 用户管理 | `/admin/users` | 用户列表、禁用/启用 |
| 评论管理 | `/admin/reviews` | 评论审核、隐藏 |

---

## 三、后端接口对接

### 3.1 认证接口

| 接口 | 方法 | 请求体 | 响应 |
|------|------|--------|------|
| `/api/user/register` | POST | `{username, password, email, phone}` | `{code, message, data: null}` |
| `/api/user/login` | POST | `{username, password}` | `{code, message, data: {token, username}}` |
| `/api/user/info` | GET | - | `{code, message, data: UserVO}` |
| `/api/user/password` | PUT | `{oldPassword, newPassword}` | `{code, message, data: null}` |
| `/api/user/profile` | PUT | `{email, phone}` | `{code, message, data: null}` |

### 3.2 书籍接口

| 接口 | 方法 | 参数 | 响应 |
|------|------|------|------|
| `/api/book/list` | GET | `keyword, categoryId, pageNum, pageSize` | `{code, message, data: {records, total, pages}}` |
| `/api/book/{id}` | GET | - | `{code, message, data: BookDetailVO}` |
| `/api/book/search` | GET | `keyword, pageNum, pageSize` | `{code, message, data: {records, total, pages}}` |
| `/api/book/category/{categoryId}` | GET | `categoryId, pageNum, pageSize` | `{code, message, data: {records, total, pages}}` |

### 3.3 分类接口

| 接口 | 方法 | 参数 | 响应 |
|------|------|------|------|
| `/api/category/list` | GET | - | `{code, message, data: CategoryVO[]}` |
| `/api/category/tree` | GET | `parentId` | `{code, message, data: CategoryVO[]}` |

### 3.4 购物车接口

| 接口 | 方法 | 请求体/参数 | 响应 |
|------|------|------------|------|
| `/api/cart/list` | GET | - | `{code, message, data: CartVO[]}` |
| `/api/cart/add` | POST | `{bookId, quantity}` | `{code, message, data: null}` |
| `/api/cart/update` | PUT | `{bookId, quantity}` | `{code, message, data: null}` |
| `/api/cart/{id}` | DELETE | - | `{code, message, data: null}` |
| `/api/cart/clear` | DELETE | - | `{code, message, data: null}` |

### 3.5 订单接口

| 接口 | 方法 | 请求体/参数 | 响应 |
|------|------|------------|------|
| `/api/order/create` | POST | `{addressId, cartItemIds, remark}` | `{code, message, data: OrderVO}` |
| `/api/order/list` | GET | `status` | `{code, message, data: OrderVO[]}` |
| `/api/order/{id}` | GET | - | `{code, message, data: OrderVO}` |
| `/api/order/{id}/pay` | PUT | - | `{code, message, data: null}` |
| `/api/order/{id}/cancel` | PUT | - | `{code, message, data: null}` |
| `/api/order/{id}/confirm` | PUT | - | `{code, message, data: null}` |

### 3.6 评论接口

| 接口 | 方法 | 参数 | 响应 |
|------|------|------|------|
| `/api/review/add` | POST | `{bookId, rating, content}` | `{code, message, data: null}` |
| `/api/review/book/{bookId}` | GET | - | `{code, message, data: ReviewVO[]}` |
| `/api/review/{id}` | DELETE | - | `{code, message, data: null}` |

### 3.7 收藏接口

| 接口 | 方法 | 参数 | 响应 |
|------|------|------|------|
| `/api/favorite/add` | POST | `{bookId}` | `{code, message, data: null}` |
| `/api/favorite/{bookId}` | DELETE | - | `{code, message, data: null}` |
| `/api/favorite/list` | GET | - | `{code, message, data: FavoriteVO[]}` |
| `/api/favorite/check/{bookId}` | GET | - | `{code, message, data: Boolean}` |
| `/api/favorite/ids` | GET | - | `{code, message, data: Set<Long>}` |

### 3.8 排行榜接口

| 接口 | 方法 | 参数 | 响应 |
|------|------|------|------|
| `/api/book/ranking` | GET | `type: sales/rating/new, period: all/week/month/quarter/year` | `{code, message, data: BookVO[]}` |

### 3.8 管理接口

| 接口 | 方法 | 参数 | 响应 |
|------|------|------|------|
| `/admin/book/add` | POST | Book实体 | `{code, message, data: null}` |
| `/admin/book/update` | PUT | Book实体 | `{code, message, data: null}` |
| `/admin/book/{id}` | DELETE | - | `{code, message, data: null}` |
| `/admin/book/{id}/status` | PUT | `status` | `{code, message, data: null}` |
| `/admin/category/add` | POST | Category实体 | `{code, message, data: null}` |
| `/admin/category/update` | PUT | Category实体 | `{code, message, data: null}` |
| `/admin/category/{id}` | DELETE | - | `{code, message, data: null}` |
| `/admin/order/list` | GET | - | 订单列表 |
| `/admin/order/{id}/ship` | PUT | - | `{code, message, data: null}` |
| `/admin/order/{id}/deliver` | PUT | - | `{code, message, data: null}` |
| `/admin/user/list` | GET | - | 用户列表 |
| `/admin/user/{id}/disable` | PUT | - | `{code, message, data: null}` |
| `/admin/user/{id}/enable` | PUT | - | `{code, message, data: null}` |
| `/admin/review/list` | GET | - | 评论列表 |
| `/admin/review/{id}` | DELETE | - | `{code, message, data: null}` |
| `/admin/review/{id}/hide` | PUT | - | `{code, message, data: null}` |

---

## 四、组件清单

### 4.1 通用组件

| 组件 | 描述 |
|------|------|
| `AppHeader` | 顶部导航栏（Logo、搜索框、用户菜单、购物车图标） |
| `AppFooter` | 页脚（版权信息、链接） |
| `BaseButton` | 基础按钮（primary、secondary、text 三种样式） |
| `BaseInput` | 输入框（支持前缀图标、错误提示） |
| `BaseSelect` | 选择器 |
| `BaseDialog` | 弹窗对话框 |
| `BasePagination` | 分页组件 |
| `BaseLoading` | 加载状态（骨架屏） |
| `BaseEmpty` | 空状态占位 |

### 4.2 业务组件

| 组件 | 描述 |
|------|------|
| `BookCard` | 书籍卡片（封面、标题、作者、价格、悬停效果） |
| `BookItem` | 书籍列表项（用于购物车、订单等） |
| `CartItem` | 购物车商品项（带数量加减、删除） |
| `OrderItem` | 订单项（订单信息展示） |
| `ReviewItem` | 评论项（用户头像、评分、内容） |
| `CategoryNav` | 分类导航（侧边栏或顶部标签） |
| `AddressCard` | 收货地址卡片 |
| `StarRating` | 星级评分组件 |

---

## 五、页面详细设计

### 5.1 首页 `/`

**布局**：
- Hero 区域：大图轮播（3-4本书籍推荐），渐变遮罩，标题文案
- 分类导航：横向滚动标签栏
- 畅销书籍：4列网格，书籍卡片
- 新书上架：横向滑动展示
- 页脚

### 5.2 登录/注册 `/login` `/register`

**布局**：
- 左侧：品牌展示区（书店名称、标语、装饰图形）
- 右侧：表单区（登录/注册表单）
- 表单项：用户名、密码、确认密码、邮箱、手机号
- 表单验证、错误提示
- 登录/注册切换链接

### 5.3 书籍列表 `/books`

**布局**：
- 左侧边栏：分类筛选（树形结构）
- 右侧：搜索结果区
- 顶部：搜索框、排序选项（价格、销量、新品）
- 内容：书籍网格（4列），分页
- 无限滚动可选

### 5.4 书籍详情 `/book/:id`

**布局**：
- 左侧：书籍封面大图
- 右侧：书籍信息（标题、作者、出版社、价格、评分、库存）
- 操作区：数量选择、加入购物车、立即购买
- 下方：Tab 页（详情描述、评论）
- 评论区：评分统计、评论列表、发表评论

### 5.5 购物车 `/cart`

**布局**：
- 购物车商品列表（全选、单个商品、数量修改、删除）
- 价格明细（商品总价、运费、总计）
- 结算按钮
- 空购物车状态

### 5.6 订单确认 `/order/confirm`

**布局**：
- 收货地址选择/新增
- 商品清单（只读）
- 订单备注
- 价格明细
- 提交订单按钮

### 5.7 订单列表 `/orders`

**布局**：
- 状态标签筛选（全部、待付款、待发货、待收货、已完成）
- 订单卡片列表
- 每个订单显示：订单号、时间、状态、商品概要、金额

### 5.8 用户中心 `/user`

**布局**：
- 侧边导航：个人信息、收货地址、修改密码、我的订单
- 右侧内容区：根据导航显示对应内容

### 5.9 管理后台 `/admin`

**布局**：
- 左侧菜单（仪表盘、书籍管理、分类管理、订单管理、用户管理、评论管理）
- 右侧内容区（数据表格、增删改查表单）

---

## 六、状态管理（Pinia Store）

| Store | 描述 |
|-------|------|
| `useUserStore` | 用户信息、登录状态、Token |
| `useCartStore` | 购物车列表、数量、总价 |
| `useCategoryStore` | 分类树、当前选中分类 |
| `useOrderStore` | 订单列表、当前订单 |
| `useFavoriteStore` | 收藏列表、已收藏书籍ID集合 |

---

## 七、项目结构

```
bookstore-frontend/
├── index.html
├── vite.config.js                  # Vite配置（代理/api、/admin、/pictures到8081）
├── package.json
├── public/
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── assets/
│   │   ├── styles/
│   │   │   ├── variables.css      # CSS变量（颜色、字体）
│   │   │   ├── reset.css          # 样式重置
│   │   │   └── global.css         # 全局样式
│   │   └── images/
│   ├── components/
│   │   ├── common/                # 通用组件
│   │   └── business/             # 业务组件
│   ├── views/
│   │   ├── user/                 # 用户端页面
│   │   └── admin/                # 管理端页面
│   ├── router/
│   │   └── index.js
│   ├── stores/                   # Pinia stores
│   ├── api/
│   │   ├── index.js              # Axios 实例
│   │   ├── user.js
│   │   ├── book.js
│   │   ├── cart.js
│   │   ├── order.js
│   │   └── admin.js
│   ├── utils/
│   │   ├── auth.js               # Token 管理
│   │   └── format.js            # 格式化工具
│   └── hooks/                    # 组合式函数
```

---

## 八、开发计划

### 第一周：项目搭建与基础页面
- [ ] 初始化 Vue 3 + Vite 项目
- [ ] 配置 Vite、开发服务器代理
- [ ] 安装依赖（Vue Router、Pinia、Axios）
- [ ] 编写 CSS 变量、全局样式
- [ ] 封装 Axios 请求（拦截器、错误处理）
- [ ] 完成登录/注册页面
- [ ] 完成 AppHeader 组件
- [ ] 完成首页

### 第二周：核心功能页面
- [ ] 完成书籍列表页
- [ ] 完成书籍详情页
- [ ] 完成购物车页面
- [ ] 完成订单确认页
- [ ] 完成用户中心页面

### 第三周：管理后台与完善
- [ ] 完成管理后台布局
- [ ] 完成书籍管理 CRUD
- [ ] 完成分类管理 CRUD
- [ ] 完成订单管理
- [ ] 完成用户管理与评论管理
- [ ] 响应式适配
- [ ] 动画与交互优化

---

## 九、技术要点

### 9.1 认证流程
1. 登录成功获取 Token，存储到 localStorage
2. Axios 请求拦截器自动附加 `Authorization: Bearer {token}`
3. Token 过期或无效跳转到登录页

### 9.2 数据预加载
- 首页同时请求：分类列表 + 畅销书籍 + 新书
- 书籍列表：进入时请求分类树 + 第一页书籍

### 9.3 状态同步
- 购物车：Pinia Store 管理，刷新后重新从后端拉取
- 用户信息：进入页面时获取，并缓存到 Store

### 9.4 路由守卫
- 需要登录的页面（购物车、订单、用户中心）检查 Token
- 管理页面检查用户角色是否为 admin

---

## 十、验收标准

1. 所有用户端页面可正常访问和交互
2. 所有管理端页面可正常访问和操作
3. 登录/注册流程完整，Token 正确处理
4. 购物车增删改查、订单创建完整流程跑通
5. 页面动画流畅，无明显卡顿
6. 响应式布局，适配 1024px+ 屏幕
7. 无控制台报错