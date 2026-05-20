<template>
  <div class="reviews-page">
    <div class="page-container">
      <div class="page-header">
        <h1>我的评价</h1>
      </div>

      <div v-if="loading" class="loading-state">
        <div v-for="i in 3" :key="i" class="review-skeleton">
          <div class="skeleton cover"></div>
          <div class="skeleton-content">
            <div class="skeleton title"></div>
            <div class="skeleton stars"></div>
            <div class="skeleton text"></div>
          </div>
        </div>
      </div>

      <div v-else-if="reviews.length" class="review-list">
        <div v-for="review in reviews" :key="review.id" class="review-item">
          <div class="review-book">
            <img :src="review.bookCoverUrl || '/placeholder-book-sm.svg'" :alt="review.bookTitle" />
            <div class="book-info">
              <h3>{{ review.bookTitle }}</h3>
              <div class="review-stars">
                <span v-for="i in 5" :key="i" :class="{ filled: i <= review.rating }">★</span>
              </div>
            </div>
          </div>
          <p class="review-content">{{ review.content }}</p>
          <div class="review-footer">
            <span class="review-time">{{ formatDateTime(review.createTime) }}</span>
            <div class="review-actions">
              <button @click="handleDelete(review.id)" class="btn-delete">删除</button>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">📝</span>
        <h3>暂无评价</h3>
        <p>购买书籍后可以发表评价</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { reviewApi } from '@/api/review'
import { formatDateTime } from '@/utils/format'

const reviews = ref([])
const loading = ref(true)

const fetchReviews = async () => {
  loading.value = true
  try {
    const res = await reviewApi.getMyReviews()
    reviews.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch reviews:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await reviewApi.delete(id)
    ElMessage.success('删除成功')
    reviews.value = reviews.value.filter(r => r.id !== id)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchReviews()
})
</script>

<style scoped>
.reviews-page {
  padding: var(--space-8) 0 var(--space-16);
}

.page-header {
  margin-bottom: var(--space-8);
}

.page-header h1 {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-text);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.review-item {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.review-book {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.review-book img {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-md);
}

.book-info h3 {
  font-size: var(--text-base);
  font-weight: 500;
  margin-bottom: var(--space-2);
}

.review-stars span {
  font-size: 14px;
  color: var(--color-divider);
}

.review-stars span.filled {
  color: var(--color-accent-muted);
}

.review-content {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  line-height: 1.6;
  margin-bottom: var(--space-4);
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.btn-delete {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  background: none;
  border: none;
  cursor: pointer;
}

.btn-delete:hover {
  color: var(--color-accent);
}

.empty-state {
  text-align: center;
  padding: var(--space-16) 0;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
  display: block;
}

.empty-state h3 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  margin-bottom: var(--space-2);
}

.empty-state p {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.review-skeleton {
  display: flex;
  gap: var(--space-4);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-bg-cream) 25%, var(--color-bg-card) 50%, var(--color-bg-cream) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

.skeleton.cover {
  width: 60px;
  height: 80px;
}

.skeleton-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.skeleton.title {
  height: 20px;
  width: 60%;
}

.skeleton.stars {
  height: 14px;
  width: 40%;
}

.skeleton.text {
  height: 40px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>