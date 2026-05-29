<template>
  <div class="ai-page">
    <section class="page-header">
      <div class="ph-inner">
        <div class="ph-left">
          <h1>🤖 AI 书友</h1>
          <p>和 AI 对话，找书、买书、了解书籍内容</p>
        </div>
      </div>
    </section>

    <div class="chat-layout">
      <div v-if="!hasStarted" class="welcome-state">
        <div class="welcome-icon">📖</div>
        <h2 class="welcome-title">你好，我是你的 AI 书友</h2>
        <p class="welcome-sub">
          我可以帮你推荐好书、介绍书籍内容，还能直接下单购买。试试说"推荐几本书"或"我想买《百年孤独》"吧！
        </p>
        <div class="welcome-suggestions">
          <div class="suggestion-card" @click="quickSend('推荐几本经典文学作品')">
            <div class="sc-icon">📚</div>
            <div class="sc-title">推荐好书</div>
            <div class="sc-desc">根据你的兴趣推荐值得阅读的作品</div>
          </div>
          <div class="suggestion-card" @click="quickSend('《三体》讲的是什么内容？')">
            <div class="sc-icon">💬</div>
            <div class="sc-title">了解书本</div>
            <div class="sc-desc">想知道一本书讲什么？我来详细介绍</div>
          </div>
          <div class="suggestion-card" @click="quickSend('我想买《活着》')">
            <div class="sc-icon">🛒</div>
            <div class="sc-title">购买书籍</div>
            <div class="sc-desc">告诉我书名，帮你快速加入购物车</div>
          </div>
          <div class="suggestion-card" @click="quickSend('帮我找一些科幻小说')">
            <div class="sc-icon">🚀</div>
            <div class="sc-title">主题探索</div>
            <div class="sc-desc">按类型、领域找到适合你的书</div>
          </div>
        </div>
      </div>

      <div v-else ref="chatRef" class="chat-area">
        <div v-for="(msg, i) in messages" :key="i" class="message-row" :class="msg.role">
          <div class="msg-avatar" :class="msg.role">
            {{ msg.role === 'ai' ? '🤖' : '👤' }}
          </div>
          <div class="msg-content">
            <div class="bubble">
              <div v-if="msg.role === 'ai' && !msg.content" class="typing-dots"><span></span><span></span><span></span></div>
              <div v-else v-html="formatText(msg.content)"></div>

              <div v-if="msg.books && msg.books.length" class="book-list-inline">
                <div v-for="book in msg.books" :key="book.id" class="book-card-inline" @click="viewBook(book.id)">
                  <div class="bci-top">
                    <div class="bci-cover" :style="coverStyle(book.id)">
                      <div class="bci-cover-text">{{ book.title }}</div>
                    </div>
                    <div class="bci-info">
                      <div class="bci-title ellipsis">{{ book.title }}</div>
                      <div class="bci-author ellipsis">{{ book.author }}</div>
                      <div class="bci-rating">
                        <span class="stars">{{ starDisplay(book.avgRating) }}</span>
                        <span class="rating-num">{{ book.avgRating }}</span>
                      </div>
                      <div class="bci-price">
                        ¥{{ Number(book.price).toFixed(2) }}
                        <span v-if="book.origPrice" class="original">¥{{ Number(book.origPrice).toFixed(2) }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="bci-actions">
                    <button class="bci-btn bci-btn-primary" @click.stop="addToCartFromCard(book)">
                      🛒 加入购物车
                    </button>
                    <button v-if="isLoggedIn" class="bci-btn bci-btn-secondary" @click.stop="quickBuy(book)">
                      立即购买
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div class="msg-time">{{ msg.time }}</div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <div class="input-row">
          <div class="input-wrap">
            <textarea
              ref="inputRef"
              v-model="inputText"
              placeholder='输入你想问的，比如「推荐一本好书」...'
              rows="1"
              @keydown="onKeydown"
              @input="autoResize"
            ></textarea>
          </div>
          <button class="send-btn" :disabled="!inputText.trim() || isLoading" @click="sendMessage">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
          </button>
        </div>
        <div class="input-hint">按 Enter 发送，Shift + Enter 换行</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiApi } from '@/api/ai'
import { cartApi } from '@/api/cart'
import { orderApi } from '@/api/order'
import { addressApi } from '@/api/address'
import { useUserStore } from '@/stores/user'
import { getCoverStyle } from '@/utils/cover'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const chatRef = ref(null)
const inputRef = ref(null)
const inputText = ref('')
const hasStarted = ref(false)
const isLoading = ref(false)
const messages = ref([])

