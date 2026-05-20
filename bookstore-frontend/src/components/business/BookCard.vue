<template>
  <div class="book-card" @click="$emit('click')">
    <div class="book-cover">
      <img
        v-if="book.coverUrl"
        :src="book.coverUrl"
        :alt="book.title"
        class="book-cover-img"
      />
      <div v-else class="book-cover-img book-cover-fallback" :class="coverVariant">
        {{ book.title.slice(0, 6) }}
      </div>
      <div v-if="book.stock === 0" class="book-badge out">缺货</div>
      <div class="book-fav" @click.stop>
        <span class="heart">♡</span>
      </div>
    </div>
    <div class="book-info">
      <div class="book-title">{{ book.title }}</div>
      <div class="book-author">{{ book.author }}</div>
      <div class="book-meta">
        <div class="book-price">
          ¥{{ book.price }}
          <span v-if="book.originalPrice" class="original">¥{{ book.originalPrice }}</span>
        </div>
        <div v-if="book.avgRating" class="book-rating">
          ★ <span>{{ book.avgRating?.toFixed(1) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  book: { type: Object, required: true }
})

defineEmits(['click'])

const variants = ['cover-1', 'cover-2', 'cover-3', 'cover-4', 'cover-5', 'cover-6', 'cover-7', 'cover-8', 'cover-9', 'cover-10']

const coverVariant = computed(() => {
  const id = props.book.id
  if (typeof id === 'string') {
    const num = parseInt(id, 10) || 1
    return variants[(num - 1) % variants.length]
  }
  return variants[((id || 1) - 1) % variants.length]
})
</script>

<style scoped>
.book-card {
  background: var(--color-bg-card);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--color-divider);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow:
    0 16px 40px var(--color-shadow-heavy),
    0 2px 6px var(--color-shadow);
  border-color: rgba(192,154,75,0.15);
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
  transition: transform 0.5s ease;
}

.book-card:hover .book-cover-img { transform: scale(1.04); }

.book-cover-fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 1.3rem;
  color: rgba(237,230,214,0.85);
  padding: 30px;
  text-align: center;
  line-height: 1.4;
}

.book-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: var(--color-accent);
  color: var(--color-primary-abyss);
  font-size: 0.65rem;
  font-weight: 700;
  padding: 3px 10px;
  border-radius: 4px;
  letter-spacing: 0.06em;
  box-shadow: 0 2px 6px rgba(192,154,75,0.25);
}

.book-badge.out {
  background: var(--color-primary-dark);
  color: rgba(237,230,214,0.5);
}

.book-fav {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  background: rgba(46,31,21,0.55);
  backdrop-filter: blur(10px);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-bg);
  font-size: 0.85rem;
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
  background: var(--color-accent);
  color: var(--color-primary-abyss);
}

.heart { line-height: 1; }

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
  gap: 2px;
  font-size: 0.72rem;
  color: var(--color-accent);
}

.book-rating span { color: var(--color-text-light); margin-left: 2px; }

.cover-1 { background: linear-gradient(160deg, #5D4037 0%, #3E2723 60%, #2C1A12 100%); }
.cover-2 { background: linear-gradient(160deg, #6D4C41 0%, #4E342E 100%); }
.cover-3 { background: linear-gradient(160deg, #1A1A2E 0%, #16213E 50%, #0F3460 100%); }
.cover-4 { background: linear-gradient(160deg, #3D2B1F 0%, #5C3A21 100%); }
.cover-5 { background: linear-gradient(160deg, #8B0000 0%, #4A0404 100%); }
.cover-6 { background: linear-gradient(160deg, #1B3A2D 0%, #0D2818 100%); }
.cover-7 { background: linear-gradient(160deg, #2C3E50 0%, #1A252F 100%); }
.cover-8 { background: linear-gradient(160deg, #3C1F3A 0%, #2A1526 100%); }
.cover-9 { background: linear-gradient(160deg, #8B4513 0%, #5C2D0E 100%); }
.cover-10 { background: linear-gradient(160deg, #B8860B 0%, #8B6914 100%); }
</style>
