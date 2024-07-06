`$attrs`是一个对象，包含所有父组件传入的标签属性

解决：通过defineProps一个一个写的麻烦，vue控制台可以看到

> 注意：`$attrs`会自动排除`props`中声明的属性(可以认为声明过的 `props` 被子组件自己“消费”了)

通过函数的形式，父向子传函数，子调用父的函数，在函数里拿到子的数据





父组件：

```vue
<template>
  <div class="father">
    <h3>父组件</h3>
    <h3>a: {{a}}</h3>  
      <!--  v-bind="{x:100,y:200}" 等价 :x=100 :y=200  -->
		<Child :a="a" :b="b" :c="c" :d="d" v-bind="{x:100,y:200}" :updateA="updateA"/>
  </div>
</template>

<script setup lang="ts" name="Father">
	import Child from './Child.vue'
	import { ref } from "vue";
	let a = ref(1)
	let b = ref(2)
	let c = ref(3)
	let d = ref(4)

	function updateA(value){
		a.value += value
	}
</script>
```

子组件：

```vue
<template>
  <div class="child">
    <h3>子组件</h3>
    <h4>x : {{$attrs.x}}</h3>
    <ul>
      <li v-for="(intem, index) in $attrs" :key="index">{{ intem }}</li>
    </ul>
    <button @click="updateAttr">更新A</button>
  </div>
</template>


<script setup lang="ts" name="Child">
import { useAttrs } from 'vue'
//`$attrs`会自动排除`props`中声明的属性
defineProps(['c','d'])
const attrs = useAttrs()

console.log(attrs)
console.log(attrs.y)
    
function updateAttr() {
  if (typeof attrs.updateA === 'function') {
    attrs.updateA(1)
  }
}    
    
</script>

<style scoped>
.child {
  margin-top: 20px;
  background-color: skyblue;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 0 10px black;
}
</style>


```

