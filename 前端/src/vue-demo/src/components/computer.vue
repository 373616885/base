<template>
  <h3>计算属性</h3>
  <h3>{{ qin.name }}</h3>
  <p>{{ qin.age > 30 ? '老年人' : '中年人' }}</p>
  <h3>{{ jie.name }}</h3>
  <!-- 计算属性不用加括号（） -->
  <!-- 计算属性会缓存，多次调用只执行一次 ，只有依赖的属性发生变化了，才会重新计算-->
  <p>{{ isOld }}</p>
  <p>{{ isOld }}</p>
  <p>{{ isOld }}</p>
  <p>{{ isOld }}</p>
  <p>{{ isOld }}</p>

  <h3>{{ peng.name }}</h3>
  <!-- 方法调用加括号（）：一次调用执行一次 -->
  <p>{{ isOldMethods() }}</p>
  <p>{{ isOldMethods() }}</p>
  <p>{{ isOldMethods() }}</p>
  <p>{{ isOldMethods() }}</p>
  <p>{{ isOldMethods() }}</p>

  <!-- 计算属性和方法的区别 -->
  <!-- 计算属性会缓存，多次调用只执行一次 -->
  <!-- 方法一次调用执行一次 -->
</template>
<script>
export default {
  data() {
    return {
      qin: {
        name: 'qin',
        age: 33
      },
      jie: {
        name: 'jie',
        age: 29
      },
      peng: {
        name: 'peng',
        age: 36
      }
    }
  },
  //计算属性：不用写括号
  computed: {
    isOld() {
      return this.jie.age > 30 ? '老年人' : '中年人'
    }
  },
  //方法才用写括号
  methods: {
    isOldMethods() {
      return this.jie.age > 30 ? '老年人' : '中年人'
    }
  }
}
</script>
