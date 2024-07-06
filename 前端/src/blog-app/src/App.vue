<template>
  <h3>{{ message }}</h3>
  <h4>{{ count }}</h4>
  <button @click="increment">点击</button>
  <!-- <div>{{ count2 }}</div>
  <button @click="increment2">点击2</button> -->

  <p v-for="(item, index) of obj" :key="index">{{ item }}</p>

  <p>{{ state.age }}</p>
  <button @click="mutateDeeply">mutateDeeply</button>
</template>
<script setup>
// ref 函数：要么在setup() 返回，要么在模板中使用
import { ref, reactive } from 'vue'

const message = '响应式API'

//ref 可以声明任意类型的响应式数据
const count = ref(0)

const obj = ref({
  nested: { count: 0 },
  arr: ['foo', 'bar']
})
//另一种声明响应式状态的方式reactive()
//不用写 .value
//它只能用于对象类型 (对象、数组和如 Map、Set 这样的集合类型)。
//它不能持有如 string、number 或 boolean 这样的原始类型
const state = reactive({ name: 'qinjp', age: 35 })

// 建议使用 ref() 作为声明响应式状态的主要 API

function increment() {
  // 在 JavaScript 中需要 .value
  count.value++
}

function mutateDeeply() {
  // 以下都会按照期望工作
  obj.value.nested.count++
  obj.value.arr.push('baz')
  state.age++
}
</script>
<!-- <script>
import { ref } from 'vue'
export default {
  data() {
    return {
      message: '响应式API',
      count: 0
    }
  },
  methods: {
    increment() {
      this.count++
    }
  },
  // `setup` 是一个特殊的钩子，专门用于组合式 API。
  setup() {
    const count2 = ref(0)
    console.log(count2)
    console.log(count2.value)
    function increment2() {
      count2.value++
      console.log(count2.value)
    }

    // 将 ref 暴露给模板
    return {
      count2,
      increment2
    }
  }
} 
</script>
-->
