```markdown
# 拾光书屋 — 完整前端设计规范与实现指南

> 本文档是「拾光书屋」网上书店前端系统的完整设计规范，可直接交给 AI 编程工具（如 Claude Code）作为实现依据。
> 
> 文档覆盖：设计系统、全部页面规格、组件规范、交互逻辑、数据结构建议、实现提示词。

---

## 目录

- [一、项目概述](#一项目概述)
- [二、技术栈建议](#二技术栈建议)
- [三、设计系统](#三设计系统)
- [四、全局组件](#四全局组件)
- [五、页面规格：首页](#五页面规格首页)
- [六、页面规格：全部书籍](#六页面规格全部书籍)
- [七、页面规格：分类浏览](#七页面规格分类浏览)
- [八、页面规格：排行榜](#八页面规格排行榜)
- [九、页面规格：新书上架](#九页面规格新书上架)
- [十、页面规格：关于我们](#十页面规格关于我们)
- [十一、页面规格：个人中心](#十一页面规格个人中心)
- [十二、页面规格：管理后台](#十二页面规格管理后台)
- [十三、API 接口设计建议](#十三api-接口设计建议)
- [十四、实现提示词（给 Claude Code）](#十四实现提示词给-claude-code)

---

## 一、项目概述

### 1.1 项目名称

**拾光书屋（ShiGuang Books）** — 一个面向中文读者的在线书店系统

### 1.2 设计定位

**「走进一家深夜还亮着暖光的老派独立书店」**

视觉语言：经典木质书店美学 — 深色胡桃木书架、泛黄纸张的温度、黄铜台灯的光晕。不是现代极简科技风，不是可爱卡通风，而是有温度、有厚度、有书卷气的传统书店质感。

### 1.3 设计原则

| 原则 | 说明 | 反面案例 |
|------|------|----------|
| 温暖而非刺眼 | 所有强调色使用古铜金 `#C09A4B`，而非亮黄 `#FFB300` | 电商大促风格的荧光黄 |
| 厚重而非沉重 | 大面积羊皮纸底色，深色区域多级递进 | 纯黑背景 + 白字 |
| 书卷气而非老气 | 衬线字体用于标题，无衬线用于正文 | 全局系统默认字体 |
| 克制而非匮乏 | 金色只用在最精准的 5% 位置 | 金色大面积铺满 |
| 留白而非空洞 | 书卡间 24px 间距，区域间 80px 呼吸空间 | 所有内容挤在一起 |

### 1.4 页面结构总览

```
前台页面（用户端）
├── 首页                 /
├── 全部书籍             /books
├── 分类浏览             /categories
├── 排行榜               /ranking
├── 新书上架             /new-arrivals
├── 书籍详情             /book/:id
├── 关于我们             /about
├── 购物车               /cart
├── 结账                 /checkout
├── 个人中心             /user
│   ├── 概览             /user?tab=overview
│   ├── 我的订单         /user?tab=orders
│   ├── 我的书架         /user?tab=bookshelf
│   ├── 阅读足迹         /user?tab=footprint
│   ├── 评价管理         /user?tab=reviews
│   ├── 优惠券           /user?tab=coupons
│   ├── 收货地址         /user?tab=addresses
│   ├── 账号设置         /user?tab=settings
│   └── 消息中心         /user?tab=messages
├── 登录                 /login
└── 注册                 /register

后台页面（管理端）
├── 仪表盘               /admin
├── 书籍管理             /admin/books
├── 分类管理             /admin/categories
├── 库存管理             /admin/inventory
├── 订单管理             /admin/orders
├── 退款售后             /admin/refunds
├── 用户管理             /admin/users
├── 评价审核             /admin/reviews
├── 优惠券管理           /admin/coupons
└── 系统设置             /admin/settings
```

---

## 二、技术栈建议

### 2.1 推荐技术栈

| 层面 | 技术 | 说明 |
|------|------|------|
| **框架** | Vue 3 + Composition API | 或 React 18，二选一 |
| **路由** | Vue Router 4 | 前台 + 后台分两个路由模块 |
| **状态管理** | Pinia | 轻量，适合中型项目 |
| **UI 框架** | 不使用第三方 UI 库 | 全部自定义组件，保证风格统一 |
| **HTTP 请求** | Axios | 统一封装拦截器 |
| **样式方案** | CSS Variables + Scoped CSS | 用 CSS 变量管理整套色板 |
| **图标** | 不使用图标库，直接 Unicode/Emoji | 保持书卷气质，避免工业感图标 |
| **构建工具** | Vite | 快速开发 |
| **字体** | Noto Serif SC + Noto Sans SC + DM Mono | Google Fonts CDN |

### 2.2 项目目录结构建议

```
shiguang-books/
├── public/
│   └── favicon.ico
├── src/
│   ├── assets/
│   │   └── styles/
│   │       ├── variables.css        # CSS 变量（色板、间距、字号）
│   │       ├── base.css             # 重置样式 + 全局基础样式
│   │       ├── animations.css       # 全局动画 keyframes
│   │       └── typography.css       # 字体加载 + 排版规范
│   ├── components/
│   │   ├── common/                  # 通用组件
│   │   │   ├── AppHeader.vue        # 前台导航栏
│   │   │   ├── AppFooter.vue        # 前台页脚
│   │   │   ├── BookCard.vue         # 书籍卡片
│   │   │   ├── BookCardList.vue     # 书籍列表形态
│   │   │   ├── CategoryPill.vue     # 分类标签
│   │   │   ├── Pagination.vue       # 分页器
│   │   │   ├── RatingStars.vue      # 评分星星
│   │   │   ├── PriceTag.vue         # 价格标签
│   │   │   ├── StatusBadge.vue      # 状态徽章
│   │   │   ├── EmptyState.vue       # 空状态
│   │   │   ├── LoadingSpinner.vue   # 加载动画
│   │   │   ├── Toast.vue            # 提示消息
│   │   │   ├── Modal.vue            # 弹窗
│   │   │   └── ConfirmDialog.vue    # 二次确认
│   │   ├── home/                    # 首页专用组件
│   │   │   ├── HeroBanner.vue
│   │   │   ├── StatsBar.vue
│   │   │   ├── BookGrid.vue
│   │   │   ├── FeaturedBanner.vue
│   │   │   └── RankingPreview.vue
│   │   ├── user/                    # 个人中心组件
│   │   │   ├── UserSidebar.vue
│   │   │   ├── OverviewPanel.vue
│   │   │   ├── OrderCard.vue
│   │   │   ├── OrderDetail.vue
│   │   │   ├── BookshelfGrid.vue
│   │   │   ├── ReadingCalendar.vue
│   │   │   ├── AchievementCard.vue
│   │   │   ├── CouponCard.vue
│   │   │   ├── AddressCard.vue
│   │   │   └── ReviewEditor.vue
│   │   └── admin/                   # 管理后台组件
│   │       ├── AdminSidebar.vue
│   │       ├── AdminHeader.vue
│   │       ├── StatCard.vue
│   │       ├── DataTable.vue
│   │       ├── BarChart.vue
│   │       ├── DonutChart.vue
│   │       └── BookForm.vue
│   ├── views/
│   │   ├── front/                   # 前台页面
│   │   │   ├── HomePage.vue
│   │   │   ├── AllBooksPage.vue
│   │   │   ├── CategoryPage.vue
│   │   │   ├── RankingPage.vue
│   │   │   ├── NewArrivalsPage.vue
│   │   │   ├── AboutPage.vue
│   │   │   ├── BookDetailPage.vue
│   │   │   ├── CartPage.vue
│   │   │   ├── CheckoutPage.vue
│   │   │   ├── LoginPage.vue
│   │   │   ├── RegisterPage.vue
│   │   │   └── UserCenterPage.vue
│   │   └── admin/                   # 后台页面
│   │       ├── DashboardPage.vue
│   │       ├── BooksManagePage.vue
│   │       ├── CategoriesManagePage.vue
│   │       ├── InventoryPage.vue
│   │       ├── OrdersManagePage.vue
│   │       ├── RefundsPage.vue
│   │       ├── UsersManagePage.vue
│   │       ├── ReviewsAuditPage.vue
│   │       ├── CouponsManagePage.vue
│   │       └── SettingsPage.vue
│   ├── router/
│   │   ├── index.js
│   │   ├── frontRoutes.js           # 前台路由
│   │   └── adminRoutes.js           # 后台路由
│   ├── stores/                      # Pinia 状态管理
│   │   ├── auth.js                  # 认证状态
│   │   ├── cart.js                  # 购物车
│   │   ├── books.js                 # 书籍数据
│   │   ├── user.js                  # 用户信息
│   │   └── admin.js                 # 后台状态
│   ├── api/                         # API 请求封装
│   │   ├── request.js               # Axios 实例 + 拦截器
│   │   ├── books.js
│   │   ├── orders.js
│   │   ├── users.js
│   │   ├── auth.js
│   │   └── admin.js
│   ├── composables/                 # 组合式函数
│   │   ├── usePagination.js
│   │   ├── useSearch.js
│   │   ├── useToast.js
│   │   └── useModal.js
│   ├── utils/
│   │   ├── format.js                # 格式化工具（价格、日期）
│   │   ├── validate.js              # 表单验证
│   │   └── storage.js               # localStorage 封装
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
└── package.json
```

