<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold">柜机列表</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新增柜机
          </el-button>
        </div>
      </template>
      <el-table :data="cabinets" stripe>
        <el-table-column prop="code" label="柜机编号" width="120" />
        <el-table-column prop="name" label="柜机名称" width="120" />
        <el-table-column prop="location" label="所在区域" width="120" />
        <el-table-column prop="address" label="详细地址" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ONLINE' ? 'success' : row.status === 'MAINTENANCE' ? 'warning' : 'danger'">
              {{ row.status === 'ONLINE' ? '在线' : row.status === 'MAINTENANCE' ? '维护' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="库存情况" width="200">
          <template #default="{ row }">
            <div style="line-height: 1.6;">
              <div>总槽位: {{ row.totalSlots }}</div>
              <div v-if="row.status === 'ONLINE'" style="color: #67c23a;">
                可借: {{ row.occupiedSlots }} 个
              </div>
              <div v-else style="color: #f56c6c;">
                <el-icon size="12"><Warning /></el-icon>
                柜机离线, 库存不可用
              </div>
              <div style="color: #909399; font-size: 12px;">
                空槽位: {{ row.availableSlots }} 个
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="heartbeat(row.id)">心跳</el-button>
            <el-button size="small" type="success" @click="showAddPbDialog(row)">添加充电宝</el-button>
            <el-button size="small" type="warning" @click="showTransferDialog(row)">调拨</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="新增柜机" width="600px">
      <el-form :model="newCabinet" label-width="100px">
        <el-form-item label="柜机编号">
          <el-input v-model="newCabinet.code" />
        </el-form-item>
        <el-form-item label="柜机名称">
          <el-input v-model="newCabinet.name" />
        </el-form-item>
        <el-form-item label="所在区域">
          <el-input v-model="newCabinet.location" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="newCabinet.address" />
        </el-form-item>
        <el-form-item label="总槽位数">
          <el-input-number v-model="newCabinet.totalSlots" :min="1" :max="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createCabinet">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAddPb" title="添加充电宝" width="500px">
      <el-form label-width="100px">
        <el-form-item label="柜机名称">
          <span>{{ selectedCabinet?.name }}</span>
        </el-form-item>
        <el-form-item label="选择充电宝">
          <el-select v-model="selectedPbId" style="width: 100%">
            <el-option v-for="pb in availablePbs" :key="pb.id" :label="pb.code" :value="pb.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="槽位号">
          <el-input-number v-model="slotNumber" :min="1" :max="selectedCabinet?.totalSlots || 10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddPb = false">取消</el-button>
        <el-button type="primary" @click="addPowerBank">确定添加</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showTransfer" title="网点调拨" width="500px">
      <el-form label-width="100px">
        <el-form-item label="源柜机">
          <span>{{ selectedCabinet?.name }}</span>
        </el-form-item>
        <el-form-item label="选择充电宝">
          <el-select v-model="transferPbId" style="width: 100%">
            <el-option v-for="pb in cabinetPbs" :key="pb.id" :label="pb.code" :value="pb.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标柜机">
          <el-select v-model="targetCabinetId" style="width: 100%">
            <el-option v-for="c in otherCabinets" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTransfer = false">取消</el-button>
        <el-button type="primary" @click="transferPowerBank">确定调拨</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import api from '../api'

const cabinets = ref([])
const powerBanks = ref([])
const showCreateDialog = ref(false)
const showAddPb = ref(false)
const showTransfer = ref(false)
const selectedCabinet = ref(null)
const selectedPbId = ref('')
const slotNumber = ref(1)
const transferPbId = ref('')
const targetCabinetId = ref('')

const newCabinet = ref({
  code: '',
  name: '',
  location: '',
  address: '',
  totalSlots: 8
})

const availablePbs = computed(() => {
  return powerBanks.value.filter(pb => pb.status === 'AVAILABLE' && !pb.cabinetId)
})

const cabinetPbs = computed(() => {
  if (!selectedCabinet.value) return []
  return powerBanks.value.filter(pb => pb.cabinetId === selectedCabinet.value.id)
})

const otherCabinets = computed(() => {
  if (!selectedCabinet.value) return []
  return cabinets.value.filter(c => c.id !== selectedCabinet.value.id)
})

const loadCabinets = async () => {
  try {
    const res = await api.get('/cabinets')
    cabinets.value = res.data
  } catch (e) {
    ElMessage.error('加载柜机失败')
  }
}

const loadPowerBanks = async () => {
  try {
    const res = await api.get('/powerbanks')
    powerBanks.value = res.data
  } catch (e) {
    ElMessage.error('加载充电宝失败')
  }
}

const createCabinet = async () => {
  try {
    await api.post('/cabinets', newCabinet.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    newCabinet.value = { code: '', name: '', location: '', address: '', totalSlots: 8 }
    loadCabinets()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

const heartbeat = async (id) => {
  try {
    await api.post(`/cabinets/${id}/heartbeat`)
    ElMessage.success('心跳发送成功')
    loadCabinets()
  } catch (e) {
    ElMessage.error('心跳失败')
  }
}

const showAddPbDialog = (cabinet) => {
  selectedCabinet.value = cabinet
  showAddPb.value = true
}

const addPowerBank = async () => {
  try {
    await api.post(`/cabinets/${selectedCabinet.value.id}/add-powerbank`, {
      powerBankId: selectedPbId.value,
      slotNumber: slotNumber.value
    })
    ElMessage.success('添加成功')
    showAddPb.value = false
    loadCabinets()
    loadPowerBanks()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

const showTransferDialog = (cabinet) => {
  selectedCabinet.value = cabinet
  showTransfer.value = true
}

const transferPowerBank = async () => {
  try {
    await api.post('/transfer', {
      fromCabinetId: selectedCabinet.value.id,
      toCabinetId: targetCabinetId.value,
      powerBankId: transferPbId.value
    })
    ElMessage.success('调拨成功')
    showTransfer.value = false
    loadCabinets()
    loadPowerBanks()
  } catch (e) {
    ElMessage.error('调拨失败')
  }
}

onMounted(() => {
  loadCabinets()
  loadPowerBanks()
})
</script>
