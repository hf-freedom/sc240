import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import User from '../views/User.vue'
import Cabinet from '../views/Cabinet.vue'
import PowerBank from '../views/PowerBank.vue'
import Order from '../views/Order.vue'
import Abnormal from '../views/Abnormal.vue'
import Logs from '../views/Logs.vue'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard, name: '数据看板' },
  { path: '/user', component: User, name: '用户管理' },
  { path: '/cabinet', component: Cabinet, name: '柜机管理' },
  { path: '/powerbank', component: PowerBank, name: '充电宝管理' },
  { path: '/order', component: Order, name: '订单管理' },
  { path: '/abnormal', component: Abnormal, name: '异常管理' },
  { path: '/logs', component: Logs, name: '操作日志' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
