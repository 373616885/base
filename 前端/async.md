## fetch 使用

```js
// async 在修饰function 表示这函数为异步函数:返回值为 Promise的函数
async function f() {
  // await 异步函数里调用异步函数：只能在async函数中使用
  // await表面上是会等待，但实际上 cpu 会被让出来
  // 如果要使用 await 就必须在 async 函数中，需要先定义一个async函数
  // async await 就可以不用 Promise 这和繁琐的对象
  const responseA = await fetch(
    "https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"
  )
    .then((res) => res.json())
    .then((data) => {
      console.log("responseA", data);
    })
    .catch((error) => {
      console.error("error", error);
    })
    .finally(() => {
      console.log("responseA end");
    });
  // await 异步等待结果
  const responseB = await fetch(
    "https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"
  );
  // await 异步等待结果
  const json = await responseB.json();
  console.log("responseB", json);

  // 高级两个并行
  const [a, b] = await Promise.all([responseA, responseB]);

  const promises = [
    fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"),
    fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"),
    fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"),
  ];
  // for循环并行
  for await (let result of promises) {
    result.json().then((data) => {
      console.log("for await", data);
    });
  }

  console.log("end");
}

f();

//上面的简化版
(async () => {
  const responseA = await fetch(
    "https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1"
  );
})();

fetch("https://v0.yiketianqi.com/free/day?appid=&appsecret=&unescape=1")
  .then(function (response) {
    return response.json();
  })
  .then(function (myJson) {
    console.log("myJson", myJson);
  })
  .finally(() => {
    console.log("myJson end");
  });



	const options = {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json', // 如果发送JSON数据
          // 或者如果是表单数据：
          // 'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: JSON.stringify(formData), // 如果是JSON格式
        // 或者如果是表单数据：
        // body: new URLSearchParams(formData).toString()
      };

     const response = await fetch('https://api.example.com/data', options);


```







## 封装的axios同步方法

 Could not find a declaration file for module 'smartAxios'问题

文件ts不认js，解决

tsconfig.json 中 "compilerOptions" 下面 加上

```js
"allowJs": true
```



```js
/*
 *  ajax请求
 *
 * @Author:    qinjp
 * @Date:      2023-11-11 20:46:03
 */
import axios from 'axios'

// token的消息头
const TOKEN_HEADER = 'x-access-token'
const APP_API_URL = 'http://localhost:8080' //import.meta.env.VITE_APP_API_URL
const USER_TOKEN = 'user_token'
// 创建axios对象
const smartAxios = axios.create({
  baseURL: APP_API_URL
})

// ================================= 请求拦截器 =================================
smartAxios.interceptors.request.use(
  (config) => {
    // 在发送请求之前消息头加入token token
    const token = localRead(USER_TOKEN)
    if (token) {
      config.headers[TOKEN_HEADER] = token
    } else {
      delete config.headers[TOKEN_HEADER]
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)

// ================================= 响应拦截器 =================================
smartAxios.interceptors.response.use(
  (response) => {
    const res = response.data
    //return Promise.resolve(res)
    if (res.code !== 1) {
      // `token` 过期或者账号已在别处登录
      if (res.code === 30007 || res.code === 30008) {
        alert('您没有登录，请重新登录')
        setTimeout(logout, 300)
      }
      return Promise.reject(response)
    } else {
      return Promise.resolve(res)
    }
  },
  (error) => {
    // 对响应错误做点什么
    if (error.message.indexOf('timeout') !== -1) {
      alert('网络超时')
    } else if (error.message === 'Network Error') {
      alert('网络连接错误')
    } else if (error.message.indexOf('Request') !== -1) {
      alert('网络发生错误')
    }
    return Promise.reject(error)
  }
)

/**
 * 通用请求封装
 * @param config
 */
export const request = (config) => {
  return smartAxios.request(config)
}

/**
 * get请求
 */
export const getRequest = (url, params) => {
  return request({ url, method: 'get', params })
}

/**
 * post请求
 */
export const postRequest = (url, data) => {
  return request({ data, url, method: 'post' })
}

/**
 * 同步get请求
 */
export const getSyncRequest = (url, params) => {
  const xhr = new XMLHttpRequest()
  console.log(APP_API_URL, url, params)
  const urlWithParams = getUrlParam(APP_API_URL + url, params)
  console.log(urlWithParams)
  xhr.open('GET', urlWithParams, false) // 第三个参数设置为false表示同步
  xhr.setRequestHeader('Content-Type', 'application/json')
  const token = localRead(USER_TOKEN)
  if (token) {
    xhr.setRequestHeader(TOKEN_HEADER, token)
  }
  xhr.send()
  if (xhr.readyState === 4 && xhr.status === 200) {
    console.log(xhr.responseText)
    return JSON.parse(xhr.responseText)
  } else {
    throw new Error('请求失败，状态码:' + xhr.status)
  }
}

/**
 * 同步发送 POST 请求
 */
export const postSyncRequest = (url, data) => {
  const xhr = new XMLHttpRequest()
  xhr.open('POST', APP_API_URL + url, false) // 第三个参数设置为false表示同步
  xhr.setRequestHeader('Content-Type', 'application/json')
  const token = localRead(USER_TOKEN)
  if (token) {
    xhr.setRequestHeader(TOKEN_HEADER, token)
  }
  xhr.send(JSON.stringify(data))
  if (xhr.readyState === 4 && xhr.status === 200) {
    console.log(xhr.responseText)
    return JSON.parse(xhr.responseText)
  } else {
    throw new Error('请求失败，状态码:' + xhr.status)
  }
}

export const localSave = (key, value) => {
  localStorage.setItem(key, value)
}

export const localRead = (key) => {
  return localStorage.getItem(key) || ''
}

export const localClear = () => {
  localStorage.clear()
}

// 退出系统
export const logout = () => {
  localClear()
  location.href = '/'
}

export const getUrlParam = (url, data) => {
  if (!data) {
    return url
  }
  let paramsStr = data instanceof Object ? getQueryString(data) : data

  if (paramsStr) {
    return url + '?' + paramsStr
  }

  return url
}

export const getQueryString = (data) => {
  let paramsArr = []
  if (data instanceof Object) {
    Object.keys(data).forEach((key) => {
      let val = data[key]
      // todo 参数Date类型需要根据后台api酌情处理
      if (val instanceof Date) {
        val = formattedDate(val, 'yyyy-MM-dd HH:mm:ss')
      }
      paramsArr.push(encodeURIComponent(key) + '=' + encodeURIComponent(val))
    })
    return paramsArr.join('&')
  }
  return data
}

class DateFormatter {
  constructor() {}

  format(date, formatStr) {
    const year = date.getFullYear()
    const month = this.padZero(date.getMonth() + 1)
    const day = this.padZero(date.getDate())
    const hours = this.padZero(date.getHours())
    const minutes = this.padZero(date.getMinutes())
    const seconds = this.padZero(date.getSeconds())

    formatStr = formatStr.replace('yyyy', year)
    formatStr = formatStr.replace('MM', month)
    formatStr = formatStr.replace('dd', day)
    formatStr = formatStr.replace('HH', hours)
    formatStr = formatStr.replace('mm', minutes)
    formatStr = formatStr.replace('ss', seconds)

    return formatStr
  }

  padZero(num) {
    return num < 10 ? '0' + num : num
  }
}
export const df = new DateFormatter()
// 使用示例
export const formattedDate = (date, formatStr) => {
  return df.format(date, formatStr)
}

```





