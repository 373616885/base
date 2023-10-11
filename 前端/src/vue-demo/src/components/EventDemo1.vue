<template>
  <h3>内联事件处理器</h3>
  <p>内联事件就是一段有返回值的js代码：实际的应用很少，毕竟有返回值的js代码很少</p>
  <button v-on:dblclick="count++">add</button>
  <button @click="count++">add-two</button>
  <p>count is {{ count }}</p>
</template>

<script>
export default {
  data() {
    return {
      count: 0
    }
  }
}
</script>
