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
  username: 'admin',
  password: 'admin123',
  remember: true
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
    const token = res?.data?.token || res?.token
    const roles = res.roles || res.data?.roles || ['ADMIN'] // Default to ADMIN for dev if missing
    
    if (token) {
      userStore.setToken(token)
      userStore.setRoles(roles)
      userStore.setInfo(res.user || {})
      message.success('登录成功')
      router.push('/dashboard')
    } else {
       // Fallback for demo if backend not ready
       if (values.username === 'admin' && values.password === 'admin123') {
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
    if (values.username === 'admin' && values.password === 'admin123') {
        userStore.setToken('mock-token')
        userStore.setRoles(['ADMIN'])
        userStore.setInfo({ name: 'Admin' })
        message.success('Mock登录成功 (Network Error)')
        router.push('/dashboard')
    } else {
        const msg = error?.response?.data?.message || '登录失败'
        message.error(msg)
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <div class="brand">
      <div class="logo">SmartMix</div>
      <div class="subtitle">智能生产 ERP</div>
    </div>
    <a-card class="login-card">
      <div class="card-title">账户登录</div>
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
          <a-checkbox v-model:checked="formState.remember">记住我</a-checkbox>
          <a style="float:right" href="/">忘记密码</a>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block :loading="loading">登录</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.login-container {display:flex;flex-direction:column;justify-content:center;align-items:center;height:100vh;background:linear-gradient(120deg,#1f2a44,#0c1628 60%,#091322);padding:24px}
.brand {text-align:center;color:#fff;margin-bottom:24px}
.brand .logo {font-size:32px;font-weight:700;letter-spacing:1px}
.brand .subtitle {opacity:.8}
.login-card {width:100%;max-width:520px;border-radius:12px;box-shadow:0 10px 24px rgba(0,0,0,.35)}
.card-title {font-size:18px;font-weight:600;margin-bottom:12px}
</style>
