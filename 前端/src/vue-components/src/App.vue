<template>
  <!-- 显示组件 -->
  <MyComponent />
  <!-- 显示组件 -->
  <my-component />
  <div class="container">测试</div>
</template>

<script>
// 第一步：引入组件
import MyComponent from './components/MyComponent.vue'
// 第二步：注入组件
export default {
  components: {
    // MyComponent: MyComponent
    // 简写
    MyComponent
  }
}
</script>
