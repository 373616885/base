<template>
  <h3>{{ message }}</h3>
  <button type="button" @click="onEventTest">测试</button>
</template>
<script>
export default {
  data() {
    return {
      message: 'ComponentB',
      n: 0
    }
  },
  methods: {
    onEventTest() {
      //执行父组件传下来的方法
      this.onEvent('onEventTest :' + this.n++)
    }
  },
  props: {
    title: String,
    onEvent: Function
  }
}
</script>
