<template>
  <div class="new-arrivals-page">
    <!-- PAGE HEADER -->
    <section class="page-header">
      <div class="page-header-inner">
        <div class="breadcrumb"><router-link to="/">首页</router-link><span class="sep">/</span><span>新书上架</span></div>
        <h1>新书上架</h1>
        <p>每一本新书，都是一扇通往未知世界的门。第一时间发现值得期待的好书。</p>
        <div class="page-header-stats" v-if="stats.total > 0">
          <div class="ph-stat"><strong>{{ stats.total }}</strong><span>新书上架</span></div>
          <div class="ph-stat"><strong>{{ stats.thisWeek }}</strong><span>本周新上架</span></div>
          <div class="ph-stat"><strong>{{ stats.thisMonth }}</strong><span>本月新上架</span></div>
        </div>
      </div>
    </section>

    <!-- EDITOR PICK HERO -->
    <section class="editor-hero" v-if="heroBook">
      <div class="editor-inner">
        <div class="editor-cover" :style="getCoverStyle(heroBook.id)">
          <div class="ec-sub">{{ heroBook.author }}</div>
          <div class="ec-title">{{ heroBook.title }}</div>
        </div>
        <div class="editor-text">
          <div class="et-tag">✦ 编辑推荐 · 本周精选</div>
          <h3>{{ heroBook.title }}</h3>
          <div class="et-author">{{ heroBook.author }}{{ heroBook.publisher ? ' · ' + heroBook.publisher : '' }}</div>
          <p v-if="heroBook.quote" class="et-desc">{{ heroBook.quote }}</p>
          <div class="editor-meta">
            <div class="editor-price">¥{{ heroBook.price }}</div>
            <div class="editor-status instock"><span class="dot"></span> 已上架</div>
          </div>
          <div class="editor-actions">
            <button class="btn-primary" @click="goToBook(heroBook.id)">立即购买 →</button>
            <button class="btn-secondary" @click="goToBook(heroBook.id)">查看详情</button>
          </div>
        </div>
      </div>
    </section>
    <div v-else-if="!heroLoaded" class="editor-hero">
      <div class="editor-inner">
        <div class="skeleton hero-sk-cover"></div>
        <div class="hero-sk-text"><div class="skeleton" style="height:28px;width:180px;margin-bottom:12px"></div><div class="skeleton" style="height:18px;width:120px;margin-bottom:20px"></div><div class="skeleton" style="height:60px;margin-bottom:24px"></div><div class="skeleton" style="height:48px;width:200px"></div></div>
      </div>
    </div>

    <!-- FILTERS -->
    <div class="filter-section">
      <div class="filter-row">
        <span class="filter-label">时间</span>
        <div v-for="t in timePills" :key="t.key"
          :class="['filter-pill', { active: activeTime === t.key }]"
          @click="switchTime(t.key)">
          {{ t.label }}
        </div>
        <div class="filter-divider"></div>
        <div class="filter-pill" :class="{ active: activeStatus === 'all' }" @click="activeStatus='all'; resetAndFetch()">全部状态</div>
        <div class="filter-pill" :class="{ active: activeStatus === 'on' }" @click="activeStatus='on'; resetAndFetch()">已上架</div>
      </div>
      <div class="filter-row">
        <span class="filter-label">分类</span>
        <div class="filter-pill" :class="{ active: activeCategory === null }" @click="switchCategory(null)">全部</div>
        <div v-for="cat in categories" :key="cat.id"
          :class="['filter-pill', { active: activeCategory === cat.id }]"
          @click="switchCategory(cat.id)">
          {{ cat.name }}
        </div>
      </div>
    </div>

    <!-- TOOLBAR -->
    <div class="toolbar" v-if="books.length > 0">
      <div class="result-count">共 <strong>{{ total }}</strong> 本新书</div>
      <div class="sort-select">
        <label>排序</label>
        <select v-model="sortBy" @change="resetAndFetch">
          <option value="publish_desc">上架时间</option>
          <option value="sales_desc">销量</option>
          <option value="price_asc">价格从低到高</option>
          <option value="price_desc">价格从高到低</option>
        </select>
      </div>
    </div>

    <!-- SKELETON -->
    <div v-if="loading && books.length === 0" class="timeline">
      <div class="timeline-group"><div class="timeline-books"><div v-for="i in 10" :key="i" class="book-skeleton"><div class="skeleton sk-cover"></div><div class="skeleton sk-title"></div><div class="skeleton sk-author"></div></div></div></div>
    </div>

    <!-- TIMELINE -->
    <div class="timeline" v-else-if="books.length > 0">
      <div v-for="(group, gidx) in groupedBooks" :key="group.date" class="timeline-group">
        <div class="timeline-date">
          <span class="timeline-date-text">
            <span v-if="group.isToday" class="today-tag">今日</span>
            {{ group.label }}
          </span>
        </div>
        <div class="timeline-books">
          <div v-for="(book, bidx) in group.books" :key="book.id" class="book-card"
            :style="{ animationDelay: ((gidx * 5 + bidx) * 0.04) + 's' }"
            @click="goToBook(book.id)">
            <div class="book-cover">
              <img v-if="book.coverUrl && !coverErrors[book.id]" class="book-cover-img" :src="book.coverUrl" :alt="book.title" loading="lazy" @error="coverErrors[book.id] = true" />
              <div v-if="!book.coverUrl || coverErrors[book.id]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(book.id)">
                <span class="cover-title">{{ book.title }}</span>
                <span class="cover-author">{{ book.author }}</span>
              </div>
              <div class="book-badge badge-new">NEW</div>
              <div class="book-fav" @click.stop="toggleFav(book.id)" :class="{ active: favSet.has(book.id) }">
                {{ favSet.has(book.id) ? '♥' : '♡' }}
              </div>
            </div>
            <div class="book-info">
              <div class="book-title">{{ book.title }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div class="book-meta">
                <div class="book-price">¥{{ book.price }}</div>
                <div class="book-rating">
                  <template v-if="book.avgRating">
                    <span class="star-display">{{ renderStars(book.avgRating) }}</span><span>{{ book.avgRating.toFixed(1) }}</span>
                  </template>
                  <span v-else class="no-rating">暂无评分</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- LOAD MORE -->
      <div class="load-more-wrap" v-if="hasMore">
        <button class="btn-load-more" :class="{ loading: loadingMore }" @click="loadMore" :disabled="loadingMore">
          <div class="spinner"></div>
          <span class="lt">加载更多新书</span>
        </button>
      </div>
    </div>

    <!-- EMPTY -->
    <div v-if="!loading && books.length === 0 && heroLoaded" class="empty-state">
      <div class="empty-icon">📚</div>
      <h3>暂无新书上架</h3>
      <p>请稍后再来看看，我们会持续更新</p>
    </div>

    <!-- COMING SOON -->
    <section class="coming-soon">
      <div class="coming-inner">
        <div class="coming-header">
          <h2>即将上架 · 先收藏不错过</h2>
          <p>以下书籍即将上架，上架后第一时间通知你</p>
        </div>
        <div class="coming-grid">
          <div class="coming-card" v-for="item in comingBooks" :key="item.title">
            <div class="coming-cover">
              <div class="coming-cover-img" :style="item.style">
                {{ item.shortTitle }}
              </div>
              <div class="coming-countdown">
                <div class="cd-label">距上架还有</div>
                <div class="cd-time">
                  <div><div class="cd-num">{{ item.countdown.d }}</div><span class="cd-unit">天</span></div>
                  <div class="cd-sep">:</div>
                  <div><div class="cd-num">{{ item.countdown.h }}</div><span class="cd-unit">时</span></div>
                  <div class="cd-sep">:</div>
                  <div><div class="cd-num">{{ item.countdown.m }}</div><span class="cd-unit">分</span></div>
                </div>
              </div>
            </div>
            <div class="coming-info">
              <div class="ci-title">{{ item.title }}</div>
              <div class="ci-author">{{ item.author }}</div>
              <div class="ci-meta">
                <span class="ci-date">{{ item.date }}</span>
                <button class="btn-notify" :class="{ active: item.notified }" @click="item.notified = !item.notified">
                  {{ item.notified ? '✓ 已设置' : '🔔 提醒我' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi } from '@/api/book'
import { categoryApi } from '@/api/category'
import { getCoverStyle, COVER_GRADIENTS } from '@/utils/cover'

const router = useRouter()

const BOOK_STATUS_ON = 1

const timePills = [
  { key: 'all', label: '全部新书' },
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' }
]

const books = ref([])
const heroBook = ref(null)
const heroLoaded = ref(false)
const categories = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const activeTime = ref('all')
const activeStatus = ref('all')
const activeCategory = ref(null)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const sortBy = ref('publish_desc')
const favSet = reactive(new Set())
const coverErrors = ref({})

const hasMore = computed(() => books.value.length < total.value)

const stats = computed(() => {
  const now = new Date()
  const weekAgo = new Date(now); weekAgo.setDate(weekAgo.getDate() - 7)
  const monthAgo = new Date(now); monthAgo.setDate(monthAgo.getDate() - 30)
  const all = books.value
  return {
    total: total.value,
    thisWeek: all.filter(b => b.publishDate && new Date(b.publishDate) >= weekAgo).length,
    thisMonth: all.filter(b => b.publishDate && new Date(b.publishDate) >= monthAgo).length
  }
})

const groupedBooks = computed(() => {
  const map = {}
  const now = new Date()
  const today = now.toISOString().slice(0, 10)
  const yesterday = new Date(now); yesterday.setDate(yesterday.getDate() - 1)
  const yStr = yesterday.toISOString().slice(0, 10)

  let list = books.value
  const timeCutoff = getTimeCutoff(activeTime.value)
  if (timeCutoff) {
    list = list.filter(b => b.publishDate && b.publishDate >= timeCutoff)
  }
  if (activeStatus.value === 'on') {
    list = list.filter(b => b.status === BOOK_STATUS_ON)
  }

  list.forEach(book => {
    const d = book.publishDate || ''
    if (!d) return
    if (!map[d]) {
      let label = d
      let isToday = false
      if (d === today) { label = '今天'; isToday = true }
      else if (d === yStr) label = '昨天'
      else {
        const parts = d.split('-')
        label = parseInt(parts[1]) + '月' + parseInt(parts[2]) + '日'
      }
      map[d] = { date: d, label, isToday, books: [] }
    }
    map[d].books.push(book)
  })
  return Object.values(map)
})

function getTimeCutoff(key) {
  if (key === 'all') return null
  const d = new Date()
  if (key === 'week') { d.setDate(d.getDate() - 7); return d.toISOString().slice(0, 10) }
  if (key === 'month') { d.setDate(d.getDate() - 30); return d.toISOString().slice(0, 10) }
  return null
}

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

const goToBook = (id) => router.push(`/book/${id}`)

const toggleFav = (id) => {
  if (favSet.has(id)) favSet.delete(id); else favSet.add(id)
}

const fetchHeroBook = async () => {
  heroLoaded.value = false
  try {
    const res = await bookApi.getList({ sortBy: 'publish_desc', pageNum: 1, pageSize: 5 })
    const list = res.data?.records || []
    heroBook.value = list.length > 0 ? list[0] : null
  } catch {
    heroBook.value = null
  } finally {
    heroLoaded.value = true
  }
}

const fetchBooks = async (append = false) => {
  if (!append) { loading.value = true } else { loadingMore.value = true }
  if (!append) { books.value = []; total.value = 0; coverErrors.value = {} }
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, sortBy: sortBy.value }
    if (activeCategory.value) params.categoryId = activeCategory.value
    const res = await bookApi.getList(params)
    const records = res.data?.records || []
    if (append) { books.value.push(...records) } else { books.value = records }
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false; loadingMore.value = false
  }
}

const fetchCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch { categories.value = [] }
}

const resetAndFetch = () => {
  pageNum.value = 1; fetchBooks(false)
}

const switchTime = (key) => {
  activeTime.value = key; resetAndFetch()
}

const switchCategory = (id) => {
  activeCategory.value = id; resetAndFetch()
}

const loadMore = () => {
  pageNum.value++; fetchBooks(true)
}

const comingBooks = reactive([
  { title: '未来简史', shortTitle: '未来\n简史', author: '尤瓦尔·赫拉利', date: '5月24日', style: { background: COVER_GRADIENTS[5] }, notified: false, countdown: { d: '03', h: '12', m: '45' } },
  { title: '长安的荔枝', shortTitle: '长安的\n荔枝', author: '马伯庸', date: '5月26日', style: { background: COVER_GRADIENTS[8] }, notified: false, countdown: { d: '05', h: '08', m: '22' } },
  { title: '规模：复杂世界的简单法则', shortTitle: '规模', author: '[美] 杰弗里·韦斯特', date: '5月29日', style: { background: COVER_GRADIENTS[6] }, notified: false, countdown: { d: '08', h: '16', m: '30' } },
  { title: '额尔古纳河右岸（精装典藏版）', shortTitle: '额尔古纳\n河右岸\n精装版', author: '迟子建', date: '6月2日', style: { background: COVER_GRADIENTS[11] }, notified: false, countdown: { d: '12', h: '06', m: '15' } }
])

