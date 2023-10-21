<template>
  <h3>{{ message }}</h3>
  <slot>
    <!-- 如果啥都没传就渲染这段默认值 -->
    <p>插槽默认值</p>
  </slot>
</template>
<script>
export default {
  data() {
    return {
      message: '插槽默认值知识'
    }
  }
}
</script>
