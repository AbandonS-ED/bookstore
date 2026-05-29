# AGENTS.md

## Commands

```bash
mvn spring-boot:run                   # backend :8081
cd bookstore-frontend; npm run dev    # frontend :5173
mysql -u root -p < sql/init.sql       # 14 tables + seed data
mvn test                              # JUnit5 + Mockito (no DB)
mvn clean package -DskipTests         # full build
```

DB password in `src/main/resources/application-local.yml` (copy from `.example`, gitignored). Default admin: `admin` / `123456`.

## Snowflake ID (critical)

MyBatis-Plus `ASSIGN_ID` generates 19-digit Longs that overflow `Number.MAX_SAFE_INTEGER`. **Must stringify**:

| Layer | Rule |
|-------|------|
| **VO** | `@JsonSerialize(using = ToStringSerializer.class)` on Long ID fields (12 of 13 VOs; `BookChapterVO` has no Longs) |
| **Entity** | `@JsonFormat(shape = Shape.STRING)` on `BaseEntity.id`, `Book.authorId`/`categoryId`, `Category.parentId` |
| **DTO** | ID fields as `String` (e.g., `OrderCreateDTO.addressId`/`cartItemIds`, `CartUpdateDTO.id`) |
| **Service** | `Long.parseLong(dto.getId())` to convert |

Exceptions: `OrderItem.id` = `IdType.AUTO`. `BookChapter` entity has no `@TableId` (independent entity, not inheriting `BaseEntity`). `FavoriteController.add()` uses `Map<String, String>` + `Long.parseLong()`.

## Vite proxy (`vite.config.js`)

| Frontend path | Backend target | Notes |
|---|---|---|
| `/api` | `localhost:8081/api` | passthrough |
| `/admin-api` | `localhost:8081/admin` | rewrite `/admin-api` → `/admin` |
| `/pictures` | `localhost:8081/pictures` | static resources |

## Two axios instances

| File | baseURL | 401 handler |
|------|---------|-------------|
| `src/api/index.js` | `/api` | clear token, `router.push('/login')` |
| `src/api/admin.js` | `/admin-api` | clear token, `window.location.href='/login'` |

Admin API paths are relative (e.g., `/book/list` → `GET /admin/book/list`).

## Architecture surprises

- **Admin controllers** (`BookManageController`, `OrderManageController`, `UserManageController`, `ReviewManageController`) inject **Mapper directly**, return raw `Page<Entity>` (no VO). `CategoryManageController` uses `CategoryService` for CRUD, `CategoryMapper` for `updateStatus`.
- **Interceptors**: `AuthInterceptor` on `/api/user/info|password|profile`, `/api/address/**`, `/api/cart/**`, `/api/favorite/**`, `/api/order/**`, `/api/review/add`, `/admin/**`. `AdminInterceptor` on `/admin/**`.
- **No MetaObjectHandler**: `BaseEntity` has `@TableField(fill = ...)` but no impl; relies on MySQL `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP`.
- **Order table**: must be backtick-quoted in raw SQL (`` `order` ``).
- **`BookChapter`** does **not** inherit `BaseEntity` (independently defined entity, no shared ID strategy).
- **Revenue SQL**: filters `pay_time >= #{start} AND pay_time < #{end}` + statuses `paid/shipped/delivered/completed/refunding/after_sale`. NULL `pay_time` excluded.

## Order state

```
created → paying → paid → shipped → delivered → completed
   ↓(expired)  ↓(cancel)
expired       cancelled
                paid/shipped → refunded
                delivered/completed → after_sale
```

- `book.sales` atomically incremented only in `OrderServiceImpl.pay()` (`UPDATE SET sales = sales + #{quantity}`)
- `cancel()` restores stock but does NOT roll back sales
- `BookMapper`: `decreaseStock` (with `WHERE stock >= #{quantity}` guard), `increaseSales`, `increaseFavoritedCount`, `decreaseFavoritedCount`, `updateStock`

## Frontend gotchas

| Where | Convention |
|-------|-----------|
| `OrderItemVO` | fields `bookTitle`/`bookAuthor` (not `title`/`author`) |
| `OrderVO` | flat `receiverName`/`receiverPhone`/`receiverAddress` (not nested `address`) |
| `PaymentVO.amount` | hand-stringified `.toString()` (not `@JsonSerialize`) |
| Cart update | `PUT {id, quantity}` — cart record ID, not `{bookId, quantity}` |
| Order create | `POST {cartItemIds: [cartRecordId], addressId}` |
| "Buy now" | `BookDetail.vue` → `/order/confirm?bookId=X&quantity=Y` → adds to cart then uses cart ID |
| Admin orders API | in `src/api/admin.js` (not `src/api/order.js`) |

## Admin search quirks

- **Refund page**: `AdminRefund.vue` default tab is `refunding` (退款中). "全部" fetches all 3 statuses in parallel, merges client-side.
- **Review search**: keyword queries user/book tables for IDs first (since `username`/`bookTitle` are transient fields).
- **User search**: keyword searches `username LIKE`, `email LIKE`, `phone LIKE` directly.
- **Stock adjustment**: `PUT /admin/book/{id}/stock {stock: N}` via `BookManageController` (atomic `UPDATE ... SET stock = N`).

## Ranking API (`BookController.getRanking`)

- `type`: `sales` / `rating` / `new` / `collection`
- `period`: `all` / `week` / `month` / `quarter` / `year` — only applies to `new` type
- `avgRating` computed live from `review` table, not a DB column

## 限时折扣 (Discount)

`Book.discountPrice` + `Book.discountEndTime`. `BookServiceImpl.isOnDiscount()` (public static) used in `OrderServiceImpl.create()` to pick discountPrice vs price. VO fields: `discountPrice`, `discountEndTime`, `onDiscount`. API: `GET /api/book/discounted`. Tag filter: `discount` for `GET /api/book/list?tag=discount`.

## Community feature

- **Tables**: `community_post` (extends `BaseEntity`), `community_like` (separate entity for like tracking)
- **User API**: `GET /api/community/list`, `GET /api/community/{id}`, `POST /api/community/add`, `PUT /api/community/update`, `DELETE /api/community/{id}`, `POST /api/community/like/{id}`
- **Admin API**: `GET /admin/community/list`, `DELETE /admin/community/{id}` — admin uses `CommunityManageController` (injects both `CommunityPostService` and `CommunityPostMapper`)
- **Frontend**: `AdminCommunity.vue` in admin views; user-facing community views in `Explore.vue`
- `CommunityPost.bookId` is optional (nullable Long); `bookTitle` is `@TableField(exist = false)` transient

## AI Assistant (MiniMax)

- **Backend**: `AIController` at `/api/ai/chat` (sync) and `/api/ai/chat/stream` (SSE)
- **Model**: `MiniMax-M2.7` via `https://api.minimax.chat/v1/text/chatfunction_v2`
- **Config**: `minimax.api.key` and `minimax.api.url` in `application.yml` (key is committed, not gitignored)
- **Tools**: `searchBooks`, `getBookDetail`, `addToCart`, `getCartItems`, `listAddresses`
- **Auth**: optional — tries `Authorization` header, sets `AuthContext` if valid JWT; guests can search/browse
- **Frontend**: `AIAssistant.vue` + `src/api/ai.js` (both `chat()` and `chatStream()` with SSE parsing)
- **SSE quirk**: reply is chunked 3 chars at a time with 15ms sleep (simulated streaming)
- **Book refs**: after reply, scans all books for title mentions, returns up to 5 as `books` field in response
