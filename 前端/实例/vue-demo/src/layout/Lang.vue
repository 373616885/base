<template>
  <el-dropdown @command="switchLangCode">
    <span class="el-dropdown-link">
      <SvgIcon icon="language" class="language" />
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="zh" :disabled="currentLanguage === 'zh'">中文</el-dropdown-item>
        <el-dropdown-item command="en" :disabled="currentLanguage === 'en'">English</el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import SvgIcon from '@/components/SvgIcon.vue'
import { currentLanguage, switchLang, type LangCode } from '@/i18n'

function switchLangCode(val: LangCode) {
  switchLang(val)
}
</script>

<style scoped>
.el-dropdown-link {
  font-size: 20px;
  margin-top: -2px;
}
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
