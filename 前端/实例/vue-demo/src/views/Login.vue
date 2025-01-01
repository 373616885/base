<template>
  <div class="login-container">
    <el-container>
      <el-header>{{ $t('login.title') }} <Lang /></el-header>
      <el-main>
        <el-icon color="white" style="left: 95%; font-size: 30px" class="is-loading" @click="resetForm">
          <Refresh />
        </el-icon>
        <el-form ref="loginForm" :model="form" label-width="100px" :rules="rules">
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="form.username"
              placeholder="please input username"
              autocomplete="off"
              maxlength="30"
              clearable
              size="large"
              :prefix-icon="User"
            ></el-input>
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
              type="password"
              v-model="form.password"
              placeholder="please input password"
              autocomplete="new-password"
              maxlength="30"
              size="large"
              :prefix-icon="Lock"
              clearable
              show-password
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="submitForm()">登录</el-button>
          </el-form-item>
        </el-form>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import Lang from '@/layout/Lang.vue'

import { ref, reactive, onBeforeMount, onBeforeUnmount, toRaw } from 'vue'
import { User, Lock, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login, type LoginResponse } from '@/api/login-api'
import { useLoginStore } from '@/stores/login'
import { useRouter } from 'vue-router'
const router = useRouter()
const loginStore = useLoginStore()

const loginForm = ref()

onBeforeMount(() => {
  document.body.style.backgroundColor = '#584e5a'
})
onBeforeUnmount(() => {
  document.body.style.removeProperty('background-color') // 恢复默认背景
})

let form = reactive({
  username: 'qinjiepeng',
  password: 'a87bae1d215f2e17783eba75ae715864'
})

const rules = ref({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const submitForm = () => {
  loginForm.value.validate((valid: boolean) => {
    if (valid) {
      login(toRaw(form)).then((data: LoginResponse) => {
        ElMessage({ message: '登录成功', type: 'success', grouping: true })
        loginStore.setToken(data.token)
        router.push('/')
      })
      return true
    }
    ElMessage({ message: '账号或密码错误', type: 'warning', grouping: true })
    return false
  })
}

const resetForm = () => {
  loginForm.value.resetFields()
}
</script>

<style scoped>
.login-container {
  max-width: 600px;
  margin: 0 auto;
}

:deep(.el-form-item__label) {
  color: white;
}

.el-button {
  width: 100%;
  border-radius: 10px;
}
.el-header {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: white;
  .el-dropdown {
    color: white;
    left: 36%;
  }
}
</style>
