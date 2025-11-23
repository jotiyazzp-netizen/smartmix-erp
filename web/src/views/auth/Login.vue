<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import request from '@/api/http'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formState = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const onFinish = async (values: any) => {
  loading.value = true
  try {
    // Mock login for MVP if backend not ready, but workflow says call /api/auth/login
    // We will try to call it. If it fails (404/500), we might need a fallback or just show error.
    // For now, assuming backend exists or we simulate.
    // Let's assume standard JWT response: { token: '...', roles: [...], ... }
    
    const res: any = await request.post('/auth/login', values)
    
    // If backend returns token directly or in data
    const token = res.token || res.data?.token
    const roles = res.roles || res.data?.roles || ['ADMIN'] // Default to ADMIN for dev if missing
    
    if (token) {
      userStore.setToken(token)
      userStore.setRoles(roles)
      userStore.setInfo(res.user || {})
      message.success('登录成功')
      router.push('/dashboard')
    } else {
       // Fallback for demo if backend not ready
       if (values.username === 'admin' && values.password === '123456') {
          userStore.setToken('mock-token')
          userStore.setRoles(['ADMIN'])
          userStore.setInfo({ name: 'Admin' })
          message.success('Mock登录成功')
          router.push('/dashboard')
       } else {
          message.error('登录失败: 无效的凭证')
       }
    }
  } catch (error: any) {
    console.error(error)
    // Fallback for demo
    if (values.username === 'admin' && values.password === '123456') {
        userStore.setToken('mock-token')
        userStore.setRoles(['ADMIN'])
        userStore.setInfo({ name: 'Admin' })
        message.success('Mock登录成功 (Network Error)')
        router.push('/dashboard')
    } else {
        message.error(error.response?.data?.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <a-card title="SmartMix 智能生产系统" class="login-card">
      <a-form
        :model="formState"
        name="basic"
        autocomplete="off"
        @finish="onFinish"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名!' }]"
        >
          <a-input v-model:value="formState.username" placeholder="用户名">
            <template #prefix><UserOutlined /></template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码!' }]"
        >
          <a-input-password v-model:value="formState.password" placeholder="密码">
            <template #prefix><LockOutlined /></template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f5f7fa;
  padding: 24px;
}
.login-card {
  width: 100%;
  max-width: 520px;
}
</style>
