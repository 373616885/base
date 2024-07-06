<template>
  <div>{{ a }}</div>

  <button :disabled="count != 0" @click="countDown(60, sendCode)">
    倒计时 {{ count || '' }}
  </button>
  <br />
  <button @click="debounceClick">防抖点击</button>
  <br />
  <button @click="throttleClick">节流点击</button>
</template>

<script setup>
import { useAdd } from '@/hooks/useAdd'
const a = useAdd()

import { useCountDown } from '@/hooks/useCountDown'
// 倒计时
const { count, countDown } = useCountDown()

const sendCode = () => {
  console.log('发送验证码')
}

import { useDebounce } from '@/hooks/useDebounce'
// 防抖
const { debounce } = useDebounce()
const fn = () => {
  console.log('点击了debounce')
}
const debounceClick = debounce(fn, 5000)

import { useThrottle } from '@/hooks/useThrottle'
const fn2 = () => {
  console.log('点击了throttle')
}
const { throttle } = useThrottle()
const throttleClick = throttle(fn2, 1000)
</script>
