## readonly()

接受一个对象 (不论是响应式还是普通的) 或是一个 [ref](https://cn.vuejs.org/api/reactivity-core.html#ref)，返回一个原值的只读代理

只读代理是深层的：对任何嵌套属性的访问都将是只读的。

它的 ref 解包行为与 `reactive()` 相同，但解包得到的值是只读的。



- 要避免深层级的转换行为，请使用 [shallowReadonly()](https://cn.vuejs.org/api/reactivity-advanced.html#shallowreadonly) 作替代。



**示例**

```js
const original = reactive({ count: 0 })

const copy = readonly(original)

watchEffect(() => {
  // 用来做响应性追踪
  console.log(copy.count)
})

// 更改源属性会触发其依赖的侦听器
original.count++

// 更改该只读副本将会失败，并会得到一个警告
copy.count++ // warning!
```



常用情况：

```vue
<template>
  <div>readonly</div>
  <h5>sum ：{{ sum }}</h5>
  <h5>sum2 ：{{ sum2 }}</h5>
  <button @click="changSum">加一</button>
  <button @click="changUser">修改user</button>
</template>

<script setup lang="ts">
import { ref, readonly } from 'vue'
const sum = ref(0)
const sum2 = readonly(sum)
function changSum() {
  sum.value++
}
function changUser() {
  console.log(sum2)
}
</script>

<style scoped></style>

```



sum 我自己控制，可以修改，

sum2 暴露给同事可以用，不可修改，

我自己修改sum，sum2也跟着变，防止别人随意修改，但我可以修改

sum：是钱的情况，很多









## shallowReadonly()

[`readonly()`](https://cn.vuejs.org/api/reactivity-core.html#readonly) 的浅层作用形式

和 `readonly()` 不同，这里没有深层级的转换：只有根层级的属性变为了只读。

属性的值都会被原样存储和暴露，这也意味着值为 ref 的属性**不会**被自动解包了。

**示例**

```js
const state = shallowReadonly({
  foo: 1,
  nested: {
    bar: 2
  }
})

// 更改状态自身的属性会失败
state.foo++

// ...但可以更改下层嵌套对象
isReadonly(state.nested) // false

// 这是可以通过的
state.nested.bar++
```



常用情况：

自己可以修改 user1,暴露给别人的只能修改地址

修改 user1，user2也跟着变化

```vue
<template>
  <div>shallowReadonly</div>
  <br />
  <h5>user ：{{ user }}</h5>
  <h5>user2 ：{{ user2 }}</h5>
  <button @click="updateUser">updateUser</button>
  <button @click="updateUser2">updateUser</button>
</template>

<script setup lang="ts">
import { ref, readonly, shallowReadonly } from 'vue'


const user = ref({
  name: 'qinjp',
  age: 36,
  info: {
    address: '中三巷'
  }
})

function updateUser() {
  user.value.info.address += user.value.info.address + '~~~'
}

const user2 = shallowReadonly(user)
function updateUser2() {
  user2.value.info.address += user2.value.info.address + '~~~'
}
</script>

<style scoped></style>

```

