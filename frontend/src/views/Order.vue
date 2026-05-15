<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold">订单列表</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            扫码租借
          </el-button>
        </div>
      </template>
      <el-table :data="orders" stripe>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userId" label="用户" width="120">
          <template #default="{ row }">
            {{ getUserName(row.userId) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="租借信息" width="200">
          <template #default="{ row }">
            柜机: {{ getCabinetName(row.rentCabinetId) }}<br/>
            时间: {{ formatTime(row.rentTime) }}
          </template>
        </el-table-column>
        <el-table-column label="费用" width="150">
          <template #default="{ row }">
            押金: ¥{{ row.depositAmount }}<br/>
            租金: ¥{{ row.rentalFee || '0' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button size="small" @click="viewOrderDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'RENTING' || row.status === 'OVERDUE'" size="small" type="success" @click="showReturnDialog(row)">归还</el-button>
            <el-button v-if="row.status === 'RENTING' || row.status === 'OVERDUE'" size="small" type="danger" @click="reportLost(row.id)">报失</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="扫码租借" width="700px">
      <el-form :model="newOrder" label-width="100px">
        <el-form-item label="选择用户">
          <el-select v-model="newOrder.userId" style="width: 100%" placeholder="请选择用户">
            <el-option v-for="user in users" :key="user.id" :label="user.username" :value="user.id" />
          </el-select>
        </el-form-item>

        <div v-if="selectedUser" style="margin: -10px 0 20px 100px; padding: 15px; background: #f5f7fa; border-radius: 8px;">
          <div style="font-weight: bold; margin-bottom: 10px; color: #606266;">
            <el-icon><Lock /></el-icon> 用户状态校验
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">信用分:</span>
                <el-tag :type="selectedUser.creditScore >= 80 ? 'success' : selectedUser.creditScore >= 60 ? 'warning' : 'danger'" size="small">
                  {{ selectedUser.creditScore }}
                </el-tag>
                <el-icon v-if="selectedUser.creditScore >= 60" color="#67c23a" style="margin-left: 5px"><CircleCheck /></el-icon>
                <el-icon v-else color="#f56c6c" style="margin-left: 5px"><CircleClose /></el-icon>
              </div>
            </el-col>
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">押金余额:</span>
                <span style="font-weight: bold; color: #409eff;">¥{{ selectedUser.deposit }}</span>
                <el-icon v-if="Number(selectedUser.deposit) >= 99" color="#67c23a" style="margin-left: 5px"><CircleCheck /></el-icon>
                <el-icon v-else color="#f56c6c" style="margin-left: 5px"><CircleClose /></el-icon>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 10px;">
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">押金状态:</span>
                <el-tag :type="selectedUser.depositLocked ? 'warning' : 'success'" size="small">
                  {{ selectedUser.depositLocked ? '已锁定' : '正常可用' }}
                </el-tag>
                <el-icon v-if="!selectedUser.depositLocked" color="#67c23a" style="margin-left: 5px"><CircleCheck /></el-icon>
                <el-icon v-else color="#f56c6c" style="margin-left: 5px"><Warning /></el-icon>
              </div>
            </el-col>
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">需押金:</span>
                <span style="font-weight: bold; color: #e6a23c;">¥99.00</span>
              </div>
            </el-col>
          </el-row>
          <div v-if="!canRentUser" style="margin-top: 12px; padding: 10px; background: #fef0f0; border-radius: 4px; color: #f56c6c;">
            <el-icon><Warning /></el-icon>
            <span style="margin-left: 5px;">{{ userErrorMsg }}</span>
          </div>
        </div>

        <el-form-item label="选择柜机">
          <el-select v-model="newOrder.cabinetId" style="width: 100%" placeholder="请选择柜机">
            <el-option v-for="cabinet in cabinets" :key="cabinet.id" :label="cabinet.name" :value="cabinet.id" />
          </el-select>
        </el-form-item>

        <div v-if="selectedCabinet" style="margin: -10px 0 20px 100px; padding: 15px; background: #f5f7fa; border-radius: 8px;">
          <div style="font-weight: bold; margin-bottom: 10px; color: #606266;">
            <el-icon><Box /></el-icon> 柜机状态校验
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">柜机名称:</span>
                <span style="font-weight: bold;">{{ selectedCabinet.name }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">在线状态:</span>
                <el-tag :type="selectedCabinet.status === 'ONLINE' ? 'success' : 'danger'" size="small">
                  {{ selectedCabinet.status === 'ONLINE' ? '在线' : '离线' }}
                </el-tag>
                <el-icon v-if="selectedCabinet.status === 'ONLINE'" color="#67c23a" style="margin-left: 5px"><CircleCheck /></el-icon>
                <el-icon v-else color="#f56c6c" style="margin-left: 5px"><CircleClose /></el-icon>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 10px;">
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">可借充电宝:</span>
                <el-tag :type="selectedCabinet.occupiedSlots > 0 ? 'success' : 'danger'" size="small">
                  {{ selectedCabinet.occupiedSlots }} 个
                </el-tag>
                <el-icon v-if="selectedCabinet.occupiedSlots > 0" color="#67c23a" style="margin-left: 5px"><CircleCheck /></el-icon>
                <el-icon v-else color="#f56c6c" style="margin-left: 5px"><CircleClose /></el-icon>
              </div>
            </el-col>
            <el-col :span="12">
              <div style="display: flex; align-items: center">
                <span style="margin-right: 8px; color: #909399;">空槽位数:</span>
                <el-tag type="info" size="small">{{ selectedCabinet.availableSlots }} 个</el-tag>
              </div>
            </el-col>
          </el-row>
          <div v-if="!canRentCabinet" style="margin-top: 12px; padding: 10px; background: #fef0f0; border-radius: 4px; color: #f56c6c;">
            <el-icon><Warning /></el-icon>
            <span style="margin-left: 5px;">{{ cabinetErrorMsg }}</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createOrder" :disabled="!canRent">确定租借</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showReturn" title="归还充电宝" width="500px">
      <el-form label-width="100px">
        <el-form-item label="订单号">
          <span>{{ selectedOrder?.orderNo }}</span>
        </el-form-item>
        <el-form-item label="用户">
          <span>{{ getUserName(selectedOrder?.userId) }}</span>
        </el-form-item>
        <el-form-item label="归还柜机">
          <el-select v-model="returnCabinetId" style="width: 100%">
            <el-option v-for="cabinet in cabinets" :key="cabinet.id" :label="cabinet.name" :value="cabinet.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReturn = false">取消</el-button>
        <el-button type="primary" @click="returnPowerBank">确定归还</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showDetail" title="订单详情" width="800px">
      <el-descriptions :column="2" border v-if="detailOrder">
        <el-descriptions-item label="订单号">{{ detailOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(detailOrder.status)">
            {{ getStatusText(detailOrder.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户">{{ getUserName(detailOrder.userId) }}</el-descriptions-item>
        <el-descriptions-item label="租借柜机">{{ getCabinetName(detailOrder.rentCabinetId) }}</el-descriptions-item>
        <el-descriptions-item label="归还柜机">{{ detailOrder.returnCabinetId ? getCabinetName(detailOrder.returnCabinetId) : '-' }}</el-descriptions-item>
        <el-descriptions-item label="押金金额">¥{{ detailOrder.depositAmount }}</el-descriptions-item>
        <el-descriptions-item label="租金费用">¥{{ detailOrder.rentalFee || '0' }}</el-descriptions-item>
        <el-descriptions-item label="赔偿金额">
          <span v-if="detailOrder.compensationAmount" style="color: #f56c6c;">¥{{ detailOrder.compensationAmount }}</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="租借时间">{{ formatTime(detailOrder.rentTime) }}</el-descriptions-item>
        <el-descriptions-item label="归还时间">{{ formatTime(detailOrder.returnTime) }}</el-descriptions-item>
      </el-descriptions>

      <div style="margin-top: 20px;">
        <h4 style="margin-bottom: 15px; font-weight: bold;">
          <el-icon><Document /></el-icon> 锁定释放记录
        </h4>
        <el-table :data="orderLogs" stripe size="small">
          <el-table-column prop="targetType" label="类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getLogTargetType(row.targetType)" size="small">{{ getLogTargetText(row.targetType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operationType" label="操作" width="100">
            <template #default="{ row }">
              <el-tag :type="getLogOpType(row.operationType)" size="small">{{ getLogOpText(row.operationType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="beforeStatus" label="操作前" />
          <el-table-column prop="afterStatus" label="操作后" />
          <el-table-column prop="remark" label="备注" min-width="200" />
          <el-table-column prop="createTime" label="时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose, Warning, Lock, Box, Document } from '@element-plus/icons-vue'
import api from '../api'

const orders = ref([])
const users = ref([])
const cabinets = ref([])
const showCreateDialog = ref(false)
const showReturn = ref(false)
const showDetail = ref(false)
const selectedOrder = ref(null)
const detailOrder = ref(null)
const returnCabinetId = ref('')
const orderLogs = ref([])
const newOrder = ref({ userId: '', cabinetId: '' })

const selectedUser = computed(() => {
  return users.value.find(u => u.id === newOrder.value.userId)
})

const selectedCabinet = computed(() => {
  return cabinets.value.find(c => c.id === newOrder.value.cabinetId)
})

const canRentUser = computed(() => {
  if (!selectedUser.value) return false
  if (selectedUser.value.creditScore < 60) return false
  if (Number(selectedUser.value.deposit) < 99) return false
  if (selectedUser.value.depositLocked) return false
  return true
})

const canRentCabinet = computed(() => {
  if (!selectedCabinet.value) return false
  if (selectedCabinet.value.status !== 'ONLINE') return false
  if (selectedCabinet.value.occupiedSlots <= 0) return false
  return true
})

const canRent = computed(() => {
  return canRentUser.value && canRentCabinet.value
})

const userErrorMsg = computed(() => {
  if (!selectedUser.value) return ''
  if (selectedUser.value.creditScore < 60) return '信用分不足 60，无法租借'
  if (Number(selectedUser.value.deposit) < 99) return '押金余额不足 ¥99，请先充值'
  if (selectedUser.value.depositLocked) return '押金已被锁定，请先完成当前订单'
  return ''
})

const cabinetErrorMsg = computed(() => {
  if (!selectedCabinet.value) return ''
  if (selectedCabinet.value.status !== 'ONLINE') return '柜机不在线，请选择其他柜机'
  if (selectedCabinet.value.occupiedSlots <= 0) return '柜机无可租借充电宝'
  return ''
})

const getStatusType = (status) => {
  const map = {
    'PENDING_PAYMENT': 'warning',
    'RENTING': 'primary',
    'RETURNED': 'success',
    'OVERDUE': 'warning',
    'LOST': 'danger',
    'ABNORMAL_CLOSED': 'info'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING_PAYMENT': '待支付',
    'RENTING': '租借中',
    'RETURNED': '已归还',
    'OVERDUE': '超时',
    'LOST': '已丢失',
    'ABNORMAL_CLOSED': '异常关闭'
  }
  return map[status] || status
}

const getUserName = (id) => {
  const user = users.value.find(u => u.id === id)
  return user ? user.username : id.substring(0, 8)
}

const getCabinetName = (id) => {
  const cabinet = cabinets.value.find(c => c.id === id)
  return cabinet ? cabinet.name : id.substring(0, 8)
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  return d.toLocaleString()
}

const getLogTargetType = (type) => {
  const map = { 'DEPOSIT': 'warning', 'CABINET_STOCK': 'primary', 'ORDER': 'success' }
  return map[type] || 'info'
}

const getLogTargetText = (type) => {
  const map = { 'DEPOSIT': '押金', 'CABINET_STOCK': '柜机库存', 'ORDER': '订单' }
  return map[type] || type
}

const getLogOpType = (op) => {
  const map = { 'LOCK': 'warning', 'UNLOCK': 'success', 'DEDUCT': 'danger', 'CREATE': 'primary', 'RETURN': 'success', 'LOST': 'danger' }
  return map[op] || 'info'
}

const getLogOpText = (op) => {
  const map = { 'LOCK': '锁定', 'UNLOCK': '释放', 'DEDUCT': '扣除', 'CREATE': '创建', 'RETURN': '归还', 'LOST': '丢失' }
  return map[op] || op
}

const loadOrders = async () => {
  try {
    const res = await api.get('/orders')
    orders.value = res.data
  } catch (e) {
    ElMessage.error('加载订单失败')
  }
}

const loadUsers = async () => {
  try {
    const res = await api.get('/users')
    users.value = res.data
  } catch (e) {
    console.error(e)
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

const openCreateDialog = () => {
  newOrder.value = { userId: '', cabinetId: '' }
  showCreateDialog.value = true
}

const createOrder = async () => {
  try {
    await api.post('/orders', newOrder.value)
    ElMessage.success('租借成功')
    showCreateDialog.value = false
    newOrder.value = { userId: '', cabinetId: '' }
    loadOrders()
    loadCabinets()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '租借失败')
  }
}

const showReturnDialog = (order) => {
  selectedOrder.value = order
  returnCabinetId.value = order.rentCabinetId
  showReturn.value = true
}

const returnPowerBank = async () => {
  try {
    await api.post(`/orders/${selectedOrder.value.id}/return`, { cabinetId: returnCabinetId.value })
    ElMessage.success('归还成功')
    showReturn.value = false
    loadOrders()
    loadCabinets()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '归还失败')
  }
}

const reportLost = async (id) => {
  try {
    await api.post(`/orders/${id}/lost`)
    ElMessage.success('报失成功')
    loadOrders()
  } catch (e) {
    ElMessage.error('报失失败')
  }
}

const viewOrderDetail = async (order) => {
  detailOrder.value = order
  showDetail.value = true
  try {
    const res = await api.get(`/orders/${order.id}/logs`)
    orderLogs.value = res.data
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadOrders()
  loadUsers()
  loadCabinets()
})
</script>
