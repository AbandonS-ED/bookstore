# AGENTS.md

## Quick commands

```bash
mvn spring-boot:run            # Start backend (port 8081)
npm run dev                    # Frontend (port 5173, proxy /api /admin → 8081)
mysql -u root -p < sql/init.sql  # Init database (creates `bookstore` DB, 8 tables)
```

Build & test:
```bash
mvn clean package              # Full build
mvn compile                    # Fast compile check only
# No test framework — `src/test/.../Test.java` has only `main()` for BCrypt hashing
```

## Critical: Snowflake ID precision

All entities use MyBatis-Plus `ASSIGN_ID` (Snowflake), producing 19-digit IDs exceeding `Number.MAX_SAFE_INTEGER`. **Every `Long` ID crossing the JS boundary must be a string**, or precision is lost silently.

Rules enforced across the codebase (`BaseEntity`, 9 VOs, ~5 DTOs):

| Layer | Pattern | Example |
|-------|---------|---------|
| **VO** (response) | `@JsonSerialize(using = ToStringSerializer.class)` on every `Long id`, `userId`, `bookId`, `categoryId`, `parentId` | `BookVO.bookId` |
| **Entity** (response + admin `@RequestBody`) | `@JsonFormat(shape = JsonFormat.Shape.STRING)` on `BaseEntity.id`, `Book.categoryId`, `Category.parentId` | bidirectional |
| **DTO** (request body) | `String` instead of `Long` for ID fields | `OrderCreateDTO.cartItemIds: List<String>` |
| **Service** | `Long.parseLong(dto.getId())` to convert back | `AddressServiceImpl.update()` |

**VOs do NOT extend BaseEntity** — each copies entity fields in `convertToVO()`. If you add a new VO, annotate every `Long` field that could hold a Snowflake ID.

## Architecture

- **Spring Boot 3.2.5** + **MyBatis-Plus 3.5.6** (no XML mapper files; `classpath:mapper/*.xml` configured but unused)
- **Controller → Service → Mapper**, DTO in / VO out, wrapped in `Result<T>(code,message,data)`
- **Pagination**: `PageResult<T>(total,records)` for book queries; `Page<Entity>` for admin list endpoints; backed by `PaginationInnerInterceptor`
- **Auth**: `AuthInterceptor` (`interceptor/` package) on `/api/user/info|password|profile`, `/api/address/**`, `/api/cart/**`, `/api/order/**`, `/api/review/add`, `/admin/**` — validates JWT (`Authorization: Bearer <token>`), stores user in `AuthContext` (ThreadLocal)
- **Admin**: `AdminInterceptor` checks `role=admin`
- **Pwd**: BCrypt via `spring-security-crypto` (no full Spring Security); `JWT`: HMAC-SHA384 (jjwt 0.12.5), 24h expiry, key at `jwt.secret` in `application.yml`
- **Orders state**: `pending → paid → shipped → delivered → completed`; `pending → cancelled`; `paid/shipped → refunded` (**defined but `refunded` transitions NOT implemented**)
- **Interceptors** live in `interceptor/` package, not `config/`

## Key constraints

- SQL `order` table must be backtick-quoted (MySQL reserved word)
- Default admin: `admin` / `123456` (BCrypt)
- `application-local.yml` (gitignored) overrides `spring.datasource.password` via `DB_PASSWORD` env var (empty default); use `application-local.yml.example` as template
- `@MapperScan("com.example.bookstore.mapper")` in `BookstoreApplication.java`
- `BaseEntity` provides `id` (Snowflake), `createTime`, `updateTime` (MyBatis-Plus `@TableField` auto-fill)

## Frontend pitfalls

- `OrderItemVO` uses `bookTitle` / `bookAuthor`, **not** `title` / `author` — templates often get this wrong
- `OrderVO` has flat `receiverName` / `receiverPhone` / `receiverAddress`, **not** `address.receiver`
- Order create endpoint expects `cartItemIds: [cartRecordId]`, **not** `items: [{bookId, quantity}]`
- Cart update endpoint expects `{id, quantity}` (cart record ID), **not** `{bookId, quantity}`
