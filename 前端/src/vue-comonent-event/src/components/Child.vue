<template>
  <h3>{{ message }}</h3>
  <button type="button" @click="clickEventHandle">传递数据</button>
</template>
<script>
export default {
  data() {
    return {
      message: 'Child'
    }
  },
  methods: {
    clickEventHandle() {
      //组件的自定义事件：someEvent
      //$emit 第一个参数是,组件的自定义事件，
      // 第二个参数是，传递的参数数据
      this.$emit('someEvent', this.message)
    }
  }
}
</script>
