<template>
  <SvgIcon :icon="iconName" class="screenfull" @click="fullScreenPage" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import screenfull from 'screenfull'

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
    } else {
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
