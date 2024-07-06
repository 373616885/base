### 准备一份svg图标集指定目录

在 `src/assets/svg` 下添加 svg 图标

![](images\aad43f3e823a4c82af09a102f0d9c34b.png)





### 下载对应的插件并进行配置

```shell
npm i vite-plugin-svg-icons -D
npm i fast-glob -D
```





### vite.config.js 配置

```ts
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { resolve } from 'path'
 
 createSvgIconsPlugin({
      // 这个是自己配置的图标路径，指出来（自己咋配置的咋来）
      iconDirs: [resolve(process.cwd(), 'src/assets/svg')],
      // 这个表示id，按这个来就对了
      symbolId: 'icon-[dir]-[name]'
})
```

完整配置

```ts
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { resolve } from 'path'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  base: './',
  plugins: [
    vue(),
    createSvgIconsPlugin({
      // 这个是自己配置的图标路径，指出来（自己咋配置的咋来）
      iconDirs: [resolve(process.cwd(), 'src/assets/svg')],
      // 这个表示id，按这个来就对了
      symbolId: 'icon-[dir]-[name]'
    }),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [ElementPlusResolver()]
    }),
    Components({
      resolvers: [ElementPlusResolver()]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
  // server: {
  //   port: 5173,
  //   open: true,
  //   proxy: {
  //     '/api': {
  //       target: 'http://localhost:8080',
  //       changeOrigin: true,
  //       rewrite: (path) => path.replace(/^\/api/, '')
  //     }
  //   }
  // }
})

```



### 创建的 `Svglcon.vue` 组件

在 `src/components` 目录下 创建 `Svglcon.vue` 组件，然后代码如下

```vue
<template>
  <svg class="svg-icon" aria-hidden="true">
    <use :xlink:href="iconName" />
  </svg>
</template>

<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps({
  icon: {
    type: String,
    required: true
  }
})
const iconName = computed(() => `#icon-${props.icon}`)
</script>

<style scoped>
.svg-icon {
  width: 1em;
  height: 1em;
  vertical-align: -0.15em;
  fill: currentColor;
  overflow: hidden;
}
</style>

```



### 最后在 `main.js` 中做以下导入如下

```ts
//这个必须要有
import 'virtual:svg-icons-register'
//自己写的svg组件
import SvgIcon from "./components/SvgIcon.vue"

const app = createApp(App)
//注册组件
app.component("svg-icon",SvgIcon)
```



### 使用

```vue
<template>
  <!-- 使用 -->
  <SvgIcon :icon="iconName" />
  <el-button type="primary" size="large" @click="getInfo">测试</el-button>
  <el-button type="primary" size="large" @click="logout">退出</el-button>
</template>

<script setup lang="ts">
// 引入
import SvgIcon from '@/components/SvgIcon.vue'

import { ref } from 'vue'
   
const iconName = ref('hamburger-opened')

</script>

<style scoped></style>

```

