<template>
  <h3>父元素：{{ message }}</h3>
  <Child @someEvent="gethandle" />
</template>
<script>
import Child from './Child.vue'
// 组件之间传递数据的方案
// 父传子： props
// 子传父： 自定义组件事件： this.$emit
export default {
  data() {
    return {
      message: '组件事件'
    }
  },
  methods: {
    gethandle(data) {
      console.log('拿到子组件传过来的数据', data)
      this.message = data
    }
  },
  components: {
    Child
  }
}
</script>
