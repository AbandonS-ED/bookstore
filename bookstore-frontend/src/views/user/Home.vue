<template>
  <div class="home-page">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-content">
          <div class="hero-tag">编辑精选 · 本周推荐</div>
          <h1>在文字中<br>遇见<span>另一个世界</span></h1>
          <p>书斋精选万册好书，从经典文学到前沿新知，用一杯咖啡的价格，开启一段跨越时空的旅程。</p>
          <div class="hero-actions">
            <router-link to="/books" class="btn-primary">开始探索 →</router-link>
            <router-link to="/books?sort=new" class="btn-secondary">本周书单</router-link>
          </div>
        </div>
        <div class="hero-visual">
          <div class="book-stack">
            <div v-for="(book, i) in heroBooks" :key="book.id" :class="['floating-book', `book-${i + 1}`]" @click="goToBook(book.id)" style="cursor:pointer">
              <div class="fb-title">{{ book.title }}</div>
              <div class="fb-author">{{ book.author }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <div class="stats-bar">
      <div class="stats-inner">
        <div class="stat-item">
          <div class="stat-number">万册</div>
          <div class="stat-label">在售图书</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">千+</div>
          <div class="stat-label">注册读者</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">99%</div>
          <div class="stat-label">好评率</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">次日达</div>
          <div class="stat-label">全国配送</div>
        </div>
      </div>
    </div>

    <!-- 热门推荐 -->
    <section class="section">
      <div class="section-header">
        <h2>热门推荐</h2>
        <router-link to="/books" class="view-all">查看全部 →</router-link>
      </div>
      <div class="categories">
        <div
          :class="['cat-pill', { active: !selectedCategory }]"
          @click="selectedCategory = null; fetchBooks()"
        >全部</div>
        <div
          v-for="cat in categories"
          :key="cat.id"
          :class="['cat-pill', { active: selectedCategory === cat.id }]"
          @click="selectedCategory = cat.id; fetchBooks()"
        >{{ cat.name }}</div>
      </div>
      <div v-if="loading" class="books-grid">
        <div v-for="i in 10" :key="i" class="book-card is-skeleton">
          <div class="book-cover"><div class="skeleton" style="width:100%;height:100%"></div></div>
          <div class="book-info">
            <div class="skeleton" style="height:20px;width:80%;margin-bottom:8px"></div>
            <div class="skeleton" style="height:14px;width:50%;margin-bottom:12px"></div>
            <div class="skeleton" style="height:18px;width:40%"></div>
          </div>
        </div>
      </div>
      <div v-else class="books-grid">
        <BookCard
          v-for="(book, idx) in books"
          :key="book.id"
          :book="book"
          :delay="idx * 60"
          @click="goToBook(book.id)"
        />
      </div>
    </section>

    <!-- 推荐横幅 -->
    <section class="featured-banner">
      <div class="featured-inner">
        <div class="featured-book-cover cover-fs">
          <div>
            <div style="font-size:0.75rem;color:var(--color-accent-light);margin-bottom:10px;letter-spacing:0.12em;">经典文学</div>
            活着<br>
            <span style="font-size:0.95rem;color:rgba(237,230,214,0.45);">余华 著</span>
          </div>
        </div>
        <div class="featured-text">
          <div class="feat-tag">编辑推荐 · 本周之选</div>
          <h3>在苦难中体悟生命</h3>
          <div class="feat-author">余华 ·  中国当代文学经典</div>
          <p>地主少爷富贵嗜赌成性，终于赌光了家业一贫如洗，穷困之中的富贵因为母亲患病前去求医，没想到半路上被国民党部队抓了壮丁，后被解放军所俘虏，回到家乡他才知道母亲已经过世，妻子家珍含辛茹苦带大了一双儿女，但女儿不幸变成了哑巴。</p>
          <div class="feat-price">
            ¥35.00
            <span class="old">¥49.00</span>
          </div>
          <div class="hero-actions" style="opacity:1;">
            <router-link to="/book/4" class="btn-primary">立即购买</router-link>
            <router-link to="/book/4" class="btn-secondary">查看详情</router-link>
          </div>
        </div>
      </div>
    </section>

    <!-- 新书上架 -->
    <section class="section">
      <div class="section-header">
        <h2>新书上架</h2>
        <router-link to="/books?sort=new" class="view-all">查看全部 →</router-link>
      </div>
      <div class="books-grid">
        <BookCard
          v-for="(book, idx) in newBooks"
          :key="book.id"
          :book="book"
          :delay="idx * 60"
          @click="goToBook(book.id)"
        />
      </div>
    </section>

    <!-- 订阅 -->
    <section class="newsletter">
      <div class="newsletter-inner">
        <h2>订阅书讯</h2>
        <p>每周一封精选书单，不错过任何一本好书。加入万千读者的阅读社区。</p>
        <div class="newsletter-form">
          <input type="email" placeholder="输入你的邮箱地址..." />
          <button class="btn-primary" style="white-space:nowrap;">立即订阅</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { bookApi } from '@/api/book'
import { categoryApi } from '@/api/category'
import BookCard from '@/components/business/BookCard.vue'

const router = useRouter()
const categories = ref([])
const books = ref([])
const newBooks = ref([])
const heroBooks = ref([])
const loading = ref(true)
const selectedCategory = ref(null)

const fetchBooks = async () => {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 10 }
    if (selectedCategory.value) {
      params.categoryId = selectedCategory.value
    }
    const res = await bookApi.getList(params)
    const records = res.data?.records || []
    books.value = records
  } catch (error) {
    console.error('Failed to load books:', error)
  } finally {
    loading.value = false
  }
}

