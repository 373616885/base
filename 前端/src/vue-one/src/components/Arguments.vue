<template>
  <h3>参数 Arguments</h3>
  <h4>v-bind 指令来绑定 HTML attribute</h4>
  <a v-bind:href="url"> url </a>
  <br />
  <!-- 简写 -->
  <a :href="url"> url </a>
  <h4>v-on 指令来绑定 HTML DOM 事件</h4>
  <a v-on:click="doSomething"> doSomething </a>
  <br />
  <!-- 简写 -->
  <a @click="doSomething"> doSomething </a>

  <h4>动态参数</h4>
  <h4>
    同样在指令参数上也可以使用一个 JavaScript 表达式，需要包含在一对方括号内：
  </h4>
  <p>
    注意，参数表达式有一些约束，动态参数中表达式的值应当是一个字符串，或者是
    null。特殊值 null 意为显式移除该绑定
  </p>
  <a v-bind:[attributeName]="url"> url </a>
  <br />
  <!-- 简写 -->
  <a :[attributeName]="url"> url </a>
  <p>相似地，你还可以将一个函数绑定到动态的事件名称上：</p>
  <a v-on:[eventName]="doSomething"> doSomething </a>
  <!-- 简写 -->
  <a @[eventName]="doSomething">doSomething</a>
  <h4 style="color: red" v-text="tip"></h4>
  <h4 style="color: red" v-text="tip2"></h4>
  <h4 style="color: red" v-text="tip3"></h4>
</template>

<script setup>
const url = 'qinjp.top'
const attributeName = 'href'
const eventName = 'focus'
const tip =
  '特别注意HTML属性要避免在名称中使用大写字母，因为浏览器会强制将其转换为小写'
const tip2 =
  '例如：<a :[someAttr]="value"> ... </a> 会被转变成 <a :[someattr]="value"> ... </a>'
const tip3 =
  '如果你的组件拥有 “someAttr” 属性而非 “someattr”，这段代码将不会工作'
</script>

<style scoped></style>
