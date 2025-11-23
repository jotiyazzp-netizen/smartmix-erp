<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  PieChartOutlined,
  DesktopOutlined,
  UserOutlined,
  FileOutlined,
  LogoutOutlined
} from '@ant-design/icons-vue'

const collapsed = ref(false)
const selectedKeys = ref<string[]>(['dashboard'])
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// Set selected key based on route
selectedKeys.value = [route.path.split('/')[1] || 'dashboard']

const onMenuClick = (e: any) => {
  router.push('/' + e.key)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <a-layout style="min-height: 100vh">
    <a-layout-sider v-model:collapsed="collapsed" collapsible :width="220">
      <div class="logo" />
      <a-menu v-model:selectedKeys="selectedKeys" theme="dark" mode="inline" @click="onMenuClick">
        <a-menu-item key="dashboard">
          <pie-chart-outlined />
          <span>仪表盘</span>
        </a-menu-item>
        <a-menu-item key="materials">
          <desktop-outlined />
          <span>材料与价格</span>
        </a-menu-item>
        <a-sub-menu key="mix">
          <template #title>
            <span>
              <user-outlined />
              <span>配比管理</span>
            </span>
          </template>
          <a-menu-item key="mix/recipes">配比库</a-menu-item>
        </a-sub-menu>
        <a-menu-item key="tasks">
          <file-outlined />
          <span>生产任务</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background: #fff; padding: 0 16px; display: flex; justify-content: flex-end; align-items: center;">
        <a-dropdown>
          <a class="ant-dropdown-link" @click.prevent>
            {{ userStore.info.name || 'User' }} <UserOutlined />
          </a>
          <template #overlay>
            <a-menu>
              <a-menu-item @click="handleLogout">
                <LogoutOutlined /> 退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>
      <a-layout-content style="margin:16px">
        <div class="content-wrap">
          <slot></slot>
        </div>
      </a-layout-content>
      <a-layout-footer style="text-align: center">
        SmartMix ©2025 Created by Antigravity
      </a-layout-footer>
    </a-layout>
  </a-layout>
</template>

<style scoped>
.logo {
  height: 32px;
  margin: 16px;
  background: rgba(255, 255, 255, 0.3);
}
.content-wrap {
  padding: 24px;
  background: #fff;
  min-height: 360px;
}
</style>
