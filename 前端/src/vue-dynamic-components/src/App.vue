<template>
  <h3>{{ message }}</h3>
  <component-a></component-a>

  <hr />
  <!-- tabComponent 赋值一定是 字符串  -->
  <!-- 当使用  <component :is 的时候组件会被卸载掉 -->
  <!-- 
    使用 <keep-alive> 组件可以保留组件状态
    <keep-alive> 包裹动态组件时，会缓存不活动的组件实例，
      而不是销毁它们
      例如：如果没有 keep-alive 包裹，切换组件后
           组件的数据会重新加载
   -->
  <keep-alive>
    <component :is="tabComponent"></component>
  </keep-alive>
  <button type="button" @click="tabChange">切换组件</button>

  <hr />

  <AsyncComponent></AsyncComponent>
</template>

<script>
/**
 * 动态组件
 * 像导航栏一样来回切换
 */
import ComponentA from './components/ComponentA.vue'

/**
 * 异步组件需要的时候才加载 Dependency Injection
 * 一般项目都几百个组件，如果全部加载，会非常消耗性能
 * */
import { defineAsyncComponent } from 'vue'

const ComponentB = defineAsyncComponent(() =>
  import('./components/ComponentB.vue')
)

export default {
  data() {
    return {
      message: '动态组件',
      tabComponent: 'ComponentA'
    }
  },
  methods: {
    tabChange() {
      this.tabComponent =
        this.tabComponent === 'ComponentB' ? 'ComponentA' : 'ComponentB'
    }
  },
  components: {
    ComponentA,
    ComponentB
  }
}
</script>
