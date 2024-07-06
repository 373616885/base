<template>
  <h3>{{ message }}</h3>
  <p>title : {{ title }}</p>
  <ComponentB :onEvent="dataFn" />
</template>
<script>
import ComponentB from './ComponentB.vue'

export default {
  data() {
    return {
      message: 'ComponentA',
      title: '标题'
    }
  },
  methods: {
    dataFn(data) {
      console.log(data)
      this.title = data
    }
  },
  components: {
    ComponentB
  }
}
</script>
