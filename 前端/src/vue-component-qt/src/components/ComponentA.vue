<template>
  <h3>{{ message }}</h3>
  <ComponentB title="标题" :age="age" :names="names" :user="user" />
</template>
<script>
import ComponentB from './ComponentB.vue'

export default {
  data() {
    return {
      message: 'ComponentA',
      age: 38,
      names: ['qin', 'jie', 'peng'],
      user: {}
    }
  },
  components: {
    ComponentB: ComponentB
  }
}
</script>
