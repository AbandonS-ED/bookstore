<template>
  <div class="ranking-page">
    <section class="ranking-hero">
      <div class="ranking-hero-inner">
        <span class="hero-bread">书斋 · 排行榜</span>
        <h1>书海排行</h1>
        <p>万千读者用阅读票选出的好书榜单</p>
      </div>
    </section>

    <div class="ranking-body">
      <div class="tab-bar">
        <div class="tab-bar-inner">
          <div class="rank-tabs">
            <button
              v-for="tab in rankTabs"
              :key="tab.key"
              :class="['tab-btn', { active: activeTab === tab.key }]"
              @click="switchTab(tab.key)"
            >
              {{ tab.label }}
              <span v-if="tab.key === activeTab" class="tab-glow" />
            </button>
          </div>
          <div class="period-tabs">
            <button
              v-for="p in periods"
              :key="p.key"
              :class="['period-btn', { active: activePeriod === p.key }]"
              @click="activePeriod = p.key"
            >
              {{ p.label }}
            </button>
          </div>
        </div>
      </div>

      <div class="ranking-content">
        <Transition name="rank-fade" mode="out-in">
          <div :key="activeTab + activePeriod" class="rank-inner">
            <div v-if="loading" class="ranking-loading">加载中...</div>
            <div v-if="!loading && books.length === 0" class="ranking-empty">暂无数据</div>
            <div v-if="topThree.length" class="top-three">
              <div class="top-card second" v-if="topThree[1]" @click="goToBook(topThree[1].id)">
                <div class="top-rank-badge silver">2</div>
                <div class="top-cover">
                  <img v-if="topThree[1].coverUrl && !coverErrors[topThree[1].id]" :src="topThree[1].coverUrl" :alt="topThree[1].title" class="top-cover-img" @error="coverErrors[topThree[1].id] = true" />
                  <div v-else class="top-cover-fallback" :style="getCoverStyle(topThree[1].id)">
                    <span class="top-fallback-title">{{ topThree[1].title }}</span>
                    <span class="top-fallback-author">{{ topThree[1].author }}</span>
                  </div>
                  <div class="top-rank-overlay">
                    <span class="top-rank-num">#2</span>
                  </div>
                </div>

                <div class="top-info">
                  <h3>{{ topThree[1].title }}</h3>
                  <p>{{ topThree[1].author }}</p>
                  <div class="top-price">¥{{ topThree[1].price }}</div>
                  <div class="top-stat">
                    <span class="stat-icon">{{ tabStatLabel }}</span>
                    {{ getStatValue(topThree[1]) }}
                  </div>
                </div>
              </div>

              <div class="top-card first" v-if="topThree[0]" @click="goToBook(topThree[0].id)">
                <div class="top-rank-badge gold">1</div>
                <div class="top-cover first-cover">
                  <img v-if="topThree[0].coverUrl && !coverErrors[topThree[0].id]" :src="topThree[0].coverUrl" :alt="topThree[0].title" class="top-cover-img" @error="coverErrors[topThree[0].id] = true" />
                  <div v-else class="top-cover-fallback" :style="getCoverStyle(topThree[0].id)">
                    <span class="top-fallback-title">{{ topThree[0].title }}</span>
                    <span class="top-fallback-author">{{ topThree[0].author }}</span>
                  </div>
                  <div class="top-rank-overlay">
                    <span class="top-rank-num">#1</span>
                  </div>
                </div>
                <div class="top-info">
                  <h3>{{ topThree[0].title }}</h3>
                  <p>{{ topThree[0].author }}</p>
                  <div class="top-price">¥{{ topThree[0].price }}</div>
                  <div class="top-stat">
                    <span class="stat-icon">{{ tabStatLabel }}</span>
                    {{ getStatValue(topThree[0]) }}
                  </div>
                </div>
              </div>

              <div class="top-card third" v-if="topThree[2]" @click="goToBook(topThree[2].id)">
                <div class="top-rank-badge bronze">3</div>
                <div class="top-cover">
                  <img v-if="topThree[2].coverUrl && !coverErrors[topThree[2].id]" :src="topThree[2].coverUrl" :alt="topThree[2].title" class="top-cover-img" @error="coverErrors[topThree[2].id] = true" />
                  <div v-else class="top-cover-fallback" :style="getCoverStyle(topThree[2].id)">
                    <span class="top-fallback-title">{{ topThree[2].title }}</span>
                    <span class="top-fallback-author">{{ topThree[2].author }}</span>
                  </div>
                  <div class="top-rank-overlay">
                    <span class="top-rank-num">#3</span>
                  </div>
                </div>
                <div class="top-info">
                  <h3>{{ topThree[2].title }}</h3>
                  <p>{{ topThree[2].author }}</p>
                  <div class="top-price">¥{{ topThree[2].price }}</div>
                  <div class="top-stat">
                    <span class="stat-icon">{{ tabStatLabel }}</span>
                    {{ getStatValue(topThree[2]) }}
                  </div>
                </div>
              </div>
            </div>

            <div class="rank-list">
              <div
                v-for="(book, idx) in rankedBooks"
                :key="book.id"
                class="rank-item"
                :style="{ animationDelay: `${idx * 40}ms` }"
                @click="goToBook(book.id)"
              >
                <div :class="['rank-num', { 'top-rank': idx < 3 }]">
                  {{ idx + 4 }}
                </div>
                <img v-if="book.coverUrl && !coverErrors[book.id]" :src="book.coverUrl" :alt="book.title" class="rank-item-img" @error="coverErrors[book.id] = true" />
                <div v-else class="rank-item-cover" :style="getCoverStyle(book.id)">
                  <span class="rank-fallback-title">{{ book.title }}</span>
                  <span class="rank-fallback-author">{{ book.author }}</span>
                </div>
                <div class="rank-item-info">
                  <div class="rank-item-title">{{ book.title }}</div>
                  <div class="rank-item-author">{{ book.author }}</div>
                </div>
                <div class="rank-item-price">¥{{ book.price }}</div>
                <div class="rank-item-stat">
                  <span class="stat-icon-sm">{{ tabStatIcon }}</span>
                  {{ getStatValue(book) }}
                </div>
                <div class="rank-item-rating" v-if="book.avgRating">
                  ★ {{ book.avgRating?.toFixed(1) }}
                </div>
              </div>
            </div>

            <div v-if="hasMore" class="load-more">
              <button class="btn-load" @click="loadMore">加载更多 · 第{{ rankedBooks.length + 4 }}~{{ Math.min(rankedBooks.length + 23, books.length) }}名</button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi } from '@/api/book'
