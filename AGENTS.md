# AGENTS.md

## Commands

```bash
mvn spring-boot:run              # backend :8081
cd bookstore-frontend; npm run dev  # frontend :5173
mysql -u root -p < sql/init.sql  # init bookstore DB (12 tables + seed)
mvn test                          # pure JUnit5 + Mockito, no DB needed
mvn clean package -DskipTests     # full build
python test_smoke.py              # Playwright e2e (needs both servers)
```

No linter/formatter. CI = `mvn test` + `npm run build`.

DB password in `src/main/resources/application-local.yml` (copied from `.example`, gitignored).

## Snowflake ID (critical)

MyBatis-Plus 3.5.6 global default `IdType.ASSIGN_ID` generates 19-digit Longs → exceeds `Number.MAX_SAFE_INTEGER`. **Must stringify**:

| Layer | Rule |
|-------|------|
| **VO** | `@JsonSerialize(using = ToStringSerializer.class)` on Long ID fields (12 of 13 VOs; `BookChapterVO` has no Long fields; `BookVO.origPrice` is `BigDecimal` but also annotated) |
| **Entity** | `@JsonFormat(shape = Shape.STRING)` on `BaseEntity.id` (`common/` pkg, not `entity/`), `Book.authorId`/`categoryId`, `Category.parentId` |
| **DTO** | ID fields are `String` (`OrderCreateDTO.addressId`/`cartItemIds`, `CartUpdateDTO.id`, `AddressUpdateDTO.id`). Others (`StockAdjustDTO.stock`) are plain types. |
| **Service** | `Long.parseLong(dto.getId())` to convert back |

**Only real auto-increment exception**: `OrderItem.id` (`@TableId(type = IdType.AUTO)`). `BookChapter` has **no** `@TableId` annotation (uses default snowflake), but its VO exposes no Long IDs so precision loss is irrelevant.

`FavoriteController.add()` takes `Map<String, String>` (not `Map<String, Long>`) and calls `Long.parseLong(body.get("bookId"))`.

## Vite proxy

| Frontend path | Backend target | Rewrite |
|---------------|----------------|---------|
| `/api` | `localhost:8081/api` | passthrough |
| `/admin-api` | `localhost:8081/admin` | `/admin-api` → `/admin` |
| `/pictures` | `localhost:8081/pictures` | passthrough |

## Two axios instances

| File | baseURL | 401 |
|------|---------|-----|
| `src/api/index.js` | `/api` | `token=null; router.push('/login')` |
| `src/api/admin.js` | `/admin-api` | `token=null; window.location.href='/login'` |

Admin paths are relative (e.g. `/book/list` → `GET /admin/book/list`).

## Architecture

- **Controller → Service → Mapper**, DTO in / VO out, unified `Result<T>(code,message,data)`
- **Admin controllers** (`BookManageController`, `OrderManageController`, `UserManageController`, `ReviewManageController`) inject Mapper directly, return raw `Page<Entity>` (no VO). `CategoryManageController` mixes both: uses `CategoryService` for add/update/delete, `CategoryMapper` for `updateStatus`.
- **Interceptors**: `AuthInterceptor` on `/api/user/info`, `/api/user/password`, `/api/user/profile`, `/api/address/**`, `/api/cart/**`, `/api/favorite/**`, `/api/order/**`, `/api/review/add`, `/admin/**`. `AdminInterceptor` on `/admin/**` (checks admin role).
- **Password**: BCrypt via `spring-security-crypto` only (no full Spring Security)
- **CORS**: allows only `http://localhost:*`
- **Default admin**: `admin` / `123456`
- **Cover images**: `pictures/` dir served as `/pictures/**` static resource
- **Entry**: `BookstoreApplication.java`, `@MapperScan("com.example.bookstore.mapper")`
- **MyBatis-Plus ID config**: no global `id-type` override → default `ASSIGN_ID` (snowflake). `application.yml` enables `map-underscore-to-camel-case: true` + stdout SQL logging.

## Order state

```
created → paying → paid → shipped → delivered → completed
   ↓ (expired)  ↓ (cancel)
expired       cancelled
                  paid/shipped → refunded
                  delivered/completed → after_sale
```

- `book.sales` incremented atomically only in `OrderServiceImpl.pay()` (`UPDATE SET sales = sales + #{quantity}`)
- `cancel()` restores stock but **does NOT roll back** sales (only paid orders count)
- `applyAfterSale()` accepts `delivered` + `completed`
- `BookMapper` atomic ops: `decreaseStock` (with `WHERE stock >= #{quantity}` guard), `increaseSales`, `increaseFavoritedCount`, `decreaseFavoritedCount`, `updateStock`

