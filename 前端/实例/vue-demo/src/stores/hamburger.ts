import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useHamburgerStore = defineStore('hamburger', () => {
  const menuCollapseType = ref(false)

  function toggleCollapse() {
    menuCollapseType.value = !menuCollapseType.value
  }

  const iconName = computed(() => {
    return menuCollapseType.value ? 'hamburger-closed' : 'hamburger-opened'
  })

  return { menuCollapseType, toggleCollapse, iconName }
})
