<template>
  <div class="book-detail-page">
    <div class="page-container">
      <div v-if="loading" class="loading-state">
        <div class="skeleton-book">
          <div class="skeleton cover"></div>
          <div class="skeleton-info">
            <div class="skeleton title"></div>
            <div class="skeleton author"></div>
            <div class="skeleton price"></div>
          </div>
        </div>
      </div>

      <div v-else-if="book" class="book-detail">
        <div class="book-main">
          <div class="book-cover">
            <img :src="book.coverUrl || '/placeholder-book.png'" :alt="book.title" />
          </div>
          <div class="book-info">
            <h1 class="book-title">{{ book.title }}</h1>
            <p class="book-author">作者：{{ book.author }}</p>
            <div class="book-meta">
              <span>出版社：{{ book.publisher }}</span>
              <span v-if="book.publishDate">出版日期：{{ book.publishDate }}</span>
              <span>ISBN：{{ book.isbn }}</span>
            </div>
            <div class="book-rating">
              <div class="stars">
                <span v-for="i in 5" :key="i" :class="{ filled: i <= Math.round(book.avgRating || 0) }">★</span>
              </div>
              <span class="rating-text">{{ book.avgRating?.toFixed(1) || '0.0' }} ({{ book.reviewCount }}条评价)</span>
            </div>
            <div class="book-price">
              <span class="price-label">售价</span>
              <span class="price-value">¥{{ book.price }}</span>
            </div>
            <div class="book-stock">
              <span v-if="book.stock > 0" class="stock-in">有货 ({{ book.stock }}件)</span>
              <span v-else class="stock-out">缺货</span>
            </div>
            <div class="book-actions">
              <div class="quantity-select">
                <button @click="quantity > 1 && quantity--">−</button>
                <input type="number" v-model.number="quantity" min="1" :max="book.stock" />
                <button @click="quantity < book.stock && quantity++">+</button>
              </div>
              <button class="btn-add-cart" @click="handleAddCart" :disabled="book.stock === 0">加入购物车</button>
              <button class="btn-buy-now" @click="handleBuyNow" :disabled="book.stock === 0">立即购买</button>
            </div>
          </div>
        </div>

        <div class="book-description">
          <h3>内容简介</h3>
          <p>{{ book.description || '暂无简介' }}</p>
        </div>

        <div class="book-reviews">
          <h3>读者评价</h3>
          <div v-if="reviews.length" class="review-list">
            <div v-for="review in reviews" :key="review.id" class="review-item">
              <div class="review-header">
                <span class="reviewer">{{ review.username }}</span>
                <div class="review-stars">
                  <span v-for="i in 5" :key="i" :class="{ filled: i <= review.rating }">★</span>
                </div>
                <span class="review-time">{{ formatDateTime(review.createTime) }}</span>
              </div>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>
          <p v-else class="no-reviews">暂无评价</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookApi } from '@/api/book'
import { reviewApi } from '@/api/review'
import { cartApi } from '@/api/cart'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const router = useRouter()

const book = ref(null)
const reviews = ref([])
const loading = ref(true)
const quantity = ref(1)

const fetchBookDetail = async () => {
  try {
    const res = await bookApi.getDetail(route.params.id)
    book.value = res.data
  } catch (error) {
    ElMessage.error('获取书籍详情失败')
  }
}

const fetchReviews = async () => {
  try {
    const res = await reviewApi.getBookReviews(route.params.id)
    reviews.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch reviews')
  }
}

