<template>
  <div class="books-page">
    <div class="page-container">
      <div class="page-header">
        <h1>全部书籍</h1>
        <p class="page-subtitle">
          共 <strong>{{ total || 0 }}</strong> 本好书等你发现
        </p>
      </div>

      <div class="filter-pills">
        <span
          v-for="pill in filterPills"
          :key="pill.key"
          :class="['pill', { active: pill.key === 'all' ? activeTags.size === 0 : activeTags.has(pill.key) }]"
          @click="changeTag(pill.key)"
        >
          {{ pill.label }}
        </span>
      </div>

      <div class="toolbar">
        <div class="toolbar-left">
          <span class="result-count">共 <strong>{{ total }}</strong> 本</span>
          <span v-for="tag in activeTags" :key="tag" class="active-tag-badge">
            {{ filterPills.find(p => p.key === tag)?.label || tag }}
            <span class="remove-tag" @click="removeTag(tag)">✕</span>
          </span>
          <button v-if="activeTags.size > 0" class="clear-all" @click="clearTags">清除全部</button>
        </div>
        <div class="toolbar-right">
          <div class="sort-select" @click.stop="toggleSort">
            <span class="sort-label">{{ currentSortLabel }}</span>
            <span class="sort-arrow">▾</span>
            <div v-if="sortOpen" class="sort-dropdown">
              <div
                v-for="opt in sortOptions"
                :key="opt.value"
                :class="['sort-item', { active: sortBy === opt.value }]"
                @click="changeSort(opt.value)"
              >
                {{ opt.label }}
                <span v-if="sortBy === opt.value" class="check">✓</span>
              </div>
            </div>
          </div>
          <div class="view-toggle">
            <span
              :class="['view-btn', { active: viewMode === 'grid' }]"
              @click="viewMode = 'grid'"
              title="网格视图"
            >▦</span>
            <span
              :class="['view-btn', { active: viewMode === 'list' }]"
              @click="viewMode = 'list'"
              title="列表视图"
            >☰</span>
          </div>
        </div>
      </div>

      <div v-if="loading" class="books-grid" :class="{ loading: true }">
        <div v-for="i in 10" :key="i" class="book-skeleton">
          <div class="skeleton cover"></div>
          <div class="skeleton title"></div>
          <div class="skeleton author"></div>
        </div>
      </div>

      <template v-else-if="books.length">
        <div v-if="viewMode === 'grid'" class="books-grid">
          <BookCard
            v-for="book in books"
            :key="book.id"
            :book="book"
            @click="goToBook(book.id)"
          />
        </div>
        <div v-else class="books-list">
          <div
            v-for="book in books"
            :key="book.id"
            class="list-item"
            @click="goToBook(book.id)"
          >
            <div class="list-cover">
              <img
                v-if="book.coverUrl"
                :src="book.coverUrl"
                :alt="book.title"
              />
              <div v-else class="list-cover-fallback">
                {{ book.title.slice(0, 4) }}
              </div>
            </div>
            <div class="list-info">
              <div class="list-title">{{ book.title }}</div>
              <div class="list-author">{{ book.author }}</div>
              <div class="list-desc" v-if="book.description">{{ book.description }}</div>
            </div>
            <div class="list-meta">
              <div class="list-price">¥{{ book.price }}</div>
              <div v-if="book.avgRating" class="list-rating">★ {{ book.avgRating.toFixed(1) }}</div>
            </div>
          </div>
        </div>
      </template>

      <div v-else class="empty-state">
        <div class="empty-icon">📭</div>
        <h3>暂时没有找到符合条件的书籍</h3>
        <p>试试调整筛选条件，或者浏览我们的</p>
        <button class="btn-link" @click="changeTag('all')">清除所有筛选</button>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="pageNum === 1" @click="changePage(pageNum - 1)">
          ‹ 上一页
        </button>
        <button
          v-for="page in visiblePages"
          :key="page"
          :class="['page-btn', { active: page === pageNum, ellipsis: page === '...' }]"
          :disabled="page === '...'"
          @click="page !== '...' && changePage(page)"
        >
          {{ page }}
        </button>
        <button class="page-btn" :disabled="pageNum === totalPages" @click="changePage(pageNum + 1)">
          下一页 ›
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { bookApi } from '@/api/book'
import BookCard from '@/components/business/BookCard.vue'

