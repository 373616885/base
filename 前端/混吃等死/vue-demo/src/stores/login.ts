import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { localRead, localSave, localClear } from '@/lib/local-util'
import { TOKEN_HEADER } from '@/constants/common-const'

export const useLoginStore = defineStore('login', () => {
  const token = ref(localRead(TOKEN_HEADER))

  const getToken = computed(() => {
    if (token.value) {
      return token.value
    }
    return localRead(TOKEN_HEADER)
  })

  function setToken(value: string) {
    token.value = value
    localSave(TOKEN_HEADER, value)
  }

  function logout(router?: any) {
    token.value = ''
    //sessionClear()
    localClear()
    if (router) {
      router.push('/login')
    } else {
      location.href = '/'
    }
  }

  return { token, getToken, setToken, logout }
})
