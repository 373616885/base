## Teleport

- 什么是Teleport？—— Teleport 是一种能够将我们的**组件html结构**移动到指定位置的技术。

```html
<!-- 移动到boby下 -->
<teleport to='body' > 
    <div class="modal" v-show="isShow">
      <h2>我是一个弹窗</h2>
      <p>我是弹窗中的一些内容</p>
      <button @click="isShow = false">关闭弹窗</button>
    </div>
</teleport>
```





```vue
<template>
  <div class="outer">
    <h2>我是App组件</h2>
    <img src="http://www.atguigu.com/images/index_new/logo.png" alt="">
    <br>
    <Modal/>
  </div>
</template>

<script setup lang="ts" name="App">
  import Modal from "./Modal.vue";
</script>

<style>
  .outer{
    background-color: #ddd;
    border-radius: 10px;
    padding: 5px;
    box-shadow: 0 0 10px;
    width: 400px;
    height: 400px;
    filter: saturate(200%);
  }
  img {
    width: 270px;
  }
</style>
```

Modal.vue

在没有 teleport 的情况下

 position: fixed; 正常情况下是已body为父类的

但由于 outer的filter: saturate(200%); 导致  position: fixed 的父类变成了outer

teleport 将 html 结构移到 boby 下， position: fixed 顶层父类一只都是 boby 不受 outer 影响

```vue
<template>
  <button @click="isShow = true">展示弹窗</button>
  <teleport to='body'>
    <div class="modal" v-show="isShow">
      <h2>我是弹窗的标题</h2>
      <p>我是弹窗的内容</p>
      <button @click="isShow = false">关闭弹窗</button>
    </div>
  </teleport>
</template>

<script setup lang="ts" name="Modal">
  import {ref} from 'vue'
  let isShow = ref(false)
</script>

<style scoped>
  .modal {
    width: 200px;
    height: 150px;
    background-color: skyblue;
    border-radius: 10px;
    padding: 5px;
    box-shadow: 0 0 5px;
    text-align: center;
    position: fixed;
    left: 50%;
    top: 20px;
    margin-left: -100px;
  }
</style>
```

