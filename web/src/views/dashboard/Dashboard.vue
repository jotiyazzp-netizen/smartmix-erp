<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MainLayout from '@/components/MainLayout.vue'

const stats = ref({
  todayTasks: 0,
  weekTasks: 0,
  c30Cost: 0,
  lastSync: ''
})

const fetchStats = async () => {
  try {
    // Mock APIs or Real APIs
    // const tasksRes = await request.get('/tasks/stats')
    // stats.value.todayTasks = tasksRes.today
    
    // const costRes = await request.get('/cost/recommendations?strengthGrade=C30&volume=1')
    // if (costRes && costRes.length > 0) {
    //   stats.value.c30Cost = costRes[0].totalCost
    // }
    
    // Mock data for now
    stats.value = {
      todayTasks: 12,
      weekTasks: 45,
      c30Cost: 285.5,
      lastSync: '2025-11-21 10:00:00'
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<template>
  <MainLayout>
    <div class="dashboard">
      <a-row :gutter="16">
        <a-col :span="6">
          <a-card title="今日任务数" :bordered="false">
            <div class="stat-value">{{ stats.todayTasks }}</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card title="本周任务数" :bordered="false">
            <div class="stat-value">{{ stats.weekTasks }}</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card title="C30 最低成本 (元/m³)" :bordered="false">
            <div class="stat-value price">¥ {{ stats.c30Cost }}</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card title="ERP 同步状态" :bordered="false">
            <div class="stat-value time">{{ stats.lastSync }}</div>
            <a-tag color="success">正常</a-tag>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </MainLayout>
</template>

<style scoped>
.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #1890ff;
}
.price {
  color: #cf1322;
}
.time {
  font-size: 16px;
  color: #666;
  margin-bottom: 8px;
}
</style>
