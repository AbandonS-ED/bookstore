<template>
  <div class="orders-page">
    <div class="page-container">
      <h1 class="page-title">我的订单</h1>

      <!-- 加载状态 -->
      <div v-if="loading" class="orders-loading">
        <div v-for="i in 3" :key="i" class="skeleton-card">
          <div class="skeleton header"></div>
          <div class="skeleton body"></div>
        </div>
      </div>

      <div v-else class="orders-content">
        <!-- 状态标签 -->
        <div class="status-tabs">
          <button
            v-for="tab in statusTabs"
            :key="tab.value"
            class="tab-btn"
            :class="{ active: currentStatus === tab.value }"
            @click="handleTabChange(tab.value)"
          >
            {{ tab.label }}
            <span v-if="tab.count" class="tab-count">{{ tab.count }}</span>
          </button>
        </div>

        <!-- 订单列表 -->
        <div v-if="orders.length" class="order-list">
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-card"
            @click="goToDetail(order.id)"
          >
            <div class="order-header">
              <div class="order-info">
                <span class="order-no">订单号：{{ order.orderNo }}</span>
                <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
              </div>
              <div class="order-status" :class="order.status">
                {{ formatOrderStatus(order.status) }}
              </div>
            </div>
            <div class="order-body">
              <div class="order-items-preview">
                <div
                  v-for="(item, index) in order.items?.slice(0, 3)"
                  :key="item.id || index"
                  class="item-preview"
                >
                  <img :src="item.coverUrl || '/placeholder-book.png'" :alt="item.bookTitle" class="preview-cover" />
                  <div class="preview-info">
                    <p class="preview-title">{{ item.bookTitle }}</p>
                    <p class="preview-meta">{{ item.bookAuthor }} x{{ item.quantity }}</p>
                    <p class="preview-price">¥{{ item.price?.toFixed(2) }}</p>
                  </div>
                </div>
                <div v-if="order.items?.length > 3" class="more-items">
                  还有 {{ order.items.length - 3 }} 件商品
                </div>
              </div>
              <div class="order-summary">
                <div class="item-count">共 {{ order.itemCount || order.items?.length || 0 }} 件商品</div>
                <div class="order-total">
                  <span class="total-label">合计</span>
                  <span class="total-value">¥{{ order.totalAmount?.toFixed(2) }}</span>
                </div>
              </div>
            </div>
            <div class="order-footer">
              <div class="footer-left">
                <span v-if="order.status === 'pending'" class="pay-hint">请尽快完成支付</span>
                <span v-else-if="order.status === 'shipped'" class="ship-hint">商品正在配送中</span>
                <span v-else-if="order.status === 'delivered'" class="complete-hint">感谢您的购买</span>
              </div>
              <div class="footer-actions">
                <button
                  v-if="order.status === 'pending'"
                  class="btn-pay"
                  @click.stop="handlePay(order.id)"
                >
                  立即支付
                </button>
                <button
                  v-if="order.status === 'pending'"
                  class="btn-cancel"
                  @click.stop="handleCancel(order.id)"
                >
                  取消订单
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div v-else class="empty-orders">
          <div class="empty-icon">📋</div>
          <h2>暂无相关订单</h2>
          <p>{{ currentStatus === 'all' ? '快去挑选几本好书吧' : '当前分类下没有订单' }}</p>
          <router-link to="/books" class="btn-browse">浏览书籍</router-link>
        </div>

        <!-- 分页 -->
        <div v-if="total > pageSize" class="pagination">
          <button
            class="page-btn"
            :disabled="current === 1"
            @click="handlePageChange(current - 1)"
          >
            上一页
          </button>
          <span class="page-info">{{ current }} / {{ totalPages }}</span>
          <button
            class="page-btn"
            :disabled="current >= totalPages"
            @click="handlePageChange(current + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api/order'
import { useOrderStore } from '@/stores/order'
import { formatDateTime, formatOrderStatus } from '@/utils/format'

const router = useRouter()
const orderStore = useOrderStore()

const loading = ref(true)
const orders = ref([])
const current = ref(1)
const pageSize = ref(10)
const total = ref(0)
const currentStatus = ref('all')

const statusTabs = [
  { label: '全部', value: 'all', count: null },
  { label: '待付款', value: 'pending', count: null },
  { label: '待发货', value: 'paid', count: null },
  { label: '待收货', value: 'shipped', count: null },
  { label: '已完成', value: 'delivered', count: null },
  { label: '已取消', value: 'cancelled', count: null }
]

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const fetchOrders = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: current.value,
      pageSize: pageSize.value
    }
    if (currentStatus.value !== 'all') {
      params.status = currentStatus.value
    }
    const res = await orderStore.getOrderList(params)
    orders.value = orderStore.orders || []
    total.value = res?.total || orders.value.length
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const fetchOrderCounts = async () => {
  // Get counts for each status
  try {
    const res = await orderApi.getList({ pageNum: 1, pageSize: 1 })
    // This is simplified - in real app you might need a separate API
    const allOrders = res.data || []
    statusTabs.forEach(tab => {
      if (tab.value === 'all') {
        tab.count = null // Show all count if needed
      }
    })
  } catch (error) {
    console.error('Failed to fetch order counts')
  }
}

