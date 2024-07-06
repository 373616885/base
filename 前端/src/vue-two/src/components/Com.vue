<template>
  <h3>计算属性：缓存结果只执行一次</h3>
  <p>{{ str }}</p>
  <p>{{ str }}</p>
  <p>{{ str }}</p>
  <p>{{ str }}</p>
  <p>{{ str }}</p>
  <h3>方法：每次都调用一次</h3>
  <p>{{ fnStr() }}</p>
  <p>{{ fnStr() }}</p>
  <p>{{ fnStr() }}</p>
  <p>{{ fnStr() }}</p>
  <h3>可写计算属性</h3>
  <h4>提供 getter 和 setter 就可以</h4>
  <p>{{ fullName }}</p>
  <button @click="click">click</button>
</template>

<script setup>
import { ref, computed } from 'vue'

const num = ref(1)
const str = computed(() => {
  console.log('计算属性：缓存结果只执行一次')
  return num.value
})
function fnStr() {
  console.log('方法：每次都调用一次')
  return num.value
}
const firstName = ref('John')
const lastName = ref('Doe')

const fullName = computed({
  // getter
  get() {
    return firstName.value + ' ' + lastName.value
  },
  // setter
  set(newValue) {
    ;[firstName.value, lastName.value] = newValue.split(' ')
  }
})

function click() {
  fullName.value = 'qinjie peng'
}
</script>
