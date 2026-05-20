<template>
  <div class="addresses-page">
    <div class="page-container">
      <div class="page-header">
        <h1>收货地址</h1>
        <button @click="openAddressDialog()" class="btn-add">新增地址</button>
      </div>

      <div v-if="loading" class="loading-state">
        <div v-for="i in 2" :key="i" class="address-skeleton">
          <div class="skeleton title"></div>
          <div class="skeleton text"></div>
          <div class="skeleton text short"></div>
        </div>
      </div>

      <div v-else-if="addresses.length" class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          :class="['address-item', { default: addr.isDefault }]"
        >
          <div class="address-main" @click="setDefault(addr.id)">
            <div class="address-info">
              <span class="receiver">{{ addr.receiverName }}</span>
              <span class="phone">{{ addr.phone }}</span>
              <span v-if="addr.isDefault" class="default-tag">默认</span>
            </div>
            <p class="address-detail">{{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detailAddress }}</p>
          </div>
          <div class="address-actions">
            <button @click="openAddressDialog(addr)">编辑</button>
            <button @click="handleDelete(addr.id)" class="btn-delete">删除</button>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">📍</span>
        <h3>暂无地址</h3>
        <p>添加收货地址方便购物</p>
      </div>

      <!-- Address Dialog -->
      <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑地址' : '新增地址'" width="500px">
        <el-form :model="addressForm" label-width="80px">
          <el-form-item label="收货人">
            <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="省份">
            <el-input v-model="addressForm.province" placeholder="请输入省份" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="addressForm.city" placeholder="请输入城市" />
          </el-form-item>
          <el-form-item label="区县">
            <el-input v-model="addressForm.district" placeholder="请输入区县" />
          </el-form-item>
          <el-form-item label="详细地址">
            <el-input v-model="addressForm.detailAddress" type="textarea" :rows="2" placeholder="请输入详细地址" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { addressApi } from '@/api/address'

const addresses = ref([])
const loading = ref(true)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)

const addressForm = ref({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})

const fetchAddresses = async () => {
  loading.value = true
  try {
    const res = await addressApi.getList()
    addresses.value = res.data || []
  } catch (error) {
    console.error('Failed to fetch addresses:', error)
  } finally {
    loading.value = false
  }
}

const openAddressDialog = (addr = null) => {
  if (addr) {
    isEdit.value = true
    editingId.value = addr.id
    addressForm.value = {
      receiverName: addr.receiverName || '',
      phone: addr.phone || '',
      province: addr.province || '',
      city: addr.city || '',
      district: addr.district || '',
      detailAddress: addr.detailAddress || ''
    }
  } else {
    isEdit.value = false
    editingId.value = null
    addressForm.value = {
      receiverName: '',
      phone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: ''
    }
  }
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (isEdit.value) {
      await addressApi.update({ id: editingId.value, ...addressForm.value })
      ElMessage.success('更新成功')
    } else {
      await addressApi.add(addressForm.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchAddresses()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await addressApi.delete(id)
    ElMessage.success('删除成功')
    addresses.value = addresses.value.filter(a => a.id !== id)
  } catch {
    ElMessage.error('删除失败')
  }
}

const setDefault = async (id) => {
  try {
    await addressApi.setDefault(id)
    ElMessage.success('已设为默认地址')
    fetchAddresses()
  } catch (error) {
    ElMessage.error('设置失败')
  }
}

onMounted(() => {
  fetchAddresses()
})
</script>

<style scoped>
.addresses-page {
  padding: var(--space-8) 0 var(--space-16);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-8);
}

.page-header h1 {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-text);
}

.btn-add {
  padding: var(--space-2) var(--space-4);
  background: var(--color-accent);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  cursor: pointer;
}

.btn-add:hover {
  background: var(--color-vermillion-light);
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.address-item {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  border: 1px solid var(--color-divider);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.address-item:hover {
  border-color: var(--color-accent);
}

.address-item.default {
  border-color: var(--color-accent);
  background: rgba(192, 154, 75, 0.03);
}

.address-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.receiver {
  font-weight: 500;
}

.phone {
  color: var(--color-text-secondary);
}

.default-tag {
  font-size: var(--text-xs);
  color: var(--color-accent);
  background: rgba(192, 154, 75, 0.1);
  padding: 2px 8px;
  border-radius: var(--radius-full);
}

.address-detail {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.address-actions {
  display: flex;
  gap: var(--space-3);
}

.address-actions button {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  background: none;
  border: none;
  cursor: pointer;
}

.address-actions button:hover {
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

.address-skeleton {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-bg-cream) 25%, var(--color-bg-card) 50%, var(--color-bg-cream) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-sm);
}

.skeleton.title {
  height: 20px;
  width: 30%;
}

.skeleton.text {
  height: 16px;
  width: 80%;
}

.skeleton.text.short {
  width: 50%;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}
</style>