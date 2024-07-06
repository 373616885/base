<template>
  <h3>{{ message }}</h3>
  <!-- v-bind 绑定属性 -->
  <div v-bind:id="dynamicId"></div>
  <p>简写语法</p>
  <div :id="dynamicId"></div>
  <p>布尔型 Attribute</p>
  <button :disabled="isButtonDisabled" v-on:click="changeDisabled">
    isButtonDisabled
  </button>
  <p>动态绑定多个值</p>
  <div v-bind="objectOfAttrs">动态绑定多个值</div>
  <button @click="changObj">Button</button>
  <div v-bind="obj">修改id和class属性</div>
</template>
<script setup>
import { ref } from 'vue'
const message = ref('Attribute 绑定')
const dynamicId = ref('container')
const isButtonDisabled = ref(false)
const objectOfAttrs = {
  id: 'container',
  class: 'wrapper'
}
const obj = ref({
  id: 'container',
  class: 'wrapper'
})
let num = 0
function changObj() {
  if (num % 2 == 0) {
    obj.value.id = 'qinjp'
    obj.value.class = 'qinjiepeng'
  } else {
    obj.value.id = 'container'
    obj.value.class = 'wrapper'
  }
  num++
}

function changeDisabled() {
  console.log(isButtonDisabled.value)
  isButtonDisabled.value = !isButtonDisabled.value
}
</script>
<style scoped>
#container {
  font-size: 20px;
}
.wrapper {
  color: red;
}
</style>
