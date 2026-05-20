<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/admin" class="logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">书斋管理</span>
        </router-link>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in menuItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="admin-info">
          <div class="admin-avatar">{{ adminUsername?.[0]?.toUpperCase() }}</div>
          <div class="admin-details">
            <span class="admin-name">{{ adminUsername }}</span>
            <span class="admin-role">管理员</span>
          </div>
        </div>
        <button class="logout-btn" @click="handleLogout">
          <span>退出</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <header class="top-header">
        <div class="header-left">
          <h1 class="page-title">{{ currentPageTitle }}</h1>
        </div>
        <div class="header-right">
          <span class="header-time">{{ currentTime }}</span>
        </div>
      </header>

      <main class="main-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const currentTime = ref('')
let timeInterval = null

const adminUsername = computed(() => userStore.username)

const menuItems = [
  { path: '/admin', icon: '📊', label: 'Dashboard', exact: true },
  { path: '/admin/books', icon: '📚', label: '书籍管理' },
  { path: '/admin/categories', icon: '📁', label: '分类管理' },
  { path: '/admin/orders', icon: '📦', label: '订单管理' },
  { path: '/admin/users', icon: '👥', label: '用户管理' },
  { path: '/admin/reviews', icon: '💬', label: '评论管理' }
]

const currentPageTitle = computed(() => {
  const item = menuItems.find(m => route.path === m.path || (m.path !== '/admin' && route.path.startsWith(m.path)))
  return item?.label || '管理后台'
})

const isActive = (path) => {
  if (path === '/admin') {
    return route.path === '/admin'
  }
  return route.path.startsWith(path)
}

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: var(--color-primary-abyss);
  color: var(--color-text-inverse);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.sidebar-header {
  padding: var(--space-6);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  text-decoration: none;
  color: var(--color-text-inverse);
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 700;
  letter-spacing: 1px;
}

.sidebar-nav {
  flex: 1;
  padding: var(--space-4) 0;
  overflow-y: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-4) var(--space-6);
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all var(--transition-fast);
  border-left: 3px solid transparent;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.05);
  color: var(--color-text-inverse);
}

.nav-item.active {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text-inverse);
  border-left-color: var(--color-accent);
}

.nav-icon {
  font-size: 18px;
  width: 24px;
  text-align: center;
}

.nav-label {
  font-size: var(--text-base);
  font-weight: 500;
}

.sidebar-footer {
  padding: var(--space-4) var(--space-6);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.admin-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-bottom: var(--space-4);
}

.admin-avatar {
  width: 40px;
  height: 40px;
  background: var(--color-accent);
  color: white;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--text-lg);
}

.admin-details {
  display: flex;
  flex-direction: column;
}

.admin-name {
  font-size: var(--text-base);
  font-weight: 600;
}

.admin-role {
  font-size: var(--text-xs);
  color: rgba(255, 255, 255, 0.6);
}

.logout-btn {
  width: 100%;
  padding: var(--space-3);
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: var(--radius-md);
  color: var(--color-text-inverse);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 主内容区 */
.main-wrapper {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.top-header {
  height: var(--header-height);
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-divider);
  padding: 0 var(--space-8);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  font-weight: 600;
  color: var(--color-text);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.header-time {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.main-content {
  flex: 1;
  padding: var(--space-8);
}

/* 响应式 */
@media (max-width: 1024px) {
  .sidebar {
    width: 80px;
  }

  .sidebar-header {
    padding: var(--space-4);
  }

  .logo-text {
    display: none;
  }

  .nav-label {
    display: none;
  }

  .nav-item {
    justify-content: center;
    padding: var(--space-4);
  }

  .admin-details {
    display: none;
  }

  .main-wrapper {
    margin-left: 80px;
  }
}
</style>