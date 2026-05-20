<template>
  <div class="category-page">
    <div class="page-hero">
      <div class="hero-container">
        <div class="breadcrumb">
          <router-link to="/">首页</router-link>
          <span class="sep">/</span>
          <span>分类浏览</span>
        </div>
        <h1>分类浏览</h1>
        <p class="hero-desc">按类别组织的结构化导航，帮你快速定位感兴趣的书</p>
        <p class="hero-stats">共 <strong>{{ totalBooks }}</strong> 本图书 · <strong>{{ categoryTree.length }}</strong> 大分类 · <strong>{{ allSubCount }}</strong> 个子分类</p>
      </div>
    </div>

    <div class="page-body">
      <div class="body-container">
        <button class="mobile-drawer-btn" @click="drawerOpen = !drawerOpen">
          ☰ 筛选与分类
        </button>

        <div class="layout">
          <aside :class="['sidebar', { open: drawerOpen }]">
            <div class="sidebar-inner">
              <div class="sidebar-section">
                <h3 class="sidebar-title">书籍分类</h3>
                <div class="category-tree">
                  <div
                    v-for="cat in categoryTree"
                    :key="cat.id"
                    class="tree-node"
                  >
                    <div
                      :class="['tree-parent', { expanded: expandedIds.has(cat.id), active: selectedId === cat.id, 'tree-active': cat.children && cat.children.some(c => c.id === selectedId) }]"
                      @click="toggleExpand(cat)"
                    >
                      <span class="tree-arrow">{{ expandedIds.has(cat.id) ? '▾' : '▸' }}</span>
                      <span class="tree-name">{{ cat.name }}</span>
                      <span class="tree-count">{{ cat.bookCount }}</span>
                    </div>
                    <div v-if="expandedIds.has(cat.id) && cat.children.length" class="tree-children">
                      <div
                        :class="['tree-child', { active: selectedId === child.id }]"
                        v-for="child in cat.children"
                        :key="child.id"
                        @click="selectCategory(child)"
                      >
                        <span class="tree-name">{{ child.name }}</span>
                      </div>
                    </div>
                  </div>
                  <div v-if="noParentCategories.length" class="tree-node">
                    <div
                      :class="['tree-parent', { expanded: expandedIds.has('no-parent'), active: selectedId === 'no-parent' }]"
                      @click="toggleExpand({ id: 'no-parent' })"
                    >
                      <span class="tree-arrow">{{ expandedIds.has('no-parent') ? '▾' : '▸' }}</span>
                      <span class="tree-name">其他</span>
                      <span class="tree-count">{{ noParentCategories.length }}</span>
                    </div>
                    <div v-if="expandedIds.has('no-parent')" class="tree-children">
                      <div
                        :class="['tree-child', { active: selectedId === cat.id }]"
                        v-for="cat in noParentCategories"
                        :key="cat.id"
                        @click="selectCategory(cat)"
                      >
                        <span class="tree-name">{{ cat.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="sidebar-section">
                <h3 class="sidebar-title">筛选</h3>
                <div class="filter-group">
                  <h4 class="filter-label">价格区间</h4>
                  <div class="price-range">
                    <input v-model="priceMin" type="number" placeholder="¥最低" class="price-input" />
                    <span class="price-sep">—</span>
                    <input v-model="priceMax" type="number" placeholder="¥最高" class="price-input" />
                  </div>
                  <button class="filter-btn" @click="applyPriceFilter">筛选</button>
                </div>
                <div class="filter-group">
                  <h4 class="filter-label">评分筛选</h4>
                  <div class="rating-options">
                    <label v-for="r in ratingOptions" :key="r.value" class="rating-option" @click="selectRating(r.value)">
                      <span :class="['rating-stars', { active: selectedRating === r.value }]">{{ r.label }}</span>
                    </label>
                  </div>
                </div>
                <div class="filter-group">
                  <h4 class="filter-label">出版时间</h4>
                  <div class="radio-options">
                    <button
                      v-for="t in timeOptions"
                      :key="t.value"
                      :class="['radio-btn', { active: selectedTime === t.value }]"
                      @click="changeTime(t.value)"
                    >
                      {{ t.label }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <button class="drawer-close" @click="drawerOpen = false">✕</button>
          </aside>

          <div class="content" @click="drawerOpen = false">
            <div class="content-toolbar">
              <div class="toolbar-left">
                <span class="result-count">共 <strong>{{ total }}</strong> 本</span>
                <span v-if="selectedCategory" class="active-cat-badge">
                  {{ selectedCategory.name }}
                  <span class="remove-cat" @click="clearCategory">✕</span>
                </span>
              </div>
              <div class="toolbar-right">
                <div class="sort-select" @click.stop="sortOpen = !sortOpen">
                  <span class="sort-label">{{ currentSortLabel }} ▾</span>
                  <div v-if="sortOpen" class="sort-dropdown">
                    <div
                      v-for="opt in sortOptions"
                      :key="opt.value"
                      :class="['sort-item', { active: sortBy === opt.value }]"
                      @click.stop="changeSort(opt.value)"
                    >
                      {{ opt.label }}
                      <span v-if="sortBy === opt.value" class="check">✓</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="loading" class="books-grid">
              <div v-for="i in 8" :key="i" class="book-skeleton">
                <div class="skeleton cover"></div>
                <div class="skeleton title"></div>
                <div class="skeleton author"></div>
              </div>
            </div>

            <template v-else-if="books.length">
              <div class="books-grid">
                <BookCard
                  v-for="book in books"
                  :key="book.id"
                  :book="book"
                  @click="goToBook(book.id)"
                />
              </div>
            </template>

            <div v-else class="empty-state">
              <div class="empty-icon">📭</div>
              <h3>该分类暂无书籍</h3>
              <p>试试选择其他分类或调整筛选条件</p>
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { categoryApi } from '@/api/category'
import { bookApi } from '@/api/book'
import BookCard from '@/components/business/BookCard.vue'

const router = useRouter()

const loading = ref(false)
const drawerOpen = ref(false)
const categoryTree = ref([])
const noParentCategories = ref([])
const allFlat = ref([])
const expandedIds = ref(new Set())

const selectedCategory = ref(null)
const sortBy = ref('default')
const sortOpen = ref(false)
const pageNum = ref(1)
const pageSize = ref(16)
const total = ref(0)
const books = ref([])

const priceMin = ref('')
const priceMax = ref('')
const selectedRating = ref(null)
const selectedTime = ref(null)

const ratingOptions = [
  { value: 9, label: '★★★★★ 9分以上' },
  { value: 8, label: '★★★★☆ 8分以上' },
  { value: 7, label: '★★★☆☆ 7分以上' }
]

const timeOptions = [
  { value: '1m', label: '近一个月' },
  { value: '3m', label: '近三个月' },
  { value: '6m', label: '近半年' },
  { value: '1y', label: '近一年' }
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

const selectedId = computed(() => {
  if (!selectedCategory.value) return null
  return selectedCategory.value.id === 'no-parent' ? 'no-parent' : selectedCategory.value.id
})

const totalBooks = ref(0)
const allSubCount = computed(() => {
  let count = 0
  categoryTree.value.forEach(c => count += c.children.length)
  return count + noParentCategories.value.length
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

const buildTree = (flatList) => {
  const map = {}
  flatList.forEach(cat => {
    map[cat.id] = { ...cat, children: [] }
  })
  const roots = []
  const orphans = []
  flatList.forEach(cat => {
    if (cat.parentId === '0' || cat.parentId === 0 || cat.parentId === '' || cat.parentId === null) {
      roots.push(map[cat.id])
    } else if (map[cat.parentId]) {
      map[cat.parentId].children.push(map[cat.id])
    } else {
      orphans.push(map[cat.id])
    }
  })
  return { roots, orphans }
}

const fetchCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getList()
    const list = res.data || []
    allFlat.value = list
    const { roots, orphans } = buildTree(list)
    categoryTree.value = roots
    noParentCategories.value = orphans

    if (roots.length && selectedCategory.value === null) {
      const first = roots[0]
      expandedIds.value.add(first.id)
      if (first.children.length) {
        selectedCategory.value = first.children[0]
      } else {
        selectedCategory.value = first
      }
    }

    const countRes = await bookApi.getList({ pageNum: 1, pageSize: 1 })
    totalBooks.value = countRes.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  } finally {
    loading.value = false
  }

  if (selectedCategory.value) {
    fetchBooks()
  }
}

const fetchBooks = async () => {
  if (!selectedCategory.value) return
  loading.value = true
  books.value = []
  total.value = 0
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      sortBy: sortBy.value === 'default' ? '' : sortBy.value,
      minPrice: priceMin.value || undefined,
      maxPrice: priceMax.value || undefined,
      minRating: selectedRating.value || undefined,
      timeRange: selectedTime.value || undefined
    }
    // Clean undefined params
    const clean = {}
    Object.keys(params).forEach(k => { if (params[k] !== undefined) clean[k] = params[k] })
    const res = await bookApi.getByCategory(selectedCategory.value.id, clean)
    books.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to fetch books:', error)
  } finally {
    loading.value = false
  }
}

const toggleExpand = (cat) => {
  const id = cat.id
  const wasExpanded = expandedIds.value.has(id)
  const next = new Set(expandedIds.value)
  if (wasExpanded) {
    next.delete(id)
  } else {
    next.add(id)
    if (cat.children && cat.children.length) {
      selectCategory(cat.children[0])
    } else {
      selectCategory(cat)
    }
  }
  expandedIds.value = next
}

const selectCategory = (cat) => {
  selectedCategory.value = cat
  pageNum.value = 1
  sortBy.value = 'default'
  drawerOpen.value = false
  fetchBooks()
}

const clearCategory = () => {
  selectedCategory.value = null
  books.value = []
  total.value = 0
}

const applyPriceFilter = () => {
  pageNum.value = 1
  fetchBooks()
}

const selectRating = (val) => {
  selectedRating.value = selectedRating.value === val ? null : val
  pageNum.value = 1
  fetchBooks()
}

const changeTime = (val) => {
  selectedTime.value = selectedTime.value === val ? null : val
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

const closeSort = () => {
  sortOpen.value = false
}

onMounted(() => {
  fetchCategories()
  document.addEventListener('click', closeSort)
})

onUnmounted(() => {
  document.removeEventListener('click', closeSort)
})
</script>

<style scoped>
.category-page {
  background: var(--color-bg);
  min-height: calc(100vh - var(--header-height));
}

.page-hero {
  background: var(--color-primary-abyss);
  padding: var(--space-8) 0;
  position: relative;
}

.page-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 2px,
    rgba(237, 230, 214, 0.02) 2px,
    rgba(237, 230, 214, 0.02) 4px
  );
  pointer-events: none;
}

.hero-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
  position: relative;
  z-index: 1;
}

