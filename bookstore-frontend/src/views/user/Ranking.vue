<template>
  <div class="ranking-page">
    <section class="page-header">
      <div class="page-header-inner">
        <div class="rank-bc">
          <a href="/" @click.prevent="router.push('/')">首页</a>
          <span class="rank-bc-sep">/</span>
          <span>排行榜</span>
        </div>
        <h1>排行榜</h1>
        <p class="ph-desc">万千读者的选择，帮你找到值得花时间的好书。基于真实销量、评分和收藏数据每日更新。</p>
        <div class="page-header-stats">
          <div class="ph-stat"><strong>{{ rankTabs.length }}</strong><span>个榜单</span></div>
          <div class="ph-stat"><strong>{{ totalBooks }}</strong><span>上榜书籍</span></div>
          <div class="ph-stat"><strong>每日</strong><span>实时更新</span></div>
        </div>
      </div>
    </section>

    <div class="ranking-tabs-bar">
      <div class="ranking-tabs-inner">
        <div class="ranking-tabs">
          <button
            v-for="tab in rankTabs"
            :key="tab.key"
            :class="['ranking-tab', { active: activeTab === tab.key }]"
            @click="switchTab(tab.key)"
          >
            {{ tab.icon }} {{ tab.label }}
          </button>
        </div>
        <div class="time-filters">
          <button
            v-for="p in periods"
            :key="p.key"
            :class="['tf-pill', { active: activePeriod === p.key }]"
            @click="activePeriod = p.key"
          >
            {{ p.label }}
          </button>
        </div>
      </div>
    </div>

    <div class="ranking-body">
      <Transition name="rank-fade" mode="out-in">
        <div :key="activeTab + activePeriod" class="tab-content">
          <div v-if="loading" class="ranking-loading">加载中...</div>
          <div v-else-if="books.length === 0" class="ranking-empty">暂无数据</div>
          <template v-else>
            <section v-if="topThree.length" class="top3-section">
              <div class="top3-grid">
                <div v-if="topThree[1]" class="top3-card side" :style="{ animationDelay: '0.05s' }" @click="goToBook(topThree[1].id)" @mouseenter="hoveredBookId = topThree[1].id" @mouseleave="hoveredBookId = null">
                  <div class="top3-rank-badge r2">2</div>
                  <div class="book-cover">
                    <img v-if="topThree[1].coverUrl && !coverErrors[topThree[1].id]" class="book-cover-img" :src="topThree[1].coverUrl" :alt="topThree[1].title" @error="coverErrors[topThree[1].id] = true" />
                    <div v-if="!topThree[1].coverUrl || coverErrors[topThree[1].id]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(topThree[1].id)">
                      <span class="cover-title">{{ topThree[1].title }}</span>
                      <span class="cover-author">{{ topThree[1].author }}</span>
                    </div>
                    <transition name="quote-fade">
                      <div v-if="topThree[1].quote && hoveredBookId === topThree[1].id" class="book-quote-overlay">
                        <div class="quote-content">
                          <svg class="quote-mark quote-mark-left" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
                          </svg>
                          <p class="quote-text">{{ topThree[1].quote }}</p>
                          <svg class="quote-mark quote-mark-right" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M18 7h-3l-2 4v6h6v-6h-3l2-4zm-8 0H7L5 11v6h6v-6H8l2-4z" fill="currentColor"/>
                          </svg>
                        </div>
                      </div>
                    </transition>
                  </div>
                  <div class="book-info">
                    <div class="book-title">{{ topThree[1].title }}</div>
                    <div class="book-author">{{ topThree[1].author }}</div>
                    <div class="book-meta">
                      <div class="book-price">¥{{ topThree[1].price }}</div>
                      <div class="book-rating">
                        <template v-if="topThree[1].avgRating">
                          <span class="stars">{{ renderStars(topThree[1].avgRating) }}</span>
                          <span>{{ topThree[1].avgRating.toFixed(1) }}</span>
                        </template>
                        <span v-else class="no-rating">暂无评分</span>
                      </div>
                    </div>
                    <div class="book-stat">{{ currentTabConfig.statLabel }}：{{ getStatValue(topThree[1]) }}</div>
                  </div>
                </div>

                <div v-if="topThree[0]" class="top3-card first" :style="{ animationDelay: '0s' }" @click="goToBook(topThree[0].id)" @mouseenter="hoveredBookId = topThree[0].id" @mouseleave="hoveredBookId = null">
                  <div class="top3-rank-badge r1">1</div>
                  <div class="book-cover">
                    <img v-if="topThree[0].coverUrl && !coverErrors[topThree[0].id]" class="book-cover-img" :src="topThree[0].coverUrl" :alt="topThree[0].title" @error="coverErrors[topThree[0].id] = true" />
                    <div v-if="!topThree[0].coverUrl || coverErrors[topThree[0].id]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(topThree[0].id)">
                      <span class="cover-title">{{ topThree[0].title }}</span>
                      <span class="cover-author">{{ topThree[0].author }}</span>
                    </div>
                    <transition name="quote-fade">
                      <div v-if="topThree[0].quote && hoveredBookId === topThree[0].id" class="book-quote-overlay">
                        <div class="quote-content">
                          <svg class="quote-mark quote-mark-left" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
                          </svg>
                          <p class="quote-text">{{ topThree[0].quote }}</p>
                          <svg class="quote-mark quote-mark-right" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M18 7h-3l-2 4v6h6v-6h-3l2-4zm-8 0H7L5 11v6h6v-6H8l2-4z" fill="currentColor"/>
                          </svg>
                        </div>
                      </div>
                    </transition>
                  </div>
                  <div class="book-info">
                    <div class="book-title">{{ topThree[0].title }}</div>
                    <div class="book-author">{{ topThree[0].author }}</div>
                    <div class="book-meta">
                      <div class="book-price">¥{{ topThree[0].price }}</div>
                      <div class="book-rating">
                        <template v-if="topThree[0].avgRating">
                          <span class="stars">{{ renderStars(topThree[0].avgRating) }}</span>
                          <span>{{ topThree[0].avgRating.toFixed(1) }}</span>
                        </template>
                        <span v-else class="no-rating">暂无评分</span>
                      </div>
                    </div>
                    <div class="book-stat">{{ currentTabConfig.statLabel }}：{{ getStatValue(topThree[0]) }}</div>
                  </div>
                </div>

                <div v-if="topThree[2]" class="top3-card side" :style="{ animationDelay: '0.1s' }" @click="goToBook(topThree[2].id)" @mouseenter="hoveredBookId = topThree[2].id" @mouseleave="hoveredBookId = null">
                  <div class="top3-rank-badge r3">3</div>
                  <div class="book-cover">
                    <img v-if="topThree[2].coverUrl && !coverErrors[topThree[2].id]" class="book-cover-img" :src="topThree[2].coverUrl" :alt="topThree[2].title" @error="coverErrors[topThree[2].id] = true" />
                    <div v-if="!topThree[2].coverUrl || coverErrors[topThree[2].id]" class="book-cover-img book-cover-fallback" :style="getCoverStyle(topThree[2].id)">
                      <span class="cover-title">{{ topThree[2].title }}</span>
                      <span class="cover-author">{{ topThree[2].author }}</span>
                    </div>
                    <transition name="quote-fade">
                      <div v-if="topThree[2].quote && hoveredBookId === topThree[2].id" class="book-quote-overlay">
                        <div class="quote-content">
                          <svg class="quote-mark quote-mark-left" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
                          </svg>
                          <p class="quote-text">{{ topThree[2].quote }}</p>
                          <svg class="quote-mark quote-mark-right" viewBox="0 0 24 24" width="20" height="20">
                            <path d="M18 7h-3l-2 4v6h6v-6h-3l2-4zm-8 0H7L5 11v6h6v-6H8l2-4z" fill="currentColor"/>
                          </svg>
                        </div>
                      </div>
                    </transition>
                  </div>
                  <div class="book-info">
                    <div class="book-title">{{ topThree[2].title }}</div>
                    <div class="book-author">{{ topThree[2].author }}</div>
                    <div class="book-meta">
                      <div class="book-price">¥{{ topThree[2].price }}</div>
                      <div class="book-rating">
                        <template v-if="topThree[2].avgRating">
                          <span class="stars">{{ renderStars(topThree[2].avgRating) }}</span>
                          <span>{{ topThree[2].avgRating.toFixed(1) }}</span>
                        </template>
                        <span v-else class="no-rating">暂无评分</span>
                      </div>
                    </div>
                    <div class="book-stat">{{ currentTabConfig.statLabel }}：{{ getStatValue(topThree[2]) }}</div>
                  </div>
                </div>
              </div>
            </section>

            <div class="rank-list">
              <div class="rank-list-header">
                <div style="text-align:center;">排名</div>
                <div>封面</div>
                <div>书籍</div>
                <div style="text-align:right;">售价</div>
                <div style="text-align:center;">评分</div>
                <div class="rh-stat">{{ currentTabConfig.statLabel }}</div>
                <div class="rh-quote">名言</div>
              </div>
              <div
                v-for="(book, idx) in rankedBooks"
                :key="book.id"
                class="rank-row"
                :style="{ animationDelay: `${idx * 40}ms` }"
                @click="goToBook(book.id)"
              >
                <div :class="['rank-num', { 'num-top': idx < 3 }]">
                  {{ idx + 4 }}
                </div>
                <img v-if="book.coverUrl && !coverErrors[book.id]" :src="book.coverUrl" :alt="book.title" class="rank-cover-img" @error="coverErrors[book.id] = true" />
                <div v-else class="rank-cover-fallback" :style="getCoverStyle(book.id)">
                  <span class="rc-title">{{ book.title }}</span>
                </div>
                <div class="rank-book-info">
                  <div class="rb-title">{{ book.title }}</div>
                  <div class="rb-author">{{ book.author }}</div>
                </div>
                <div class="rank-price">¥{{ book.price }}</div>
                <div class="rank-rating">
                  <template v-if="book.avgRating">
                    <span class="stars">{{ renderStars(book.avgRating) }}</span>
                    <span>{{ book.avgRating.toFixed(1) }}</span>
                  </template>
                  <span v-else class="no-rating">暂无评分</span>
                </div>
                <div class="rank-stat-col">
                  <span class="stat-val">{{ getStatValue(book) }}</span>
                </div>
                <div v-if="book.quote" class="rank-quote-col">
                  <svg class="rq-mark" viewBox="0 0 24 24" width="14" height="14">
                    <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
                  </svg>
                  <p class="rq-text">{{ book.quote }}</p>
                </div>
                <div v-else class="rank-quote-col"></div>
              </div>
            </div>

            <div v-if="hasMore" class="load-more-wrap">
              <button class="btn-load-more" @click="loadMore">
                <span>查看第{{ rankedBooks.length + 4 }}~{{ Math.min(rankedBooks.length + 17, books.length) }}名</span>
              </button>
            </div>
          </template>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi } from '@/api/book'
