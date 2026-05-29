# AGENTS.md

## Commands

```bash
mvn spring-boot:run                   # backend :8081
cd bookstore-frontend; npm run dev    # frontend :5173
mysql -u root -p < sql/init.sql       # 12 tables + seed data
mvn test                              # JUnit5 + Mockito (no DB)
mvn clean package -DskipTests         # full build
python test_smoke.py                  # Playwright e2e (needs both servers)
```

DB password in `src/main/resources/application-local.yml` (copy from `.example`, gitignored). Default admin: `admin` / `123456`.

MiniMax API key uses `${MINIMAX_API_KEY}` env var (no default). Put actual key in `application-local.yml` (gitignored).

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

## AI 书友 (AI Assistant)

- **AIController** (`/api/ai/chat`, `/api/ai/chat/stream`): Uses MiniMax `chatcompletion_v2` with OpenAI-compatible format (`role`/`content`). Supports function calling.
- **Functions**: `searchBooks`, `getBookDetail`, `addToCart`, `getCartItems`, `listAddresses` — AI can search/recommend books AND execute cart/address operations on behalf of logged-in user.
- **Auth**: Frontend sends `Authorization` header via axios interceptor; backend `trySetAuth()` sets `AuthContext` per request. Functions like `addToCart` use `AuthContext.getCurrentUserId()`.
- **Floating widget**: `components/ai/AIFloatingWidget.vue` added to `App.vue` (draggable, fixed bottom-right). Same API as full AIAssistant page.
- **RestTemplate UTF-8**: `CorsConfig.restTemplate()` sets `StringHttpMessageConverter` charset to UTF-8 — required for Chinese characters in MiniMax requests.

## Community (书斋社区)

- **Entities**: `CommunityPost` (title/content/image/authorId), `CommunityLike` (postId/userId), `BookExcerpt` (bookId/excerpt/sourcePage/sourceUser)
- **API**: `GET /api/community/list` (paginated, with like count + liked status), `POST /api/community`, `POST /api/community/like` (toggle)
- **Admin**: `CommunityManageController` (injects Mapper directly) supports `PUT /admin-api/community/update` to modify status
- **Interceptors**: AuthInterceptor covers `/api/community/**` (JWT required for CRUD)

## Review purchase check

`ReviewServiceImpl.add()` checks `OrderItemMapper.countPurchased(userId, bookId) > 0` before allowing a review — user must have purchased the book.
