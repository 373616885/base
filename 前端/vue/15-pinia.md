

### Pinia

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
```



### 持久化存储 `pinia-plugin-persistedstate`

由于pinia里没有自带的持久化存储

> https://prazdevs.github.io/pinia-plugin-persistedstate/zh/

```
npm i pinia-plugin-persistedstate
```

```js
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)


```



选项式语法： persist: true

```ts
import { defineStore } from 'pinia'

export const useStore = defineStore('main', {
  state: () => {
    return {
      someState: '你好 pinia',
    }
  },
  persist: true,
})
```

组合式语法： persist: true

```ts
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useStore = defineStore(
  'main',
  () => {
    const someState = ref('你好 pinia')
    return { someState }
  },
  {
    persist: true,
  },
)
```

### 全局持久化配置

```ts
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'

const pinia = createPinia()

pinia.use(
  createPersistedState({
    storage: localStorage,
  })
)

或者
pinia.use(
  createPersistedState({
    storage: sessionStorage,
  })
)
```



### 启用所有 Store 默认持久化

如果想禁止必须配置 `persist: false` 显式禁用持久化

```ts
import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'

const pinia = createPinia()

pinia.use(
  createPersistedState({
    auto: true,
  })
)
```



### 集中式数据管理

在多个组件之间，传递数据（共享数据），刷新会导致数据丢失



### stores

pinia 里面的数据的文件夹

里面的文件名，一般都是和组件名一致

例如你的组件加 ：Login.vue

你的 文件就叫：login.ts

让人一眼就指定你的 数据是 Login这个组件的



#### 声明

有三个概念：`state`、`getter`、`action`，相当于组件中的： `data`、 `computed` 和 `methods`

**count.ts**

```ts
import { ref } from 'vue'
import { defineStore } from 'pinia'

// useCountStore 这个用 hooks 的命名方式
// count 这个用模块的命名方式
export const useCountStore = defineStore('count', {
  // 数据存储的地方
  state() {
    return {
      sum: ref(0),
      school: '达开'
    }
  },
  // 动作
  actions: {
    //加
    increment(value: number) {
      if (this.sum < 10) {
        //操作countStore中的sum
        this.sum += value
      }
    },
    //减
    decrement(value: number) {
      if (this.sum > 1) {
        this.sum -= value
      }
    }
  },
  // 计算
  getters: {
    bigSum: (state): number => state.sum * 10,
    upperSchool(): string {
      return this.school.toUpperCase()
    }
  }
})

```



组合式写法

```ts
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)
  function increment() {
    count.value++
  }
  return { count, doubleCount, increment }
})

```



#### 使用：两种写法 （$state）

countStore.sum)
countStore.$state.sum

```vue
<template>
  <div class="count">
    <h2>第一种写法count：{{ countStore.sum }}</h2>
    <h2>第二种写法count：{{ countStore.$state.sum }}</h2>
    <button @click="add">加</button>
    <button @click="del">减</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useCountStore } from '@/stores/count'
// 变量也遵循 hooks 命名
const countStore = useCountStore()
// 两种写法
console.log(countStore.sum)
console.log(countStore.$state.sum)


const add = () => {
  countStore.sum += n.value
  console.log(countStore.sum)
  console.log(countStore.$state.sum)
}
const del = () => {
  countStore.sum -= n.value
  console.log(countStore.sum)
  console.log(countStore.$state.sum)
}
</script>
```



#### 修改数据(三种方式)

1. 第一种修改方式，直接修改

   ```ts
   countStore.sum = 666
   ```

2. 第二种修改方式：批量修改

   （性能优化）在vue控制台里面的 时间事件里component event发生了一次，上面的发生了多次

   ```ts
   countStore.$patch({
     sum:999,
     school:'atguigu'
   })
   ```

3. 第三种修改方式：借助`action`修改（`action`中一般用于写通用的业务逻辑--例如支付有不同的支付方式）

   ```js
   import { defineStore } from 'pinia'
   
   export const useCountStore = defineStore('count', {
     /*************/
     actions: {
       //加 参数就是接收的参数，使用 this 访问对象
       increment(value:number) {
         if (this.sum < 10) {
           //操作countStore中的sum
           this.sum += value
         }
       },
       //减
       decrement(value:number){
         if(this.sum > 1){
           this.sum -= value
         }
       }
     },
     /*************/
   })
   ```

4. 组件中调用`action`即可

   ```js
   // 使用countStore
   const countStore = useCountStore()
   
   // 调用对应action
   countStore.incrementOdd(n.value)
   ```



	#### getters

```getters```配置

参数会拿到一个 state，通过state拿到数据

或者 this 拿到数据

this 就是state，不写this，可以使用箭头函数

```ts
// 引入defineStore用于创建store
import {defineStore} from 'pinia'