---

## 三、设计系统

### 3.1 色板（CSS Variables）

```css
:root {
  /* ── 主色：胡桃木色系 ── */
  --color-primary:       #4A3526;
  --color-primary-mid:   #5C4434;
  --color-primary-light: #7A6252;
  --color-primary-dark:  #2E1F15;
  --color-primary-abyss: #1C120C;

  /* ── 背景：羊皮纸色系 ── */
  --color-bg:        #EDE6D6;
  --color-bg-warm:   #E5DDD0;
  --color-bg-card:   #F5F0E8;
  --color-bg-cream:  #F8F4ED;

  /* ── 强调色：古铜金色系 ── */
  --color-accent:        #C09A4B;
  --color-accent-light:  #D4B06A;
  --color-accent-muted:  #A68638;
  --color-accent-glow:   rgba(192,154,75,0.12);

  /* ── 文字 ── */
  --color-text:            #2B1D14;
  --color-text-secondary:  #6B5B4E;
  --color-text-light:      #9B8B7E;

  /* ── 辅助 ── */
  --color-divider:         rgba(74,53,38,0.1);
  --color-divider-strong:  rgba(74,53,38,0.18);
  --color-shadow:          rgba(44,30,20,0.06);
  --color-shadow-heavy:    rgba(44,30,20,0.14);
  --color-shadow-deep:     rgba(30,18,10,0.25);

  /* ── 语义色 ── */
  --color-green:     #5C8856;
  --color-green-bg:  rgba(92,136,86,0.1);
  --color-red:       #A04040;
  --color-red-bg:    rgba(160,64,64,0.08);
  --color-blue:      #4A6E8A;
  --color-blue-bg:   rgba(74,110,138,0.08);

  /* ── 间距 ── */
  --space-xs:  4px;
  --space-sm:  8px;
  --space-md:  16px;
  --space-lg:  24px;
  --space-xl:  32px;
  --space-2xl: 48px;
  --space-3xl: 80px;

  /* ── 圆角 ── */
  --radius-sm:  6px;
  --radius-md:  10px;
  --radius-lg:  14px;
  --radius-xl:  20px;
  --radius-full: 50%;

  /* ── 过渡 ── */
  --transition-fast:   0.2s ease;
  --transition-normal: 0.3s ease;
  --transition-slow:   0.5s cubic-bezier(0.25,0.46,0.45,0.94);
}
```

### 3.2 字体系统

```css
/* Google Fonts 加载 */
@import url('https://fonts.googleapis.com/css2?family=Noto+Serif+SC:wght@400;600;700;900&family=Noto+Sans+SC:wght@300;400;500;700&family=DM+Mono:wght@400;500&display=swap');

/* 标题 */
h1, h2, h3, h4 {
  font-family: 'Noto Serif SC', serif;
  font-weight: 700;
  line-height: 1.3;
  color: var(--color-text);
}

/* 正文 */
body {
  font-family: 'Noto Sans SC', sans-serif;
  font-weight: 400;
  line-height: 1.7;
}

/* 等宽（订单号、ISBN、价格） */
.mono {
  font-family: 'DM Mono', monospace;
}
```

**字号规范：**

| 元素 | 字体 | 字重 | 字号 |
|------|------|------|------|
| 页面 H1 | Noto Serif SC | 900 | 2.4rem |
| 区域 H2 | Noto Serif SC | 700 | 1.7rem |
| 卡片 H3 | Noto Serif SC | 700 | 1.0rem |
| 书名 | Noto Serif SC | 600 | 0.95rem |
| 正文 | Noto Sans SC | 400 | 0.95rem |
| 辅助文字 | Noto Sans SC | 400 | 0.85rem |
| 角标/标签 | Noto Sans SC | 700 | 0.65rem |
| 价格 | Noto Serif SC | 700 | 1.1rem |
| 数据大字 | Noto Serif SC | 900 | 1.8rem |

### 3.3 金色使用规则

> **金色（`--color-accent`）全站占比不超过 5%。**

✅ 允许使用金色的位置：价格、徽章角标、评分星星、选中下划线、Hero 装饰光晕、导航栏底线

❌ 禁止使用金色的位置：大面积背景、正文文字、导航链接文字、卡片边框、主按钮外框

### 3.4 纸张肌理效果

所有页面叠加一层极淡的 noise 纹理，模拟纸张质感：

```css
body::after {
  content: '';
  position: fixed;
  inset: 0;
  /* 使用 SVG feTurbulence 生成 noise */
  background-image: url("data:image/svg+xml,...fractalNoise...");
  opacity: 0.025;
  pointer-events: none;
  z-index: 9999;
}
```

---

## 四、全局组件

### 4.1 导航栏（前台）

**结构：**
- 左侧：Logo（📖 图标 + 「拾光书屋」 + 英文副标 `SHIGUANG BOOKS`）
- 中间：导航链接（首页、全部书籍、分类浏览、排行榜、新书上架、关于我们）
- 右侧：搜索框 + 购物车图标（带数量角标）+ 用户头像（登录态）

**样式：**
- 背景：`--color-primary-abyss`
- 底部：1px 金色半透明边框 `rgba(192,154,75,0.15)`
- 高度：68px
- `position: sticky; top: 0`
- 导航链接默认 `rgba(237,230,214,0.5)`，hover 变亮，active 金色
- 选中链接底部有金色下划线动画（`width: 0 → 100%`）
- 搜索框聚焦时有金色光晕 `box-shadow: 0 0 0 3px var(--color-accent-glow)`

### 4.2 页脚

**结构：**
- 深色背景 `--color-primary-abyss`
- 四列：品牌简介、书籍分类、帮助中心、关于我们
- 底部版权信息

### 4.3 书籍卡片（BookCard）

**这是全站使用频率最高的组件，有两种形态。**

#### 网格形态（默认）

```
┌─────────────────┐
│                  │  ← 3:4 封面色块
│    [角标]    [♡] │  ← 左上角标 + 右上收藏
│                  │
├─────────────────┤
│ 书名（最多2行）   │  ← Serif 字体
│ 作者             │  ← 浅色辅助文字
│ ¥45.60  ★9.4    │  ← 价格金色 + 评分
└─────────────────┘
```

**Props：**

| Prop | 类型 | 说明 |
|------|------|------|
| `id` | Number | 书籍 ID |
| `title` | String | 书名 |
| `author` | String | 作者 |
| `cover` | String | 封面图 URL（可为空，用色块代替） |
| `coverColor` | String | 无封面时的渐变色方案编号 1~12 |
| `price` | Number | 售价 |
| `originalPrice` | Number | 原价（可选） |
| `rating` | Number | 评分 |
| `badge` | String | 角标文字（畅销/新书/经典/热卖，可选） |
| `isFavorited` | Boolean | 是否已收藏 |

**交互：**
- 悬停：`translateY(-5px)` + 阴影加深 + 封面 `scale(1.04)`
- 收藏按钮：hover 时才显示（opacity 0→1），点击切换 ♥/♡
- 进入视口：`fadeUp` 动画，每张卡片错开 30ms

