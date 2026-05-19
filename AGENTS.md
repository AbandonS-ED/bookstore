# AGENTS.md

## Quick commands

```bash
mvn clean package              # Full build
mvn spring-boot:run            # Start backend (port 8081)
mvn test -Dtest=ClassName      # Run single test (only `main()` class exists)
mysql -u root -p < sql/init.sql  # Init database (creates `bookstore` DB, 8 tables)
```

Frontend (`bookstore-frontend/`, Vue 3 + Vite + Element Plus + Pinia):
```bash
npm run dev   # Port 5173, proxies /api and /admin → 8081
npm run build # Production bundle
```

## Architecture

- **Controller → Service → Mapper** (MyBatis-Plus, **no XML mapper files** — `classpath:mapper/*.xml` configured but unused)
- **DTO in / VO out**: requests use DTOs, responses use VOs, wrapped in `Result<T>(code,message,data)`
- **Pagination**: `PageResult<T>(total,records)` via MyBatis-Plus `PaginationInnerInterceptor`
- **Auth**: `AuthInterceptor` on `/api/user/info|password|profile`, `/api/address/**`, `/api/cart/**`, `/api/order/**`, `/api/review/add`, `/admin/**` — validates JWT (`Authorization: Bearer <token>`), stores user in `AuthContext` (ThreadLocal)
- **Admin**: `AdminInterceptor` on `/admin/**` — checks `role=admin`
- **Pwd**: BCrypt via `spring-security-crypto` (no full Spring Security)
- **JWT**: HMAC-SHA384 (jjwt 0.12.5), key at `jwt.secret` in `application.yml`, 24h expiry
- **Orders state**: `pending → paid → shipped → delivered → completed`; `pending → cancelled`; `paid/shipped → refunded` (**defined but `refunded` transitions NOT implemented**)

## Key constraints

- SQL `order` table must be backtick-quoted (MySQL reserved word)
- Default admin: `admin` / `123456`
- `application-local.yml` overrides `spring.datasource.password` via `DB_PASSWORD` env var (empty default)
- Only test: `src/test/.../Test.java` — has `main()` method only (no Spring tests, no test framework usage)
- `@MapperScan("com.example.bookstore.mapper")` in `BookstoreApplication.java`
- `BaseEntity` provides `id`, `createTime`, `updateTime` (MyBatis-Plus `@TableField` auto-fill)
- Port: backend 8081, frontend 5173
