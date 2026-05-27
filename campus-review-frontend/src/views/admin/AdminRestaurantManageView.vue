<template>
  <AppLayout>
    <div class="admin-restaurant-page" v-loading="loading">
      <div class="page-header">
        <div class="header-left">
          <el-icon :size="28" class="header-icon"><Shop /></el-icon>
          <h1>餐厅管理</h1>
        </div>
        <router-link to="/admin/restaurants/new">
          <el-button type="primary">
            <el-icon><Plus /></el-icon>
            添加餐厅
          </el-button>
        </router-link>
      </div>

      <!-- 搜索栏 -->
      <el-card class="search-card">
        <div class="search-form">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索餐厅名称或地址"
            clearable
            style="width: 320px"
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </el-card>

      <!-- 餐厅列表 -->
      <el-card class="restaurants-card">
        <el-table :data="paginatedRestaurants" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="餐厅名称" min-width="180" />
          <el-table-column prop="campus" label="校区" min-width="120" />
          <el-table-column prop="address" label="地址" min-width="200" />
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formattedTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(row)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="restaurants.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无餐厅数据" />
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredRestaurants.length"
            @size-change="handlePageChange"
            @current-change="handlePageChange"
          />
        </div>
      </el-card>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Shop, Plus, Search, Refresh, Delete } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import AppLayout from '@/components/layout/AppLayout.vue'
import { getAdminRestaurants, deleteRestaurant } from '@/api/admin'
import { getLogger } from '@/utils/logger'
import type { RestaurantDTO } from '@/types'

dayjs.locale('zh-cn')

const logger = getLogger('views-admin-restaurant-manage')

const loading = ref(false)
const restaurants = ref<RestaurantDTO[]>([])
const searchKeyword = ref('')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)

const formattedTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadRestaurants = async () => {
  loading.value = true
  try {
    const res = await getAdminRestaurants()
    restaurants.value = res.data || []
  } catch (error) {
    logger.error('加载餐厅列表失败:', error)
    ElMessage.error('加载餐厅列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤餐厅列表
const filteredRestaurants = computed(() => {
  if (!searchKeyword.value) {
    return restaurants.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return restaurants.value.filter(restaurant =>
    restaurant.name?.toLowerCase().includes(keyword) ||
    restaurant.campus?.toLowerCase().includes(keyword) ||
    restaurant.address?.toLowerCase().includes(keyword)
  )
})

// 分页后的餐厅列表
const paginatedRestaurants = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredRestaurants.value.slice(start, end)
})

const handleSearch = () => {
  currentPage.value = 1 // 搜索时重置到第一页
}

const resetSearch = () => {
  searchKeyword.value = ''
  currentPage.value = 1
}

const handlePageChange = () => {
  // 页码或每页数量变化时，滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleDelete = async (restaurant: RestaurantDTO) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除餐厅 "${restaurant.name}" 吗？此操作不可恢复！`,
      '删除餐厅',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    await deleteRestaurant(restaurant.id!)
    ElMessage.success('餐厅已删除')
    loadRestaurants()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('删除餐厅失败:', error)
    }
  }
}

onMounted(() => {
  loadRestaurants()
})
</script>

<style scoped>
.admin-restaurant-page {
  padding-bottom: 40px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  color: #f97316;
}

.header-left h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 16px;
}

.search-form {
  display: flex;
  gap: 12px;
  align-items: center;
}

.restaurants-card {
  border-radius: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0;
}

.empty-state {
  padding: 40px 0;
}

:deep(.el-table) {
  --el-table-border-color: var(--neutral-200);
  --el-table-row-hover-bg-color: rgba(249, 115, 22, 0.05);
}

:deep(.el-table th) {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.05) 0%, rgba(251, 146, 60, 0.05) 100%);
  color: #333;
  font-weight: 600;
}

:deep(.el-tag) {
  border-radius: 20px;
  font-weight: 600;
}

/* ========== 暗夜模式样式 ========== */
html.dark .admin-restaurant-page .page-header h1 {
  color: #fafafa;
}

html.dark .search-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 100%);
  border-color: rgba(249, 115, 22, 0.15);
}

html.dark .restaurants-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 100%);
  border-color: rgba(249, 115, 22, 0.15);
}
</style>
