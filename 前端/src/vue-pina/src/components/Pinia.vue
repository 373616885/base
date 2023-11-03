<template>
  <h1>{{ proxy.$store.useStore.useCounterStore().count }}</h1>
  <button @click="handleClick">click</button>
  <br />
  <h1>{{ count }}</h1>
  <button @click="increment">click</button>
  <br />
  <h1>清空数据</h1>
  <button @click="clear">一键清空</button>
  <button @click="clear2">一键清空2</button>
</template>

<script setup>
import { getCurrentInstance, toRef } from 'vue'
import { storeToRefs } from 'pinia'

const { proxy } = getCurrentInstance()

function handleClick() {
  proxy.$store.useStore.useCounterStore().increment()
}

const st = proxy.$store.useStore.useCounterStore()
console.log(st)
// `name` 和 `doubleCount` 是响应式的 ref
// 同时通过插件添加的属性也会被提取为 ref
// 并且会跳过所有的 action 或非响应式 (不是 ref 或 reactive) 的属性
const { count } = storeToRefs(st)
console.log(count)
// 作为 action 的 increment 可以直接解构
const { increment } = st
console.log(increment)

function clear() {
  // 真正清空
  window.localStorage.clear()
}
function clear2() {
  // 重置
  st.$reset()
}
</script>
