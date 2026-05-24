<template>
  <div class="admin-inventory">
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon brown">📦</div>
        <div>
          <div class="stat-value">{{ totalStock }}</div>
          <div class="stat-label">总库存</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">⚠️</div>
        <div>
          <div class="stat-value" style="color: var(--color-red);">{{ lowStockCount }}</div>
          <div class="stat-label">库存不足 (&lt;10)</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">📋</div>
        <div>
          <div class="stat-value" style="color: var(--color-accent);">{{ zeroStockCount }}</div>
          <div class="stat-label">缺货</div>
        </div>
      </div>
    </div>

    <div class="toolbar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索书名、作者..."
          clearable
          @clear="loadBooks"
          @keyup.enter="handleSearch"
        >
          <template #prefix><span>🔍</span></template>
        </el-input>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-select v-model="stockFilter" placeholder="库存筛选" style="width: 140px" @change="handleSearch">
        <el-option label="全部" value="" />
        <el-option label="库存不足 (&lt;10)" value="low" />
        <el-option label="缺货 (=0)" value="zero" />
        <el-option label="库存充足 (≥10)" value="enough" />
      </el-select>
    </div>

    <el-table :data="books" v-loading="loading" stripe class="inventory-table">
      <el-table-column label="书籍" min-width="220">
        <template #default="{ row }">
          <div class="book-cell">
            <div class="book-cover-sm" :style="getCoverStyle(row.id)"></div>
            <div>
              <div class="b-title">{{ row.title }}</div>
              <div class="b-author">{{ row.author }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="stock" label="当前库存" width="100">
        <template #default="{ row }">
          <span :class="{ 'stock-low': row.stock < 10, 'stock-zero': row.stock === 0 }">
            {{ row.stock }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="sales" label="总销量" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.stock === 0" type="danger" size="small">缺货</el-tag>
          <el-tag v-else-if="row.stock < 10" type="warning" size="small">库存不足</el-tag>
          <el-tag v-else type="success" size="small">充足</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openAdjustDialog(row)">
            调整库存
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadBooks"
        @current-change="loadBooks"
      />
    </div>

    <el-dialog
      v-model="dialogVisible"
      title="调整库存"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="adjust-info">
        <div class="adjust-book">
          <div class="book-cover-sm" :style="getCoverStyle(adjustBook?.id)"></div>
          <div>
            <div class="adjust-book-title">{{ adjustBook?.title }}</div>
            <div class="adjust-book-author">{{ adjustBook?.author }}</div>
          </div>
        </div>
        <div class="adjust-current">
          当前库存：<strong :class="{ 'stock-low': adjustBook?.stock < 10, 'stock-zero': adjustBook?.stock === 0 }">{{ adjustBook?.stock }}</strong>
        </div>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="新库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确认调整
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import { categoryApi } from '@/api/category'
import { getCoverStyle } from '@/utils/cover'

const loading = ref(false)
const submitLoading = ref(false)
const books = ref([])
const searchKeyword = ref('')
const stockFilter = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const adjustBook = ref(null)
const formRef = ref(null)
const form = reactive({ stock: 0 })

const rules = {
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

const totalStock = computed(() => books.value.reduce((sum, b) => sum + (b.stock || 0), 0))
const lowStockCount = computed(() => books.value.filter(b => b.stock > 0 && b.stock < 10).length)
const zeroStockCount = computed(() => books.value.filter(b => b.stock === 0).length)

const loadBooks = async () => {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    const res = await adminApi.getInventoryList(params)
    let records = res.data.records || []
    if (stockFilter.value === 'low') records = records.filter(b => b.stock > 0 && b.stock < 10)
    else if (stockFilter.value === 'zero') records = records.filter(b => b.stock === 0)
    else if (stockFilter.value === 'enough') records = records.filter(b => b.stock >= 10)
    books.value = records
    total.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to load books:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadBooks()
}

const openAdjustDialog = (row) => {
  adjustBook.value = row
  form.stock = row.stock
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      await adminApi.updateBookStock(adjustBook.value.id, form.stock)
      ElMessage.success(`「${adjustBook.value.title}」库存已调整为 ${form.stock}`)
      dialogVisible.value = false
      loadBooks()
    } catch (error) {
      ElMessage.error(error.message || '调整失败')
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  loadBooks()
})
</script>

<style scoped>
.admin-inventory {
  max-width: 1400px;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: 12px;
  padding: 22px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s;
}

.stat-card:hover {
  border-color: rgba(192, 154, 75, 0.15);
  box-shadow: 0 8px 24px var(--color-shadow);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  flex-shrink: 0;
}

.stat-icon.brown { background: rgba(74, 53, 38, 0.06); color: var(--color-primary-mid); }
.stat-icon.red { background: var(--color-red-bg); color: var(--color-red); }
.stat-icon.orange { background: var(--color-accent-glow); color: var(--color-accent); }

.stat-value {
  font-family: var(--font-display);
  font-size: 1.6rem;
  font-weight: 900;
  color: var(--color-text);
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
  gap: 12px;
}

.search-box {
  display: flex;
  gap: var(--space-3);
  flex: 1;
}

.search-box .el-input {
  width: 300px;
}

.inventory-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.book-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-cover-sm {
  width: 36px;
  height: 48px;
  border-radius: 4px;
  flex-shrink: 0;
}

.b-title { font-weight: 500; font-size: 0.85rem; }
.b-author { font-size: 0.74rem; color: var(--color-text-light); }

.stock-low { color: var(--color-accent-muted); font-weight: 700; }
.stock-zero { color: var(--color-red); font-weight: 700; }

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}

.adjust-info {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--color-bg-cream);
  border-radius: 8px;
  border: 1px solid var(--color-divider);
}

.adjust-book {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.adjust-book-title {
  font-weight: 600;
  font-size: 0.95rem;
}

.adjust-book-author {
  font-size: 0.8rem;
  color: var(--color-text-light);
}

.adjust-current {
  font-size: 0.88rem;
  color: var(--color-text-secondary);
}
</style>
