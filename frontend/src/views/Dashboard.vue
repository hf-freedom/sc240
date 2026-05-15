<template>
  <div>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">总用户数</p>
              <p style="font-size: 32px; font-weight: bold; color: #409EFF">{{ stats.totalUsers }}</p>
            </div>
            <el-icon size="40" color="#409EFF"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">在线柜机</p>
              <p style="font-size: 32px; font-weight: bold; color: #67C23A">{{ stats.onlineCabinets }}/{{ stats.totalCabinets }}</p>
            </div>
            <el-icon size="40" color="#67C23A"><Box /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">可借充电宝</p>
              <p style="font-size: 32px; font-weight: bold; color: #67C23A">{{ stats.availablePowerBanks }}/{{ stats.totalPowerBanks }}</p>
              <p v-if="stats.offlineCabinetPbs > 0" style="font-size: 12px; color: #F56C6C; margin-top: 5px">
                <el-icon size="12"><Warning /></el-icon>
                离线柜机: {{ stats.offlineCabinetPbs }} 个不可用
              </p>
            </div>
            <el-icon size="40" color="#67C23A"><Lightning /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">总订单数</p>
              <p style="font-size: 32px; font-weight: bold; color: #F56C6C">{{ stats.totalOrders }}</p>
            </div>
            <el-icon size="40" color="#F56C6C"><Document /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">租借中订单</p>
              <p style="font-size: 28px; font-weight: bold; color: #409EFF">{{ stats.rentingOrders }}</p>
            </div>
            <el-icon size="35" color="#409EFF"><Clock /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">超时订单</p>
              <p style="font-size: 28px; font-weight: bold; color: #E6A23C">{{ stats.overdueOrders }}</p>
            </div>
            <el-icon size="35" color="#E6A23C"><Timer /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">丢失订单</p>
              <p style="font-size: 28px; font-weight: bold; color: #F56C6C">{{ stats.lostOrders }}</p>
            </div>
            <el-icon size="35" color="#F56C6C"><Delete /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <p style="color: #909399; font-size: 14px">待处理异常</p>
              <p style="font-size: 28px; font-weight: bold; color: #F56C6C">{{ stats.pendingAbnormals }}</p>
            </div>
            <el-icon size="35" color="#F56C6C"><Warning /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">营收统计</span>
            </div>
          </template>
          <div style="padding: 20px 0">
            <div style="display: flex; justify-content: space-around">
              <div style="text-align: center">
                <p style="color: #909399; font-size: 14px">总租赁收入</p>
                <p style="font-size: 36px; font-weight: bold; color: #67C23A">¥{{ stats.totalRevenue }}</p>
              </div>
              <div style="text-align: center">
                <p style="color: #909399; font-size: 14px">总赔偿收入</p>
                <p style="font-size: 36px; font-weight: bold; color: #E6A23C">¥{{ stats.totalCompensation }}</p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span style="font-weight: bold">快速操作</span>
            </div>
          </template>
          <div style="padding: 20px 0">
            <el-row :gutter="10">
              <el-col :span="12" style="margin-bottom: 10px">
                <el-button type="primary" style="width: 100%" @click="$router.push('/order')">扫码租借</el-button>
              </el-col>
              <el-col :span="12" style="margin-bottom: 10px">
                <el-button type="success" style="width: 100%" @click="$router.push('/cabinet')">网点调拨</el-button>
              </el-col>
              <el-col :span="12">
                <el-button type="warning" style="width: 100%" @click="$router.push('/abnormal')">异常处理</el-button>
              </el-col>
              <el-col :span="12">
                <el-button type="danger" style="width: 100%" @click="$router.push('/user')">押金管理</el-button>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import api from '../api'

const stats = ref({})

const loadStats = async () => {
  try {
    const res = await api.get('/stats')
    stats.value = res.data
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadStats()
})
</script>
