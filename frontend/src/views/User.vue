<template>
  <div>
    <el-card shadow="hover" style="margin-bottom: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span style="font-weight: bold">用户列表</span>
          <el-button type="primary" @click="showCreateDialog = true">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>
      <el-table :data="users" stripe>
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="creditScore" label="信用分">
          <template #default="{ row }">
            <el-tag :type="row.creditScore >= 80 ? 'success' : row.creditScore >= 60 ? 'warning' : 'danger'">
              {{ row.creditScore }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deposit" label="押金余额">
          <template #default="{ row }">
            ¥{{ row.deposit }}
          </template>
        </el-table-column>
        <el-table-column prop="depositLocked" label="押金状态">
          <template #default="{ row }">
            <el-tag :type="row.depositLocked ? 'warning' : 'success'">
              {{ row.depositLocked ? '已锁定' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button size="small" @click="showRechargeDialog(row)">充值押金</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showCreateDialog" title="新增用户" width="500px">
      <el-form :model="newUser" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="newUser.username" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="newUser.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="createUser">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showRecharge" title="充值押金" width="500px">
      <el-form label-width="80px">
        <el-form-item label="当前用户">
          <span>{{ selectedUser?.username }}</span>
        </el-form-item>
        <el-form-item label="当前押金">
          <span>¥{{ selectedUser?.deposit }}</span>
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="1" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRecharge = false">取消</el-button>
        <el-button type="primary" @click="rechargeDeposit">确定充值</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const users = ref([])
const showCreateDialog = ref(false)
const showRecharge = ref(false)
const selectedUser = ref(null)
const rechargeAmount = ref(100)
const newUser = ref({ username: '', phone: '' })

const loadUsers = async () => {
  try {
    const res = await api.get('/users')
    users.value = res.data
  } catch (e) {
    ElMessage.error('加载用户失败')
  }
}

const createUser = async () => {
  try {
    await api.post('/users', newUser.value)
    ElMessage.success('创建成功')
    showCreateDialog.value = false
    newUser.value = { username: '', phone: '' }
    loadUsers()
  } catch (e) {
    ElMessage.error('创建失败')
  }
}

const showRechargeDialog = (user) => {
  selectedUser.value = user
  showRecharge.value = true
}

const rechargeDeposit = async () => {
  try {
    await api.post(`/users/${selectedUser.value.id}/deposit`, { amount: rechargeAmount.value })
    ElMessage.success('充值成功')
    showRecharge.value = false
    loadUsers()
  } catch (e) {
    ElMessage.error('充值失败')
  }
}

onMounted(() => {
  loadUsers()
})
</script>
