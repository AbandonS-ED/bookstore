<template>
  <div class="admin-reviews">
    <!-- 搜索和操作栏 -->
    <div class="toolbar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名、书名..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <span>🔍</span>
          </template>
        </el-input>
        <el-select v-model="statusFilter" style="width: 120px" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="显示" :value="1" />
          <el-option label="隐藏" :value="0" />
        </el-select>
        <el-select v-model="ratingFilter" style="width: 120px" @change="handleSearch">
          <el-option label="全部评分" value="" />
          <el-option label="5星" :value="5" />
          <el-option label="4星" :value="4" />
          <el-option label="3星" :value="3" />
          <el-option label="2星" :value="2" />
          <el-option label="1星" :value="1" />
        </el-select>
      </div>
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
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '显示' : '隐藏' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'primary'"
            link
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '隐藏' : '显示' }}
          </el-button>
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
const searchKeyword = ref('')
const statusFilter = ref('')
const ratingFilter = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadReviews = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value || undefined,
      status: statusFilter.value !== '' ? Number(statusFilter.value) : undefined,
      rating: ratingFilter.value !== '' ? Number(ratingFilter.value) : undefined
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

const handleSearch = () => {
  pageNum.value = 1
  loadReviews()
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
  })  .catch(() => {})
}

const toggleStatus = async (row) => {
  try {
    const action = row.status === 1 ? '隐藏' : '显示'
    await ElMessageBox.confirm(`确定要${action}该评论吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    if (row.status === 1) {
      await adminApi.hideReview(row.id)
    } else {
      await adminApi.showReview(row.id)
    }
    ElMessage.success(`${action}成功`)
    loadReviews()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败')
    }
  }
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

.search-box {
  display: flex;
  gap: var(--space-3);
}

.search-box .el-input {
  width: 300px;
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