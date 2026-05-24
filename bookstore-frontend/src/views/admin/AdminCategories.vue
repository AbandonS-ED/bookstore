<template>
  <div class="admin-categories">
    <div class="page-header-bar">
      <h2>分类管理</h2>
      <div class="actions">
        <el-button type="primary" class="add-btn" @click="openAddDialog(null)">
          + 添加分类
        </el-button>
      </div>
    </div>

    <el-table :data="flatList" v-loading="loading" stripe class="categories-table">
      <el-table-column label="分类名称" min-width="220">
        <template #default="{ row }">
          <div class="category-name-cell" :style="{ paddingLeft: row._depth * 28 + 'px' }">
            <span class="folder-icon">📁</span>
            <span :class="{ 'parent-name': row._depth === 0 }">{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="父分类" width="150">
        <template #default="{ row }">
          <span class="parent-label">{{ row.parentId ? getParentName(row.parentId) : '—' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="书籍数" width="100" align="center">
        <template #default="{ row }">
          <span class="book-count">{{ row.bookCount ?? '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="排序" width="80" align="center">
        <template #default="{ row }">
          {{ row.sort }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag
            :type="row.status === 1 ? 'success' : 'danger'"
            size="small"
            class="status-tag"
            style="cursor: pointer"
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button v-if="row._depth === 0" size="small" type="primary" link @click="openAddDialog(row)">
            子分类
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : isSub ? '添加子分类' : '添加分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="父分类" prop="parentId">
          <el-select v-model="form.parentId" placeholder="顶级分类" clearable style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'
import { categoryApi } from '@/api/category'

const loading = ref(false)
const submitLoading = ref(false)
const categories = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const isSub = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  parentId: null,
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const getParentName = (parentId) => {
  const parent = categories.value.find(c => c.id === parentId)
  return parent?.name || '—'
}

const flatList = computed(() => {
  const result = []
  const map = {}

  categories.value.forEach(cat => {
    map[cat.id] = { ...cat, _depth: 0, children: [] }
  })

  const roots = []
  categories.value.forEach(cat => {
    const node = map[cat.id]
    if (cat.parentId && map[cat.parentId]) {
      map[cat.parentId].children.push(node)
    } else {
      roots.push(node)
    }
  })

  const flatten = (nodes, depth) => {
    nodes.forEach(node => {
      node._depth = depth
      result.push(node)
      if (node.children.length) {
        flatten(node.children, depth + 1)
      }
    })
  }

  flatten(roots, 0)
  return result
})

const loadCategories = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getList()
    categories.value = res.data || []
  } catch (error) {
    console.error('Failed to load categories:', error)
  } finally {
    loading.value = false
  }
}

const openAddDialog = (parent) => {
  isEdit.value = false
  isSub.value = !!parent
  Object.assign(form, {
    id: null,
    name: '',
    parentId: parent?.id || null,
    sort: 0
  })
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  isSub.value = false
  Object.assign(form, {
    id: row.id,
    name: row.name,
    parentId: row.parentId,
    sort: row.sort
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
        await adminApi.updateCategory(form)
        ElMessage.success('更新成功')
      } else {
        await adminApi.addCategory(form)
        ElMessage.success('添加成功')
      }
      dialogVisible.value = false
      loadCategories()
    } catch (error) {
      ElMessage.error(error.message || '操作失败')
    } finally {
      submitLoading.value = false
    }
  })
}

const toggleStatus = async (row) => {
  try {
    const newStatus = row.status === 1 ? 0 : 1
    await adminApi.updateCategoryStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    loadCategories()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await adminApi.deleteCategory(id)
      ElMessage.success('删除成功')
      loadCategories()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.admin-categories {
  max-width: 1400px;
}

.page-header-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.page-header-bar h2 {
  font-family: var(--font-display);
  font-size: 1.3rem;
  font-weight: 700;
}

.actions {
  display: flex;
  gap: 10px;
}

.add-btn {
  background: var(--color-accent);
  border-color: var(--color-accent);
}

.categories-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.category-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.folder-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.parent-name {
  font-weight: 600;
}

.parent-label {
  color: var(--color-text-secondary);
  font-size: 0.85rem;
}

.book-count {
  font-family: var(--font-display);
  font-weight: 600;
  color: var(--color-text-secondary);
}

.status-tag {
  font-weight: 500;
}
</style>
