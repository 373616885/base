<template>
  <h3>{{ msg }}</h3>
  <p>{{ str1 }}</p>
  <div>{{ str2 }}</div>
</template>
<script setup>
const msg = '修饰符 Modifiers'
const str1 =
  '修饰符是以点开头的特殊后缀: 例如 .prevent 修饰符会告知 v-on 指令对触发的事件调用 event.preventDefault()：'

const str2 = '<form @submit.prevent="onSubmit">...</form>'
</script>
