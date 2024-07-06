## watch

- 作用：监视数据的变化（和`Vue2`中的`watch`作用一致）
- 特点：`Vue3`中的`watch`只能监视以下**四种数据**：

> 1. `ref`定义的数据。
> 2. `reactive`定义的数据。
> 3. 函数返回一个值（`getter`函数）。
> 4. 一个包含上述内容的数组。

监视，只监视ref数据，watch 里面不需要	.value

watch 返回值，是已一个 停止函数，解除监视



**我们在`Vue3`中使用`watch`的时候，通常会遇到以下几种情况：**



### 情况一

监视`ref`定义的【基本类型】数据：直接写数据名即可，监视的是其`value`值的改变。



watch 监听一般不关心旧值直接写

const stopWatch2 = watch(sum,(value)=>{    })

watch 返回值，是已一个 停止函数



```vue
<template>
  <div class="person">
    <h1>情况一：监视【ref】定义的【基本类型】数据</h1>
    <h2>当前求和为：{{sum}}</h2>
    <button @click="changeSum">点我sum+1</button>
  </div>
</template>

<script lang="ts" setup name="Person">
  import {ref,watch} from 'vue'
  // 数据
  let sum = ref(0)
  // 方法
  function changeSum(){
    sum.value += 1
  }
  // 监视，情况一：监视【ref】定义的【基本类型】数据
  // 一般情况下，不关心旧值  
  const stopWatch2 = watch(sum,(value)=>{
    console.log('sum变化了',value)
    if(newValue >= 10){
      stopWatch2()
    }
  })
   const stopWatch = watch(sum,(newValue,oldValue)=>{
    console.log('sum变化了',newValue,oldValue)
    if(newValue >= 10){
      stopWatch()
    }
  })
  
  
</script>
```

vue2 写法

```vue
<template>
  <h2>watch-vue2-demo</h2>
  水位： <input type="text" v-model="height" /> <br />
  温度：<input type="text" v-model="temperature" /> <br />
  <button @click="changeHeight">修改高度</button>
  <button @click="changeTemperature">修改温度</button>
</template>

<script>
export default {
  data() {
    return {
      height: 0,
      temperature: 0
    }
  },
  methods: {
    changeHeight() {
      this.height += 10
    },
    changeTemperature() {
      this.temperature += 20
    }
  },
  watch: {
    height(newVal, oldVal) {
      console.log('height', newVal, oldVal)
      if (newVal >= 100) {
        console.log('水位过高')
      }
    },
    temperature(newVal, oldVal) {
      console.log('temperature', newVal, oldVal)
      if (newVal > 80) {
        console.log('温度过高')
      }
    }
  }
}
</script>

<style scoped></style>


```



### 情况二

监视`ref`定义的【对象类型】数据：直接写数据名，监视的是对象的【地址值】

若想监视对象内部的数据，要手动开启深度监视。

```js
/* 
    监视，情况一：监视【ref】定义的【对象类型】数据，监视的是对象的地址值，若想监视对象内部属性的变化，
    需要手动开启深度监视
    watch的第一个参数是：被监视的数据
    watch的第二个参数是：监视的回调
    watch的第三个参数是：配置对象（deep、immediate等等.....） 
  */
watch(person,(newValue)=>{
	console.log('person变化了',newValue)
},{deep:true})
```



注意：

* 若修改的是`ref`定义的对象中的属性，`newValue` 和 `oldValue` 都是新值，因为它们是同一个对象。

* 若修改整个`ref`定义的对象，`newValue` 是新值， `oldValue` 是旧值，因为不是同一个对象了。

