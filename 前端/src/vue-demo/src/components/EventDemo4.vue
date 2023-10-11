<template>
  <h3>事件传参</h3>
  <ul>
    <li @click="getName(item, $event)" v-for="(item, index) in names" :key="index">{{ item }}</li>
  </ul>
</template>

<script>
export default {
  data() {
    return {
      names: ['qin', 'jie', 'peng']
    }
  },
  // 所有的方法或者函数都放这里
  methods: {
    getName(name, e) {
      //读取到data里面的数据的方案：this
      console.log(e)
      console.log(e.target)
      console.log(name)
      console.log(e)
    }
  }
}
</script>