import { getCoverStyle } from '@/utils/cover'

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

const router = useRouter()
const displayCount = ref(17)
const books = ref([])
const loading = ref(false)
const coverErrors = ref({})
const hoveredBookId = ref(null)

const rankTabs = [
  { key: 'sales', label: '畅销总榜', icon: '📈' },
  { key: 'rating', label: '好评榜', icon: '⭐' },
  { key: 'new', label: '新锐榜', icon: '🚀' },
  { key: 'collection', label: '收藏榜', icon: '❤️' }
]

const tabConfig = {
  sales: { statLabel: '销量', statFormat: v => `${v || 0}册` },
  rating: { statLabel: '评分', statFormat: v => `${v?.toFixed(1) || '0.0'}分` },
  new: { statLabel: '上架时间', statFormat: v => v || '近期' },
  collection: { statLabel: '收藏量', statFormat: v => `${v || 0}` }
}

const periods = [
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
  { key: 'quarter', label: '本季' },
  { key: 'year', label: '全年' },
  { key: 'all', label: '总榜' }
]

const activeTab = ref('sales')
const activePeriod = ref('all')

const currentTabConfig = computed(() => tabConfig[activeTab.value])

const totalBooks = computed(() => books.value.length)

const getStatValue = (book) => {
  const cfg = currentTabConfig.value
  switch (activeTab.value) {
    case 'rating':
      return cfg.statFormat(book.avgRating)
    case 'new':
      return cfg.statFormat(book.publishDate)
    case 'collection':
      return cfg.statFormat(book.favoritedCount)
    default:
      return cfg.statFormat(book.sales)
  }
}