const goToBook = (id) => {
  router.push({ path: `/book/${id}` })
}

onMounted(async () => {
  try {
    const catRes = await categoryApi.getList()
    categories.value = catRes.data || []
    const rankRes = await bookApi.getRanking({ type: 'sales', period: 'all' })
    heroBooks.value = (rankRes.data || []).slice(0, 3)
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
  await fetchBooks()
  newBooks.value = [...books.value].reverse()
})
</script>

<style scoped>
.home-page {
  overflow-x: hidden;
  margin-top: calc(-1 * var(--header-height));
}

/* ── HERO ── */
.hero {
  position: relative;
  min-height: 520px;
  display: flex;
  align-items: center;
  overflow: hidden;
  background: var(--color-primary-abyss);
}

.hero::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 70% 40%, rgba(192,154,75,0.08) 0%, transparent 50%),
    radial-gradient(ellipse at 20% 80%, rgba(92,68,52,0.3) 0%, transparent 45%),
    linear-gradient(135deg, rgba(46,31,21,0.5) 0%, rgba(28,18,12,1) 100%);
  pointer-events: none;
}

.hero::after {
  content: '';
  position: absolute;
  inset: 0;
  background:
    repeating-linear-gradient(90deg, transparent, transparent 80px, rgba(192,154,75,0.02) 80px, rgba(192,154,75,0.02) 81px);
  pointer-events: none;
}

.hero-inner {
  position: relative;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 80px 40px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  z-index: 1;
}

.hero-content { z-index: 1; }

.hero-tag {
  display: inline-block;
  background: rgba(192,154,75,0.1);
  color: var(--color-accent);
  font-size: 0.75rem;
  font-weight: 500;
  padding: 6px 18px;
  border-radius: 20px;
  border: 1px solid rgba(192,154,75,0.18);
  letter-spacing: 0.08em;
  margin-bottom: 28px;
  animation: fadeUp 0.8s ease forwards;
}

.hero h1 {
  font-size: 3rem;
  color: var(--color-bg-warm);
  font-weight: 900;
  margin-bottom: 22px;
  letter-spacing: 0.02em;
  animation: fadeUp 0.8s ease 0.15s forwards;
  opacity: 0;
}

.hero h1 span {
  color: var(--color-accent);
  font-style: italic;
}

.hero p {
  color: rgba(237,230,214,0.5);
  font-size: 1.02rem;
  max-width: 460px;
  margin-bottom: 40px;
  animation: fadeUp 0.8s ease 0.3s forwards;
  opacity: 0;
}

.hero-actions {
  display: flex;
  gap: 14px;
  animation: fadeUp 0.8s ease 0.45s forwards;
  opacity: 0;
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%);
  color: var(--color-primary-abyss);
  font-family: var(--font-body);
  font-weight: 700;
  font-size: 0.92rem;
  padding: 13px 30px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.35s ease;
  box-shadow: 0 2px 12px rgba(192,154,75,0.2);
  letter-spacing: 0.02em;
  text-decoration: none;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(192,154,75,0.3);
  letter-spacing: 0.06em;
  color: var(--color-primary-abyss);
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  color: rgba(237,230,214,0.7);
  font-family: var(--font-body);
  font-weight: 500;
  font-size: 0.92rem;
  padding: 13px 30px;
  border-radius: 8px;
  border: 1px solid rgba(237,230,214,0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  text-decoration: none;
}

