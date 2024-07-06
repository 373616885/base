<template>
  <el-row :gutter="24">
    <el-col :span="24" style="text-align: right">
      <el-button :icon="CloseBold" circle @click="$router.push('/user/list')" />
    </el-col>
  </el-row>
  <el-form ref="form" :model="user" label-width="auto" style="margin: 0 auto; max-width: 600px" :rules="rules">
    <el-form-item :label="$t('user.username')" prop="username">
      <el-input v-model="user.username" />
    </el-form-item>
    <el-form-item :label="$t('user.password')" prop="password">
      <el-input v-model="user.password" type="password" show-password autocomplete="new-password" clearable />
    </el-form-item>
    <el-form-item :label="$t('user.name')" prop="name">
      <el-input v-model="user.name" />
    </el-form-item>

    <el-form-item :label="$t('user.phone')" prop="phone">
      <el-input v-model="user.phone" />
    </el-form-item>
    <el-form-item :label="$t('user.email')" prop="email">
      <el-input v-model="user.email" />
    </el-form-item>

    <el-form-item :label="$t('user.avatar')" prop="avatar">
      <el-input v-model="user.avatar" />
    </el-form-item>

    <el-form-item :label="$t('user.lastLogin')" prop="lastLogin">
      <el-date-picker
        v-model="user.lastLogin"
        type="date"
        :placeholder="$t('user.lastLogin')"
        format="YYYY-MM-DD"
        value-format="YYYY-MM-DD HH:mm:ss"
      />
    </el-form-item>

    <el-form-item :label="$t('user.sex')" prop="sex">
      <el-form-item prop="sex">
        <el-radio-group v-model="user.sex">
          <el-radio value="男">{{ $t('user.male') }}</el-radio>
          <el-radio value="女">{{ $t('user.female') }}</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form-item>

    <el-form-item :label="$t('user.age')" prop="age">
      <el-input v-model.number="user.age" />
    </el-form-item>

    <el-row :gutter="24">
      <el-col :span="24" style="text-align: right">
        <el-button @click="reset">{{ $t('btn.reset') }}</el-button>
        <el-button type="primary" @click.prevent="save">{{ $t('btn.save') }}</el-button>
      </el-col>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { CloseBold } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'

import { ref, reactive } from 'vue'

import { userInsert } from '@/api/user-api'

const { t } = useI18n()
const router = useRouter()

const form = ref()

const user = reactive({
  username: null,
  password: null,
  name: null,
  sex: '男',
  phone: null,
  email: null,
  avatar: null,
  lastLogin: null,
  age: 1
})

const checkUsername = (rule: any, value: any, callback: any) => {
  if (!value) {
    return callback(new Error('please input the username ' + t('user.username')))
  }
  const reg = /^[0-9a-zA-Z]+$/
  if (!reg.test(value)) {
    return callback(new Error('please input the number or english letters ' + t('user.username')))
  }
  return callback()
}

const rules = reactive({
  username: [
    { required: true, min: 3, max: 30, message: t('user.username'), trigger: 'blur' },
    { min: 3, max: 30, message: t('user.username') + ' length should be 3 to 30', trigger: 'blur' },
    { validator: checkUsername, trigger: 'blur' }
  ],
  password: [
    { required: true, min: 3, max: 30, message: t('user.password'), trigger: 'blur' },
    { min: 3, max: 30, message: t('user.password') + ' length should be 3 to 30', trigger: 'blur' }
  ],
  name: [
    { required: true, message: t('user.name'), trigger: 'blur' },
    { min: 3, max: 30, message: t('user.name') + ' length should be 3 to 30', trigger: 'blur' }
  ],
  sex: [{ required: true, message: t('user.sex'), trigger: 'blur' }],
  phone: [
    {
      required: true,
      pattern: /^1[3456789]\d{9}$/, // eslint-disable-line
      message: t('user.phone'),
      trigger: 'blur'
    }
  ],
  email: [
    { required: true, type: 'email', message: t('user.email'), trigger: ['blur', 'change'] },
    {
      pattern: /^([0-9a-zA-Z_\.\-\])+\@([0-9a-zA-Z_\.\-\])+\.([a-zA-Z]+)$/, // eslint-disable-line
      message: t('user.email')
    }
  ],
  avatar: [{ required: true, message: t('user.avatar'), trigger: 'blur' }],
  lastLogin: [{ required: true, type: 'date', message: t('user.lastLogin'), trigger: 'blur' }],
  age: [{ required: true, type: 'number', message: t('user.age'), trigger: ['blur', 'change'] }]
})

const save = () => {
  form.value.validate((valid: boolean) => {
    if (valid) {
      userInsert(user).then(() => {
        ElMessage({ message: t('message.saveSuccess'), type: 'success' })
        router.push('/user/list')
      })
    }
  })
}

const reset = () => {
  form.value.resetFields()
}
</script>

<style scoped></style>
