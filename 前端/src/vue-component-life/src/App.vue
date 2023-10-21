<template>
  <h1>{{ msg }}</h1>
  <life />
  <button @click="changeData" type="button">点击</button>
</template>
<script>
import life from './components/Life.vue'
/**
 * 生命周期函数
 * 创建期：        beforeCreate  created
 * 挂载期：        beforeMounte  mounted
 * 更新期：        beforeUpdate  updated
 * 销毁期：        beforeUnmount unmounted
 *
 *  创建期 ：组件即将创建
 *  挂载期 ：渲染（页面看的见了）
 *  更新期 ：data数据发生变化，这个过程可能多次循环（不停地发生交互）
 *  销毁期 ：组件将要销毁的时候（组件已经不存在了）
 *
 */
export default {
  data() {
    return {
      num: 0,
      msg: '数据'
    }
  },
  methods: {
    changeData() {
      this.num++
      this.msg = '数据' + +this.num
    }
  },
  beforeCreate() {
    console.log('组件创建之前')
  },
  created() {
    console.log('组件创建之后')
  },
  beforeMount() {
    console.log('组件渲染之前')
  },
  mounted() {
    console.log('组件渲染之后')
  },
  beforeUpdate() {
    console.log('数据更新之前')
  },
  updated() {
    console.log('数据更新之后')
  },
  beforeUnmount() {
    console.log('组件销毁之前')
  },
  unmounted() {
    console.log('组件销毁之后')
  },
  components: {
    life
  }
}
</script>
