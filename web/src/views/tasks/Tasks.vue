<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { message } from 'ant-design-vue'
import { PlusOutlined, CalculatorOutlined, FilePdfOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isDispatchOrAdmin = computed(() => userStore.roles.includes('DISPATCH') || userStore.roles.includes('ADMIN'))

interface ProductionTask {
  id: string
  taskNumber: string
  projectName: string
  strengthGrade: string
  volume: number
  slump: string
  source: 'ERP' | 'MANUAL'
  status: string
  selectedRecipeNumber?: string
  unitCost?: number
  totalCost?: number
  createTime: string
}

interface CostRecommendation {
  recipeNumber: string
  unitCost: number
  totalCost: number
  materialsSummary: string
  isRecommended: boolean
}

const columns: TableColumnsType = [
  { title: '任务号', dataIndex: 'taskNumber', key: 'taskNumber' },
  { title: '工程名称', dataIndex: 'projectName', key: 'projectName' },
  { title: '强度等级', dataIndex: 'strengthGrade', key: 'strengthGrade' },
  { title: '方量 (m³)', dataIndex: 'volume', key: 'volume' },
  { title: '来源', dataIndex: 'source', key: 'source' },
  { title: '选定配比', dataIndex: 'selectedRecipeNumber', key: 'selectedRecipeNumber' },
  { title: '单方成本', dataIndex: 'unitCost', key: 'unitCost' },
  { title: '总成本', dataIndex: 'totalCost', key: 'totalCost' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 200 }
]

const data = ref<ProductionTask[]>([])
const loading = ref(false)

const fetchTasks = async () => {
  loading.value = true
  try {
    // Mock API
    data.value = [
      {
        id: '1',
        taskNumber: 'T20251121001',
        projectName: '万达广场A区',
        strengthGrade: 'C30',
        volume: 100,
        slump: '120±20',
        source: 'ERP',
        status: 'CREATED',
        createTime: '2025-11-21 08:30:00'
      },
      {
        id: '2',
        taskNumber: 'T20251121002',
        projectName: '市政大桥',
        strengthGrade: 'C40',
        volume: 500,
        slump: '160±20',
        source: 'MANUAL',
        status: 'CREATED',
        selectedRecipeNumber: 'C40-005',
        unitCost: 320.5,
        totalCost: 160250,
        createTime: '2025-11-21 09:15:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

// Create Task Modal
const createVisible = ref(false)
const createForm = reactive({
  taskNumber: '',
  projectName: '',
  strengthGrade: '',
  volume: 0,
  slump: '',
  specialRequirements: '',
  sapOrderNumber: ''
})

const handleCreate = () => {
  createVisible.value = true
  Object.assign(createForm, {
    taskNumber: 'T' + new Date().getTime(),
    projectName: '',
    strengthGrade: '',
    volume: 0,
    slump: '',
    specialRequirements: '',
    sapOrderNumber: ''
  })
}

const submitCreate = async () => {
  // Mock submit
  message.success('任务创建成功')
  createVisible.value = false
  fetchTasks()
}

// Cost Recommendation Modal
const costVisible = ref(false)
const recommendations = ref<CostRecommendation[]>([])
const currentTaskId = ref('')
const recommending = ref(false)

const showRecommendations = async (record: ProductionTask) => {
  currentTaskId.value = record.id
  costVisible.value = true
  recommending.value = true
  try {
    // Mock API call: /api/cost/recommendations?strengthGrade=...
    // await request.get(...)
    
    // Mock data
    recommendations.value = [
      { recipeNumber: 'C30-001', unitCost: 285.5, totalCost: 285.5 * record.volume, materialsSummary: '水泥:300, 砂:750, 石:1050', isRecommended: true },
      { recipeNumber: 'C30-003', unitCost: 290.0, totalCost: 290.0 * record.volume, materialsSummary: '水泥:310, 砂:740, 石:1060', isRecommended: false },
      { recipeNumber: 'C30-OLD', unitCost: 305.2, totalCost: 305.2 * record.volume, materialsSummary: '水泥:330, 砂:720, 石:1040', isRecommended: false }
    ]
  } finally {
    recommending.value = false
  }
}

const selectRecipe = async (recipe: CostRecommendation) => {
  // Mock API: POST /api/tasks/{id}/select-mix
  message.success(`已选择配比 ${recipe.recipeNumber}`)
  costVisible.value = false
  
  // Update local data
  const task = data.value.find(t => t.id === currentTaskId.value)
  if (task) {
    task.selectedRecipeNumber = recipe.recipeNumber
    task.unitCost = recipe.unitCost
    task.totalCost = recipe.totalCost
  }
}

const downloadPdf = (_record: ProductionTask) => {
  // Mock download
  // window.open(`${import.meta.env.VITE_API_BASE_URL}/tasks/${record.id}/pdf`)
  message.success('正在下载任务单 PDF...')
}

onMounted(() => {
  fetchTasks()
})
</script>

<template>
  <MainLayout>
    <div class="tasks-page">
      <div class="header">
        <h2>生产任务管理</h2>
        <a-button v-if="isDispatchOrAdmin" type="primary" @click="handleCreate"><PlusOutlined /> 创建生产任务</a-button>
      </div>

      <a-table :columns="columns" :data-source="data" :loading="loading" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a v-if="isDispatchOrAdmin" @click="showRecommendations(record)"><CalculatorOutlined /> 成本推荐</a>
              <a v-if="isDispatchOrAdmin" @click="downloadPdf(record)"><FilePdfOutlined /> 下载任务单</a>
            </a-space>
          </template>
        </template>
      </a-table>

      <!-- Create Modal -->
      <a-modal v-model:open="createVisible" title="创建生产任务" @ok="submitCreate">
        <a-form :model="createForm" layout="vertical">
          <a-form-item label="任务号" required><a-input v-model:value="createForm.taskNumber" /></a-form-item>
          <a-form-item label="工程名称" required><a-input v-model:value="createForm.projectName" /></a-form-item>
          <a-form-item label="强度等级" required>
            <a-select v-model:value="createForm.strengthGrade">
              <a-select-option value="C30">C30</a-select-option>
              <a-select-option value="C40">C40</a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item label="需求方量 (m³)" required><a-input-number v-model:value="createForm.volume" style="width: 100%" /></a-form-item>
          <a-form-item label="坍落度"><a-input v-model:value="createForm.slump" /></a-form-item>
        </a-form>
      </a-modal>

      <!-- Cost Recommendation Modal -->
      <a-modal v-model:open="costVisible" title="成本优化推荐" width="800px" :footer="null">
        <a-table :data-source="recommendations" :pagination="false" row-key="recipeNumber" :loading="recommending">
          <a-table-column title="配比编号" data-index="recipeNumber">
            <template #default="{ record }">
              {{ record.recipeNumber }}
              <a-tag v-if="record.isRecommended" color="green">最低成本推荐</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="单方成本" data-index="unitCost" />
          <a-table-column title="总成本" data-index="totalCost" />
          <a-table-column title="主要材料" data-index="materialsSummary" />
          <a-table-column title="操作">
            <template #default="{ record }">
              <a-button type="primary" size="small" @click="selectRecipe(record)">选择此配比</a-button>
            </template>
          </a-table-column>
        </a-table>
      </a-modal>
    </div>
  </MainLayout>
</template>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
