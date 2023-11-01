import { inject } from 'vue'

export const filters = {
  // 获取性别值
  sexName: (sex) => {
    const sexList = inject('sexList')
    if (sexList) {
      return sexList.find((obj) => obj.value == sex).name
    }
    return '数据丢失'
  }
}
