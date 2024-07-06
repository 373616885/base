### 安装

```shell
npm install mockjs --save-dev
```



### 创建配置文件

在 src 下 创建 mock 文件夹下创建一个配置文件`index.ts`

```ts
import Mock from 'mockjs'

Mock.setup({
  // 延迟时间200毫秒
  timeout: '200-600' // 设置响应时间随机范围
})

Mock.mock('/api/login', 'post', {
  code: 200,
  msg: 'ok',
  data: {
    token: '@id'
  }
})

```



### main.ts中添加配置

```ts
// 正式的时候需要注释掉
import '@/mock'
```

