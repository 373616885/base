import { filter } from '@/lib/filter'
import { ComponentCustomProperties } from 'vue'

declare module '@vue/runtime-core' {
  export interface ComponentCustomProperties {
    // Example：
    // $http: typeof axios
    // $validate: (data: object, rule: object) => boolean
    $filter: typeof filter
  }
}
// 必须导出，才能在其他文件中使用
export default ComponentCustomProperties
