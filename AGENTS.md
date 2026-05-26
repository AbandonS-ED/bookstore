# AGENTS.md

## Quick start

```
mysql -u root -p bookstore < sql/init.sql       # 12 tables InnoDB + seed data
mvn spring-boot:run                              # backend :8081
cd bookstore-frontend; npm install && npm run dev # frontend :5173
```

DB password in `src/main/resources/application-local.yml` (copy from `.example`, gitignored). Default admin: `admin` / `123456`. MiniMax API key: env `MINIMAX_API_KEY` or `application-local.yml`.

## Commands

| Command | What |
|---------|------|
| `mvn spring-boot:run` | Backend on :8081 |
| `mvn test` | JUnit5 + Mockito (no Spring context, no DB) |
| `mvn clean package -DskipTests` | Full backend build |
| `cd bookstore-frontend && npm run dev` | Vite dev :5173 |
| `cd bookstore-frontend && npm run build` | Frontend production |
| `python test_smoke.py` | Playwright e2e (needs both servers) |

No CI/CD, no Swagger, no integration tests.

## Snowflake ID (critical)

MyBatis-Plus `ASSIGN_ID` generates 19-digit Longs that overflow `Number.MAX_SAFE_INTEGER`. **Must stringify** on every Java→JS crossing:

| Layer | Rule |
|-------|------|
| **VO** | `@JsonSerialize(using = ToStringSerializer.class)` on all Long ID fields (13 VOs). Exceptions: `BookChapterVO` has no Longs; `PaymentVO.amount` is `String` type (hand-stringified `.toString()`). |
| **Entity** | `@JsonFormat(shape = Shape.STRING)` on `BaseEntity.id`, `Book.authorId`/`categoryId`, `Category.parentId`, `Order.userId`/`addressId`/`paymentId`, `Review.userId`/`bookId`/`orderItemId` |
| **DTO** | ID fields as `String` (e.g. `OrderCreateDTO.addressId`/`cartItemIds`, `CartUpdateDTO.id`, `BookQueryDTO.categoryId`) |
| **Service** | `Long.parseLong(dto.getId())` to convert |

Exceptions: `OrderItem.id` = `IdType.AUTO` (only non-Snowflake entity). `BookChapter` has **no `@TableId`** (does not inherit `BaseEntity`). `FavoriteController.add()` uses `Map<String, String>` + `Long.parseLong()`.

## Vite proxy (`vite.config.js`)

| Frontend | Target | Rewrite |
|----------|--------|---------|
| `/api` | `localhost:8081/api` | passthrough |
| `/admin-api` | `localhost:8081/admin` | strip `/admin-api` → `/admin` |
| `/pictures` | `localhost:8081/pictures` | passthrough |

## Two axios instances

| File | baseURL | 401 handler |
|------|---------|-------------|
| `src/api/index.js` | `/api` | clear token → `router.push('/login')` |
| `src/api/admin.js` | `/admin-api` | clear token → `window.location.href='/login'` |

Admin paths are relative (e.g. `/book/list` → `GET /admin/book/list`).

## Architecture surprises

- **Admin controllers** inject **Mapper directly**, return raw `Page<Entity>` (no VO). `CategoryManageController` is the exception (uses `CategoryService` for CRUD, `CategoryMapper` for `updateStatus`).
- **Interceptors**: `AuthInterceptor` covers `/api/user/info|password|profile`, `/api/address/**`, `/api/cart/**`, `/api/favorite/**`, `/api/order/**`, `/api/review/add`, `/api/ai/**`, `/admin/**`. `AdminInterceptor` covers `/admin/**`.
- **No `MetaObjectHandler`**: `BaseEntity` has `@TableField(fill = ...)` but **no impl** — relies on MySQL `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP`.
- **No Spring Security web**: only `spring-security-crypto` for BCrypt. Auth is custom via `AuthInterceptor` + `JwtUtils` + `AuthContext` (ThreadLocal).
- **`order` table**: must be backtick-quoted in raw SQL (`` `order` ``).
- **Static files**: `pictures/` at project root (not `src/main/resources/`) served via `WebMvcConfig`.
- **Book status**: `0`=off, `1`=on, `2`=pre-sale, `3`=coming-soon (`Constants.java`).
- **Revenue SQL** (`OrderMapper`): filters `pay_time >= #{start} AND pay_time < #{end}` + statuses `paid/shipped/delivered/completed/refunding/after_sale`. NULL `pay_time` excluded.
- **`BookServiceImpl.isOnDiscount()`** is `public static` — called by `OrderServiceImpl.create()` to pick `discountPrice` vs `price`.
- **`decreaseStock`** has `WHERE stock >= #{quantity}` guard (no overselling). `cancel()` restores stock but does NOT decrement sales.
- **`Review` entity**: `username`/`bookTitle` are `@TableField(exist = false)` transient fields (not stored in DB).
- **`StockAdjustDTO`**: admin 库存调整的请求 DTO，含 `@NotNull` + `@PositiveOrZero` 校验。

