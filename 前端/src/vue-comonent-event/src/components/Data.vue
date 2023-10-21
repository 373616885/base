<template>
  <h3>{{ message }}</h3>
  Data：
  <p>{{ searchData }}</p>
</template>
<script>
export default {
  data() {
    return {
      message: 'Data'
    }
  },
  props: {
    searchData: {
      type: String,
      default: 'qinjp'
    }
  }
}
</script>
