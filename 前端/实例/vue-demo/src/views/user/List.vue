<template>
  <el-card>
    <template #header>
      <el-row :gutter="24">
        <el-col :span="18">
          <el-form :inline="true" :model="search" class="demo-form-inline">
            <el-form-item :label="$t('user.username')">
              <el-input v-model.trim="search.username" clearable :placeholder="$t('user.username')" :aria-label="$t('user.username')"> </el-input>
            </el-form-item>
            <el-form-item :label="$t('user.name')">
              <el-input v-model.trim="search.name" clearable :placeholder="$t('user.name')" :aria-label="$t('user.name')"></el-input>
            </el-form-item>

            <el-form-item>
              <el-date-picker
                v-model="search.registerTime"
                type="date"
                :placeholder="$t('user.registerTime')"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :icon="Search" @click="query">{{ $t('btn.query') }}</el-button>
              <el-button type="primary" @click="reset">{{ $t('btn.reset') }}</el-button>
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="6" style="text-align: right">
          <el-button type="primary" :icon="Plus" @click="add">{{ $t('btn.add') }}</el-button>
        </el-col>
      </el-row>
    </template>

    <el-table :data="tableData" stripe>
      <el-table-column prop="id" :label="$t('user.id')" min-width="50" />

      <el-table-column min-width="200">
        <template #default="scpoe">
          <el-button type="primary" @click.prevent.stop="edit(scpoe.row)" :icon="Edit">{{ $t('btn.edit') }}</el-button>
          <el-button type="danger" @click.prevent.stop="del(scpoe.row.id)" :icon="Delete">{{ $t('btn.del') }}</el-button>
        </template>
      </el-table-column>

      <el-table-column prop="isLock" :label="$t('user.isLock')" min-width="140" align="center">
        <template #default="scpoe">
          <el-switch
            v-model.trim="scpoe.row.isLock"
            active-value="正常"
            inactive-value="锁定"
            active-text="正常"
            inactive-text="锁定"
            @click="lock(scpoe.row)"
            style="--el-switch-on-color: #409eff; --el-switch-off-color: #ff4949"
          />
        </template>
      </el-table-column>
      <el-table-column prop="username" :label="$t('user.username')" min-width="120" />
      <el-table-column prop="name" :label="$t('user.name')" min-width="100" />
      <el-table-column prop="sex" :label="$t('user.sex')" min-width="120" align="center">
        <template #default="scpoe">
          <el-switch
            v-model.trim="scpoe.row.sex"
            active-value="男"
            active-text="男"
            inactive-value="女"
            inactive-text="女"
            style="--el-switch-on-color: #409eff; --el-switch-off-color: #e6a23c"
            :before-change="() => false"
          />
        </template>
      </el-table-column>
      <el-table-column prop="phone" :label="$t('user.phone')" min-width="120" />
      <el-table-column prop="email" :label="$t('user.email')" min-width="170" />
      <el-table-column prop="avatar" :label="$t('user.avatar')" min-width="60">
        <template #default="scpoe">
          <el-image :src="scpoe.row.avatar" :preview-src-list="[scpoe.row.avatar]" :size="30" preview-teleported style="width: 30px; height: 30px" />
        </template>
      </el-table-column>
      <el-table-column prop="lastLogin" :label="$t('user.lastLogin')" min-width="170" align="center" />
      <el-table-column prop="time" :label="$t('user.time')" min-width="120" align="center">
        <template #default="scpoe"> {{ $filter.formatDate(scpoe.row.time) }} </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <el-pagination
        style="justify-content: center; align-items: center"
        v-model:current-page="search.current"
        v-model:page-size="search.size"
        :page-sizes="PAGE_SIZE_OPTIONS"
        :size="size"
        :disabled="disabled"
        :background="background"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </template>
  </el-card>

  <Update v-model="visible" v-bind="updateAttr" v-if="visible" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

import { Search, Edit, Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type ComponentSize } from 'element-plus'

import Update from '@/views/user/Update.vue'

import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '@/constants/common-const'
import { userQuery, userLock, userDelete, type UserQuery, type User } from '@/api/user-api'

const { t } = useI18n()
const router = useRouter()

const background = ref(true)
const disabled = ref(false)
const size = ref<ComponentSize>('default')
const total = ref(0)
const visible = ref(false)

const updateAttr = ref({
  width: 800,
  row: {},
  beforeClose: () => query()
})

function edit(row: any) {
  visible.value = true
  updateAttr.value.row = row
}
function add() {
  router.push({ name: 'UserAdd' })
}

function del(id: any) {
  ElMessageBox.confirm(t('message.confirmDelete'), t('message.warning'), { type: 'warning' })
    .then(() => {
      userDelete({ id: id }).then(() => {
        ElMessage({ message: t('message.deleteSuccess'), type: 'success' })
        query()
      })
    })
    .catch(() => {
      // catch error
    })
}

const tableData = ref<User[]>([])

const search = ref<UserQuery>({
  username: undefined,
  name: undefined,
  registerTime: undefined,
  size: PAGE_SIZE,
  current: 1
})

function reset() {
  search.value.username = undefined
  search.value.name = undefined
  search.value.registerTime = undefined
}

function query() {
  console.log('query')
  userQuery(search.value).then((res) => {
    tableData.value = res.records
    total.value = res.total
  })
}
query()

const handleSizeChange = (val: number) => {
  search.value.current = 1
  search.value.size = val
  query()
}
const handleCurrentChange = (val: number) => {
  search.value.current = val
  query()
}

function lock(row: any) {
  const lock = row.isLock === '正常' ? 0 : 1
  userLock({ id: row.id, lock: lock }).then(() => {
    ElMessage({ message: t('message.updateSuccess'), type: 'success' })
  })
}
</script>
<style scoped>
.el-switch {
  /** 防止选中 附近的文字 **/
  user-select: none;
}
.demo-form-inline .el-input {
  --el-input-width: 150px;
}
</style>
