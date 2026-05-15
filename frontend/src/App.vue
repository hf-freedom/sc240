<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #2e3b4e">
      <div style="color: white; padding: 20px; text-align: center; font-size: 18px; font-weight: bold">
        充电宝调度系统
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#2e3b4e"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/cabinet">
          <el-icon><Box /></el-icon>
          <span>柜机管理</span>
        </el-menu-item>
        <el-menu-item index="/powerbank">
          <el-icon><Lightning /></el-icon>
          <span>充电宝管理</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/abnormal">
          <el-icon><Warning /></el-icon>
          <span>异常管理</span>
        </el-menu-item>
        <el-menu-item index="/logs">
          <el-icon><List /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between">
        <span style="font-size: 20px; font-weight: bold">{{ currentPageName }}</span>
        <el-button type="primary" @click="refreshData">
          <el-icon><Refresh /></el-icon>
          刷新数据
        </el-button>
      </el-header>
      <el-main style="background-color: #f5f7fa">
        <router-view @refresh="refreshData" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const currentPageName = computed(() => route.matched[0]?.name || '数据看板')

const refreshData = () => {
  window.location.reload()
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