#### 列表形态

左侧封面 120px + 右侧书名/作者/2行简介/价格评分

### 4.4 分页器（Pagination）

```
‹ 上一页  [1]  2   3   4   5  ...  186  下一页 ›
```

**Props：**

| Prop | 类型 | 说明 |
|------|------|------|
| `currentPage` | Number | 当前页 |
| `totalPages` | Number | 总页数 |
| `totalItems` | Number | 总条数 |

**Events：** `@page-change(page)`

### 4.5 Toast 提示

**Props：** `message: String, type: 'ok' | 'error', duration: Number(默认3000)`

- 位置：右上角
- 动画：从右侧滑入 → 自动消失
- 背景 `--color-primary-dark`

### 4.6 弹窗（Modal）

**Props：** `visible: Boolean, title: String, width: String(默认520px)`

- 居中，深色半透明遮罩 `rgba(28,18,12,0.55)`
- 卡片 `--color-bg-card`，圆角 14px
- 点击遮罩关闭
- 弹出时 `translateY(16px) → translateY(0)`

### 4.7 空状态（EmptyState）

```
┌────────────────────────────┐
│        (线描插画)            │
│                            │
│    描述文字                  │
│      [行动按钮]             │
└────────────────────────────┘
```

**Props：** `icon: String, title: String, description: String, actionText: String`

---

## 五、页面规格：首页

**路由：** `/`

### 5.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏                                  │
├─────────────────────────────────────────┤
│  Hero Banner                            │
│  - 深色木纹渐变背景                       │
│  - 左侧：标签 + H1 + 描述 + 两个按钮      │
│  - 右侧：三本浮动书籍（CSS 动画）          │
├─────────────────────────────────────────┤
│  数据统计栏（4列）                        │
│  128,000+ 图书 | 56,000+ 读者 | 98.6% 好评│
├─────────────────────────────────────────┤
│  热门推荐                                │
│  - 8 个分类标签（可切换）                  │
│  - 5 列书籍网格（10 本书）                │
├─────────────────────────────────────────┤
│  编辑推荐横幅                            │
│  - 深色背景                              │
│  - 左侧大封面 + 右侧详细推荐文案           │
├─────────────────────────────────────────┤
│  本周畅销榜（6 本，2列排行卡片）           │
├─────────────────────────────────────────┤
│  邮件订阅                                │
├─────────────────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 5.2 关键组件

| 组件 | 说明 |
|------|------|
| `HeroBanner.vue` | 全宽深色区，含浮动书本 CSS 动画 |
| `StatsBar.vue` | 4列统计数字，深色底 |
| `BookGrid.vue` | 接收书籍数组 + 分类标签，渲染网格 |
| `FeaturedBanner.vue` | 编辑推荐，全宽深色横幅 |
| `RankingPreview.vue` | 排行榜预览，2列 rank-card |

### 5.3 实现要点

- Hero 的三本书使用 `@keyframes float` 做缓慢上下浮动
- 书籍网格使用 `IntersectionObserver` 实现进入视口时触发 `fadeUp` 动画
- 分类标签切换时，书卡数组带过渡动画切换

---

## 六、页面规格：全部书籍

**路由：** `/books`

### 6.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏                                  │
├─────────────────────────────────────────┤
│  页面标题区（可选，浅色）                  │
│  全部书籍 / 共 128,000+ 本好书            │
├─────────────────────────────────────────┤
│  快速筛选标签栏                           │
│  [全部] [今日特价] [限时折扣] [包邮] ...   │
├─────────────────────────────────────────┤
│  工具栏                                  │
│  结果数 | 筛选标签 | 排序 ▾ | 视图切换 ▦☰ │
├─────────────────────────────────────────┤
│  书籍网格（5列）/ 列表                    │
├─────────────────────────────────────────┤
│  分页器 / 无限滚动                        │
├─────────────────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 6.2 功能清单

| 功能 | 说明 |
|------|------|
| 快速筛选标签 | 多选，选中后反映在工具栏筛选标签中 |
| 排序 | 综合推荐/销量/价格升降/评分/出版时间/评论数 |
| 视图切换 | 网格 5列 / 列表 1列 |
| 书卡操作 | 收藏、悬停上浮 |
| 分页 | 传统分页或无限滚动（二选一） |
| 空状态 | 无结果时显示引导 |

---

## 七、页面规格：分类浏览

**路由：** `/categories`

### 7.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏                                  │
├─────────────────────────────────────────┤
│  页面标题区（深色背景）                    │
│  面包屑 + 标题 + 描述 + 统计              │
├──────────┬──────────────────────────────┤
│  侧边栏   │  工具栏 + 书籍网格 + 分页     │
│  - 分类树  │                              │
│  - 价格   │                              │
│  - 评分   │                              │
│  - 形式   │                              │
│  - 库存   │                              │
├──────────┴──────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 7.2 功能清单

| 功能 | 说明 |
|------|------|
| 分类树 | 8个一级分类可展开/折叠，每个含子分类和数量。选中高亮 |
| 价格区间 | 双输入框 ¥最低—¥最高 + 确认按钮 |
| 评分筛选 | 单选 9分以上/8分以上/7分以上 |
| 商品形式 | 多选 纸质书/电子书/有声书/套装 |
| 库存状态 | 多选 有货/预售/支持退货 |
| 工具栏 | 同全部书籍 |
| 书籍网格 | 4列（因左侧有侧边栏，从5列缩减） |
| 移动端 | 侧边栏隐藏，变为抽屉式侧滑面板 |

### 7.3 分类数据结构

```javascript
const categories = [
  {
    id: 1,
    name: '文学小说',
    count: 24680,
    children: [
      { id: 11, name: '中国当代小说', count: 6240 },
      { id: 12, name: '中国古典小说', count: 3180 },
      { id: 13, name: '外国文学', count: 8560 },
      { id: 14, name: '悬疑推理', count: 3420 },
      { id: 15, name: '科幻奇幻', count: 2180 },
      { id: 16, name: '散文随笔', count: 1100 },
    ]
  },
  // ... 社科人文、科技新知、商业财经、艺术设计、生活美学、教育考试、童书绘本
];
```

---

## 八、页面规格：排行榜

**路由：** `/ranking`

### 8.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏                                  │
├─────────────────────────────────────────┤
│  页面标题区（深色背景）                    │
├─────────────────────────────────────────┤
│  榜单 Tab：畅销/好评/新锐/借阅/收藏/口碑  │
│  时间筛选：本周/本月/本季/全年/总榜        │
├─────────────────────────────────────────┤
│  前三名特写区（三列，大封面）              │
│  🥇           🥈           🥉            │
├─────────────────────────────────────────┤
│  第4~20名列表（水平列表行）               │
├─────────────────────────────────────────┤
│  [加载更多 21~50]                        │
├─────────────────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 8.2 榜单类型

| 榜单 | 排序依据 | 主指标 | 次指标 |
|------|----------|--------|--------|
| 畅销总榜 | 销售量 | 累计销量 | 周环比 |
| 好评榜 | 评分×评论数权重 | 评分 | 评论数 |
| 新锐榜 | 近30天销量增速 | 周增速% | 排名变化 ↑↓ |
| 借阅榜 | 借阅量 | 借阅次数 | — |
| 收藏榜 | 收藏数 | 收藏总数 | 本周新增 |
| 口碑上升榜 | 近7天评分变化 | 评分变化 Δ | 评论数 |

### 8.3 前三名特写区

- 🥇 第一名居中，封面高度 320px，金色描边光晕
- 🥈🥉 第二三名左右，封面高度 280px
- 入场动画：第一名 `scale(0.95)→scale(1)`，二三名从左右 `slideIn`

---

## 九、页面规格：新书上架

**路由：** `/new-arrivals`

