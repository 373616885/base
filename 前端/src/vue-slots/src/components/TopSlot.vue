<template>
  <h3>{{ message }}</h3>
  <slot></slot>
</template>
<script>
export default {
  data() {
    return {
      message: '插槽续集知识'
    }
  }
}
</script>
