### 在 component 上的 ref 属性

component 上的 ref 属性 其实主要配合 defineExpose 来实现子组件向父组件传递数据或者方法

简单理解：

component 上的 ref 属性  相当于 通过 id 获取组件对象，然后才能拿到子组件暴露的内容



#### vue3 写法

父组件：通过 ref 对象拿到 子组件实例对象，然后拿到子组件 defineExpose  暴露的内容

子组件不暴露的父组件拿不到

```vue
<template>
  <h1 ref="title">vue-three</h1>
  <div>
    <RefDemo3 ref="refDemo3" />
    <p>defineExpose暴露内容:{{ name }}- {{ age }}</p>
    <button @click="showRefDemo3">refDemo3-暴露内容</button>
  </div>
</template>

<script setup lang="ts">
    
import RefDemo3 from './components/RefDemo3.vue'
    
import { ref } from 'vue'
// 创建已个ref容器，没有任何参数--名称必须和上面的一致
const title = ref()
console.log(title.value)
// 创建refDemo3 ref对象--名称必须和上面的一致
const refDemo3 = ref()
// 拿到 undefined ，得等到 加载之后才能拿到  onMounted 之后
console.log(refDemo3.value)

let name = ref()
let age = ref()

function showRefDemo3() {
  // ref 对象
  console.log(refDemo3)
  // 获取refDemo3组件的代理对象--就是refDemo3组件实例
  console.log(refDemo3.value)
  // 从refDemo3组件实例里获取子组件暴露的属性
  name.value = refDemo3.value.name
  age.value = refDemo3.value.age
  console.log(name.value, age.value)
}
</script>

<style scoped></style>

```



子组件：通过 defineExpose暴露到子组件实例上，父组件通过 ref 拿到子组件实例，然后拿到暴露的内容

// define 开头的宏函数可以不引用

```vue
<template>
  <h2>子组件使用：使用defineExpose暴露内容</h2>
  <h3>姓名：{{ name }}</h3>
  <h3>年龄：{{ age }}</h3>
</template>
<script setup>
// define 开头的宏函数可以不引用
import { ref, defineExpose } from 'vue'
// 数据
let name = ref('张三')
let age = ref(18)
//defineExpose({ name: name, age: age })
//自动解构成上面的
defineExpose({ name, age })
</script>
<style scoped></style>

```





#### vue2 写法

子组件：通过 expose 暴露到子组件实例上，父组件通过 ref 拿到子组件实例，然后拿到暴露的内容

```vue
<template>
  <h2>子组件使用：使用defineExpose暴露内容</h2>
  <h3>姓名：{{ name }}</h3>
  <h3>年龄：{{ age }}</h3>
</template>
<script>
export default {
  expose: ['name', 'age'],
  data() {
    return {
      name: '张三2',
      age: 99
    }
  }
}
</script>
<style scoped></style>
```



父组件

```vue

<template>
  <RefDemo4 ref="refDemo4" />
  <p>defineExpose暴露内容:{{ name }}- {{ age }}</p>
  <button @click="showRefDemo4">refDemo4-暴露内容</button>
</template>

<script>
import RefDemo4 from './RefDemo4.vue'

export default {
  // vue2必须注册才可以使用-不然下面的不认
  components: {
    RefDemo4
  },
  methods: {
    showRefDemo4() {
      //是 <RefDemo4 /> 组件的实例
      this.name = this.$refs.refDemo4.name
      this.age = this.$refs.refDemo4.age
    }
  },
  data() {
    return {
      name: '--',
      age: 0
    }
  },
  mounted() {
    //是 <RefDemo4 /> 组件的实例
    //this.$refs.refDemo4.name
  }
}
</script>


```







#### 暴露方法

子组件

```vue
<template>  
  <div>  
    <p>Child Component</p>  
    <button @click="exposedMethod">调用公开方法</button>  
  </div>  
</template>  
  
<script setup>  
import { defineExpose } from 'vue';  
  
// 假设这是内部变量  
const internalValue = 'Internal Value';  
  
// 假设这是内部方法，但它并未在模板中使用  
const internalMethod = () => {  
  console.log('Internal Method');  
};  
  
// 公开给父组件的内容  
defineExpose({  
  // 公开属性  
  exposedValue: 'Exposed Value',  
  // 公开方法  
  exposedMethod: () => {  
    console.log('Exposed Method');  
    // 如果需要的话，也可以访问和修改内部变量  
    console.log(internalValue);  
    // 但请注意，在 setup 中，我们通常不直接修改响应式状态之外的变量  
  }  
});  
</script>  
  
<style scoped>  
/* 这里可以添加组件的样式 */  
</style>
```

父组件

```vue
<template>  
  <div>  
    <p>Parent Component</p>  
    <child-component ref="childComponent"></child-component>  
    <button @click="callExposedMethod">调用子组件公开方法</button>  
  </div>  
</template>  
  
<script setup>  
import { ref } from 'vue';  
  
// 使用 ref 来创建对子组件的引用  
const childComponent = ref(null);  
  
// 定义一个方法来调用子组件的 exposedMethod  
const callExposedMethod = () => {  
  // 确保 childComponent 已经被挂载，即不是 null  
  if (childComponent.value) {  
    // 调用子组件的 exposedMethod 方法  
    childComponent.value.exposedMethod();  
  } else {  
    console.error('子组件尚未挂载');  
  }  
};  
</script>  
  
<!-- 如果需要，可以在这里添加子组件的样式 -->  
<style scoped>  
/* 子组件样式 */  
</style>
```