### 9.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏                                  │
├─────────────────────────────────────────┤
│  页面标题区（深色背景）                    │
├─────────────────────────────────────────┤
│  编辑精选新书 Hero（深色横幅）             │
│  大封面 + 推荐文案 + 预售信息 + 按钮       │
├─────────────────────────────────────────┤
│  快速筛选（时间 + 分类，两行标签）         │
├─────────────────────────────────────────┤
│  时间线式布局                             │
│  ─── 5月19日·今日 ───                    │
│  [书卡] [书卡] [书卡] [书卡]              │
│  ─── 5月18日 ───                         │
│  [书卡] [书卡] [书卡] [书卡]              │
├─────────────────────────────────────────┤
│  [加载更多]                              │
├─────────────────────────────────────────┤
│  即将上架预告区（带倒计时）               │
├─────────────────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 9.2 核心差异化设计

**时间线式布局** — 按上架日期分组展示，每组以日期标题分隔：

```
────── 5月19日 · 周一 · 今日 ──────
```

- 日期标题居中，左右分割线
- 今日额外标注「· 今日」高亮
- 日期分组间间距 48px
- 滚动时日期标题短暂 sticky

### 9.3 预售书卡增强

在标准书卡基础上增加：
- 预计发货日期
- 「加入购物车」替代「立即购买」

### 9.4 即将上架预告区

- 封面半透明（opacity 0.7）
- 倒计时：`距上架还有 3天12时45分`
- 唯一操作：「🔔 上架提醒」

---

## 十、页面规格：关于我们

**路由：** `/about`

### 10.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏                                  │
├─────────────────────────────────────────┤
│  Hero 品牌宣言（全屏深色 + 视差滚动）     │
│  "在快时代，做一家慢书店"                │
├─────────────────────────────────────────┤
│  品牌故事（左图右文）                     │
├─────────────────────────────────────────┤
│  核心理念（三列卡片）                     │
│  人工选书 | 深度阅读 | 读者社区 | 品质服务 │
├─────────────────────────────────────────┤
│  用数字说话（2行×4列统计 + CountUp 动画）│
├─────────────────────────────────────────┤
│  团队介绍（3×2 网格人物卡）              │
├─────────────────────────────────────────┤
│  发展历程（垂直时间线）                   │
│  ● 2018 → ● 2019 → ... → ● 2026        │
├─────────────────────────────────────────┤
│  联系我们（左表单 + 右联系信息）          │
├─────────────────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 10.2 关键交互

- Hero 视差滚动：背景 `background-attachment: fixed` 或 JS 控制
- 数字统计：滚动进入视口时 CountUp 动画（0→目标值，1.5s）
- 团队卡片：交错 `fadeUp` 动画
- 时间线节点：滚动时依次 `fadeLeft`
- 联系表单：实时验证 + 提交后成功提示

---

## 十一、页面规格：个人中心

**路由：** `/user`

### 11.1 页面结构

```
┌─────────────────────────────────────────┐
│  导航栏（登录态，显示头像 + 用户名）       │
├──────────┬──────────────────────────────┤
│  侧边栏   │  内容区域（根据选中项切换）    │
│  240px    │                              │
│          │  概览面板（默认）              │
│  用户信息 │  我的订单                     │
│  等级积分 │  我的书架                     │
│  进度条   │  阅读足迹                     │
│          │  评价管理                     │
│  我的交易 │  优惠券                       │
│  我的阅读 │  收货地址                     │
│  我的账户 │  账号设置                     │
│          │  消息中心                     │
│  退出登录 │                              │
├──────────┴──────────────────────────────┤
│  页脚                                    │
└─────────────────────────────────────────┘
```

### 11.2 模块清单

| 模块 | 子视图 | 核心功能 |
|------|--------|----------|
| **概览** | — | 待办事项(4卡片)、正在阅读(横向滚动)、最近订单(3条)、本月阅读(热力图缩略)、未读消息 |
| **我的订单** | 全部/待付款/待发货/待收货/已完成/退款售后 | 订单列表 + Tab 筛选 + 订单详情页 + 支付流程 |
| **我的书架** | 想读/在读/已读 | 收藏管理 + 阅读进度条 + 批量操作 |
| **阅读足迹** | — | 阅读统计 + 热力图 + 偏好分析 + 成就系统 + 月度报告 + 分享图片 |
| **评价管理** | 待评价/已评价/草稿 | 评价撰写弹窗(打星+文字+图片) + 编辑/删除 |
| **优惠券** | 可使用/已使用/已过期 | 券卡片(满减/折扣) + 兑换码输入 |
| **收货地址** | — | 地址卡片列表 + 添加/编辑表单(省市区三级联动) + 设为默认 + 上限10个 |
| **账号设置** | — | 个人信息 + 安全设置(改密码/两步验证/设备管理) + 通知偏好 + 隐私设置 |
| **消息中心** | 全部/系统通知/订单消息/互动消息 | 消息列表 + 标记已读 + 批量操作 |

### 11.3 等级体系

| 等级 | 名称 | 积分 | 权益 |
|------|------|------|------|
| LV0 | 新读者 | 0 | 基础购买 |
| LV1 | 爱书人 | 500 | 95折 |
| LV2 | 读书人 | 1,200 | 9折 + 生日券 |
| LV3 | 书虫 | 2,000 | 88折 + 专属客服 |
| LV4 | 藏书家 | 3,000 | 85折 + 新书优先购 |
| LV5 | 博览者 | 5,000 | 8折 + 线下活动邀请 |

### 11.4 成就系统

| 成就 | 条件 |
|------|------|
| 初来乍到 | 完成第一本书 |
| 书虫达人 | 连续阅读30天 |
| 百书成诵 | 累计读完100本 |
| 夜读之星 | 晚10点后阅读10次 |
| 跨界阅读 | 阅读5个以上分类 |
| 评论之王 | 发表50条书评 |
| 年度书虫 | 一年读完52本 |

---

## 十二、页面规格：管理后台

**路由：** `/admin/*`

### 12.1 整体布局

```
┌──────────┬────────────────────────────────┐
│  侧边栏   │  顶部 Header                    │
│  260px    │  面包屑 + 搜索 + 通知 + 前台预览 │
│          ├────────────────────────────────┤
│  Logo    │  内容区域                        │
│  导航分组 │  (根据侧边栏选中项切换)           │
│          │                                │
│          │                                │
│  用户信息 │                                │
└──────────┴────────────────────────────────┘
```

### 12.2 模块清单

| 模块 | 核心功能 | 关键组件 |
|------|----------|----------|
| **仪表盘** | 4 统计卡片(含环比) + 销售趋势柱状图 + 品类饼图 + 最近订单表 + 快捷操作 + 待处理事项 | StatCard, BarChart, DonutChart, DataTable |
| **书籍管理** | 书籍列表表格 + 多条件筛选 + 添加/编辑弹窗表单 + 批量导入导出 + 上下架 | DataTable, BookForm(Modal) |
| **分类管理** | 树形分类表 + 子分类展开 + 排序 + 启用/禁用 | TreeTable |
| **库存管理** | 库存概览 + 不足预警列表 + 创建补货单 + 安全库存设置 | AlertList |
| **订单管理** | 订单统计(5状态) + 订单列表 + 发货/退款操作 + 导出 | DataTable |
| **退款售后** | 退款申请列表 + 批准/拒绝 + 退款记录 | DataTable |
| **用户管理** | 用户统计 + 用户列表 + 等级/状态筛选 + 禁用 | DataTable |
| **评价审核** | 待审核列表 + 通过/拒绝 + 举报处理 | DataTable |
| **优惠券** | 券列表 + 创建券弹窗 + 使用统计 | CouponForm(Modal) |
| **系统设置** | 站点信息 + 支付配置 + 物流配置 + 邮件模板 + 操作日志 | FormSections |

### 12.3 管理后台独有组件

| 组件 | 说明 |
|------|------|
| `AdminSidebar.vue` | 固定左侧 260px，深色背景，分组导航，用户信息 |
| `AdminHeader.vue` | sticky 顶部，含面包屑 + 搜索 + 通知铃铛 |
| `StatCard.vue` | 统计卡片，含图标 + 数值 + 环比变化 |
| `DataTable.vue` | 通用数据表格，含表头 + 行 + 分页 + 操作按钮 |
| `BarChart.vue` | 纯 CSS 柱状图（或使用 Chart.js / ECharts） |
| `DonutChart.vue` | `conic-gradient` 实现的环形图 |

