<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold">异常记录</span>
          <div>
            <el-badge :value="pendingCount" class="item" type="danger" style="margin-right: 20px">
              <span>待处理</span>
            </el-badge>
            <el-badge :value="processingCount" class="item" type="warning">
              <span>处理中</span>
            </el-badge>
          </div>
        </div>
      </template>
      <el-table :data="abnormals" stripe>
        <el-table-column prop="type" label="异常类型" width="150">
          <template #default="{ row }">
            <el-tag :type="getTypeColor(row.type)">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="relatedType" label="关联类型" width="120">
          <template #default="{ row }">
            {{ getRelatedTypeText(row.relatedType) }}
          </template>
        </el-table-column>
        <el-table-column prop="relatedId" label="关联ID" width="150">
          <template #default="{ row }">
            {{ row.relatedId?.substring(0, 12) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="handler" label="处理人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="openHandleDialog(row)">
              <el-icon><Tools /></el-icon>处理
            </el-button>
            <el-button size="small" @click="viewDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showDetail" title="异常详情" width="800px">
      <div v-if="detailData">
        <el-descriptions :column="2" border style="margin-bottom: 20px">
          <el-descriptions-item label="异常类型">
            <el-tag :type="getTypeColor(detailData.record.type)">{{ getTypeText(detailData.record.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.record.status)">{{ getStatusText(detailData.record.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联类型">{{ getRelatedTypeText(detailData.record.relatedType) }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detailData.record.handler || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailData.record.description }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(detailData.record.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatTime(detailData.record.handleTime) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detailData.cabinet">
          <h4 style="margin-bottom: 15px; font-weight: bold">柜机信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="柜机名称">{{ detailData.cabinet.name }}</el-descriptions-item>
            <el-descriptions-item label="位置">{{ detailData.cabinet.location }}</el-descriptions-item>
            <el-descriptions-item label="总槽位">{{ detailData.cabinet.totalSlots }}</el-descriptions-item>
            <el-descriptions-item label="可用槽位">{{ detailData.cabinet.availableSlots }}</el-descriptions-item>
            <el-descriptions-item label="充电宝数量">{{ detailData.powerBanks?.length || 0 }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="detailData.order">
          <h4 style="margin-bottom: 15px; font-weight: bold">订单信息</h4>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="订单号">{{ detailData.order.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="用户">{{ detailData.user?.username }}</el-descriptions-item>
            <el-descriptions-item label="押金金额">¥{{ detailData.order.depositAmount }}</el-descriptions-item>
            <el-descriptions-item label="租金">¥{{ detailData.order.rentalFee || 0 }}</el-descriptions-item>
            <el-descriptions-item label="租借柜机">{{ getCabinetName(detailData.order.rentCabinetId) }}</el-descriptions-item>
            <el-descriptions-item label="租借时间">{{ formatTime(detailData.order.rentTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showHandle" title="处理异常" width="900px">
      <div v-if="selectedRecord">
        <el-alert :title="selectedRecord.description" :type="getAlertType(selectedRecord.type)" style="margin-bottom: 20px" />
        
        <el-tabs v-model="activeTab">
          <el-tab-pane label="快速操作" name="quick">
            <div style="padding: 20px 0">
              <el-row :gutter="20">
                <el-col :span="8" v-if="selectedRecord.type === 'CABINET_OFFLINE'">
                  <el-card shadow="hover" class="action-card" @click="doRecoverCabinet">
                    <div style="text-align: center">
                      <el-icon size="40" color="#67c23a"><Connection /></el-icon>
                      <h4 style="margin: 15px 0 10px">恢复柜机在线</h4>
                      <p style="color: #909399; font-size: 12px">手动恢复柜机在线状态，恢复正常服务</p>
                    </div>
                  </el-card>
                </el-col>
                
                <el-col :span="8" v-if="selectedRecord.type === 'CABINET_OFFLINE'">
                  <el-card shadow="hover" class="action-card" @click="doRollback">
                    <div style="text-align: center">
                      <el-icon size="40" color="#e6a23c"><RefreshLeft /></el-icon>
                      <h4 style="margin: 15px 0 10px">回滚占用资源</h4>
                      <p style="color: #909399; font-size: 12px">释放该柜机关联的充电宝和用户押金</p>
                    </div>
                  </el-card>
                </el-col>
                
                <el-col :span="8">
                  <el-card shadow="hover" class="action-card" @click="showTaskDialog = true">
                    <div style="text-align: center">
                      <el-icon size="40" color="#409eff"><List /></el-icon>
                      <h4 style="margin: 15px 0 10px">生成处理任务</h4>
                      <p style="color: #909399; font-size: 12px">创建处理任务并指派给相关人员</p>
                    </div>
                  </el-card>
                </el-col>
                
                <el-col :span="8" v-if="selectedRecord.type === 'ORDER_OVERDUE' || selectedRecord.type === 'CROSS_SITE_RETURN'">
                  <el-card shadow="hover" class="action-card" @click="doCloseOrder">
                    <div style="text-align: center">
                      <el-icon size="40" color="#909399"><SwitchButton /></el-icon>
                      <h4 style="margin: 15px 0 10px">关闭订单并释放</h4>
                      <p style="color: #909399; font-size: 12px">异常关闭订单，释放押金和资源</p>
                    </div>
                  </el-card>
                </el-col>
                
                <el-col :span="8" v-if="selectedRecord.type === 'POWERBANK_LOST' || selectedRecord.type === 'AUTO_LOST'">
                  <el-card shadow="hover" class="action-card" @click="doLostCompensation">
                    <div style="text-align: center">
                      <el-icon size="40" color="#f56c6c"><Warning /></el-icon>
                      <h4 style="margin: 15px 0 10px">执行丢失赔偿</h4>
                      <p style="color: #909399; font-size: 12px">扣除用户押金作为赔偿，标记充电宝丢失</p>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="手动处理" name="manual">
            <el-form label-width="100px" style="padding: 20px 0">
              <el-form-item label="处理人">
                <el-input v-model="handler" placeholder="请输入处理人" />
              </el-form-item>
              <el-form-item label="处理备注">
                <el-input v-model="remark" type="textarea" :rows="4" placeholder="请输入处理备注" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="confirmHandle">确认处理完成</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <el-dialog v-model="showTaskDialog" title="生成处理任务" width="600px">
      <el-form label-width="100px">
        <el-form-item label="处理人">
          <el-input v-model="taskHandler" placeholder="请输入创建任务的人" />
        </el-form-item>
        <el-form-item label="任务描述">
          <el-input v-model="taskDesc" type="textarea" :rows="3" placeholder="请描述任务内容" />
        </el-form-item>
        <el-form-item label="指派给">
          <el-input v-model="taskAssignee" placeholder="请输入指派人员" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmCreateTask">创建任务</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showConfirmDialog" title="确认操作" width="500px">
      <div style="text-align: center; padding: 20px 0">
        <el-icon :size="60" :color="confirmColor"><Warning /></el-icon>
        <p style="margin-top: 20px; font-size: 16px">{{ confirmMessage }}</p>
        <p style="color: #909399; margin-top: 10px">此操作不可撤销，请谨慎操作</p>
      </div>
      <template #footer>
        <el-button @click="showConfirmDialog = false">取消</el-button>
        <el-button type="primary" @click="executeConfirmAction">确认执行</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Tools, View, Connection, RefreshLeft, List, SwitchButton, Warning } from '@element-plus/icons-vue'
import api from '../api'

const abnormals = ref([])
const showDetail = ref(false)
const showHandle = ref(false)
const showTaskDialog = ref(false)
const showConfirmDialog = ref(false)
const detailData = ref(null)
const selectedRecord = ref(null)
const handler = ref('管理员')
const remark = ref('')
const activeTab = ref('quick')
const taskHandler = ref('管理员')
const taskDesc = ref('')
const taskAssignee = ref('')
const confirmMessage = ref('')
const confirmAction = ref('')
const confirmColor = ref('#e6a23c')
const cabinets = ref([])

const pendingCount = computed(() => {
  return abnormals.value.filter(a => a.status === 'PENDING').length
})

const processingCount = computed(() => {
  return abnormals.value.filter(a => a.status === 'PROCESSING').length
})

const getTypeColor = (type) => {
  const map = {
    'CABINET_OFFLINE': 'danger',
    'CABINET_FULL': 'warning',
    'POWERBANK_LOST': 'danger',
    'ORDER_OVERDUE': 'warning',
    'CROSS_SITE_RETURN': 'info',
    'AUTO_LOST': 'danger'
  }
  return map[type] || 'info'
}

const getTypeText = (type) => {
  const map = {
    'CABINET_OFFLINE': '柜机离线',
    'CABINET_FULL': '柜机已满',
    'POWERBANK_LOST': '充电宝丢失',
    'ORDER_OVERDUE': '订单超时',
    'CROSS_SITE_RETURN': '跨网点归还',
    'AUTO_LOST': '自动报失'
  }
  return map[type] || type
}

const getRelatedTypeText = (type) => {
  const map = {
    'CABINET': '柜机',
    'ORDER': '订单',
    'POWERBANK': '充电宝',
    'USER': '用户'
  }
  return map[type] || type
}

const getStatusType = (status) => {
  const map = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'HANDLED': 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'HANDLED': '已处理'
  }
  return map[status] || status
}

const getAlertType = (type) => {
  const map = {
    'CABINET_OFFLINE': 'error',
    'CABINET_FULL': 'warning',
    'POWERBANK_LOST': 'error',
    'ORDER_OVERDUE': 'warning',
    'CROSS_SITE_RETURN': 'info',
    'AUTO_LOST': 'error'
  }
  return map[type] || 'info'
}

const getCabinetName = (id) => {
  const cabinet = cabinets.value.find(c => c.id === id)
  return cabinet ? cabinet.name : id?.substring(0, 8) || '-'
}

const formatTime = (time) => {
  if (!time) return '-'
  const d = new Date(time)
  return d.toLocaleString()
}

const loadAbnormals = async () => {
  try {
    const res = await api.get('/abnormals')
    abnormals.value = res.data
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

const viewDetail = async (record) => {
  try {
    const res = await api.get(`/abnormals/${record.id}/detail`)
    detailData.value = res.data
    showDetail.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const openHandleDialog = (record) => {
  selectedRecord.value = record
  handler.value = '管理员'
  remark.value = ''
  taskHandler.value = '管理员'
  taskDesc.value = getTypeText(record.type) + '处理'
  taskAssignee.value = ''
  activeTab.value = 'quick'
  showHandle.value = true
}

const confirmHandle = async () => {
  try {
    await api.post(`/abnormals/${selectedRecord.value.id}/handle`, {
      handler: handler.value,
      remark: remark.value
    })
    ElMessage.success('处理成功')
    showHandle.value = false
    loadAbnormals()
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

const confirmCreateTask = async () => {
  try {
    await api.post(`/abnormals/${selectedRecord.value.id}/create-task`, {
      handler: taskHandler.value,
      taskDesc: taskDesc.value,
      assignee: taskAssignee.value
    })
    ElMessage.success('任务创建成功')
    showTaskDialog.value = false
    showHandle.value = false
    loadAbnormals()
  } catch (e) {
    ElMessage.error('创建任务失败')
  }
}

const doRecoverCabinet = () => {
  confirmMessage.value = '确认要恢复该柜机的在线状态吗？'
  confirmAction.value = 'recoverCabinet'
  confirmColor.value = '#67c23a'
  showConfirmDialog.value = true
}

const doRollback = () => {
  confirmMessage.value = '确认要回滚该柜机的所有占用资源吗？此操作将释放所有关联的充电宝状态和用户押金。'
  confirmAction.value = 'rollback'
  confirmColor.value = '#e6a23c'
  showConfirmDialog.value = true
}

const doCloseOrder = () => {
  confirmMessage.value = '确认要异常关闭该订单并释放所有资源吗？'
  confirmAction.value = 'closeOrder'
  confirmColor.value = '#909399'
  showConfirmDialog.value = true
}

const doLostCompensation = () => {
  confirmMessage.value = '确认要执行丢失赔偿吗？将扣除用户押金并标记充电宝为丢失状态。'
  confirmAction.value = 'lostCompensation'
  confirmColor.value = '#f56c6c'
  showConfirmDialog.value = true
}

const executeConfirmAction = async () => {
  try {
    switch (confirmAction.value) {
      case 'recoverCabinet':
        await api.post(`/abnormals/${selectedRecord.value.id}/recover-cabinet`, {
          cabinetId: selectedRecord.value.relatedId,
          handler: '管理员'
        })
        ElMessage.success('柜机已恢复在线')
        break
      case 'rollback':
        await api.post(`/abnormals/${selectedRecord.value.id}/rollback`, {
          handler: '管理员'
        })
        ElMessage.success('资源已回滚')
        break
      case 'closeOrder':
        await api.post(`/abnormals/${selectedRecord.value.id}/close-order`, {
          orderId: selectedRecord.value.relatedId,
          handler: '管理员',
          reason: '异常处理关闭订单'
        })
        ElMessage.success('订单已关闭并释放资源')
        break
      case 'lostCompensation':
        await api.post(`/abnormals/${selectedRecord.value.id}/lost-compensation`, {
          orderId: selectedRecord.value.relatedId,
          handler: '管理员'
        })
        ElMessage.success('丢失赔偿已执行')
        break
    }
    showConfirmDialog.value = false
    showHandle.value = false
    loadAbnormals()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadAbnormals()
  loadCabinets()
})
</script>

<style scoped>
.action-card {
  cursor: pointer;
  transition: all 0.3s;
}
.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}
</style>
