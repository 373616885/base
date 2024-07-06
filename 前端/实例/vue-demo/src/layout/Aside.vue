<template>
  <el-menu
    active-text-color="#ffd04b"
    background-color="#545c64"
    class="el-menu-vertical-demo"
    text-color="#fff"
    :default-active="defaultActive"
    router
    unique-opened
    :collapse="hamburgerStore.menuCollapseType"
    :collapse-transition="false"
  >
    <template v-for="menu in menus" :key="menu.id">
      <el-menu-item :index="menu.url" v-if="menu.childMenu.length === 0" @click="saveDefaultActive(menu.url)">
        <el-icon> <Menu /></el-icon>
        <span>{{ $t(`menus.${menu.name as string}`) }}</span>
      </el-menu-item>
      <el-sub-menu :index="menu.id" v-else>
        <template #title>
          <el-icon> <Menu /></el-icon>
          <span>{{ $t(`menus.${menu.name as string}`) }}</span>
        </template>
        <el-menu-item :index="childMenu.url" v-for="childMenu in menu.childMenu" :key="childMenu.id" @click="saveDefaultActive(childMenu.url)">
          <template #title>
            <el-icon> <FolderOpened /></el-icon>
            <span>{{ $t(`menus.${childMenu.name as string}`) }}</span>
          </template>
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup lang="ts">
import { Menu, FolderOpened } from '@element-plus/icons-vue'
import { useHamburgerStore } from '@/stores/hamburger'
import { useAsideStore } from '@/stores/aside'

const hamburgerStore = useHamburgerStore()
const asideStore = useAsideStore()

// 默认激活的菜单
const defaultActive = asideStore.defaultActiveMenu

function saveDefaultActive(url: string) {
  asideStore.saveDefaultActiveMenu(url)
}

const menus = asideStore.menus
</script>

<style scoped>
.el-menu {
  border: none;
}
.el-menu {
  width: 100%;
  height: 100%;
}
</style>
