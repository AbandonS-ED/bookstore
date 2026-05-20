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
            <div v-if="topThree.length" class="top-three">
              <div class="top-card second" v-if="topThree[1]">
                <div class="top-rank-badge silver">2</div>
                <div class="top-cover" :class="getCoverClass(topThree[1])">
                  <div class="top-cover-text">{{ topThree[1].title.slice(0, 4) }}</div>
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
                    {{ topThree[1].statValue }}
                  </div>
                </div>
              </div>

              <div class="top-card first" v-if="topThree[0]">
                <div class="top-rank-badge gold">1</div>
                <div class="top-cover first-cover" :class="getCoverClass(topThree[0])">
                  <div class="top-cover-text">{{ topThree[0].title.slice(0, 4) }}</div>
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
                    {{ topThree[0].statValue }}
                  </div>
                </div>
              </div>

              <div class="top-card third" v-if="topThree[2]">
                <div class="top-rank-badge bronze">3</div>
                <div class="top-cover" :class="getCoverClass(topThree[2])">
                  <div class="top-cover-text">{{ topThree[2].title.slice(0, 4) }}</div>
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
                    {{ topThree[2].statValue }}
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
                <div class="rank-item-cover" :class="getCoverClass(book)">
                  <span>{{ book.title.slice(0, 3) }}</span>
                </div>
                <div class="rank-item-info">
                  <div class="rank-item-title">{{ book.title }}</div>
                  <div class="rank-item-author">{{ book.author }}</div>
                </div>
                <div class="rank-item-price">¥{{ book.price }}</div>
                <div class="rank-item-stat">
                  <span class="stat-icon-sm">{{ tabStatIcon }}</span>
                  {{ book.statValue }}
                </div>
                <div class="rank-item-rating" v-if="book.avgRating">
                  ★ {{ book.avgRating?.toFixed(1) }}
                </div>
              </div>
            </div>

            <div v-if="hasMore" class="load-more">
              <button class="btn-load" @click="loadMore">加载更多 · 第{{ rankedBooks.length + 4 }}~{{ Math.min(rankedBooks.length + 23, allBooks.length) }}名</button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const displayCount = ref(17)

const rankTabs = [
  { key: 'sales', label: '畅销总榜' },
  { key: 'rating', label: '好评榜' },
  { key: 'new', label: '新锐榜' },
  { key: 'collection', label: '收藏榜' }
]

const periods = [
  { key: 'week', label: '本周' },
  { key: 'month', label: '本月' },
  { key: 'quarter', label: '本季' },
  { key: 'year', label: '全年' },
  { key: 'all', label: '总榜' }
]

const activeTab = ref('sales')
const activePeriod = ref('week')

const tabStatLabel = computed(() => {
  const map = { sales: '销量', rating: '评分', new: '热度', collection: '收藏' }
  return map[activeTab.value] || '销量'
})

const tabStatIcon = computed(() => {
  const map = { sales: '📈', rating: '⭐', new: '🔥', collection: '💛' }
  return map[activeTab.value] || '📊'
})

