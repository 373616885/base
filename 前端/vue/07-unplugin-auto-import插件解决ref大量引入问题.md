解决

 `import { ref , reactive ..... } from 'vue'` 

UI 组件的大量引入的问题

大量引入的问题

```shell
npm install -D unplugin-auto-import
npm install -D unplugin-vue-components
```

配置后可以不用引入，直接使用

vite.config.js   

NaiveUi 例子

```js
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import AutoImport from "unplugin-auto-import/vite";
import Components from 'unplugin-vue-components/vite'
import { NaiveUiResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: [
          "vue",
          "vue-router",
          'pinia',
          {
          	'naive-ui': [
                'useDialog',
                'useMessage',
                'useNotification',
                'useLoadingBar'
          	]
          }
      ],
    }),
    Components({
      resolvers: [NaiveUiResolver()]
    })
  ],
});
```

