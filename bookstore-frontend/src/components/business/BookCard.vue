<template>
  <div
    ref="cardRef"
    :class="['book-card', { 'is-visible': isVisible, 'list-mode': list }]"
    :style="{ transitionDelay: `${delay}ms` }"
    @click="$emit('click')"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <div class="book-cover">
      <img
        v-if="book.coverUrl && !coverError"
        :src="book.coverUrl"
        :alt="book.title"
        class="book-cover-img"
        loading="lazy"
        @error="coverError = true"
      />
      <div v-if="!book.coverUrl || coverError" class="book-cover-img book-cover-fallback" :style="fallbackStyle">
        <span class="cover-title">{{ book.title }}</span>
        <span class="cover-author">{{ book.author }}</span>
      </div>

      <div v-if="badge" class="book-badge" :class="badgeClass">{{ badge }}</div>

      <button class="book-fav" :class="{ active: isFavored }" @click.stop="toggleFav">
        <span class="heart">{{ isFavored ? '♥' : '♡' }}</span>
      </button>

      <transition name="quote-fade">
        <div v-if="book.quote && isHovered && !list" class="book-quote-overlay">
          <div class="quote-content">
            <svg class="quote-mark quote-mark-left" viewBox="0 0 24 24" width="20" height="20">
              <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
            </svg>
            <p class="quote-text">{{ book.quote }}</p>
            <svg class="quote-mark quote-mark-right" viewBox="0 0 24 24" width="20" height="20">
              <path d="M18 7h-3l-2 4v6h6v-6h-3l2-4zm-8 0H7L5 11v6h6v-6H8l2-4z" fill="currentColor"/>
            </svg>
          </div>
        </div>
      </transition>
    </div>

    <div class="book-info">
      <div class="book-title">{{ book.title }}</div>
      <div class="book-author">{{ book.author }}</div>
      <div class="book-meta">
        <div class="book-price">
          ¥{{ book.price }}
          <span v-if="book.originalPrice && book.originalPrice !== book.price" class="original">¥{{ book.originalPrice }}</span>
        </div>
        <div class="book-rating">
          <template v-if="book.avgRating">
            <span class="stars">{{ renderStars(book.avgRating) }}</span>
            <span class="rating-val">{{ book.avgRating?.toFixed(1) }}</span>
          </template>
          <span v-else class="no-rating">暂无评分</span>
        </div>
      </div>
    </div>

    <div v-if="list && book.quote" class="book-quote-col">
      <svg class="quote-col-mark" viewBox="0 0 24 24" width="14" height="14">
        <path d="M6 17h3l2-4V7H5v6h3l-2 4zm8 0h3l2-4V7h-6v6h3l-2 4z" fill="currentColor"/>
      </svg>
      <p class="quote-col-text">{{ book.quote }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { favoriteApi } from '@/api/favorite'
import { useFavoriteStore } from '@/stores/favorite'

const props = defineProps({
  book: { type: Object, required: true },
  delay: { type: Number, default: 0 },
  list: { type: Boolean, default: false }
})

const emit = defineEmits(['click'])

const favoriteStore = useFavoriteStore()
const cardRef = ref(null)
const isVisible = ref(false)
const isFavored = ref(false)
const isHovered = ref(false)
const coverError = ref(false)

import { getCoverStyle } from '@/utils/cover'

const fallbackStyle = computed(() => getCoverStyle(props.book.id))

const badge = computed(() => {
  if (props.book.badge) return props.book.badge
  if (props.book.stock === 0) return ''
  if (props.book.avgRating && props.book.avgRating >= 9) return '经典'
  if (props.book.sales && props.book.sales > 5000) return '畅销'
  return ''
})

const badgeClass = computed(() => {
  switch (badge.value) {
    case '畅销': return 'badge-hot'
    case '新书': return 'badge-new'
    case '经典': return 'badge-classic'
    case '热卖': return 'badge-sale'
    default: return ''
  }
})

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

const toggleFav = async () => {
  const bookId = String(props.book.id)
  try {
    if (isFavored.value) {
      await favoriteApi.remove(bookId)
      isFavored.value = false
      favoriteStore.favoriteIds = favoriteStore.favoriteIds.filter(id => id !== bookId)
    } else {
      await favoriteApi.add(bookId)
      isFavored.value = true
      if (!favoriteStore.favoriteIds.includes(bookId)) {
        favoriteStore.favoriteIds.push(bookId)
      }
    }
  } catch {
    isFavored.value = !isFavored.value
  }
}

let observer = null

onMounted(() => {
  if (!cardRef.value) return
  if (!favoriteStore.favoriteIds.length) {
    favoriteStore.fetchFavoriteIds()
  }
  isFavored.value = favoriteStore.isFavorited(props.book.id)
  observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        setTimeout(() => {
          isVisible.value = true
        }, props.delay)
        observer.unobserve(cardRef.value)
      }
    },
    { threshold: 0.1 }
  )
  observer.observe(cardRef.value)
})

