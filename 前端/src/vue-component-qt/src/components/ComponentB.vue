<template>
  <h3>{{ message }}</h3>
  <p>{{ title }}</p>
  <button @click="updateHandle">update</button>
  <p>{{ age }}</p>
  <ul>
    <li v-for="(item, index) of names" :key="index">{{ item }}</li>
  </ul>
  <p v-for="(item, index) in user" :key="index">{{ item }}</p>
</template>
<script>
export default {
  data() {
    return {
      message: 'ComponentB'
    }
  },
  methods: {
    updateHandle() {
      //能读到，但不能修改
      console.log(this.title)
      this.title = '修改'
    }
  },
  props: {
    title: {
      type: [String, Number, Array, Object]
    },
    age: {
      type: Number,
      default: 35
    },
    // 注意数字和字符串可以直接 default
    // 数组和对象必须通过工厂函数返回默认值（函数）
    names: {
      type: Array,
      default() {
        return ['空']
      },
      required: true
    },
    user: {
      type: Object,
      required: true,
      default() {
        return {
          name: 'default',
          age: 'default',
          adress: 'default'
        }
      }
    }
  }
}
</script>