## Order state

```
created → paying → paid → shipped → delivered → completed
   ↓(expired)  ↓(cancel)
expired       cancelled
                paid/shipped → refunded
                delivered/completed → after_sale
```

Order status constants (`String`): `created`, `paying`, `paid`, `shipped`, `delivered`, `completed`, `cancelled`, `expired`, `refunding`, `after_sale`, `refunded`.

## Frontend gotchas

| Where | Convention |
|-------|-----------|
| `CartVO`, `OrderItemVO` | fields `bookTitle`/`bookAuthor` (not `title`/`author`) |
| `OrderVO` | flat `receiverName`/`receiverPhone`/`receiverAddress` |
| `PaymentVO.amount` | `String` type, hand-stringified `.toString()` |
| Cart update | `PUT {id, quantity}` — `id` is cart record ID, not `{bookId, quantity}` |
| Order create | `POST {cartItemIds: [cartRecordId], addressId}` |
| "Buy now" | `BookDetail.vue` → `/order/confirm?bookId=X&quantity=Y` → adds to cart then uses cart ID |
| Admin orders API | in `src/api/admin.js` (not `src/api/order.js`) |

## Admin search quirks

- **Refund page**: `AdminRefund.vue` default tab = `refunding`. "全部" fetches all 3 statuses in parallel, merges client-side.
- **Review search**: keyword queries `user`/`book` tables for IDs first (since `username`/`bookTitle` are transient fields).
- **User search**: keyword searches `username LIKE`, `email LIKE`, `phone LIKE` directly.
- **Stock adjustment**: `PUT /admin/book/{id}/stock {stock: N}` via `BookManageController` (atomic `UPDATE ... SET stock = N`).

## Ranking API (`BookController.getRanking`)

- `type`: `sales` / `rating` / `new` / `collection`
- `period`: `all` / `week` / `month` / `quarter` / `year` — only applies to `new` type
- `avgRating` computed live from `review` table (not a stored column)

## Discount

`Book.discountPrice` + `Book.discountEndTime`. Static helper `BookServiceImpl.isOnDiscount()`. VO fields: `discountPrice`, `discountEndTime`, `onDiscount`. API: `GET /api/book/discounted`. Tag filter: `discount` for `GET /api/book/list?tag=discount`.

## AI 书友 (`/ai-assistant`)

- **Route**: `/ai-assistant` registered in `router/index.js`. Linked from `AppHeader.vue` nav icon (robot SVG).
- **Backend**: `AIController.java` (largest file, ~500 lines). Two endpoints: `POST /api/ai/chat` (sync) and `POST /api/ai/chat/stream` (SSE). MiniMax Anthropic-compatible API with function calling (max 5 tool iterations).
- **Frontend**: `AIAssistant.vue` + `src/api/ai.js`. SSE stream consumed via `fetch` + `ReadableStream.getReader()`. `chatMessages` filter strips AI replies before sending (only user messages go to API).
- **Auth**: `/api/ai/**` requires login (`AuthInterceptor`).
- **SSE format**: Spring `SseEmitter` sends `event:done` and `data:..` **without trailing space** after colon. Frontend parser handles both `event:` and `data:` with optional space.
- **Vue reactivity**: AI message objects must be `reactive()` — `ref` arrays track `push/splice` but NOT in-place property changes on array elements. Without `reactive`, streaming content only appears on next user input.
- **5 tools**: `searchBooks`, `getBookDetail`, `addToCart`, `getCartItems`, `listAddresses`.
- **Config** (`application.yml`):
  ```yaml
  minimax:
    api:
      key: ${MINIMAX_API_KEY:}
      url: https://api.minimaxi.com/anthropic/v1/messages
  ```

## Router guard notes

- `meta: { guest: true }` on Login/Register redirects logged-in users to Home.
- `meta: { requiresAuth: true }` checks `userStore.isLoggedIn`.
- `meta: { requiresAdmin: true }` on `/admin` and children calls `userStore.getUserInfo()` and checks `role !== 'admin'`.

## Book covers

`CoverStyle.getCoverStyle()` generates colored backgrounds with book title text overlay. Covers are NOT real images — just CSS gradients with text. Books without covers render as fallback with title text.
