<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click.self="close">
        <div class="modal-card" :style="{ maxWidth: width }">
          <div class="modal-header">
            <h3 class="modal-title">{{ title }}</h3>
            <button class="modal-close" @click="close">✕</button>
          </div>
          <div class="modal-body">
            <slot />
          </div>
          <div v-if="$slots.footer" class="modal-footer">
            <slot name="footer" />
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
const props = defineProps({
  visible: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '520px' }
})

const emit = defineEmits(['update:visible'])

const close = () => emit('update:visible', false)
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(28, 18, 12, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 5000;
  padding: 20px;
}

.modal-card {
  width: 100%;
  background: #F5F0E8;
  border-radius: 14px;
  box-shadow: 0 25px 80px rgba(28, 18, 12, 0.4);
  border: 1px solid rgba(74, 53, 38, 0.08);
  overflow: hidden;
  transform: translateY(16px);
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 0;
}

.modal-title {
  font-family: 'Noto Serif SC', serif;
  font-size: 1.1rem;
  font-weight: 700;
  color: #2B1D14;
}

.modal-close {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: #9B8B7E;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: rgba(74, 53, 38, 0.06);
  color: #2B1D14;
}

.modal-body {
  padding: 20px 24px;
}

.modal-footer {
  padding: 0 24px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-enter-active { transition: opacity 0.25s ease; }
.modal-leave-active { transition: opacity 0.2s ease; }
.modal-enter-from, .modal-leave-to { opacity: 0; }

.modal-enter-active .modal-card {
  transition: transform 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.modal-enter-from .modal-card { transform: translateY(16px); }
.modal-leave-to .modal-card { transform: translateY(0); }
</style>