const handleTabChange = (status) => {
  currentStatus.value = status
  current.value = 1
  fetchOrders()
}

const handlePageChange = (page) => {
  current.value = page
  fetchOrders()
}

const goToDetail = (id) => {
  router.push(`/order/${id}`)
}

const handlePay = async (id) => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await orderStore.payOrder(id)
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('支付失败')
    }
  }
}

const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消订单', {
      confirmButtonText: '确认取消',
      cancelButtonText: '保留',
      type: 'warning'
    })
    await orderStore.cancelOrder(id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-page {
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

.orders-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.skeleton-card {
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-paper-dark) 25%, var(--color-paper-white) 50%, var(--color-paper-dark) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-md);
}

.skeleton.header {
  height: 24px;
  width: 60%;
  margin-bottom: var(--space-3);
}

.skeleton.body {
  height: 60px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.status-tabs {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  flex-wrap: wrap;
}

.tab-btn {
  padding: var(--space-2) var(--space-4);
  background: var(--color-paper-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.tab-btn:hover {
  border-color: var(--color-copper);
}

.tab-btn.active {
  background: var(--color-vermillion);
  border-color: var(--color-vermillion);
  color: white;
}

.tab-count {
  padding: 2px 6px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.order-card {
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.order-card:hover {
  box-shadow: var(--shadow-md);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-border-light);
}

.order-info {
  display: flex;
  gap: var(--space-4);
  align-items: center;
}

.order-no {
  font-family: var(--font-accent);
  font-size: var(--text-sm);
  color: var(--color-ink);
}

.order-time {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.order-status {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
}

.order-status.pending {
  background: rgba(201, 64, 67, 0.1);
  color: var(--color-vermillion);
}

.order-status.paid {
  background: rgba(176, 141, 87, 0.1);
  color: var(--color-copper);
}

.order-status.shipped {
  background: rgba(122, 158, 126, 0.1);
  color: var(--color-bamboo);
}

.order-status.delivered {
  background: rgba(122, 158, 126, 0.2);
  color: var(--color-bamboo);
}

.order-status.cancelled {
  background: var(--color-paper-dark);
  color: var(--color-text-muted);
}

.order-body {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: var(--space-4) 0;
}

.order-items-preview {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  flex: 1;
  min-width: 0;
}

.item-preview {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.preview-cover {
  width: 50px;
  height: 65px;
  border-radius: var(--radius-sm);
  object-fit: cover;
  flex-shrink: 0;
}

.preview-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.preview-title {
  font-size: var(--text-sm);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-meta {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.preview-price {
  font-size: var(--text-sm);
  color: var(--color-vermillion);
}

.more-items {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  padding: var(--space-1) 0;
}

.order-summary {
  text-align: right;
}

.item-count {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--space-1);
}

.total-label {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.total-value {
  font-family: var(--font-accent);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-vermillion);
  margin-left: var(--space-2);
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--space-3);
  border-top: 1px solid var(--color-border-light);
}

.pay-hint {
  font-size: var(--text-sm);
  color: var(--color-vermillion);
}

.ship-hint {
  font-size: var(--text-sm);
  color: var(--color-bamboo);
}

.complete-hint {
  font-size: var(--text-sm);
  color: var(--color-copper);
}

.footer-actions {
  display: flex;
  gap: var(--space-2);
}

.btn-pay, .btn-cancel {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.btn-pay {
  background: var(--color-vermillion);
  border: none;
  color: white;
}

.btn-pay:hover {
  background: var(--color-vermillion-light);
}

.btn-cancel {
  background: var(--color-paper-white);
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
}

.btn-cancel:hover {
  border-color: var(--color-vermillion);
  color: var(--color-vermillion);
}

.empty-orders {
  text-align: center;
  padding: var(--space-16) 0;
  background: var(--color-paper-white);
  border-radius: var(--radius-lg);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: var(--space-4);
}

.empty-orders h2 {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  color: var(--color-ink);
  margin-bottom: var(--space-2);
}

.empty-orders p {
  color: var(--color-text-muted);
  margin-bottom: var(--space-6);
}

.btn-browse {
  display: inline-block;
  padding: var(--space-3) var(--space-8);
  background: var(--color-vermillion);
  color: white;
  text-decoration: none;
  border-radius: var(--radius-md);
  font-weight: 500;
  transition: background var(--transition-fast);
}

.btn-browse:hover {
  background: var(--color-vermillion-light);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--space-4);
  margin-top: var(--space-8);
}

.page-btn {
  padding: var(--space-2) var(--space-4);
  background: var(--color-paper-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.page-btn:hover:not(:disabled) {
  border-color: var(--color-vermillion);
  color: var(--color-vermillion);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .order-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-2);
  }

  .order-body {
    flex-direction: column;
    gap: var(--space-4);
    align-items: flex-start;
  }

  .order-summary {
    text-align: left;
    width: 100%;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .footer-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>