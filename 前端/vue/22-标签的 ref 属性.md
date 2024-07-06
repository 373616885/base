### 标签上的 ref

需要直接访问底层 DOM 元素。

要实现这一点，我们可以使用特殊的 `ref` attribute



解决的问题：

解决 vue 中html标签存在的 id 冲突，或者标签冲突



ref 和 响应式的ref 是一个东西

ref 里面的标记是隔开的，局部变量的意思



注意，只有**在组件挂载后**才能访问模板引用。

在初次渲染时会是 `undefined`。这是因为在初次渲染前这个元素还不存在呢！

如果你想在模板中的访问，必须在 onMounted 之后



### vue3 写法

```vue
<template>
  <h3>中国</h3>
  <h4 ref="title">广西</h4>
  <h5>贵港</h5>
  <button @click="show">one-show</button>
</template>
<script setup>
import { ref, onMounted } from 'vue'
// 创建已个ref容器，没有任何参数
const title = ref()

console.log(title.value) // 这里输出 `undefined`

//在生命周期函数中 setup 函数在 beforeCreate  和    Created 之间执行
//由于 setup 函数的执行时间要先于 html 标签的渲染，所以我们不能直接在 setup 函数中初始化 box 标签
onMounted(() => {
  console.log(title.value) // 这里会输出div元素的DOM对象
})
//只有**在组件挂载后**才能访问模板引用。
//在初次渲染时会是 `undefined`。这是因为在初次渲染前这个元素还不存在呢！
//如果你想在模板中的访问，必须在 onMounted 之后
function show() {
  console.log('one-show', title.value)
}
</script>
<style scoped></style>


```



### vue2 写法

使用 `this.$refs` 

```vue
<template>
  <h3>中国</h3>
  <h4 ref="title">广西</h4>
  <h5>贵港</h5>
  <button @click="show">two-show</button>
</template>
<script>
export default {
  methods: {
    show() {
      console.log('two-show', this.$refs.title)
    }
  }
}
</script>
<style scoped></style>

```













