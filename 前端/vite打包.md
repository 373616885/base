### 正常情况下

vite 打包都将所有的东西都合并到一起了



### 分开将 modules 分开打包

vite -> esbuild (影响开发环境)

​		-> rollup (影响生产环境)

​				-> manualChunks

例子

```ts
import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import { createSvgIconsPlugin } from 'vite-plugin-svg-icons'
import { resolve } from 'path'

import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

import vue from '@vitejs/plugin-vue'

//console.log(import.meta.env.VITE_NODE_ENV)
console.log(process.env.VITE_NODE_ENV)
console.log(process.env.NODE_ENV)
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
  },

  server: {
    port: 5173,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  //  主要看的就是这个 build 的部分
  build: {
    minify: true, // false = 是设置打包后的文件不压缩，方便查看
    rollupOptions: {
      output: {
        // 打包输出的配置
        manualChunks: (id) => {
          console.log(id)
          // 这个ID，就是所有文件的绝对路径
          if (id.includes('node_modules/element-plus')) {
            return 'element-plus'
          }
          if (id.includes('node_modules/axios')) {
            return 'axios'
          }
          if (id.includes('node_modules/driver')) {
            return 'driver'
          }
          if (id.includes('node_modules/lodash-es')) {
            return 'lodash-es'
          }
          // 其他
          if (id.includes('node_modules')) {
            // 因为 node_modules 中的依赖通常是不会改变的
            // 所以直接单独打包出去
            // 这个return 的值就是打包的名称
            return 'vendor'
          }
        }
      }
    }
  }
})

```





### vite 打包分析

```
npm i rollup-plugin-visualizer -D
```

使用：

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { visualizer } from 'rollup-plugin-visualizer'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],

  build: {
    rollupOptions: {
      plugins: [visualizer()]
    },
  }
})
```



