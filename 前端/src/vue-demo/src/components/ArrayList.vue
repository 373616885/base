<template>
  <h3>监听数组变化</h3>
  <button type="button" @click="addhandle">添加数据</button>
  <ul>
    <li v-for="(item, index) of names" :key="index">{{ item }}</li>
  </ul>

  <ul>
    <li v-for="(item, index) of num1" :key="index">{{ item }}</li>
  </ul>
  <ul>
    <li v-for="(item, index) of num2" :key="index">{{ item }}</li>
  </ul>
</template>
<script>
export default {
  data() {
    return {
      names: ['qin', 'jie', 'peng'],
      num1: [1, 2, 3, 4, 5],
      num2: [5, 6, 7, 8]
    }
  },
  methods: {
    addhandle() {
      //引起数组发生变化
      this.names.push('qinjiepeng')
      //不会发生变化，因为concat产生一个新的数组，原数组没有变化
      this.names.concat(['qinjp'])
      console.log(this.names.concat(['qinjp']))
      //想要改变 this.names = this.names.concat(['qinjp'])
      this.num1 = this.num1.concat(this.num2)
    }
  }
}
</script>
