import { ref } from 'vue'

const toasts = ref([])
let id = 0

export function useToast() {
  const showToast = (message, type = 'ok', duration = 3000) => {
    const toastId = ++id
    toasts.value.push({ id: toastId, message, type, duration, visible: false })

    requestAnimationFrame(() => {
      const t = toasts.value.find(t => t.id === toastId)
      if (t) t.visible = true
    })

    setTimeout(() => {
      const t = toasts.value.find(t => t.id === toastId)
      if (t) t.visible = false
      setTimeout(() => {
        toasts.value = toasts.value.filter(t => t.id !== toastId)
      }, 300)
    }, duration)
  }

  return { toasts, showToast }
}
