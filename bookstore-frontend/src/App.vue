<template>
  <div id="app">
    <AppHeader v-if="showHeader" />
    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter v-if="showHeader" />
    <ToastContainer :toasts="toasts" />
  </div>
</template>

<script setup>
import { computed, onMounted, provide } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useToast } from '@/composables/useToast'
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import ToastContainer from '@/components/common/ToastContainer.vue'

const route = useRoute()
const userStore = useUserStore()
const { toasts, showToast } = useToast()

provide('showToast', showToast)

const showHeader = computed(() => {
  return !route.path.startsWith('/admin')
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.getUserInfo()
  }
})
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.page-enter-active,
.page-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

:root {
  --el-color-primary: #4A3526;
  --el-color-primary-light-3: #7A6250;
  --el-color-primary-light-5: #A08A74;
  --el-color-primary-light-7: #C5B4A0;
  --el-color-primary-light-8: #D8CBBB;
  --el-color-primary-light-9: #EDE6D6;
  --el-color-primary-dark-2: #3A281C;
  --el-color-success: #4A3526;
  --el-color-warning: #C09A4B;
  --el-color-danger: #C09A4B;
  --el-color-info: #8A7A6A;
  --el-border-radius-base: 6px;
  --el-border-radius-small: 4px;
  --el-font-size-base: 14px;
  --el-font-size-small: 13px;
  --el-font-size-large: 16px;
}

.el-dialog__title {
  font-family: var(--font-display);
  font-size: var(--text-lg);
}

.el-pagination.is-background .el-pager li.is-active {
  background-color: var(--el-color-primary);
}

.el-table th.el-table__cell {
  background-color: var(--color-bg-cream);
  color: var(--color-text);
  font-weight: 600;
}

.el-radio-button__inner:hover { color: var(--el-color-primary); }
.el-radio-button.is-active .el-radio-button__inner {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.el-tag--primary {
  --el-tag-bg-color: rgba(74, 53, 38, 0.08);
  --el-tag-border-color: rgba(74, 53, 38, 0.15);
  --el-tag-text-color: #4A3526;
}

.el-tag--warning {
  --el-tag-bg-color: rgba(192, 154, 75, 0.1);
  --el-tag-border-color: rgba(192, 154, 75, 0.2);
  --el-tag-text-color: #C09A4B;
}

.el-tag--success {
  --el-tag-bg-color: rgba(74, 53, 38, 0.08);
  --el-tag-border-color: rgba(74, 53, 38, 0.15);
  --el-tag-text-color: #4A3526;
}

.el-tag--danger {
  --el-tag-bg-color: rgba(192, 154, 75, 0.1);
  --el-tag-border-color: rgba(192, 154, 75, 0.2);
  --el-tag-text-color: #C09A4B;
}

@media (max-width: 768px) {
  .el-dialog { width: 92% !important; }
}
</style>