const router = useRouter()
const route = useRoute()

const books = ref([])
const loading = ref(false)
const activeTags = ref(new Set())
const sortBy = ref('default')
const sortOpen = ref(false)
const viewMode = ref('grid')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filterPills = [
  { key: 'all', label: '全部' },
  { key: 'discount', label: '今日特价' },
  { key: 'sale', label: '限时折扣' },
  { key: 'freeShip', label: '包邮' },
  { key: 'set', label: '套装' },
  { key: 'preorder', label: '预售新品' },
  { key: 'ebook', label: '电子书' }
]

const sortOptions = [
  { value: 'default', label: '综合推荐' },
  { value: 'sales_desc', label: '销量从高到低' },
  { value: 'price_asc', label: '价格从低到高' },
  { value: 'price_desc', label: '价格从高到低' },
  { value: 'publish_desc', label: '出版时间从新到旧' }
]

const currentSortLabel = computed(() => {
  const found = sortOptions.find(o => o.value === sortBy.value)
  return found ? found.label : '综合推荐'
})

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const tp = totalPages.value
  const cur = pageNum.value
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) pages.push(i)
  } else if (cur <= 3) {
    for (let i = 1; i <= 5; i++) pages.push(i)
    pages.push('...', tp)
  } else if (cur >= tp - 2) {
    pages.push(1, '...')
    for (let i = tp - 4; i <= tp; i++) pages.push(i)
  } else {
    pages.push(1, '...')
    for (let i = cur - 1; i <= cur + 1; i++) pages.push(i)
    pages.push('...', tp)
  }
  return pages
})

const toggleSort = () => {
  sortOpen.value = !sortOpen.value
}

const closeSort = () => {
  sortOpen.value = false
}

const fetchBooks = async () => {
  loading.value = true
  books.value = []
  total.value = 0
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value === 'default' ? '' : sortBy.value
    }
    if (activeTags.value.size > 0) {
      params.tag = [...activeTags.value].join(',')
    }
    if (route.query.keyword) {
      params.keyword = route.query.keyword
    }
    if (route.query.categoryId) {
      params.categoryId = Number(route.query.categoryId)
    }
    const res = await bookApi.getList(params)
    books.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch books:', error)
  } finally {
    loading.value = false
  }
}

const changeTag = (key) => {
  if (key === 'all') {
    activeTags.value = new Set()
  } else {
    const next = new Set(activeTags.value)
    if (next.has(key)) {
      next.delete(key)
    } else {
      next.add(key)
    }
    activeTags.value = next
  }
  pageNum.value = 1
  fetchBooks()
}

const removeTag = (key) => {
  const next = new Set(activeTags.value)
  next.delete(key)
  activeTags.value = next
  pageNum.value = 1
  fetchBooks()
}

const clearTags = () => {
  activeTags.value = new Set()
  pageNum.value = 1
  fetchBooks()
}

const changeSort = (sort) => {
  sortBy.value = sort
  sortOpen.value = false
  fetchBooks()
}

const changePage = (page) => {
  pageNum.value = page
  fetchBooks()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const goToBook = (id) => {
  router.push(`/book/${id}`)
}

watch(() => route.query, () => {
  pageNum.value = 1
  fetchBooks()
})

onMounted(() => {
  document.addEventListener('click', closeSort)
  fetchBooks()
})
</script>

<style scoped>
.books-page {
  padding: var(--space-12) 0 var(--space-16);
  background: var(--color-bg);
  min-height: calc(100vh - var(--header-height));
}

.page-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.page-header {
  margin-bottom: var(--space-8);
}

.page-header h1 {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  color: var(--color-primary);
  margin-bottom: var(--space-2);
  letter-spacing: 0.04em;
}

.page-subtitle {
  font-size: var(--text-sm);
  color: var(--color-text-light);
}

.page-subtitle strong {
  color: var(--color-accent-muted);
  font-weight: 700;
}

.filter-pills {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
}

.pill {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  padding: 6px 18px;
  border-radius: var(--radius-full);
  border: 1px solid var(--color-divider);
  background: var(--color-bg-card);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.pill:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.pill.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-bg-warm);
  font-weight: 500;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) 0;
  margin-bottom: var(--space-6);
  border-top: 1px solid var(--color-divider);
  border-bottom: 1px solid var(--color-divider);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.result-count {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.result-count strong {
  color: var(--color-accent-muted);
  font-weight: 700;
}

.active-tag-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-bg-warm);
  background: var(--color-primary);
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.remove-tag {
  cursor: pointer;
  opacity: 0.7;
  font-size: 11px;
}