const handleAddCart = async () => {
  try {
    await cartApi.add({ bookId: book.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (error) {
    ElMessage.error('加入购物车失败')
  }
}

const handleBuyNow = () => {
  router.push({ path: '/order/confirm', query: { bookId: book.value.id, quantity: quantity.value } })
}

onMounted(async () => {
  loading.value = true
  await Promise.all([fetchBookDetail(), fetchReviews()])
  loading.value = false
})
</script>

<style scoped>
.book-detail-page { padding: var(--space-8) 0 var(--space-16); background: var(--color-bg); min-height: calc(100vh - var(--header-height)); }
.loading-state { padding: var(--space-16) 0; }
.skeleton-book { display: flex; gap: var(--space-10); }
.skeleton { background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: var(--radius-md); }
.skeleton.cover { width: 300px; height: 400px; }
.skeleton-info { flex: 1; display: flex; flex-direction: column; gap: var(--space-4); }
.skeleton.title { height: 40px; width: 70%; }
.skeleton.author { height: 24px; width: 40%; }
.skeleton.price { height: 36px; width: 30%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

.book-main { display: flex; gap: var(--space-10); margin-bottom: var(--space-10); }
.book-cover { width: 320px; flex-shrink: 0; }
.book-cover img { width: 100%; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); }
.book-info { flex: 1; }
.book-title { font-family: var(--font-display); font-size: var(--text-3xl); margin-bottom: var(--space-4); color: var(--color-text); }
.book-author { font-size: var(--text-lg); color: var(--color-text-secondary); margin-bottom: var(--space-4); }
.book-meta { display: flex; flex-wrap: wrap; gap: var(--space-4); font-size: var(--text-sm); color: var(--color-text-muted); margin-bottom: var(--space-6); }
.book-rating { display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-6); }
.stars span { font-size: 20px; color: var(--color-divider); }
.stars span.filled { color: var(--color-accent); }
.rating-text { font-size: var(--text-sm); color: var(--color-text-muted); }
.book-price { display: flex; align-items: baseline; gap: var(--space-3); margin-bottom: var(--space-4); }
.price-label { font-size: var(--text-sm); color: var(--color-text-muted); }
.price-value { font-family: var(--font-display); font-size: var(--text-4xl); font-weight: 700; color: var(--color-accent); }
.book-stock { margin-bottom: var(--space-6); }
.stock-in { color: var(--color-primary); font-size: var(--text-sm); }
.stock-out { color: var(--color-accent); font-size: var(--text-sm); }
.book-actions { display: flex; gap: var(--space-4); }
.quantity-select { display: flex; align-items: center; border: 1px solid var(--color-divider-strong); border-radius: var(--radius-md); }
.quantity-select button { width: 40px; height: 44px; border: none; background: var(--color-bg-cream); font-size: 18px; cursor: pointer; color: var(--color-text-secondary); }
.quantity-select button:hover { background: var(--color-divider); }
.quantity-select input { width: 60px; height: 44px; text-align: center; border: none; font-size: var(--text-base); color: var(--color-text); }
.btn-add-cart, .btn-buy-now { padding: var(--space-3) var(--space-6); font-size: var(--text-base); font-weight: 500; border-radius: var(--radius-md); cursor: pointer; transition: all var(--transition-fast); }
.btn-add-cart { background: var(--color-bg-card); border: 1px solid var(--color-accent); color: var(--color-accent); }
.btn-add-cart:hover:not(:disabled) { background: var(--color-accent); color: var(--color-primary-abyss); }
.btn-buy-now { background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%); border: none; color: var(--color-primary-abyss); font-weight: 700; box-shadow: 0 2px 12px rgba(192,154,75,0.2); }
.btn-buy-now:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 24px rgba(192,154,75,0.3); }
.btn-add-cart:disabled, .btn-buy-now:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }

.book-description, .book-reviews { background: var(--color-bg-card); border-radius: var(--radius-lg); padding: var(--space-8); margin-bottom: var(--space-6); border: 1px solid var(--color-divider); }
.book-description h3, .book-reviews h3 { font-family: var(--font-display); font-size: var(--text-xl); margin-bottom: var(--space-4); padding-bottom: var(--space-3); border-bottom: 1px solid var(--color-divider); }
.book-description p { font-size: var(--text-base); line-height: 1.8; color: var(--color-text-secondary); }
.review-list { display: flex; flex-direction: column; gap: var(--space-6); }
.review-item { padding-bottom: var(--space-6); border-bottom: 1px solid var(--color-divider); }
.review-item:last-child { border-bottom: none; padding-bottom: 0; }
.review-header { display: flex; align-items: center; gap: var(--space-4); margin-bottom: var(--space-3); }
.reviewer { font-weight: 500; color: var(--color-text); }
.review-stars span { font-size: 14px; color: var(--color-divider-strong); }
.review-stars span.filled { color: var(--color-accent); }
.review-time { font-size: var(--text-xs); color: var(--color-text-muted); margin-left: auto; }
.review-content { font-size: var(--text-sm); color: var(--color-text-secondary); line-height: 1.6; }
.no-reviews { color: var(--color-text-muted); text-align: center; padding: var(--space-8); }

@media (max-width: 768px) {
  .book-main { flex-direction: column; }
  .book-cover { width: 200px; margin: 0 auto; }
}
</style>
