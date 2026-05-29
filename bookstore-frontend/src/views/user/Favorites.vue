<template>
  <div class="favorites-page">
    <section class="page-header">
      <div class="ph-inner">
        <div class="ph-left">
          <h1>♡ 我的收藏</h1>
          <p>你收藏的好书都在这里，随时下单</p>
        </div>
        <div class="ph-stats">
          <div class="phs">
            <div class="phs-num">{{ books.length }}</div>
            <div class="phs-label">全部</div>
          </div>
          <div class="phs">
            <div class="phs-num">{{ priceDropCount }}</div>
            <div class="phs-label">有降价</div>
          </div>
          <div class="phs">
            <div class="phs-num">{{ lowStockCount }}</div>
            <div class="phs-label">库存紧张</div>
          </div>
        </div>
      </div>
    </section>

    <div class="toolbar">
      <div class="tl">
        <div class="select-all-wrap" @click="toggleSelectAll">
          <div :class="['custom-checkbox', { checked: selectedIds.size === visibleBooks.length && visibleBooks.length > 0 }]"></div>
          <span>全选</span>
        </div>
        <div :class="['batch-actions', { show: selectedIds.size > 0 }]">
          <button class="batch-btn primary" @click="batchAddCart">🛒 加入购物车</button>
          <button class="batch-btn danger" @click="batchRemove">✕ 取消收藏</button>
        </div>
        <div class="filter-pills">
          <div :class="['filter-pill', { active: currentFilter === 'all' }]" @click="currentFilter = 'all'">全部</div>
          <div :class="['filter-pill', { active: currentFilter === 'pricedrop' }]" @click="currentFilter = 'pricedrop'">有降价</div>
          <div :class="['filter-pill', { active: currentFilter === 'lowstock' }]" @click="currentFilter = 'lowstock'">库存紧张</div>
          <div :class="['filter-pill', { active: currentFilter === 'instock' }]" @click="currentFilter = 'instock'">有货</div>
        </div>
      </div>
      <div class="tr">
        <div class="search-mini">
          <span class="si">🔍</span>
          <input type="text" placeholder="搜索收藏..." v-model="searchQuery" @input="renderBooks">
        </div>
        <div class="sort-select">
          <label>排序</label>
          <select v-model="sortBy" @change="renderBooks">
            <option value="recent">最近收藏</option>
            <option value="price-asc">价格↑</option>
            <option value="price-desc">价格↓</option>
            <option value="discount">折扣力度</option>
            <option value="rating">评分</option>
          </select>
        </div>
        <div class="view-toggle">
          <div :class="['view-btn', { active: viewMode === 'grid' }]" @click="viewMode = 'grid'">▦</div>
          <div :class="['view-btn', { active: viewMode === 'list' }]" @click="viewMode = 'list'">☰</div>
        </div>
      </div>
    </div>

    <div class="books-area">
      <div v-if="loading" class="loading-grid">
        <div v-for="i in 10" :key="i" class="book-card is-skeleton">
          <div class="book-cover"><div class="skeleton" style="width:100%;height:100%"></div></div>
          <div class="book-info">
            <div class="skeleton" style="height:20px;width:80%;margin-bottom:8px"></div>
            <div class="skeleton" style="height:14px;width:50%;margin-bottom:12px"></div>
            <div class="skeleton" style="height:18px;width:40%"></div>
          </div>
        </div>
      </div>
      <div v-else-if="visibleBooks.length === 0" class="empty-state show">
        <div class="empty-icon">♡</div>
        <h3>还没有收藏任何书籍</h3>
        <p>浏览书籍时点击爱心，把心仪的好书收藏到这里</p>
        <router-link to="/books" class="btn-discover">去逛逛 →</router-link>
      </div>
      <div v-else :class="['books-grid', { 'list-view': viewMode === 'list' }]">
        <div
          v-for="(book, idx) in visibleBooks"
          :key="book.id"
          :class="['book-card', { selected: selectedIds.has(book.bookId) }]"
          :style="{ animation: `fadeUp 0.4s ease ${idx * 0.03}s forwards` }"
          @click="toggleSelect(book.bookId)"
        >
          <div :class="['card-check', { checked: selectedIds.has(book.bookId) }]" @click.stop="toggleSelect(book.bookId)"></div>
          <button class="card-unfav" @click.stop="unfav(book.bookId)" title="取消收藏">♥</button>
          <div class="book-cover" @click.stop="goToBook(book.bookId)">
            <img v-if="book.coverUrl && !coverErrors[book.bookId]" class="book-cover-img" :src="book.coverUrl" @error="coverErrors[book.bookId] = true" />
            <div v-if="!book.coverUrl || coverErrors[book.bookId]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(book.bookId)">
              <span class="cover-title">{{ book.title }}</span>
              <span class="cover-author">{{ book.author }}</span>
            </div>
            <div class="cover-badges">
              <span v-if="book.favoritedPrice && book.price < book.favoritedPrice" class="badge-price-drop">↓ 降价</span>
              <span v-if="book.stock <= 5" class="badge-low-stock">仅剩 {{ book.stock }} 本</span>
              <span v-else-if="!(book.favoritedPrice && book.price < book.favoritedPrice)" class="badge-in-stock">有货</span>
            </div>
            <div v-if="book.favoritedPrice && book.price < book.favoritedPrice" class="price-alert">🔔 比收藏时便宜了 ¥{{ (book.favoritedPrice - book.price).toFixed(0) }}</div>
          </div>
          <div class="book-info" @click.stop="goToBook(book.bookId)">
            <div class="bi-title">{{ book.title }}</div>
            <div class="bi-author">{{ book.author }}</div>
            <div class="bi-price-row">
              <span class="bi-price">¥{{ book.price }}</span>
              <span v-if="book.origPrice" class="bi-original">¥{{ book.origPrice }}</span>
              <span v-if="book.origPrice" class="bi-discount">-{{ Math.round((1 - book.price / book.origPrice) * 100) }}%</span>
            </div>
            <div class="bi-meta">
              <div class="bi-rating">
                <template v-if="book.avgRating">
                  <span class="stars">{{ renderStars(book.avgRating) }}</span>
                  <span>{{ book.avgRating.toFixed(1) }}</span>
                </template>
                <span v-else class="no-rating">暂无评分</span>
              </div>
              <div v-if="book.createTime" class="bi-fav-date">收藏于 {{ formatDate(book.createTime) }}</div>
            </div>
            <div class="card-btns">
              <button v-if="inCartIds.has(book.bookId)" class="card-btn in-cart" @click.stop>
                <span>✓</span> 已在购物车
              </button>
              <button v-else class="card-btn cart-btn" @click.stop="addToCart(book)">
                <span>🛒</span> 加入购物车
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="summary-bar">
      <div class="summary-inner">
        <div class="sum-left">
          <div class="sum-item"><span class="s-label">已选</span><span class="s-val" id="sumSel">{{ selectedIds.size }}</span><span class="s-label">本</span></div>
          <div class="sum-item"><span class="s-label">合计</span><span class="s-val accent">{{ selectedTotal }}</span></div>
          <div class="sum-item"><span class="s-label">已省</span><span class="s-val red">{{ selectedSaved }}</span></div>
        </div>
        <div class="sum-right">
          <button class="btn-sum outline" @click="addAllToCart">全部加入购物车</button>
          <button class="btn-sum filled" @click="batchAddCart">选中加入购物车</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { favoriteApi } from '@/api/favorite'