import { getCoverStyle } from '@/utils/cover'

const router = useRouter()
const displayCount = ref(17)
const books = ref([])
const loading = ref(false)
const coverErrors = ref({})

const rankTabs = [
  { key: 'sales', label: '畅销总榜' },
  { key: 'rating', label: '好评榜' },
  { key: 'new', label: '新锐榜' }
]

const periods = [
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
  { key: 'quarter', label: '本季' },
  { key: 'year', label: '全年' },
  { key: 'all', label: '总榜' }
]

const activeTab = ref('sales')
const activePeriod = ref('all')

const tabStatLabel = computed(() => {
  const map = { sales: '销量', rating: '评分', new: '上架时间' }
  return map[activeTab.value] || '销量'
})

const tabStatIcon = computed(() => {
  const map = { sales: '📈', rating: '⭐', new: '🔥' }
  return map[activeTab.value] || '📊'
})

const getStatValue = (book) => {
  switch (activeTab.value) {
    case 'rating': return `${book.avgRating?.toFixed(1) || '0.0'}分`
    case 'new': return book.publishDate || '近期'
    default: return `${book.sales || 0}册`
  }
}

const topThree = computed(() => books.value.slice(0, 3))
const rankedBooks = computed(() => books.value.slice(3, 3 + displayCount.value))
const hasMore = computed(() => 3 + displayCount.value < books.value.length)

const loadMore = () => { displayCount.value += 17 }

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
  margin-top: calc(-1 * var(--header-height));
}

.ranking-hero {
  background: var(--color-primary-abyss);
  position: relative;
  overflow: hidden;
  padding: 100px 0 80px;
}

