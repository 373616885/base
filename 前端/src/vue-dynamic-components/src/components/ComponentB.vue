<template>
  <div>
    <h3>{{ msg }}</h3>
  </div>
</template>
<script>
export default {
  data() {
    return {
      msg: '组件B'
    }
  },
  beforeUnmount() {
    console.log('组件B卸载之前')
  },
  unmounted() {
    console.log('组件B卸载了')
  }
}
</script>
