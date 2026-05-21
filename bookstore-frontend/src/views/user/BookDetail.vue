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
          <div v-if="book.coverUrl && !coverError" class="book-cover">
            <img :src="book.coverUrl" :alt="book.title" @error="coverError = true" />
          </div>
          <div v-else class="book-cover cover-placeholder" :style="getCoverStyle(book.id)">
            <span class="cover-title">{{ book.title }}</span>
            <span class="cover-author">{{ book.author }}</span>
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
              <span v-if="book.reviewCount > 0" class="rating-text">{{ book.avgRating?.toFixed(1) }} ({{ book.reviewCount }}条评价)</span>
              <span v-else class="rating-text no-rating">暂无评分</span>
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

        <div class="book-reviews" ref="reviewSectionRef">
          <h3>读者评价 <span class="review-count" v-if="book.reviewCount > 0">({{ book.reviewCount }})</span></h3>

          <!-- Review Form -->
          <div v-if="formVisible" class="review-form">
            <div v-if="!isLoggedIn" class="review-login-hint">
              <span>请 <router-link to="/login" class="login-link">登录</router-link> 后发表评价</span>
            </div>
            <div v-else-if="hasReviewed" class="review-already">
              <span class="alr-stars">★ {{ userRating }}</span>
              <span class="alr-text">你已评价，感谢你的反馈</span>
            </div>
            <div v-else class="review-form-body">
              <div class="form-star-row">
                <span class="form-label">评分</span>
                <div class="star-input">
                  <span
                    v-for="i in 5"
                    :key="i"
                    class="star-click"
                    :class="{ filled: i <= rating, hovered: i <= hoverRating && !rating }"
                    @mouseenter="hoverRating = i"
                    @mouseleave="hoverRating = 0"
                    @click="rating = i"
                  >★</span>
                  <span class="star-hint">{{ ratingText }}</span>
                </div>
              </div>
              <div class="form-text-row">
                <span class="form-label">评价</span>
                <textarea
                  v-model="content"
                  placeholder="说说你对这本书的看法..."
                  maxlength="500"
                  rows="3"
                  class="form-textarea"
                  @input="content = content.slice(0, 500)"
                ></textarea>
                <span class="char-count">{{ content.length }}/500</span>
              </div>
              <div class="form-actions">
                <button class="btn-submit" :disabled="!canSubmit || submitting" @click="handleSubmitReview">
                  <span v-if="submitting" class="btn-loading-icon">⟳</span>
                  {{ submitting ? '提交中...' : '提交评价' }}
                </button>
                <span v-if="submitError" class="submit-error">{{ submitError }}</span>
              </div>
            </div>
          </div>

          <!-- Review List -->
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
          <p v-else class="no-reviews">暂无评价，快来写第一条评价吧</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookApi } from '@/api/book'
import { reviewApi } from '@/api/review'
import { cartApi } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import { getCoverStyle } from '@/utils/cover'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const book = ref(null)
const reviews = ref([])
const loading = ref(true)
const quantity = ref(1)
const coverError = ref(false)
const reviewSectionRef = ref(null)

// Review form state
const rating = ref(0)
const hoverRating = ref(0)
const content = ref('')
const submitting = ref(false)
const submitError = ref('')

const ratingTexts = ['', '很差', '较差', '还行', '推荐', '力荐']
const ratingText = computed(() => rating.value ? ratingTexts[rating.value] : '')

const isLoggedIn = computed(() => userStore.isLoggedIn)

const hasReviewed = computed(() => {
  if (!isLoggedIn.value || !userStore.userInfo?.id) return false
  const uid = String(userStore.userInfo.id)
  return reviews.value.some(r => String(r.userId) === uid)
})

const userRating = computed(() => {
  if (!isLoggedIn.value || !userStore.userInfo?.id) return 0
  const uid = String(userStore.userInfo.id)
  const myReview = reviews.value.find(r => String(r.userId) === uid)
  return myReview ? myReview.rating : 0
})

const canSubmit = computed(() => rating.value >= 1 && content.value.trim().length > 0)

const formVisible = ref(false)

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

