<template>
  <h3>事件传参</h3>
  <button @click="addCount">add</button>
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
    addCount(e) {
      //读取到data里面的数据的方案：this
      console.log(e)
      console.log(e.target)
      e.target.innerHTML = ' add ' + this.count
      this.count++
    }
  }
}
</script>
