<template>
  <h1>{{ message }}</h1>
  <parent></parent>
</template>
<script>
import Parent from './components/Parent.vue'
/**
 * 一般情况下：
 *  父组件传子组件使用 props 传递
 *  子组件向父组件通信使用
 *    1. $.emit 触发事件
 *    2. props 传递 Function
 *       然后子组件调用者这个函数（这个函数是父组件传下的可以传参数data）
 *  这种方式有一种情况非常麻烦
 *    1. 需要组件链逐级向下传递（几十层都是少的，大项目几百层都有）
 *  这个问题被称为 prop 逐级透传
 *
 * 依赖注入：就是为了解决这个问题的
 *  provide: 父组件为其后代组件（无论多深的层级）提供依赖数据
 *  inject: 孙子组件注入依赖父组件的数据
 * 静态数据：
 * provide：{
 *   message: '老祖宗的财产'
 * }
 * 想要读取data的数据则可以使用 this 访问
 * provide（）{
 *  return {
 *    message: this.message
 *  }
 * }
 * 全局提供数据：
 * 在
 *
 * 温馨提示：
 *  provide 和 inject 只能父组件向子组件传递数据
 *  不能子组件向父组件传递数据
 *
 *
 *
 * */
export default {
  data() {
    return {
      message: '老祖宗'
    }
  },
  components: {
    Parent
  },
  // provide: {
  //   msg: '老祖宗的财产'
  // }
  provide() {
    return {
      msg: this.message + '的财产!!!'
    }
  }
}
</script>
