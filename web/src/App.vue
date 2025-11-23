<script setup lang="ts">
import { RouterView } from 'vue-router'
import { theme } from 'ant-design-vue'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
</script>

<template>
  <a-config-provider :locale="zhCN" :theme="{ algorithm: theme.defaultAlgorithm }">
    <RouterView />
  </a-config-provider>
</template>

<style>
#app {
  height: 100vh;
}
</style>