.breadcrumb {
  font-size: var(--text-xs);
  color: var(--color-text-light);
  margin-bottom: var(--space-6);
}

.breadcrumb a {
  color: var(--color-accent-muted);
  text-decoration: none;
}

.breadcrumb a:hover {
  color: var(--color-accent);
}

.breadcrumb .sep {
  margin: 0 var(--space-2);
  opacity: 0.5;
}

.page-hero h1 {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  color: var(--color-bg-warm);
  margin-bottom: var(--space-3);
  letter-spacing: 0.04em;
}

.hero-desc {
  font-size: var(--text-base);
  color: var(--color-text-light);
  margin-bottom: var(--space-4);
}

.hero-stats {
  font-size: var(--text-sm);
  color: var(--color-accent-muted);
}

.hero-stats strong {
  color: var(--color-accent);
  font-weight: 700;
}

.page-body {
  padding: var(--space-8) 0 var(--space-16);
}

.body-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.mobile-drawer-btn {
  display: none;
  width: 100%;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text);
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-lg);
  cursor: pointer;
  text-align: left;
  margin-bottom: var(--space-4);
}

.layout {
  display: flex;
  gap: var(--space-8);
  align-items: flex-start;
}

.sidebar {
  width: 260px;
  flex-shrink: 0;
  position: sticky;
  top: calc(var(--header-height) + var(--space-8));
}

