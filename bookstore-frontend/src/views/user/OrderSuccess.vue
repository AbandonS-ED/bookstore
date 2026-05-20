<template>
  <div class="order-success-page">
    <div class="page-container">
      <div class="success-card">
        <div class="success-icon">
          <svg width="80" height="80" viewBox="0 0 80 80">
            <circle cx="40" cy="40" r="38" fill="none" stroke="#7a9e7e" stroke-width="2"/>
            <path d="M24 42 L34 52 L56 30" fill="none" stroke="#7a9e7e" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="success-title">订单提交成功</h1>
        <p class="order-number" v-if="orderId">订单号：{{ orderId }}</p>
        <p class="success-tip">感谢您的购买，订单已成功提交</p>
        <div class="success-actions">
          <router-link to="/user/orders" class="btn-primary">查看订单</router-link>
          <router-link to="/books" class="btn-secondary">继续购物</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const orderId = ref('')

onMounted(() => {
  orderId.value = route.query.orderId || ''
})
</script>

<style scoped>
.order-success-page {
  padding: var(--space-16) 0;
  min-height: 60vh;
  display: flex;
  align-items: center;
}

.success-card {
  max-width: 500px;
  margin: 0 auto;
  text-align: center;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-12);
  box-shadow: var(--shadow-md);
}

.success-icon {
  margin-bottom: var(--space-6);
}

.success-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-text);
  margin-bottom: var(--space-4);
}

.order-number {
  font-size: var(--text-base);
  color: var(--color-text-secondary);
  margin-bottom: var(--space-2);
}

.success-tip {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--space-8);
}

.success-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: center;
}

.btn-primary,
.btn-secondary {
  padding: var(--space-3) var(--space-6);
  font-size: var(--text-base);
  font-weight: 500;
  border-radius: var(--radius-md);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.btn-primary {
  background: var(--color-accent);
  color: white;
  border: none;
}

.btn-primary:hover {
  background: var(--color-accent-muted);
}

.btn-secondary {
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  border: 1px solid var(--color-divider);
}

.btn-secondary:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}
</style>