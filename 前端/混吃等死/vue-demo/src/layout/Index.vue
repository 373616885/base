<template>
  <el-container class="app-container">
    <el-aside :width="asideWidth" class="aside-container">
      <Aside />
    </el-aside>
    <el-container class="container" :class="{ hidderContainer: hamburgerStore.menuCollapseType }">
      <el-header><Header /> </el-header>
      <el-main>
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import Aside from '@/layout/Aside.vue'
import Header from '@/layout/Header.vue'
import { computed, onMounted, onUnmounted } from 'vue'
import { useHamburgerStore } from '@/stores/hamburger'
import { throttle } from 'lodash-es'

const hamburgerStore = useHamburgerStore()
const asideWidth = computed(() => {
  return hamburgerStore.menuCollapseType ? '67px' : '200px'
})
// 监听具名函数事件
const resize = throttle(function () {
  if (window.matchMedia('(max-width:1000px)').matches) {
    hamburgerStore.menuCollapseType = true
  } else {
    hamburgerStore.menuCollapseType = false
  }
}, 1000)
onMounted(() => {
  window.addEventListener('resize', resize)
})
onUnmounted(() => {
  window.removeEventListener('resize', resize)
})
</script>

<style scoped>
.app-container {
  width: 100%;
  height: 100%;
}
.aside-container {
  height: 100%;
  position: fixed;
  background-color: #545c64;
}
.container {
  width: calc(100% - 210px);
  height: 100%;
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  transition: all 0.28s;
  &.hidderContainer {
    width: calc(100% - 67px);
  }
}
/** 导航菜单栏折叠和展开会卡顿一秒文字才消失的处理 **/
.el-aside {
  transition: width 0.3s;
  -webkit-transition: width 0.3s;
  -moz-transition: width 0.3s;
  -webkit-transition: width 0.3s;
  -o-transition: width 0.3s;
}
.el-header {
  display: flex;
}
</style>
