<template>
  <div ref="container" class="container">{{ content }}</div>
  <button @click="getElemt">获取元素</button>
</template>

<script>
/**
 * 内容改变：{{ 模版语法}}
 * 属性改变： v-bind: 指令
 * 事件：v-on:click
 * 如果没有特别的需求：不要操作DOM
 */
export default {
  data() {
    return {
      content: '内容'
    }
  },
  methods: {
    getElemt() {
      console.log('this.content before', this.content)
      this.$refs.container.innerHTML = '哈哈'
      console.log('this.content after', this.content)
      console.log(' this.$refs.container', this.$refs.container)
    }
  }
}
</script>
