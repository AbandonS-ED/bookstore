<template>
  <div class="ai-float-widget" :style="widgetStyle">
    <Transition name="float-scale">
      <button
        v-if="!open"
        ref="dragRef"
        class="ai-float-btn"
        :class="{ dragging: dragging }"
        @mousedown.prevent="startDrag"
        @touchstart.prevent="startDrag"
        @click="onBtnClick"
      >
        <span class="afb-avatar">🤖</span>
      </button>
    </Transition>

    <Transition name="float-panel">
      <div v-if="open" class="ai-float-panel">
        <div class="afp-header">
          <div class="afp-h-left">
            <span class="afp-h-icon">🤖</span>
            <span class="afp-h-title">AI 书友</span>
          </div>
          <button class="afp-h-close" @click="closePanel">─</button>
        </div>

        <div ref="chatRef" class="afp-body">
          <div v-if="!hasStarted" class="afp-welcome">
            <div class="afp-w-icon">📖</div>
            <p class="afp-w-text">你好，我是你的 AI 书友～想问什么尽管说！</p>
          </div>

          <div v-for="(msg, i) in messages" :key="i" class="afp-msg" :class="msg.role">
            <div class="afp-msg-avatar">{{ msg.role === 'ai' ? '🤖' : '👤' }}</div>
            <div class="afp-msg-content">
              <div class="afp-bubble">
                <div v-if="msg.role === 'ai' && !msg.content" class="afp-typing">
                  <span></span><span></span><span></span>
                </div>
                <div v-else v-html="formatText(msg.content)"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="afp-input">
          <textarea
            v-model="inputText"
            placeholder="输入消息..."
            rows="1"
            @keydown="onKeydown"
            @input="autoResize"
          ></textarea>
          <button class="afp-send" :disabled="!inputText.trim() || loading" @click="sendMessage">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
          </button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { aiApi } from '@/api/ai'

const open = ref(false)
const hasStarted = ref(false)
const loading = ref(false)
const inputText = ref('')
const messages = ref([])
const chatRef = ref(null)
const dragRef = ref(null)

const pos = ref({ x: 24, y: 24 })
const dragging = ref(false)
const dragStart = ref({ x: 0, y: 0, px: 0, py: 0 })
const clicked = ref(false)

const widgetStyle = computed(() => ({
  right: pos.value.x + 'px',
  bottom: pos.value.y + 'px',
}))

function formatText(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br>')
}

function getTime() {
  const d = new Date()
  return String(d.getHours()).padStart(2, '0') + ':' + String(d.getMinutes()).padStart(2, '0')
}

function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 100) + 'px'
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
      chatRef.value.scrollTop = chatRef.value.scrollHeight
    }
  })
}

function closePanel() {
  open.value = false
}

function onBtnClick() {
  if (!clicked.value) return
  clicked.value = false
  open.value = true
}

function startDrag(e) {
  dragging.value = true
  clicked.value = false
  const ev = e.touches ? e.touches[0] : e
  dragStart.value = {
    x: ev.clientX,
    y: ev.clientY,
    px: pos.value.x,
    py: pos.value.y,
  }
}

function onMove(e) {
  if (!dragging.value) return
  const ev = e.touches ? e.touches[0] : e
  const dx = dragStart.value.x - ev.clientX
  const dy = dragStart.value.y - ev.clientY
  pos.value = {
    x: Math.max(0, dragStart.value.px + dx),
    y: Math.max(0, dragStart.value.py + dy),
  }
}

function onEnd(e) {
  if (!dragging.value) return
  dragging.value = false
  const ev = e.changedTouches ? e.changedTouches[0] : e
  const dx = dragStart.value.x - ev.clientX
  const dy = dragStart.value.y - ev.clientY
  const dist = Math.sqrt(dx * dx + dy * dy)
  if (dist < 10) {
    clicked.value = true
  }
}

onMounted(() => {
  document.addEventListener('mousemove', onMove)
  document.addEventListener('mouseup', onEnd)
  document.addEventListener('touchmove', onMove, { passive: false })
  document.addEventListener('touchend', onEnd)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onMove)
  document.removeEventListener('mouseup', onEnd)
  document.removeEventListener('touchmove', onMove)
  document.removeEventListener('touchend', onEnd)
})

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  hasStarted.value = true
  messages.value.push({ role: 'user', content: text, time: getTime() })
  inputText.value = ''
  loading.value = true
  scrollToBottom()

  const chatMessages = messages.value.map(m => ({ role: m.role, content: m.content }))
  messages.value.push({ role: 'ai', content: '', time: getTime() })
  scrollToBottom()

  try {
    const res = await aiApi.chat(chatMessages)
    const last = messages.value[messages.value.length - 1]
    last.content = res.data?.reply || '抱歉，没有获取到回复'
  } catch (e) {
    const last = messages.value[messages.value.length - 1]
    last.content = '请求失败，请稍后再试'
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-float-widget {
  position: fixed;
  z-index: 9990;
  user-select: none;
}

.ai-float-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, var(--color-accent, #C09A4B), #a88430);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 20px rgba(192, 154, 75, 0.35);
  transition: transform 0.25s, box-shadow 0.25s;
  animation: floatAnim 3s ease-in-out infinite;
}

.ai-float-btn:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 28px rgba(192, 154, 75, 0.45);
}

