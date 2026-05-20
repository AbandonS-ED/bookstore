<template>
  <div
    ref="cardRef"
    :class="['book-card', { 'is-visible': isVisible }]"
    :style="{ transitionDelay: `${delay}ms` }"
    @click="$emit('click')"
  >
    <div class="book-cover">
      <img
        v-if="book.coverUrl"
        :src="book.coverUrl"
        :alt="book.title"
        class="book-cover-img"
        loading="lazy"
      />
      <div v-else class="book-cover-img book-cover-fallback" :class="coverVariant">
        <span class="cover-title">{{ book.title.slice(0, 4) }}</span>
      </div>

      <div v-if="badge" class="book-badge" :class="badgeClass">{{ badge }}</div>

      <button class="book-fav" :class="{ active: isFavored }" @click.stop="toggleFav">
        <span class="heart">{{ isFavored ? '♥' : '♡' }}</span>
      </button>
    </div>

    <div class="book-info">
      <div class="book-title">{{ book.title }}</div>
      <div class="book-author">{{ book.author }}</div>
      <div class="book-meta">
        <div class="book-price">
          ¥{{ book.price }}
          <span v-if="book.originalPrice && book.originalPrice !== book.price" class="original">¥{{ book.originalPrice }}</span>
        </div>
        <div v-if="book.avgRating" class="book-rating">
          <svg class="star-icon" viewBox="0 0 20 20" width="12" height="12">
            <path d="M10 1l2.39 4.84L17.6 6.7l-3.8 3.7.9 5.24L10 13.2l-4.7 2.44.9-5.24L2.4 6.7l5.21-.86L10 1z" fill="currentColor"/>
          </svg>
          <span class="rating-val">{{ book.avgRating?.toFixed(1) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  book: { type: Object, required: true },
  delay: { type: Number, default: 0 }
})

const emit = defineEmits(['click'])

const cardRef = ref(null)
const isVisible = ref(false)
const isFavored = ref(false)

const variants = [
  'cover-1', 'cover-2', 'cover-3', 'cover-4', 'cover-5',
  'cover-6', 'cover-7', 'cover-8', 'cover-9', 'cover-10',
  'cover-11', 'cover-12'
]

const coverVariant = computed(() => {
  const id = props.book.id
  const num = typeof id === 'string' ? parseInt(id, 10) || 1 : (id || 1)
  return variants[(num - 1) % variants.length]
})

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

const toggleFav = () => {
  isFavored.value = !isFavored.value
}

let observer = null

onMounted(() => {
  if (!cardRef.value) return
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
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.2rem;
  padding: 24px;
  text-align: center;
  line-height: 1.5;
}

.cover-title {
  color: rgba(237,230,214,0.85);
  text-shadow: 0 1px 4px rgba(0,0,0,0.2);
  word-break: break-all;
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

.rating-val {
  font-size: 0.72rem;
  color: var(--color-text-light);
}

.star-icon {
  display: block;
}

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
</style>
