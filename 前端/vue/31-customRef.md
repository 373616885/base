作用：创建一个自定义的`ref`，并对其依赖项跟踪和更新触发进行逻辑控制。

配合 track （跟踪）和trigger （触发）使用

和计算属性，watch 有点像

```vue
<template>
  <div>
    <h2>{{ msg }}</h2>
    <input type="text" v-model="msg" />
  </div>
</template>

<script setup lang="ts">
import { customRef } from 'vue'

//需求，数据变化后，一秒之后页面才变化
let time
let value = 'init'
let msg = customRef((track, trigger) => {
  return {
    get() {
      track()
      return value
    },
    set(newValue) {
        // setTimeout 顺序不稳定导致 顺序异常
      clearTimeout(time)
      value = newValue
      timer = setTimeout(() => {
        trigger() //通知Vue数据msg变化了
      }, 1000)
    }
  }
})
</script>

<style scoped></style>


```





一般都将 customRef 封装成 hooks

```ts
import {customRef } from "vue";

export default function(initValue:string,delay:number){
  let timer:number
  let msg = customRef((track,trigger)=>{
    return {
      get(){
        track() // 告诉Vue数据msg很重要，要对msg持续关注，一旦变化就更新
        return initValue
      },
      set(value){
        clearTimeout(timer)
        timer = setTimeout(() => {
          initValue = value
          trigger() //通知Vue数据msg变化了
        }, delay);
      }
    }
  }) 
  return {msg}
}
```







