<template>
  <div class="order-detail-page">
    <div class="page-container">
      <h1 class="page-title">订单详情</h1>

      <!-- 加载状态 -->
      <div v-if="loading" class="detail-loading">
        <div class="skeleton-section">
          <div class="skeleton title"></div>
          <div class="skeleton content"></div>
        </div>
      </div>

      <div v-else-if="order" class="detail-content">
        <!-- 订单状态进度 -->
        <div class="section status-section">
          <div class="status-progress">
            <div
              v-for="(step, index) in statusSteps"
              :key="step.key"
              class="progress-step"
              :class="{
                active: isStepActive(step.key),
                completed: isStepCompleted(step.key)
              }"
            >
              <div class="step-indicator">
                <span v-if="isStepCompleted(step.key)" class="check-icon">✓</span>
                <span v-else class="step-number">{{ index + 1 }}</span>
              </div>
              <div class="step-label">{{ step.label }}</div>
              <div v-if="step.time" class="step-time">{{ step.time }}</div>
            </div>
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="section address-section">
          <h2 class="section-title">收货地址</h2>
          <div class="address-card">
            <div class="address-header">
              <span class="receiver">{{ order.receiverName }}</span>
              <span class="phone">{{ order.receiverPhone }}</span>
            </div>
            <p class="address-detail">{{ order.receiverAddress }}</p>
          </div>
        </div>

        <!-- 订单商品 -->
        <div class="section items-section">
          <h2 class="section-title">订单商品</h2>
          <div class="order-items">
            <div
              v-for="item in order.items"
              :key="item.id"
              class="order-item"
            >
              <div class="item-cover">
                <img :src="item.coverUrl || '/placeholder-book.png'" :alt="item.bookTitle" />
              </div>
              <div class="item-info">
                <h4 class="item-title">{{ item.bookTitle }}</h4>
                <p class="item-author">{{ item.bookAuthor }}</p>
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
              <span>订单编号</span>
              <span class="order-no">{{ order.orderNo }}</span>
            </div>
            <div class="summary-row">
              <span>下单时间</span>
              <span>{{ formatDateTime(order.createTime) }}</span>
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

        <!-- 订单操作 -->
        <div class="section actions-section">
          <div class="actions-content">
            <div class="actions-left">
              <span v-if="order.status === 'created' || order.status === 'pending' || order.status === 'paying'" class="pay-hint">
                请在 <strong>{{ order.expireTime }}</strong> 前完成支付，逾期订单将自动取消
              </span>
            </div>
            <div class="actions-right">
              <button
                v-if="order.status === 'created' || order.status === 'pending'"
                class="btn-pay"
                @click="showPaymentDialog = true"
              >
                立即支付
              </button>
              <button
                v-if="order.status === 'paying'"
                class="btn-pay"
                @click="handlePay"
              >
                继续支付
              </button>
              <button
                v-if="order.status === 'created' || order.status === 'pending' || order.status === 'paying'"
                class="btn-cancel"
                @click="handleCancel"
              >
                取消订单
              </button>
              <button
                v-if="order.status === 'shipped'"
                class="btn-logistics"
                @click="showLogistics = true"
              >
                查看物流
              </button>
              <button
                v-if="order.status === 'shipped'"
                class="btn-confirm"
                @click="handleConfirm"
              >
                确认收货
              </button>
              <router-link
                v-if="order.status === 'delivered' || order.status === 'completed'"
                to="/user/orders"
                class="btn-back"
              >
                返回订单列表
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- 支付方式选择弹窗 -->
      <el-dialog v-model="showPaymentDialog" title="选择支付方式" width="400px">
        <div class="payment-methods">
          <div
            v-for="method in paymentMethods"
            :key="method.value"
            class="payment-method"
            :class="{ selected: selectedPaymentMethod === method.value }"
            @click="selectedPaymentMethod = method.value"
          >
            <div class="method-icon">{{ method.icon }}</div>
            <div class="method-info">
              <div class="method-name">{{ method.name }}</div>
              <div class="method-desc">{{ method.desc }}</div>
            </div>
            <div class="method-check" v-if="selectedPaymentMethod === method.value">✓</div>
          </div>
        </div>
        <template #footer>
          <div class="payment-footer">
            <div class="payment-amount">支付金额：<span>¥{{ Number(order?.totalAmount).toFixed(2) }}</span></div>
            <div class="payment-actions">
              <el-button @click="showPaymentDialog = false">取消</el-button>
              <el-button type="primary" @click="handlePayApply" :loading="paying">确认支付</el-button>
            </div>
          </div>
        </template>
      </el-dialog>

      <!-- 物流弹窗 -->
      <el-dialog v-model="showLogistics" title="物流信息" width="500px">
        <div class="logistics-content">
          <div v-if="logistics.length" class="logistics-timeline">
            <div
              v-for="(log, index) in logistics"
              :key="index"
              class="logistics-item"
              :class="{ latest: index === 0 }"
            >
              <div class="log-time">{{ log.time }}</div>
              <div class="log-detail">{{ log.detail }}</div>
            </div>
          </div>
          <div v-else-if="order?.expressNo" class="express-info">
            <p>物流单号：{{ order.expressNo }}</p>
            <p class="express-tip">物流信息正在更新中，请稍后查看</p>
          </div>
          <div v-else class="no-logistics">
            <p>暂无物流信息</p>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useOrderStore } from '@/stores/order'
