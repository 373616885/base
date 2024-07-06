### component 可以实现动态组件

v-if ，v-show 很像，但 v-if，v-show 

 v-if ，v-show 需要指定具体的组件

v-show：还必须在外面嵌套一层



component  可以根据名字动态挂载

```vue
<template>
  <div>APP</div>

  <label><input type="radio" v-model="current" :value="CompA" /> A</label>
  <label><input type="radio" v-model="current" :value="CompB" /> B</label>

  <component :is="current"></component>

  <br />
  <br />
  <br />
  <br />
  <label><input type="radio" v-model="condition" :value="!condition" /> Condition</label>
  <CompA v-if="condition" />
  <div v-else>
    <CompB />
  </div>
</template>

<script setup lang="ts">
import { shallowRef, ref } from 'vue'
import CompA from './components/CompA.vue'
import CompB from './components/CompB.vue'
//必须使用 shallowRef 浅层响应
const current = shallowRef(CompA)

let condition = shallowRef(false)
</script>

<style scoped></style>


```



### 配合 KeepAlive 可以变成 显示隐藏，而不是挂载和卸载

切换组件数据还在，不至于消失

```
<template>
  <div>APP</div>

  <label><input type="radio" v-model="current" :value="CompA" /> A</label>
  <label><input type="radio" v-model="current" :value="CompB" /> B</label>
  <KeepAlive>
    <component :is="current"></component>
  </KeepAlive>
  <br />
  <br />
  <br />
  <br />
  <label><input type="radio" v-model="condition" :value="!condition" /> Condition</label>
  <CompA v-if="condition" />
  <div v-else>
    <CompB />
  </div>
</template>

<script setup lang="ts">
import { shallowRef, ref } from 'vue'
import CompA from './components/CompA.vue'
import CompB from './components/CompB.vue'

const current = shallowRef(CompA)

let condition = ref(false)
</script>

<style scoped></style>



```