* 由于实际开发中，一般不管旧值，直接 watch(person,(newValue)

```vue
<template>
  <h2>watch监听ref对象类型数据</h2>
  <input v-model="user.name" /> <br />
  <input v-model="user.age" /><br />
  <button @click="changeName">改变名字</button><br />
  <button @click="changeAge">改变年龄</button><br />
  <button @click="init">初始化</button><br />
</template>
<script setup>
import test from 'node:test'
import { ref, watch } from 'vue'

let user = ref({
  name: '张三',
  age: 18
})

function changeName() {
  user.value.name += '~'
}

function changeAge() {
  user.value.age += 5
}

function init() {
  user.value = {
    name: 'qinjp',
    age: 35
  }
}

// 监听ref对象类型数据，必须 开启 deep: true,
// newValue, oldValue 在属性变化的时候, 值是相同的
// 只有 user.value = {} 整个对象替换的时候才有新旧的变化
watch(
  user,
  (newValue, oldValue) => {
    console.log('user', newValue, oldValue)
  },
  { deep: true, immediate: true }
)
//() => user.value 这是一个get函数写法
watch(
  () => user.value,
  (newValue, oldValue) => {
    console.log('user.value', newValue, oldValue)
  },
  { deep: true, immediate: true }
)
</script>
<style scoped></style>

```





### 情况三

监视`reactive`定义的【对象类型】数据，且默认开启了深度监视。

`reactive`定义的数据，默认就开启了深度监视，只要属性发生变化就触发wacth

而且不可以关闭，vue3底层就隐式创建了深度监听

```js
// 监视，情况三：监视【reactive】定义的【对象类型】数据，且默认是开启深度监视的,不需要开启深度监听
//  newValue, oldValue 值是相同的  Object.assign 没有改变地址值
watch(person,(newValue,oldValue)=>{
    console.log('person变化了',newValue,oldValue)
})
```

```vue
<template>
  <h2>watch监听reactive对象类型数据</h2>
  <input v-model="user.name" /> <br />
  <input v-model="user.age" /><br />
  <button @click="changeName">改变名字</button><br />
  <button @click="changeAge">改变年龄</button><br />
  <button @click="init">初始化</button><br />
</template>
<script setup>
import test from 'node:test'
import { reactive, watch } from 'vue'

let user = reactive({
  name: '张三',
  age: 18
})
// watch 监听 reactive 对象默认开启深度监听，这里会触发
function changeName() {
  user.name += '~'
}
// watch 监听 reactive 对象默认开启深度监听，这里会触发
function changeAge() {
  user.age += 5
}

function init() {
  Object.assign(user, {
    name: 'qinjp',
    age: 35
  })
}
    
//  newValue, oldValue 值是相同的  Object.assign 没有改变地址值
watch(user, (newVal, oldVal) => {
  console.log('user', newVal, oldVal)
})
</script>
<style scoped></style>

```





### 情况四

监视`ref`或`reactive`定义的【对象类型】数据中的**某个属性**，注意点如下：

1. 若该属性值**不是**【对象类型】，需要写成函数形式。

   一个 有返回值的 get 函数：简写成箭头函数，return 也可以去掉，

   简写：（）=> 属性

2. 若该属性值是**依然**是【对象类型】，可直接写属性对象，也可写成函数，建议写成函数。

   如果 监听对象属性地址，可直接写属性对象，也可写成函数

   直接写属性对象，监视的是地址值

   如果 即监听对象属性地址也监听属性内容变化，必须写成函数，且必须加上 deep:true

   

结论：监视的要是对象里的属性，那么最好写函数式，

注意点：若是对象监视的是地址值，需要关注对象内部，需要手动开启深度监视。

```js
 // 如果修改整个car对象地址改变，就写成函数，如果加上 deep:true，某个属性变化也监听上了
// 加上 deep:true  changeC1 和 changeC2 才能触发
  watch(()=>person.car,(newValue,oldValue)=>{
    console.log('person.car变化了',newValue,oldValue)
  },{deep:true})
```





```vue
<template>
  <div class="person">
    <h1>情况四：监视【ref】或【reactive】定义的【对象类型】数据中的某个属性</h1>
    <h2>姓名：{{ person.name }}</h2>
    <h2>年龄：{{ person.age }}</h2>
    <h2>汽车：{{ person.car.c1 }}、{{ person.car.c2 }}</h2>
    <button @click="changeName">修改名字</button>
    <button @click="changeAge">修改年龄</button>
    <button @click="changeC1">修改第一台车</button>
    <button @click="changeC2">修改第二台车</button>
    <button @click="changeCar">修改整个车</button>
  </div>
</template>

<script lang="ts" setup name="Person">
  import {reactive,watch} from 'vue'

  // 数据
  let person = reactive({
    name:'张三',
    age:18,
    car:{
      c1:'奔驰',
      c2:'宝马'
    }
  })
  // 方法
  function changeName(){
    person.name += '~'
  }
  function changeAge(){
    person.age += 1
  }
  function changeC1(){
    person.car.c1 = '奥迪'
  }
  function changeC2(){
    person.car.c2 = '大众'
  }
  function changeCar(){
    // car 是可以直接修改的
    person.car = {c1:'雅迪',c2:'爱玛'}
  }

  // 监视，情况四：监视响应式对象中的某个属性，且该属性是基本类型的，要写成函数式
  /* 
  watch(()=> {return person.name },(newValue,oldValue)=>{
    console.log('person.name变化了',newValue,oldValue)
  })
   */
  // 由于监听了具体的属性，  newValue,oldValue 是有不同的
  watch(()=> person.name,(newValue,oldValue)=>{
    console.log('person.name变化了',newValue,oldValue)
  })

  // 监视，情况四：监视响应式对象中的某个属性，且该属性是对象类型的，可以直接写，也能写函数，更推荐写函数
  // changeCar 修改整个car对象地址 就不行了  
  watch(person.car,(newValue,oldValue)=>{
    console.log('person.car变化了',newValue,oldValue)
  },{deep:true})
   // 如果修改整个car对象地址改变，就写成函数，如果加上 deep:true，某个属性变化也监听上了
  watch(()=>person.car,(newValue,oldValue)=>{
    console.log('person.car变化了',newValue,oldValue)
  },{deep:true})
</script>
```



### 情况五

监视上述的多个数据

```vue
<template>
  <div class="person">
    <h1>情况五：监视上述的多个数据</h1>
    <h2>姓名：{{ person.name }}</h2>
    <h2>年龄：{{ person.age }}</h2>
    <h2>汽车：{{ person.car.c1 }}、{{ person.car.c2 }}</h2>
    <button @click="changeName">修改名字</button>
    <button @click="changeAge">修改年龄</button>
    <button @click="changeC1">修改第一台车</button>
    <button @click="changeC2">修改第二台车</button>
    <button @click="changeCar">修改整个车</button>
  </div>
</template>

<script lang="ts" setup name="Person">
  import {reactive,watch} from 'vue'

  // 数据
  let person = reactive({
    name:'张三',
    age:18,
    car:{
      c1:'奔驰',
      c2:'宝马'
    }
  })
  // 方法
  function changeName(){
    person.name += '~'
  }
  function changeAge(){
    person.age += 1
  }
  function changeC1(){
    person.car.c1 = '奥迪'
  }
  function changeC2(){
    person.car.c2 = '大众'
  }
  function changeCar(){
    person.car = {c1:'雅迪',c2:'爱玛'}
  }

  // 监视，情况五：监视上述的多个数据 newValue 和oldValue  是一个name和Car的数组  [person.name,person.car]
  watch([()=>person.name,person.car],(newValue,oldValue)=>{
    console.log('person.car变化了',newValue,oldValue)
  },{deep:true})

</script>
```









### 总结

`ref`定义的【对象类型】若想监视对象内部的数据，要手动开启深度监视

直接写 要监视的对象，不需要写成 get函数的形式，但要开启深度监视

监听 ref 对象需要开启 深度监视

注意：

newValue, oldValue 在属性变化的时候, 值是相同的

只有 user.value = {} 整个对象替换的时候才有新旧的变化

```
watch(person,(newValue)=>{
	console.log('person变化了',newValue)
},{deep:true})
```



`reactive`定义的数据，默认就开启了深度监视，只要属性发生变化就触发wacth

也是直接写 要监视的对象，不需要写成 get函数的形式，不需要开启深度监视

```
watch(person,(newValue,oldValue)=>{
	console.log('person变化了',newValue,oldValue)
})
```

监听 reactive 对象不需要开启深度监视





只有要监听对象里的某个属性才需要写成 get 函数的形式

如果监听对象里的某个属性也是对象，才需要开启深度监视

```
 //监听对象person的name是基本类型不需要开启深度监视
 watch(() =>person.name,(newValue,oldValue)=>{
    console.log('person.name变化了',newValue,oldValue)
  })
  //监听对象person的car是对象，需要开启深度监视
 watch(()=>person.car,(newValue,oldValue)=>{
    console.log('person.car变化了',newValue,oldValue)
  },{deep:true})
```



## watchEffect

官网：立即运行一个函数，同时响应式地追踪其依赖，并在依赖更改时重新执行该函数。

`watch`对比`watchEffect`

> 1. 都能监听响应式数据的变化，不同的是监听数据变化的方式不同
> 2. `watch`：要明确指出监视的数据
> 3. `watchEffect`：不用明确指出监视的数据（**函数中用到哪些属性，那就监视哪些属性，没写的就不监视**）。
> 4. 立即运行一个函数，等于 immediate：true

示例代码：

```vue
<template>
  <div class="person">
    <h1>需求：水温达到50℃，或水位达到20cm，则联系服务器</h1>
    <h2 id="demo">水温：{{temp}}</h2>
    <h2>水位：{{height}}</h2>
    <button @click="changePrice">水温+1</button>
    <button @click="changeSum">水位+10</button>
  </div>
</template>

<script lang="ts" setup name="Person">
  import {ref,watch,watchEffect} from 'vue'
  // 数据
  let temp = ref(0)
  let height = ref(0)

  // 方法
  function changePrice(){
    temp.value += 10
  }
  function changeSum(){
    height.value += 1
  }

  // 用watch实现，需要明确的指出要监视：temp、height
  watch([temp,height],(value)=>{
    // 从value中获取最新的temp值、height值
    const [newTemp,newHeight] = value
    // 室温达到50℃，或水位达到20cm，立刻联系服务器
    if(newTemp >= 50 || newHeight >= 20){
      console.log('联系服务器')
    }
  })

  // 用watchEffect实现，不用
  const stopWtach = watchEffect(()=>{
    // 立即运行一个函数，等于 immediate：true
    console.log('watchEffect执行了')
      
    // 室温达到50℃，或水位达到20cm，立刻联系服务器
    if(temp.value >= 50 || height.value >= 20){
      console.log(document.getElementById('demo')?.innerText)
      console.log('联系服务器')
    }
    // 水温达到100，或水位达到50，取消监视
    if(temp.value === 100 || height.value === 50){
      console.log('清理了')
      stopWtach()
    }
  })
</script>
```