.ranking-hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 50%, rgba(192,154,75,0.06) 0%, transparent 50%),
    radial-gradient(ellipse at 70% 80%, rgba(92,68,52,0.2) 0%, transparent 40%);
  pointer-events: none;
}

.ranking-hero-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 40px;
  position: relative;
  z-index: 1;
}

.hero-bread {
  font-size: 0.78rem;
  color: rgba(192,154,75,0.6);
  letter-spacing: 0.12em;
  margin-bottom: 12px;
  display: block;
}

.ranking-hero h1 {
  font-size: 2.6rem;
  color: var(--color-bg-warm);
  margin-bottom: 10px;
}

.ranking-hero p {
  color: rgba(237,230,214,0.4);
  font-size: 1rem;
}

.ranking-body {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 40px 80px;
}

.tab-bar {
  position: sticky;
  top: var(--header-height);
  z-index: 50;
  background: var(--color-bg);
  padding: 16px 0;
  border-bottom: 1px solid var(--color-divider);
  margin-bottom: 40px;
}

.tab-bar-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.rank-tabs {
  display: flex;
  gap: 4px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: 10px;
  padding: 4px;
}

.tab-btn {
  position: relative;
  padding: 10px 20px;
  font-size: 0.88rem;
  font-weight: 500;
  color: var(--color-text-secondary);
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: var(--font-body);
  white-space: nowrap;
}

.tab-btn:hover { color: var(--color-text); }

.tab-btn.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
}

.period-tabs {
  display: flex;
  gap: 2px;
  background: var(--color-bg-card);
  border-radius: 8px;
  padding: 3px;
  border: 1px solid var(--color-divider);
}

.period-btn {
  padding: 7px 16px;
  font-size: 0.82rem;
  color: var(--color-text-light);
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.25s ease;
  font-family: var(--font-body);
}

.period-btn:hover { color: var(--color-text); }

.period-btn.active {
  background: var(--color-bg-cream);
  color: var(--color-accent-muted);
  font-weight: 600;
}

.rank-inner {
  animation: fadeIn 0.4s ease;
}

/* ── Top 3 ── */
.top-three {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 24px;
  align-items: start;
  margin-bottom: 50px;
}

.top-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
  padding: 24px 16px;
  border-radius: 14px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  cursor: pointer;
}

.top-card.first {
  padding-top: 36px;
  border-color: rgba(192,154,75,0.15);
  box-shadow: 0 0 0 1px rgba(192,154,75,0.06), 0 8px 32px var(--color-shadow);
  transform: scale(1.04);
}

.top-card.second, .top-card.third {
  margin-top: 20px;
}

.top-rank-badge {
  position: absolute;
  top: -16px;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 900;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.top-rank-badge.gold {
  background: linear-gradient(135deg, #C09A4B, #A68638);
  color: #1C120C;
  width: 52px;
  height: 52px;
  font-size: 1.3rem;
  top: -24px;
}

.top-rank-badge.silver {
  background: linear-gradient(135deg, #C5C5C5, #9E9E9E);
  color: #2E1F15;
}

.top-rank-badge.bronze {
  background: linear-gradient(135deg, #CD7F32, #A0522D);
  color: #F5F0E8;
}

.top-cover {
  width: 140px;
  height: 200px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.2rem;
  color: rgba(237,230,214,0.85);
  position: relative;
  margin-bottom: 16px;
  overflow: hidden;
  box-shadow: 0 8px 24px var(--color-shadow-heavy);
}

.top-cover-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px;
}

.top-cover-fallback {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 12px;
  border-radius: 6px;
  text-align: center;
}

.top-fallback-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.85rem;
  color: rgba(237,230,214,0.95);
  line-height: 1.3;
  word-break: break-word;
}

.top-fallback-author {
  font-family: var(--font-body);
  font-size: 0.65rem;
  color: rgba(237,230,214,0.55);
  line-height: 1.2;
}

.first-cover {
  width: 160px;
  height: 230px;
  font-size: 1.3rem;
  box-shadow: 0 0 0 2px rgba(192,154,75,0.3), 0 12px 36px var(--color-shadow-deep);
}

.top-rank-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: linear-gradient(transparent, rgba(0,0,0,0.4));
}

.top-rank-num {
  font-size: 0.72rem;
  color: rgba(237,230,214,0.7);
  font-weight: 400;
}

.top-info h3 {
  font-size: 1rem;
  color: var(--color-text);
  margin-bottom: 4px;
}

.top-info p {
  font-size: 0.8rem;
  color: var(--color-text-light);
  margin-bottom: 8px;
}

.top-price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.2rem;
  color: var(--color-accent-muted);
  margin-bottom: 6px;
}

