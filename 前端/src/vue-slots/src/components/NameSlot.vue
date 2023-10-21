<template>
  <h3>{{ message }}</h3>
  <slot name="head"></slot>
  <p>我是中间的内容</p>
  <slot name="main"></slot>
  <p>我是中间的内容</p>
  <slot name="foot"></slot>
</template>
<script>
export default {
  data() {
    return {
      message: '具体名字插槽'
    }
  }
}
</script>
