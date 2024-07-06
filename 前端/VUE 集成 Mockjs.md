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

Mock.mock('/api/get/token', 'post', {
  code: 200,
  msg: 'ok',
  data: {
    token: '@id'
  }
})

Mock.mock('/api/login', 'post', {
  code: 200,
  msg: null,
  data: {
    token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJxaW4iLCJpZCI6MSwiZXhwIjoxNzIyNTE0NTU3fQ._SlImJcfS55Sl13ylk9h4LPqe1SVt5dAGXmmQsgmPNE',
    refreshToken: '1818621620011352064',
    user: {
      id: '1',
      username: 'qinjiepeng',
      name: '覃杰鹏',
      sex: '男',
      phone: '18594201848',
      email: '373616885@qq.com',
      avatar: null,
      isLock: '正常',
      lastLogin: '2024-07-21 23:32:41',
      registerTime: '2024-07-21 23:32:41',
      updateTime: '2024-07-21 23:32:41'
    }
  }
})

Mock.mock('/api/menu/list', 'post', {
  code: 200,
  msg: null,
  data: [
    {
      id: '1815667362815164416',
      name: '首页1',
      url: '/home',
      seq: 10,
      type: '菜单',
      childMenu: [],
      pid: '-1'
    },
    {
      id: '1815667403843956736',
      name: '用户管理1',
      url: '/user',
      seq: 20,
      type: '菜单',
      childMenu: [
        {
          id: '1815669263254386736',
          name: '用户列表1',
          url: '/user/list',
          seq: 200,
          type: '菜单',
          childMenu: [],
          pid: '1815667403843956736'
        }
      ],
      pid: '-1'
    },
    {
      id: '1815667436479807488',
      name: '系统管理1',
      url: '/system',
      seq: 30,
      type: '菜单',
      childMenu: [
        {
          id: '1815669700334427488',
          name: '角色管理1',
          url: '/system/role',
          seq: 300,
          type: '菜单',
          childMenu: [],
          pid: '1815667436479807488'
        },
        {
          id: '1815670186483587488',
          name: '菜单管理1',
          url: '/system/menu',
          seq: 301,
          type: '菜单',
          childMenu: [],
          pid: '1815667436479807488'
        },
        {
          id: '1815670819005627488',
          name: '权限管理1',
          url: '/system/permission',
          seq: 302,
          type: '菜单',
          childMenu: [],
          pid: '1815667436479807488'
        }
      ],
      pid: '-1'
    },
    {
      id: '1815668165693050880',
      name: '数据统计1',
      url: '/data',
      seq: 40,
      type: '菜单',
      childMenu: [],
      pid: '-1'
    }
  ]
})


```



### main.ts中添加配置

```ts
// 正式的时候需要注释掉
import '@/mock'
```

