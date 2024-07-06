### shallowRef

1. 作用：创建一个响应式数据，但只对顶层属性进行响应式处理。

2. 用法：

   ```js
   let myVar = shallowRef(initialValue);
   ```

3. 特点：只跟踪引用值的变化，不关心值内部的属性变化。每次都换了个新对象



### triggerRef

配合 shallowRef 手动跟新视图 

triggerRef() 会强制触发依赖于一个 shallowRef

```
// 手动更新视图
let person = shallowRef({ name: 'qinjp', age: 36 })

const updateShallowRef3 = function () {
  person.value.name = 'jpqin-3'
  triggerRef(person)
}
```



### shallowReactive

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
  <button @click="updateShallowRef3">手动更新视图updateShallowRef3</button>

  <div>{{ myobj }}</div>

  <button @click="updateshallowReactive">修改shallowReactive</button>
  <button @click="updateshallowReactive2">修改shallowReactive2</button>
</template>

<script setup lang="ts">
import { ref, shallowRef, shallowReactive, triggerRef } from 'vue'

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

  // 可以修改
  // 但是一般不会怎么写
  // 也可以理解为bug，想要解决这个问题，只能是在开发时避免这个这种写法
  // 真实： person.value 已经是一个新的 ref 对象，
  // ref的值更新，shallowRef的值也被更新啦
  // 修改ref的值触发了triggerRefValue函数，间接影响到了shallowRef的值，才会使视图更新
  person.value.age = 666
  person.value.name = 'jpqin-1'
}

function updateShallowRef2() {
  // 不可以修改
  person.value.name = 'jpqin-2'
}

// 手动更新视图
const updateShallowRef3 = function () {
  person.value.name = 'jpqin-3'
  triggerRef(person)
}

// BGU说明 可以修改
// 其它数据的更新带动了视图层的更新（其他 ref 的值的改变，间接影响了shallowRef的值）
// 会更新到视图，因为这里视图还未创建
// 所以要想不更新到视图，需要把下面代码放在onMounted(()=>{person.value.age = 8888})事件之后执行。
// 或者开发时避免这个这种写法
person.value.age = 8888

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
>
> shallowRef 有一些难以理解的 bug 
>
> 其他 ref 对象的跟新，会执行 triggerRefValue 函数来，这个函数会跟新视图，间接影响了 shallowRef 
>
> shallowRef 核心是 ref 值变了，不跟新视图，等到  triggerRef 才去跟新视图，其他 ref 对象的跟新执行 triggerRefValue 函数来跟新视图
>
> 所以尽量使用 shallowReactive
>
> 





