<template>
  <SvgIcon :icon="iconName" class="screenfull" @click="fullScreenPage" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import screenfull from 'screenfull'
import { useHamburgerStore } from '@/stores/hamburger'

const hamburgerStore = useHamburgerStore()

const iconName = ref('fullscreen')

const fullScreenPage = () => {
  if (screenfull.isEnabled) {
    screenfull.toggle()
  }
}

if (screenfull.isEnabled) {
  screenfull.on('change', () => {
    if (screenfull.isFullscreen) {
      iconName.value = 'exit-fullscreen'
      hamburgerStore.menuCollapseType = true
    } else {
      hamburgerStore.menuCollapseType = false
      iconName.value = 'fullscreen'
    }
  })
}
</script>

<style scoped>
.screenfull {
  font-size: 18px;
  /** 防止选中 附近的文字 **/
  user-select: none;
}
</style>