---

## 十三、API 接口设计建议

### 13.1 前台接口

```
# 认证
POST   /api/auth/login            # 登录
POST   /api/auth/register         # 注册
POST   /api/auth/logout           # 登出
GET    /api/auth/me               # 获取当前用户信息

# 书籍
GET    /api/books                  # 书籍列表（支持分页、筛选、排序）
GET    /api/books/:id              # 书籍详情
GET    /api/books/search?q=        # 搜索
GET    /api/books/new              # 新书列表
GET    /api/books/bestsellers      # 畅销榜
GET    /api/books/top-rated        # 好评榜

# 分类
GET    /api/categories             # 分类树（含子分类和数量）

# 排行榜
GET    /api/ranking/:type          # type: sales/rating/new/borrow/collection/reputation
GET    /api/ranking/:type?period=  # period: week/month/quarter/year/all

# 购物车
GET    /api/cart                    # 获取购物车
POST   /api/cart                    # 添加到购物车
PUT    /api/cart/:id               # 更新数量
DELETE /api/cart/:id               # 移除

# 订单
GET    /api/orders                  # 我的订单列表
GET    /api/orders/:id             # 订单详情
POST   /api/orders                 # 创建订单
PUT    /api/orders/:id/cancel      # 取消订单
PUT    /api/orders/:id/confirm     # 确认收货
POST   /api/orders/:id/pay         # 支付

# 书架
GET    /api/bookshelf               # 我的书架
POST   /api/bookshelf               # 添加到书架
PUT    /api/bookshelf/:id/progress  # 更新阅读进度
DELETE /api/bookshelf/:id           # 从书架移除

# 评价
GET    /api/reviews?book_id=        # 书籍评价列表
POST   /api/reviews                 # 发表评价
PUT    /api/reviews/:id             # 编辑评价
DELETE /api/reviews/:id             # 删除评价

# 用户
GET    /api/user/profile            # 个人信息
PUT    /api/user/profile            # 更新个人信息
PUT    /api/user/password           # 修改密码
GET    /api/user/addresses          # 收货地址列表
POST   /api/user/addresses          # 添加地址
PUT    /api/user/addresses/:id      # 编辑地址
DELETE /api/user/addresses/:id      # 删除地址

# 优惠券
GET    /api/coupons                  # 我的优惠券
POST   /api/coupons/redeem          # 兑换优惠券

# 消息
GET    /api/messages                 # 消息列表
PUT    /api/messages/:id/read       # 标记已读
PUT    /api/messages/read-all       # 全部已读

# 阅读足迹
GET    /api/footprint                # 阅读数据统计
GET    /api/footprint/calendar       # 热力图数据
GET    /api/footprint/report?month=  # 月度报告
GET    /api/achievements             # 成就列表

# 其他
POST   /api/newsletter               # 订阅邮件
POST   /api/contact                  # 联系表单
```

### 13.2 后台接口

```
# 仪表盘
GET    /api/admin/dashboard          # 统计概览
GET    /api/admin/dashboard/chart    # 图表数据

# 书籍管理
GET    /api/admin/books              # 书籍列表（管理员视图）
POST   /api/admin/books              # 添加书籍
PUT    /api/admin/books/:id          # 编辑书籍
DELETE /api/admin/books/:id          # 删除书籍
PUT    /api/admin/books/:id/status   # 上下架

# 分类管理
POST   /api/admin/categories         # 添加分类
PUT    /api/admin/categories/:id     # 编辑分类
DELETE /api/admin/categories/:id     # 删除分类

# 库存管理
GET    /api/admin/inventory/alerts   # 库存预警列表
POST   /api/admin/inventory/restock  # 创建补货单

# 订单管理
GET    /api/admin/orders             # 所有订单
PUT    /api/admin/orders/:id/ship    # 标记发货
PUT    /api/admin/orders/:id/refund  # 处理退款

# 用户管理
GET    /api/admin/users              # 所有用户
PUT    /api/admin/users/:id/status   # 禁用/启用

# 评价审核
GET    /api/admin/reviews/pending    # 待审核评价
PUT    /api/admin/reviews/:id/approve # 通过
PUT    /api/admin/reviews/:id/reject  # 拒绝

# 优惠券管理
GET    /api/admin/coupons            # 所有优惠券
POST   /api/admin/coupons            # 创建优惠券
PUT    /api/admin/coupons/:id        # 编辑优惠券
DELETE /api/admin/coupons/:id        # 删除优惠券

# 系统
GET    /api/admin/settings           # 系统设置
PUT    /api/admin/settings           # 更新设置
GET    /api/admin/logs               # 操作日志
```

---

## 十四、实现提示词（给 Claude Code）

> 以下是可以直接复制粘贴给 Claude Code 的分步骤实现提示词。
> 每个提示词对应一个阶段，按顺序执行即可逐步构建完整系统。

---

### 提示词 1：项目初始化 + 设计系统

```
请帮我初始化一个 Vue 3 + Vite 项目，项目名为 shiguang-books。

要求：
1. 使用 Vue 3 + Composition API + Vue Router 4 + Pinia + Axios
2. 不使用任何第三方 UI 组件库，所有组件手写
3. 创建完整的设计系统 CSS 文件：

src/assets/styles/variables.css — 包含以下 CSS 变量：
- 主色系（胡桃木）：primary #4A3526, primary-mid #5C4434, primary-light #7A6252, primary-dark #2E1F15, primary-abyss #1C120C
- 背景色系（羊皮纸）：bg #EDE6D6, bg-warm #E5DDD0, bg-card #F5F0E8, bg-cream #F8F4ED
- 强调色系（古铜金）：accent #C09A4B, accent-light #D4B06A, accent-muted #A68638
- 文字色：text #2B1D14, text-secondary #6B5B4E, text-light #9B8B7E
- 语义色：green #5C8856, red #A04040, blue #4A6E8A
- 间距、圆角、阴影、过渡等变量

src/assets/styles/base.css — 全局重置样式 + body 背景色 + 纸张肌理 noise overlay（用 SVG feTurbulence 实现，opacity 0.025）

src/assets/styles/typography.css — 字体加载（Google Fonts: Noto Serif SC 400/600/700/900, Noto Sans SC 300/400/500/700, DM Mono 400/500）+ 标题/正文/等宽字体族定义 + 各级字号规范

src/assets/styles/animations.css — 定义全局动画：fadeUp（20px→0）、fadeIn、slideInLeft、float（上下浮动）

4. 创建路由结构：
- 前台路由（/）：首页、全部书籍、分类浏览、排行榜、新书上架、关于我们、书籍详情、购物车、个人中心、登录、注册
- 后台路由（/admin）：仪表盘、书籍管理、分类管理、库存管理、订单管理、退款售后、用户管理、评价审核、优惠券管理、系统设置
- 前台和后台使用不同的 Layout

5. 创建 Pinia stores 骨架：auth.js, cart.js, books.js, user.js

6. 创建 API 请求封装：src/api/request.js，包含 Axios 实例、baseURL、请求拦截器（自动加 token）、响应拦截器（处理 401 跳转登录）

请生成完整的文件结构和代码。
```

---

### 提示词 2：全局通用组件

