<template>
  <el-config-provider :locale="locale">
    <RouterView />
  </el-config-provider>
</template>

<script lang="ts" setup>
import { ElConfigProvider } from 'element-plus'
import { currentLanguage } from '@/i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

import { computed } from 'vue'

const locale = computed(() => {
  return currentLanguage.value === 'zh' ? zhCn : en
})
</script>

<style scoped></style>
