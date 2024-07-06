<template>
  <h2>{{ golabData }}</h2>
  <h3>{{ message }}</h3>
  <!-- 直接拿依赖的数据 -->
  <p>{{ msg }}</p>
  <!-- 通过data拿依赖的数据 -->
  <p>{{ fullMsg }}</p>
</template>
<script>
export default {
  data() {
    return {
      message: '儿子',
      fullMsg: this.msg
    }
  },
  inject: ['msg', 'golabData']
}
</script>
