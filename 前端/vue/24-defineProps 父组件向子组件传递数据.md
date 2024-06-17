### defineProps 

props 属性的意思，暴露属性给子组件

接收父类component传过来的值

单向数据流，所有的 props 都遵循着**单向绑定**原则，props 因父组件的更新而变化，不会逆向传递

这避免了子组件意外修改父组件的状态的情况，不然应用的数据流将很容易变得混乱而难以理解

所以**不应该**在子组件中去更改一个 prop。若你这么做了，Vue 会在控制台上向你抛出警告

但是，如果是 对象 / 数组类型的 props，修改内部是无法感知的，实践中，应该尽可能避免这样的更改



用法：

```vue
<template>
  <h3>msg : {{ msg }}</h3>
  <h3>content : {{ content }}</h3>
  <h3>post:{{ id }}-{{ title }}</h3>
  <h3>c:{{ c }}</h3>
  <h3>a:{{ a }}</h3>
  <h3>b:{{ b }}</h3>
  <h3>c:{{ c }}</h3>
  <ul>
    <li>老大：{{ user.id }} - {{ user.name }} - {{ user.age }}</li>
    <li v-for="item in list" :key="item.id">{{ item.name }}</li>
  </ul>
</template>

<script setup lang="ts">
import { type IUser } from '@/types'
//只接收 msg    
const x = defineProps(['msg'])
//第一种写法 ：这种写法，如果传错了，没有任何警告    
//接收父组件传过来的值 
const x = defineProps(['msg', 'content', 'user', 'a', 'b', 'c', 'list'])
// post 是 一个对象绑定多个 prop
// 得要分开获取 id 和 title


//第二种写法：如果传入的值不满足类型要求，控制台中抛出警告来提醒使用者
// 接收父类component传过来的值
const x = defineProps({
  msg: String,
  content: String,
  id: [String, Number],
  title: String,
  user: {
    type: Object as () => IUser,
    default: () => ({ id: '1', name: 'qinjp', age: 36 }),
    required: true
  },
  a: {
    type: Number,
    default: 100,
    required: false
  },
  b: {
    type: String,
    default: 100,
    required: false
  },
  c: {
    type: [String, Number],
    default: 100,
    required: false
  },
  list: {
    type: Array<IUser>,
    default: () => [{ id: '0', name: 'qinjp', age: 36 }],
    required: true
  }
})
    
const name = 'child'
//暴露给父类，父类通过ref获取组件实例，拿到这个属性
defineExpose({ name })

console.log(x)
</script>
```



父组件

```vue
<template>
  <h2>{{ title }}</h2>
  <!-- 根据一个变量的值动态传入  :content= 变量 -->
  <!-- 根据一个变量的值动态传入  msg= 常量 -->
  <!-- 根据ref获取组件代理对象,子组件通过defineExpose暴露   ref=常量 -->
  <!-- 组件里也可以通过 v-bind 来绑定 会被解构成 :id="post.id" :title="post.title"  -->
  <Child
    msg="父组件数据-msg"
    :content="content"
    :a="1 + 1"
    b="1 + 1"
    c="c"
    :list="userList"
    :user="userList[0]"
    v-bind="post"
    ref="rc"
  />
  <button @click="show">show</button>
  <p>{{ showMsg }}</p>
</template>
<script setup lang="ts">
import { ref, type Ref } from 'vue'
import Child from './components/Child.vue'
import { type IUser } from '@/types'

const title = '父组件向子组件传数据:props'
const content = '父组件数据-content'

let userList: Ref<IUser[]> = ref([
  { id: '1001', name: '张三', age: 35 },
  { id: '1002', name: '李四', age: 35 },
  { id: '1003', name: '王五', age: 35 }
])

const rc = ref()
const showMsg = ref('')
function show() {
  // 获取子组件的属性
  showMsg.value = rc.value.name
  console.log(rc.value.name)
}

const post = {
  id: 1,
  title: 'My Journey with Vue'
}
</script>
<style scoped></style>

```





[基于类型的 prop 声明](https://cn.vuejs.org/api/sfc-script-setup.html#type-only-props-emit-declarations)  TS 类型声明

如果使用了 ts ，defineProps 最好声明一下类型：

```js
const props = defineProps<{ msg: string ,count?: number}>()
```



注意`defineProps<{ msg: string }>` 会被编译为 `{ msg: { type: String, required: true }}`  

在 ts 使用 ？ 号表示是否可传可不传   
withDefaults 使用默认值    , withDefaults 是宏函数，可以不用引入

```vue
<template>
  <div>
    <ul>
      <li v-for="item in list" :key="item.id">
        {{ item.name }}--{{ item.age }}
      </li>
    </ul>
  </div>
</template>

<script lang="ts" setup name="Child2">
import { defineProps,withDefaults } from 'vue'
import { type IUsers } from '@/types'

// 第一种写法：仅接收
//const users = defineProps(['list'])

// 第二种写法：接收+限制类型
// 在 ts 使用 ？ 号表示是否可传可不传    
// const users = defineProps<{ list: IUsers }>()

// 第三种写法：接收+限制类型+指定默认值+限制必要性
// 在 ts 使用 ？ 号表示是否可传可不传   
// withDefaults 使用默认值    
let users = withDefaults(defineProps<{ list?: IUsers }>(), {
  // 这里不能写直接写数组，写 一个函数 返回 对象的形式
  list: () => [{ id: 'asdasg01', name: '小猪佩奇', age: 18 }]
})
console.log(users)
</script>

```



types/index.ts

```ts
export interface IUser {
  id: string
  name: string
  age?: number // ? 可选
}
// 暴露数组类型的两种写法
export type IUsers = IUser[]
export type IUsers2 = Array<IUser>

export interface IPost {
  id: number
  title: string
}

```