// 定义并暴露一个store
export const useCountStore = defineStore('count',{
  // 动作
  actions:{
    /************/
  },
  // 状态
  state(){
    return {
      sum:1,
      school:'atguigu'
    }
  },
  // 计算
  getters:{
    bigSum:(state):number => state.sum *10,
    upperSchool():string{
      return this. school.toUpperCase()
    }
  }
})
```



读取数据

```ts
const {increment,decrement} = countStore
let {sum,school,bigSum,upperSchool} = storeToRefs(countStore)
```



### storeToRefs

将`storeToRefs`将`store`中的数据转为`ref`对象，方便在模板中使用

直接解构会失去响应式

```ts
let {sum,school,bigSum,upperSchool} =countStore
```

使用storeToRefs 则保持响应式

```ts
let {sum,school,bigSum,upperSchool} = storeToRefs(countStore)
```



如果使用 vue`的`toRefs解构 `store`中数据

会很吓人，里面的很多属性都解构成 ref 引用

```

//只解构 state 里面的数据
console.log(storeToRefs(countStore))
//方法，计算，state的额外属性都被解构了
console.log(ref(countStore))
```







### $subscribe 订阅

通过 store 的 `$subscribe()` 方法侦听 `state` 及其变化

两个参数，state

```ts
talkStore.$subscribe((mutate,state)=>{
  console.log('LoveTalk',mutate,state)
  localStorage.setItem('talk',JSON.stringify(state.talkList))
})

取：加上 || [] 防止 null 对象的情况
talkList = JSON.parse(localStorage.getItem('talk') as string) || '[]'
```





### store组合式写法

必须return

缺点：数据多的时候，return 太多

```ts
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)
  function increment() {
    count.value++
  }
  return { count, doubleCount, increment }
})
```



### 完整实例

```vue
<template>
  <div class="love-talk">
    <n-button type="info" :loading="loading" :render-icon="renderIcon" @click="getInfo">
      获取土味情话
    </n-button>
    <ul>
      <li v-for="l in list" :key="l.id">{{ l.title }}</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { h, ref } from 'vue'
import { NButton } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { MailUnreadOutline } from '@vicons/ionicons5'
import { useTalkStore } from '@/stores/Talk'
const talkStore = useTalkStore()

const loading = ref(false)

let list = talkStore.list

const message = useMessage()
function getInfo() {
  loading.value = !loading.value
  message.info('正在获取土味情话...', {
    closable: true,
    duration: 6000,
    keepAliveOnHover: true,
    onClose: () => {
      console.log('onClose！')
    },
    onLeave: () => {
      console.log('onLeave！')
    }
  })

  talkStore.getLoveTalk()

  setTimeout(() => {
    loading.value = !loading.value
  }, 3000)

  message.success('获取成功！')
}

const renderIcon = () => {
  return h(MailUnreadOutline, null, {
    default: () => h(MailUnreadOutline)
  })
}

talkStore.$subscribe((mutate, state) => {
  console.log('talk', mutate, state)
  localStorage.setItem('talk', JSON.stringify(state.list))
})
</script>

<style scoped>
.love-talk {
  width: 80%;
  overflow: auto;
  margin: auto;
  margin-top: 10px;
  background: skyblue;
  padding: 10px;
  border-radius: 10px;
  font-size: large;
  font-weight: bold;
  color: rgb(80, 21, 190);
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}
</style>

```



```ts
import { reactive } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import { randomLenNum } from '@/utils/Snowflake'

export const useTalkStore = defineStore('talk', () => {
  //加上 || [] 防止 null 对象的情况
  const list = reactive(JSON.parse(localStorage.getItem('talk') as string) || [])

  async function getLoveTalk() {
    const result = await axios.get('https://api.uomg.com/api/rand.qinghua?format=json')

    const obj = { id: randomLenNum(), title: result.data.content }

    console.log(obj)

    list.unshift(obj)
  }

  return { list, getLoveTalk }
})

```