import { formatDateTime, formatOrderStatus } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const loading = ref(true)
const order = ref(null)
const showLogistics = ref(false)
const logistics = ref([])
const showPaymentDialog = ref(false)
const selectedPaymentMethod = ref('alipay')
const paying = ref(false)

const paymentMethods = [
  { value: 'alipay', name: '支付宝', icon: '💙', desc: '推荐使用支付宝支付' },
  { value: 'wechat', name: '微信支付', icon: '🟢', desc: '推荐使用微信支付' },
  { value: 'card', name: '银行卡', icon: '💳', desc: '支持各大银行卡' }
]

const statusSteps = computed(() => [
  { key: 'created', label: '提交订单', time: order.value?.createTime },
  { key: 'paying', label: '支付中', time: null },
  { key: 'paid', label: '支付成功', time: order.value?.payTime },
  { key: 'shipped', label: '商品发货', time: null },
  { key: 'delivered', label: '确认收货', time: null }
])

const normalizeStatus = (s) => s === 'pending' ? 'created' : s

const statusOrder = ['created', 'paying', 'paid', 'shipped', 'delivered', 'completed', 'cancelled']

const isStepActive = (stepKey) => {
  const status = normalizeStatus(order.value?.status)
  if (status === 'cancelled') {
    return stepKey === 'created'
  }
  if (status === 'expired') {
    return stepKey === 'created'
  }
  const currentIndex = statusOrder.indexOf(status)
  const stepIndex = statusOrder.indexOf(stepKey)
  return stepIndex === currentIndex
}

const isStepCompleted = (stepKey) => {
  const status = normalizeStatus(order.value?.status)
  if (status === 'cancelled' || status === 'expired') {
    return false
  }
  const currentIndex = statusOrder.indexOf(status)
  const stepIndex = statusOrder.indexOf(stepKey)
  return stepIndex < currentIndex
}

const subtotal = computed(() => {
  if (!order.value?.items) return 0
  return order.value.items.reduce((sum, item) => {
    return sum + parseFloat(item.price) * item.quantity
  }, 0)
})

const shipping = computed(() => {
  return subtotal.value >= 99 ? 0 : 10
})

const total = computed(() => {
  return subtotal.value + shipping.value
})

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const res = await orderStore.getOrderDetail(route.params.id)
    order.value = res || orderStore.currentOrder
  } catch (error) {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await orderStore.payOrder(order.value.id)
    ElMessage.success('支付成功')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  }
}

const handlePayApply = async () => {
  if (!selectedPaymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  paying.value = true
  try {
    // 1. 调用 pay-apply 创建支付记录，订单状态变为 paying
    await orderStore.payApplyOrder(order.value.id, selectedPaymentMethod.value)
    showPaymentDialog.value = false
    // 2. 模拟支付回调，直接将订单设为已支付（实际应该调用支付网关）
    await orderStore.payOrder(order.value.id)
    ElMessage.success('支付成功')
    fetchOrderDetail()
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败')
  } finally {
    paying.value = false
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '保留',
      type: 'warning'
    })
    await orderStore.cancelOrder(order.value.id)
    ElMessage.success('订单已取消')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    await orderStore.confirmOrder(order.value.id)
    ElMessage.success('已确认收货')
    fetchOrderDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败')
    }
  }
}

onMounted(async () => {
  await fetchOrderDetail()
  if (route.query.pay === '1') {
    showPaymentDialog.value = true
  }
})
</script>

<style scoped>
.order-detail-page {
  padding: var(--space-8) 0 var(--space-16);
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg);
}

.page-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.page-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-text);
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-4);
  border-bottom: 2px solid var(--color-accent);
}

.detail-loading {
  padding: var(--space-8) 0;
}

.skeleton-section {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-bg-cream) 25%, var(--color-bg-card) 50%, var(--color-bg-cream) 75%);
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
  height: 200px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.section {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  margin-bottom: var(--space-6);
}

.section-title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-divider);
}

/* Status Progress */
.status-section {
  background: linear-gradient(135deg, var(--color-text) 0%, var(--color-primary-abyss) 100%);
  color: white;
}

.status-progress {
  display: flex;
  justify-content: space-between;
  position: relative;
}

.status-progress::before {
  content: '';
  position: absolute;
  top: 18px;
  left: 40px;
  right: 40px;
  height: 2px;
  background: rgba(255, 255, 255, 0.3);
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  z-index: 1;
  flex: 1;
}

.step-indicator {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-2);
  transition: all var(--transition-base);
}

.progress-step.completed .step-indicator {
  background: var(--color-primary);
}

