<template>
  <div class="admin-layout">
    <!-- Overlay for mobile -->
    <div class="sidebar-overlay" :class="{ show: sidebarOpen }" @click="sidebarOpen = false"></div>

    <!-- Sidebar -->
    <aside class="sidebar" :class="{ open: sidebarOpen }">
      <div class="sidebar-logo">
        <div class="logo-icon">📖</div>
        <div>
          <div class="logo-text">书斋</div>
          <span class="logo-sub">ADMIN CONSOLE</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <template v-for="(section, sIdx) in menuSections" :key="sIdx">
          <div class="sidebar-section">
            <div class="sidebar-section-title">{{ section.title }}</div>
            <router-link
              v-for="item in section.items"
              :key="item.path"
              :to="item.path"
              class="sidebar-item"
              :class="{ active: isActive(item) }"
            >
              <span class="item-icon">{{ item.icon }}</span>
              <span class="item-label">{{ item.label }}</span>
              <span v-if="item.badge" class="item-badge" :class="{ warn: item.badgeWarn }">{{ item.badge }}</span>
            </router-link>
          </div>
        </template>
      </nav>

      <div class="sidebar-bottom">
        <div class="sidebar-user" @click="sidebarOpen = false">
          <div class="avatar">{{ adminUsername?.[0]?.toUpperCase() }}</div>
          <div class="user-info">
            <div class="name">{{ adminUsername }}</div>
            <div class="role">超级管理员</div>
          </div>
        </div>
        <div class="sidebar-actions">
          <router-link to="/" class="action-link">
            <span class="action-icon">←</span>
            <span>返回前台</span>
          </router-link>
          <button class="action-link logout" @click="handleLogout">
            <span class="action-icon">⊘</span>
            <span>退出登录</span>
          </button>
        </div>
      </div>
    </aside>

    <!-- Main -->
    <div class="main">
      <!-- Header -->
      <header class="header">
        <div class="header-left">
          <button class="menu-toggle" @click="sidebarOpen = !sidebarOpen">
            <span></span><span></span><span></span>
          </button>
          <h2>{{ currentPageTitle }}</h2>
          <div class="breadcrumb">
            <span>管理后台</span> / <span id="breadcrumbPage">{{ currentSectionTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="header-search">
            <span class="s-icon">🔍</span>
            <input type="text" placeholder="搜索书籍、订单、用户...">
          </div>
          <button class="header-icon-btn" title="通知">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            <span class="dot"></span>
          </button>
          <button class="header-icon-btn" title="前台预览" @click="router.push('/')">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/></svg>
          </button>
        </div>
      </header>

      <!-- Content -->
      <div class="content">
        <router-view />
      </div>
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

const sidebarOpen = ref(false)

const handleSidebarResize = () => {
  if (window.innerWidth > 768) sidebarOpen.value = false
}

const adminUsername = computed(() => userStore.username)

const menuSections = [
  {
    title: '概览',
    items: [
      { path: '/admin', icon: '📊', label: '仪表盘', exact: true }
    ]
  },
  {
    title: '商品管理',
    items: [
      { path: '/admin/books', icon: '📚', label: '书籍管理' },
      { path: '/admin/inventory', icon: '📦', label: '库存管理' },
      { path: '/admin/categories', icon: '📂', label: '分类管理' }
    ]
  },
  {
    title: '交易管理',
    items: [
      { path: '/admin/orders', icon: '🧾', label: '订单管理' },
      { path: '/admin/refunds', icon: '↩️', label: '退款售后' }
    ]
  },
  {
    title: '用户与运营',
    items: [
      { path: '/admin/users', icon: '👥', label: '用户管理' },
      { path: '/admin/community', icon: '💬', label: '社区管理' },
      { path: '/admin/reviews', icon: '✍️', label: '评价审核', badge: '12', badgeWarn: true }
    ]
  }
]

const currentPageTitle = computed(() => {
  for (const section of menuSections) {
    for (const item of section.items) {
      if (item.path === route.path || (!item.exact && route.path.startsWith(item.path + '/'))) return item.label
      if (item.exact && route.path === item.path) return item.label
    }
  }
  return '管理后台'
})

const currentSectionTitle = computed(() => {
  for (const section of menuSections) {
    for (const item of section.items) {
      if (item.path === route.path || (!item.exact && route.path.startsWith(item.path + '/'))) return section.title
      if (item.exact && route.path === item.path) return section.title
    }
  }
  return '概览'
})

const isActive = (item) => {
  if (item.exact) return route.path === item.path
  return route.path === item.path || route.path.startsWith(item.path + '/')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

onMounted(() => {
  window.addEventListener('resize', handleSidebarResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleSidebarResize)
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* ── Sidebar ── */
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 260px;
  background: var(--color-primary-abyss);
  border-right: 1px solid rgba(192, 154, 75, 0.08);
  display: flex;
  flex-direction: column;
  z-index: 100;
  overflow-y: auto;
}

.sidebar::-webkit-scrollbar { width: 3px; }
.sidebar::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.08); border-radius: 3px; }

.sidebar-logo {
  padding: 22px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.sidebar-logo .logo-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  transform: rotate(-2deg);
  box-shadow: 0 2px 8px rgba(192, 154, 75, 0.2);
  flex-shrink: 0;
}

.sidebar-logo .logo-text {
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 900;
  color: var(--color-bg-warm);
  letter-spacing: 0.04em;
  line-height: 1.3;
}

.sidebar-logo .logo-sub {
  font-size: 0.55rem;
  color: rgba(237, 230, 214, 0.3);
  letter-spacing: 0.12em;
  display: block;
  line-height: 1;
}

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.sidebar-nav::-webkit-scrollbar { width: 3px; }
.sidebar-nav::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.08); border-radius: 3px; }

