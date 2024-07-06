## Pinia

> [https://pinia.vuejs.org/zh/](https://gitee.com/link?target=https%3A%2F%2Fpinia.vuejs.org%2Fzh%2F)

### 安装

```
npm install pinia
```



### 配置

```js
// pinia
import { createPinia } from 'pinia';
const pinia = createPinia();
app.use(pinia);
// store
// store--引用index.js
//import store from '@/store/index.js'
// 或者简写
import store from '@/store'
// 注册全局变量
app.config.globalProperties.$store = store
```



### 持久化存储 `pinia-plugin-persistedstate`

```
npm i pinia-plugin-persistedstate
```

```js
// 持久化存储
import { createPersistedState } from 'pinia-plugin-persistedstate';
pinia.use(
  createPersistedState({
    auto: true, // 启用所有 Store 默认持久化
  }),
);
```



> 注意： pinia持久化的无法通过一键清空数据
>
> 得通过： window.localStorage.clear()
>
> 正常：写一个重置方法

```
function clear() {
  // 真正清空
  window.localStorage.clear()
}
function clear2() {
  // 重置
  st.$reset()
}
```



### 使用

src 下 store 文件夹 index.js

>  store模块化

```js
// 拿到modules下的所有文件
const modulesFiles = import.meta.globEager('./modules/*.*')
const modules = {}
for (const key in modulesFiles) {
  const moduleName = key.replace(/(.*\/)*([^.]+).*/gi, '$2')
  const value = modulesFiles[key]
  modules[moduleName] = value
}
console.log(modules)
export default modules

```

> main.js

```js

import { createApp } from 'vue'
import App from './App.vue'
const app = createApp(App)
// pinia
import { createPinia } from 'pinia'
const pinia = createPinia()

import { createPersistedState } from 'pinia-plugin-persistedstate'
pinia.use(
  createPersistedState({
    auto: true // 启用所有 Store 默认持久化
  })
)

app.use(pinia)
// store--引用index.js
//import store from '@/store/index.js'
// 或者简写
import store from '@/store'

app.config.globalProperties.$store = store

app.mount('#app')

```

> modules 下 useStore.js

```js
import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 将 count 保存起来到 Local storage  ,key 为 counter
 */
export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  function increment() {
    count.value++
  }
  function $reset() {
    count.value = 0
  }
  return { count, increment, $reset }
})

```



> vue

```vue
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

```



### 





