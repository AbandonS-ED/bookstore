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
        <li><router-link to="/explore" class="nav-link" active-class="active">探索好书</router-link></li>
        <li><router-link to="/about" class="nav-link" active-class="active">关于我们</router-link></li>
      </ul>
      <div class="nav-right">
        <div :class="['search-box', { suggested: showSuggestions }]">
          <span class="search-icon">🔍</span>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索书名、作者..."
            @keydown="onSuggestionKeydown"
            @keyup.enter="handleSearchInput"
          />
          <div v-if="showSuggestions" class="search-suggestions">
            <div
              v-for="(book, i) in suggestions"
              :key="book.id"
              :class="['ss-item', { active: i === selectedIdx }]"
              @mousedown.prevent="selectSuggestion(book)"
            >
              <span class="ss-title">{{ book.title }}</span>
              <span class="ss-author">{{ book.author }}</span>
            </div>
          </div>
        </div>
        <router-link to="/cart" class="nav-cart">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 01-8 0"/>
          </svg>
          <div v-if="cartCount > 0" class="cart-badge">{{ cartCount > 99 ? '99+' : cartCount }}</div>
        </router-link>
        <router-link to="/ai-assistant" class="nav-ai">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
            <rect x="4" y="9" width="16" height="11" rx="2.5"/>
            <path d="M8 4h8"/>
            <path d="M12 4v5"/>
            <circle cx="9" cy="14.5" r="1.2" fill="currentColor" stroke="none"/>
            <circle cx="15" cy="14.5" r="1.2" fill="currentColor" stroke="none"/>
            <path d="M9.5 18.5h5"/>
          </svg>
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
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
const selectedIdx = ref(-1)
let suggestTimer = null

const cartCount = computed(() => cartStore.totalCount)
const isAdmin = computed(() => userStore.userInfo?.role === 'admin')

watch(searchKeyword, (val) => {
  clearTimeout(suggestTimer)
  if (!val.trim()) {
    suggestions.value = []
    showSuggestions.value = false
    return
  }
  suggestTimer = setTimeout(() => {
    fetchSuggestions(val.trim())
  }, 200)
})

function fetchSuggestions(keyword) {
  bookApi.getList({ keyword, pageSize: 6, pageNum: 1 }).then(res => {
    suggestions.value = res.data?.records || []
    showSuggestions.value = suggestions.value.length > 0
    selectedIdx.value = -1
  }).catch(() => {
    suggestions.value = []
    showSuggestions.value = false
  })
}

function selectSuggestion(book) {
  showSuggestions.value = false
  suggestions.value = []
  searchKeyword.value = ''
  router.push(`/book/${book.id}`)
}

function onSuggestionKeydown(e) {
  if (!showSuggestions.value) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    selectedIdx.value = Math.min(selectedIdx.value + 1, suggestions.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    selectedIdx.value = Math.max(selectedIdx.value - 1, 0)
  } else if (e.key === 'Enter' && selectedIdx.value >= 0) {
    e.preventDefault()
    selectSuggestion(suggestions.value[selectedIdx.value])
  } else if (e.key === 'Escape') {
    showSuggestions.value = false
  }
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/books', query: { keyword: searchKeyword.value } })
    searchKeyword.value = ''
    showSuggestions.value = false
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

function handleSearchInput(e) {
  const el = e.target
  if (e.key === 'Enter' && !showSuggestions.value) {
    handleSearch()
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
  clearTimeout(suggestTimer)
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
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(237,230,214,0.06);
  border: 1px solid rgba(237,230,214,0.1);
  border-radius: 8px;
  padding: 7px 14px;
  transition: all 0.2s ease;
  min-width: 180px;
}
.search-box.suggested {
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  border-bottom-color: transparent;
}
.search-box.suggested:focus-within {
  border-bottom-color: transparent;
  box-shadow: none;
}

.search-icon { color: rgba(237,230,214,0.35); font-size: 0.9rem; margin-right: 8px; }

.search-box input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--color-bg);
  font-size: 0.85rem;
  font-family: var(--font-body);
}
.search-box:focus-within {
  background: rgba(237,230,214,0.1);
  border-color: rgba(192,154,75,0.4);
  box-shadow: 0 0 0 3px var(--color-accent-glow);
}

.search-box input::placeholder { color: rgba(237,230,214,0.25); }

.search-suggestions {
  position: absolute;
  top: 100%;
  left: -0.5px;
  right: -0.5px;
  margin-top: -1px;
  background: linear-gradient(rgba(237,230,214,0.06), rgba(237,230,214,0.06)), var(--color-primary-abyss);
  border: 1px solid rgba(237,230,214,0.1);
  border-top: none;
  border-radius: 0 0 8px 8px;
  z-index: 200;
  overflow: hidden;
  padding: 4px 0;
}
.search-box:focus-within .search-suggestions {
  background: linear-gradient(rgba(237,230,214,0.1), rgba(237,230,214,0.1)), var(--color-primary-abyss);
  border-color: rgba(192,154,75,0.4);
  border-top-color: transparent;
}
.ss-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 7px 4px;
  cursor: pointer;
  transition: background 0.15s;
  border-radius: 6px;
  gap: 10px;
}
.ss-item:hover, .ss-item.active {
  background: rgba(192,154,75,0.12);
}
.ss-title {
  font-size: 0.8rem;
  color: rgba(237,230,214,0.7);
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  min-width: 0;
}
.ss-author {
  font-size: 0.7rem;
  color: rgba(192,154,75,0.35);
  flex-shrink: 0;
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
.nav-cart svg { display: block; }

.nav-ai {
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
.nav-ai:hover { color: var(--color-accent); }
.nav-ai svg { display: block; }

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
  .search-box input { width: 100px; }
}
</style>
