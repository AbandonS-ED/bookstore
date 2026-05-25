<template>
  <header class="navbar">
    <div class="navbar-inner">
      <router-link to="/" class="logo">
        <div class="logo-icon">📖</div>
        <div>
          书斋
        </div>
      </router-link>
      <ul class="nav-links">
        <li><router-link to="/" class="nav-link" active-class="active">首页</router-link></li>
        <li><router-link to="/books" class="nav-link" active-class="active">全部书籍</router-link></li>
        <li><router-link to="/categories" class="nav-link" active-class="active">分类浏览</router-link></li>
        <li><router-link to="/ranking" class="nav-link" active-class="active">排行榜</router-link></li>
        <li><router-link to="/new-arrivals" class="nav-link" active-class="active">新书上架</router-link></li>
        <li><router-link to="/about" class="nav-link" active-class="active">关于我们</router-link></li>
      </ul>
      <div class="nav-right">
        <div class="search-container-light" :class="{ 'has-suggestions': showSuggestions }">
          <div class="search-box-light">
            <svg class="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
            <input
              v-model="searchKeyword"
              type="text"
              placeholder="搜索"
              @input="handleSearchInput"
              @keyup.enter="handleSearch"
              @blur="hideSuggestions"
              @focus="handleSearchInput"
            />
          </div>
          <div v-if="showSuggestions" class="search-suggestions-light">
            <div
              v-for="(item, index) in suggestions"
              :key="index"
              class="suggestion-item-light"
              @mousedown.prevent="selectSuggestion(item)"
            >
              {{ item }}
            </div>
          </div>
        </div>
        <router-link to="/cart" class="nav-cart">
          🛒
          <div v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</div>
        </router-link>
        <router-link to="/favorites" class="nav-fav">♡</router-link>
        <template v-if="userStore.isLoggedIn">
          <div class="user-menu" @click="showUserMenu = !showUserMenu">
            <span class="avatar">{{ userStore.username?.[0]?.toUpperCase() }}</span>
            <span class="caret">▼</span>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { bookApi } from '@/api/book'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showUserMenu = ref(false)
const suggestions = ref([])
const showSuggestions = ref(false)
const searchTimer = ref(null)

const cartCount = computed(() => cartStore.totalCount)
const isAdmin = computed(() => userStore.userInfo?.role === 'admin')

const handleSearchInput = () => {
  clearTimeout(searchTimer.value)
  if (searchKeyword.value.trim().length < 1) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  searchTimer.value = setTimeout(async () => {
    try {
      const res = await bookApi.searchSuggestions(searchKeyword.value)
      if (res.code === 200 && res.data) {
        suggestions.value = res.data
        showSuggestions.value = suggestions.value.length > 0
      }
    } catch (e) {
      console.error('Search suggestions error:', e)
    }
  }, 200)
}

const selectSuggestion = (suggestion) => {
  const title = suggestion.split(' - ')[0]
  searchKeyword.value = title
  showSuggestions.value = false
  router.push({ path: '/books', query: { keyword: title } })
  searchKeyword.value = ''
}

const hideSuggestions = () => {
  setTimeout(() => {
    showSuggestions.value = false
  }, 150)
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    showSuggestions.value = false
    router.push({ path: '/books', query: { keyword: searchKeyword.value } })
    searchKeyword.value = ''
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/')
}

const closeMenu = (e) => {
  if (!e.target.closest('.user-menu')) {
    showUserMenu.value = false
  }
  if (!e.target.closest('.search-box')) {
    showSuggestions.value = false
  }
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    cartStore.getCartList()
  }
  document.addEventListener('click', closeMenu)
})

onUnmounted(() => {
  document.removeEventListener('click', closeMenu)
  clearTimeout(searchTimer.value)
})
</script>

<style scoped>
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--color-primary-abyss);
  border-bottom: 1px solid rgba(192,154,75,0.15);
  box-shadow: 0 1px 20px var(--color-shadow-deep);
}

.navbar-inner {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
}

.logo {
  font-family: var(--font-display);
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--color-bg-warm);
  letter-spacing: 0.06em;
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
}

.logo-icon {
  width: 34px;
  height: 34px;
  background: linear-gradient(135deg, var(--color-accent), var(--color-accent-muted));
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.1rem;
  transform: rotate(-2deg);
  box-shadow: 0 2px 8px rgba(192,154,75,0.2);
}

.logo-text-sub {
  font-size: 0.6rem;
  color: var(--color-text-light);
  font-weight: 400;
  letter-spacing: 0.15em;
  display: block;
  margin-top: -2px;
}

.nav-links {
  display: flex;
  gap: 32px;
  list-style: none;
}

