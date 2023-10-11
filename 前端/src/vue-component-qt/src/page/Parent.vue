<template>
  <div>
    <h3>{{ message }}</h3>
  </div>
  <!-- 动态传递数据 : -->
  <Child
    title="Parent data"
    :msg="msg"
    :age="age"
    :names="names"
    :info="info"
  />
</template>

<script>
import Child from './Child.vue'
export default {
  data() {
    return {
      message: 'Parent',
      msg: 'qinjp',
      age: 35,
      names: ['qin', 'jie', 'peng'],
      info: {
        name: 'qinjp',
        age: 35,
        adress: '石羊塘'
      }
    }
  },
  components: {
    Child
  }
}
</script>
