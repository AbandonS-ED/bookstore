<template>
  <div class="cart-page">
    <div class="page-container">
      <h1 class="page-title">我的购物车</h1>

      <div v-if="loading" class="cart-loading">
        <div v-for="i in 3" :key="i" class="skeleton-item">
          <div class="skeleton cover"></div>
          <div class="skeleton-info">
            <div class="skeleton title"></div>
            <div class="skeleton author"></div>
            <div class="skeleton price"></div>
          </div>
        </div>
      </div>

      <div v-else-if="!cartStore.items.length" class="empty-cart">
        <div class="empty-icon">📚</div>
        <h2>购物车空空如也</h2>
        <p>快去挑选几本好书吧</p>
        <router-link to="/books" class="btn-browse">浏览书籍</router-link>
      </div>

      <div v-else class="cart-content">
        <div class="cart-main">
          <div class="cart-items">
            <div
              v-for="item in cartStore.items"
              :key="item.id"
              class="cart-item"
            >
              <div class="item-cover">
                <img :src="item.coverUrl || '/placeholder-book.png'" :alt="item.title" />
              </div>
              <div class="item-info">
                <h3 class="item-title">{{ item.title }}</h3>
                <p class="item-author">{{ item.author }}</p>
                <p class="item-price">¥{{ item.price }}</p>
              </div>
              <div class="item-quantity">
                <button
                  @click="handleUpdateQuantity(item, item.quantity - 1)"
                  :disabled="item.quantity <= 1"
                >−</button>
                <span>{{ item.quantity }}</span>
                <button @click="handleUpdateQuantity(item, item.quantity + 1)">+</button>
              </div>
              <div class="item-subtotal">
                <span class="subtotal-label">小计</span>
                <span class="subtotal-value">¥{{ (item.price * item.quantity).toFixed(2) }}</span>
              </div>
              <button class="item-remove" @click="handleRemove(item.id)">
                删除
              </button>
            </div>
          </div>
        </div>

        <div class="cart-sidebar">
          <div class="order-summary">
            <h3>订单摘要</h3>
            <div class="summary-row">
              <span>商品总数</span>
              <span>{{ cartStore.totalCount }} 件</span>
            </div>
            <div class="summary-row total">
              <span>合计</span>
              <span class="total-amount">¥{{ cartStore.totalAmount.toFixed(2) }}</span>
            </div>
            <button class="btn-checkout" @click="handleCheckout">
              去结算
            </button>
            <router-link to="/books" class="btn-continue">继续购物</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { cartApi } from '@/api/cart'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(true)

const handleUpdateQuantity = async (item, newQuantity) => {
  if (newQuantity < 1) return
  try {
    await cartApi.update({ id: item.id, quantity: newQuantity })
    item.quantity = newQuantity
  } catch (error) {
    ElMessage.error('更新数量失败')
  }
}

