import Mock from 'mockjs'

Mock.setup({
  // 延迟时间200毫秒
  timeout: '200-600' // 设置响应时间随机范围
})

Mock.mock('/api/login', 'post', {
  code: 200,
  msg: null,
  data: {
    token: 'eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjIzNTQzODU2NzMsImlkIjoxfQ.JCz2yPiiVvxKRqNzw906JPFRtKtqdsoEhD_MFIIPY8A',
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
      name: '首页',
      url: '/home',
      seq: 10,
      type: '菜单',
      childMenu: [],
      pid: '-1'
    },
    {
      id: '1815667403843956736',
      name: '用户管理',
      url: '/user',
      seq: 20,
      type: '菜单',
      childMenu: [
        {
          id: '1815669263254386736',
          name: '用户列表',
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
      name: '系统管理',
      url: '/system',
      seq: 30,
      type: '菜单',
      childMenu: [
        {
          id: '1815669700334427488',
          name: '角色管理',
          url: '/system/role',
          seq: 300,
          type: '菜单',
          childMenu: [],
          pid: '1815667436479807488'
        },
        {
          id: '1815670186483587488',
          name: '菜单管理',
          url: '/system/menu',
          seq: 301,
          type: '菜单',
          childMenu: [],
          pid: '1815667436479807488'
        },
        {
          id: '1815670819005627488',
          name: '权限管理',
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
      name: '数据统计',
      url: '/data',
      seq: 40,
      type: '菜单',
      childMenu: [],
      pid: '-1'
    }
  ]
})

Mock.mock('/api/user/query', 'post', {
  code: 200,
  msg: null,
  data: {
    records: [
      {
        id: '34',
        username: 'jpqin-x5',
        name: 'jpqin-x5',
        sex: '男',
        phone: '18697511849',
        email: '372685881@qq.com',
        avatar: './home.jpeg',
        isLock: '正常',
        lastLogin: '2024-08-10 03:05:26',
        registerTime: '2024-08-10 03:05:26',
        updateTime: '2024-08-10 03:05:26'
      },
      {
        id: '32',
        username: 'jpqin-x1',
        name: 'jpqin-x1',
        sex: '男',
        phone: '18697211849',
        email: '372686881@qq.com',
        avatar: './avatar.jpg',
        isLock: '正常',
        lastLogin: '2024-08-10 02:50:35',
        registerTime: '2024-08-10 02:50:35',
        updateTime: '2024-08-10 02:50:35'
      },
      {
        id: '31',
        username: 'jpqin-xx',
        name: 'jpqin-xx',
        sex: '男',
        phone: '18694211849',
        email: '372616881@qq.com',
        avatar: './qin.png',
        isLock: '正常',
        lastLogin: '2024-08-10 02:49:06',
        registerTime: '2024-08-10 02:49:06',
        updateTime: '2024-08-10 02:49:06'
      },
      {
        id: '28',
        username: 'qinjiep',
        name: 'qinjp1',
        sex: '男',
        phone: '18594211849',
        email: '371616881@qq.com',
        avatar: './avatar.webp',
        isLock: '正常',
        lastLogin: '2024-08-10 02:43:14',
        registerTime: '2024-08-10 02:43:14',
        updateTime: '2024-08-10 02:43:14'
      },
      {
        id: '13',
        username: 'qinjiepengs',
        name: 'qinjp',
        sex: '男',
        phone: '18594211848',
        email: '373616888@qq.com',
        avatar: './avatar.jpg',
        isLock: '正常',
        lastLogin: '2024-08-10 02:33:46',
        registerTime: '2024-08-10 02:33:46',
        updateTime: '2024-08-10 02:42:16'
      },
      {
        id: '16',
        username: 'qinjiepeng11',
        name: 'qinjp',
        sex: '男',
        phone: '18594211841',
        email: '373616888qq.com',
        avatar: './avatar.webp',
        isLock: '正常',
        lastLogin: '2024-08-10 02:34:22',
        registerTime: '2024-08-10 02:34:22',
        updateTime: '2024-08-10 02:42:16'
      },
      {
        id: '17',
        username: 'qinjiepeng22',
        name: 'qinjp1',
        sex: '男',
        phone: '18594211842',
        email: '373616888',
        avatar: './qin.png',
        isLock: '正常',
        lastLogin: '2024-08-10 02:34:53',
        registerTime: '2024-08-10 02:34:53',
        updateTime: '2024-08-10 02:42:16'
      },
      {
        id: '3',
        username: 'jpqin',
        name: '覃杰鹏',
        sex: '男',
        phone: '18594201843',
        email: '4522629@qq.com',
        avatar: './home.jpeg',
        isLock: '正常',
        lastLogin: '2024-07-21 23:32:41',
        registerTime: '2024-07-21 23:32:41',
        updateTime: '2024-08-09 03:53:28'
      },
      {
        id: '1',
        username: 'qinjiepeng',
        name: '覃杰鹏',
        sex: '男',
        phone: '18594201848',
        email: '373616885@qq.com',
        avatar: './favicon.ico',
        isLock: '正常',
        lastLogin: '2024-07-21 23:32:41',
        registerTime: '2024-07-21 23:32:41',
        updateTime: '2024-07-21 23:32:41'
      },
      {
        id: '2',
        username: 'qjp',
        name: '覃杰鹏',
        sex: '男',
        phone: '18594201841',
        email: '78802581@qq.com',
        avatar: './avatar.jpg',
        isLock: '正常',
        lastLogin: '2024-07-21 23:32:41',
        registerTime: '2024-07-21 23:32:41',
        updateTime: '2024-07-21 23:32:41'
      }
    ],
    total: 15,
    size: 10,
    current: 1,
    pages: 2
  }
})

Mock.mock('/api/user/lock', 'post', { code: 200, msg: null, data: null })

Mock.mock('/api/user/insert', 'post', { code: 200, msg: null, data: null })

Mock.mock('/api/user/delete', 'post', { code: 200, msg: null, data: null })

Mock.mock('/api/user/update', 'post', { code: 200, msg: null, data: null })
