<template>
  <h3>{{ message }}</h3>
  <p>搜索的内容：{{ search }}</p>
  <Search @searchEvent="getSearchData" />
  <Data :searchData="search" />
  <ComponentA />
</template>
<script>
import Search from './Search.vue'
import Data from './Data.vue'
import ComponentA from './ComponentA.vue'
export default {
  data() {
    return {
      message: 'Main页面',
      search: 'search',
      msg: ''
    }
  },
  components: {
    Search,
    Data,
    ComponentA
  },
  props: {},
  methods: {
    getSearchData(data) {
      this.search = data
    }
  }
}
</script>
