<template>
  <div class="user-page">
    <div class="page-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="user-loading">
        <div class="skeleton-profile">
          <div class="skeleton avatar"></div>
          <div class="skeleton info"></div>
        </div>
      </div>

      <div v-else class="user-content">
        <!-- 用户信息卡片 -->
        <div class="user-profile">
          <div class="profile-avatar">
            <span class="avatar-text">{{ userStore.username?.charAt(0)?.toUpperCase() }}</span>
          </div>
          <div class="profile-info">
            <h2 class="username">{{ userStore.username }}</h2>
            <p class="member-since">加入于 {{ formatDate(userInfo?.createTime) }}</p>
            <div class="profile-tags">
              <span v-if="userInfo?.role === 'admin'" class="tag admin">管理员</span>
              <span v-else class="tag member">会员</span>
            </div>
          </div>
          <div class="profile-stats">
            <div class="stat-item">
              <span class="stat-value">{{ stats.totalOrders }}</span>
              <span class="stat-label">总订单</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.pendingOrders }}</span>
              <span class="stat-label">待付款</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ stats.reviewsCount }}</span>
              <span class="stat-label">我的评价</span>
            </div>
          </div>
        </div>

        <!-- 功能菜单 -->
        <div class="user-menu">
          <router-link to="/user/orders" class="menu-item">
            <div class="menu-icon">📋</div>
            <div class="menu-content">
              <h3>我的订单</h3>
              <p>查看所有订单</p>
            </div>
            <span class="menu-arrow">→</span>
          </router-link>

          <router-link to="/user/reviews" class="menu-item">
            <div class="menu-icon">✏️</div>
            <div class="menu-content">
              <h3>我的评价</h3>
              <p>管理我的书评</p>
            </div>
            <span class="menu-arrow">→</span>
          </router-link>

          <router-link to="/user/addresses" class="menu-item">
            <div class="menu-icon">📍</div>
            <div class="menu-content">
              <h3>收货地址</h3>
              <p>管理收货地址</p>
            </div>
            <span class="menu-arrow">→</span>
          </router-link>

          <router-link to="/user/settings" class="menu-item">
            <div class="menu-icon">⚙️</div>
            <div class="menu-content">
              <h3>账号设置</h3>
              <p>修改密码、绑定信息</p>
            </div>
            <span class="menu-arrow">→</span>
          </router-link>

          <router-link v-if="userStore.isAdmin" to="/admin" class="menu-item">
            <div class="menu-icon">🔧</div>
            <div class="menu-content">
              <h3>管理后台</h3>
              <p>书籍、订单、用户管理</p>
            </div>
            <span class="menu-arrow">→</span>
          </router-link>
        </div>

        <!-- 最近订单 -->
        <div class="recent-orders">
          <div class="section-header">
            <h2>最近订单</h2>
            <router-link to="/user/orders" class="more-link">查看全部 →</router-link>
          </div>
          <div v-if="recentOrders.length" class="order-list">
            <div
              v-for="order in recentOrders"
              :key="order.id"
              class="order-item"
              @click="goToOrderDetail(order.id)"
            >
              <div class="order-info">
                <span class="order-no">{{ order.orderNo }}</span>
                <span class="order-time">{{ formatDateTime(order.createTime) }}</span>
              </div>
              <div class="order-status" :class="order.status">
                {{ formatOrderStatus(order.status) }}
              </div>
              <div class="order-total">
                <span class="total-value">¥{{ order.totalAmount?.toFixed(2) }}</span>
                <span class="total-items">{{ order.items?.length || 0 }} 件商品</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-orders">
            <p>暂无订单</p>
            <router-link to="/books" class="btn-browse">去逛逛书店</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { orderApi } from '@/api/order'
import { formatDate, formatDateTime, formatOrderStatus } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const userInfo = ref(null)
const recentOrders = ref([])
const stats = reactive({
  totalOrders: 0,
  pendingOrders: 0,
  reviewsCount: 0
})

const fetchUserInfo = async () => {
  try {
    userInfo.value = await userStore.getUserInfo()
  } catch (error) {
    console.error('Failed to fetch user info')
  }
}

const fetchRecentOrders = async () => {
  try {
    const res = await orderApi.getList({ pageNum: 1, pageSize: 5 })
    recentOrders.value = res.data || []
    stats.totalOrders = res.data?.length || 0
  } catch (error) {
    console.error('Failed to fetch recent orders')
  }
}

const fetchStats = async () => {
  try {
    // Get pending orders count
    const pendingRes = await orderApi.getList({ status: 'pending', pageNum: 1, pageSize: 1 })
    stats.pendingOrders = pendingRes.data?.length || 0
  } catch (error) {
    console.error('Failed to fetch stats')
  }
}

const goToOrderDetail = (id) => {
  router.push(`/order/${id}`)
}

