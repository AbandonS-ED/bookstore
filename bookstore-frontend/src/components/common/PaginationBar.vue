<template>
  <div v-if="totalPages > 1" class="pagination-bar">
    <button
      class="page-btn prev"
      :disabled="modelValue <= 1"
      @click="go(modelValue - 1)"
    >
      ‹ 上一页
    </button>

    <button
      v-for="p in pages"
      :key="p"
      :class="['page-btn', { active: p === modelValue, dot: p === '...' }]"
      :disabled="p === '...'"
      @click="p !== '...' && go(p)"
    >
      {{ p === '...' ? '⋯' : p }}
    </button>

    <button
      class="page-btn next"
      :disabled="modelValue >= totalPages"
      @click="go(modelValue + 1)"
    >
      下一页 ›
    </button>

    <span class="page-total">共 {{ totalItems !== undefined ? totalItems + '条' : totalPages + '页' }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Number, default: 1 },
  totalPages: { type: Number, default: 1 },
  totalItems: { type: Number, default: 0 }
})

const emit = defineEmits(['update:modelValue'])

const go = (page) => emit('update:modelValue', page)

const pages = computed(() => {
  const p = []
  const tp = props.totalPages
  const cur = props.modelValue
  if (tp <= 7) {
    for (let i = 1; i <= tp; i++) p.push(i)
  } else if (cur <= 3) {
    for (let i = 1; i <= 5; i++) p.push(i)
    p.push('...', tp)
  } else if (cur >= tp - 2) {
    p.push(1, '...')
    for (let i = tp - 4; i <= tp; i++) p.push(i)
  } else {
    p.push(1, '...')
    for (let i = cur - 1; i <= cur + 1; i++) p.push(i)
    p.push('...', tp)
  }
  return p
})
</script>

<style scoped>
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 48px;
}

.page-btn {
  min-width: 40px;
  height: 40px;
  padding: 0 14px;
  font-size: 0.88rem;
  color: #6B5B4E;
  background: #F5F0E8;
  border: 1px solid rgba(74, 53, 38, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Noto Sans SC', sans-serif;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover:not(:disabled):not(.dot) {
  border-color: #C09A4B;
  color: #C09A4B;
  background: rgba(192, 154, 75, 0.04);
}

.page-btn.active {
  background: #4A3526;
  border-color: #4A3526;
  color: #E5DDD0;
  font-weight: 600;
}

.page-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-btn.dot {
  border: none;
  background: transparent;
  cursor: default;
  color: #9B8B7E;
  min-width: 24px;
  padding: 0;
}

.page-btn.prev,
.page-btn.next {
  font-weight: 500;
}

.page-total {
  font-size: 0.82rem;
  color: #9B8B7E;
  margin-left: 12px;
  font-family: 'Noto Sans SC', sans-serif;
}

@media (max-width: 768px) {
  .pagination-bar {
    flex-wrap: wrap;
    gap: 4px;
  }
  .page-btn {
    min-width: 36px;
    height: 36px;
    font-size: 0.82rem;
    padding: 0 10px;
  }
  .page-total { display: none; }
}
</style>
