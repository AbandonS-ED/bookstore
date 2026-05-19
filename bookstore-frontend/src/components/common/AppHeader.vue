<template>
  <header class="header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <span class="logo-icon">📚</span>
          <span class="logo-text">书斋</span>
        </router-link>
        <nav class="nav">
          <router-link to="/books" class="nav-link">书库</router-link>
          <router-link to="/books" class="nav-link">分类</router-link>
        </nav>
      </div>

      <div class="header-center">
        <div class="search-box">
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索书名、作者..."
            class="search-input"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">🔍</button>
        </div>
      </div>

      <div class="header-right">
        <template v-if="userStore.isLoggedIn">
          <router-link to="/cart" class="icon-btn cart-btn">
            <span class="icon">🛒</span>
            <span v-if="cartCount > 0" class="badge">{{ cartCount }}</span>
          </router-link>
          <div class="user-menu" @click="showUserMenu = !showUserMenu">
            <span class="avatar">{{ userStore.username?.[0]?.toUpperCase() }}</span>
            <span class="username">{{ userStore.username }}</span>
            <span class="arrow">▼</span>
            <div v-if="showUserMenu" class="dropdown-menu">
              <router-link to="/user" class="dropdown-item">个人中心</router-link>
              <router-link to="/user/orders" class="dropdown-item">我的订单</router-link>
              <router-link v-if="isAdmin" to="/admin" class="dropdown-item">管理后台</router-link>
              <div class="dropdown-divider"></div>
              <a class="dropdown-item" @click="handleLogout">退出登录</a>
            </div>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-login">登录</router-link>
          <router-link to="/register" class="btn-register">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showUserMenu = ref(false)

const cartCount = computed(() => cartStore.totalCount)
const isAdmin = computed(() => userStore.userInfo?.role === 'admin')

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/books', query: { keyword: searchKeyword.value } })
    searchKeyword.value = ''
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.getCartList()
  }
})
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: var(--header-height);
  background: var(--color-paper-white);
  border-bottom: 1px solid var(--color-border);
  z-index: 1000;
  backdrop-filter: blur(10px);
  background: rgba(254, 254, 254, 0.95);
}

.header-container {
  max-width: var(--max-width);
  height: 100%;
  margin: 0 auto;
  padding: 0 var(--space-6);
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-8);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
}

.logo-icon {
  font-size: 28px;
}

.logo-text {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  font-weight: 700;
  color: var(--color-ink);
  letter-spacing: 2px;
}

.nav {
  display: flex;
  gap: var(--space-6);
}

.nav-link {
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  transition: color var(--transition-fast);
}

.nav-link:hover {
  color: var(--color-vermillion);
}

.header-center {
  flex: 1;
  max-width: 480px;
}

.search-box {
  display: flex;
  align-items: center;
  background: var(--color-paper);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-full);
  overflow: hidden;
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
}

.search-box:focus-within {
  border-color: var(--color-vermillion);
  box-shadow: 0 0 0 3px rgba(201, 64, 67, 0.1);
}

.search-input {
  flex: 1;
  padding: var(--space-3) var(--space-5);
  border: none;
  background: transparent;
  font-size: var(--text-sm);
  color: var(--color-text-primary);
}

.search-input::placeholder {
  color: var(--color-text-muted);
}

.search-input:focus {
  outline: none;
}

.search-btn {
  padding: var(--space-3) var(--space-5);
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: var(--text-lg);
  transition: opacity var(--transition-fast);
}

.search-btn:hover {
  opacity: 0.7;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.icon-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  transition: background var(--transition-fast);
}

.icon-btn:hover {
  background: var(--color-paper-dark);
}

.icon {
  font-size: 20px;
}

.badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: var(--color-vermillion);
  color: white;
  font-size: 11px;
  font-weight: 600;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: background var(--transition-fast);
}

.user-menu:hover {
  background: var(--color-paper-dark);
}

.avatar {
  width: 32px;
  height: 32px;
  background: var(--color-vermillion);
  color: white;
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: var(--text-sm);
}

.username {
  font-size: var(--text-sm);
  color: var(--color-text-primary);
}

.arrow {
  font-size: 10px;
  color: var(--color-text-muted);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  min-width: 160px;
  background: var(--color-paper-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-2);
  animation: fadeIn 0.2s ease;
}

.dropdown-item {
  display: block;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text-primary);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
  cursor: pointer;
}

.dropdown-item:hover {
  background: var(--color-paper-dark);
}

.dropdown-divider {
  height: 1px;
  background: var(--color-border);
  margin: var(--space-2) 0;
}

.btn-login,
.btn-register {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.btn-login {
  color: var(--color-text-primary);
}

.btn-login:hover {
  color: var(--color-vermillion);
}

.btn-register {
  background: var(--color-vermillion);
  color: white;
}

.btn-register:hover {
  background: var(--color-vermillion-light);
  color: white;
}
</style>