<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, CopyOutlined, CheckOutlined, StopOutlined, EditOutlined } from '@ant-design/icons-vue'
import type { TableColumnsType } from 'ant-design-vue'

interface MixMaterial {
  materialCode: string
  materialName: string
  usagePerCubic: number
}

interface MixRecipe {
  id: string
  recipeNumber: string
  strengthGrade: string
  slump: string
  status: 'PENDING_APPROVAL' | 'APPROVED' | 'DISABLED'
  createdBy: string
  createTime: string
  materials: MixMaterial[]
  remarks?: string
}

const columns: TableColumnsType = [
  { title: '配比编号', dataIndex: 'recipeNumber', key: 'recipeNumber' },
  { title: '强度等级', dataIndex: 'strengthGrade', key: 'strengthGrade' },
  { title: '坍落度', dataIndex: 'slump', key: 'slump' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '创建人', dataIndex: 'createdBy', key: 'createdBy' },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime' },
  { title: '操作', key: 'action', width: 250 }
]

const data = ref<MixRecipe[]>([])
const loading = ref(false)
const filters = reactive({
  strengthGrade: undefined,
  status: undefined
})

const fetchRecipes = async () => {
  loading.value = true
  try {
    // Mock API
    data.value = [
      {
        id: '1',
        recipeNumber: 'C30-001',
        strengthGrade: 'C30',
        slump: '120±20',
        status: 'APPROVED',
        createdBy: 'LabUser',
        createTime: '2025-11-20 10:00:00',
        materials: [
          { materialCode: 'M001', materialName: 'P.O 42.5 水泥', usagePerCubic: 300 },
          { materialCode: 'M002', materialName: '中砂', usagePerCubic: 750 },
          { materialCode: 'M003', materialName: '碎石 5-25mm', usagePerCubic: 1050 },
          { materialCode: 'M006', materialName: '水', usagePerCubic: 180 }
        ]
      },
      {
        id: '2',
        recipeNumber: 'C30-002',
        strengthGrade: 'C30',
        slump: '120±20',
        status: 'PENDING_APPROVAL',
        createdBy: 'LabUser',
        createTime: '2025-11-21 09:00:00',
        materials: [
           { materialCode: 'M001', materialName: 'P.O 42.5 水泥', usagePerCubic: 290 },
           { materialCode: 'M004', materialName: '粉煤灰', usagePerCubic: 60 }
        ]
      }
    ]
  } finally {
    loading.value = false
  }
}

// Modal
const modalVisible = ref(false)
const modalTitle = ref('新建配比')
const formState = reactive<Partial<MixRecipe>>({
  recipeNumber: '',
  strengthGrade: '',
  slump: '',
  materials: []
})

// Mock Materials for Select
const materialOptions = [
  { value: 'M001', label: 'P.O 42.5 水泥' },
  { value: 'M002', label: '中砂' },
  { value: 'M003', label: '碎石 5-25mm' },
  { value: 'M004', label: '粉煤灰 II级' },
  { value: 'M005', label: '外加剂' },
  { value: 'M006', label: '水' }
]

const handleAddMaterial = () => {
  if (!formState.materials) formState.materials = []
  formState.materials.push({ materialCode: '', materialName: '', usagePerCubic: 0 })
}

const handleRemoveMaterial = (index: number) => {
  formState.materials?.splice(index, 1)
}

const handleMaterialChange = (val: string, index: number) => {
  const mat = materialOptions.find(m => m.value === val)
  if (mat && formState.materials && formState.materials[index]) {
    formState.materials[index].materialName = mat.label
  }
}

import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isLabOrAdmin = computed(() => userStore.roles.includes('LAB') || userStore.roles.includes('ADMIN'))

const openModal = (mode: 'create' | 'edit' | 'copy', record?: MixRecipe) => {
  modalVisible.value = true
  if (mode === 'create') {
    modalTitle.value = '新建配比'
    Object.assign(formState, {
      id: undefined,
      recipeNumber: '',
      strengthGrade: '',
      slump: '',
      status: 'PENDING_APPROVAL',
      materials: []
    })
  } else if (mode === 'edit' && record) {
    modalTitle.value = '编辑配比'
    Object.assign(formState, JSON.parse(JSON.stringify(record)))
  } else if (mode === 'copy' && record) {
    modalTitle.value = '复制配比'
    Object.assign(formState, JSON.parse(JSON.stringify(record)))
    formState.id = undefined
    formState.recipeNumber = record.recipeNumber + '-COPY'
    formState.status = 'PENDING_APPROVAL'
  }
}