onUnmounted(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
.book-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  cursor: pointer;
  opacity: 0;
  transform: translateY(24px);
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.book-card.is-visible {
  opacity: 1;
  transform: translateY(0);
}

.book-card:hover {
  transform: translateY(-6px);
  box-shadow:
    0 16px 40px var(--color-shadow-heavy),
    0 2px 6px var(--color-shadow);
  border-color: rgba(192,154,75,0.15);
}

.book-card.is-visible:hover {
  transform: translateY(-6px);
}

.book-cover {
  position: relative;
  aspect-ratio: 3/4;
  overflow: hidden;
}

.book-cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.book-card:hover .book-cover-img { transform: scale(1.06); }

.book-cover-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  text-align: center;
  box-shadow:
    0 30px 60px rgba(0,0,0,0.35),
    inset 0 1px 0 rgba(192,154,75,0.08);
  border-left: 3px solid rgba(192,154,75,0.12);
  border-radius: 6px;
}

.cover-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.8rem;
  color: rgba(237,230,214,0.85);
  text-shadow: 0 1px 4px rgba(0,0,0,0.2);
  word-break: break-all;
  line-height: 1.3;
  margin-bottom: 12px;
}

.cover-author {
  font-size: 0.85rem;
  color: rgba(237,230,214,0.55);
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.book-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  font-size: 0.6rem;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 4px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  z-index: 2;
}

.badge-hot { background: #C09A4B; color: #1C120C; }
.badge-new { background: #5C8856; color: #F5F0E8; }
.badge-classic { background: #4A6E8A; color: #F5F0E8; }
.badge-sale { background: #A04040; color: #F5F0E8; }

.book-fav {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(46,31,21,0.55);
  backdrop-filter: blur(8px);
  border-radius: 50%;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transform: scale(0.85);
  transition: all 0.3s ease;
  z-index: 3;
}

.book-card:hover .book-fav {
  opacity: 1;
  transform: scale(1);
}

.book-fav:hover {
  background: rgba(192,154,75,0.7);
}

.book-fav.active {
  opacity: 1;
  transform: scale(1);
}

.book-fav.active .heart {
  color: #C09A4B;
}

.heart {
  font-size: 0.95rem;
  color: rgba(237,230,214,0.8);
  line-height: 1;
  transition: transform 0.2s ease;
}

.book-fav:hover .heart {
  transform: scale(1.15);
}

.book-fav.active:hover .heart {
  transform: scale(1.15);
}

.book-quote-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(28, 18, 12, 0.92) 0%,
    rgba(44, 31, 21, 0.88) 50%,
    rgba(28, 18, 12, 0.95) 100%
  );
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
  color: rgba(192, 154, 75, 0.6);
  flex-shrink: 0;
}

.quote-mark-left {
  margin-bottom: 8px;
  align-self: flex-start;
  margin-left: -4px;
}

.quote-mark-right {
  margin-top: 8px;
  align-self: flex-end;
  margin-right: -4px;
}

.quote-text {
  font-family: 'Noto Serif SC', 'STSong', 'SimSun', serif;
  font-size: 0.85rem;
  line-height: 1.8;
  color: rgba(237, 230, 214, 0.95);
  text-align: justify;
  margin: 0;
  max-height: 100%;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  -webkit-box-orient: vertical;
  letter-spacing: 0.05em;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.quote-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.quote-fade-leave-active {
  transition: all 0.3s cubic-bezier(0.55, 0, 1, 0.45);
}

.quote-fade-enter-from {
  opacity: 0;
  transform: scale(1.05);
}

.quote-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.book-info {
  padding: 16px 18px 20px;
}

.book-title {
  font-family: var(--font-display);
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  font-size: 0.8rem;
  color: var(--color-text-light);
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.book-price {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.1rem;
  color: var(--color-accent-muted);
}

.book-price .original {
  font-size: 0.75rem;
  color: var(--color-text-light);
  text-decoration: line-through;
  font-weight: 400;
  margin-left: 5px;
}

.book-rating {
  display: flex;
  align-items: center;
  gap: 3px;
  color: var(--color-accent);
}

.book-rating .stars {
  font-size: 0.7rem;
}

.book-rating .no-rating {
  font-size: 0.7rem;
  color: var(--color-text-light);
  font-style: italic;
}

.rating-val {
  font-size: 0.72rem;
  color: var(--color-text-light);
}

/* Quote column (list mode) */
.book-quote-col {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 20px;
  border-left: 1px solid var(--color-divider);
  width: 220px;
  flex-shrink: 0;
  align-self: center;
}
.quote-col-mark {
  flex-shrink: 0;
  margin-top: 3px;
  color: var(--color-accent);
  opacity: 0.5;
}
.quote-col-text {
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

/* List mode */
.book-card.list-mode {
  display: flex;
  flex-direction: row;
  opacity: 1;
  transform: none;
}
.book-card.list-mode:hover {
  transform: translateY(-2px);
}
.book-card.list-mode .book-cover {
  width: 110px;
  height: 150px;
  aspect-ratio: auto;
  flex-shrink: 0;
}
.book-card.list-mode .book-cover-img {
  border-radius: 0;
}
.book-card.list-mode .book-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 14px 20px;
}
.book-card.list-mode .book-title {
  font-size: 1rem;
  -webkit-line-clamp: 1;
}
.book-card.list-mode .book-author {
  margin-bottom: 6px;
}
.book-card.list-mode .book-fav {
  top: auto;
  bottom: 10px;
  right: 10px;
}
.book-card.list-mode .book-badge {
  top: 8px;
  left: 8px;
}
.book-card.list-mode .book-quote-overlay {
  display: none !important;
}
</style>
