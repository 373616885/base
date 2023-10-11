<template>
  <div>组件基础组成</div>
  <div class="container">{{ message }}</div>
</template>

<script>
export default {
  data() {
    return {
      message: 'hello world'
    }
  }
}
</script>
<!-- scoped 让当前样式只在当前样式中生效 -->
<style scoped>
.container {
  font-size: 30px;
  color: red;
}
</style>