const handleOk = () => {
  // Validate and save
  if (!formState.recipeNumber || !formState.strengthGrade) {
    message.error('请填写必填项')
    return
  }
  message.success('保存成功')
  modalVisible.value = false
  fetchRecipes()
}

const handleApprove = (record: MixRecipe) => {
  Modal.confirm({
    title: '确认审核通过?',
    onOk() {
      record.status = 'APPROVED'
      message.success('已审核')
    }
  })
}

const handleDisable = (record: MixRecipe) => {
  Modal.confirm({
    title: '确认停用?',
    onOk() {
      record.status = 'DISABLED'
      message.success('已停用')
    }
  })
}

onMounted(() => {
  fetchRecipes()
})
</script>

<template>
  <MainLayout>
    <div class="mix-page">
      <div class="header">
        <div class="filters">
          <a-select v-model:value="filters.strengthGrade" placeholder="强度等级" style="width: 120px" allowClear>
            <a-select-option value="C30">C30</a-select-option>
            <a-select-option value="C40">C40</a-select-option>
          </a-select>
          <a-select v-model:value="filters.status" placeholder="状态" style="width: 120px; margin-left: 8px" allowClear>
            <a-select-option value="PENDING_APPROVAL">待审核</a-select-option>
            <a-select-option value="APPROVED">已审核</a-select-option>
            <a-select-option value="DISABLED">已停用</a-select-option>
          </a-select>
          <a-button type="primary" @click="fetchRecipes" style="margin-left: 8px">查询</a-button>
        </div>
        <a-button v-if="isLabOrAdmin" type="primary" @click="openModal('create')"><PlusOutlined /> 新建配比</a-button>
      </div>

      <a-table :columns="columns" :data-source="data" :loading="loading" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'PENDING_APPROVAL' ? 'orange' : 'red'">
              {{ record.status === 'APPROVED' ? '已审核' : record.status === 'PENDING_APPROVAL' ? '待审核' : '已停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a v-if="isLabOrAdmin && record.status === 'PENDING_APPROVAL'" @click="openModal('edit', record)"><EditOutlined /> 编辑</a>
              <a v-if="isLabOrAdmin && record.status === 'PENDING_APPROVAL'" @click="handleApprove(record)"><CheckOutlined /> 审核</a>
              <a v-if="isLabOrAdmin && record.status === 'APPROVED'" @click="handleDisable(record)" style="color: red"><StopOutlined /> 停用</a>
              <a v-if="isLabOrAdmin" @click="openModal('copy', record)"><CopyOutlined /> 复制</a>
            </a-space>
          </template>
        </template>
      </a-table>

      <a-modal v-model:open="modalVisible" :title="modalTitle" width="800px" @ok="handleOk">
        <a-form :model="formState" layout="vertical">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="配比编号" required>
                <a-input v-model:value="formState.recipeNumber" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="强度等级" required>
                <a-select v-model:value="formState.strengthGrade">
                  <a-select-option value="C30">C30</a-select-option>
                  <a-select-option value="C40">C40</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item label="坍落度">
                <a-input v-model:value="formState.slump" />
              </a-form-item>
            </a-col>
          </a-row>
          
          <div class="materials-section">
            <h4>材料清单</h4>
            <a-button type="dashed" block @click="handleAddMaterial" style="margin-bottom: 16px">
              <PlusOutlined /> 添加材料
            </a-button>
            <div v-for="(item, index) in formState.materials" :key="index" class="material-row">
               <a-space>
                 <a-select 
                   v-model:value="item.materialCode" 
                   style="width: 200px" 
                   placeholder="选择材料"
                   @change="(val: string) => handleMaterialChange(val, index)"
                 >
                   <a-select-option v-for="opt in materialOptions" :key="opt.value" :value="opt.value">
                     {{ opt.label }}
                   </a-select-option>
                 </a-select>
                 <a-input-number v-model:value="item.usagePerCubic" placeholder="单方用量" style="width: 150px" addon-after="kg/m³" />
                 <a-button type="text" danger @click="handleRemoveMaterial(index)">删除</a-button>
               </a-space>
            </div>
          </div>
        </a-form>
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
.material-row {
  margin-bottom: 8px;
}
</style>