.nav-link {
  color: rgba(237,230,214,0.5);
  font-size: 0.88rem;
  font-weight: 400;
  letter-spacing: 0.04em;
  position: relative;
  padding-bottom: 4px;
  transition: color 0.3s ease;
  text-decoration: none;
}

.nav-link::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 0;
  height: 1.5px;
  background: var(--color-accent);
  transition: all 0.3s ease;
  transform: translateX(-50%);
}

.nav-link:hover { color: rgba(237,230,214,0.85); }
.nav-link:hover::after { width: 100%; }
.nav-link.active { color: var(--color-accent); }
.nav-link.active::after { width: 100%; }

.nav-right {
  display: flex;
  align-items: center;
  gap: 18px;
}

.search-container-light {
  position: relative;
  width: 180px;
  transition: width 0.25s ease;
}

.search-container-light.has-suggestions {
  width: 220px;
}

.search-box-light {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 8px 12px;
  transition: all 0.25s ease;
}

.search-container-light.has-suggestions .search-box-light {
  background: #fff;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  border-bottom: none;
}

.search-box-light:focus-within {
  background: #fff;
  border-color: #ccc;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.search-icon-svg {
  width: 16px;
  height: 16px;
  color: #999;
  margin-right: 8px;
  flex-shrink: 0;
}

.search-box-light input {
  background: transparent;
  border: none;
  outline: none;
  color: #333;
  font-size: 0.88rem;
  width: 100%;
  font-family: var(--font-body);
}

.search-box-light input::placeholder { color: #999; }

.search-suggestions-light {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #ccc;
  border-top: none;
  border-radius: 0 0 10px 10px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  max-height: 280px;
  overflow-y: auto;
  z-index: 999;
}

.suggestion-item-light {
  padding: 10px 12px;
  font-size: 0.85rem;
  color: #333;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggestion-item-light:first-child {
  border-radius: 0;
}

.suggestion-item-light:last-child {
  border-radius: 0 0 10px 10px;
}

.suggestion-item-light:hover {
  background: #f5f5f5;
}

.suggestion-item-light:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.nav-cart {
  position: relative;
  color: rgba(237,230,214,0.5);
  font-size: 1.15rem;
  cursor: pointer;
  transition: color 0.2s;
  text-decoration: none;
}

.nav-cart:hover { color: var(--color-accent); }

.nav-fav {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: rgba(237,230,214,0.5);
  font-size: 1.15rem;
  cursor: pointer;
  text-decoration: none;
  transition: color 0.2s;
}
.nav-fav::before {
  content: '♥';
  position: absolute;
  opacity: 0;
  transform: scale(0) rotate(-20deg);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.nav-fav:hover { color: transparent; }
.nav-fav:hover::before {
  opacity: 1;
  transform: scale(1) rotate(0deg);
  color: var(--color-accent);
}

.cart-badge {
  position: absolute;
  top: -7px;
  right: -10px;
  background: var(--color-accent);
  color: var(--color-primary-abyss);
  font-size: 0.6rem;
  font-weight: 700;
  min-width: 17px;
  height: 17px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 3px;
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-menu:hover { background: rgba(237,230,214,0.06); }

.avatar {
  width: 28px;
  height: 28px;
  background: var(--color-accent);
  color: var(--color-primary-abyss);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.8rem;
}

.caret {
  font-size: 0.55rem;
  color: rgba(237,230,214,0.35);
}

.dropdown-menu {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  min-width: 160px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-divider);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--space-2);
  animation: fadeIn 0.2s ease;
}

.dropdown-item {
  display: block;
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-text);
  border-radius: var(--radius-md);
  transition: background var(--transition-fast);
  cursor: pointer;
  text-decoration: none;
}

.dropdown-item:hover { background: var(--color-bg-warm); }

.dropdown-divider {
  height: 1px;
  background: var(--color-divider);
  margin: var(--space-2) 0;
}

.btn-login,
.btn-register {
  font-size: 0.85rem;
  font-weight: 500;
  transition: all var(--transition-fast);
  text-decoration: none;
}

.btn-login {
  color: rgba(237,230,214,0.6);
  padding: 8px 16px;
}

.btn-login:hover { color: var(--color-accent); }

.btn-register {
  background: var(--color-accent);
  color: var(--color-primary-abyss);
  padding: 8px 20px;
  border-radius: 8px;
  font-weight: 700;
}

.btn-register:hover {
  background: var(--color-accent-light);
  color: var(--color-primary-abyss);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(192,154,75,0.3);
}

@media (max-width: 768px) {
  .navbar-inner { padding: 0 20px; }
  .nav-links { display: none; }
  .search-container-light { width: 140px; }
  .search-container-light.has-suggestions { width: 160px; }
}
</style>
