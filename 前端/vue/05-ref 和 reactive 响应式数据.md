### 声明响应式状态

vue2 响应式数据都在data() {} 里面 ，然后在顶层的方法和生命周期钩子中使用 `this`

vue3  响应式数据ref和reactive 

响应式数据：再项目里面，数据需要修改才需要响应式数据，不需要修改的尽量不要做成响应式数据





#### 选项式 API 

使用data选项来声明响应式

然后在顶层的方法和生命周期钩子中使用 `this`

```js
export default {
  data() {
    return {
      count: 1
    }
  },
  methods: {
    increment() {
      this.count++
    }
  },
  // `mounted` 是生命周期钩子，之后我们会讲到
  mounted() {
    // `this` 指向当前组件实例
    console.log(this.count) // => 1

    // 数据属性也可以被更改
    this.count = 2
  }
}
```

注意：

定义 `methods` 时使用箭头函数，因为箭头函数没有自己的 `this` 上下文

```js
export default {
  methods: {
    increment: () => {
      // 反例：无法访问此处的 `this`!
    }
  }
}
```







#### 组合式 API

vue3  响应式数据使用 ref和reactive 

使用 [`ref()`](https://cn.vuejs.org/api/reactivity-core.html#ref) 和 reactive() 函数来声明响应式

注意 ref 函数：要么在setup() 中使用，要么在 <script setup> 中使用



- setup() 中使用

`ref()` 接收参数，并将其包裹在一个带有 `.value` 属性的 RefImpl 对象 

count.value 是值

count 是  RefImpl 对象 



```vue
<template>
  <div>{{ count }}</div>
  <button @click="increment"> {{ count }}</button>
</template>

<script>

import { ref } from 'vue'

export default {
  // `setup` 是一个特殊的钩子，专门用于组合式 API。
  setup() {
    const count = ref(0)
    console.log(count) // { value: 0 }
	console.log(count.value) // 0

	count.value++
	console.log(count.value) // 1
	
    function increment() {
      // 在 JavaScript 中需要 .value
      count.value++
    }

    // 不要忘记同时暴露 increment 函数
    return {
      count,
      increment
    }
  }
}
</script>
```



- 在 <script setup> 中使用： <script setup> 就是用来简化setup()函数的

```vue
<script setup>
import { ref } from 'vue'

const count = ref(0)

function increment() {
  count.value++
}
</script>

<template>
  <button @click="increment">
    {{ count }}
  </button>
</template>
```

ref 对象

```vue
<template>
  <h2>RefDemo2</h2>
  <input type="text" v-model="user.name" /> <br />
  <input type="text" v-model="user.age" /><br />

  <input type="button" @click="changeName" value="修改名字" />
  <input type="button" @click="changeAge" value="修改年龄" />
  <input type="button" @click="init" value="初始化" />
</template>

<script setup>
import { ref } from 'vue'
let user = ref({
  name: '张三',
  age: 18
})

function changeName() {
  user.value.name += '~'
}

function changeAge() {
  user.value.age += 1
}

// 初始化
function init() {
  user.value = {
    name: '张三',
    age: 18
  }
}
</script>


```

注意：使用ref .value 重新分配对象，新对象还保持响应式，reactive 重新分配对象就不可以





还有一种声明响应式状态的方式，即使用 `reactive()` API。与将内部值包装在特殊对象中的 ref 不同，`reactive()` 将使对象本身具有响应性：

```vue
<template>
  <P>{{ count }}</P>
  <button @click="increment">点击</button>
  <P>{{ state.age }}</P>
  <P>{{ state.name }}</P>	
  <button @click="mutateDeeply">mutateDeeply</button>
</template>

<script setup>
import { ref, reactive } from 'vue'
//ref 可以声明任意类型的响应式数据
const count = ref(0)

function increment() {
  // 在 JavaScript 中需要 .value
  count.value++
}

const obj = ref({
  nested: { count: 0 },
  arr: ['foo', 'bar']
})
//另一种声明响应式状态的方式reactive()
//不用写 .value
//它只能用于对象类型 (对象、数组和如 Map、Set 这样的集合类型)。
//它不能持有如 string、number 或 boolean 这样的原始类型
const state = reactive({ name: 'qinjp', age: 35 })
function mutateDeeply() {
  // 以下都会按照期望工作
  obj.value.nested.count++
  obj.value.arr.push('baz')
  state.age++
}
</script>
```

reactive 重新分配对象地址需要使用 Object.assign ，属性的变化直接修改就可以

```vue
<template>
  <h2>ReactiveDemo</h2>
  <input type="text" v-model="user.name" /> <br />
  <input type="text" v-model="user.age" /><br />
  <input type="text" v-model="user.car.brand" /><br />
  <input type="text" v-model="user.car.color" /><br />
  <button @click="changeName">修改名字</button>
  <button @click="changeAge">修改年龄</button>
  <button @click="init">初始化</button>
  <button @click="changeCar">修改汽车信息</button>
</template>

<script setup>
import { reactive, watch } from 'vue'
let user = reactive({
  name: '张三',
  age: 18,
  car: {
    brand: 'BMW',
    color: 'red'
  }
})

function changeName() {
  user.name += '~'
}

function changeAge() {
  user.age += 1
}

// 重新分配对象地址需要使用 Object.assign
function init() {
  Object.assign(user, {
    name: '张三',
    age: 18,
    car: {
      brand: '雅迪电动车',
      color: '绿色'
    }
  })
}
// 属性的变化，没有重新分配地址就可以直接替换
function changeCar() {
  user.car.brand = '爱码电动车'
  user.car.color = '黑色'
}
</script>

```



`reactive()` API 有一些局限性：

**有限的值类型**：它只能用于对象类型 (对象、数组和如 `Map`、`Set` 这样的[集合类型](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects#keyed_collections))。它不能持有如 `string`、`number` 或 `boolean` 这样的[原始类型](https://developer.mozilla.org/en-US/docs/Glossary/Primitive)。





### reactive  内嵌 ref

解构出来不需要 .value

```ts
let user = reactive({
  name: '张三',
  age: ref(18)
})

console.log(user.name)
console.log(user.age) // 这里不需要 .value




```





## 【ref 对比 reactive】

宏观角度看：

> 1. `ref`用来定义：**基本类型数据**、**对象类型数据**；
>
> 2. `reactive`用来定义：**对象类型数据**。

- 区别：

> 1. `ref`创建的变量必须使用`.value`（可以使用`volar`插件自动添加`.value`）。
>
>    <img src="../images/自动补充value.png" alt="自动补充value" style="zoom:50%;border-radius:20px" /> 
>
> 2. `reactive`重新分配一个新对象，会**失去**响应式（可以使用`Object.assign`去整体替换）。
>
>    情况：实际项目中，后台返回一个对象，不能一个一个属性重新赋值（太多太麻烦）
>
>    ```js
>    Object.assign(obj,newObj)
>    Object.assign(car,{name:'小米',price:21})
>    
>    或者不使用reactive
>    使用ref
>    car.value = {name:'小米',price:21} //直接响应式
>    
>    ```
>
>    



使用原则：

1. 若需要一个基本类型的响应式数据，必须使用`ref`。

2. 若需要一个响应式对象，层级不深，`ref`、`reactive`都可以。

   > 层级深的使用 ref 导致 .value 太多，使用 `reactive`可以避免.value 爆炸
   >
   > 还有一个复杂的表单，有几十个属性，如果一个一个ref，那是很蠢的事情

3. 若需要一个响应式对象，且层级较深，推荐使用`reactive`。





### 本质

数据和函数的关联（数据一变，界面跟新，这是其中一种表象）

简单理解：数据变化，函数重新运行

数据一变，界面跟新，其实是数据变化，重新运行了render函数，这个函数重新去渲染视图



哪些数据：

1. 函数用到的数据（读取到某个属性）
2. 读取的对象是响应式的 （ref 或者 reactive）



哪些函数（被监控的函数）：

1. render 函数
2. watch 
3. watchEffect
4. computed



```
const a =ref(0)
//这个函数用到响应式的数据，但是没有被监控起来,就是 a 数字变了 f 没有重新运行
function f(){
	a.value = 2
}
```



### 常见情况（错误）

```vue
<template>
  <h2>{{ count }}</h2>
  <h2>{{ doubleCount }}</h2>
  <button @click="updateCount">updateCount</button>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const count = ref(5)

/** 情况 start **/
// 这个本质 ref(一个数字)
// 没有跟 count 关联起来
const doubleCount = ref(count.value * 2)

function updateCount() {
  count.value++
}
// 修改
watch(count, (newVal) => {
  doubleCount.value = newVal * 2
})   
/** 情况 end **/

    
</script>

<style scoped></style>

```





































