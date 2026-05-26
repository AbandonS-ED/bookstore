<template>
  <div class="admin-books">
    <!-- 搜索和操作栏 -->
    <div class="toolbar">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索书名、作者..."
          clearable
          @clear="loadBooks"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <span>🔍</span>
          </template>
        </el-input>
        <el-select v-model="categoryId" style="width: 140px" @change="handleSearch">
          <el-option label="全部分类" value="" />
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
        <el-select v-model="statusFilter" style="width: 120px" @change="handleSearch">
          <el-option label="全部状态" value="" />
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
          <el-option label="预售" :value="2" />
          <el-option label="即将上架" :value="3" />
          <el-option label="限时折扣" value="discount" />
        </el-select>
        <el-select v-model="sortBy" style="width: 140px" @change="handleSearch">
          <el-option label="排序方式" value="" />
          <el-option label="上架时间" value="createTime" />
          <el-option label="销量" value="sales" />
          <el-option label="价格" value="price" />
          <el-option label="库存" value="stock" />
        </el-select>
      </div>
      <el-button type="primary" class="add-btn" @click="openAddDialog">
        + 添加书籍
      </el-button>
    </div>

    <!-- 书籍表格 -->
    <el-table :data="books" v-loading="loading" stripe class="books-table">
      <el-table-column label="封面" width="100">
        <template #default="{ row }">
          <img v-if="row.coverUrl && !coverErrors[row.id]" :src="row.coverUrl" class="book-cover" @error="coverErrors[row.id] = true" />
          <div v-else class="book-cover-fallback" :style="getCoverStyle(row.id)">{{ row.title }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="书名" min-width="180" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column label="价格/折扣" width="140">
        <template #default="{ row }">
          <div class="price-cell">
            <span class="price">¥{{ row.price }}</span>
            <el-tag v-if="isOnDiscount(row)" type="danger" size="small" effect="dark" class="discount-tag">特价</el-tag>
            <span v-if="isOnDiscount(row)" class="discount-price">¥{{ row.discountPrice }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag
            :type="statusType(row.status)"
            size="small"
          >
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button
            v-if="row.status === 0 || row.status === 1"
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '下架' : '上架' }}
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
        @size-change="loadBooks"
        @current-change="loadBooks"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑书籍' : '添加书籍'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="出版日期" prop="publishDate">
          <el-date-picker
            v-model="form.publishDate"
            type="date"
            placeholder="选择出版日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入书籍描述"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" placeholder="请输入库存" />
        </el-form-item>
        <el-form-item label="封面URL" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="请输入封面图片URL" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">限时折扣（可选）</el-divider>
        <el-form-item label="折扣价">
          <el-input-number v-model="form.discountPrice" :min="0" :precision="2" :value-on-clear="null" placeholder="留空则不设折扣" style="width: 100%" />
        </el-form-item>
        <el-form-item label="截止时间">
          <el-date-picker
            v-model="form.discountEndTime"
            type="datetime"
            placeholder="选择折扣截止时间"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { categoryApi } from '@/api/category'
import { getCoverStyle } from '@/utils/cover'

const loading = ref(false)
const submitLoading = ref(false)
const books = ref([])
const coverErrors = ref({})
const categories = ref([])
const searchKeyword = ref('')
const categoryId = ref('')
const statusFilter = ref('')
const sortBy = ref('')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  publishDate: '',
  description: '',
  price: 0,
  stock: 0,
  coverUrl: '',
  categoryId: null,
  discountPrice: null,
  discountEndTime: null
})

const isOnDiscount = (row) => {
  return row.discountPrice != null && row.discountEndTime != null && new Date(row.discountEndTime) > new Date()
}

const statusType = (s) => {
  if (s === 1) return 'success'
  if (s === 2) return 'warning'
  if (s === 3) return 'info'
  return 'danger'
}

const statusLabel = (s) => {
  if (s === 1) return '上架'
  if (s === 2) return '预售'
  if (s === 3) return '即将上架'
  return '下架'
}



const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const loadBooks = async () => {
  loading.value = true
  coverErrors.value = {}
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: searchKeyword.value,
      categoryId: categoryId.value,
      discount: statusFilter.value === 'discount' ? true : undefined,
      status: statusFilter.value !== '' && statusFilter.value !== 'discount' ? Number(statusFilter.value) : undefined,
      sortBy: sortBy.value || undefined
    }
    const res = await adminApi.getBookList(params)
    books.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('Failed to load books:', error)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  }
}

const handleSearch = () => {
  pageNum.value = 1
  loadBooks()
}

const openAddDialog = () => {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    title: '',
    author: '',
    isbn: '',
    publisher: '',
    publishDate: null,
    description: '',
    price: 0,
    stock: 0,
    coverUrl: '',
    categoryId: null,
    discountPrice: null,
    discountEndTime: null
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    title: row.title,
    author: row.author,
    isbn: row.isbn || '',
    publisher: row.publisher || '',
    publishDate: row.publishDate ?? null,
    description: row.description || '',
    price: row.price,
    stock: row.stock,
    coverUrl: row.coverUrl || '',
    categoryId: row.categoryId,
    discountPrice: row.discountPrice ?? null,
    discountEndTime: row.discountEndTime ?? null
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      if (isEdit.value) {
        await adminApi.updateBook(form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.addBook(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadBooks()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这本书吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.deleteBook(id)
      ElMessage.success('删除成功')
      loadBooks()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadBooks()
  loadCategories()
})

const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await adminApi.updateBookStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
    loadBooks()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}
</script>

<style scoped>
.admin-books {
  max-width: 1400px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-6);
}

.search-box {
  display: flex;
  gap: var(--space-3);
}

.search-box .el-input {
  width: 300px;
}

.add-btn {
  background: var(--color-accent);
  border-color: var(--color-accent);
}

.books-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.book-cover {
  width: 60px;
  height: 80px;
  object-fit: cover;
  border-radius: var(--radius-sm);
}
.book-cover-fallback {
  width: 60px;
  height: 80px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display);
  font-weight: 600;
  font-size: 0.55rem;
  color: rgba(237,230,214,0.7);
  text-align: center;
  padding: 4px;
  line-height: 1.2;
  overflow: hidden;
}

.price {
  font-family: var(--font-display);
  font-weight: 600;
  color: var(--color-primary);
}

.price-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.price-cell .discount-tag {
  font-size: 11px;
}

.price-cell .discount-price {
  font-family: var(--font-display);
  font-weight: 700;
  color: var(--color-danger, #f56c6c);
  font-size: 13px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}
</style>