onMounted(() => {
  fetchHeroBook(); fetchBooks(false); fetchCategories()
})
</script>

<style scoped>
.new-arrivals-page {
  background: var(--color-bg);
  min-height: calc(100vh - var(--header-height));
}

/* ── PAGE HEADER ── */
.page-header { background: var(--color-primary-dark, #2E1F15); position: relative; overflow: hidden; }
.page-header::before { content: ''; position: absolute; inset: 0; background: radial-gradient(ellipse at 70% 30%, rgba(192,154,75,0.06) 0%, transparent 50%), radial-gradient(ellipse at 20% 80%, rgba(92,68,52,0.15) 0%, transparent 45%); pointer-events: none; }
.page-header::after { content: ''; position: absolute; inset: 0; background: repeating-linear-gradient(90deg, transparent, transparent 90px, rgba(192,154,75,0.012) 90px, rgba(192,154,75,0.012) 91px); pointer-events: none; }
.page-header-inner { position: relative; z-index: 1; max-width: var(--max-width); margin: 0 auto; padding: 48px 40px 40px; }
.breadcrumb { display: flex; align-items: center; gap: 8px; font-size: .8rem; color: rgba(237,230,214,0.35); margin-bottom: 16px; }
.page-header h1 { font-size: 2.2rem; color: var(--color-bg-warm); font-weight: 900; margin-bottom: 8px; letter-spacing: .02em; }
.page-header-inner > p { color: rgba(237,230,214,0.4); font-size: .93rem; max-width: 600px; margin-bottom: 22px; }
.page-header-stats { display: flex; gap: 36px; }
.ph-stat { display: flex; align-items: baseline; gap: 6px; }
.ph-stat strong { font-family: var(--font-display); font-size: 1.25rem; font-weight: 900; color: var(--color-accent-light); }
.ph-stat span { font-size: .78rem; color: rgba(237,230,214,0.32); }

/* ── EDITOR HERO ── */
.editor-hero { background: var(--color-primary-abyss); position: relative; overflow: hidden; }
.editor-hero::before { content: ''; position: absolute; right: -100px; top: -100px; width: 460px; height: 460px; background: radial-gradient(circle, rgba(192,154,75,0.07) 0%, transparent 70%); pointer-events: none; }
.editor-inner { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 60px 40px; display: grid; grid-template-columns: 260px 1fr; gap: 56px; align-items: center; position: relative; z-index: 1; }
.editor-cover { width: 260px; height: 370px; border-radius: 6px; box-shadow: 0 30px 60px rgba(0,0,0,0.35), inset 0 1px 0 rgba(192,154,75,0.08); display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 28px; text-align: center; border-left: 3px solid rgba(192,154,75,0.12); margin: 0 auto; }
.editor-cover .ec-sub { font-size: .72rem; color: var(--color-accent-light, #D4B06A); letter-spacing: .12em; margin-bottom: 8px; }
.editor-cover .ec-title { font-family: var(--font-display); font-size: 1.7rem; font-weight: 900; color: var(--color-bg-warm); margin-bottom: 6px; line-height: 1.3; }
.editor-text .et-tag { display: inline-block; color: var(--color-accent); font-size: .75rem; font-weight: 500; letter-spacing: .12em; margin-bottom: 14px; }
.editor-text h3 { font-family: var(--font-display); font-size: 1.9rem; color: var(--color-bg-warm); margin-bottom: 6px; }
.editor-text .et-author { color: rgba(237,230,214,0.4); font-size: .9rem; margin-bottom: 16px; }
.editor-text .et-desc { color: rgba(237,230,214,0.5); font-size: .93rem; line-height: 1.9; margin-bottom: 22px; max-width: 520px; display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden; }
.editor-meta { display: flex; align-items: center; gap: 24px; margin-bottom: 24px; }
.editor-price { font-family: var(--font-display); font-size: 2rem; font-weight: 900; color: var(--color-accent-light, #D4B06A); }
.editor-status { display: inline-flex; align-items: center; gap: 6px; padding: 6px 16px; border-radius: 8px; font-size: .82rem; font-weight: 500; }
.editor-status.instock { background: rgba(92,136,86,0.12); color: var(--color-green, #5C8856); }
.editor-status .dot { width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
.editor-actions { display: flex; gap: 12px; }
.btn-primary { display: inline-flex; align-items: center; gap: 8px; background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted)); color: var(--color-primary-abyss); font-weight: 700; font-size: .92rem; padding: 13px 30px; border-radius: 8px; border: none; cursor: pointer; transition: all .35s; box-shadow: 0 2px 12px rgba(192,154,75,0.2); letter-spacing: .02em; }
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(192,154,75,0.3); }
.btn-secondary { display: inline-flex; align-items: center; gap: 8px; background: transparent; color: rgba(237,230,214,0.7); font-weight: 500; font-size: .92rem; padding: 13px 30px; border-radius: 8px; border: 1px solid rgba(237,230,214,0.15); cursor: pointer; transition: all .3s; }
.btn-secondary:hover { border-color: rgba(192,154,75,0.4); color: var(--color-accent); transform: translateY(-2px); }

/* Hero skeleton */
.hero-sk-cover { width: 260px; height: 370px; border-radius: 6px; }
.hero-sk-text { flex: 1; }

/* ── FILTERS ── */
.filter-section { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 28px 40px 0; }
.filter-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 12px; }
.filter-row:last-child { margin-bottom: 0; }
.filter-label { font-size: .78rem; color: var(--color-text-light); font-weight: 500; margin-right: 4px; white-space: nowrap; }
.filter-pill { padding: 8px 20px; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 24px; font-size: .84rem; font-weight: 400; cursor: pointer; transition: all .25s; user-select: none; color: var(--color-text-secondary); }
.filter-pill:hover { border-color: var(--color-primary-mid, #5C4434); color: var(--color-text); }
.filter-pill.active { background: var(--color-primary); color: var(--color-bg-warm); border-color: var(--color-primary); font-weight: 500; }
.filter-divider { width: 1px; height: 20px; background: var(--color-divider-strong, rgba(74,53,38,0.18)); margin: 0 8px; }

/* ── TOOLBAR ── */
.toolbar { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 16px 40px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--color-divider); }
.result-count { font-size: .85rem; color: var(--color-text-light); }
.result-count strong { color: var(--color-text); }
.sort-select { display: flex; align-items: center; gap: 6px; position: relative; }
.sort-select label { font-size: .8rem; color: var(--color-text-light); white-space: nowrap; }
.sort-select select { appearance: none; background: var(--color-bg-card); border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 6px; padding: 7px 32px 7px 12px; font-size: .84rem; color: var(--color-text); cursor: pointer; outline: none; }
.sort-select::after { content: '▾'; position: absolute; right: 10px; top: 50%; transform: translateY(-50%); font-size: .7rem; color: var(--color-text-light); pointer-events: none; }

/* ── TIMELINE ── */
.timeline { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 40px 40px 20px; }
.timeline-date { display: flex; align-items: center; gap: 20px; margin-bottom: 28px; }
.timeline-date::before, .timeline-date::after { content: ''; flex: 1; height: 1px; background: var(--color-divider-strong, rgba(74,53,38,0.18)); }
.timeline-date-text { font-family: var(--font-display); font-size: 1rem; font-weight: 700; color: var(--color-primary); white-space: nowrap; letter-spacing: .04em; display: flex; align-items: center; gap: 8px; }
.today-tag { font-size: .68rem; font-weight: 700; background: var(--color-accent); color: var(--color-primary-abyss); padding: 2px 10px; border-radius: 10px; letter-spacing: .06em; }
.timeline-books { display: grid; grid-template-columns: repeat(5, 1fr); gap: 22px; margin-bottom: 16px; }
.timeline-group { margin-bottom: 20px; }
.timeline-group:last-child { margin-bottom: 0; }

/* ── BOOK CARDS ── */
.book-card { background: var(--color-bg-card); border-radius: 12px; overflow: hidden; border: 1px solid var(--color-divider); transition: all .4s cubic-bezier(.25,.46,.45,.94); cursor: pointer; position: relative; opacity: 0; animation: fadeUp .55s ease forwards; }
.book-card:hover { transform: translateY(-5px); box-shadow: 0 16px 40px var(--color-shadow-heavy, rgba(44,30,20,0.14)), 0 2px 6px var(--color-shadow, rgba(44,30,20,0.06)); border-color: rgba(192,154,75,0.15); }
.book-cover { position: relative; aspect-ratio: 3/4; overflow: hidden; }
.book-cover-img { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-weight: 700; font-size: 1.2rem; color: rgba(237,230,214,0.85); padding: 28px; text-align: center; line-height: 1.4; transition: transform .5s ease; object-fit: cover; }
.book-card:hover .book-cover-img { transform: scale(1.04); }
.book-cover-fallback { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 24px; text-align: center; box-shadow: 0 30px 60px rgba(0,0,0,0.35), inset 0 1px 0 rgba(192,154,75,0.08); border-left: 3px solid rgba(192,154,75,0.12); border-radius: 6px; }
.cover-title { font-family: var(--font-display); font-weight: 700; font-size: 1.6rem; color: rgba(237,230,214,0.85); text-shadow: 0 1px 4px rgba(0,0,0,0.2); word-break: break-all; line-height: 1.3; margin-bottom: 10px; }
.cover-author { font-size: .8rem; color: rgba(237,230,214,0.5); text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.book-badge { position: absolute; top: 10px; left: 10px; font-size: .63rem; font-weight: 700; padding: 3px 10px; border-radius: 4px; letter-spacing: .06em; box-shadow: 0 2px 6px rgba(0,0,0,0.15); }
.badge-new { background: var(--color-accent); color: var(--color-primary-abyss); }
.book-fav { position: absolute; top: 10px; right: 10px; width: 30px; height: 30px; background: rgba(46,31,21,0.55); backdrop-filter: blur(10px); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: .8rem; cursor: pointer; opacity: 0; transform: scale(.85); transition: all .3s; }
.book-card:hover .book-fav { opacity: 1; transform: scale(1); }
.book-fav:hover, .book-fav.active { background: var(--color-accent); color: var(--color-primary-abyss); }
.book-info { padding: 14px 16px 18px; }
.book-title { font-family: var(--font-display); font-size: .92rem; font-weight: 600; color: var(--color-text); margin-bottom: 3px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.book-author { font-size: .78rem; color: var(--color-text-light); margin-bottom: 10px; }
.book-meta { display: flex; align-items: center; justify-content: space-between; }
.book-price { font-family: var(--font-display); font-weight: 700; font-size: 1.05rem; color: var(--color-accent-muted); }
.book-rating { display: flex; align-items: center; gap: 2px; font-size: .72rem; color: var(--color-accent); }
.book-rating span { color: var(--color-text-light); margin-left: 2px; }
.book-rating .no-rating { font-size: .65rem; font-style: italic; color: var(--color-text-muted); margin-left: 0; }

/* ── LOAD MORE ── */
.load-more-wrap { text-align: center; padding: 12px 0 20px; }
.btn-load-more { display: inline-flex; align-items: center; gap: 8px; padding: 13px 48px; background: var(--color-bg-card); border: 1.5px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 10px; font-size: .9rem; font-weight: 500; color: var(--color-text-secondary); cursor: pointer; transition: all .3s; }
.btn-load-more:hover { border-color: var(--color-primary-mid, #5C4434); color: var(--color-text); transform: translateY(-2px); box-shadow: 0 6px 18px var(--color-shadow, rgba(44,30,20,0.06)); }
.spinner { display: none; width: 16px; height: 16px; border: 2px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-top-color: var(--color-accent); border-radius: 50%; animation: spin .8s linear infinite; }
.btn-load-more.loading .spinner { display: block; }
.btn-load-more.loading .lt { display: none; }
.btn-load-more:disabled { opacity: .6; cursor: not-allowed; }
@keyframes spin { to { transform: rotate(360deg); } }

/* ── SKELETON ── */
.skeleton { background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 6px; }
.book-skeleton { background: var(--color-bg-card); border-radius: 12px; border: 1px solid var(--color-divider); overflow: hidden; }
.sk-cover { aspect-ratio: 3/4; border-radius: 0; }
.sk-title { height: 20px; margin: 12px 16px 8px; }
.sk-author { height: 14px; margin: 0 16px 16px; width: 60%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }

/* ── EMPTY ── */
.empty-state { text-align: center; padding: 80px 0; max-width: var(--max-width, 1320px); margin: 0 auto; }
.empty-icon { font-size: 64px; margin-bottom: 16px; opacity: .5; }
.empty-state h3 { font-family: var(--font-display); font-size: 1.25rem; margin-bottom: 8px; color: var(--color-text); }
.empty-state p { font-size: .88rem; color: var(--color-text-light); }

/* ── COMING SOON ── */
.coming-soon { background: var(--color-bg-warm); border-top: 1px solid var(--color-divider); border-bottom: 1px solid var(--color-divider); margin-top: 40px; }
.coming-inner { max-width: var(--max-width, 1320px); margin: 0 auto; padding: 60px 40px; }
.coming-header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 36px; }
.coming-header h2 { font-family: var(--font-display); font-size: 1.6rem; color: var(--color-text); padding-left: 18px; position: relative; }
.coming-header h2::before { content: ''; position: absolute; left: 0; top: 4px; bottom: 4px; width: 3px; background: var(--color-accent); border-radius: 2px; }
.coming-header p { font-size: .88rem; color: var(--color-text-light); }
.coming-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 22px; }
.coming-card { background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; overflow: hidden; transition: all .35s; cursor: default; }
.coming-card:hover { transform: translateY(-4px); box-shadow: 0 14px 36px var(--color-shadow-heavy, rgba(44,30,20,0.14)); border-color: rgba(192,154,75,0.15); }
.coming-cover { aspect-ratio: 3/4; overflow: hidden; position: relative; }
.coming-cover-img { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-family: var(--font-display); font-weight: 700; font-size: 1.1rem; color: rgba(237,230,214,0.65); padding: 24px; text-align: center; opacity: .7; transition: all .4s; line-height: 1.4; }
.coming-card:hover .coming-cover-img { opacity: 1; transform: scale(1.03); }
.coming-countdown { position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(transparent, rgba(28,18,12,0.85)); padding: 24px 14px 12px; text-align: center; }
.cd-label { font-size: .68rem; color: rgba(237,230,214,0.5); margin-bottom: 4px; }
.cd-time { display: flex; align-items: center; justify-content: center; gap: 4px; }
.cd-num { font-family: 'DM Mono', monospace; font-size: 1.15rem; font-weight: 500; color: var(--color-accent-light, #D4B06A); background: rgba(192,154,75,0.12); padding: 2px 8px; border-radius: 4px; min-width: 32px; }
.cd-sep { color: rgba(237,230,214,0.3); font-size: .9rem; }
.cd-unit { font-size: .55rem; color: rgba(237,230,214,0.35); display: block; margin-top: 1px; }
.coming-info { padding: 14px 16px 18px; }
.ci-title { font-family: var(--font-display); font-size: .9rem; font-weight: 600; color: var(--color-text); margin-bottom: 2px; display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical; overflow: hidden; }
.ci-author { font-size: .76rem; color: var(--color-text-light); margin-bottom: 10px; }
.ci-meta { display: flex; align-items: center; justify-content: space-between; }
.ci-date { font-size: .76rem; color: var(--color-text-light); }
.btn-notify { display: flex; align-items: center; gap: 4px; padding: 6px 14px; background: transparent; border: 1px solid var(--color-divider-strong, rgba(74,53,38,0.18)); border-radius: 6px; font-size: .74rem; font-weight: 500; color: var(--color-text-secondary); cursor: pointer; transition: all .25s; }
.btn-notify:hover { border-color: var(--color-accent); color: var(--color-accent); background: var(--color-accent-glow, rgba(192,154,75,0.12)); }
.btn-notify.active { background: var(--color-accent); border-color: var(--color-accent); color: var(--color-primary-abyss); }

/* ── RESPONSIVE ── */
@media (max-width: 1200px) {
  .timeline-books { grid-template-columns: repeat(4, 1fr); }
  .coming-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 1024px) {
  .editor-inner { grid-template-columns: 1fr; text-align: center; }
  .editor-cover { margin-bottom: 24px; }
  .editor-text .et-desc { max-width: none; }
  .editor-meta { justify-content: center; }
  .editor-actions { justify-content: center; }
  .timeline-books { grid-template-columns: repeat(3, 1fr); }
  .coming-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 768px) {
  .page-header-inner { padding: 32px 20px 28px; }
  .page-header h1 { font-size: 1.6rem; }
  .filter-section { padding: 20px; }
  .toolbar { padding: 14px 20px; flex-direction: column; gap: 10px; align-items: flex-start; }
  .timeline { padding: 28px 20px 10px; }
  .timeline-books { grid-template-columns: repeat(2, 1fr); gap: 14px; }
  .coming-inner { padding: 40px 20px; }
  .coming-grid { grid-template-columns: repeat(2, 1fr); gap: 14px; }
  .editor-inner { padding: 40px 20px; }
  .page-header-stats { gap: 16px; flex-wrap: wrap; }
}
</style>