const topThree = computed(() => books.value.slice(0, 3))
const rankedBooks = computed(() => books.value.slice(3, 3 + displayCount.value))
const hasMore = computed(() => 3 + displayCount.value < books.value.length)

const loadMore = () => {
  displayCount.value += 17
}

const fetchRanking = async () => {
  loading.value = true
  try {
    const res = await bookApi.getRanking({ type: activeTab.value, period: activePeriod.value })
    books.value = res.data || []
  } catch (e) {
    console.error('Failed to fetch ranking:', e)
    books.value = []
  } finally {
    loading.value = false
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  displayCount.value = 17
}

watch(activeTab, () => { displayCount.value = 17; fetchRanking() })
watch(activePeriod, () => { displayCount.value = 17; fetchRanking() })

onMounted(fetchRanking)

const goToBook = (id) => router.push(`/book/${id}`)
</script>

<style scoped>
.ranking-page {
  background: var(--color-bg);
}

/* ═══ PAGE HEADER ═══ */
.page-header {
  background: var(--color-primary-dark);
  position: relative;
  overflow: hidden;
  z-index: 1;
}

.page-header::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 70% 30%, rgba(192,154,75,0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 20% 80%, rgba(92,68,52,0.15) 0%, transparent 45%);
  pointer-events: none;
}

