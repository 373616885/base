import axios from 'axios'
import { useLoginStore } from '@/stores/login'
import { TOKEN_HEADER } from '@/constants/common-const'
import { ElMessage } from 'element-plus'

const APP_API_URL = import.meta.env.VITE_APP_API_URL //'http://localhost:8080' //

// 创建axios对象
const http = axios.create({
  baseURL: APP_API_URL,
  timeout: 5000
})

// ================================= 请求拦截器 =================================
http.interceptors.request.use(
  (config) => {
    const loginStore = useLoginStore()
    // 在发送请求之前消息头加入token token
    const token = loginStore.getToken
    if (token) {
      config.headers[TOKEN_HEADER] = token
    } else {
      delete config.headers[TOKEN_HEADER]
    }
    return config
  },
  (error) => {
    // 对请求错误做些什么
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

// ================================= 响应拦截器 =================================
http.interceptors.response.use(
  (response) => {
    // 解构 data
    const result = response.data

    // token过期
    if (result.code === 411) {
      const loginStore = useLoginStore()
      setTimeout(loginStore.logout(), 3000)
      return Promise.reject(new Error(result.msg || 'Error'))
    }
    // 成功调用
    if (result.code === 200) {
      // 这是解构服务端返回的 data
      return Promise.resolve(result.data)
    }
    ElMessage.error({ duration: 50000 })
    return Promise.reject(new Error(result.msg || 'Error'))
  },
  (error) => {
    ElMessage.error(error.message)
    return Promise.reject(error)
  }
)

/**
 * 通用请求封装
 * @param config
 */
export const request = (config) => {
  return http.request(config)
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
