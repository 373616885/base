<template>
  <h3>{{ msg }}</h3>
  <h4>v-if、 v-for、v-on 和 v-slot</h4>
  <p v-if="num <= 1">Now you see : 1</p>
  <p v-else-if="num <= 2">Now you see : 2</p>
  <p v-else-if="num <= 3">Now you see : 3</p>
  <p v-else>Now you see : 4</p>
</template>
<script setup>
const msg = '指令是带有 v- 前缀的特殊 attribute'
const num = 1
</script>
