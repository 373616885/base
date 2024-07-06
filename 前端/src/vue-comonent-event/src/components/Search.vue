<template>
  <h3>{{ message }}</h3>
  搜索：<input type="text" v-model="search" />
</template>
<script>
export default {
  data() {
    return {
      message: 'Search页面',
      search: 'Search'
    }
  },
  watch: {
    search(newValue, OldValue) {
      this.$emit('searchEvent', newValue)
    }
  }
}
</script>