const allBooks = computed(() => {
  const books = []
  const titles = [
    '百年孤独', '小王子', '活着', '三体', '红楼梦',
    '挪威的森林', '围城', '平凡的世界', '白夜行', '月亮与六便士',
    '人间失格', '解忧杂货店', '追风筝的人', '边城', '骆驼祥子',
    '呐喊', '家', '茶馆', '雷雨', '子夜',
    '黄金时代', '沉默的大多数', '杀死一只知更鸟', '1984', '霍乱时期的爱情'
  ]
  const authors = [
    '加西亚·马尔克斯', '安托万·德·圣-埃克苏佩里', '余华', '刘慈欣', '曹雪芹',
    '村上春树', '钱钟书', '路遥', '东野圭吾', '毛姆',
    '太宰治', '东野圭吾', '卡勒德·胡赛尼', '沈从文', '老舍',
    '鲁迅', '巴金', '老舍', '曹禺', '茅盾',
    '王小波', '王小波', '哈珀·李', '乔治·奥威尔', '加西亚·马尔克斯'
  ]

  for (let i = 0; i < titles.length; i++) {
    const rating = (8 + Math.random() * 1.8).toFixed(1)
    const basePrice = (20 + Math.random() * 80).toFixed(2)
    let statValue
    switch (activeTab.value) {
      case 'rating': statValue = `${rating}分`; break
      case 'new': statValue = `${Math.floor(200 + Math.random() * 800)}%`; break
      case 'collection': statValue = `${Math.floor(5000 + Math.random() * 95000)}`; break
      default: statValue = `${Math.floor(10000 + Math.random() * 190000)}册`
    }

    books.push({
      id: i + 1,
      title: titles[i],
      author: authors[i],
      price: Number(basePrice),
      originalPrice: Number((basePrice * (1 + Math.random() * 0.3)).toFixed(2)),
      avgRating: Number(rating),
      statValue,
      coverColor: (i % 12) + 1
    })
  }
  return books
})

const sortedBooks = computed(() => {
  const sorted = [...allBooks.value]
  switch (activeTab.value) {
    case 'rating': return sorted.sort((a, b) => b.avgRating - a.avgRating)
    case 'new': return sorted.sort((a, b) => parseInt(b.statValue) - parseInt(a.statValue))
    case 'collection': return sorted.sort((a, b) => parseInt(b.statValue) - parseInt(a.statValue))
    default: return sorted.sort((a, b) => parseInt(b.statValue) - parseInt(a.statValue))
  }
})

const topThree = computed(() => sortedBooks.value.slice(0, 3))

const rankedBooks = computed(() => sortedBooks.value.slice(3, 3 + displayCount.value))

const hasMore = computed(() => 3 + displayCount.value < sortedBooks.value.length)

const loadMore = () => {
  displayCount.value += 17
}

const switchTab = (tab) => {
  activeTab.value = tab
  displayCount.value = 17
}

const getCoverClass = (book) => {
  const variants = [
    'cover-1', 'cover-2', 'cover-3', 'cover-4', 'cover-5',
    'cover-6', 'cover-7', 'cover-8', 'cover-9', 'cover-10',
    'cover-11', 'cover-12'
  ]
  const num = typeof book.id === 'string' ? parseInt(book.id, 10) || 1 : (book.id || 1)
  return variants[(num - 1) % variants.length]
}

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
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 0.72rem;
  color: rgba(237,230,214,0.8);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.rank-item-cover span {
  text-shadow: 0 1px 3px rgba(0,0,0,0.2);
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

/* Cover gradients */
.cover-1 { background: linear-gradient(160deg, #5D4037 0%, #3E2723 60%, #2C1A12 100%); }
.cover-2 { background: linear-gradient(160deg, #6D4C41 0%, #4E342E 100%); }
.cover-3 { background: linear-gradient(160deg, #1A1A2E 0%, #16213E 50%, #0F3460 100%); }
.cover-4 { background: linear-gradient(160deg, #3D2B1F 0%, #5C3A21 100%); }
.cover-5 { background: linear-gradient(160deg, #5C1A1A 0%, #3A0A0A 100%); }
.cover-6 { background: linear-gradient(160deg, #1B3A2D 0%, #0D2818 100%); }
.cover-7 { background: linear-gradient(160deg, #2C3E50 0%, #1A252F 100%); }
.cover-8 { background: linear-gradient(160deg, #3C1F3A 0%, #2A1526 100%); }
.cover-9 { background: linear-gradient(160deg, #8B4513 0%, #5C2D0E 100%); }
.cover-10 { background: linear-gradient(160deg, #4A3A2A 0%, #2A1A0A 100%); }
.cover-11 { background: linear-gradient(160deg, #2A4A3A 0%, #1A2A1A 100%); }
.cover-12 { background: linear-gradient(160deg, #3A2A4A 0%, #2A1A3A 100%); }

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
