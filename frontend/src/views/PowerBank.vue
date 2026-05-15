<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold">充电宝列表</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新增充电宝
          </el-button>
        </div>
      </template>
      <el-table :data="powerBanks" stripe>
        <el-table-column prop="code" label="设备编号" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cabinetId" label="所属柜机" width="180">
          <template #default="{ row }">
            <div v-if="row.cabinetId">
              <div>{{ getCabinetName(row.cabinetId) }}</div>
              <div style="display: flex; align-items: center; margin-top: 3px;">
                <el-tag size="small" :type="isCabinetOnline(row.cabinetId) ? 'success' : 'danger'" style="transform: scale(0.85); transform-origin: left center;">
                  {{ isCabinetOnline(row.cabinetId) ? '柜机在线' : '柜机离线' }}
                </el-tag>
                <span v-if="!isCabinetOnline(row.cabinetId) && row.status === 'AVAILABLE'" style="color: #f56c6c; font-size: 12px; margin-left: 5px;">
                  (暂不可借)
                </span>
              </div>
            </div>
            <span v-else style="color: #909399;">未绑定</span>
          </template>
        </el-table-column>
        <el-table-column prop="slotNumber" label="槽位号" width="100" />
        <el-table-column prop="batteryLevel" label="电量" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.batteryLevel" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="新增充电宝" width="500px">
      <el-form :model="newPb" label-width="100px">
        <el-form-item label="设备编号">
          <el-input v-model="newPb.code" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createPowerBank">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const powerBanks = ref([])
const cabinets = ref([])
const showCreateDialog = ref(false)
const newPb = ref({ code: '' })

const getStatusType = (status) => {
  const map = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'primary',
    'CHARGING': 'warning',
    'ABNORMAL': 'danger',
    'LOST': 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'AVAILABLE': '可用',
    'OCCUPIED': '使用中',
    'CHARGING': '充电中',
    'ABNORMAL': '异常',
    'LOST': '丢失'
  }
  return map[status] || status
}

const getCabinetName = (id) => {
  const cabinet = cabinets.value.find(c => c.id === id)
  return cabinet ? cabinet.name : id.substring(0, 8)
}

const isCabinetOnline = (cabinetId) => {
  const cabinet = cabinets.value.find(c => c.id === cabinetId)
  return cabinet ? cabinet.status === 'ONLINE' : false
}

const loadPowerBanks = async () => {
  try {
    const res = await api.get('/powerbanks')
    powerBanks.value = res.data
  } catch (e) {
    ElMessage.error('加载失败')
  }
}

const loadCabinets = async () => {
  try {
    const res = await api.get('/cabinets')
    cabinets.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const createPowerBank = async () => {
  try {
    await api.post('/powerbanks', newPb.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    newPb.value = { code: '' }
    loadPowerBanks()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

onMounted(() => {
  loadPowerBanks()
  loadCabinets()
})
</script>