.remove-tag:hover {
  opacity: 1;
}

.clear-all {
  font-size: var(--text-xs);
  color: var(--color-text-light);
  text-decoration: underline;
  text-underline-offset: 3px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.clear-all:hover {
  color: var(--color-accent);
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.sort-select {
  position: relative;
  cursor: pointer;
  user-select: none;
}

.sort-label {
  font-size: var(--text-sm);
  color: var(--color-text);
  padding: 6px 12px;
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-md);
  background: var(--color-bg-card);
  display: flex;
  align-items: center;
  gap: 6px;
}

.sort-arrow {
  font-size: 10px;
  color: var(--color-text-light);
}

.sort-dropdown {
  position: absolute;
  top: calc(100% + 4px);
  right: 0;
  min-width: 180px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-xl);
  z-index: 100;
  overflow: hidden;
}

.sort-item {
  font-size: var(--text-sm);
  color: var(--color-text);
  padding: 10px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background var(--transition-fast);
}

.sort-item:hover {
  background: var(--color-bg-cream);
}

.sort-item.active {
  color: var(--color-accent-muted);
  font-weight: 500;
}

.sort-item .check {
  color: var(--color-accent);
  font-weight: 700;
}

.view-toggle {
  display: flex;
  gap: 2px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.view-btn {
  font-size: 16px;
  padding: 6px 10px;
  cursor: pointer;
  color: var(--color-text-light);
  transition: all var(--transition-fast);
}

.view-btn:hover {
  color: var(--color-text);
}

.view-btn.active {
  color: var(--color-accent-muted);
  background: var(--color-accent-glow);
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: var(--space-5);
}

.books-grid.loading {
  pointer-events: none;
}

.book-skeleton {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  border: 1px solid var(--color-divider);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

.skeleton.cover {
  aspect-ratio: 3 / 4;
  margin-bottom: var(--space-3);
}

.skeleton.title {
  height: 20px;
  margin-bottom: var(--space-2);
}

.skeleton.author {
  height: 14px;
  width: 60%;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.books-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--color-divider);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.list-item {
  display: flex;
  align-items: center;
  gap: var(--space-5);
  padding: var(--space-4) var(--space-5);
  background: var(--color-bg-card);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.list-item:hover {
  background: var(--color-bg-cream);
}

.list-cover {
  width: 80px;
  flex-shrink: 0;
  aspect-ratio: 3 / 4;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.list-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.list-cover-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, var(--color-primary-light), var(--color-primary-dark));
  color: var(--color-bg-warm);
  font-family: var(--font-display);
  font-size: var(--text-xs);
  text-align: center;
}

.list-info {
  flex: 1;
  min-width: 0;
}

.list-title {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--color-text);
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.list-author {
  font-size: var(--text-xs);
  color: var(--color-text-light);
  margin-bottom: 4px;
}

.list-desc {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.list-meta {
  text-align: right;
  flex-shrink: 0;
}

.list-price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: var(--text-lg);
  color: var(--color-accent-muted);
  margin-bottom: 4px;
}

.list-rating {
  font-size: var(--text-xs);
  color: var(--color-accent);
}

.empty-state {
  text-align: center;
  padding: var(--space-16) 0;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: var(--space-4);
  opacity: 0.5;
}

.empty-state h3 {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  margin-bottom: var(--space-2);
  color: var(--color-text);
}

.empty-state p {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--space-3);
}

.btn-link {
  font-size: var(--text-sm);
  color: var(--color-accent);
  background: none;
  border: none;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.btn-link:hover {
  color: var(--color-accent-muted);
}

.pagination {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
  margin-top: var(--space-10);
}

.page-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled) {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.page-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: var(--color-bg-warm);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn.ellipsis {
  border: none;
  background: transparent;
  cursor: default;
  color: var(--color-text-light);
}

@media (max-width: 1200px) {
  .books-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 1024px) {
  .books-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .toolbar {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }
  .toolbar-right {
    width: 100%;
    justify-content: space-between;
  }
  .filter-pills {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: var(--space-2);
  }
  .pill {
    flex-shrink: 0;
  }
}
</style>
