<template>
  <div class="books-page">
    <!-- PAGE HEADER -->
    <section class="page-header">
      <div class="page-header-inner">
        <div class="breadcrumb"><span>首页</span><span>/</span><span>全部书籍</span></div>
        <h1>全部书籍</h1>
        <p class="ph-desc">漫步在万册藏书中，从经典文学到前沿新知，每一本都经过编辑团队的精心挑选。</p>
        <div class="page-header-stats">
          <div class="ph-stat"><strong>{{ (total || 0).toLocaleString() }}</strong><span>本在售</span></div>
          <div class="ph-stat"><strong>{{ categoryCount }}</strong><span>大分类</span></div>
        </div>
      </div>
    </section>

    <!-- QUICK FILTERS -->
    <div class="quick-filters">
      <div class="qf-row">
        <span class="qf-label">快捷筛选</span>
        <div v-for="pill in filterPills" :key="pill.key"
          :class="['qf-pill', { active: pill.key === 'all' ? activeTags.size === 0 : activeTags.has(pill.key) }]"
          @click="changeTag(pill.key)">
          {{ pill.label }}
        </div>
      </div>
    </div>

    <!-- TOOLBAR -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="result-count">共 <strong>{{ (total || 0).toLocaleString() }}</strong> 本</div>
        <div class="search-bar">
          <input v-model="searchKeyword" type="text" placeholder="搜索书名、作者..." @keyup.enter="handleSearch" />
          <button @click="handleSearch">🔍</button>
        </div>
        <div class="active-filters" v-if="activeTags.size > 0">
          <div v-for="tag in activeTags" :key="tag" class="active-filter-tag" @click="removeTag(tag)">
            {{ filterPills.find(p => p.key === tag)?.label || tag }}
            <span class="af-remove">✕</span>
          </div>
          <span class="clear-filters" @click="clearTags">清除全部</span>
        </div>
      </div>
      <div class="toolbar-right">
        <div class="sort-select">
          <label>排序</label>
          <select v-model="sortBy" @change="changeSort">
            <option value="default">综合推荐</option>
            <option value="sales_desc">销量从高到低</option>
            <option value="price_asc">价格从低到高</option>
            <option value="price_desc">价格从高到低</option>
            <option value="rating">评分从高到低</option>
            <option value="publish_desc">出版时间</option>
          </select>
        </div>
        <div class="view-toggle">
          <div :class="['view-btn', { active: viewMode === 'grid' }]" @click="viewMode = 'grid'" title="网格视图">▦</div>
          <div :class="['view-btn', { active: viewMode === 'list' }]" @click="viewMode = 'list'" title="列表视图">☰</div>
        </div>
      </div>
    </div>

    <!-- BOOKS AREA -->
    <div class="books-area">
      <!-- GRID SKELETON -->
      <div v-if="loading && books.length === 0" class="books-grid">
        <div v-for="i in 10" :key="i" class="book-skeleton"><div class="skeleton sk-cover"></div><div class="skeleton sk-title"></div><div class="skeleton sk-author"></div></div>
      </div>

      <!-- GRID VIEW -->
      <div v-else-if="viewMode === 'grid' && books.length" class="books-grid">
        <div v-for="(book, idx) in books" :key="book.id" class="book-card" :style="{ animationDelay: (idx * 0.03) + 's' }" @click="goToBook(book.id)" @mouseenter="hoveredBookId = book.id" @mouseleave="hoveredBookId = null">
          <div class="book-cover">
            <img v-if="book.coverUrl && !coverErrors[book.id]" class="book-cover-img" :src="book.coverUrl" :alt="book.title" loading="lazy" @error="coverErrors[book.id] = true" />
            <div v-if="!book.coverUrl || coverErrors[book.id]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(book.id)">
              <span class="cover-title">{{ book.title }}</span>
              <span class="cover-author">{{ book.author }}</span>
            </div>
            <div class="book-badge" :class="getBadge(book)">{{ getBadgeText(book) }}</div>
            <div class="book-fav" @click.stop="toggleFav(book.id)" :class="{ active: favoriteStore.isFavorited(book.id) }">{{ favoriteStore.isFavorited(book.id) ? '♥' : '♡' }}</div>
            <transition name="quote-fade">
              <div v-if="book.quote && hoveredBookId === book.id" class="book-quote-overlay">
                <div class="quote-content">
                  <svg class="quote-mark quote-mark-left" viewBox="0 0 24 24" width="20" height="20">
                    <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
                  </svg>
                  <p class="quote-text">{{ book.quote }}</p>
                  <svg class="quote-mark quote-mark-right" viewBox="0 0 24 24" width="20" height="20">
                    <path d="M18 7h-3l-2 4v6h6v-6h-3l2-4zm-8 0H7L5 11v6h6v-6H8l2-4z" fill="currentColor"/>
                  </svg>
                </div>
              </div>
            </transition>
          </div>
          <div class="book-info">
            <div class="book-title">{{ book.title }}</div>
            <div class="book-author">{{ book.author }}</div>
            <div class="book-tags" v-if="book.tags && book.tags.length">
              <span v-for="t in book.tags" :key="t" class="book-tag">{{ t }}</span>
            </div>
            <div class="book-meta">
              <div class="book-price">¥{{ book.price }}</div>
              <div class="book-rating">
                <template v-if="book.avgRating">
                  <span class="stars">{{ renderStars(book.avgRating) }}</span>
                  <span class="rating-val">{{ book.avgRating.toFixed(1) }}</span>
                </template>
                <span v-else class="no-rating">暂无评分</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- LIST VIEW -->
      <div v-else-if="viewMode === 'list' && books.length" class="books-grid list-view">
        <div v-for="book in books" :key="book.id" class="book-card" @click="goToBook(book.id)">
          <div class="book-cover">
            <img v-if="book.coverUrl && !coverErrors[book.id]" class="book-cover-img" :src="book.coverUrl" :alt="book.title" loading="lazy" @error="coverErrors[book.id] = true" />
            <div v-if="!book.coverUrl || coverErrors[book.id]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(book.id)">
              <span class="cover-title">{{ book.title }}</span>
              <span class="cover-author">{{ book.author }}</span>
            </div>
            <div class="book-badge" :class="getBadge(book)">{{ getBadgeText(book) }}</div>
          </div>
          <div class="book-info">
            <div class="book-title">{{ book.title }}</div>
            <div class="book-author">{{ book.author }}</div>
            <div v-if="book.description" class="book-desc">{{ book.description }}</div>
            <div class="book-tags" v-if="book.tags && book.tags.length">
              <span v-for="t in book.tags" :key="t" class="book-tag">{{ t }}</span>
            </div>
            <div class="book-meta">
              <div class="book-price">¥{{ book.price }}</div>
              <div class="book-rating">
                <template v-if="book.avgRating">
                  <span class="stars">{{ renderStars(book.avgRating) }}</span>
                  <span class="rating-val">{{ book.avgRating.toFixed(1) }}</span>
                </template>
                <span v-else class="no-rating">暂无评分</span>
              </div>
            </div>
          </div>
          <div v-if="book.quote" class="book-quote-col">
            <svg class="quote-col-mark" viewBox="0 0 24 24" width="14" height="14">
              <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
            </svg>
            <p class="quote-col-text">{{ book.quote }}</p>
          </div>
        </div>
      </div>

      <!-- EMPTY -->
      <div v-else-if="!loading && books.length === 0" class="empty-state show">
        <div class="empty-icon">📭</div>
        <h3>暂时没有找到符合条件的书籍</h3>
        <p>试试调整筛选条件，或者浏览我们的热门推荐</p>
        <button class="btn-empty-action" @click="clearTags">清除所有筛选</button>
      </div>
    </div>

    <!-- PAGINATION -->
    <div class="pagination-wrap" v-if="totalPages > 1">
      <div class="pg-info">
        显示 <strong>{{ (pageNum - 1) * pageSize + 1 }}-{{ Math.min(pageNum * pageSize, total) }}</strong> 条，共 <strong>{{ (total || 0).toLocaleString() }}</strong> 条
      </div>
      <div class="pagination">
        <div :class="['pg-btn', 'nav-arrow', { disabled: pageNum <= 1 }]" @click="pageNum > 1 && (pageNum--, scrollTop())">‹ 上一页</div>
        <template v-for="(p, i) in pageNumbers" :key="i">
          <span v-if="p === '...'" class="pg-ellipsis">...</span>
          <div v-else :class="['pg-btn', { active: p === pageNum }]" @click="pageNum = p; scrollTop()">{{ p }}</div>
        </template>
        <div :class="['pg-btn', 'nav-arrow', { disabled: pageNum >= totalPages }]" @click="pageNum < totalPages && (pageNum++, scrollTop())">下一页 ›</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookApi } from '@/api/book'