const handleSubmitReview = async () => {
  if (!canSubmit.value) return
  submitting.value = true
  submitError.value = ''
  try {
    await reviewApi.add({
      bookId: String(book.value.id),
      rating: rating.value,
      content: content.value.trim()
    })
    ElMessage.success('评价成功')
    rating.value = 0
    content.value = ''
    await Promise.all([fetchBookDetail(), fetchReviews()])
  } catch (error) {
    submitError.value = error.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.getUserInfo().catch(() => {})
  }
  await Promise.all([fetchBookDetail(), fetchReviews()])
  loading.value = false
  formVisible.value = true
  await nextTick()
  if (route.query.review === '1' && reviewSectionRef.value) {
    reviewSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
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
.cover-placeholder { width: 320px; height: 440px; border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center; padding: 30px; border-left: 3px solid rgba(192,154,75,0.12); box-shadow: 0 30px 60px rgba(0,0,0,0.35), inset 0 1px 0 rgba(192,154,75,0.08); }
.cover-placeholder .cover-title { font-family: var(--font-display); font-weight: 700; font-size: 1.8rem; color: rgba(237,230,214,0.85); text-shadow: 0 1px 4px rgba(0,0,0,0.2); word-break: break-all; line-height: 1.3; margin-bottom: 12px; }
.cover-placeholder .cover-author { font-size: 0.85rem; color: rgba(237,230,214,0.55); text-shadow: 0 1px 2px rgba(0,0,0,0.2); }
.book-info { flex: 1; }
.book-title { font-family: var(--font-display); font-size: var(--text-3xl); margin-bottom: var(--space-4); color: var(--color-text); }
.book-author { font-size: var(--text-lg); color: var(--color-text-secondary); margin-bottom: var(--space-4); }
.book-meta { display: flex; flex-wrap: wrap; gap: var(--space-4); font-size: var(--text-sm); color: var(--color-text-muted); margin-bottom: var(--space-6); }
.book-rating { display: flex; align-items: center; gap: var(--space-3); margin-bottom: var(--space-6); }
.stars span { font-size: 20px; color: var(--color-divider); }
.stars span.filled { color: var(--color-accent); }
.rating-text { font-size: var(--text-sm); color: var(--color-text-muted); }
.no-rating { color: var(--color-text-light); font-style: italic; }
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

.review-count { font-size: var(--text-base); color: var(--color-text-muted); font-weight: 400; }

.review-form { margin-bottom: var(--space-6); padding: var(--space-5); background: var(--color-bg); border-radius: var(--radius-lg); border: 1px solid var(--color-divider); }

.review-login-hint { text-align: center; padding: var(--space-4); color: var(--color-text-muted); font-size: var(--text-sm); }
.login-link { color: var(--color-accent); font-weight: 500; text-decoration: none; }
.login-link:hover { text-decoration: underline; }

.review-already { display: flex; align-items: center; gap: var(--space-3); justify-content: center; padding: var(--space-4); }
.alr-stars { font-size: 1.2rem; color: var(--color-accent); font-weight: 700; }
.alr-text { font-size: var(--text-sm); color: var(--color-text-muted); }

.review-form-body { display: flex; flex-direction: column; gap: var(--space-4); }
.form-star-row { display: flex; align-items: center; gap: var(--space-4); }
.form-label { font-size: var(--text-sm); color: var(--color-text-secondary); min-width: 40px; }
.star-input { display: flex; align-items: center; gap: 2px; }
.star-click { font-size: 24px; color: var(--color-divider-strong); cursor: pointer; transition: all 0.15s ease; user-select: none; }
.star-click.filled, .star-click.hovered { color: var(--color-accent); }
.star-click:hover { transform: scale(1.2); }
.star-click.filled:hover { transform: scale(1.15); }
.star-hint { font-size: var(--text-xs); color: var(--color-accent-muted); margin-left: var(--space-2); min-width: 40px; }

.form-text-row { position: relative; }
.form-textarea { width: 100%; padding: var(--space-3); font-size: var(--text-sm); font-family: var(--font-body); color: var(--color-text); background: var(--color-bg-card); border: 1px solid var(--color-divider-strong); border-radius: var(--radius-md); resize: vertical; min-height: 80px; outline: none; transition: border-color 0.2s ease; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-accent); box-shadow: 0 0 0 3px rgba(192,154,75,0.08); }
.form-textarea::placeholder { color: var(--color-text-light); }
.char-count { position: absolute; right: var(--space-3); bottom: var(--space-3); font-size: var(--text-xs); color: var(--color-text-muted); }

.form-actions { display: flex; align-items: center; gap: var(--space-4); }
.btn-submit { padding: var(--space-2) var(--space-6); font-size: var(--text-sm); font-weight: 500; color: var(--color-bg-warm); background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%); border: none; border-radius: var(--radius-md); cursor: pointer; transition: all 0.3s ease; font-family: var(--font-body); }
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(192,154,75,0.25); }
.btn-submit:disabled { opacity: 0.5; cursor: not-allowed; transform: none; }
.btn-loading-icon { display: inline-block; animation: spin 0.8s linear infinite; margin-right: 4px; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
.submit-error { font-size: var(--text-xs); color: var(--color-accent); }

@media (max-width: 768px) {
  .book-main { flex-direction: column; }
  .book-cover { width: 200px; margin: 0 auto; }
  .cover-placeholder { width: 200px; height: 280px; margin: 0 auto; padding: 16px; }
  .cover-placeholder .cover-title { font-size: 1.2rem; }
  .cover-placeholder .cover-author { font-size: 0.75rem; }
}
</style>