.sidebar-section { padding: 20px 16px 8px; }
.sidebar-section:first-child { padding-top: 16px; }

.sidebar-section-title {
  font-size: 0.65rem;
  font-weight: 500;
  color: rgba(237, 230, 214, 0.25);
  letter-spacing: 0.15em;
  text-transform: uppercase;
  padding: 0 8px;
  margin-bottom: 8px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border-radius: 8px;
  margin-bottom: 2px;
  font-size: 0.86rem;
  color: rgba(237, 230, 214, 0.45);
  text-decoration: none;
  cursor: pointer;
  transition: all 0.25s;
  user-select: none;
}

.sidebar-item:hover {
  background: rgba(237, 230, 214, 0.04);
  color: rgba(237, 230, 214, 0.7);
}

.sidebar-item.active {
  background: rgba(192, 154, 75, 0.1);
  color: var(--color-accent);
}

.sidebar-item .item-icon {
  font-size: 1rem;
  width: 22px;
  text-align: center;
  flex-shrink: 0;
}

.sidebar-item .item-label {
  font-size: 0.86rem;
  font-weight: 500;
}

.sidebar-item .item-badge {
  margin-left: auto;
  font-size: 0.65rem;
  font-weight: 700;
  background: var(--color-accent);
  color: var(--color-primary-abyss);
  padding: 1px 7px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  line-height: 1.5;
}

.sidebar-item .item-badge.warn {
  background: var(--color-red);
  color: #fff;
}

.sidebar-bottom {
  margin-top: auto;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
  flex-shrink: 0;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.sidebar-user:hover { background: rgba(237, 230, 214, 0.04); }

.sidebar-user .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary-mid);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.82rem;
  font-weight: 700;
  color: var(--color-accent-light);
  border: 2px solid rgba(192, 154, 75, 0.2);
  flex-shrink: 0;
}

.sidebar-user .user-info .name { font-size: 0.82rem; color: rgba(237, 230, 214, 0.7); font-weight: 500; }
.sidebar-user .user-info .role { font-size: 0.7rem; color: rgba(237, 230, 214, 0.3); }

.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 2px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
}

.action-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  font-size: 0.78rem;
  color: rgba(237, 230, 214, 0.3);
  text-decoration: none;
  transition: all 0.2s;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  width: 100%;
}

.action-link:hover { background: rgba(237, 230, 214, 0.04); color: rgba(237, 230, 214, 0.5); }

.action-link .action-icon { font-size: 0.8rem; opacity: 0.5; }
.action-link.logout:hover { color: var(--color-red); }

/* ── Main ── */
.main {
  margin-left: 260px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  width: calc(100% - 260px);
}

/* ── Header ── */
.header {
  position: sticky;
  top: 0;
  z-index: 90;
  height: 64px;
  background: var(--color-bg-card);
  border-bottom: 1px solid var(--color-divider);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 1px 6px var(--color-shadow);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-left h2 {
  font-family: var(--font-display);
  font-size: 1.15rem;
  font-weight: 700;
  color: var(--color-text);
}

.breadcrumb {
  font-size: 0.8rem;
  color: var(--color-text-light);
  display: flex;
  gap: 6px;
  align-items: center;
}

.breadcrumb span { opacity: 0.5; }

.menu-toggle {
  display: none;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: 1px solid var(--color-divider);
  background: transparent;
  cursor: pointer;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 0;
  transition: all 0.2s;
}

.menu-toggle:hover { background: var(--color-bg-warm); }

.menu-toggle span {
  display: block;
  width: 16px;
  height: 2px;
  background: var(--color-text-secondary);
  border-radius: 1px;
  transition: all 0.2s;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-search {
  display: flex;
  align-items: center;
  background: var(--color-bg-warm);
  border: 1px solid var(--color-divider);
  border-radius: 8px;
  padding: 7px 14px;
  gap: 8px;
  transition: all 0.3s;
}

.header-search:focus-within {
  border-color: var(--color-primary-mid);
  box-shadow: 0 0 0 3px rgba(74, 53, 38, 0.06);
}

.header-search input {
  background: transparent;
  border: none;
  outline: none;
  font-size: 0.84rem;
  color: var(--color-text);
  width: 200px;
  font-family: var(--font-body);
}

.header-search input::placeholder { color: var(--color-text-light); }

.header-search .s-icon { color: var(--color-text-light); font-size: 0.88rem; }

.header-icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid var(--color-divider);
  color: var(--color-text-secondary);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.header-icon-btn:hover { background: var(--color-bg-warm); border-color: var(--color-divider-strong); }

.header-icon-btn .dot {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--color-accent);
}

/* ── Content ── */
.content {
  flex: 1;
  padding: 28px 32px 60px;
}

/* ── Overlay ── */
.sidebar-overlay {
  display: none;
  position: fixed;
  inset: 0;
  background: rgba(28, 18, 12, 0.5);
  z-index: 99;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
}

/* ── Responsive ── */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  }

  .sidebar.open { transform: translateX(0); }

  .main {
    margin-left: 0;
    width: 100%;
  }

  .menu-toggle { display: flex; }

  .header { padding: 0 16px; }
  .header-left h2 { font-size: 1rem; }

  .content { padding: 20px 16px 40px; }

  .header-search { display: none; }

  .breadcrumb { display: none; }

  .sidebar-overlay { display: block; }
  .sidebar-overlay.show { opacity: 1; pointer-events: auto; }
}
</style>
