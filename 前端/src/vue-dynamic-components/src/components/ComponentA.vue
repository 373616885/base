<template>
  <div>
    <h3>{{ msg }}</h3>
    <p>{{ str }}</p>
    <button @click="changeStr">改变数据</button>
  </div>
</template>

<script>
export default {
  data() {
    return {
      msg: '组件A',
      str: '老数据'
    }
  },
  methods: {
    changeStr() {
      this.str = '新数据'
    }
  },
  beforeUnmount() {
    console.log('组件A被卸载之前')
  },
  unmounted() {
    console.log('组件A被卸载')
  }
}
</script>
