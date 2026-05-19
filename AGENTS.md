# AGENTS.md

## 快速命令

```bash
mvn clean package              # 完整构建
mvn spring-boot:run            # 启动后端（端口 8081）
mvn test -Dtest=ClassName      # 运行单个测试
mysql -u root -p < sql/init.sql  # 初始化数据库（8 张表）
```

bookstore-frontend/ 是独立前端（Vue 3 + Vite）：
```bash
npm run dev   # 启动前端（端口 5173，代理 /api, /admin → 8081）
npm run build # 构建生产包
```

## 架构要点

- **三层架构**：Controller → Service → Mapper（MyBatis-Plus），无 XML 映射文件
- **DTO 入 / VO 出**：请求用 DTO，响应用 VO，统一 `Result<T>(code,message,data)` 包装
- **分页响应**：`PageResult<T>(total,records)`，MyBatis-Plus `PaginationInnerInterceptor` 分页
- **认证**：`AuthInterceptor` 拦截 `/api/cart/**`, `/api/order/**`, `/api/user/info|password|profile`, `/api/review/add`, `/admin/**`，校验 JWT（Header: `Authorization: Bearer <token>`），用户信息存 `AuthContext`（ThreadLocal）
- **管理员**：`AdminInterceptor` 拦截所有 `/admin/**`，校验 role=admin
- **全局异常**：`GlobalExceptionHandler` 捕获 `BusinessException` 和通用 `Exception`
- **订单状态流**：`pending → paid → shipped → delivered → completed`；`pending → cancelled`；`paid/shipped → refunded`
- **密码**：BCrypt 加密

## 关键约束

- SQL 中 `order` 表名需反引号包裹（MySQL 保留字）
- 默认管理员：`admin` / `123456`
- JWT 密钥在 `application.yml` 的 `jwt.secret`，有效期 24h
- 后端端口 8081，前端端口 5173（Vite 代理后端）
- `@MapperScan("com.example.bookstore.mapper")` 在 `BookstoreApplication.java`
- `BaseEntity` 提供 `id` `createTime` `updateTime` 公共字段
- 测试仅一个 `main` 方法类（BCrypt 哈希生成），无 Spring 测试
