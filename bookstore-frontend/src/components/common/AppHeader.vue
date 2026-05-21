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
        <div class="search-box">
          <span class="search-icon">🔍</span>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索书名、作者..."
            @keyup.enter="handleSearch"
          />
        </div>
        <router-link to="/cart" class="nav-cart">
          🛒
          <div v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</div>
        </router-link>
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

const closeMenu = (e) => {
  if (!e.target.closest('.user-menu')) {
    showUserMenu.value = false
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

.search-box {
  display: flex;
  align-items: center;
  background: rgba(237,230,214,0.06);
  border: 1px solid rgba(237,230,214,0.1);
  border-radius: 8px;
  padding: 7px 14px;
  transition: all 0.3s ease;
}

.search-box:focus-within {
  background: rgba(237,230,214,0.1);
  border-color: rgba(192,154,75,0.4);
  box-shadow: 0 0 0 3px var(--color-accent-glow);
}

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--color-bg);
  font-size: 0.85rem;
  width: 160px;
  font-family: var(--font-body);
}

.search-box input::placeholder { color: rgba(237,230,214,0.25); }
.search-icon { color: rgba(237,230,214,0.35); font-size: 0.9rem; margin-right: 8px; }

.nav-cart {
  position: relative;
  color: rgba(237,230,214,0.5);
  font-size: 1.15rem;
  cursor: pointer;
  transition: color 0.2s;
  text-decoration: none;
}

.nav-cart:hover { color: var(--color-accent); }

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
  .search-box input { width: 100px; }
}
</style>
