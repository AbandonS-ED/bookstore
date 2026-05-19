<template>
  <div class="order-confirm-page">
    <div class="page-container">
      <h1 class="page-title">确认订单</h1>

      <!-- 加载状态 -->
      <div v-if="loading" class="order-loading">
        <div class="skeleton-section">
          <div class="skeleton title"></div>
          <div class="skeleton content"></div>
        </div>
      </div>

      <div v-else class="order-content">
        <!-- 收货地址 -->
        <div class="section address-section">
          <h2 class="section-title">收货地址</h2>
          <div v-if="addresses.length" class="address-list">
            <div
              v-for="addr in addresses"
              :key="addr.id"
              class="address-item"
              :class="{ selected: selectedAddressId === addr.id }"
              @click="selectedAddressId = addr.id"
            >
              <div class="address-radio">
                <input
                  type="radio"
                  :value="addr.id"
                  v-model="selectedAddressId"
                  :id="'addr-' + addr.id"
                />
                <label :for="'addr-' + addr.id"></label>
              </div>
              <div class="address-info">
                <div class="address-header">
                  <span class="receiver">{{ addr.receiver }}</span>
                  <span class="phone">{{ addr.phone }}</span>
                  <span v-if="addr.isDefault" class="default-tag">默认</span>
                </div>
                <p class="address-detail">{{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detail }}</p>
              </div>
            </div>
          </div>
          <div v-else class="no-address">
            <p>暂无收货地址</p>
            <button class="btn-add-address" @click="openAddressDialog()">添加地址</button>
          </div>
        </div>

        <!-- 订单商品 -->
        <div class="section items-section">
          <h2 class="section-title">订单商品</h2>
          <div class="order-items">
            <div
              v-for="item in orderItems"
              :key="item.id || item.bookId"
              class="order-item"
            >
              <div class="item-cover">
                <img :src="item.coverUrl || '/placeholder-book.png'" :alt="item.title" />
              </div>
              <div class="item-info">
                <h4 class="item-title">{{ item.title }}</h4>
                <p class="item-author">{{ item.author }}</p>
              </div>
              <div class="item-price">¥{{ item.price }}</div>
              <div class="item-quantity">x{{ item.quantity }}</div>
              <div class="item-subtotal">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
          </div>
        </div>

        <!-- 订单摘要 -->
        <div class="section summary-section">
          <h2 class="section-title">订单摘要</h2>
          <div class="summary-content">
            <div class="summary-row">
              <span>商品总数</span>
              <span>{{ totalCount }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品金额</span>
              <span>¥{{ subtotal.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span>运费</span>
              <span>¥{{ shipping.toFixed(2) }}</span>
            </div>
            <div class="summary-row total">
              <span>合计</span>
              <span class="total-amount">¥{{ total.toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <!-- 提交按钮 -->
        <div class="submit-section">
          <button class="btn-submit" @click="handleSubmit" :disabled="submitting || !selectedAddressId">
            {{ submitting ? '提交中...' : '提交订单' }}
          </button>
        </div>

        <!-- 添加地址对话框 -->
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
            <el-button type="primary" @click="handleSaveAddress">保存</el-button>
          </template>
        </el-dialog>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api/order'
import { cartApi } from '@/api/cart'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const loading = ref(true)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const showAddressDialog = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const addressForm = ref({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: ''
})
const orderItems = ref([])

const subtotal = computed(() => {
  return orderItems.value.reduce((sum, item) => {
    return sum + parseFloat(item.price) * item.quantity
  }, 0)
})

const shipping = computed(() => {
  return subtotal.value >= 99 ? 0 : 10
})

const total = computed(() => {
  return subtotal.value + shipping.value
})

const totalCount = computed(() => {
  return orderItems.value.reduce((sum, item) => sum + item.quantity, 0)
})

const fetchAddresses = async () => {
  try {
    const res = await fetch('/api/address/list')
    const data = await res.json()
    addresses.value = data.data || []
    const defaultAddr = addresses.value.find(a => a.isDefault === 1)
    if (defaultAddr) {
      selectedAddressId.value = defaultAddr.id
    } else if (addresses.value.length) {
      selectedAddressId.value = addresses.value[0].id
    }
  } catch (error) {
    console.error('Failed to fetch addresses')
  }
}

const openAddressDialog = (addr = null) => {
  if (addr) {
    isEdit.value = true
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

const handleSaveAddress = async () => {
  try {
    const token = localStorage.getItem('token')
    const res = await fetch('/api/address/add', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(addressForm.value)
    })
    const data = await res.json()
    if (data.code === 200) {
      ElMessage.success('添加成功')
      dialogVisible.value = false
      fetchAddresses()
    } else {
      ElMessage.error(data.message || '添加失败')
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const initOrderItems = async () => {
  const { bookId, quantity } = route.query

  if (bookId) {
    // Direct purchase
    orderItems.value = [{
      bookId: parseInt(bookId),
      title: '书籍',
      author: '',
      price: 0,
      quantity: parseInt(quantity) || 1,
      coverUrl: ''
    }]
    // Fetch book details
    try {
      const res = await fetch(`/api/book/${bookId}`)
      const data = await res.json()
      if (data.data) {
        const book = data.data
        orderItems.value = [{
          bookId: book.id,
          title: book.title,
          author: book.author,
          price: book.price,
          quantity: parseInt(quantity) || 1,
          coverUrl: book.coverUrl
        }]
      }
    } catch (error) {
      console.error('Failed to fetch book details')
    }
  } else {
    // From cart
    orderItems.value = cartStore.items.map(item => ({
      bookId: item.bookId,
      title: item.title,
      author: item.author,
      price: item.price,
      quantity: item.quantity,
      coverUrl: item.coverUrl
    }))
  }
}

const handleSubmit = async () => {
  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddressId.value,
      items: orderItems.value.map(item => ({
        bookId: item.bookId,
        quantity: item.quantity
      }))
    }

    const res = await orderApi.create(orderData)
    const orderId = res.data?.id || res.data?.orderId

    // Clear cart if from cart
    if (!route.query.bookId) {
      await cartStore.clearCart()
    }

    ElMessage.success('订单提交成功')
    router.push(`/order/success?orderId=${orderId}`)
  } catch (error) {
    ElMessage.error('订单提交失败')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([initOrderItems(), fetchAddresses()])
  loading.value = false
})
</script>

<style scoped>
.order-confirm-page {
  padding: var(--space-8) 0 var(--space-16);
  min-height: calc(100vh - var(--header-height));
  background: var(--color-paper);
}

.page-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.page-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-ink);
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-4);
  border-bottom: 2px solid var(--color-vermillion);
}

.order-loading {
  padding: var(--space-8) 0;
}

.skeleton-section {
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-paper-dark) 25%, var(--color-paper-white) 50%, var(--color-paper-dark) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-md);
}

.skeleton.title {
  height: 24px;
  width: 120px;
  margin-bottom: var(--space-4);
}

.skeleton.content {
  height: 100px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.section {
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border-light);
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.address-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.address-item:hover {
  border-color: var(--color-copper);
}

.address-item.selected {
  border-color: var(--color-vermillion);
  background: rgba(201, 64, 67, 0.05);
}

.address-radio {
  padding-top: var(--space-1);
}

.address-radio input {
  display: none;
}

.address-radio label {
  display: block;
  width: 18px;
  height: 18px;
  border: 2px solid var(--color-border);
  border-radius: 50%;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.address-item.selected .address-radio label {
  border-color: var(--color-vermillion);
  background: var(--color-vermillion);
}

.address-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-2);
}

.receiver {
  font-weight: 600;
}

.phone {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
}

.default-tag {
  padding: 2px 8px;
  background: var(--color-copper);
  color: white;
  font-size: var(--text-xs);
  border-radius: var(--radius-full);
}

.address-detail {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.no-address {
  text-align: center;
  padding: var(--space-6);
  color: var(--color-text-muted);
}

.btn-add-address {
  margin-top: var(--space-3);
  padding: var(--space-2) var(--space-4);
  background: var(--color-paper-dark);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-add-address:hover {
  border-color: var(--color-vermillion);
  color: var(--color-vermillion);
}

.order-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.order-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-3);
  background: var(--color-paper);
  border-radius: var(--radius-md);
}

.item-cover {
  width: 60px;
  height: 80px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-family: var(--font-display);
  font-size: var(--text-sm);
  font-weight: 600;
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-author {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.item-price {
  font-family: var(--font-accent);
  color: var(--color-copper);
}

.item-quantity {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
}

.item-subtotal {
  font-family: var(--font-accent);
  font-weight: 600;
  color: var(--color-vermillion);
  min-width: 80px;
  text-align: right;
}

.summary-content {
  max-width: 300px;
  margin-left: auto;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.summary-row.total {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--color-ink);
}

.total-amount {
  font-family: var(--font-accent);
  font-size: var(--text-xl);
  color: var(--color-vermillion);
}

.submit-section {
  display: flex;
  justify-content: flex-end;
}

.btn-submit {
  padding: var(--space-4) var(--space-10);
  background: var(--color-vermillion);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: 600;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.btn-submit:hover:not(:disabled) {
  background: var(--color-vermillion-light);
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .order-item {
    flex-wrap: wrap;
  }

  .item-price,
  .item-quantity {
    margin-left: auto;
  }

  .item-subtotal {
    width: 100%;
    text-align: left;
    padding-left: 64px;
  }
}
</style>