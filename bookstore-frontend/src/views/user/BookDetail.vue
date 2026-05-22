<template>
  <div class="book-detail-page">
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

    <template v-else-if="book">
      <!-- BREADCRUMB -->
      <div class="breadcrumb">
        <router-link to="/">首页</router-link><span class="sep">/</span>
        <router-link to="/books">全部图书</router-link><span class="sep">/</span>
        <router-link v-if="book.categoryName" :to="`/books?categoryId=${book.categoryId}`">{{ book.categoryName }}</router-link>
        <span v-if="book.categoryName" class="sep">/</span>
        <span>{{ book.title }}</span>
      </div>

      <!-- ═══ PRODUCT HERO ═══ -->
      <section class="product-hero">
        <div class="cover-side">
          <div
            v-if="book.coverUrl && !coverError"
            class="cover-main"
            :style="{
              backgroundImage: `url(${book.coverUrl})`,
              backgroundSize: 'cover',
              backgroundPosition: 'center'
            }"
          >
            <span class="cover-badge" v-if="badgeText">{{ badgeText }}</span>
          </div>
          <div v-else class="cover-main" :style="getCoverStyle(book.id)">
            <span class="cover-badge" v-if="badgeText">{{ badgeText }}</span>
            <span>{{ book.title }}</span>
          </div>
        </div>

        <div class="info-side">
          <div class="info-category">📚 {{ book.categoryName || '图书' }}</div>
          <h1 class="info-title">{{ book.title }}</h1>

          <div class="info-authors">
            <div class="author-chip">
              <div class="author-avatar">{{ book.author?.charAt(0) }}</div>
              <div class="author-info">
                <div class="ai-name">{{ book.author }}</div>
                <div class="ai-role">作者</div>
              </div>
            </div>
          </div>

          <div class="info-rating-row" v-if="book.avgRating">
            <div class="rating-big">
              <span class="rb-num">{{ book.avgRating.toFixed(1) }}</span>
              <div>
                <div class="rb-stars">{{ renderStars(book.avgRating) }}</div>
                <div class="rb-label">{{ book.reviewCount }} 条评价</div>
              </div>
            </div>
            <div class="rating-bar-group">
              <div class="rb-row" v-for="(pct, i) in ratingDistribution" :key="i">
                <span class="rb-level"></span>
                <div class="rb-bar"><div class="rb-bar-fill" :style="{ width: pct + '%' }"></div></div>
                {{ pct }}%
              </div>
            </div>
          </div>

          <div class="info-meta">
            <div class="meta-item" v-if="book.publisher"><span class="mi-label">出版社</span><span class="mi-val">{{ book.publisher }}</span></div>
            <div class="meta-item" v-if="book.publishDate"><span class="mi-label">出版时间</span><span class="mi-val">{{ book.publishDate }}</span></div>
            <div class="meta-item" v-if="book.isbn"><span class="mi-label">ISBN</span><span class="mi-val">{{ book.isbn }}</span></div>
            <div class="meta-item"><span class="mi-label">销量</span><span class="mi-val">{{ book.sales || 0 }} 件</span></div>
            <div class="meta-item"><span class="mi-label">库存</span><span class="mi-val">{{ book.stock || 0 }} 件</span></div>
          </div>

          <div class="price-block">
            <div class="price-row">
              <span class="price-now">¥{{ book.price }}</span>
              <span v-if="discountPercent" class="price-original">¥{{ book.origPrice }}</span>
              <span v-if="discountPercent" class="price-discount">-{{ discountPercent }}%</span>
            </div>
            <div class="price-save" v-if="discountPercent">已省 ¥{{ (book.origPrice - book.price).toFixed(2) }}，限时折扣</div>
          </div>

          <div class="qty-row">
            <span class="qty-label">数量</span>
            <div class="qty-control">
              <button class="qty-btn" @click="quantity > 1 && quantity--">−</button>
              <input class="qty-input" type="text" :value="quantity" readonly>
              <button class="qty-btn" @click="quantity < book.stock && quantity++">+</button>
            </div>
            <span class="qty-stock" v-if="book.stock > 0">有货，24h内发货</span>
            <span class="qty-stock oos" v-else>缺货</span>
          </div>

          <div class="action-row" ref="actionRowRef">
            <button class="btn-action btn-buy" @click="handleBuyNow" :disabled="book.stock === 0">立即购买</button>
            <button class="btn-action btn-cart" @click="handleAddCart" :disabled="book.stock === 0">🛒 加入购物车</button>
            <button
              v-if="isLoggedIn"
              :class="['btn-action btn-fav', { active: book.isFavorited }]"
              @click="toggleFavorite"
            >{{ book.isFavorited ? '♥' : '♡' }}</button>
          </div>

          <div class="promise-row">
            <div class="promise"><span class="p-icon">✅</span> 正版保证</div>
            <div class="promise"><span class="p-icon">🚚</span> 满49包邮</div>
            <div class="promise"><span class="p-icon">🔄</span> 7天无理由退换</div>
            <div class="promise"><span class="p-icon">📦</span> 无酸纸包裹</div>
          </div>
        </div>
      </section>

      <!-- ═══ DETAIL TABS + SIDEBAR ═══ -->
      <section class="detail-section">
        <div class="detail-main">
          <div class="detail-tabs" ref="tabsRef">
            <div
              :class="['detail-tab', { active: activeTab === 'desc' }]"
              @click="activeTab = 'desc'"
            >书籍简介</div>
            <div
              :class="['detail-tab', { active: activeTab === 'author' }]"
              @click="activeTab = 'author'"
            >作者介绍</div>
            <div
              :class="['detail-tab', { active: activeTab === 'toc' }]"
              @click="activeTab = 'toc'"
            >目录</div>
            <div
              :class="['detail-tab', { active: activeTab === 'reviews' }]"
              @click="activeTab = 'reviews'"
            >读者评论 ({{ book.reviewCount || 0 }})</div>
          </div>

          <!-- TAB: Description -->
          <div class="tab-panel" v-show="activeTab === 'desc'">
            <div class="desc-content">
              <div class="desc-label">内容简介</div>
              <p v-for="(para, i) in descriptionParagraphs" :key="i">{{ para }}</p>
              <div class="desc-quote" v-if="book.quote">「{{ book.quote }}」</div>
            </div>
          </div>

          <!-- TAB: Author -->
          <div class="tab-panel" v-show="activeTab === 'author'" v-if="book.authorInfo">
            <div class="author-section">
              <div class="author-big-avatar">{{ book.authorInfo.name?.charAt(0) || '作' }}</div>
              <div class="author-bio">
                <h4>{{ book.authorInfo.name }}</h4>
                <div class="ab-country" v-if="book.authorInfo.country || book.authorInfo.birthYear">{{ book.authorInfo.country || '' }}{{ book.authorInfo.country && book.authorInfo.birthYear ? ' · ' : '' }}{{ book.authorInfo.birthYear || '' }}</div>
                <p>{{ book.authorInfo.bio || '暂无介绍' }}</p>
                <div class="ab-awards" v-if="book.authorInfo.awards">
                  <span class="award" v-for="award in splitAwards(book.authorInfo.awards)" :key="award">🏆 {{ award }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- TAB: TOC -->
          <div class="tab-panel" v-show="activeTab === 'toc'" v-if="book.chapters?.length">
            <div class="toc-list">
              <div v-for="ch in book.chapters" :key="ch.chapterNum" class="toc-item">
                <span class="ti-num">{{ ch.chapterNum }}</span>
                <span class="ti-chapter">{{ ch.title }}</span>
                <span class="ti-dots"></span>
                <span class="ti-page" v-if="ch.page">p.{{ ch.page }}</span>
              </div>
            </div>
          </div>

          <!-- TAB: Reviews -->
          <div class="tab-panel" v-show="activeTab === 'reviews'" ref="reviewSectionRef">
            <div class="reviews-summary" v-if="book.reviewCount > 0">
              <div class="rs-left">
                <div class="rs-score">{{ book.avgRating?.toFixed(1) }}</div>
                <div class="rs-stars">{{ renderStars(book.avgRating) }}</div>
                <div class="rs-count">{{ book.reviewCount }} 条评价</div>
              </div>
              <div class="rs-bars">
                <div class="rsb-row" v-for="(pct, i) in reviewDistribution" :key="i">
                  <span class="rsb-label">{{ 5 - i }}★</span>
                  <div class="rsb-bar"><div class="rsb-bar-fill" :style="{ width: pct + '%' }"></div></div>
                  <span class="rsb-pct">{{ pct }}%</span>
                </div>
              </div>
            </div>

            <div class="review-form" v-if="formVisible">
              <div v-if="!isLoggedIn" class="review-login-hint">
                请 <router-link to="/login" class="login-link">登录</router-link> 后发表评价
              </div>
              <div v-else-if="hasReviewed" class="review-already">
                你已评价，感谢你的反馈
              </div>
              <div v-else class="review-form-body">
                <div class="form-star-row">
                  <span class="form-label">评分</span>
                  <div class="star-input">
                    <span
                      v-for="i in 5" :key="i"
                      class="star-click"
                      :class="{ filled: i <= (hoverRating || rating) }"
                      @mouseenter="hoverRating = i"
                      @mouseleave="hoverRating = 0"
                      @click="rating = i"
                    >★</span>
                    <span class="star-hint">{{ ratingText }}</span>
                  </div>
                </div>
                <textarea
                  v-model="content"
                  placeholder="说说你对这本书的看法..."
                  maxlength="500"
                  rows="3"
                  class="form-textarea"
                ></textarea>
                <div class="form-actions">
                  <button class="btn-submit" :disabled="!canSubmit || submitting" @click="handleSubmitReview">
                    {{ submitting ? '提交中...' : '提交评价' }}
                  </button>
                  <span v-if="submitError" class="submit-error">{{ submitError }}</span>
                </div>
              </div>
            </div>

            <div class="review-list" v-if="reviews.length">
              <div v-for="review in reviews" :key="review.id" class="review-card">
                <div class="rc-head">
                  <div class="rc-user">
                    <div class="rc-avatar" :style="{ background: reviewAvatarColor(review) }">{{ review.username?.charAt(0) }}</div>
                    <div>
                      <div class="rc-name">{{ review.username }}</div>
                      <div class="rc-date">{{ formatDateTime(review.createTime) }} · 已购</div>
                    </div>
                  </div>
                  <div class="rc-stars">{{ renderStars(review.rating) }}</div>
                </div>
                <div class="rc-content">{{ review.content }}</div>
              </div>
            </div>
            <p v-else class="no-reviews">暂无评价，快来写第一条评价吧</p>
          </div>
        </div>

        <!-- SIDEBAR -->
        <div class="detail-sidebar">
          <div class="sidebar-section" v-if="relatedBooks.length">
            <h4>相关推荐</h4>
            <div
              v-for="rb in relatedBooks"
              :key="rb.id"
              class="related-book"
              @click="goToBook(rb.id)"
            >
              <div class="rb-cover" :style="getCoverStyle(rb.id)">{{ rb.title.slice(0, 2) }}</div>
              <div class="rb-info">
                <div class="rbi-title">{{ rb.title }}</div>
                <div class="rbi-author">{{ rb.author }}</div>
                <div class="rbi-price" v-if="rb.price">¥{{ rb.price }}</div>
                <div class="rbi-rating" v-if="rb.avgRating">★ {{ rb.avgRating.toFixed(1) }}</div>
              </div>
            </div>
          </div>
          <div class="sidebar-section" v-if="sameAuthorBooks.length">
            <h4>同一作者</h4>
            <div
              v-for="rb in sameAuthorBooks"
              :key="rb.id"
              class="same-author-book"
              @click="goToBook(rb.id)"
            >
              <div class="sab-cover" :style="getCoverStyle(rb.id)">{{ rb.title.slice(0, 2) }}</div>
              <div>
                <div class="sab-title">{{ rb.title }}</div>
                <div class="sab-price" v-if="rb.price">¥{{ rb.price }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- STICKY BUY BAR -->
      <div class="sticky-bar" :class="{ show: stickyShow }">
        <div class="sb-inner">
          <div class="sb-book">
            <div class="sb-cover" :style="getCoverStyle(book.id)">{{ book.title.slice(0, 2) }}</div>
            <div class="sb-title">{{ book.title }}</div>
          </div>
          <div class="sb-right">
            <div class="sb-price">¥{{ book.price }} <span class="sb-orig" v-if="discountPercent">¥{{ book.origPrice }}</span></div>
            <div class="sb-btns">
              <button class="sb-btn cart" @click="handleAddCart" :disabled="book.stock === 0">加入购物车</button>
              <button class="sb-btn buy" @click="handleBuyNow" :disabled="book.stock === 0">立即购买</button>
            </div>
          </div>
        </div>
      </div>

      <!-- TOAST -->
      <div class="toast-wrap" id="toastWrap"></div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { bookApi } from '@/api/book'
import { reviewApi } from '@/api/review'
import { cartApi } from '@/api/cart'
import { favoriteApi } from '@/api/favorite'
import { useUserStore } from '@/stores/user'
import { useFavoriteStore } from '@/stores/favorite'
import { formatDateTime } from '@/utils/format'
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

function computeDistribution(avg) {
  if (!avg || avg <= 0) return [0, 0, 0]
  const a = avg
  if (a >= 9.0) return [72, 18, 6, 3, 1]
  if (a >= 8.5) return [60, 25, 10, 3, 2]
  if (a >= 8.0) return [48, 28, 14, 6, 4]
  if (a >= 7.5) return [35, 30, 18, 10, 7]
  if (a >= 7.0) return [25, 28, 22, 14, 11]
  if (a >= 6.0) return [15, 22, 28, 20, 15]
  return [8, 12, 20, 28, 32]
}

function toast(msg) {
  const w = document.getElementById('toastWrap')
  if (!w) return
  const t = document.createElement('div')
  t.className = 'toast'
  t.innerHTML = '<span class="td"></span>' + msg
  w.appendChild(t)
  setTimeout(() => {
    t.classList.add('out')
    setTimeout(() => t.remove(), 300)
  }, 2500)
}

const avatarColors = ['#5C4434', '#4A6E8A', '#5C8856', '#A04040', '#7A6252']
function reviewAvatarColor(review) {
  const hash = (review.username || '').split('').reduce((a, c) => a + c.charCodeAt(0), 0)
  return avatarColors[hash % avatarColors.length]
}

function splitAwards(awards) {
  if (!awards) return []
  return awards.split(',').map(s => s.trim()).filter(Boolean)
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const favoriteStore = useFavoriteStore()

const book = ref(null)
const reviews = ref([])
const relatedBooks = ref([])
const loading = ref(true)
const quantity = ref(1)
const coverError = ref(false)
const activeTab = ref('desc')
const formVisible = ref(false)
const stickyShow = ref(false)

const rating = ref(0)
const hoverRating = ref(0)
const content = ref('')
const submitting = ref(false)
const submitError = ref('')

const actionRowRef = ref(null)
const reviewSectionRef = ref(null)
const tabsRef = ref(null)

const ratingTexts = ['', '很差', '较差', '还行', '推荐', '力荐']
const ratingText = computed(() => rating.value ? ratingTexts[rating.value] : '')

const isLoggedIn = computed(() => userStore.isLoggedIn)

const discountPercent = computed(() => {
  if (!book.value?.origPrice || !book.value?.price) return 0
  if (book.value.origPrice <= book.value.price) return 0
  return Math.round((1 - book.value.price / book.value.origPrice) * 100)
})

const badgeText = computed(() => {
  const b = book.value
  if (!b) return ''
  if (b.avgRating && b.avgRating >= 9) return '经典'
  if (b.sales && b.sales > 5000) return '畅销'
  return ''
})

const ratingDistribution = computed(() => {
  return computeDistribution(book.value?.avgRating).slice(0, 3)
})

const reviewDistribution = computed(() => {
  return computeDistribution(book.value?.avgRating)
})

const descriptionParagraphs = computed(() => {
  const desc = book.value?.description
  if (!desc) return ['暂无简介']
  return desc.split('\n\n').filter(Boolean)
})

const hasReviewed = computed(() => {
  if (!isLoggedIn.value || !userStore.userInfo?.id) return false
  const uid = String(userStore.userInfo.id)
  return reviews.value.some(r => String(r.userId) === uid)
})

const canSubmit = computed(() => rating.value >= 1 && content.value.trim().length > 0)

const sameAuthorBooks = computed(() => {
  if (!book.value?.author) return []
  return relatedBooks.value.filter(rb => rb.author === book.value.author)
})

const fetchBookDetail = async () => {
  const res = await bookApi.getDetail(route.params.id)
  book.value = res.data
  if (userStore.isLoggedIn) {
    await favoriteStore.fetchFavoriteIds()
    book.value.isFavorited = favoriteStore.isFavorited(book.value.id)
  }
}

const fetchReviews = async () => {
  const res = await reviewApi.getBookReviews(route.params.id)
  reviews.value = res.data || []
}

const fetchRelatedBooks = async () => {
  try {
    const res = await bookApi.getList({ pageNum: 1, pageSize: 8 })
    relatedBooks.value = (res.data?.records || []).filter(b => String(b.id) !== route.params.id).slice(0, 4)
  } catch {}
}

const handleAddCart = async () => {
  try {
    await cartApi.add({ bookId: book.value.id, quantity: quantity.value })
    toast('已加入购物车')
  } catch {
    ElMessage.error('加入购物车失败')
  }
}

const handleBuyNow = () => {
  router.push({ path: '/order/confirm', query: { bookId: book.value.id, quantity: quantity.value } })
}

const toggleFavorite = async () => {
  try {
    const id = book.value.id
    if (favoriteStore.isFavorited(id)) {
      await favoriteApi.remove(id)
      book.value.isFavorited = false
      favoriteStore.favoriteIds = favoriteStore.favoriteIds.filter(fid => fid !== String(id))
    } else {
      await favoriteApi.add(id)
      book.value.isFavorited = true
      if (!favoriteStore.favoriteIds.includes(String(id))) {
        favoriteStore.favoriteIds.push(String(id))
      }
    }
  } catch {
    // re-sync with server
    favoriteStore.fetchFavoriteIds()
    ElMessage.error('操作失败')
  }
}

const goToBook = (id) => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
  router.push({ path: `/book/${id}` })
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
    toast('评价成功')
    rating.value = 0
    content.value = ''
    await Promise.all([fetchBookDetail(), fetchReviews()])
  } catch (error) {
    submitError.value = error.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

let observer = null

onMounted(async () => {
  loading.value = true
  if (userStore.isLoggedIn && !userStore.userInfo) {
    userStore.getUserInfo().catch(() => {})
  }
  await Promise.all([fetchBookDetail(), fetchReviews(), fetchRelatedBooks()])
  loading.value = false
  formVisible.value = true

  await nextTick()
  if (route.query.review === '1' && reviewSectionRef.value) {
    activeTab.value = 'reviews'
    await nextTick()
    reviewSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  await nextTick()
  if (actionRowRef.value) {
    observer = new IntersectionObserver(entries => {
      stickyShow.value = !entries[0].isIntersecting
    }, { threshold: 0, rootMargin: '-68px 0px 0px 0px' })
    observer.observe(actionRowRef.value)
  }
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.book-detail-page {
  background: var(--color-bg);
  min-height: 100vh;
  padding-bottom: 80px;
  position: relative;
}
.book-detail-page::after {
  content: '';
  position: fixed;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 512 512' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.75' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.45'/%3E%3C/svg%3E");
  opacity: 0.025;
  pointer-events: none;
  z-index: 9999;
}

.loading-state { max-width: 1320px; margin: 0 auto; padding: 40px; }
.skeleton-book { display: flex; gap: 40px; }
.skeleton { background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 8px; }
.skeleton.cover { width: 300px; height: 400px; }
.skeleton-info { flex: 1; display: flex; flex-direction: column; gap: 16px; }
.skeleton.title { height: 40px; width: 70%; }
.skeleton.author { height: 24px; width: 40%; }
.skeleton.price { height: 36px; width: 30%; }
@keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

/* Breadcrumb */
.breadcrumb { max-width: 1320px; margin: 0 auto; padding: 16px 40px 0; display: flex; align-items: center; gap: 6px; font-size: .8rem; color: var(--color-text-light); flex-wrap: wrap; }
.breadcrumb a { color: var(--color-text-light); text-decoration: none; }
.breadcrumb a:hover { color: var(--color-accent); }
.breadcrumb .sep { opacity: .4; }

/* ═══ PRODUCT HERO ═══ */
.product-hero { max-width: 1320px; margin: 0 auto; padding: 28px 40px 48px; display: grid; grid-template-columns: 360px 1fr; gap: 56px; align-items: start; }

.cover-side { position: sticky; top: 100px; }
.cover-main { width: 100%; aspect-ratio: 3/4; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-family: 'Noto Serif SC', 'STSong', serif; font-weight: 900; font-size: 2rem; color: rgba(237,230,214,0.85); padding: 48px; text-align: center; line-height: 1.4; box-shadow: 0 20px 50px var(--color-shadow-deep); position: relative; overflow: hidden; cursor: crosshair; }
.cover-main::before { content: ''; position: absolute; inset: 0; background: linear-gradient(135deg, rgba(255,255,255,0.05) 0%, transparent 50%, rgba(0,0,0,0.1) 100%); pointer-events: none; }
.cover-main .cover-badge { position: absolute; top: 14px; left: 14px; font-size: .7rem; font-weight: 700; padding: 4px 12px; border-radius: 5px; background: var(--color-accent); color: var(--color-primary-abyss); font-family: 'Noto Sans SC', sans-serif; }

.info-side {}
.info-category { font-size: .78rem; color: var(--color-accent-muted); margin-bottom: 6px; display: flex; align-items: center; gap: 6px; }
.info-title { font-size: 2.2rem; font-weight: 900; color: var(--color-text); margin-bottom: 4px; font-family: 'Noto Serif SC', 'STSong', serif; }
.info-subtitle { font-size: 1rem; color: var(--color-text-secondary); margin-bottom: 16px; font-style: italic; }

.info-authors { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid var(--color-divider); }
.author-chip { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.author-avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--color-primary-mid); display: flex; align-items: center; justify-content: center; font-size: .8rem; font-weight: 700; color: var(--color-accent-light); flex-shrink: 0; }
.author-info .ai-name { font-size: .88rem; font-weight: 600; color: var(--color-text); }
.author-info .ai-role { font-size: .72rem; color: var(--color-text-light); }

.info-rating-row { display: flex; align-items: center; gap: 18px; margin-bottom: 20px; flex-wrap: wrap; }
.rating-big { display: flex; align-items: baseline; gap: 6px; }
.rating-big .rb-num { font-family: 'Noto Serif SC', 'STSong', serif; font-size: 2.2rem; font-weight: 900; color: var(--color-accent); line-height: 1; }
.rating-big .rb-stars { font-size: 1rem; color: var(--color-accent); letter-spacing: 1px; }
.rating-big .rb-label { font-size: .78rem; color: var(--color-text-light); }
.rating-bar-group { display: flex; flex-direction: column; gap: 3px; }
.rb-row { display: flex; align-items: center; gap: 6px; font-size: .72rem; color: var(--color-text-light); white-space: nowrap; }
.rb-row .rb-level { width: 4px; height: 4px; border-radius: 50%; background: var(--color-accent); flex-shrink: 0; }
.rb-bar { width: 80px; height: 4px; background: var(--color-divider); border-radius: 2px; overflow: hidden; }
.rb-bar-fill { height: 100%; background: var(--color-accent); border-radius: 2px; }

.info-meta { display: flex; flex-wrap: wrap; gap: 8px 20px; margin-bottom: 20px; }
.meta-item { display: flex; align-items: center; gap: 5px; font-size: .82rem; color: var(--color-text-secondary); }
.meta-item .mi-label { color: var(--color-text-light); }
.meta-item .mi-val { font-weight: 500; }

.price-block { background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; padding: 20px 24px; margin-bottom: 24px; }
.price-row { display: flex; align-items: baseline; gap: 12px; margin-bottom: 8px; }
.price-now { font-family: 'Noto Serif SC', 'STSong', serif; font-size: 2rem; font-weight: 900; color: var(--color-accent-muted); line-height: 1; }
.price-original { font-size: 1rem; color: var(--color-text-light); text-decoration: line-through; }
.price-discount { font-size: .82rem; font-weight: 700; padding: 2px 10px; border-radius: 5px; background: rgba(160,64,64,0.08); color: #A04040; }
.price-save { font-size: .8rem; color: #5C8856; margin-bottom: 8px; }

.qty-row { display: flex; align-items: center; gap: 16px; margin-bottom: 20px; }
.qty-label { font-size: .84rem; color: var(--color-text-secondary); }
.qty-control { display: flex; align-items: center; border: 1px solid var(--color-divider-strong); border-radius: 8px; overflow: hidden; }
.qty-btn { width: 36px; height: 36px; border: none; background: var(--color-bg-cream); color: var(--color-text-secondary); font-size: 1rem; cursor: pointer; transition: all .2s; display: flex; align-items: center; justify-content: center; }
.qty-btn:hover { background: var(--color-bg-warm); color: var(--color-text); }
.qty-input { width: 48px; height: 36px; border: none; text-align: center; font-size: .92rem; font-weight: 600; color: var(--color-text); background: var(--color-bg-card); font-family: 'DM Mono', monospace; outline: none; }
.qty-stock { font-size: .78rem; color: #5C8856; }
.qty-stock.oos { color: #A04040; }

.action-row { display: flex; gap: 10px; margin-bottom: 16px; }
.btn-action { padding: 14px 0; border-radius: 10px; font-size: .95rem; font-weight: 600; cursor: pointer; transition: all .25s; border: none; display: flex; align-items: center; justify-content: center; gap: 8px; font-family: 'Noto Sans SC', sans-serif; }
.btn-action:disabled { opacity: 0.4; cursor: not-allowed; transform: none !important; }
.btn-buy { flex: 2; background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted)); color: var(--color-primary-abyss); box-shadow: 0 4px 16px rgba(192,154,75,0.25); }
.btn-buy:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 28px rgba(192,154,75,0.35); }
.btn-cart { flex: 1.5; background: var(--color-primary); color: var(--color-bg-warm); }
.btn-cart:hover:not(:disabled) { background: var(--color-primary-mid); transform: translateY(-2px); }
.btn-fav { width: 52px; height: 52px; border-radius: 10px; border: 1.5px solid var(--color-divider-strong); background: var(--color-bg-card); color: var(--color-text-light); font-size: 1.2rem; cursor: pointer; transition: all .2s; flex: none; }
.btn-fav:hover { border-color: #A04040; color: #A04040; background: rgba(160,64,64,0.08); }
.btn-fav.active { background: #A04040; border-color: #A04040; color: #fff; }

.promise-row { display: flex; gap: 16px; flex-wrap: wrap; }
.promise { display: flex; align-items: center; gap: 5px; font-size: .76rem; color: var(--color-text-light); }
.promise .p-icon { font-size: .8rem; }

/* ═══ DETAIL TABS ═══ */
.detail-section { max-width: 1320px; margin: 0 auto; padding: 0 40px 60px; display: grid; grid-template-columns: 1fr 320px; gap: 48px; }
.detail-main {}
.detail-tabs { display: flex; gap: 0; border-bottom: 2px solid var(--color-divider); margin-bottom: 28px; position: sticky; top: 68px; background: var(--color-bg); z-index: 50; padding-top: 4px; }
.detail-tab { padding: 14px 24px; font-size: .9rem; color: var(--color-text-light); cursor: pointer; border-bottom: 2px solid transparent; margin-bottom: -2px; transition: all .25s; font-weight: 400; }
.detail-tab:hover { color: var(--color-text); }
.detail-tab.active { color: var(--color-primary); font-weight: 600; border-bottom-color: var(--color-accent); }
.tab-panel { animation: fadeUp .4s ease; }

.desc-content { font-size: .94rem; color: var(--color-text-secondary); line-height: 2; }
.desc-content p { margin-bottom: 16px; }
.desc-content .desc-quote { padding: 20px 24px; background: var(--color-bg-card); border-left: 4px solid var(--color-accent); border-radius: 0 8px 8px 0; margin: 20px 0; font-family: 'Noto Serif SC', 'STSong', serif; font-size: 1rem; color: var(--color-text); font-style: italic; line-height: 1.9; }
.desc-content .desc-label { font-size: .82rem; font-weight: 600; color: var(--color-text); margin-bottom: 6px; margin-top: 24px; }
.desc-content .desc-label:first-child { margin-top: 0; }

/* Author Section */
.author-section { display: flex; gap: 24px; align-items: flex-start; }
.author-big-avatar { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, var(--color-primary-mid), var(--color-primary-dark)); display: flex; align-items: center; justify-content: center; font-size: 1.5rem; font-weight: 900; color: var(--color-accent-light); flex-shrink: 0; }
.author-bio h4 { font-size: 1.05rem; margin-bottom: 6px; font-family: 'Noto Serif SC', 'STSong', serif; }
.author-bio .ab-country { font-size: .78rem; color: var(--color-accent-muted); margin-bottom: 10px; }
.author-bio p { font-size: .9rem; color: var(--color-text-secondary); line-height: 1.9; }
.author-bio .ab-awards { margin-top: 12px; display: flex; gap: 8px; flex-wrap: wrap; }
.award { font-size: .72rem; padding: 3px 10px; border-radius: 4px; background: var(--color-accent-glow); color: var(--color-accent-muted); border: 1px solid rgba(192,154,75,0.12); }

/* TOC */
.toc-list { display: flex; flex-direction: column; gap: 0; }
.toc-item { display: flex; align-items: baseline; gap: 8px; padding: 10px 0; border-bottom: 1px solid var(--color-divider); font-size: .88rem; color: var(--color-text-secondary); cursor: pointer; transition: color .2s; }
.toc-item:hover { color: var(--color-accent); }
.toc-item .ti-num { font-family: 'DM Mono', monospace; font-size: .78rem; color: var(--color-text-light); width: 24px; flex-shrink: 0; }
.toc-item .ti-chapter { flex: 1; }
.toc-item .ti-dots { flex: 1; border-bottom: 1px dotted var(--color-divider-strong); margin: 0 8px; min-width: 20px; }
.toc-item .ti-page { font-family: 'DM Mono', monospace; font-size: .76rem; color: var(--color-text-light); }

/* Reviews Summary */
.reviews-summary { display: flex; gap: 28px; align-items: center; padding: 20px 24px; background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; margin-bottom: 24px; }
.rs-left { text-align: center; min-width: 80px; }
.rs-score { font-family: 'Noto Serif SC', 'STSong', serif; font-size: 2.8rem; font-weight: 900; color: var(--color-accent); line-height: 1; }
.rs-stars { font-size: .9rem; color: var(--color-accent); margin-top: 2px; }
.rs-count { font-size: .76rem; color: var(--color-text-light); margin-top: 4px; }
.rs-bars { flex: 1; display: flex; flex-direction: column; gap: 5px; }
.rsb-row { display: flex; align-items: center; gap: 8px; }
.rsb-label { font-size: .74rem; color: var(--color-text-light); width: 24px; text-align: right; }
.rsb-bar { flex: 1; height: 8px; background: var(--color-divider); border-radius: 4px; overflow: hidden; }
.rsb-bar-fill { height: 100%; background: var(--color-accent); border-radius: 4px; transition: width .6s ease; }
.rsb-pct { font-size: .72rem; color: var(--color-text-light); width: 32px; }

/* Review Cards */
.review-card { padding: 18px 20px; background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 10px; transition: border-color .2s; margin-bottom: 12px; }
.review-card:hover { border-color: rgba(192,154,75,0.12); }
.rc-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.rc-user { display: flex; align-items: center; gap: 10px; }
.rc-avatar { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: .7rem; font-weight: 700; color: var(--color-accent-light); flex-shrink: 0; }
.rc-name { font-size: .84rem; font-weight: 600; color: var(--color-text); }
.rc-date { font-size: .72rem; color: var(--color-text-light); }
.rc-stars { font-size: .8rem; color: var(--color-accent); letter-spacing: 1px; white-space: nowrap; }
.rc-content { font-size: .88rem; color: var(--color-text-secondary); line-height: 1.8; }
.no-reviews { color: var(--color-text-light); text-align: center; padding: 40px; font-style: italic; }

/* Review Form */
.review-form { padding: 20px 24px; background: var(--color-bg-card); border: 1px solid var(--color-divider); border-radius: 12px; margin-bottom: 24px; }
.review-login-hint { text-align: center; padding: 16px; color: var(--color-text-light); font-size: .88rem; }
.login-link { color: var(--color-accent); font-weight: 500; text-decoration: none; }
.login-link:hover { text-decoration: underline; }
.review-already { text-align: center; padding: 16px; color: var(--color-text-light); font-size: .88rem; }
.review-form-body { display: flex; flex-direction: column; gap: 12px; }
.form-star-row { display: flex; align-items: center; gap: 12px; }
.form-label { font-size: .84rem; color: var(--color-text-secondary); }
.star-input { display: flex; align-items: center; gap: 2px; }
.star-click { font-size: 22px; color: var(--color-divider-strong); cursor: pointer; transition: all .15s; user-select: none; }
.star-click.filled { color: var(--color-accent); }
.star-click:hover { transform: scale(1.2); }
.star-hint { font-size: .78rem; color: var(--color-accent-muted); margin-left: 8px; }
.form-textarea { width: 100%; padding: 12px; font-size: .88rem; font-family: 'Noto Sans SC', sans-serif; color: var(--color-text); background: var(--color-bg-cream); border: 1px solid var(--color-divider-strong); border-radius: 8px; resize: vertical; min-height: 80px; outline: none; transition: border-color .2s; box-sizing: border-box; }
.form-textarea:focus { border-color: var(--color-accent); }
.form-textarea::placeholder { color: var(--color-text-light); }
.form-actions { display: flex; align-items: center; gap: 12px; }
.btn-submit { padding: 10px 28px; font-size: .84rem; font-weight: 500; color: var(--color-bg-warm); background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted)); border: none; border-radius: 8px; cursor: pointer; transition: all .3s; font-family: 'Noto Sans SC', sans-serif; }
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 16px rgba(192,154,75,0.25); }
.btn-submit:disabled { opacity: .5; cursor: not-allowed; }
.submit-error { font-size: .78rem; color: #A04040; }

/* SIDEBAR */
.detail-sidebar { position: sticky; top: 100px; }
.sidebar-section { margin-bottom: 28px; }
.sidebar-section h4 { font-size: .92rem; font-weight: 700; color: var(--color-text); margin-bottom: 14px; padding-bottom: 10px; border-bottom: 1px solid var(--color-divider); font-family: 'Noto Serif SC', 'STSong', serif; }

.related-book { display: flex; gap: 12px; align-items: center; padding: 10px; border-radius: 10px; cursor: pointer; transition: all .25s; margin-bottom: 6px; }
.related-book:hover { background: var(--color-bg-card); transform: translateX(4px); }
.related-book .rb-cover { width: 48px; height: 64px; border-radius: 5px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-family: 'Noto Serif SC', 'STSong', serif; font-weight: 700; font-size: .65rem; color: rgba(237,230,214,0.7); }
.related-book .rb-info .rbi-title { font-size: .84rem; font-weight: 600; color: var(--color-text); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 180px; }
.related-book .rb-info .rbi-author { font-size: .72rem; color: var(--color-text-light); }
.related-book .rb-info .rbi-price { font-family: 'Noto Serif SC', 'STSong', serif; font-weight: 700; font-size: .88rem; color: var(--color-accent-muted); margin-top: 2px; }
.related-book .rb-info .rbi-rating { font-size: .7rem; color: var(--color-accent); }

.same-author-book { display: flex; gap: 10px; padding: 8px; border-radius: 8px; cursor: pointer; transition: all .25s; margin-bottom: 6px; align-items: center; }
.same-author-book:hover { background: var(--color-bg-card); }
.same-author-book .sab-cover { width: 40px; height: 54px; border-radius: 4px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-family: 'Noto Serif SC', 'STSong', serif; font-weight: 700; font-size: .5rem; color: rgba(237,230,214,0.7); }
.same-author-book .sab-title { font-size: .8rem; color: var(--color-text-secondary); }
.same-author-book .sab-price { font-size: .76rem; color: var(--color-accent-muted); font-weight: 600; }

/* STICKY BUY BAR */
.sticky-bar { position: fixed; bottom: 0; left: 0; right: 0; z-index: 100; background: var(--color-bg-card); border-top: 1px solid var(--color-divider); box-shadow: 0 -4px 24px var(--color-shadow-deep); transform: translateY(100%); transition: transform .35s cubic-bezier(.25,.46,.45,.94); }
.sticky-bar.show { transform: translateY(0); }
.sb-inner { max-width: 1320px; margin: 0 auto; padding: 12px 40px; display: flex; align-items: center; justify-content: space-between; gap: 20px; }
.sb-book { display: flex; align-items: center; gap: 12px; min-width: 0; }
.sb-cover { width: 40px; height: 54px; border-radius: 5px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; font-family: 'Noto Serif SC', 'STSong', serif; font-weight: 700; font-size: .6rem; color: rgba(237,230,214,0.7); }
.sb-title { font-size: .88rem; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.sb-right { display: flex; align-items: center; gap: 16px; flex-shrink: 0; }
.sb-price { font-family: 'Noto Serif SC', 'STSong', serif; font-size: 1.3rem; font-weight: 900; color: var(--color-accent-muted); white-space: nowrap; }
.sb-price .sb-orig { font-size: .82rem; color: var(--color-text-light); text-decoration: line-through; font-weight: 400; margin-left: 4px; }
.sb-btns { display: flex; gap: 8px; }
.sb-btn { padding: 10px 24px; border-radius: 8px; font-size: .86rem; font-weight: 600; cursor: pointer; transition: all .2s; border: none; font-family: 'Noto Sans SC', sans-serif; }
.sb-btn:disabled { opacity: .4; cursor: not-allowed; }
.sb-btn.buy { background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted)); color: var(--color-primary-abyss); }
.sb-btn.buy:hover:not(:disabled) { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(192,154,75,0.3); }
.sb-btn.cart { background: var(--color-primary); color: var(--color-bg-warm); }
.sb-btn.cart:hover:not(:disabled) { background: var(--color-primary-mid); }

/* TOAST */
:global(.toast-wrap) { position: fixed; top: 80px; right: 32px; z-index: 300; display: flex; flex-direction: column; gap: 8px; pointer-events: none; }
:global(.toast) { background: var(--color-primary-dark); color: var(--color-bg-warm); padding: 12px 20px; border-radius: 8px; font-size: .84rem; box-shadow: 0 8px 24px var(--color-shadow-deep); display: flex; align-items: center; gap: 10px; animation: tIn .35s ease; }
:global(.toast.out) { animation: tOut .3s ease forwards; }
:global(.toast .td) { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; background: #5C8856; }
@keyframes tIn { from { opacity: 0; transform: translateX(20px); } to { opacity: 1; transform: translateX(0); } }
@keyframes tOut { to { opacity: 0; transform: translateX(20px); } }
@keyframes fadeUp { from { opacity: 0; transform: translateY(14px); } to { opacity: 1; transform: translateY(0); } }

@media (max-width: 1024px) {
  .product-hero { grid-template-columns: 280px 1fr; gap: 36px; }
  .detail-section { grid-template-columns: 1fr 260px; gap: 32px; }
}
@media (max-width: 768px) {
  .breadcrumb { padding: 12px 20px 0; }
  .product-hero { grid-template-columns: 1fr; gap: 28px; padding: 20px 20px 36px; }
  .cover-side { position: static; display: flex; flex-direction: column; align-items: center; }
  .cover-main { max-width: 260px; }
  .info-title { font-size: 1.6rem; }
  .detail-section { grid-template-columns: 1fr; padding: 0 20px 40px; }
  .detail-sidebar { position: static; margin-top: 32px; }
  .reviews-summary { flex-direction: column; text-align: center; }
  .sb-inner { padding: 10px 16px; }
  .sb-book { display: none; }
  .sb-btn { padding: 9px 16px; font-size: .8rem; }
}
</style>
