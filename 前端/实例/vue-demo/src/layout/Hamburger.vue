<template>
  <div class="hamburger" @click.prevent.stop="hamburgerStore.toggleCollapse"><SvgIcon :icon="hamburgerStore.iconName" /></div>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/SvgIcon.vue'
import { useHamburgerStore } from '@/stores/hamburger'

const hamburgerStore = useHamburgerStore()
</script>

<style scoped>
.hamburger {
  margin-right: 16px;
  cursor: pointer;
  font-size: 20px;
  overflow: hidden;
  /** 防止选中 附近的文字 **/
  user-select: none;
  height: 100%;
  width: 30px;
  vertical-align: middle;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中，如需要 */
  text-align: center;
  display: flex;
}
</style>
