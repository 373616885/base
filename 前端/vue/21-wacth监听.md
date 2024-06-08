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