.progress-step.active .step-indicator {
  background: var(--color-accent);
  box-shadow: 0 0 0 4px rgba(192, 154, 75, 0.3);
}

.check-icon {
  font-size: 16px;
  font-weight: bold;
}

.step-number {
  font-size: var(--text-sm);
  font-weight: 600;
}

.step-label {
  font-size: var(--text-sm);
}

.step-time {
  font-size: var(--text-xs);
  opacity: 0.7;
  margin-top: var(--space-1);
}

/* Address */
.address-card {
  padding: var(--space-4);
  background: var(--color-bg);
  border-radius: var(--radius-md);
}

.address-header {
  display: flex;
  gap: var(--space-4);
  margin-bottom: var(--space-2);
}

.receiver {
  font-weight: 600;
}

.phone {
  color: var(--color-text-secondary);
  font-size: var(--text-sm);
}

.address-detail {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

/* Order Items */
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
  background: var(--color-bg);
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
  font-family: var(--font-display);
  color: var(--color-accent-muted);
  min-width: 70px;
}

.item-quantity {
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  min-width: 40px;
  text-align: center;
}

.item-subtotal {
  font-family: var(--font-display);
  font-weight: 600;
  color: var(--color-accent);
  min-width: 80px;
  text-align: right;
}

/* Summary */
.summary-content {
  max-width: 400px;
  margin-left: auto;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.summary-row .order-no {
  font-family: var(--font-display);
  color: var(--color-text);
}

.summary-row.total {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-divider);
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--color-text);
}

.total-amount {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  color: var(--color-accent);
}

/* Actions */
.actions-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pay-hint {
  font-size: var(--text-sm);
  color: var(--color-accent);
}

.pay-hint strong {
  font-weight: 600;
}

.actions-right {
  display: flex;
  gap: var(--space-3);
}

.btn-pay, .btn-cancel, .btn-logistics, .btn-confirm, .btn-back {
  padding: var(--space-3) var(--space-6);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
  text-decoration: none;
  display: inline-block;
}

.btn-pay {
  background: var(--color-accent);
  border: none;
  color: white;
}

.btn-pay:hover {
  background: var(--color-vermillion-light);
}

.btn-cancel {
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  color: var(--color-text-secondary);
}

.btn-cancel:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.btn-logistics {
  background: var(--color-primary);
  border: none;
  color: white;
}

.btn-logistics:hover {
  background: var(--color-bamboo-light);
}

.btn-confirm {
  background: var(--color-accent-muted);
  border: none;
  color: white;
}

.btn-confirm:hover {
  background: var(--color-copper-light);
}

.btn-back {
  background: var(--color-bg-cream);
  border: 1px solid var(--color-divider);
  color: var(--color-text-secondary);
}

.btn-back:hover {
  border-color: var(--color-accent-muted);
  color: var(--color-accent-muted);
}

/* Payment Methods */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.payment-method {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.payment-method:hover {
  border-color: var(--color-vermillion);
}

.payment-method.selected {
  border-color: var(--color-vermillion);
  background: rgba(201, 64, 67, 0.05);
}

.method-icon {
  font-size: 32px;
}

.method-info {
  flex: 1;
}

.method-name {
  font-weight: 600;
  margin-bottom: var(--space-1);
}

.method-desc {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.method-check {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--color-vermillion);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.payment-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.payment-amount {
  font-size: var(--text-base);
  color: var(--color-text-secondary);
}

.payment-amount span {
  color: var(--color-vermillion);
  font-weight: 600;
  font-size: var(--text-lg);
}

.payment-actions {
  display: flex;
  gap: var(--space-3);
}

/* Logistics */
.logistics-content {
  padding: var(--space-4);
}

.logistics-timeline {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.logistics-item {
  display: flex;
  gap: var(--space-4);
  padding-left: var(--space-4);
  border-left: 2px solid var(--color-divider);
}

.logistics-item.latest {
  border-left-color: var(--color-accent);
}

.log-time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  white-space: nowrap;
}

.log-detail {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.logistics-item.latest .log-detail {
  color: var(--color-text);
  font-weight: 500;
}

.no-logistics {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-text-muted);
}

.express-info {
  text-align: center;
  padding: var(--space-6);
}

.express-info p {
  margin-bottom: var(--space-2);
}

.express-tip {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

@media (max-width: 768px) {
  .status-progress {
    flex-wrap: wrap;
    gap: var(--space-4);
  }

  .status-progress::before {
    display: none;
  }

  .progress-step {
    flex: 0 0 45%;
  }

  .order-item {
    flex-wrap: wrap;
  }

  .item-price,
  .item-quantity {
    margin-left: 76px;
  }

  .item-subtotal {
    width: 100%;
    text-align: left;
    padding-left: 76px;
  }

  .summary-content {
    max-width: 100%;
  }

  .actions-content {
    flex-direction: column;
    gap: var(--space-4);
  }

  .actions-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .actions-right button,
  .actions-right a {
    flex: 1;
    min-width: 100px;
    text-align: center;
  }
}
</style>