## Frontend conventions (easy to get wrong)

| Where | Convention |
|-------|-----------|
| `OrderItemVO` | fields `bookTitle`/`bookAuthor` (not `title`/`author`) |
| `OrderVO` | flat `receiverName`/`receiverPhone`/`receiverAddress` (not nested `address`) |
| Cart update | `PUT {id, quantity}` (cart record ID, not `{bookId, quantity}`) |
| Order create | `POST {cartItemIds: [cartRecordId], addressId}` (not `{items: [{bookId,qty}]}`) |
| "Buy now" | `BookDetail.vue` → `/order/confirm?bookId=X&quantity=Y` → calls `cartStore.addToCart` then uses cart ID |
| Admin orders API | in `src/api/admin.js` (not `src/api/order.js`) |

## Gotchas

- **DDL `AUTO_INCREMENT` is a fallback only** — actual inserts use snowflake 19-digit IDs (except `OrderItem.id`)
- **No MetaObjectHandler**: `BaseEntity` has `@TableField(fill = ...)` but no implementation; `createTime`/`updateTime` rely on MySQL `DEFAULT CURRENT_TIMESTAMP` / `ON UPDATE CURRENT_TIMESTAMP`
- **`avgRating`**: computed live from `review` table at query time, not a DB column
- **`favorited_count`**: orchestrated by `FavoriteServiceImpl`, atomic SQL in `BookMapper` (`SET favorited_count = favorited_count +/- 1`)
- **`Address.entity`**: explicit `@TableField("user_id")` column mapping
- **`PaymentVO.amount`**: `String` (hand-written `.toString()`, not `@JsonSerialize`)
- **`OrderVO.paymentId`**: `Long` **without** `@JsonSerialize` (potential precision loss — `Payment` uses snowflake ID)
- **`BookVO.origPrice`**: `BigDecimal` but annotated with `@JsonSerialize(using = ToStringSerializer.class)` (non-standard for non-Long)
- **`BookChapterVO`**: no Long ID fields, no `@JsonSerialize`
- **`BookChapter`**: does **not** extend `BaseEntity`, has **no** `@TableId` (uses default snowflake)
- **`order` table**: must be backtick-quoted in SQL (`OrderMapper` raw `@Select` queries use `` `order` ``)
- **Ranking API** (`BookController.getRanking`): `type` (`sales`/`rating`/`new`/`collection`) works; `period` (`all`/`week`/`month`/`quarter`/`year`) only applies to `new` type
- **Pagination response**: user-facing controllers wrap via `PageResult<T>(total, records)`. Admin controllers return raw MyBatis-Plus `Page<Entity>` (many irrelevant fields, but Jackson only serializes what's accessible).
- **Revenue SQL** (`OrderMapper`): filters by `pay_time BETWEEN start AND end` AND status IN `('paid','shipped','delivered','completed','refunding','after_sale')`. NULL `pay_time` = excluded regardless of status. `pay()` must set `payTime`.
- **Review purchase validation** (`ReviewServiceImpl.add()`): when `orderItemId` is set, validates user owns the item, order is `delivered`/`completed`, and no duplicate review exists for that order_item_id. `review` table has `uk_order_item` unique constraint.
- **Admin refund page**: `/admin/refunds` route → `AdminRefund.vue`. 4 stat cards + 4 filter tabs (全部/退款中/售后中/已退款). "全部" fetches all 3 statuses in parallel, merges client-side. Default tab is `refunding` (退款中).
- **Admin refund/after-sale endpoints** (all in `OrderManageController`): `PUT /admin/order/{id}/refund`, `/approve-refund`, `/reject-refund`, `/approve-after-sale`, `/reject-after-sale`.
- **Stock adjustment**: `PUT /admin/book/{id}/stock` with `{stock: N}` via `BookManageController`. Uses `BookMapper.updateStock()` (atomic `UPDATE ... SET stock = N`). Admin route: `/admin/inventory` → `AdminInventory.vue`.
- **Review entity transient fields**: `Review` has `@TableField(exist = false)` fields `username` and `bookTitle` — populated post-query from `UserMapper`/`BookMapper` maps in `ReviewManageController.list()`. Same pattern: `Book.categoryName`.
- **Admin review/user search**: `ReviewManageController.list()` accepts `keyword`, `status`, `rating` params. `UserManageController.list()` accepts `keyword`, `status`, `role` params. Keyword search on reviews queries user/book tables for IDs since `username`/`bookTitle` are transient; user keyword searches `username LIKE`, `email LIKE`, `phone LIKE` directly.