onMounted(async () => {
  loading.value = true
  await Promise.all([fetchUserInfo(), fetchRecentOrders(), fetchStats()])
  loading.value = false
})
</script>

<style scoped>
.user-page {
  padding: var(--space-8) 0 var(--space-16);
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg);
}

.page-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.user-loading {
  padding: var(--space-8) 0;
}

.skeleton-profile {
  display: flex;
  gap: var(--space-6);
  padding: var(--space-8);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-bg-cream) 25%, var(--color-bg-card) 50%, var(--color-bg-cream) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-md);
}

.skeleton.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
}

.skeleton.info {
  flex: 1;
  height: 80px;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* User Profile */
.user-profile {
  display: flex;
  align-items: center;
  gap: var(--space-6);
  padding: var(--space-8);
  background: linear-gradient(135deg, var(--color-text) 0%, var(--color-primary-dark) 100%);
  border-radius: var(--radius-lg);
  color: white;
  margin-bottom: var(--space-6);
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: var(--color-accent-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.avatar-text {
  font-family: var(--font-display);
  font-size: var(--text-4xl);
  font-weight: 700;
  color: white;
}

.profile-info {
  flex: 1;
}

.username {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  margin-bottom: var(--space-2);
}

.member-since {
  font-size: var(--text-sm);
  opacity: 0.8;
  margin-bottom: var(--space-3);
}

.profile-tags {
  display: flex;
  gap: var(--space-2);
}

.tag {
  padding: var(--space-1) var(--space-3);
  border-radius: var(--radius-full);
  font-size: var(--text-xs);
  font-weight: 500;
}

.tag.admin {
  background: var(--color-accent);
}

.tag.member {
  background: var(--color-accent-muted);
}

.profile-stats {
  display: flex;
  gap: var(--space-8);
}

.stat-item {
  text-align: center;
  padding: var(--space-4) var(--space-6);
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-lg);
}

.stat-value {
  display: block;
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  margin-bottom: var(--space-1);
}

.stat-label {
  font-size: var(--text-sm);
  opacity: 0.8;
}

/* User Menu */
.user-menu {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-6);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.menu-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.menu-icon {
  font-size: 32px;
  flex-shrink: 0;
}

.menu-content {
  flex: 1;
}

.menu-content h3 {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: var(--space-1);
}

.menu-content p {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.menu-arrow {
  font-size: var(--text-lg);
  color: var(--color-text-muted);
  transition: transform var(--transition-fast);
}

.menu-item:hover .menu-arrow {
  transform: translateX(4px);
  color: var(--color-accent);
}

/* Recent Orders */
.recent-orders {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-divider);
}

.section-header h2 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  color: var(--color-text);
}

.more-link {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.more-link:hover {
  color: var(--color-accent);
}

.order-list {
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
  cursor: pointer;
  transition: all var(--transition-fast);
}

.order-item:hover {
  background: var(--color-bg-cream);
}

.order-info {
  flex: 1;
}

.order-no {
  display: block;
  font-family: var(--font-display);
  font-size: var(--text-sm);
  color: var(--color-text);
  margin-bottom: var(--space-1);
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
  background: rgba(192, 154, 75, 0.1);
  color: var(--color-accent);
}

.order-status.paid {
  background: rgba(192, 154, 75, 0.08);
  color: var(--color-accent-muted);
}

.order-status.shipped {
  background: rgba(74, 53, 38, 0.08);
  color: var(--color-primary);
}

.order-status.delivered {
  background: rgba(74, 53, 38, 0.12);
  color: var(--color-primary);
}

.order-status.cancelled {
  background: var(--color-bg-cream);
  color: var(--color-text-muted);
}

.order-total {
  text-align: right;
}

.total-value {
  display: block;
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  color: var(--color-accent);
}

.total-items {
  font-size: var(--text-xs);
  color: var(--color-text-muted);
}

.empty-orders {
  text-align: center;
  padding: var(--space-8);
  color: var(--color-text-muted);
}

.empty-orders p {
  margin-bottom: var(--space-4);
}

.btn-browse {
  display: inline-block;
  padding: var(--space-2) var(--space-6);
  background: var(--color-accent);
  color: white;
  text-decoration: none;
  border-radius: var(--radius-md);
  font-size: var(--text-sm);
  transition: background var(--transition-fast);
}

.btn-browse:hover {
  background: var(--color-accent-muted);
}

@media (max-width: 768px) {
  .user-profile {
    flex-direction: column;
    text-align: center;
  }

  .profile-stats {
    width: 100%;
    justify-content: space-around;
  }

  .user-menu {
    grid-template-columns: 1fr;
  }

  .order-item {
    flex-wrap: wrap;
  }

  .order-status {
    order: 3;
    margin-left: 76px;
  }

  .order-total {
    order: 4;
    width: 100%;
    text-align: left;
    padding-left: 76px;
    margin-top: var(--space-2);
  }
}
</style>