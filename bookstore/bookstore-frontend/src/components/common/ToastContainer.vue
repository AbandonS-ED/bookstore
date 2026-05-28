<template>
  <div class="toast-container">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        :class="['toast-item', `toast-${toast.type}`, { visible: toast.visible }]"
      >
        <span class="toast-icon">{{ toast.type === 'ok' ? '✓' : '✕' }}</span>
        <span class="toast-msg">{{ toast.message }}</span>
        <div class="toast-progress" :style="{ animationDuration: (toast.duration || 3000) + 'ms' }" />
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
const props = defineProps({
  toasts: { type: Array, default: () => [] }
})
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 20px;
  background: #2B1D14;
  border: 1px solid rgba(192, 154, 75, 0.15);
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(30, 18, 10, 0.3);
  min-width: 300px;
  max-width: 420px;
  transform: translateX(120%);
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  pointer-events: auto;
  overflow: hidden;
  position: relative;
}

.toast-item.visible {
  transform: translateX(0);
}

.toast-icon {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}

.toast-ok .toast-icon {
  background: rgba(92, 136, 86, 0.15);
  color: #6BA368;
}

.toast-error .toast-icon {
  background: rgba(160, 64, 64, 0.15);
  color: #C05050;
}

.toast-msg {
  font-size: 0.88rem;
  color: rgba(237, 230, 214, 0.85);
  font-family: 'Noto Sans SC', sans-serif;
  flex: 1;
}

.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 2px;
  background: linear-gradient(90deg, rgba(192, 154, 75, 0.5), rgba(192, 154, 75, 0.2));
  animation: toastShrink linear forwards;
}

.toast-ok .toast-progress {
  background: linear-gradient(90deg, rgba(92, 136, 86, 0.6), rgba(92, 136, 86, 0.2));
}

.toast-error .toast-progress {
  background: linear-gradient(90deg, rgba(160, 64, 64, 0.6), rgba(160, 64, 64, 0.2));
}

@keyframes toastShrink {
  from { width: 100%; }
  to { width: 0%; }
}

.toast-enter-active { transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94); }
.toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from { transform: translateX(120%); opacity: 0; }
.toast-leave-to { transform: translateX(120%); opacity: 0; }
</style>
