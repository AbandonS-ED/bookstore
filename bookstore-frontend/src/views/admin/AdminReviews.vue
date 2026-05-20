<template>
  <div class="admin-reviews">
    <!-- 操作栏 -->
    <div class="toolbar">
      <span class="toolbar-title">共 {{ total }} 条评论</span>
    </div>

    <!-- 评论表格 -->
    <el-table :data="reviews" v-loading="loading" stripe class="reviews-table">
      <el-table-column prop="bookTitle" label="书籍" min-width="180" show-overflow-tooltip />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="rating" label="评分" width="120">
        <template #default="{ row }">
          <div class="rating">
            <span v-for="i in 5" :key="i" class="star" :class="{ filled: i <= row.rating }">★</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="评论内容" min-width="300" show-overflow-tooltip />
      <el-table-column prop="createTime" label="评论时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="danger" link @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadReviews"
        @current-change="loadReviews"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const reviews = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadReviews = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const res = await adminApi.getReviewList(params)
    reviews.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load reviews:', error)
  } finally {
    loading.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.deleteReview(id)
      ElMessage.success('删除成功')
      loadReviews()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadReviews()
})
</script>

<style scoped>
.admin-reviews {
  max-width: 1400px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.toolbar-title {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.reviews-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.rating {
  display: flex;
  align-items: center;
  gap: 2px;
}

.star {
  font-size: 14px;
  color: var(--color-divider);
}

.star.filled {
  color: var(--color-accent);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}
</style>