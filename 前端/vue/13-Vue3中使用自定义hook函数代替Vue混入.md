### 自定义hook函数替换Vue混入（mixin）

hooks文件名 一般 以 use 开头，叫做 useStore.js

在 src 下新建一个 hooks 目录专门存放 hooks 函数

```js
import { reactive } from 'vue'

export const store = reactive({
  count: 0,
  increment() {
    this.count++
  }
})

```

hooks 中生命周期与组件中的生命周期执行顺序其实很好判断

就看它们谁的同级生命周期函数先创建那就先执行谁的,比如在 useAdd 中加几个生命周期



### 其实

Vue 3 `Composition API`引入一个 hooks 函数

就像在 vue2 中使用一个 mixin 一样

hooks 函数中的`ref`,`reactive`就相当于 mixin 中的`data`,

hooks 引入一些生命周期函数,watch 等在 mixin 中都有体现





### 使用

```vue
<template>From A: {{ store.count }}</template>

<script setup>
import { store } from '@/hooks/useStore.js'
</script>

```





### useAdd

 hooks 目录专门存放 hooks 函数, useAdd.js

```js
import { ref } from 'vue'
export const useAdd = () => {
  const a = ref(1)
  setInterval(() => {
    a.value++
  }, 1000)
  return a
}

```



### vue 中使用

```vue
<template>
  <div>{{ a }}</div>
</template>

<script setup>
import { useAdd } from '@/hooks/useAdd'
const a = useAdd()
</script>

```



### Hooks 常用 Demo

#### 验证码倒计时

```js
 /**
 *  倒计时
 *  @param {Number} second 倒计时秒数
 *  @return {Number} count 倒计时秒数
 *  @return {Function} countDown 倒计时函数
 *  @example
 *  const { count, countDown } = useCountDown()
 *  countDown(60)
 * <div>{{ count }}</div>
 */
import { ref, onBeforeMount } from 'vue'

export function useCountDown() {
  const count = ref(0);
  const timer = ref(null);
  const countDown = (second, ck) => {
    if (count.value === 0 && timer.value === null) {
      ck();
      count.value = second;
      timer.value = setInterval(() => {
        count.value--;
        if (count.value === 0) clearInterval(timer.value);
      }, 1000);
    }
  };
  onBeforeMount(() => {
    timer.value && clearInterval(timer.value);
  });
  return {
    count,
    countDown,
  };
}
```

#### 使用 

```vue
<template>
  <button :disabled="count!=0" @click="countDown(5,sendCode)">倒计时 {{ count || '' }} </button>
</template>

<script setup>
import {useCountDown} from "@/hooks";
// 倒计时 60 秒，执行 sendCode
const { count, countDown } = useCountDown();
const sendCode = () => {
  console.log("发送验证码");
};
</script>

```





#### 防抖

```js
/**
 * @params {Function} fn  需要防抖的函数 delay 防抖时间
 * @returns {Function} debounce 防抖函数
 * @example
 * const { debounce } = useDebounce()
 * const fn = () => { console.log('防抖') }
 * const debounceFn = debounce(fn, 1000)
 * debounceFn()
 *
 */
export function useDebounce() {
  const debounce = (fn, delay) => {
    let timer = null
    return function () {
      if (timer) {
        console.log('防抖', timer)
        // timer 已经存在就清除
        clearTimeout(timer)
          // 执行重新生成的函数
      }
      timer = setTimeout(() => {
        fn.apply(this, arguments)
      }, delay)
    }
  }
  return { debounce }
}

```



#### 使用

```vue
<template>
	<button @click="debounceClick">防抖点击</button>
</template>

<script setup>
import { useDebounce } from '@/hooks/useDebounce'
// 防抖
const { debounce } = useDebounce()
const fn = () => {
  console.log('点击了哈')
}
const debounceClick = debounce(fn, 5000)
</script>
```



####  节流

```js
/**
 * @params {Function} fn  需要节流的函数 delay 节流时间
 * @returns {Function} throttle 节流函数
 * @example
 * const { throttle } = useThrottle()
 * const fn = () => { console.log('节流') }
 * const throttleFn = throttle(fn, 1000)
 * throttleFn()
 *
 *
 *  */
export function useThrottle() {
  const throttle = (fn, delay) => {
    let timer = null
    return function () {
      // 已经存在了，就不执行
      if (!timer) {
        timer = setTimeout(() => {
          fn.apply(this, arguments)
          timer = null
        }, delay)
      }
    }
  }
  return { throttle }
}

```



#### 使用

```vue
 <template>
	<button @click="throttleClick">节流点击</button>
</template>

<script setup lang="ts">
import { useThrottle} from "@/hooks";
const fn = () => {
   console.log('点击了哈');
}
const { throttle } = useThrottle()
const throttleClick =  throttle(fn,1000)
</script>

```

