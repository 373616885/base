<template>
  <h2>{{ golabData }}</h2>
  <h3>{{ message }}</h3>
  <child></child>
</template>
<script>
import Child from './Child.vue'

export default {
  data() {
    return {
      message: '父亲'
    }
  },
  inject: ['golabData'],
  components: {
    Child
  }
}
</script>