import { getCoverStyle } from '@/utils/cover'
import { categoryApi } from '@/api/category'
import { favoriteApi } from '@/api/favorite'
import { useFavoriteStore } from '@/stores/favorite'

const router = useRouter()
const route = useRoute()
const favoriteStore = useFavoriteStore()

const books = ref([])
const categories = ref([])
const loading = ref(false)
const activeTags = ref(new Set())
const sortBy = ref('default')
const viewMode = ref('grid')
const hoveredBookId = ref(null)
const searchKeyword = ref(route.query.keyword || '')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const coverErrors = ref({})

const categoryCount = computed(() => categories.value.length)

const filterPills = [
  { key: 'all', label: '全部' },
  { key: 'discount', label: '限时折扣' },
  { key: 'freeShip', label: '包邮' },
  { key: 'set', label: '套装' },
  { key: 'preorder', label: '预售新品' },
  { key: 'ebook', label: '电子书' },
  { key: 'highrate', label: '9分以上' }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const pageNumbers = computed(() => {
  const tp = totalPages.value
  if (tp <= 7) return Array.from({ length: tp }, (_, i) => i + 1)
  const p = pageNum.value
  const pages = []
  if (p <= 4) {
    for (let i = 1; i <= 5; i++) pages.push(i)
    pages.push('...', tp)
  } else if (p >= tp - 3) {
    pages.push(1, '...')
    for (let i = tp - 4; i <= tp; i++) pages.push(i)
  } else {
    pages.push(1, '...')
    for (let i = p - 1; i <= p + 1; i++) pages.push(i)
    pages.push('...', tp)
  }
  return pages
})

const scrollTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

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

function getBadge(book) {
  if (book.sales && book.sales > 10000) return 'badge-hot'
  if (book.avgRating && book.avgRating >= 9.0) return 'badge-classic'
  return 'badge-new'
}

function getBadgeText(book) {
  if (book.sales && book.sales > 10000) return '畅销'
  if (book.avgRating && book.avgRating >= 9.0) return '经典'
  return '新书'
}

const toggleFav = async (id) => {
  const bookId = String(id)
  try {
    if (favoriteStore.isFavorited(bookId)) {
      await favoriteApi.remove(bookId)
      favoriteStore.favoriteIds = favoriteStore.favoriteIds.filter(fid => fid !== bookId)
    } else {
      await favoriteApi.add(bookId)
      if (!favoriteStore.favoriteIds.includes(bookId)) {
        favoriteStore.favoriteIds.push(bookId)
      }
    }
  } catch { /* ignore */ }
}

const fetchBooks = async () => {
  loading.value = true
  books.value = []
  total.value = 0
  coverErrors.value = {}
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, sortBy: sortBy.value === 'default' ? '' : sortBy.value }
    if (activeTags.value.size > 0) {
      params.tag = [...activeTags.value].join(',')
    }
    if (route.query.keyword) params.keyword = route.query.keyword
    if (route.query.categoryId) params.categoryId = Number(route.query.categoryId)
    const res = await bookApi.getList(params)
    books.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch { categories.value = [] }
}

