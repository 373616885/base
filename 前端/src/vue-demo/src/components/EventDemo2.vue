<template>
  <h3>方法事件处理器</h3>
  <p>方法事件处理: 基本都是用这个，内联事件处理很少</p>
  <button @dblclick="addCount">add</button>
  <p>count is {{ count }}</p>
</template>

<script>
export default {
  data() {
    return {
      count: 0
    }
  },
  // 所有的方法或者函数都放这里
  methods: {
    addCount() {
      //读取到data里面的数据的方案：this
      console.log(this.count)
      this.count++
    }
  }
}
</script>