.page-header::after {
  content: '';
  position: absolute;
  inset: 0;
  background: repeating-linear-gradient(
    90deg,
    transparent,
    transparent 90px,
    rgba(192,154,75,0.012) 90px,
    rgba(192,154,75,0.012) 91px
  );
  pointer-events: none;
}

.page-header-inner {
  position: relative;
  z-index: 1;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 48px 40px 40px;
}

.rank-bc {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  color: rgba(237,230,214,0.35);
  margin-bottom: 16px;
}

.rank-bc a {
  color: rgba(237,230,214,0.45);
  text-decoration: none;
}

.rank-bc a:hover {
  color: var(--color-accent);
}

.rank-bc-sep {
  margin: 0 4px;
  opacity: 0.5;
}

.page-header h1 {
  font-size: 2.2rem;
  color: var(--color-bg-warm);
  font-weight: 900;
  margin-bottom: 8px;
  letter-spacing: 0.02em;
}

.ph-desc {
  color: rgba(237,230,214,0.4);
  font-size: 0.93rem;
  max-width: 600px;
  margin-bottom: 22px;
}

.page-header-stats {
  display: flex;
  gap: 36px;
}

.ph-stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.ph-stat strong {
  font-family: var(--font-display);
  font-size: 1.25rem;
  font-weight: 900;
  color: var(--color-accent-light);
}

.ph-stat span {
  font-size: 0.78rem;
  color: rgba(237,230,214,0.32);
}

/* ═══ TABS BAR ═══ */
.ranking-tabs-bar {
  position: sticky;
  top: var(--header-height);
  z-index: 50;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-divider);
}

.ranking-tabs-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.ranking-tabs {
  display: flex;
  gap: 0;
  overflow-x: auto;
}

.ranking-tab {
  padding: 16px 18px;
  font-size: 0.88rem;
  font-weight: 400;
  color: var(--color-text-light);
  background: transparent;
  border: none;
  border-bottom: 3px solid transparent;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.25s;
  font-family: var(--font-body);
}

.ranking-tab:hover {
  color: var(--color-text);
}

.ranking-tab.active {
  color: var(--color-primary);
  font-weight: 600;
  border-bottom-color: var(--color-accent);
}

.time-filters {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.tf-pill {
  padding: 6px 14px;
  background: var(--color-bg);
  border: 1px solid var(--color-divider-strong);
  border-radius: 20px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
  color: var(--color-text-secondary);
  font-weight: 400;
  font-family: var(--font-body);
  white-space: nowrap;
}

.tf-pill:hover {
  border-color: var(--color-primary-mid);
  color: var(--color-text);
}

.tf-pill.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
  font-weight: 500;
}

/* ═══ BODY ═══ */
.ranking-body {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 40px 80px;
}

.tab-content {
  animation: fadeIn 0.35s ease;
}

.ranking-loading,
.ranking-empty {
  text-align: center;
  padding: 80px 20px;
  color: var(--color-text-light);
  font-size: 1rem;
}

/* ═══ TOP 3 ═══ */
.top3-section {
  padding: 32px 0 24px;
}

.top3-grid {
  display: grid;
  grid-template-columns: 1fr 1.15fr 1fr;
  gap: 28px;
  align-items: end;
  max-width: 840px;
  margin: 0 auto;
}

