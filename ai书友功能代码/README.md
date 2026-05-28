# AI书友功能代码

## 功能概述

AI书友（拾光书屋AI助手）是一个基于大模型Function Calling能力的智能购书助手，可以帮助用户：
- 搜索和推荐书籍
- 查询书籍详细信息
- 将书籍加入购物车
- 查看购物车和收货地址

## 目录结构

```
ai书友功能代码/
├── README.md                          # 本文件
├── AIController.java                  # 后端AI控制器（Spring Boot）
├── frontend/
│   ├── src/
│   │   ├── views/user/AIAssistant.vue # 前端AI聊天页面
│   │   └── api/ai.js                  # 前端AI API调用
│   └── (需配合 bookstore-frontend 使用)
└── whitebox/
    └── controller/
        └── AIControllerWhiteBoxTest.java  # 白盒测试代码
```

## 后端文件说明

### AIController.java
- **路径**: `src/main/java/com/example/bookstore/controller/AIController.java`
- **功能**:
  - `/api/ai/chat` - POST 同步聊天接口
  - `/api/ai/chat/stream` - POST SSE流式聊天接口
- **核心逻辑**:
  - `processChat()` - 处理聊天请求，调用MiniMax API
  - `executeTool()` - 分发工具调用（searchBooks/getBookDetail/addToCart/getCartItems/listAddresses）
  - `buildTools()` - 构建Function Calling工具定义

### 配置要求
在 `application-local.yml` 中配置：
```yaml
minimax:
  api:
    key: your-api-key
    url: https://api.minimax.chat/v1/text/chatfunction_v2
```

## 前端文件说明

### AIAssistant.vue
- **路径**: `bookstore-frontend/src/views/user/AIAssistant.vue`
- **功能**:
  - 聊天界面UI
  - 流式输出展示
  - 书籍卡片内嵌显示
  - 快捷操作（加入购物车、立即购买）

### ai.js
- **路径**: `bookstore-frontend/src/api/ai.js`
- **功能**:
  - `chat()` - 普通聊天请求
  - `chatStream()` - SSE流式聊天，解析SSE事件

### 路由配置
```javascript
// bookstore-frontend/src/router/index.js
{
  path: '/ai-assistant',
  name: 'AIAssistant',
  component: () => import('@/views/user/AIAssistant.vue')
}
```

### 导航栏入口
```vue
<!-- bookstore-frontend/src/components/common/AppHeader.vue -->
<router-link to="/ai-assistant" class="nav-ai">
  <!-- AI图标 -->
</router-link>
```

## 白盒测试说明

### AIControllerWhiteBoxTest.java
- **路径**: `src/test/java/com/example/bookstore/whitebox/controller/AIControllerWhiteBoxTest.java`
- **测试用例**:
  - `chat_withSearchBooksToolCall_shouldReturnBooks` - 测试搜索书籍工具调用
  - `chatStream_shouldReturnSseEvents` - 测试SSE流式响应
  - `executeTool_searchBooks_shouldParseKeywordFromArgs` - 测试工具参数解析

## 技术栈

- **后端**: Spring Boot 3.2.5 + MyBatis-Plus 3.5.6
- **前端**: Vue 3 + Vite 5 + Element Plus
- **AI模型**: MiniMax (MiniMax-M2.7)
- **通信**: REST API + Server-Sent Events (SSE)