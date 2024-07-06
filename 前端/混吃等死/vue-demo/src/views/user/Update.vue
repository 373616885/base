<template>
  <el-dialog :model-value="modelValue" :show-close="false" destroy-on-close :width="width" @close="handleClose" :fullscreen="isfull" center>
    <template #header="{ close, titleId, titleClass }">
      <div>
        <span :id="titleId" :class="titleClass">{{ t('btn.edit') }}</span>
        <el-button :icon="FullScreen" @click="full" class="el-dialog__headerbtn" style="right: 48px" />
        <el-button :icon="CloseBold" @click="close" class="el-dialog__headerbtn" style="right: 0px" />
      </div>
    </template>
    <el-form ref="form" :model="user" label-width="auto" style="margin: 0 auto; max-width: 600px" :rules="rules">
      <el-form-item :label="$t('user.username')" prop="username">
        <el-input v-model="user.username" />
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
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">{{ $t('btn.cancel') }}</el-button>
        <el-button type="primary" @click="update">{{ $t('btn.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { CloseBold, FullScreen } from '@element-plus/icons-vue'
import { ElButton, ElDialog, ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

import { userUpdate } from '@/api/user-api'

const { t } = useI18n()

const props = defineProps(['width', 'modelValue', 'beforeClose', 'row'])
// 声明事件
const emit = defineEmits(['update:modelValue'])

const handleClose = () => {
  props.beforeClose()
  emit('update:modelValue', false)
}

const isfull = ref(false)
const full = () => {
  isfull.value = !isfull.value
}

const form = ref()

const user = ref({
  id: props.row.id,
  username: props.row.username,
  name: props.row.name,
  sex: props.row.sex,
  phone: props.row.phone,
  email: props.row.email,
  avatar: props.row.avatar,
  lastLogin: props.row.lastLogin
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

const update = () => {
  form.value.validate((valid: any) => {
    if (valid) {
      userUpdate(user.value).then(() => {
        ElMessage({ message: t('message.saveSuccess'), type: 'success' })
        handleClose()
      })
    }
  })
}
</script>

<style scoped></style>
