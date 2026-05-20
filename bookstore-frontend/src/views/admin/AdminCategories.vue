<template>
  <div class="admin-categories">
    <!-- 操作栏 -->
    <div class="toolbar">
      <el-button type="primary" class="add-btn" @click="openAddDialog(null)">
        + 添加分类
      </el-button>
    </div>

    <!-- 分类树形表格 -->
    <el-table
      :data="categoryTree"
      v-loading="loading"
      row-key="id"
      default-expand-all
      stripe
      class="categories-table"
    >
      <el-table-column prop="name" label="分类名称" min-width="200">
        <template #default="{ row }">
          <div class="category-name-cell">
            <span class="folder-icon">📁</span>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="parentId" label="父分类" width="150">
        <template #default="{ row }">
          {{ row.parentId ? getParentName(row.parentId) : '无' }}
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="100" align="center" />
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" link @click="openAddDialog(row)">
            添加子分类
          </el-button>
          <el-button size="small" type="primary" link @click="openEditDialog(row)">
            编辑
          </el-button>
          <el-button size="small" type="danger" link @click="handleDelete(row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="form.parentId" placeholder="请选择父分类（可选）" clearable style="width: 100%">
            <el-option
              v-for="cat in flatCategories"
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

const loading = ref(false)
const submitLoading = ref(false)
const categories = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
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

// Flatten categories for select options (excluding current when editing)
const flatCategories = computed(() => {
  return categories.value
})

// Build tree structure from flat list
const categoryTree = computed(() => {
  const tree = []
  const map = {}

  // First pass: create map
  categories.value.forEach(cat => {
    map[cat.id] = { ...cat, children: [] }
  })

  // Second pass: build tree
  categories.value.forEach(cat => {
    const node = map[cat.id]
    if (cat.parentId && map[cat.parentId]) {
      map[cat.parentId].children.push(node)
    } else {
      tree.push(node)
    }
  })

  return tree
})

const getParentName = (parentId) => {
  const parent = categories.value.find(c => c.id === parentId)
  return parent?.name || '无'
}

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

.toolbar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom: var(--space-6);
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
  gap: var(--space-2);
}

.folder-icon {
  font-size: 18px;
}
</style>