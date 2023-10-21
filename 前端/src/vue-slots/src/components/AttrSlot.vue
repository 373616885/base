<template>
  <h3>{{ message }}</h3>
  <slot :msg="childMsg"></slot>
</template>
<script>
export default {
  data() {
    return {
      message: '测试内容',
      childMsg: '子元素的内容'
    }
  }
}
</script>
