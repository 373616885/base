### 痛点

使用 ref 进行响应式 数据开发

每次修改数据，读取数据都需要 .value

响应性语法糖  $ref()

### 安装

```js
npm i -D @vue-macros/reactivity-transform
```

### 使用

vite.config.js

引入 

import ReactivityTransform from ' @vue-macros/reactivity-transform'

plugins ： 配置 ReactivityTransform()



```vue
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import ReactivityTransform from ' @vue-macros/reactivity-transform'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    ReactivityTransform(),
    AutoImport({
      imports: ['vue', 'vue-router']
    })
  ],
  build: {
    rollupOptions: {
      output: {
        // manualChunks: {
        //   vue: ['vue']
        // }
        manualChunks(id) {
          console.log(id)
          if (id.includes('node_modules')) {
            return 'vendor'
          }
        }
      }
    }
  }
})

```



### 代码

```vue
	import { $ref } from 'vue/macros'
	let num = $ref(0)
	const add = ()=>{
		// 不需要num.value++
		num++
	}

```