.btn-secondary:hover {
  border-color: rgba(192,154,75,0.4);
  color: var(--color-accent);
  transform: translateY(-2px);
}

/* ── Hero Books ── */
.hero-visual {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  animation: fadeUp 1s ease 0.5s forwards;
  opacity: 0;
}

.book-stack {
  position: relative;
  width: 320px;
  height: 380px;
}

.floating-book {
  position: absolute;
  border-radius: 4px;
  box-shadow:
    0 15px 35px var(--color-shadow-deep),
    inset 0 1px 0 rgba(192,154,75,0.1);
  transition: transform 0.4s ease;
}

.floating-book:hover {
  transform: rotate(0deg) scale(1.04) !important;
  z-index: 10;
}

.book-1 {
  width: 180px; height: 260px;
  background: linear-gradient(160deg, #5C4434 0%, #3A2820 60%, #2E1F15 100%);
  top: 20px; left: 0;
  transform: rotate(-8deg);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 24px;
  text-align: center;
  z-index: 2;
  border-left: 3px solid rgba(192,154,75,0.15);
}

.book-2 {
  width: 160px; height: 230px;
  background: linear-gradient(160deg, #7A6252 0%, #5C4434 100%);
  top: 60px; right: 10px;
  transform: rotate(5deg);
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  text-align: center;
  border-left: 3px solid rgba(237,230,214,0.06);
}

.book-3 {
  width: 140px; height: 200px;
  background: linear-gradient(160deg, var(--color-accent) 0%, var(--color-accent-muted) 100%);
  bottom: 20px; left: 50px;
  transform: rotate(3deg);
  z-index: 3;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 18px;
  text-align: center;
  border-left: 3px solid rgba(255,255,255,0.1);
}

.fb-title {
  color: var(--color-accent);
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: 700;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-2 .fb-title { color: var(--color-bg-warm); font-size: 0.95rem; }
.book-3 .fb-title { color: var(--color-primary-abyss); font-size: 0.9rem; }

.fb-author {
  color: rgba(237,230,214,0.4);
  font-size: 0.72rem;
  margin-top: 8px;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-2 .fb-author { color: rgba(237,230,214,0.35); }
.book-3 .fb-author { color: rgba(46,31,21,0.5); }

/* ── Stats ── */
.stats-bar {
  background: var(--color-primary-dark);
  padding: 0 40px;
  border-bottom: 1px solid rgba(192,154,75,0.06);
}

.stats-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}

.stat-item {
  padding: 26px 24px;
  text-align: center;
  border-right: 1px solid rgba(237,230,214,0.04);
  transition: background 0.3s ease;
}

.stat-item:last-child { border-right: none; }
.stat-item:hover { background: rgba(192,154,75,0.03); }

.stat-number {
  font-family: var(--font-display);
  font-size: 1.65rem;
  font-weight: 900;
  color: var(--color-accent-light);
  line-height: 1;
}

.stat-label {
  font-size: 0.8rem;
  color: rgba(237,230,214,0.35);
  margin-top: 6px;
  letter-spacing: 0.04em;
}

/* ── Section ── */
.section {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 80px 40px;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 40px;
}

.section-header h2 {
  font-size: 1.7rem;
  color: var(--color-text);
  position: relative;
  padding-left: 20px;
}

.section-header h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 5px;
  bottom: 5px;
  width: 3px;
  background: linear-gradient(180deg, var(--color-accent), var(--color-accent-muted));
  border-radius: 2px;
}

.section-header .view-all {
  font-size: 0.85rem;
  color: var(--color-text-light);
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: all 0.3s ease;
  text-decoration: none;
}

.section-header .view-all:hover { gap: 8px; color: var(--color-accent); }

/* ── Categories ── */
.categories {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 36px;
}

.cat-pill {
  padding: 9px 22px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider-strong);
  border-radius: 28px;
  font-size: 0.85rem;
  font-weight: 400;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  color: var(--color-text-secondary);
}

.cat-pill:hover {
  border-color: var(--color-primary-mid);
  color: var(--color-text);
  background: var(--color-bg-cream);
}

.cat-pill.active {
  background: var(--color-primary);
  color: var(--color-bg-warm);
  border-color: var(--color-primary);
  font-weight: 500;
}

/* ── Books Grid ── */
.books-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 24px;
}

.book-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
}

.book-card.is-skeleton {
  pointer-events: none;
}

.book-cover {
  position: relative;
  aspect-ratio: 3/4;
  overflow: hidden;
}

