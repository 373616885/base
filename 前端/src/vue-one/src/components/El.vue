<template>
  <h3>使用 JavaScript 表达式</h3>

  <p>{{ number + 1 }}</p>

  <p>{{ ok ? 'YES' : 'NO' }}</p>

  <p>{{ message.split('').reverse().join('') }}</p>

  <div :id="`list-${id}`">id</div>

  <p>每个绑定仅支持单一表达式，也就是一段能够被求值的 JavaScript 代码</p>
  <p>一个简单的判断方法是是否可以合法地写在 return 后面</p>
  <!-- 这是一个语句，而非表达式 -->
  <p>这是一个语句，而非表达式</p>
  <span v-text="'{{ var a = 1 }}'"></span>

  <!-- 条件控制也不支持，请使用三元表达式 -->
  <p>条件控制也不支持，请使用三元表达式</p>
  <span v-text="'{{ if (ok) { return message } }}'"></span>

  <!-- 调用函数 -->
  <p>调用函数</p>
  <span>{{ getMsg() }}</span>

  <br />
  <br />
  <br />

  <p>vue语法 `${ }` （模版字符串）</p>
  <p>console.info(`大家好，我叫${name}，今年${age}岁了`) 等价于</p>
  <p>console.info('大家好，我叫' + name + '，今年' + age + '岁了')</p>

  <p :style="`color:${getColor(a.statustext)}`">[ {{ a.statustext }} ]</p>

  <button type="button" @click="next">下一步</button>

  <p>修饰符 Modifiers</p>
  <form @submit.prevent="onSubmit">
    <input type="submit" value="Submit" />
  </form>
</template>
<script setup>
import { ref } from 'vue'

const number = 1
const ok = true
const message = 'qinjp'
const id = 1
const name = 'qinjp'
const age = 35

/**
 * vue语法 `${ }` （模版字符串）
 * console.info(`大家好，我叫${name}，今年${age}岁了`)
 * 等价于
 * console.info('大家好，我叫' + name + '，今年' + age + '岁了')
 * */
console.info('大家好，我叫${name}，今年${age}岁了')
console.info(`大家好，我叫${name}，今年${age}岁了`)
const a = ref({
  statustext: '待受理'
})
function getColor(status) {
  let color
  if (status === '待受理') {
    color = '#2e6be5'
  }
  if (status === '办结') {
    color = '#35b389'
  }
  if (status === '已回复，待评价') {
    color = '#f79042'
  }
  if (status === '退回') {
    color = '#e64545'
  }
  return color
}
let num = 1
function next() {
  console.log(num)
  if (num == 1) {
    a.value.statustext = '待受理'
  } else if (num == 2) {
    a.value.statustext = '办结'
  } else if (num == 3) {
    a.value.statustext = '已回复，待评价'
  } else {
    a.value.statustext = '退回'
  }
  num++
  if (num > 4) {
    num = 1
  }
}

function getMsg() {
  return message
}
</script>