.top3-card {
  position: relative;
  background: var(--color-bg-card);
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  opacity: 0;
  animation: fadeUp 0.45s ease forwards;
}

.top3-card.side {
  margin-bottom: 20px;
}

.top3-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px var(--color-shadow-heavy);
  border-color: rgba(192,154,75,0.12);
}

.top3-card.first {
  border-color: rgba(192,154,75,0.2);
  box-shadow: 0 0 0 1px rgba(192,154,75,0.08), 0 4px 20px var(--color-shadow);
}

.top3-card.first:hover {
  box-shadow: 0 16px 40px var(--color-shadow-heavy), 0 0 0 1px rgba(192,154,75,0.2);
}

.top3-rank-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  z-index: 2;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 0.75rem;
  font-weight: 900;
  box-shadow: 0 2px 8px rgba(0,0,0,0.3);
  line-height: 1;
}

.top3-rank-badge.r1 {
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  color: var(--color-primary-abyss);
  width: 30px;
  height: 30px;
  font-size: 0.9rem;
  box-shadow: 0 2px 10px rgba(192,154,75,0.4);
}

.top3-rank-badge.r2 {
  background: linear-gradient(135deg, #A8A8A8, #7A7A7A);
  color: var(--color-bg-warm);
}

.top3-rank-badge.r3 {
  background: linear-gradient(135deg, #C07A4B, #9A5E36);
  color: var(--color-bg-warm);
}

.top3-card .book-cover {
  position: relative;
  overflow: hidden;
  aspect-ratio: 3 / 3.6;
}

.top3-card .book-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.45s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.95rem;
  color: rgba(237,230,214,0.85);
  padding: 20px;
  text-align: center;
  line-height: 1.3;
}

.top3-card:hover .book-cover-img {
  transform: scale(1.04);
}

.book-cover-fallback {
  background: linear-gradient(160deg, #5D4037, #3E2723, #2C1A12);
  flex-direction: column;
  gap: 4px;
}

.cover-author {
  color: rgba(237,230,214,0.5);
  font-size: 0.65rem;
  font-family: var(--font-body);
}

.top3-card .book-info {
  padding: 10px 12px 12px;
}

.top3-card.first .book-info {
  padding: 12px 14px 14px;
}

.top3-card .book-title {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 0.85rem;
  color: var(--color-text);
  margin-bottom: 1px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top3-card.first .book-title {
  font-size: 0.9rem;
}

.top3-card .book-author {
  font-size: 0.7rem;
  color: var(--color-text-light);
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top3-card .book-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top3-card .book-price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.9rem;
  color: var(--color-accent-muted);
}

.top3-card.first .book-price {
  font-size: 0.95rem;
}

.top3-card .book-rating {
  font-size: 0.68rem;
  color: var(--color-accent);
  display: flex;
  align-items: center;
  gap: 2px;
}
.top3-card .book-rating .no-rating,
.rank-rating .no-rating {
  font-size: 0.65rem;
  font-style: italic;
  color: var(--color-text-light);
}

.top3-card .book-stat {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px solid var(--color-divider);
  font-size: 0.65rem;
  color: var(--color-text-light);
}

/* ═══ QUOTE OVERLAY ═══ */
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

/* ═══ RANK LIST ═══ */
.rank-list {
  padding: 12px 0 20px;
}

.rank-list-header {
  display: grid;
  grid-template-columns: 50px 80px 1fr 100px 80px 100px 220px;
  gap: 16px;
  padding: 10px 20px;
  font-size: 0.72rem;
  font-weight: 500;
  color: var(--color-text-light);
  letter-spacing: 0.06em;
  border-bottom: 1px solid var(--color-divider);
  margin-bottom: 4px;
}

.rh-stat {
  text-align: right;
}

.rank-row {
  display: grid;
  grid-template-columns: 50px 80px 1fr 100px 80px 100px 220px;
  gap: 16px;
  padding: 14px 20px;
  align-items: center;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: 10px;
  margin-bottom: 6px;
  transition: all 0.3s;
  cursor: pointer;
  animation: fadeUp 0.4s ease both;
}

.rank-row:hover {
  transform: translateX(4px);
  box-shadow: 0 8px 24px var(--color-shadow);
  border-color: rgba(192,154,75,0.12);
}

.rank-num {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 900;
  color: var(--color-divider-strong);
  text-align: center;
}

.rank-row:nth-child(2) .rank-num { color: var(--color-accent); }
.rank-row:nth-child(3) .rank-num { color: var(--color-primary-mid); }
.rank-row:nth-child(4) .rank-num { color: var(--color-primary-light); }

.rank-cover-img {
  width: 56px;
  height: 76px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}

.rank-cover-fallback {
  width: 56px;
  height: 76px;
  border-radius: 6px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.55rem;
  color: rgba(237,230,214,0.7);
  padding: 6px;
  text-align: center;
  background: linear-gradient(160deg, #5D4037, #3E2723);
}

.rc-title {
  word-break: break-all;
  line-height: 1.2;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.rank-book-info {
  min-width: 0;
}

.rb-title {
  font-family: var(--font-display);
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--color-text);
  word-break: break-word;
  line-height: 1.3;
}

.rb-author {
  font-size: 0.76rem;
  color: var(--color-text-light);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1rem;
  color: var(--color-accent-muted);
  text-align: right;
}

.rank-rating {
  font-size: 0.78rem;
  color: var(--color-accent);
  display: flex;
  align-items: center;
  gap: 2px;
  text-align: center;
  justify-content: center;
}

.rank-stat-col {
  font-size: 0.82rem;
  color: var(--color-text-secondary);
  text-align: right;
}

.rank-stat-col .stat-val {
  font-weight: 500;
}

.rh-quote {
  font-size: 0.72rem;
  font-weight: 500;
  color: var(--color-text-light);
  letter-spacing: 0.06em;
}
.rank-quote-col {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 0;
  border-left: 1px solid var(--color-divider);
  padding-left: 16px;
  overflow: hidden;
  align-self: center;
}
.rq-mark {
  flex-shrink: 0;
  margin-top: 3px;
  color: var(--color-accent);
  opacity: 0.5;
}
.rq-text {
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

/* ═══ LOAD MORE ═══ */
.load-more-wrap {
  padding: 8px 0 20px;
  text-align: center;
}

.btn-load-more {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 13px 48px;
  background: var(--color-bg-card);
  border: 1.5px solid var(--color-divider-strong);
  border-radius: 10px;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  font-family: var(--font-body);
  transition: all 0.3s;
}

.btn-load-more:hover {
  border-color: var(--color-primary-mid);
  color: var(--color-text);
  transform: translateY(-2px);
  box-shadow: 0 6px 18px var(--color-shadow);
}

/* ═══ ANIMATIONS ═══ */
.rank-fade-enter-active { transition: all 0.35s ease; }
.rank-fade-leave-active { transition: all 0.2s ease; }
.rank-fade-enter-from { opacity: 0; transform: translateY(10px); }
.rank-fade-leave-to { opacity: 0; transform: translateY(-10px); }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ═══ RESPONSIVE ═══ */
@media (max-width: 1024px) {
  .top3-grid { gap: 20px; }
  .top3-card.side { margin-bottom: 14px; }
  .rank-list-header,
  .rank-row { grid-template-columns: 40px 64px 1fr 80px 70px; gap: 12px; }
  .rh-stat, .rh-quote,
  .rank-stat-col, .rank-quote-col { display: none; }
}

@media (max-width: 768px) {
  .page-header-inner { padding: 32px 20px 28px; }
  .page-header h1 { font-size: 1.5rem; }
  .page-header-stats { gap: 20px; }
  .ranking-tabs-inner { padding: 0 16px; flex-direction: column; gap: 0; }
  .ranking-tab { padding: 14px 12px; font-size: 0.82rem; }
  .time-filters { padding: 8px 0 12px; gap: 4px; flex-wrap: wrap; justify-content: center; }
  .tf-pill { padding: 5px 12px; font-size: 0.76rem; }
  .ranking-body { padding: 0 20px 60px; }
  .top3-section { padding: 16px 0 10px; }
  .top3-grid { max-width: 100%; grid-template-columns: 1fr; gap: 12px; }
  .top3-card.side { margin-bottom: 0; }
  .rank-list { padding: 10px 0 16px; }
  .rank-list-header { display: none; }
  .rank-row { grid-template-columns: 36px 52px 1fr; gap: 10px; padding: 12px 14px; }
  .rank-price,
  .rank-rating,
  .rank-stat-col { display: none; }
}
</style>
