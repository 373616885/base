<template>
  <div v-masonry class="container">
    <div
      v-masonry-tile
      v-for="(item, index) in items"
      :key="index"
      :style="{
        width: `${100 / imagePerRow}%`
      }"
    >
      <!-- 你的元素内容 -->
      <el-image :src="item" fit="fill" />
    </div>
  </div>
</template>

<script lang="ts" setup>
const items = [
  // 你的数据项
  'avatar.jpg',
  'avatar.webp',

  'home.jpeg',
  'favicon.png',
  'home.jpeg',
  'qin.ico',
  'avatar.jpg',
  'avatar.webp',
  'home.jpeg',
  'favicon.png',
  'avatar.webp',
  'home.jpeg',
  'avatar.jpg',
  'qin.png',
  'favicon.ico',
  'favicon.png'
]

const imagePerRow = 3 // 每行显示的图片数量
</script>

<style scoped>
.el-image {
  padding: 10px;
}
:deep(.el-image__inner) {
  border-radius: 10px;
}
</style>