.top-stat {
  font-size: 0.8rem;
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
}

.stat-icon { font-size: 0.75rem; }

/* ── Rank List ── */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 1px;
  background: var(--color-divider);
  border: 1px solid var(--color-divider);
  border-radius: 12px;
  overflow: hidden;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  background: var(--color-bg-card);
  cursor: pointer;
  transition: background 0.2s ease;
  animation: slideInLeft 0.4s ease backwards;
}

.rank-item:hover {
  background: var(--color-bg-cream);
}

.rank-num {
  width: 32px;
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--color-text-light);
  text-align: center;
  flex-shrink: 0;
}

.rank-num.top-rank {
  color: var(--color-accent-muted);
}

.rank-item-cover {
  width: 52px;
  height: 72px;
  border-radius: 4px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  padding: 4px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.rank-fallback-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.6rem;
  color: rgba(237,230,214,0.9);
  line-height: 1.2;
  word-break: break-word;
}

.rank-fallback-author {
  font-family: var(--font-body);
  font-size: 0.5rem;
  color: rgba(237,230,214,0.5);
  line-height: 1.1;
}

.rank-item-cover span {
  text-shadow: 0 1px 3px rgba(0,0,0,0.2);
}

.rank-item-img {
  width: 52px;
  height: 72px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.rank-item-info {
  flex: 1;
  min-width: 0;
}

.rank-item-title {
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 0.92rem;
  color: var(--color-text);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-item-author {
  font-size: 0.78rem;
  color: var(--color-text-light);
}

.rank-item-price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.95rem;
  color: var(--color-accent-muted);
  flex-shrink: 0;
  min-width: 60px;
  text-align: right;
}

.rank-item-stat {
  font-size: 0.78rem;
  color: var(--color-text-secondary);
  flex-shrink: 0;
  min-width: 70px;
  text-align: right;
}

.stat-icon-sm { margin-right: 3px; }

.rank-item-rating {
  font-size: 0.78rem;
  color: var(--color-accent);
  min-width: 50px;
  text-align: right;
  flex-shrink: 0;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.btn-load {
  padding: 12px 36px;
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider-strong);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-family: var(--font-body);
}

.btn-load:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: rgba(192,154,75,0.04);
}

.rank-fade-enter-active { transition: all 0.35s ease; }
.rank-fade-leave-active { transition: all 0.2s ease; }
.rank-fade-enter-from { opacity: 0; transform: translateY(10px); }
.rank-fade-leave-to { opacity: 0; transform: translateY(-10px); }

/* Responsive */
@media (max-width: 1024px) {
  .top-three { grid-template-columns: repeat(3, 1fr); gap: 16px; }
  .top-card.first { transform: none; }
  .first-cover { width: 140px; height: 200px; }
}

@media (max-width: 768px) {
  .ranking-hero { padding: 60px 0 50px; }
  .ranking-hero h1 { font-size: 1.8rem; }
  .ranking-body { padding: 0 20px 60px; }
  .tab-bar-inner { flex-direction: column; gap: 12px; }
  .rank-tabs { width: 100%; overflow-x: auto; }
  .tab-btn { flex-shrink: 0; }
  .period-tabs { width: 100%; justify-content: center; }
  .top-three { grid-template-columns: 1fr; gap: 20px; }
  .top-card.second, .top-card.third { margin-top: 0; }
  .rank-item { flex-wrap: wrap; gap: 10px; }
  .rank-item-stat { min-width: auto; }
  .rank-item-rating { display: none; }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-15px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
