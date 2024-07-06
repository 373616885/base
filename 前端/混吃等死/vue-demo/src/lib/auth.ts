import { localRead, localSave } from '@/lib/local-util'
import { whitelist } from '@/constants/common-const'

/**
 * 被动退出
 * 在请求拦截器里每次请求都 isTokenTimeOut 判断一下是否过期
 * 不需要等到服务端返回 token 失效的才去更新token
 */

// 被动退出 start
export const TOKEN_TIME = 'token_time_value'

// 2 小时
export const TOKEN_TIME_VALUE = 2 * 60 * 60 * 1000

export const setTokenTime = () => localSave(TOKEN_TIME, Date.now())

export const getTokenTime = () => localRead(TOKEN_TIME)

// token 是否失效
export const isTokenTimeOut = () => Date.now() - Number(getTokenTime()) > TOKEN_TIME_VALUE

// 一般退出都 localStorage.clear() 可以不调用这个
export const removeTokenTime = () => localStorage.removeItem(TOKEN_TIME)

// 被动退出 end

/**
 * 是否白名单内
 */
export const isWhitelist = (url: string) => {
  return whitelist.includes(url)
}
