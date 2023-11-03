import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 将 count 保存起来到 Local storage  ,key 为 counter
 */
export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  function increment() {
    count.value++
  }
  function $reset() {
    count.value = 0
  }
  return { count, increment, $reset }
})
