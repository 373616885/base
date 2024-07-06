// 抽取公用的实例 - 操作成功与失败消息提醒内容等
export const messageObj = {
  success: '操作成功',
  fail: '操作失败',
  loading: '加载中...'
}

export default {
  data() {
    return {
      sexList: [
        { name: '未知', value: 0 },
        { name: '男', value: 1 },
        { name: '女', value: 2 }
      ],
      // 弹出框标题
      dialogTitleObj: {
        add: '添加',
        update: '编辑',
        detail: '详情'
      }
    }
  },
  methods: {
    // 操作成功消息提醒内容
    submitOk(msg, call) {
      if (msg) {
        console.log(msg)
      } else {
        console.log('操作成功！')
      }
      //执行回调
      call && call()
    },
    // 操作失败消息提醒内容
    submitFail(msg) {
      if (msg) {
        console.log(msg)
      } else {
        console.log('网络异常，请稍后重试！')
      }
    }
  }
}