## 消除fetch异步传递

思路：另一个思路，将异步的 fetch 改成 同步的 XMLHttpRequest

函数中有异步操作，使用throw中断函数后续的执行

异步操作执行完毕时，缓存异步结果(一个异步操作对应一个缓存)

重新执行函数，异步操作直接返回缓存内容

1. 怎么重新执行函数？（使用try catch捕获错误，就可以在catch重新执行函数）
2. 什么时机重新执行函数？（当异步有结果后，即Promise改变状态后，即可重新执行）

```js
import { toValue } from 'vue'

function getUser() {
  // 测试普通错误
  // throw new Error("普通错误");

  const options = {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json' // 如果发送JSON数据
      // 或者如果是表单数据：
      // 'Content-Type': 'application/x-www-form-urlencoded'
    },
    body: JSON.stringify({
      name: 'jp',
      age: 2
    })
  }

  const user2 = fetch('http://localhost:8080/postMsg', options)

  return user2
}

function m1() {
  console.log('m1')
  return getUser()
}

function m2() {
  console.log('m2')
  return m1()
}

function m3() {
  console.log('m3')
  return m2()
}

function main() {
  console.log('main')
  const user = m3()
  console.log('feng', user)
}

// run函数为主要代码，该函数会将异步操作进行处理。
function run(func) {
  // 保留原有fetch，发起请求时使用。
  const oldFetch = window.fetch
  // 缓存结果
  const cache = {
    status: 'pending',
    value: null
  }

  function newFetch(...args) {
    // 如果缓存状态为fulfilled，则直接返回Promise，等待结果
    if (cache.status === 'fulfilled') {
      return cache.value
    } else if (cache.status === 'rejected') {
      throw cache.value
    }

    // 没有缓存
    const p = oldFetch(...args)
      .then((data) => data.json())
      .then((data) => {
        cache.status = 'fulfilled'
        cache.value = data
      })
      .catch((err) => {
        cache.status = 'rejected'
        cache.value = err
      })

    // 抛出错误
    throw p
  }
  window.fetch = newFetch
  try {
    func()
  } catch (error) {
    if (error instanceof Promise) {
      error.finally(() => {
        window.fetch = newFetch
        func()
        window.fetch = oldFetch
      })
    }
  }
  window.fetch = oldFetch
}

run(main)


```













