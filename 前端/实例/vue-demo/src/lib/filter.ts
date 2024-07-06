import dayjs from 'dayjs'
import { isEmpty } from 'lodash-es'
export const filter = {
  /**
   * 格式化时间
   * @param date
   * @param format
   */
  formatTime(date: string | number | Date, format: string = 'YYYY-MM-DD HH:mm:ss') {
    return dayjs(date).format(format)
  },
  /**
   * 格式化日期
   * @param date
   * @param format
   */
  formatDate(date: string | number | Date, format: string = 'YYYY-MM-DD') {
    return dayjs(date).format(format)
  },
  dayjs: dayjs,
  isEmpty(value: any) {
    return isEmpty(value)
  }
}
