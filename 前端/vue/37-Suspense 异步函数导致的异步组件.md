### Suspense

-  等待异步组件时渲染一些额外内容，让应用有更好的用户体验 
-  使用步骤： 
   -  异步引入组件或者 setup 里面有 await 
   -  使用`Suspense`包裹组件，并配置好`default` 与 `fallback`
   -  `default` 成功的插槽      `fallback`开始的默认插槽

```tsx
import { defineAsyncComponent,Suspense } from "vue";
const Child = defineAsyncComponent(()=>import('./Child.vue'))
```

```vue
<template>
  <div class="app">
    <h2>我是App组件</h2>
    <Suspense>
      <template v-slot:default>
        <Child/>
      </template>
      <template v-slot:fallback>
        <h2>加载中......</h2>
      </template>
    </Suspense>
  </div>
</template>

<script setup lang="ts" name="App">
  import {Suspense} from 'vue'
  import Child from './Child.vue'
</script>

<style>
  .app {
    background-color: #ddd;
    border-radius: 10px;
    padding: 10px;
    box-shadow: 0 0 10px;
  }
</style>
```



setup 里面有 await 导致整个setup 都是异步的

```vue
<template>
  <div class="child">
    <h2>我是Child组件</h2>
    <h3>当前求和为：{{ sum }}</h3>
  </div>
</template>

<script setup lang="ts">
  import {ref} from 'vue'
  import axios from 'axios'

  let sum = ref(0)
  let {data:{content}} = await axios.get('https://api.uomg.com/api/rand.qinghua?format=json')
  console.log(content)

</script>

<style scoped>
  .child {
    background-color: skyblue;
    border-radius: 10px;
    padding: 10px;
    box-shadow: 0 0 10px;
  }
</style>
```



