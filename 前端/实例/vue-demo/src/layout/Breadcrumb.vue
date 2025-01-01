<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="item.path" v-for="(item, index) in breadcrumbList" :key="item.name">
      <span :class="isRedirect(index)" @click="saveDefaultActiveMenu(item.path)"> {{ $t(`menus.${item.name as string}`) }}</span>
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAsideStore } from '@/stores/aside'

const route = useRoute()

//根据路由监听面包屑
const breadcrumbList = computed(() => {
  let currentName = route.name
  if (!currentName || typeof currentName !== 'string') {
    return []
  }
  return route.matched
})

function isRedirect(index: number) {
  return index !== breadcrumbList.value.length - 1 ? 'redirect' : 'no-redirect'
}

//另一种写法
// const breadcrumbList: any = ref([])
// watch(
//   () => route.path,
//   () => {
//     breadcrumbList.value = route.matched
//   },
//   { immediate: true }
// )

function saveDefaultActiveMenu(path: string) {
  const asideStore = useAsideStore()
  if (path === '/') {
    path = '/home'
  }
  asideStore.saveDefaultActiveMenu(path)
}
</script>

<style scoped>
.el-breadcrumb {
  height: 100%;
  display: flex;
  align-items: center;
  vertical-align: middle;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中，如需要 */
  text-align: center;
  /** 防止选中 附近的文字 **/
  user-select: none;
  box-sizing: border-box;
}
.redirect {
  cursor: pointer;
  color: #666;
  font-weight: 600;
  &:hover {
    color: #304156;
  }
}
.no-redirect {
  cursor: text;
  color: #97a8be;
}
</style>
