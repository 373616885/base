<template>
  <!-- 这个组件有且仅有一个元素--属性会透传过来，很少用到 -->
  <!-- 常见的class style id 这些属性 -->
  <!-- 当仅有一个单元素为根作为渲染时，透传的 attribute会自动被添加到根元素上 -->
  <h3>{{ message }}</h3>
</template>
<script>
export default {
  data() {
    return {
      message: '属性透传'
    }
  }
}
</script>
<style>
.arr-container {
  color: red;
}
</style>