/* ── Featured Banner ── */
.featured-banner {
  background: var(--color-primary-dark);
  position: relative;
  overflow: hidden;
}

.featured-banner::before {
  content: '';
  position: absolute;
  right: -120px;
  top: -120px;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(192,154,75,0.06) 0%, transparent 70%);
  pointer-events: none;
}

.featured-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 70px 40px;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 60px;
  align-items: center;
}

.featured-book-cover {
  width: 260px;
  height: 370px;
  border-radius: 6px;
  box-shadow:
    0 30px 60px rgba(0,0,0,0.35),
    inset 0 1px 0 rgba(192,154,75,0.08);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 30px;
  text-align: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.8rem;
  color: var(--color-bg-warm);
  border-left: 3px solid rgba(192,154,75,0.12);
  position: relative;
  margin: 0 auto;
  background: linear-gradient(160deg, #3D2B1F 0%, #5C3A21 100%);
}

.featured-text { z-index: 1; }

.featured-text .feat-tag {
  display: inline-block;
  color: var(--color-accent);
  font-size: 0.75rem;
  font-weight: 500;
  letter-spacing: 0.12em;
  margin-bottom: 16px;
}

.featured-text h3 {
  font-size: 2rem;
  color: var(--color-bg-warm);
  margin-bottom: 8px;
}

.featured-text .feat-author {
  color: rgba(237,230,214,0.4);
  font-size: 0.9rem;
  margin-bottom: 20px;
  font-family: var(--font-body);
}

.featured-text p {
  color: rgba(237,230,214,0.5);
  font-size: 0.93rem;
  line-height: 1.9;
  margin-bottom: 28px;
  max-width: 500px;
}

.feat-price {
  font-family: var(--font-display);
  font-size: 2rem;
  font-weight: 900;
  color: var(--color-accent-light);
  margin-bottom: 28px;
}

.feat-price .old {
  font-size: 1rem;
  color: rgba(237,230,214,0.2);
  text-decoration: line-through;
  font-weight: 400;
  margin-left: 8px;
}

/* ── Newsletter ── */
.newsletter {
  background: var(--color-bg-warm);
  border-top: 1px solid var(--color-divider);
  border-bottom: 1px solid var(--color-divider);
}

.newsletter-inner {
  max-width: 580px;
  margin: 0 auto;
  text-align: center;
  padding: 70px 40px;
}

.newsletter h2 {
  font-size: 1.7rem;
  margin-bottom: 12px;
  color: var(--color-text);
}

.newsletter p {
  color: var(--color-text-light);
  font-size: 0.93rem;
  margin-bottom: 32px;
  line-height: 1.8;
}

.newsletter-form {
  display: flex;
  gap: 12px;
}

.newsletter-form input {
  flex: 1;
  padding: 13px 18px;
  border: 1px solid var(--color-divider-strong);
  border-radius: 8px;
  font-size: 0.9rem;
  font-family: var(--font-body);
  background: var(--color-bg-card);
  color: var(--color-text);
  transition: all 0.3s ease;
  outline: none;
}

.newsletter-form input:focus {
  border-color: var(--color-primary-mid);
  box-shadow: 0 0 0 3px rgba(74,53,38,0.06);
}

.newsletter-form input::placeholder { color: var(--color-text-light); }

/* ── Animations ── */
@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.book-1 { animation: float 7s ease-in-out infinite; }
.book-2 { animation: float 7s ease-in-out 1.5s infinite; }
.book-3 { animation: float 7s ease-in-out 3s infinite; }

/* ── Responsive ── */
@media (max-width: 1024px) {
  .books-grid { grid-template-columns: repeat(3, 1fr); }
  .hero-inner { grid-template-columns: 1fr; }
  .hero-visual { display: none; }
  .featured-inner { grid-template-columns: 1fr; text-align: center; }
  .featured-book-cover { margin-bottom: 20px; }
  .featured-text p { margin: 0 auto 28px; }
  .featured-text .hero-actions { justify-content: center; }
  .stats-inner { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 768px) {
  .hero { min-height: 400px; }
  .hero-inner { padding: 50px 20px; }
  .hero h1 { font-size: 2rem; }
  .section { padding: 50px 20px; }
  .books-grid { grid-template-columns: repeat(2, 1fr); gap: 16px; }
  .newsletter-form { flex-direction: column; }
  .stats-inner { grid-template-columns: 1fr 1fr; }
  .cat-pill { font-size: 0.8rem; padding: 7px 16px; }
}
</style>