function formatText(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function coverStyle(id) {
  return getCoverStyle(id)
}

function starDisplay(rating) {
  if (!rating) return '☆☆☆☆☆'
  const full = Math.floor(rating)
  return '★'.repeat(full) + '☆'.repeat(5 - full)
}

function getTime() {
  const d = new Date()
  return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}

function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 160) + 'px'
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (chatRef.value) {
      chatRef.value.scrollTo({ top: chatRef.value.scrollHeight, behavior: 'smooth' })
    }
  })
}

function addMessage(role, content, books) {
  messages.value.push({
    role,
    content,
    books: books || [],
    time: getTime()
  })
}

function quickSend(text) {
  inputText.value = text
  sendMessage()
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  hasStarted.value = true
  addMessage('user', text)
  inputText.value = ''
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
  isLoading.value = true
  await scrollToBottom()

  const chatMessages = messages.value
    .filter(m => m.role === 'user')
    .map(m => ({ role: 'user', content: m.content }))

  const aiMsg = reactive({ role: 'ai', content: '', books: [], time: getTime() })
  messages.value.push(aiMsg)
  await scrollToBottom()

  try {
    await aiApi.chatStream(
      chatMessages,
      (chunk) => {
        aiMsg.content += chunk
      },
      (books) => {
        aiMsg.books = books
      },
      () => {
        isLoading.value = false
        scrollToBottom()
      }
    )
  } catch {
    aiMsg.content = '抱歉，我暂时无法回应，请稍后再试。'
    isLoading.value = false
    await scrollToBottom()
  }
}

function viewBook(id) {
  router.push(`/book/${id}`)
}

async function addToCartFromCard(book) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再添加购物车')
    router.push('/login')
    return
  }
  try {
    await cartStore.addToCart(book.id, 1)
    ElMessage.success(`《${book.title}》已加入购物车`)
  } catch {
    ElMessage.error('添加失败，请重试')
  }
}

async function quickBuy(book) {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await cartStore.addToCart(book.id, 1)
    await cartStore.getCartList()
    router.push('/order/confirm')
  } catch {
    ElMessage.error('操作失败，请重试')
  }
}

onMounted(async () => {
  if (inputRef.value) {
    inputRef.value.focus()
  }
  if (userStore.isLoggedIn) {
    try {
      const res = await aiApi.getHistory()
      const history = res.data || []
      if (history.length > 0) {
        hasStarted.value = true
        for (const msg of history) {
          addMessage(msg.role, msg.content, [])
        }
        scrollToBottom()
      }
    } catch {}
  }
})
</script>

