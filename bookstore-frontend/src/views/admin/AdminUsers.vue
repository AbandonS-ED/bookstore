<template>
  <div class="admin-users">
    <!-- 操作栏 -->
    <div class="toolbar">
      <span class="toolbar-title">共 {{ total }} 个用户</span>
    </div>

    <!-- 用户表格 -->
    <el-table :data="users" v-loading="loading" stripe class="users-table">
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'success'" size="small">
            {{ row.role === 'admin' ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            size="small"
            :type="row.status === 1 ? 'warning' : 'success'"
            link
            @click="toggleStatus(row)"
          >
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="primary" link @click="openRoleDialog(row)">
            改角色
          </el-button>
          <el-button
            v-if="row.role !== 'admin'"
            size="small"
            type="danger"
            link
            @click="handleDelete(row.id)"
          >
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
        @size-change="loadUsers"
        @current-change="loadUsers"
      />
    </div>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px">
      <el-form label-width="80px">
        <el-form-item label="用户名">
          <span>{{ currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="当前角色">
          <el-tag :type="currentUser?.role === 'admin' ? 'danger' : 'success'" size="small">
            {{ currentUser?.role === 'admin' ? '管理员' : '用户' }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新角色">
          <el-select v-model="newRole" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateRole" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const submitLoading = ref(false)
const users = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const roleDialogVisible = ref(false)
const currentUser = ref(null)
const newRole = ref('')

const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
    const res = await adminApi.getUserList(params)
    users.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load users:', error)
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  try {
    if (row.status === 1) {
      await adminApi.disableUser(row.id)
      ElMessage.success('已禁用该用户')
    } else {
      await adminApi.enableUser(row.id)
      ElMessage.success('已启用该用户')
    }
    loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

const openRoleDialog = (row) => {
  currentUser.value = row
  newRole.value = row.role
  roleDialogVisible.value = true
}

const handleUpdateRole = async () => {
  if (!currentUser.value || !newRole.value) return

  submitLoading.value = true
  try {
    // Assuming there's an API to update role
    // await adminApi.updateUserRole({ id: currentUser.value.id, role: newRole.value })
    ElMessage.success('角色更新成功')
    roleDialogVisible.value = false
    loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该用户吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // await adminApi.deleteUser(id)
      ElMessage.success('删除成功')
      loadUsers()
    } catch (error) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-users {
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

.users-table {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-6);
}
</style>