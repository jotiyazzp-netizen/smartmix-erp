<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MainLayout from '@/components/MainLayout.vue'
import { message } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'

interface Material {
  materialCode: string
  description: string
  plantCode: string
  baseUnit: string
  pricePerKg: number
  sourceSystem: string
  currency: string
}

const columns: TableColumnsType = [
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode' },
  { title: '物料描述', dataIndex: 'description', key: 'description' },
  { title: '工厂', dataIndex: 'plantCode', key: 'plantCode' },
  { title: '单位', dataIndex: 'baseUnit', key: 'baseUnit' },
  { title: '当前价 (元/kg)', dataIndex: 'pricePerKg', key: 'pricePerKg' },
  { title: '来源', dataIndex: 'sourceSystem', key: 'sourceSystem' },
  { title: '操作', key: 'action' }
]

const data = ref<Material[]>([])
const loading = ref(false)
const searchText = ref('')

const fetchMaterials = async () => {
  loading.value = true
  try {
    // Mock API call
    // const res = await request.get('/materials', { params: { search: searchText.value } })
    // data.value = res
    
    // Mock data
    data.value = [
      { materialCode: 'M001', description: 'P.O 42.5 水泥', plantCode: '1000', baseUnit: 'TON', pricePerKg: 0.45, sourceSystem: 'SAP-MM', currency: 'CNY' },
      { materialCode: 'M002', description: '中砂', plantCode: '1000', baseUnit: 'TON', pricePerKg: 0.08, sourceSystem: 'SAP-MM', currency: 'CNY' },
      { materialCode: 'M003', description: '碎石 5-25mm', plantCode: '1000', baseUnit: 'TON', pricePerKg: 0.07, sourceSystem: 'SAP-MM', currency: 'CNY' },
      { materialCode: 'M004', description: '粉煤灰 II级', plantCode: '1000', baseUnit: 'TON', pricePerKg: 0.25, sourceSystem: 'SAP-MM', currency: 'CNY' },
      { materialCode: 'M005', description: '外加剂', plantCode: '1000', baseUnit: 'KG', pricePerKg: 2.5, sourceSystem: 'SAP-MM', currency: 'CNY' },
      { materialCode: 'M006', description: '水', plantCode: '1000', baseUnit: 'TON', pricePerKg: 0.004, sourceSystem: 'SAP-MM', currency: 'CNY' }
    ].filter(item => item.description.includes(searchText.value) || item.materialCode.includes(searchText.value))
    
  } catch (e) {
    message.error('获取材料列表失败')
  } finally {
    loading.value = false
  }
}

const historyVisible = ref(false)
const currentMaterial = ref<Material | null>(null)
const historyData = ref<any[]>([])

const showHistory = (record: Material) => {
  currentMaterial.value = record
  historyVisible.value = true
  // Mock history
  historyData.value = [
    { date: '2025-11-01', price: record.pricePerKg * 0.95, currency: 'CNY' },
    { date: '2025-10-01', price: record.pricePerKg * 0.98, currency: 'CNY' },
    { date: '2025-09-01', price: record.pricePerKg * 0.92, currency: 'CNY' }
  ]
}

const onSearch = () => {
  fetchMaterials()
}

onMounted(() => {
  fetchMaterials()
})
</script>

<template>
  <MainLayout>
    <div class="materials-page">
      <div class="header">
        <h2>材料与价格</h2>
        <a-input-search
          v-model:value="searchText"
          placeholder="搜索物料编码或描述"
          style="width: 300px"
          @search="onSearch"
        />
      </div>
      
      <a-table :columns="columns" :data-source="data" :loading="loading" row-key="materialCode">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a @click="showHistory(record)">历史价格</a>
          </template>
        </template>
      </a-table>

      <a-modal v-model:open="historyVisible" :title="`历史价格 - ${currentMaterial?.description}`" :footer="null">
        <a-table :data-source="historyData" :pagination="false" row-key="date">
          <a-table-column title="生效日期" data-index="date" />
          <a-table-column title="价格 (元/kg)" data-index="price" />
          <a-table-column title="币种" data-index="currency" />
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
