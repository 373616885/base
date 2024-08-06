import { computed } from 'vue'
import { createI18n } from 'vue-i18n'
import { localRead, localSave } from '@/lib/local-util'
import EN from './en'
import ZH from './zh'

const messages = {
  en: {
    ...EN
  },
  zh: {
    ...ZH
  }
}

const getLocaleLangCode = () => {
  let code = localRead('lang')
  if (code) {
    return code
  }
  code = navigator.language
  code = code.indexOf('zh') > -1 ? 'zh' : 'en'
  localSave('lang', code)
  return code
}

export type LangCode = 'zh' | 'en'

export const currentLanguage = computed(() => {
  return i18n.global.locale.value
})

// 切换语言
export function switchLang(value: LangCode) {
  i18n.global.locale.value = value
  localSave('lang', value)
}

export const i18n = createI18n({
  legacy: false,
  globalInjection: true, // 全局注入
  locale: getLocaleLangCode() || 'zh', // set locale
  messages
})