.ai-float-btn:active {
  transform: scale(0.95);
}

.ai-float-btn.dragging {
  cursor: grabbing;
  animation: none;
  transform: scale(1.05);
}

.afb-avatar {
  font-size: 1.5rem;
}

.ai-float-panel {
  width: 360px;
  height: 520px;
  background: var(--color-bg-card, #F5F0E8);
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(44, 30, 20, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(74, 53, 38, 0.08);
}

.afp-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--color-primary, #4A3526), #3f2d20);
  flex-shrink: 0;
}

.afp-h-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.afp-h-icon {
  font-size: 1.1rem;
}

.afp-h-title {
  font-family: var(--font-display, 'Noto Serif SC', serif);
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--color-bg-warm, #E5DDD0);
}

.afp-h-close {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: none;
  background: rgba(255,255,255,0.08);
  color: rgba(237, 230, 214, 0.6);
  cursor: pointer;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.afp-h-close:hover {
  background: rgba(255,255,255,0.15);
  color: var(--color-bg-warm);
}

.afp-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  scroll-behavior: smooth;
}

.afp-body::-webkit-scrollbar { width: 3px; }
.afp-body::-webkit-scrollbar-track { background: transparent; }
.afp-body::-webkit-scrollbar-thumb { background: rgba(74, 53, 38, 0.1); border-radius: 10px; }

.afp-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 8px;
}

.afp-w-icon {
  font-size: 2rem;
}

.afp-w-text {
  font-size: 0.82rem;
  color: var(--color-text-light, #9B8B7E);
  text-align: center;
  line-height: 1.6;
  max-width: 260px;
}

.afp-msg {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  animation: fadeUp 0.25s ease both;
}

.afp-msg.user {
  flex-direction: row-reverse;
}

.afp-msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.afp-msg.ai .afp-msg-avatar {
  background: linear-gradient(135deg, var(--color-accent, #C09A4B), #a88430);
}

.afp-msg.user .afp-msg-avatar {
  background: linear-gradient(135deg, #5C4434, #4A3526);
}

.afp-msg-content {
  max-width: 260px;
  min-width: 0;
}

.afp-bubble {
  padding: 10px 14px;
  border-radius: 14px;
  font-size: 0.85rem;
  line-height: 1.65;
  word-break: break-word;
}

.afp-msg.ai .afp-bubble {
  background: var(--color-bg, #EDE6D6);
  border: 1px solid rgba(74, 53, 38, 0.06);
  border-top-left-radius: 4px;
  color: var(--color-text);
}

.afp-msg.user .afp-bubble {
  background: linear-gradient(135deg, #4A3526, #3f2d20);
  color: var(--color-bg-cream, #F8F4ED);
  border-top-right-radius: 4px;
}

.afp-typing {
  display: flex;
  gap: 5px;
  align-items: center;
}

.afp-typing span {
  width: 7px;
  height: 7px;
  background: var(--color-accent, #C09A4B);
  border-radius: 50%;
  animation: dotPulse 1.2s ease-in-out infinite;
  opacity: 0.4;
}

.afp-typing span:nth-child(2) { animation-delay: 0.15s; }
.afp-typing span:nth-child(3) { animation-delay: 0.3s; }

.afp-input {
  display: flex;
  gap: 8px;
  padding: 12px 14px;
  border-top: 1px solid rgba(74, 53, 38, 0.06);
  flex-shrink: 0;
  align-items: flex-end;
}

.afp-input textarea {
  flex: 1;
  min-height: 38px;
  max-height: 100px;
  padding: 9px 14px;
  background: var(--color-bg, #EDE6D6);
  border: 1.5px solid rgba(74, 53, 38, 0.1);
  border-radius: 10px;
  font-size: 0.85rem;
  font-family: var(--font-body, 'Noto Sans SC', sans-serif);
  color: var(--color-text);
  line-height: 1.5;
  resize: none;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  box-sizing: border-box;
}

.afp-input textarea::placeholder {
  color: var(--color-text-light, #9B8B7E);
  font-size: 0.82rem;
}

.afp-input textarea:focus {
  border-color: var(--color-accent, #C09A4B);
  box-shadow: 0 0 0 2px rgba(192, 154, 75, 0.1);
}

.afp-send {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, var(--color-primary, #4A3526), #3f2d20);
  color: var(--color-bg-warm, #E5DDD0);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.afp-send:hover:not(:disabled) {
  background: linear-gradient(135deg, var(--color-accent, #C09A4B), #a88430);
  transform: translateY(-1px);
}

.afp-send:active:not(:disabled) { transform: scale(0.92); }
.afp-send:disabled { opacity: 0.35; cursor: not-allowed; }

@keyframes floatAnim {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.35; transform: translateY(0) scale(0.9); }
  50% { opacity: 1; transform: translateY(-3px) scale(1); }
}

.float-scale-enter-active { transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); }
.float-scale-leave-active { transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); }
.float-scale-enter-from, .float-scale-leave-to { opacity: 0; transform: scale(0.5); }

.float-panel-enter-active { transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); }
.float-panel-leave-active { transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); }
.float-panel-enter-from, .float-panel-leave-to { opacity: 0; transform: scale(0.9) translateY(20px); }

@media (max-width: 480px) {
  .ai-float-panel {
    position: fixed;
    inset: 0;
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
}
</style>