```
基于已有的设计系统（variables.css 中定义的 CSS 变量），请帮我创建以下全局通用组件：

1. BookCard.vue — 书籍卡片组件
   - Props: id, title, author, coverUrl, coverColor(1-12的编号), price, originalPrice, rating, badge(畅销/新书/经典/热卖), isFavorited, mode('grid'|'list')
   - grid模式：3:4封面色块（当无封面图时用编号对应的渐变色）+ 角标 + 收藏按钮 + 书名(最多2行) + 作者 + 价格(金色) + 评分(金色星星)
   - list模式：左侧120px封面 + 右侧书名/作者/简介/价格
   - 交互：hover上浮5px+阴影加深+封面scale(1.04)；收藏按钮hover才显示；点击收藏切换
   - 12种封面渐变色方案，全部是深色渐变（棕色系、深蓝、深绿、酒红等）
   - 使用 IntersectionObserver 实现进入视口时 fadeUp 动画

2. Pagination.vue — 分页器
   - Props: currentPage, totalPages, totalItems
   - Event: @page-change(page)
   - 样式：‹ 上一页 [1] 2 3 4 5 ... 186 下一页 ›

3. Toast.vue — 提示消息（使用 provide/inject 或 composable）
   - useToast() composable，提供 showToast(msg, type) 方法
   - 类型：ok（绿色圆点）、error（红色圆点）
   - 位置：右上角，3秒自动消失

4. Modal.vue — 弹窗
   - Props: visible(v-model), title, width
   - 深色半透明遮罩 + 居中卡片 + 头部标题/关闭 + 内容slot + 底部slot
   - 点击遮罩关闭

5. EmptyState.vue — 空状态
   - Props: icon, title, description, actionText
   - 居中布局，线描风格

6. StatusBadge.vue — 状态徽章
   - Props: type('green'|'orange'|'blue'|'red'|'gray'), text
   - 圆角胶囊，不同颜色

7. RatingStars.vue — 评分星星
   - Props: rating(Number), showValue(Boolean)
   - 金色星星 + 数值

8. PriceTag.vue — 价格标签
   - Props: price, originalPrice
   - 金色 Serif 字体 + 原价划线

请生成所有组件的完整代码，确保与设计系统的 CSS 变量一致。风格为老派书店质感（深色胡桃木 + 古铜金 + 羊皮纸底色）。
```

---

### 提示词 3：前台导航栏 + 页脚 + 首页布局

```
请帮我创建前台的 Layout 和首页：

1. FrontLayout.vue — 前台布局
   - 顶部 AppHeader（sticky导航栏）
     - 背景 #1C120C，底部1px rgba(192,154,75,0.15) 边框
     - 左：Logo图标(金色渐变背景) + "拾光书屋" + "SHIGUANG BOOKS"副标
     - 中：导航链接（首页/全部书籍/分类浏览/排行榜/新书上架/关于我们）
     - 右：搜索框 + 购物车图标(带数量角标) + 用户头像(登录态)/登录按钮(未登录)
     - 导航链接：默认 rgba(237,230,214,0.5)，hover变亮，router-link-active 金色 + 底部下划线动画
   - 主内容区域 <router-view>
   - 底部 AppFooter
     - 背景 #1C120C
     - 四列：品牌简介 / 书籍分类 / 帮助中心 / 关于我们
     - 底部版权

2. HomePage.vue — 首页
   - HeroBanner区域：
     - 背景：深色木纹渐变 #1C120C→#2E1F15→#5C4434
     - 左侧：标签"编辑精选·本周推荐" + H1"在文字中遇见另一个世界"(「另一个世界」金色斜体) + 描述文字 + 两个按钮(主按钮金色渐变 + 副按钮透明边框)
     - 右侧：三本书浮动卡片（百年孤独/小王子/活着），CSS float动画，各自不同旋转角度
   - StatsBar区域：
     - 4列统计（128,000+图书 / 56,000+读者 / 98.6%好评 / 次日达配送）
     - 深色底，金色数字
   - 热门推荐区域：
     - H2 + "查看全部→"
     - 8个分类标签（全部/文学小说/社科人文...），点击切换
     - 5列书籍网格，使用 BookCard 组件，展示10本书
     - 每本书的数据包含：title, author, coverColor, price, originalPrice, rating, badge
   - 编辑推荐横幅：
     - 深色背景全宽
     - 左侧大封面 + 右侧：标签"编辑推荐·本周之选" + 书名 + 作者 + 简介 + 价格 + 两个按钮
   - 畅销榜预览：
     - H2 + "完整榜单→"
     - 2列6个排行卡片（排名序号 + 小封面 + 书名 + 作者 + 价格）
     - 前三名序号用金色/棕色/深棕色区分
   - 邮件订阅：
     - 暖色背景，标题+描述+邮箱输入框+订阅按钮

3. 创建 mock 数据文件 src/mock/books.js，包含至少15本书的数据（使用真实的中文书名和作者名），每本书包含：id, title, author, coverColor(1-12), price, originalPrice, rating, badge, category, description, isbn

请生成完整代码。所有颜色必须使用 CSS 变量。风格保持古铜金+胡桃木+羊皮纸的书店质感。
```

---

### 提示词 4：全部书籍 + 分类浏览页面

```
请帮我创建全部书籍页面和分类浏览页面：

1. AllBooksPage.vue — 全部书籍
   - 快速筛选标签栏：全部/今日特价/限时折扣/包邮/套装优惠/预售新品/电子书（可多选）
   - 工具栏：
     - 左：结果计数"共 128,000 本" + 已选筛选标签(胶囊形,可点✕移除) + "清除全部"
     - 右：排序下拉(综合推荐/销量/价格升降/评分/出版时间) + 视图切换(网格▦/列表☰)
   - 书籍网格：5列使用BookCard组件，默认grid模式，切换list模式
   - 分页器
   - 空状态处理
   - 响应式：平板3列，手机2列

2. CategoryPage.vue — 分类浏览
   - 页面标题区（深色背景）：面包屑 + 标题 + 描述 + 统计
   - 双栏布局：240px侧边栏 + 自适应内容区
   - 侧边栏（sticky）：
     - 分类树：8个一级分类可展开/折叠，每个含子分类和数量
     - 一级分类点击toggle子分类（箭头旋转动画▸→▾）
     - 选中态：深色底+浅色文字+金色计数气泡
     - 价格区间：双输入框 ¥最低—¥最高 + 筛选按钮
     - 评分筛选：单选 9分以上/8分以上/7分以上
     - 商品形式：多选复选框（纸质书/电子书/有声书/套装）
     - 库存状态：多选复选框（有货/预售/支持退货）
   - 内容区：
     - 工具栏（同全部书籍）
     - 4列书籍网格
     - 分页器
   - 移动端：侧边栏隐藏，显示"☰筛选与分类"按钮，点击弹出左侧抽屉面板(300px)

3. 创建 mock 数据文件 src/mock/categories.js，包含完整的8个分类数据，每个分类有子分类和count

请生成完整代码。交互要流畅，动画要自然。
```

---

### 提示词 5：排行榜 + 新书上架页面

```
请帮我创建排行榜页面和新书上架页面：

1. RankingPage.vue — 排行榜
   - 页面标题区（深色背景）
   - 榜单Tab：畅销总榜/好评榜/新锐榜/借阅榜/收藏榜/口碑上升榜
     - Tab横向排列，选中态底部金色线条+粗体
   - 时间筛选按钮组：本周/本月/本季/全年/总榜
   - 前三名特写区：
     - 三列布局，🥇居中稍大(封面320px高)，🥈🥉左右(280px高)
     - 每张：大封面色块 + 排名徽章(金色圆形1/2/3) + 书名 + 作者 + 价格 + 评分 + 关键指标(销量/评分等)
     - 第一名有金色描边光晕
     - 入场动画：第一名scale弹入，二三名从左右slideIn
   - 第4~20名：水平列表行
     - 每行：排名数字 + 68×95封面 + 书名 + 作者 + 价格 + 评分 + 指标
     - 排名4-10用 primary-mid，11-20用 text-light
     - 悬停：底色微变 + 轻微左移
   - "加载更多"按钮，点击追加21-50名
   - 不同榜单Tab切换时内容淡出→淡入

2. NewArrivalsPage.vue — 新书上架
   - 页面标题区（深色背景）
   - 编辑精选Hero：深色横幅，大封面 + 标签 + 书名 + 作者 + 出版社 + 推荐语 + 价格 + 状态(预售中/已到货) + 两个按钮
   - 快速筛选：两行标签（时间：全部/本周/本月/预售中/已到货 + 分类：文学/人文/科技/商业/艺术/童书），可交叉筛选
   - 时间线式布局（核心差异化）：
     - 按上架日期分组，每组以日期标题分隔
     - 日期标题居中，左右分割线，今日额外标注"·今日"高亮
     - 每个日期下4列书籍网格
     - 书卡角标区分：NEW(新到货)/预售/再版
     - 日期分组间间距48px
   - 预售书卡增强：额外显示预计发货日期
   - 即将上架预告区：
     - 标题"即将上架·先收藏不错过"
     - 3列预告卡片，封面半透明(opacity 0.7)
     - 倒计时"距上架还有 3天12时45分"(数字用Serif金色)
     - 唯一操作"🔔上架提醒"
   - "加载更多"按钮

请生成完整代码。注意排行榜的数据要根据榜单类型不同展示不同的指标。
```

