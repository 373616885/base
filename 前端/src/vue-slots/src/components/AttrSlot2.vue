<template>
  <h3>{{ message }}</h3>
  <slot name="two" :msg2="childMsg"></slot>
  <slot name="three" :msg3="threeMsg"></slot>
</template>
<script>
export default {
  data() {
    return {
      message: '测试2内容',
      childMsg: '子元素2的内容',
      threeMsg: 'qinjp'
    }
  }
}
</script>
