<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold">操作日志</span>
          <el-tag type="info">共 {{ logs.length }} 条记录</el-tag>
        </div>
      </template>
      <el-table :data="logs" stripe>
        <el-table-column prop="targetType" label="目标类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTargetType(row.targetType)" size="small">{{ getTargetText(row.targetType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetId" label="目标ID" width="150">
          <template #default="{ row }">
            {{ row.targetId?.substring(0, 12) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getOpType(row.operationType)" size="small">{{ getOpText(row.operationType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="beforeStatus" label="操作前状态" width="180">
          <template #default="{ row }">
            <span v-if="row.beforeStatus" style="color: #909399;">{{ row.beforeStatus }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="afterStatus" label="操作后状态" width="180">
          <template #default="{ row }">
            <span v-if="row.afterStatus" style="color: #409eff;">{{ row.afterStatus }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100" />
        <el-table-column prop="remark" label="备注" min-width="250" />
        <el-table-column prop="createTime" label="操作时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const logs = ref([])

const getTargetType = (type) => {
  const map = {
    'DEPOSIT': 'warning',
    'CABINET_STOCK': 'primary',
    'ORDER': 'success',
    'CABINET': 'info',
    'POWERBANK': 'success',
    'USER': 'warning'
  }
  return map[type] || 'info'
}

const getTargetText = (type) => {
  const map = {
    'DEPOSIT': '押金',
    'CABINET_STOCK': '柜机库存',
    'ORDER': '订单',
    'CABINET': '柜机',
    'POWERBANK': '充电宝',
    'USER': '用户'
  }
  return map[type] || type
}

const getOpType = (op) => {
  const map = {
    'LOCK': 'warning',
    'UNLOCK': 'success',
    'DEDUCT': 'danger',
    'CREATE': 'primary',
    'UPDATE': 'info',
    'DELETE': 'danger',
    'STATUS_CHANGE': 'warning',
    'RETURN': 'success',
    'LOST': 'danger',
    'CLOSE': 'info',
    'ONLINE': 'success',
    'ADD_POWERBANK': 'success',
    'REMOVE_POWERBANK': 'danger',
    'TRANSFER': 'primary'
  }
  return map[op] || 'info'
}

const getOpText = (op) => {
  const map = {
    'LOCK': '锁定',
    'UNLOCK': '释放',
    'DEDUCT': '扣除',
    'CREATE': '创建',
    'UPDATE': '更新',
    'DELETE': '删除',
    'STATUS_CHANGE': '状态变更',
    'RETURN': '归还',
    'LOST': '丢失',
    'CLOSE': '关闭',
    'ONLINE': '上线',
    'ADD_POWERBANK': '添加充电宝',
    'REMOVE_POWERBANK': '移除充电宝',
    'TRANSFER': '调拨'
  }
  return map[op] || op
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString()
}

const loadLogs = async () => {
  try {
    const res = await api.get('/logs')
    logs.value = res.data
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

onMounted(() => {
  loadLogs()
})
</script>