---

### 提示词 6：关于我们页面

```
请帮我创建关于我们页面：

AboutPage.vue — 完整的关于我们页面

1. Hero品牌宣言：
   - 全屏深色背景(#1C120C)
   - 视差滚动效果
   - 中央大标题Serif 3rem "在快时代，做一家慢书店"
   - 副标题"我们相信，每一本书都值得被认真对待"
   - 纸张肌理暗纹叠加

2. 品牌故事：
   - 左图右文布局
   - 左侧：模拟旧照片的色块（带sepia色调和圆角阴影）
   - 右侧：Serif标题 + Sans正文，讲述2018年三位爱书人创办拾光书屋的故事
   - 滚动进入视口时图片从左侧slideIn，文字从右侧fadeUp

3. 核心理念：
   - 3-4列等分卡片
   - 每张：图标(用Unicode符号，金色) + 标题 + 说明文字
   - 卡片：--color-bg-card底色，细边框，hover上浮+金色边框

4. 用数字说话：
   - 2行×4列数据
   - 数据：128,000+图书 / 56,000+读者 / 2,800+出版社 / 365天运营 / 98.6%好评 / 4.8均分 / 24h客服 / 7年历程
   - 数字使用Serif 900，金色，2rem
   - IntersectionObserver触发CountUp动画（0→目标值，1.5秒）

5. 团队介绍：
   - "我们是一群相信文字力量的人"
   - 3×2网格人物卡
   - 每张：圆形头像(首字+柔和背景色) + 姓名(Serif粗体) + 角色(金色标签) + 简介 + "最爱的书"
   - 卡片交错fadeUp动画

6. 发展历程：
   - 垂直时间线布局
   - 左侧竖线2px + 节点圆点12px(--color-accent)
   - 节点：2018诞生→2019线上→2020编辑推荐→2022突破30000→2024突破10万→2026全新改版
   - 最新节点圆点放大+金色光晕
   - 滚动时节点依次fadeLeft出现

7. 联系我们：
   - 左右两栏
   - 左栏：联系表单（姓名/邮箱/主题下拉/内容文本域 + 发送按钮）
     - 实时验证 + 提交成功替换为成功提示
   - 右栏：地址/电话/邮箱/工作时间/社交媒体图标

请生成完整代码。这个页面要特别注重滚动动画效果和视觉层次。
```

---

### 提示词 7：个人中心页面

```
请帮我创建个人中心页面：

UserCenterPage.vue — 个人中心（双栏布局）

1. 布局：
   - 左侧 240px 侧边栏（sticky, top:88px）
   - 右侧内容区域根据选中项动态切换

2. 侧边栏（UserSidebar.vue）：
   - 顶部用户信息卡：圆形头像(72px, 金色边框) + 昵称(Serif粗体) + "LV3 书虫 · 2,460积分" + 升级进度条(68%) + "距LV4还需540积分"
   - 导航分组：
     - 概览（默认选中）
     - ── 我的交易 ──：我的订单、优惠券、收货地址
     - ── 我的阅读 ──：我的书架、阅读足迹、评价管理
     - ── 我的账户 ──：账号设置、消息中心
     - 退出登录
   - 选中态：rgba(74,53,38,0.06)底色 + primary文字 + 左侧3px金色竖条

3. 概览面板（OverviewPanel.vue，默认展示）：
   - 欢迎语："欢迎回来，林书语 👋（根据时段变化早上好/下午好/晚上好）"
   - 待办事项：4卡片（3待付款/1待发货/2待收货/5待评价），有数字时高亮
   - 正在阅读：横向滚动的小书卡(带进度条)
   - 最近订单：简化表格3-5条
   - 本月阅读：左侧迷你热力图 + 右侧关键数字(读完8本/阅读45小时/最长连续7天)
   - 未读消息：2-3条

4. 我的订单（OrderPanel.vue）：
   - Tab：全部/待付款(3)/待发货(1)/待收货(2)/已完成/退款售后
   - 搜索+筛选栏
   - 订单卡片：头部(订单号+时间+状态标签) → 商品列表(封面+书名+作者+数量+小计) → 底部(合计金额+操作按钮)
   - 操作按钮根据状态不同：待付款→取消/支付 / 待发货→联系客服/提醒发货 / 待收货→查看物流/确认收货 / 已完成→再次购买/评价晒单
   - 点击卡片展开订单详情（状态进度条 + 物流信息 + 价格明细 + 收货信息）

5. 我的书架（BookshelfPanel.vue）：
   - Tab：想读(86) / 在读(24) / 已读(156)
   - 想读：标准书卡 + "开始阅读"/"移除"按钮
   - 在读：书卡 + 阅读进度条 + "更新进度"/"标记已读"/"移除"
   - 已读：书卡 + 完成日期 + "写评价"/"再次阅读"/"移除"
   - 进度更新弹窗：输入页码或百分比 + 一句话感受

6. 阅读足迹（FootprintPanel.vue）：
   - 阅读概览：4统计(156本已读/1280小时/48本今年/720万字)
   - 阅读日历热力图（类似GitHub）：行周一~日，列按周，方块颜色表示时长(5级)
   - 阅读偏好分析：分类水平条形图 + 作者卡片
   - 阅读成就：卡片网格，已获得(金色)+未获得(灰色+半透明)+进度
   - 月度阅读报告：月份选择器 + 统计 + "生成分享图片"按钮

7. 评价管理（ReviewPanel.vue）：
   - Tab：待评价(5) / 已评价(32) / 草稿(2)
   - 待评价：封面+书名+购买日期+评价截止+"写评价"/"不评价"
   - 已评价：封面+书名+评分+一句话+详细评价+发布时间+有用数+回复数+"编辑"/"删除"
   - 写评价弹窗：打星(5星,hover预览) + 一句话总结(50字) + 详细评价(500字) + 添加图片(最多9张) + 分享/匿名选项

8. 优惠券（CouponPanel.vue）：
   - Tab：可使用(8) / 已使用(12) / 已过期(5)
   - 券卡片：左侧金额/折扣区(深色底金色文字,虚线分隔) + 右侧(条件/范围/有效期/操作)
   - 即将过期(7天内)：有效期变红+⚠
   - 已使用/已过期：降低透明度+"已使用"/"已过期"印章
   - 兑换码输入框

9. 收货地址（AddressPanel.vue）：
   - "+添加新地址"按钮
   - 地址卡片：收货人+电话+详细地址+邮编 + [默认]标签 + "设为默认"/"编辑"/"删除"
   - 添加/编辑表单弹窗：收货人/手机/省市区三级联动/详细地址/邮编/设为默认
   - 上限10个

10. 账号设置（SettingsPanel.vue）：
    - 个人信息：头像(可更换)+昵称+性别+生日+简介+手机+邮箱+微信
    - 安全设置：登录密码(修改弹窗，含密码强度检测)/支付密码/两步验证/登录设备管理
    - 通知偏好：各类型通知开关 + 推送渠道复选
    - 隐私设置：公开书架/评价/阅读记录/私信
    - 危险操作：注销账号

11. 消息中心（MessagePanel.vue）：
    - Tab：全部/系统通知(3)/订单消息(5)/互动消息(2)
    - 消息列表：未读(金色圆点+粗体) / 已读(常规)
    - 每条：图标(根据类型) + 内容 + 时间 + 操作按钮
    - 批量操作：全选/标记已读/删除

请生成完整代码。注意移动端适配：侧边栏变为底部Tab或顶部下拉。
```

---

### 提示词 8：管理后台