<style scoped>
.ai-page {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 20px;
  height: calc(100vh - 68px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.page-header {
  padding: 16px 0 10px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--color-border, rgba(74, 53, 38, 0.06));
}

.ph-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.ph-left h1 {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: 0.02em;
}

.ph-left p {
  display: none;
}

.chat-layout {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.welcome-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, var(--color-accent, #C09A4B), #b8913e);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.6rem;
  margin-bottom: 12px;
  box-shadow: 0 6px 24px rgba(192, 154, 75, 0.2);
  transform: rotate(-4deg);
  transition: transform 0.3s ease;
}

.welcome-state:hover .welcome-icon {
  transform: rotate(0deg) scale(1.05);
}

.welcome-title {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 4px;
  letter-spacing: 0.01em;
}

.welcome-sub {
  color: var(--color-text-light, #9B8B7E);
  font-size: 0.8rem;
  text-align: center;
  max-width: 400px;
  line-height: 1.6;
  margin-bottom: 16px;
}

.welcome-suggestions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  width: 100%;
  max-width: 440px;
}

.suggestion-card {
  background: var(--color-bg-card, #F5F0E8);
  border: 1px solid var(--color-border, rgba(74, 53, 38, 0.08));
  border-radius: 10px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.suggestion-card:hover {
  border-color: rgba(192, 154, 75, 0.3);
  transform: translateY(-3px);
  box-shadow: 0 8px 24px var(--color-shadow, rgba(44, 30, 20, 0.08));
  background: #f7f3eb;
}

.suggestion-card:active {
  transform: translateY(-1px);
}

.sc-icon { font-size: 1.1rem; margin-bottom: 6px; display: block; }
.sc-title { font-family: var(--font-display, 'Noto Serif SC', serif); font-size: 0.82rem; font-weight: 600; color: var(--color-text); margin-bottom: 2px; }
.sc-desc { font-size: 0.72rem; color: var(--color-text-light, #9B8B7E); line-height: 1.4; }

.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px 12px;
  scroll-behavior: smooth;
}

.chat-area::-webkit-scrollbar { width: 4px; }
.chat-area::-webkit-scrollbar-track { background: transparent; }
.chat-area::-webkit-scrollbar-thumb { background: rgba(74, 53, 38, 0.12); border-radius: 10px; }
.chat-area::-webkit-scrollbar-thumb:hover { background: rgba(74, 53, 38, 0.2); }

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 22px;
  animation: fadeUp 0.3s cubic-bezier(0.4, 0, 0.2, 1) both;
}

.message-row.user {
  flex-direction: row-reverse;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  flex-shrink: 0;
  margin-top: 4px;
}

.msg-avatar.ai {
  background: linear-gradient(135deg, var(--color-accent, #C09A4B), #a88430);
  box-shadow: 0 2px 8px rgba(192, 154, 75, 0.2);
}

.msg-avatar.user {
  background: linear-gradient(135deg, #5C4434, #4A3526);
  color: var(--color-bg-warm, #E5DDD0);
}

.msg-content {
  max-width: 680px;
  min-width: 0;
}

.bubble {
  padding: 14px 18px;
  border-radius: 18px;
  font-size: 0.92rem;
  line-height: 1.75;
  word-break: break-word;
  position: relative;
}

.message-row.ai .bubble {
  background: var(--color-bg-card, #F5F0E8);
  border: 1px solid var(--color-border, rgba(74, 53, 38, 0.08));
  border-top-left-radius: 4px;
  color: var(--color-text);
  box-shadow: 0 1px 4px rgba(44, 30, 20, 0.04);
}

.message-row.user .bubble {
  background: linear-gradient(135deg, #4A3526, #3f2d20);
  color: var(--color-bg-cream, #F8F4ED);
  border-top-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(74, 53, 38, 0.15);
}

.message-row.user .bubble strong {
  color: var(--color-accent, #C09A4B);
}

.msg-time {
  font-size: 0.68rem;
  color: var(--color-text-light, #9B8B7E);
  margin-top: 5px;
  padding: 0 4px;
  opacity: 0.7;
}

.message-row.user .msg-time {
  text-align: right;
}

.typing-dots {
  background: var(--color-bg-card, #F5F0E8);
  border: 1px solid var(--color-border, rgba(74, 53, 38, 0.08));
  border-radius: 18px;
  border-top-left-radius: 4px;
  padding: 16px 22px;
  display: flex;
  gap: 6px;
  align-items: center;
  box-shadow: 0 1px 4px rgba(44, 30, 20, 0.04);
}

.typing-dots span {
  width: 8px;
  height: 8px;
  background: var(--color-accent, #C09A4B);
  border-radius: 50%;
  animation: dotPulse 1.2s ease-in-out infinite;
  opacity: 0.4;
}

.typing-dots span:nth-child(2) { animation-delay: 0.15s; }
.typing-dots span:nth-child(3) { animation-delay: 0.3s; }

@keyframes dotPulse {
  0%, 100% { opacity: 0.35; transform: translateY(0) scale(0.9); }
  50% { opacity: 1; transform: translateY(-4px) scale(1); }
}

.book-list-inline {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
}

.book-card-inline {
  background: var(--color-bg-cream, #F8F4ED);
  border: 1px solid var(--color-border, rgba(74, 53, 38, 0.08));
  border-radius: 14px;
  padding: 14px;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.book-card-inline:hover {
  border-color: rgba(192, 154, 75, 0.25);
  box-shadow: 0 6px 20px var(--color-shadow, rgba(44, 30, 20, 0.06));
  background: #faf6ef;
}

.bci-top {
  display: flex;
  gap: 14px;
  margin-bottom: 10px;
}

.bci-cover {
  width: 60px;
  height: 84px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-weight: 700;
  font-size: 0.5rem;
  color: rgba(237, 230, 214, 0.85);
  padding: 6px;
  text-align: center;
  flex-shrink: 0;
  line-height: 1.3;
}

.bci-cover-text {
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
}

.bci-info {
  flex: 1;
  min-width: 0;
}

.bci-title {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: 2px;
}

.bci-author {
  font-size: 0.76rem;
  color: var(--color-text-light, #9B8B7E);
  margin-bottom: 5px;
}

.bci-rating {
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 3px;
}

.stars { color: var(--color-accent, #C09A4B); letter-spacing: 0.04em; }

.rating-num {
  color: var(--color-text-light, #9B8B7E);
  font-size: 0.75rem;
}

.bci-price {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--color-accent-muted, #A68638);
}

.bci-price .original {
  font-size: 0.7rem;
  color: var(--color-text-light, #9B8B7E);
  text-decoration: line-through;
  font-weight: 400;
  margin-left: 6px;
}

.bci-actions {
  display: flex;
  gap: 8px;
}

.bci-btn {
  padding: 7px 16px;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  gap: 5px;
}

.bci-btn-primary {
  background: linear-gradient(135deg, var(--color-primary, #4A3526), #3f2d20);
  color: var(--color-bg-warm, #E5DDD0);
}

.bci-btn-primary:hover {
  background: linear-gradient(135deg, #5C4434, #4A3526);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--color-shadow-heavy, rgba(44, 30, 20, 0.14));
}

.bci-btn-primary:active {
  transform: translateY(0);
}

.bci-btn-secondary {
  background: transparent;
  color: var(--color-primary, #4A3526);
  border: 1.5px solid var(--color-border, rgba(74, 53, 38, 0.15));
}

.bci-btn-secondary:hover {
  border-color: var(--color-accent, #C09A4B);
  color: var(--color-accent, #C09A4B);
  background: rgba(192, 154, 75, 0.04);
}

.input-area {
  padding: 12px 0 14px;
  flex-shrink: 0;
  border-top: 1px solid var(--color-border, rgba(74, 53, 38, 0.08));
  background: linear-gradient(180deg, transparent, var(--color-bg, #EDE6D6) 8px);
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input-wrap {
  flex: 1;
  position: relative;
}

.input-wrap textarea {
  width: 100%;
  min-height: 48px;
  max-height: 140px;
  padding: 13px 18px;
  background: var(--color-bg-card, #F5F0E8);
  border: 1.5px solid var(--color-border, rgba(74, 53, 38, 0.13));
  border-radius: 14px;
  font-size: 0.92rem;
  font-family: var(--font-body, 'Noto Sans SC', sans-serif);
  color: var(--color-text);
  line-height: 1.6;
  resize: none;
  outline: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}

.input-wrap textarea::placeholder {
  color: var(--color-text-light, #9B8B7E);
}

.input-wrap textarea:focus {
  border-color: var(--color-accent, #C09A4B);
  box-shadow: 0 0 0 3px rgba(192, 154, 75, 0.1), 0 2px 8px rgba(44, 30, 20, 0.04);
  background: #f8f4ed;
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  border: none;
  background: linear-gradient(135deg, var(--color-primary, #4A3526), #3f2d20);
  color: var(--color-bg-warm, #E5DDD0);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  cursor: pointer;
}

.send-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-accent, #C09A4B), #a88430);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(192, 154, 75, 0.3);
}

.send-btn:active:not(:disabled) { transform: scale(0.95); }
.send-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.send-btn svg {
  transition: transform 0.2s;
}

.send-btn:hover:not(:disabled) svg {
  transform: translateX(1px);
}

.input-hint {
  text-align: center;
  font-size: 0.7rem;
  color: var(--color-text-light, #9B8B7E);
  margin-top: 8px;
  opacity: 0.6;
}

.ellipsis {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .ai-page { padding: 0 12px; }
  .page-header { padding: 12px 0 8px; }
  .ph-left h1 { font-size: 1.05rem; }
  .chat-area { padding: 8px 8px 12px; }
  .welcome-icon { width: 48px; height: 48px; font-size: 1.3rem; border-radius: 12px; }
  .welcome-title { font-size: 1.05rem; }
  .welcome-sub { font-size: 0.78rem; }
  .welcome-suggestions { grid-template-columns: 1fr; max-width: 320px; }
  .message-row { margin-bottom: 16px; }
  .message-row.ai .bubble { max-width: 100%; }
  .bci-top { flex-direction: column; align-items: center; text-align: center; }
  .bci-actions { justify-content: center; }
  .input-hint { display: none; }
}
</style>