const changeTag = (key) => {
  if (key === 'all') { activeTags.value = new Set()
  } else {
    const next = new Set(activeTags.value)
    if (next.has(key)) next.delete(key); else next.add(key)
    activeTags.value = next
  }
  pageNum.value = 1; fetchBooks()
}

const removeTag = (key) => {
  const next = new Set(activeTags.value)
  next.delete(key)
  activeTags.value = next
  pageNum.value = 1; fetchBooks()
}

const clearTags = () => {
  activeTags.value = new Set(); pageNum.value = 1; fetchBooks()
}

const changeSort = () => {
  pageNum.value = 1; fetchBooks()
}

const handleSearch = () => {
  const query = { ...route.query }
  if (searchKeyword.value.trim()) {
    query.keyword = searchKeyword.value.trim()
  } else {
    delete query.keyword
  }
  router.push({ path: '/books', query })
}

const goToBook = (id) => router.push(`/book/${id}`)

watch(pageNum, () => fetchBooks())

watch(() => route.query, () => {
  pageNum.value = 1; fetchBooks()
})

onMounted(() => {
  fetchBooks(); fetchCategories(); favoriteStore.fetchFavoriteIds()
})
</script>

<style scoped>
.books-page { background: var(--color-bg); min-height: calc(100vh - var(--header-height)); }