```
请帮我创建管理后台的所有页面：

1. AdminLayout.vue — 后台布局
   - 左侧 AdminSidebar.vue（固定260px, #1C120C背景）
     - Logo区："拾光书屋"+"ADMIN CONSOLE"
     - 导航分组：概览(仪表盘) / 商品管理(书籍/分类/库存) / 交易管理(订单/退款售后) / 用户与运营(用户/评价审核/优惠券) / 系统(系统设置)
     - 导航项带图标+文字+角标数字(待处理项红色角标)
     - 底部用户信息(头像+姓名+角色)
   - 顶部 AdminHeader.vue（sticky, 64px高）
     - 左：当前页面标题 + 面包屑
     - 右：搜索框 + 通知铃铛(带圆点) + 前台预览按钮
   - 右侧内容区 <router-view>

2. DashboardPage.vue — 仪表盘
   - 4统计卡片：本月销售额¥186,420(+12.5%) / 本月订单2,847(+8.3%) / 注册用户56,320(+5.1%) / 在售图书128,640(-2.1%)
   - 图表行（2列）：
     - 左：销售趋势柱状图（7天数据，纯CSS bar chart，hover显示金额tooltip，Tab切换7天/30天/90天）
     - 右：品类分布环形图（conic-gradient实现，中心显示总数，右侧图例）
   - 最近订单表：5列最新订单(订单号/客户/商品/金额/状态/时间/操作)，底部"查看全部→"
   - 快捷操作（2列）：
     - 左：添加新书/创建优惠券/发布公告
     - 右：待处理事项(23笔待发货/5笔退款/12条评价审核/8本库存不足)

3. BooksManagePage.vue — 书籍管理
   - 页面头部：标题 + 导入/导出/添加书籍按钮
   - 筛选栏：搜索框 + 分类下拉 + 状态下拉 + 排序下拉
   - 数据表格：书籍(封面小图+书名+作者) / ISBN / 分类 / 售价 / 库存 / 销量 / 状态 / 操作(编辑/下架/补货)
   - 分页
   - 添加书籍弹窗(Modal)：书名/作者/ISBN/分类/出版社/售价/原价/库存/状态/简介，含表单验证

4. OrdersManagePage.vue — 订单管理
   - 5个统计卡片(全部156/待付款18/待发货23/待收货34/退款中5)
   - 筛选栏 + 数据表格(订单号/客户/商品/金额/状态/时间/操作)
   - 操作：发货(显示toast)/提醒/取消/批准退款/拒绝

5. CategoriesManagePage.vue — 分类管理
   - 树形表格：分类名/父分类/书籍数/排序/状态/操作
   - 子分类缩进展示

6. InventoryPage.vue — 库存管理
   - 3统计卡片 + 库存表格(书籍/当前库存/安全库存/日均销量/预计可售天数/状态/操作)
   - 库存不足项红色高亮，可"创建补货单"(显示toast)

7. UsersManagePage.vue — 用户管理
   - 4统计 + 用户表格(用户头像+名/邮箱/等级/订单数/消费总额/注册时间/状态/操作)

8. 其余页面（退款售后/评价审核/优惠券/系统设置）创建占位页面即可，显示功能说明

请生成完整代码。管理后台风格与前台一致（古铜金+胡桃木），但信息密度更高、表格更多。
```

---

### 提示词 9：响应式适配 + 微交互完善

```
请帮我完善所有页面的响应式适配和微交互：

1. 响应式断点：
   - Desktop L（≥1280px）：5列书籍网格，侧边栏240-260px
   - Desktop（1024~1279px）：4列网格，侧边栏200px
   - Tablet（768~1023px）：3列网格，前台分类页侧边栏隐藏→抽屉，后台侧边栏折叠
   - Mobile（<768px）：2列网格，抽屉式筛选，导航汉堡菜单，个人中心底部Tab

2. 移动端适配细节：
   - 前台导航栏：链接隐藏，显示汉堡菜单图标，点击弹出全屏导航面板
   - 分类浏览：侧边栏隐藏，显示"筛选"按钮，点击弹出左侧抽屉(300px, 带遮罩)
   - 排行榜前三：三列→纵向堆叠
   - 关于我们：Hero/故事/团队改为单栏堆叠
   - 个人中心：侧边栏→底部Tab栏(5个核心入口：订单/书架/足迹/消息/设置)
   - 管理后台：侧边栏可折叠为图标模式(60px宽)
   - 分页器：只显示上一页/当前页/下一页

3. 微交互完善：
   - 所有按钮hover：translateY(-2px) + 阴影变化
   - 书卡hover：translateY(-5px) + 阴影从shadow→shadow-heavy + 封面scale(1.04)
   - 导航链接hover：底部金色线条width:0→100%动画
   - 分类树展开/折叠：子分类max-height过渡 + 箭头旋转
   - Tab切换：内容淡出→数据更新→淡入
   - Toast：从右侧slideIn → 自动slideOut
   - 弹窗：遮罩fadeIn + 卡片从translateY(16px)→0
   - 收藏切换：♡→♥ + 微弹scale(1.1)→scale(1)
   - 进入视口动画：使用IntersectionObserver，fadeUp + 每项错开30ms
   - 数字递增：CountUp效果，缓入缓出
   - 热力图tooltip：hover方块显示日期+时长+书名
   - 表格行hover：背景微变

4. 性能优化提示：
   - 书卡进入视口动画使用IntersectionObserver而非scroll事件
   - 大量书卡使用虚拟滚动（如 vue-virtual-scroller）
   - 图片懒加载
   - 路由懒加载（所有页面组件使用 () => import()）

请更新所有相关组件的代码，确保在各断点下都有良好的体验。
```

---

### 提示词 10：Mock 数据 + 联调准备

```
请帮我创建完整的 mock 数据层和前端状态管理：

1. src/mock/ 目录下创建：

books.js — 30本书的数据，每本包含：
  id, title, author, coverColor(1-12随机), price, originalPrice, rating(8.0-9.8),
  badge(畅销/新书/经典/热卖/null), categoryId, description(50-100字),
  isbn, publisher, publishDate, stock, sales, pages, language, format(纸质书/电子书)

categories.js — 完整8分类树，每分类含子分类和count

orders.js — 10笔不同状态的订单，每笔含：
  id, orderNo, userId, items(数组,每项含bookId/title/author/coverColor/price/qty),
  totalAmount, shippingFee, discount, payAmount, status, paymentMethod,
  createdAt, paidAt, shippedAt, shippingNo, address

users.js — 10个用户，每用户含：
  id, name, avatar(首字), email, phone, level(0-5), levelName, points,
  totalOrders, totalSpent, registerDate, status

coupons.js — 8张优惠券，含不同类型(满减/折扣)和状态(可使用/已使用/已过期)

reviews.js — 15条评价，含不同状态(已通过/待审核/已拒绝)

messages.js — 10条消息，含不同类型(订单/系统/互动/优惠券)

achievements.js — 10个成就定义

2. 更新 Pinia stores：

stores/auth.js：
  - state: user, token, isLoggedIn
  - actions: login(email, password), logout, fetchUser
  - 使用 localStorage 持久化 token

stores/cart.js：
  - state: items[]
  - actions: addItem, removeItem, updateQuantity, clearCart
  - getters: totalPrice, totalItems

stores/books.js：
  - state: books[], currentBook, filters, pagination
  - actions: fetchBooks(filters), fetchBook(id), searchBooks(query)
  - 支持分页、筛选、排序

stores/user.js：
  - state: profile, orders[], bookshelf[], coupons[], addresses[], messages[]
  - actions: fetchProfile, fetchOrders, fetchBookshelf, updateProfile, ...

3. API 层对接准备：
  - src/api/request.js — Axios实例，baseURL指向 /api
  - 所有 API 函数先返回 mock 数据（用 setTimeout 模拟 300ms 延迟）
  - 注释标注对应的后端接口路径
  - 切换到真实后端时只需修改 request.js 的 baseURL 和移除 setTimeout

请生成完整的 mock 数据和 store 代码。mock 数据要真实、丰富、有层次（不同状态、不同分类、不同等级）。
```

---

> **使用说明：** 将以上 10 个提示词按顺序依次发给 Claude Code，每个提示词完成后检查生成的代码，确认无误后再进行下一个。整个过程将逐步构建出完整的拾光书屋前端系统。
```