import { cartApi } from '@/api/cart'
import { useCartStore } from '@/stores/cart'
import { useFavoriteStore } from '@/stores/favorite'
import { getCoverStyle } from '@/utils/cover'

const router = useRouter()
const cartStore = useCartStore()
const favoriteStore = useFavoriteStore()

const books = ref([])
const loading = ref(true)
const selectedIds = ref(new Set())
const currentFilter = ref('all')
const searchQuery = ref('')
const sortBy = ref('recent')
const viewMode = ref('grid')
const inCartIds = ref(new Set())
const coverErrors = ref({})

const lowStockCount = computed(() => books.value.filter(b => b.stock > 0 && b.stock <= 5).length)
const priceDropCount = computed(() => books.value.filter(b => b.favoritedPrice && b.price < b.favoritedPrice).length)

const selectedTotal = computed(() => {
  const sel = books.value.filter(b => selectedIds.value.has(b.bookId))
  return '¥' + sel.reduce((s, b) => s + b.price, 0).toFixed(2)
})
const selectedSaved = computed(() => {
  const sel = books.value.filter(b => selectedIds.value.has(b.bookId))
  return '¥' + sel.reduce((s, b) => s + (b.origPrice ? b.origPrice - b.price : 0), 0).toFixed(2)
})

const visibleBooks = computed(() => {
  let list = [...books.value]
  const q = searchQuery.value.trim().toLowerCase()
  if (q) {
    list = list.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q))
  }
  if (currentFilter.value === 'pricedrop') {
    list = list.filter(b => b.favoritedPrice && b.price < b.favoritedPrice)
  } else if (currentFilter.value === 'lowstock') {
    list = list.filter(b => b.stock > 0 && b.stock <= 5)
  } else if (currentFilter.value === 'instock') {
    list = list.filter(b => b.stock > 0)
  }
  switch (sortBy.value) {
    case 'price-asc': list.sort((a, b) => a.price - b.price); break
    case 'price-desc': list.sort((a, b) => b.price - a.price); break
    case 'discount': list.sort((a, b) => {
      const da = a.origPrice ? 1 - a.price / a.origPrice : 0
      const db = b.origPrice ? 1 - b.price / b.origPrice : 0
      return db - da
    }); break
    case 'rating': list.sort((a, b) => (b.avgRating || 0) - (a.avgRating || 0)); break
  }
  return list
})

