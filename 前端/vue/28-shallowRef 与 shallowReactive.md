### `shallowRef`

1. 作用：创建一个响应式数据，但只对顶层属性进行响应式处理。

2. 用法：

   ```js
   let myVar = shallowRef(initialValue);
   ```

3. 特点：只跟踪引用值的变化，不关心值内部的属性变化。每次都换了个新对象

### `shallowReactive`

1. 作用：创建一个浅层响应式对象，只会使对象的最顶层属性变成响应式的，对象内部的嵌套属性则不会变成响应式的

2. 用法：

   ```js
   const myObj = shallowReactive({ ... });
   ```

3. 特点：对象的顶层属性是响应式的，但嵌套对象的属性不是。





例如修改：

shallowRef 只关注整体修改

shallowReactive 只关注对象的最顶层属性变成响应式的

```vue
<template>
  <div>ShallowRef</div>
  <div>{{ user }}</div>

  <button @click="updateRef">修改ref</button>

  <div>{{ person }}</div>

  <button @click="updateShallowRef">修改updateShallowRef</button>
  <button @click="updateShallowRef2">修改updateShallowRef2</button>

  <div>{{ myobj }}</div>

  <button @click="updateshallowReactive">修改shallowReactive</button>
  <button @click="updateshallowReactive2">修改shallowReactive2</button>
</template>

<script setup lang="ts">
import { ref, shallowRef, shallowReactive } from 'vue'

let user = ref({ name: 'qinjp', age: 36 })

function updateRef() {
  // 可以响应式
  user.value = {
    name: 'lishi',
    age: 30
  }
  // 可以响应式
  user.value.age = 80
}

// 但
let person = shallowRef({ name: 'qinjp', age: 36 })

function updateShallowRef() {
  //只可以修改整个对象 .value 已经是第一层了
  person.value = {
    name: 'lishi',
    age: 60
  }

  // 可以修改第一层
  person.value.name = 'jpqin'
}
function updateShallowRef2() {
  // 可以修改第一层
  person.value.name = 'jpqin'
}

// 不可以修改了
person.value.age = 80

const myobj = shallowReactive({ name: 'qinjp', age: 36, car: { brand: '奔驰', color: '红色' } })

function updateshallowReactive() {
  //可运行修改响应式
  myobj.name = 'jpqin'
  myobj.age = 60
  myobj.car = {
    brand: '小米SU7',
    color: '浅蓝色'
  }
}
function updateshallowReactive2() {
  //不可以修改
  myobj.car.brand = '宝马'
  //不可以修改
  myobj.car.color = '宝马'
}
</script>

<style scoped></style>


```





### 总结

> 通过使用 [`shallowRef()`](https://cn.vuejs.org/api/reactivity-advanced.html#shallowref) 和 [`shallowReactive()`](https://cn.vuejs.org/api/reactivity-advanced.html#shallowreactive) 来绕开深度响应。浅层式 `API` 创建的状态只在其顶层是响应式的，对所有深层的对象不会做任何处理，避免了对每一个内部属性做响应式所带来的性能成本，这使得属性的访问变得更快，可提升性能。



