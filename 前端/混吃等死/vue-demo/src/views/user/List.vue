<template>
  <el-card>
    <template #header>
      <el-row :gutter="24">
        <el-col :span="6">
          <el-input placeholder="请输入内容"></el-input>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" :icon="Search">搜索</el-button>
          <el-button type="primary">重置</el-button>
        </el-col>
        <el-col :span="6" :offset="6" style="text-align: right">
          <el-button type="primary" @click="add">添加用户</el-button>
        </el-col>
      </el-row>
    </template>

    <el-table :data="tableData" stripe>
      <el-table-column prop="date" label="Date" />
      <el-table-column prop="name" label="Name" />
      <el-table-column prop="address" label="Address" />
      <el-table-column prop="status" label="Sddress">
        <template #default="scpoe">
          <el-switch v-model.trim.number="scpoe.row.status" :before-change="() => false" :active-value="1" :inactive-value="0" />
        </template>
      </el-table-column>
      <el-table-column>
        <template #default="scpoe">
          <el-button type="primary" @click="edit(scpoe.row)">编辑</el-button>
          <el-button type="danger" @click="edit(scpoe.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>Footer content</template>
  </el-card>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

function edit(row: any) {
  console.log(row)
}
function add() {
  console.log('add')
}

function switchChange() {
  return false
}
const tableData = [
  {
    date: '2016-05-03',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
    status: 1
  },
  {
    date: '2016-05-02',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
    status: 1
  },
  {
    date: '2016-05-04',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
    status: 0
  },
  {
    date: '2016-05-01',
    name: 'Tom',
    address: 'No. 189, Grove St, Los Angeles',
    status: true
  }
]
</script>
<style scoped>
.el-switch {
  /** 防止选中 附近的文字 **/
  user-select: none;
}
</style>
