import { ref, onMounted, onUnmounted } from 'vue'

export function useIntersectionObserver(options = {}) {
  const target = ref(null)
  const isVisible = ref(false)

  let observer = null

  onMounted(() => {
    if (!target.value) return
    observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          isVisible.value = true
          if (!options.once === false) {
            observer.unobserve(target.value)
          }
        } else if (!options.once) {
          isVisible.value = false
        }
      },
      {
        threshold: options.threshold || 0.1,
        rootMargin: options.rootMargin || '0px'
      }
    )
    observer.observe(target.value)
  })

  onUnmounted(() => {
    if (observer) observer.disconnect()
  })

  return { target, isVisible }
}
