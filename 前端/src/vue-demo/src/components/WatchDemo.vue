<template>
  <h3>侦听器</h3>
  <p>{{ message }}</p>
  <button v-on:click="updateHandle" type="button">侦听器</button>
  <p>{{ add }}</p>
</template>
<script>
export default {
  // 响应式数据绑定对象
  data() {
    return {
      message: 'hello'
    }
  },
  // 方法（函数）事件对象
  methods: {
    updateHandle() {
      console.log('=============')
      this.message = 'qinjiepeng'
    }
  },
  // 计算属性:一般计算date的值
  computed: {
    add() {
      return this.message.length > 9 ? 'qin' : 'jp'
    }
  },
  watch: {
    // 侦听方法必须和date的保持一致
    // newValue : 新值
    // oldValue : 旧值
    // 函数名必须与侦听的数据对象保持一致
    message(newValue, oldValue) {
      console.log('newValue', newValue)
      console.log('oldValue', oldValue)
    }
  }
}
</script>