/* ── PAGE HEADER ── */
.page-header { background: var(--color-primary-dark, #2E1F15); position: relative; overflow: hidden; }
.page-header::before { content: ''; position: absolute; inset: 0; background: radial-gradient(ellipse at 75% 35%, rgba(192,154,75,0.06) 0%, transparent 50%), radial-gradient(ellipse at 20% 80%, rgba(92,68,52,0.15) 0%, transparent 45%); pointer-events: none; }
.page-header::after { content: ''; position: absolute; inset: 0; background: repeating-linear-gradient(90deg, transparent, transparent 90px, rgba(192,154,75,0.012) 90px, rgba(192,154,75,0.012) 91px); pointer-events: none; }
.page-header-inner { position: relative; z-index: 1; max-width: var(--max-width, 1320px); margin: 0 auto; padding: 48px 40px 40px; }
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: .8rem; color: rgba(237,230,214,0.35); margin-bottom: 16px; }
.page-header h1 { font-size: 2.2rem; color: var(--color-bg-warm); font-weight: 900; margin-bottom: 8px; letter-spacing: .02em; }
.ph-desc { color: rgba(237,230,214,0.4); font-size: .93rem; max-width: 600px; margin-bottom: 22px; }
.page-header-stats { display: flex; gap: 36px; }
.ph-stat { display: flex; align-items: baseline; gap: 6px; }
.ph-stat strong { font-family: var(--font-display); font-size: 1.25rem; font-weight: 900; color: var(--color-accent-light, #D4B06A); }
.ph-stat span { font-size: .78rem; color: rgba(237,230,214,0.32); }

/* ── QUICK FILTERS ── */
.quick-filters { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 24px 40px 0; }
.qf-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.qf-label { font-size: .76rem; color: var(--color-text-light); font-weight: 500; letter-spacing: .06em; margin-right: 2px; white-space: nowrap; }
.qf-pill { padding: 8px 20px; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 24px; font-size: .84rem; cursor: pointer; transition: all .25s; user-select: none; color: var(--color-text-secondary); }
.qf-pill:hover { border-color: var(--color-primary-mid, #5C4434); color: var(--color-text); }
.qf-pill.active { background: var(--color-primary); color: var(--color-bg-warm); border-color: var(--color-primary); font-weight: 500; }

/* ── TOOLBAR ── */
.toolbar { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 16px 40px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--color-divider); }
.toolbar-left { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.result-count { font-size: .85rem; color: var(--color-text-light); white-space: nowrap; }
.result-count strong { color: var(--color-text); font-weight: 600; }
.search-bar { display: flex; align-items: center; gap: 0; margin-left: 4px; }
.search-bar input { width: 140px; padding: 6px 10px; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 6px 0 0 6px; font-size: .82rem; color: var(--color-text); outline: none; transition: border-color .2s; }
.search-bar input:focus { border-color: var(--color-primary-mid, #5C4434); width: 180px; }
.search-bar button { padding: 6px 10px; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-left: none; border-radius: 0 6px 6px 0; cursor: pointer; font-size: .82rem; color: var(--color-text-light); transition: all .2s; }
.search-bar button:hover { color: var(--color-accent); background: var(--color-bg); }
.active-filters { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.active-filter-tag { display: flex; align-items: center; gap: 6px; padding: 4px 12px; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 16px; font-size: .78rem; color: var(--color-text-secondary); cursor: pointer; transition: all .25s; user-select: none; }
.active-filter-tag:hover { border-color: var(--color-red, #A04040); background: var(--color-red-bg, rgba(160,64,64,0.08)); }
.af-remove { font-size: .65rem; color: var(--color-text-light); transition: color .2s; }
.active-filter-tag:hover .af-remove { color: var(--color-red, #A04040); }
.clear-filters { font-size: .78rem; color: var(--color-text-light); cursor: pointer; border-bottom: 1px dashed var(--color-divider-strong, rgba(74,53,38,0.18)); padding-bottom: 1px; transition: color .2s; white-space: nowrap; }
.clear-filters:hover { color: var(--color-accent); }
.toolbar-right { display: flex; align-items: center; gap: 16px; }
.sort-select { display: flex; align-items: center; gap: 6px; position: relative; }
.sort-select label { font-size: .8rem; color: var(--color-text-light); white-space: nowrap; }
.sort-select select { appearance: none; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 6px; padding: 7px 32px 7px 12px; font-size: .84rem; color: var(--color-text); cursor: pointer; outline: none; transition: border-color .2s; }
.sort-select select:focus { border-color: var(--color-primary-mid, #5C4434); }
.sort-select::after { content: '▾'; position: absolute; right: 10px; top: 50%; transform: translateY(-50%); font-size: .7rem; color: var(--color-text-light); pointer-events: none; }
.view-toggle { display: flex; gap: 4px; }
.view-btn { width: 34px; height: 34px; display: flex; align-items: center; justify-content: center; border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 6px; background: var(--color-bg-card); color: var(--color-text-light); font-size: .85rem; cursor: pointer; transition: all .2s; }
.view-btn:hover { border-color: var(--color-primary-mid, #5C4434); color: var(--color-text); }
.view-btn.active { background: var(--color-primary); border-color: var(--color-primary); color: var(--color-bg-warm); }

/* ── BOOKS AREA ── */
.books-area { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 28px 40px 20px; }
.books-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 22px; }
.books-grid.list-view { grid-template-columns: 1fr; gap: 16px; }

/* ── QUOTE OVERLAY ── */
.book-quote-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(28,18,12,0.92) 0%, rgba(44,31,21,0.88) 50%, rgba(28,18,12,0.95) 100%);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  overflow: hidden;
  z-index: 1;
}
.quote-content {
  position: relative;
  text-align: center;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.quote-mark {
  color: rgba(192,154,75,0.6);
  flex-shrink: 0;
}
.quote-mark-left { margin-bottom: 8px; align-self: flex-start; margin-left: -4px; }
.quote-mark-right { margin-top: 8px; align-self: flex-end; margin-right: -4px; }
.quote-text {
  font-family: 'Noto Serif SC', 'STSong', 'SimSun', serif;
  font-size: 0.85rem;
  line-height: 1.8;
  color: rgba(237,230,214,0.95);
  text-align: justify;
  margin: 0;
  max-height: 100%;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  -webkit-box-orient: vertical;
  letter-spacing: 0.05em;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
}
.quote-fade-enter-active { transition: all 0.4s cubic-bezier(0.25,0.46,0.45,0.94); }
.quote-fade-leave-active { transition: all 0.3s cubic-bezier(0.55,0,1,0.45); }
.quote-fade-enter-from { opacity: 0; transform: scale(1.05); }
.quote-fade-leave-to { opacity: 0; transform: scale(0.95); }

/* ── BOOK CARD ── */
.book-card { background: var(--color-bg-card); border-radius: 12px; overflow: hidden; border: 1px solid var(--color-divider); transition: all .4s cubic-bezier(.25,.46,.45,.94); cursor: pointer; position: relative; opacity: 0; animation: fadeUp .55s ease forwards; }
.book-card:hover { transform: translateY(-6px); box-shadow: 0 16px 40px var(--color-shadow-heavy, rgba(44,30,20,0.14)), 0 2px 6px var(--color-shadow, rgba(44,30,20,0.06)); border-color: rgba(192,154,75,0.15); }
.book-cover { position: relative; aspect-ratio: 3/4; overflow: hidden; }
.book-cover-img { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-weight: 700; font-size: 1.2rem; color: rgba(237,230,214,0.85); padding: 28px; text-align: center; line-height: 1.4; transition: transform .5s ease; object-fit: cover; }
.book-card:hover .book-cover-img { transform: scale(1.04); }
.book-cover-fallback { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; text-align: center; box-shadow: 0 30px 60px rgba(0,0,0,0.35), inset 0 1px 0 rgba(192,154,75,0.08); border-left: 3px solid rgba(192,154,75,0.12); border-radius: 6px; }
.cover-title { font-family: var(--font-display); font-weight: 700; font-size: 1.6rem; color: rgba(237,230,214,0.85); text-shadow: 0 1px 4px rgba(0,0,0,0.2); word-break: break-all; line-height: 1.3; margin-bottom: 10px; }
.cover-author { font-size: .8rem; color: rgba(237,230,214,0.5); text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.book-badge { position: absolute; top: 10px; left: 10px; font-size: .63rem; font-weight: 700; padding: 3px 10px; border-radius: 4px; letter-spacing: .06em; box-shadow: 0 2px 6px rgba(0,0,0,0.15); }
.badge-hot { background: var(--color-accent); color: var(--color-primary-abyss); }
.badge-sale { background: var(--color-red, #A04040); color: #fff; }
.badge-new { background: var(--color-green, #5C8856); color: #fff; }
.badge-classic { background: var(--color-primary-mid, #5C4434); color: var(--color-bg-warm); }
.book-fav { position: absolute; top: 10px; right: 10px; width: 30px; height: 30px; background: rgba(46,31,21,0.55); backdrop-filter: blur(10px); border-radius: 50%; display: flex; align-items: center; justify-content: center; color: var(--color-bg); font-size: .8rem; cursor: pointer; opacity: 0; transform: scale(.85); transition: all .3s; z-index: 3; }
.book-card:hover .book-fav { opacity: 1; transform: scale(1); }
.book-fav:hover, .book-fav.active { background: var(--color-accent); color: var(--color-primary-abyss); }
.book-info { padding: 14px 16px 18px; }
.book-title { font-family: var(--font-display); font-size: .92rem; font-weight: 600; color: var(--color-text); margin-bottom: 3px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.book-author { font-size: .78rem; color: var(--color-text-light); margin-bottom: 6px; }
.book-tags { display: flex; gap: 4px; flex-wrap: wrap; margin-bottom: 8px; }
.book-tag { font-size: .65rem; padding: 2px 7px; border-radius: 4px; background: var(--color-accent-glow, rgba(192,154,75,0.12)); color: var(--color-accent-muted); font-weight: 500; }
.book-meta { display: flex; align-items: center; justify-content: space-between; }
.book-price { font-family: var(--font-display); font-weight: 700; font-size: 1.05rem; color: var(--color-accent-muted); }
.book-rating { display: flex; align-items: center; gap: 2px; font-size: .72rem; color: var(--color-accent); }
.book-rating .stars { font-size: .7rem; }
.book-rating .rating-val { color: var(--color-text-light); margin-left: 2px; }
.book-rating .no-rating { font-size: .7rem; font-style: italic; color: var(--color-text-light); margin-left: 0; }

/* ── LIST VIEW ── */
.books-grid.list-view .book-card { display: flex; border-radius: 10px; }
.books-grid.list-view .book-cover { width: 110px; height: 150px; aspect-ratio: auto; flex-shrink: 0; }
.books-grid.list-view .book-cover-img { font-size: .9rem; padding: 14px; }
.books-grid.list-view .book-info { flex: 1; min-width: 0; display: flex; flex-direction: column; justify-content: center; padding: 14px 20px; }
.books-grid.list-view .book-title { font-size: 1rem; -webkit-line-clamp: 1; }
.books-grid.list-view .book-desc { font-size: .82rem; color: var(--color-text-light); line-height: 1.7; margin: 6px 0 10px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.books-grid.list-view .book-tags { margin-bottom: 8px; }
.books-grid.list-view .book-fav { display: none; }
.books-grid.list-view .book-card:hover { transform: translateY(-2px); }
/* Quote column */
.books-grid.list-view .book-quote-col {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 20px;
  border-left: 1px solid var(--color-divider);
  width: 220px;
  flex-shrink: 0;
  align-self: center;
}
.books-grid.list-view .quote-col-mark {
  flex-shrink: 0;
  margin-top: 3px;
  color: var(--color-accent);
  opacity: 0.5;
}
.books-grid.list-view .quote-col-text {
  font-family: 'Noto Serif SC', 'STSong', serif;
  font-size: 0.82rem;
  line-height: 1.7;
  color: var(--color-text-secondary);
  font-style: italic;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ── SKELETON ── */
.skeleton { background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 6px; }
.book-skeleton { background: var(--color-bg-card); border-radius: 12px; border: 1px solid var(--color-divider); overflow: hidden; }
.sk-cover { aspect-ratio: 3/4; border-radius: 0; }
.sk-title { height: 20px; margin: 12px 16px 8px; }
.sk-author { height: 14px; margin: 0 16px 16px; width: 60%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

/* ── EMPTY ── */
.empty-state { text-align: center; padding: 80px 0; display: none; }
.empty-state.show { display: block; }
.empty-icon { font-size: 3rem; margin-bottom: 16px; opacity: .5; }
.empty-state h3 { font-family: var(--font-display); font-size: 1.2rem; color: var(--color-text); margin-bottom: 8px; }
.empty-state p { font-size: .9rem; color: var(--color-text-light); margin-bottom: 24px; }
.btn-empty-action { padding: 10px 28px; background: var(--color-primary); color: var(--color-bg-warm); border: none; border-radius: 8px; font-size: .88rem; font-weight: 500; cursor: pointer; transition: all .2s; }
.btn-empty-action:hover { background: var(--color-primary-mid, #5C4434); }

/* ── PAGINATION ── */
.pagination-wrap { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 16px 40px 20px; display: flex; align-items: center; justify-content: space-between; border-top: 1px solid var(--color-divider); padding-top: 24px; }
.pg-info { font-size: .82rem; color: var(--color-text-light); }
.pg-info strong { color: var(--color-text); }
.pagination { display: flex; align-items: center; gap: 6px; }
.pg-btn { min-width: 38px; height: 38px; display: flex; align-items: center; justify-content: center; border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 8px; background: var(--color-bg-card); color: var(--color-text-secondary); font-size: .88rem; font-family: var(--font-display); font-weight: 600; cursor: pointer; transition: all .25s; padding: 0 10px; user-select: none; }
.pg-btn:hover { border-color: var(--color-primary-mid, #5C4434); color: var(--color-text); background: var(--color-bg-cream); }
.pg-btn.active { background: var(--color-primary); border-color: var(--color-primary); color: var(--color-bg-warm); }
.pg-btn.nav-arrow { font-size: .78rem; color: var(--color-text-light); font-weight: 400; }
.pg-btn.nav-arrow:hover { color: var(--color-accent); }
.pg-btn.disabled { opacity: .35; pointer-events: none; }
.pg-ellipsis { font-size: .82rem; color: var(--color-text-light); padding: 0 4px; }

/* ── RESPONSIVE ── */
@media (max-width: 1200px) { .books-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 1024px) {
  .books-grid { grid-template-columns: repeat(3, 1fr); }
  .toolbar { flex-direction: column; gap: 12px; align-items: flex-start; }
  .toolbar-right { width: 100%; justify-content: space-between; }
}
@media (max-width: 768px) {
  .page-header-inner { padding: 32px 20px 28px; }
  .page-header h1 { font-size: 1.5rem; }
  .quick-filters { padding: 18px 20px 0; }
  .toolbar { padding: 14px 20px; }
  .books-area { padding: 20px; }
  .books-grid { grid-template-columns: repeat(2, 1fr); gap: 12px; }
  .books-grid.list-view { grid-template-columns: 1fr; }
  .books-grid.list-view .book-card { flex-direction: column; }
  .books-grid.list-view .book-cover { width: 100%; height: 160px; }
  .books-grid.list-view .book-quote-col { width: auto; border-left: none; border-top: 1px solid var(--color-divider); padding: 10px 14px; }
  .pagination-wrap { padding: 16px 20px; flex-direction: column; gap: 14px; }
  .pagination .pg-btn:not(.active):not(.nav-arrow) { display: none; }
  .page-header-stats { gap: 20px; flex-wrap: wrap; }
}
</style>
