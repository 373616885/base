<template>
  <el-dropdown :hide-on-click="false" class="avatar">
    <span class="el-dropdown-link">
      <el-avatar fit="contain" shape="square" :size="40" src="./avatar.webp" />
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
        <el-dropdown-item>个人信息</el-dropdown-item>
        <el-dropdown-item>备&emsp;&emsp;注</el-dropdown-item>
        <el-dropdown-item>修改密码</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useLoginStore } from '@/stores/login'
const router = useRouter()
const LoginStore = useLoginStore()
const logout = () => {
  LoginStore.logout(router)
}
</script>

<style scoped>
/** 下拉菜单 移入会有黑色边框问题 **/
.el-dropdown-link:focus {
  outline: none;
}
:deep(.el-dropdown-menu__item) {
  /** 防止选中 附近的文字 **/
  user-select: none !important;
  /** 阻止元素中的文本换行 **/
  white-space: nowrap;
}
</style>
