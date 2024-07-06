<template>
  <h3>{{ message }}</h3>
  <p>{{ title }}</p>
  <p>{{ msg }}</p>
  <p>{{ age }}</p>
  <ul>
    <li v-for="(item, index) of names" :key="index">{{ item }}</li>
  </ul>
  <p v-for="(item, index) of info" :key="index">{{ item }}</p>
</template>

<script>
export default {
  data() {
    return {
      message: 'Child'
    }
  },
  // 注意：props传递数据只能父级传子级，不能子级传父级
  // 可以传递任何类型
  props: ['title', 'msg', 'age', 'names', 'info']
}
</script>