.sidebar-inner {
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.sidebar-section {
  padding: var(--space-6);
}

.sidebar-section + .sidebar-section {
  border-top: 1px solid var(--color-divider);
}

.sidebar-title {
  font-family: var(--font-display);
  font-size: var(--text-base);
  color: var(--color-text);
  margin-bottom: var(--space-4);
  letter-spacing: 0.03em;
}

.tree-node {
  margin-bottom: 2px;
}

.tree-parent {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 8px 12px;
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.tree-parent:hover {
  background: var(--color-bg-cream);
  color: var(--color-text);
}

.tree-parent.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  font-weight: 500;
}

.tree-arrow {
  font-size: 10px;
  width: 16px;
  flex-shrink: 0;
  transition: transform var(--transition-fast);
}

.tree-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tree-count {
  font-size: var(--text-xs);
  color: var(--color-accent-muted);
  background: var(--color-accent-glow);
  padding: 1px 8px;
  border-radius: var(--radius-full);
}

.tree-active .tree-count {
  color: var(--color-bg-warm);
  background: rgba(237, 230, 214, 0.15);
}

.tree-children {
  padding-left: var(--space-6);
}

.tree-child {
  padding: 6px 12px;
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.tree-child:hover {
  background: var(--color-bg-cream);
  color: var(--color-text);
}

.tree-child.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  font-weight: 500;
}

.filter-group {
  margin-bottom: var(--space-5);
}

.filter-group:last-child {
  margin-bottom: 0;
}

.filter-label {
  font-size: var(--text-xs);
  color: var(--color-text-light);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: var(--space-3);
}

.price-range {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.price-input {
  flex: 1;
  padding: 6px 10px;
  font-size: var(--text-xs);
  color: var(--color-text);
  background: var(--color-bg);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-md);
  outline: none;
  width: 0;
}

.price-input:focus {
  border-color: var(--color-accent);
}

.price-sep {
  color: var(--color-text-light);
  font-size: var(--text-xs);
}

.filter-btn {
  margin-top: var(--space-2);
  width: 100%;
  padding: 6px;
  font-size: var(--text-xs);
  color: var(--color-bg-warm);
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.filter-btn:hover {
  background: var(--color-primary-mid);
}

.rating-options {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.rating-option {
  cursor: pointer;
  padding: 4px 0;
}

.rating-stars {
  font-size: var(--text-xs);
  color: var(--color-text-light);
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.rating-stars.active {
  background: var(--color-accent-glow);
  color: var(--color-accent-muted);
}

.radio-options {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.radio-btn {
  font-size: var(--text-xs);
  color: var(--color-text-secondary);
  padding: 6px 10px;
  background: none;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  text-align: left;
  transition: all var(--transition-fast);
}

.radio-btn:hover {
  background: var(--color-bg-cream);
}

.radio-btn.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
}

.drawer-close {
  display: none;
}

.content {
  flex: 1;
  min-width: 0;

}

.content-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-3) 0;
  margin-bottom: var(--space-6);
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

.active-cat-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: var(--text-xs);
  color: var(--color-bg-warm);
  background: var(--color-primary);
  padding: 3px 10px;
  border-radius: var(--radius-full);
}

.remove-cat {
  cursor: pointer;
  opacity: 0.7;
  font-size: 11px;
}

.remove-cat:hover {
  opacity: 1;
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

.books-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
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
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1024px) {
  .layout {
    flex-direction: column;
  }
  .sidebar {
    width: 100%;
    position: static;
  }
  .sidebar-inner {
    display: block;
  }
  .mobile-drawer-btn {
    display: block;
  }
  .sidebar {
    display: none;
  }
  .sidebar.open {
    display: block;
    position: fixed;
    inset: 0;
    z-index: 1000;
    background: rgba(28, 18, 12, 0.5);
    width: 100%;
  }
  .sidebar.open .sidebar-inner {
    width: 300px;
    max-width: 80vw;
    height: 100vh;
    overflow-y: auto;
    border-radius: 0;
    border: none;
  }
  .drawer-close {
    display: flex;
    position: fixed;
    top: var(--space-4);
    right: var(--space-4);
    width: 36px;
    height: 36px;
    align-items: center;
    justify-content: center;
    font-size: var(--text-lg);
    color: var(--color-bg-warm);
    background: rgba(255,255,255,0.1);
    border: none;
    border-radius: 50%;
    cursor: pointer;
    z-index: 1001;
  }
}

@media (max-width: 768px) {
  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .page-hero {
    padding: var(--space-8) 0;
  }
  .page-hero h1 {
    font-size: var(--text-3xl);
  }
}
</style>