const handleRemove = async (id) => {
  try {
    await cartStore.removeItem(id)
    ElMessage.success('已删除')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

const handleCheckout = () => {
  router.push('/order/confirm')
}

onMounted(async () => {
  try {
    await cartStore.getCartList()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.cart-page {
  padding: var(--space-8) 0 var(--space-16);
  min-height: calc(100vh - var(--header-height));
  background: var(--color-bg);
}

.page-container {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.page-title {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-text);
  margin-bottom: var(--space-8);
  padding-bottom: var(--space-4);
  border-bottom: 2px solid var(--color-accent);
}

.cart-loading {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.skeleton-item {
  display: flex;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-divider);
}

.skeleton {
  background: linear-gradient(90deg, var(--color-divider) 25%, var(--color-bg-cream) 50%, var(--color-divider) 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: var(--radius-md);
}

.skeleton.cover {
  width: 100px;
  height: 130px;
}

.skeleton-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.skeleton.title {
  height: 24px;
  width: 60%;
}

.skeleton.author {
  height: 16px;
  width: 40%;
}

.skeleton.price {
  height: 20px;
  width: 20%;
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.empty-cart {
  text-align: center;
  padding: var(--space-16) 0;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-divider);
}

.empty-icon {
  font-size: 80px;
  margin-bottom: var(--space-4);
}

.empty-cart h2 {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  color: var(--color-text);
  margin-bottom: var(--space-2);
}

.empty-cart p {
  color: var(--color-text-muted);
  margin-bottom: var(--space-6);
}

.btn-browse {
  display: inline-block;
  padding: var(--space-3) var(--space-8);
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%);
  color: var(--color-primary-abyss);
  text-decoration: none;
  border-radius: var(--radius-md);
  font-weight: 700;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 12px rgba(192,154,75,0.2);
}

.btn-browse:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(192,154,75,0.3);
}

.cart-content {
  display: flex;
  gap: var(--space-6);
}

.cart-main {
  flex: 1;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.cart-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-divider);
  transition: box-shadow var(--transition-fast);
}

.cart-item:hover {
  box-shadow: var(--shadow-md);
}

.item-cover {
  width: 80px;
  height: 100px;
  flex-shrink: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-family: var(--font-display);
  font-size: var(--text-base);
  font-weight: 600;
  margin-bottom: var(--space-1);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--color-text);
}

.item-author {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
  margin-bottom: var(--space-2);
}

.item-price {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  color: var(--color-accent-muted);
}

.item-quantity {
  display: flex;
  align-items: center;
  border: 1px solid var(--color-divider-strong);
  border-radius: var(--radius-md);
}

.item-quantity button {
  width: 32px;
  height: 36px;
  border: none;
  background: var(--color-bg-cream);
  font-size: 16px;
  cursor: pointer;
  transition: background var(--transition-fast);
  color: var(--color-text-secondary);
}

.item-quantity button:hover:not(:disabled) {
  background: var(--color-divider);
}

.item-quantity button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.item-quantity span {
  width: 40px;
  text-align: center;
  font-size: var(--text-base);
  color: var(--color-text);
}

.item-subtotal {
  width: 100px;
  text-align: right;
}

.subtotal-label {
  display: block;
  font-size: var(--text-xs);
  color: var(--color-text-muted);
  margin-bottom: var(--space-1);
}

.subtotal-value {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  font-weight: 600;
  color: var(--color-accent);
}

.item-remove {
  padding: var(--space-2) var(--space-3);
  background: none;
  border: 1px solid var(--color-divider-strong);
  border-radius: var(--radius-md);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.item-remove:hover {
  border-color: var(--color-accent);
  color: var(--color-accent);
}

.cart-sidebar {
  width: 320px;
  flex-shrink: 0;
}

.order-summary {
  position: sticky;
  top: calc(var(--header-height) + var(--space-6));
  padding: var(--space-6);
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-divider);
}

.order-summary h3 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--color-divider);
  color: var(--color-text);
}

.summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
}

.summary-row.total {
  margin-top: var(--space-4);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-divider);
  font-weight: 600;
  font-size: var(--text-base);
  color: var(--color-text);
}

.total-amount {
  font-family: var(--font-display);
  font-size: var(--text-xl);
  color: var(--color-accent);
}

.btn-checkout {
  width: 100%;
  padding: var(--space-4);
  margin-top: var(--space-4);
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%);
  color: var(--color-primary-abyss);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--text-base);
  font-weight: 700;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 2px 12px rgba(192,154,75,0.2);
}

.btn-checkout:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(192,154,75,0.3);
}

.btn-continue {
  display: block;
  text-align: center;
  margin-top: var(--space-3);
  color: var(--color-text-muted);
  font-size: var(--text-sm);
  text-decoration: none;
  transition: color var(--transition-fast);
}

.btn-continue:hover {
  color: var(--color-accent);
}

@media (max-width: 768px) {
  .cart-content {
    flex-direction: column;
  }

  .cart-sidebar {
    width: 100%;
  }

  .order-summary {
    position: static;
  }

  .cart-item {
    flex-wrap: wrap;
  }

  .item-subtotal {
    width: auto;
    text-align: left;
    margin-left: auto;
  }
}
</style>
