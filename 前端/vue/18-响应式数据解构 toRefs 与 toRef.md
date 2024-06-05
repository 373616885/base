### toRefs 与 toRef

toRef 将响应式对象解构出来

```tsx
let name = toRef(user, 'name')
```

toRefs  将响应式所有的属性解构出来

```tsx
let {name,age} = toRefs(user)
```



问题：在项目中 往往一个表单有几十个属性，这时候一般得用 reactive 使数据变成响应式的

```tsx
let user: IUser = reactive({
  id: 1,
  name: '张三',
  age: 18
})

```

需求需要改某个属性的时候，还需要把这个属性保持响应式的

将某个属性直接解构出来直接就改了

```tsx
const user = reactive({
  name: '张三',
  age: 18
})

let name = toRef(user, 'name')

function changeName() {
  // 需要注意解构出来的属性需要加 .value  
  name.value = name.value + '~~'
  // 还需要注意，解构出来的属性变了，原 user 的属性也变了 
  console.log(name.value, user.name)
}
```



完整实例：

```vue
<template>
  <h2>toRef</h2>
  <input type="text" v-model="name" placeholder="请输入名字" /> <br />
  <input type="number" v-model="age" placeholder="请输入年龄" /> <br />
  <input type="button" @click="changeName" value="改变名字" /><br />
  <input type="button" @click="init" value="初始化" /><br />
  <input type="button" @click="changeAge" value="改变年龄" /><br />
</template>

<script setup>
import { reactive, toRef, toRefs } from 'vue'
const user = reactive({
  name: '张三',
  age: 18
})
// 将数据都解构出来
let user2 = toRefs(user)
let {name,age} = toRefs(user)

console.log(user2)
// 解构单属性   
let name = toRef(user, 'name')
let age = toRef(user, 'age')

console.log(name)
console.log(age)

function changeName() {
  name.value = name.value + '~~'
  console.log(name.value, user.name)
}

function changeAge() {
  age.value = age.value + 1
  console.log(age.value, user.age)
}

function init() {
  user2.name.value = 'lisi'
  user2.age.value = 18
}
</script>

```