function renderStars(rating) {
  if (!rating) return ''
  const full = Math.floor(rating)
  const half = rating - full >= 0.5
  let s = ''
  for (let i = 0; i < full; i++) s += '★'
  if (half) s += '☆'
  const empty = 5 - full - (half ? 1 : 0)
  for (let i = 0; i < empty; i++) s += '☆'
  return s
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

function toggleSelect(bookId) {
  const s = new Set(selectedIds.value)
  s.has(bookId) ? s.delete(bookId) : s.add(bookId)
  selectedIds.value = s
}

function toggleSelectAll() {
  if (selectedIds.value.size === visibleBooks.value.length) {
    selectedIds.value = new Set()
  } else {
    selectedIds.value = new Set(visibleBooks.value.map(b => b.bookId))
  }
}

async function addToCart(book) {
  try {
    await cartStore.addToCart(book.bookId, 1)
    inCartIds.value.add(book.bookId)
    ElMessage.success('已加入购物车')
  } catch {
    ElMessage.error('加入购物车失败')
  }
}

async function addAllToCart() {
  const toAdd = visibleBooks.value
    .filter(b => b.stock > 0 && !inCartIds.value.has(b.bookId))
    .map(b => b.bookId)
  if (toAdd.length === 0) {
    ElMessage.warning('全部已在购物车中')
    return
  }
  try {
    for (const bookId of toAdd) {
      await cartApi.add({ bookId, quantity: 1 })
      inCartIds.value.add(bookId)
    }
    await cartStore.getCartList()
    ElMessage.success(`${toAdd.length} 本已加入购物车`)
  } catch {
    ElMessage.error('操作失败')
  }
}

async function batchAddCart() {
  const toAdd = [...selectedIds.value].filter(id => !inCartIds.value.has(id))
  if (toAdd.length === 0) {
    ElMessage.warning('所选书籍已在购物车中')
    return
  }
  try {
    for (const bookId of toAdd) {
      await cartApi.add({ bookId, quantity: 1 })
      inCartIds.value.add(bookId)
    }
    await cartStore.getCartList()
    selectedIds.value = new Set()
    ElMessage.success(`${toAdd.length} 本已加入购物车`)
  } catch {
    ElMessage.error('操作失败')
  }
}

async function unfav(bookId) {
  try {
    await favoriteApi.remove(bookId)
    books.value = books.value.filter(b => b.bookId !== bookId)
    const s = new Set(selectedIds.value)
    s.delete(bookId)
    selectedIds.value = s
    favoriteStore.favoriteIds = favoriteStore.favoriteIds.filter(id => id !== String(bookId))
    ElMessage.success('已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}

async function batchRemove() {
  try {
    for (const bookId of selectedIds.value) {
      await favoriteApi.remove(bookId)
    }
    books.value = books.value.filter(b => !selectedIds.value.has(b.bookId))
    favoriteStore.favoriteIds = favoriteStore.favoriteIds.filter(id => !selectedIds.value.has(id))
    selectedIds.value = new Set()
    ElMessage.success(`已取消收藏`)
  } catch {
    ElMessage.error('操作失败')
  }
}

function goToBook(id) {
  router.push(`/book/${id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    coverErrors.value = {}
    const [favRes] = await Promise.all([
      favoriteApi.list(),
      cartStore.getCartList()
    ])
    books.value = favRes.data || []
    inCartIds.value = new Set(cartStore.items.map(item => String(item.bookId)))
  } catch {
    ElMessage.error('获取收藏列表失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-header {
  background: var(--color-primary-dark);
  position: relative;
  overflow: hidden;
}
.page-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse at 75% 35%, rgba(192,154,75,0.06) 0%, transparent 50%),
              radial-gradient(ellipse at 20% 80%, rgba(92,68,52,0.15) 0%, transparent 45%);
  pointer-events: none;
}
.page-header::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(90deg, transparent, transparent 90px, rgba(192,154,75,0.012) 90px, rgba(192,154,75,0.012) 91px);
  pointer-events: none;
}
.ph-inner {
  position: relative;
  z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 44px 40px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
}
.ph-left h1 {
  font-family: var(--font-display);
  font-size: 2rem;
  color: var(--color-bg-warm);
  font-weight: 900;
  margin-bottom: 6px;
}
.ph-left p {
  font-size: 0.9rem;
  color: rgba(237,230,214,0.4);
}
.ph-stats { display: flex; gap: 16px; }
.phs {
  background: rgba(237,230,214,0.04);
  border: 1px solid rgba(237,230,214,0.06);
  border-radius: 10px;
  padding: 14px 22px;
  text-align: center;
  min-width: 80px;
}
.phs-num {
  font-family: var(--font-display);
  font-size: 1.35rem;
  font-weight: 900;
  color: var(--color-accent-light);
  line-height: 1;
}
.phs-label {
  font-size: 0.72rem;
  color: rgba(237,230,214,0.35);
  margin-top: 4px;
}

.toolbar {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 16px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  flex-wrap: wrap;
  border-bottom: 1px solid var(--color-divider);
}
.tl { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.select-all-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.84rem;
  color: var(--color-text-secondary);
  cursor: pointer;
  user-select: none;
}
.custom-checkbox {
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-divider-strong);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;
}
.custom-checkbox.checked {
  background: var(--color-primary);
  border-color: var(--color-primary);
}
.custom-checkbox.checked::after {
  content: '✓';
  font-size: 0.7rem;
  color: var(--color-bg-warm);
}
.batch-actions {
  display: flex;
  gap: 8px;
  opacity: 0;
  width: 0;
  overflow: hidden;
  transition: all 0.3s;
}
.batch-actions.show { opacity: 1; width: auto; }
.batch-btn {
  padding: 6px 14px;
  border: 1px solid var(--color-divider-strong);
  border-radius: 6px;
  font-size: 0.78rem;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
  font-family: var(--font-body);
}
.batch-btn:hover { border-color: var(--color-primary-mid); color: var(--color-text); }
.batch-btn.primary {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
}
.batch-btn.danger:hover { border-color: var(--color-red); color: var(--color-red); background: var(--color-red-bg); }
.filter-pills { display: flex; gap: 6px; }
.filter-pill {
  padding: 5px 14px;
  border: 1px solid var(--color-divider-strong);
  border-radius: 18px;
  font-size: 0.78rem;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--color-text-secondary);
  background: var(--color-bg-card);
}
.filter-pill:hover { border-color: var(--color-primary-mid); }
.filter-pill.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
}
.tr { display: flex; align-items: center; gap: 12px; }
.search-mini {
  display: flex;
  align-items: center;
  background: var(--color-bg-cream);
  border: 1px solid var(--color-divider);
  border-radius: 8px;
  padding: 6px 12px;
  gap: 6px;
  transition: all 0.2s;
}
.search-mini:focus-within {
  border-color: var(--color-primary-mid);
  box-shadow: 0 0 0 2px rgba(74,53,38,0.05);
}
.search-mini input {
  background: transparent;
  border: none;
  outline: none;
  font-size: 0.84rem;
  width: 140px;
  font-family: var(--font-body);
  color: var(--color-text);
}
.search-mini input::placeholder { color: var(--color-text-light); }
.search-mini .si { color: var(--color-text-light); font-size: 0.85rem; }
.sort-select {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
}
.sort-select label { font-size: 0.8rem; color: var(--color-text-light); white-space: nowrap; }
.sort-select select {
  appearance: none;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider-strong);
  border-radius: 6px;
  padding: 7px 32px 7px 12px;
  font-size: 0.84rem;
  font-family: var(--font-body);
  color: var(--color-text);
  cursor: pointer;
  outline: none;
}
.sort-select::after {
  content: '▾';
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 0.7rem;
  color: var(--color-text-light);
  pointer-events: none;
}

.books-area {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 20px 40px 24px;
}
.loading-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
}
.book-card.is-skeleton {
  pointer-events: none;
}
.book-card.is-skeleton .book-cover {
  aspect-ratio: 4/5;
  background: var(--color-divider);
}
.skeleton {
  background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 6px;
}
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

.books-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 22px;
}
.book-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  position: relative;
  opacity: 0;
}
.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 14px 36px var(--color-shadow-heavy);
  border-color: rgba(192,154,75,0.15);
}
.book-card.selected {
  border-color: var(--color-accent);
  box-shadow: 0 0 0 2px var(--color-accent-glow);
}
.card-check {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 5;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  background: rgba(46,31,21,0.4);
  backdrop-filter: blur(8px);
  border: 2px solid rgba(237,230,214,0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  opacity: 0;
  transform: scale(0.8);
}
.book-card:hover .card-check,
.book-card.selected .card-check { opacity: 1; transform: scale(1); }
.book-card.selected .card-check {
  background: var(--color-accent);
  border-color: var(--color-accent);
}
.book-card.selected .card-check.checked::after {
  content: '✓';
  font-size: 0.7rem;
  color: var(--color-primary-abyss);
  font-weight: 700;
}
.card-unfav {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 5;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: rgba(46,31,21,0.45);
  backdrop-filter: blur(10px);
  border: none;
  color: var(--color-accent);
  font-size: 0.9rem;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.85);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.book-card:hover .card-unfav { opacity: 1; transform: scale(1); }
.card-unfav:hover { background: var(--color-red); color: #fff; transform: scale(1.1) !important; }
.book-cover {
  position: relative;
  aspect-ratio: 4/5;
  overflow: hidden;
}
.book-cover-img {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.15rem;
  color: rgba(237,230,214,0.85);
  padding: 28px;
  text-align: center;
  line-height: 1.4;
  transition: transform 0.4s ease;
}
.book-card:hover .book-cover-img { transform: scale(1.04); }
.book-cover-img img { width: 100%; height: 100%; object-fit: cover; }
.book-cover-fallback { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; text-align: center; box-shadow: 0 30px 60px rgba(0,0,0,0.35), inset 0 1px 0 rgba(192,154,75,0.08); border-left: 3px solid rgba(192,154,75,0.12); border-radius: 6px; }
.cover-title { font-family: var(--font-display); font-weight: 700; font-size: 1.6rem; color: rgba(237,230,214,0.85); text-shadow: 0 1px 4px rgba(0,0,0,0.2); word-break: break-all; line-height: 1.3; margin-bottom: 10px; }
.cover-author { font-size: .8rem; color: rgba(237,230,214,0.5); text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.cover-badges {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px 12px;
  background: linear-gradient(transparent, rgba(28,18,12,0.6));
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 6px;
}
.badge-price-drop {
  font-size: 0.68rem;
  font-weight: 700;
  padding: 3px 8px;
  border-radius: 4px;
  background: var(--color-red);
  color: #fff;
}
.price-alert {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 6px 12px;
  background: linear-gradient(90deg, var(--color-green), #4A7A44);
  font-size: 0.72rem;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  transform: translateY(100%);
  transition: transform 0.3s;
}
.book-card:hover .price-alert {
  transform: translateY(0);
}
.badge-low-stock {
  font-size: 0.65rem;
  padding: 3px 8px;
  border-radius: 4px;
  background: rgba(192,120,48,0.1);
  color: var(--color-orange);
  border: 1px solid rgba(192,120,48,0.2);
}
.badge-in-stock {
  font-size: 0.65rem;
  color: rgba(237,230,214,0.5);
}
.book-info { padding: 14px 16px 16px; }
.bi-title {
  font-family: var(--font-display);
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 2px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.bi-author {
  font-size: 0.76rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
}
.bi-price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-bottom: 8px;
}
.bi-price {
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-accent-muted);
}
.bi-original {
  font-size: 0.76rem;
  color: var(--color-text-light);
  text-decoration: line-through;
}
.bi-discount {
  font-size: 0.65rem;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--color-red-bg);
  color: var(--color-red);
}
.bi-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.bi-rating { display: flex; align-items: center; gap: 2px; font-size: .72rem; color: var(--color-accent); }
.bi-rating .stars { font-size: .7rem; }
.bi-rating span { color: var(--color-text-light); margin-left: 2px; }
.bi-rating .no-rating { font-size: .65rem; font-style: italic; color: var(--color-text-muted); margin-left: 0; }
.bi-fav-date { font-size: .7rem; color: var(--color-text-light); }
.card-btns { display: flex; gap: 6px; }
.card-btn {
  flex: 1;
  padding: 8px 0;
  text-align: center;
  border-radius: 6px;
  font-size: 0.78rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid var(--color-divider-strong);
  background: var(--color-bg-cream);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-family: var(--font-body);
}
.card-btn:hover { border-color: var(--color-primary-mid); color: var(--color-text); }
.card-btn.cart-btn {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
}
.card-btn.cart-btn:hover { background: var(--color-primary-mid); }
.card-btn.in-cart {
  background: #5C8856;
  color: #fff;
  border-color: #5C8856;
  cursor: default;
}

.view-toggle { display: flex; gap: 4px; }
.view-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--color-divider-strong);
  border-radius: 6px;
  background: var(--color-bg-card);
  color: var(--color-text-light);
  font-size: .85rem;
  cursor: pointer;
  transition: all .2s;
}
.view-btn:hover { border-color: var(--color-primary-mid); color: var(--color-text); }
.view-btn.active { background: var(--color-primary); border-color: var(--color-primary); color: var(--color-bg-warm); }

.summary-bar { max-width: var(--max-width); margin: 0 auto; padding: 0 40px 28px; }
.summary-inner {
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: 10px;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}
.sum-left { display: flex; align-items: center; gap: 28px; }
.sum-item { display: flex; align-items: baseline; gap: 5px; }
.sum-item .s-label { font-size: .82rem; color: var(--color-text-light); }
.sum-item .s-val { font-family: var(--font-display); font-size: 1.2rem; font-weight: 700; color: var(--color-text); }
.sum-item .s-val.accent { color: var(--color-accent-muted); }
.sum-item .s-val.red { color: var(--color-red); }
.sum-right { display: flex; gap: 10px; }
.btn-sum {
  padding: 10px 22px;
  border-radius: 8px;
  font-size: .86rem;
  font-weight: 600;
  cursor: pointer;
  transition: all .25s;
  border: none;
  font-family: var(--font-body);
}
.btn-sum.outline { background: transparent; border: 1.5px solid var(--color-divider-strong); color: var(--color-text-secondary); }
.btn-sum.outline:hover { border-color: var(--color-primary-mid); color: var(--color-text); }
.btn-sum.filled {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
  box-shadow: 0 2px 12px rgba(192,154,75,0.2);
}
.btn-sum.filled:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(192,154,75,0.3); }

/* List view */
.books-grid.list-view { grid-template-columns: 1fr; gap: 12px; }
.books-grid.list-view .book-card { display: flex; border-radius: 10px; }
.books-grid.list-view .book-card:hover { transform: translateY(-2px); box-shadow: 0 10px 28px var(--color-shadow-heavy); }
.books-grid.list-view .book-cover { width: 110px; height: 150px; aspect-ratio: auto; flex-shrink: 0; }
.books-grid.list-view .book-cover-img { font-size: .9rem; padding: 14px; }
.books-grid.list-view .book-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; padding: 14px 20px; }
.books-grid.list-view .bi-title { font-size: 1rem; -webkit-line-clamp: 1; }
.books-grid.list-view .card-unfav { opacity: .5; transform: scale(.8); }

.empty-state {
  text-align: center;
  padding: 80px 40px;
}
.empty-state.show { display: block; }
.empty-icon { font-size: 3.5rem; margin-bottom: 16px; opacity: 0.5; }
.empty-state h3 { font-size: 1.2rem; color: var(--color-text); margin-bottom: 8px; }
.empty-state p { font-size: 0.9rem; color: var(--color-text-light); margin-bottom: 24px; max-width: 380px; margin-left: auto; margin-right: auto; }
.btn-discover {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 11px 28px;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
  font-weight: 700;
  font-size: 0.88rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(192,154,75,0.2);
  text-decoration: none;
}
.btn-discover:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(192,154,75,0.3);
}



@keyframes fadeUp {
  from { opacity: 0; transform: translateY(14px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1200px) { .books-grid, .loading-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 1024px) {
  .books-grid, .loading-grid { grid-template-columns: repeat(3, 1fr); }
  .ph-inner { flex-direction: column; align-items: flex-start; }
}
@media (max-width: 768px) {
  .ph-inner { padding: 28px 20px; }
  .ph-left h1 { font-size: 1.4rem; }
  .toolbar { padding: 14px 20px; }
  .books-area { padding: 16px 20px 20px; }
  .books-grid, .loading-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .summary-bar { padding: 0 20px 20px; }
  .sum-left { gap: 16px; }
  .btn-sum { font-size: .8rem; padding: 9px 16px; }
  .books-grid.list-view .book-card { flex-direction: column; }
  .books-grid.list-view .book-cover { width: 100%; height: 160px; }
}
</style>
