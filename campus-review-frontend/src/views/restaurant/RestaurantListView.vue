<template>
  <AppLayout>
    <div class="restaurant-list-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-content">
          <div class="header-icon">
            <el-icon :size="32"><Food /></el-icon>
          </div>
          <div class="header-text">
            <h1>探索美食</h1>
            <p>发现校园里的每一味美好</p>
          </div>
        </div>
      </div>

      <!-- 搜索栏 -->
      <el-card class="search-card">
        <div class="search-header">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item>
              <div class="search-input-wrapper">
                <el-icon class="search-icon"><Search /></el-icon>
                <el-input
                  v-model="searchForm.name"
                  placeholder="输入餐馆名称"
                  clearable
                  @clear="handleSearch"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <div class="search-input-wrapper">
                <el-icon class="search-icon"><Location /></el-icon>
                <el-input
                  v-model="searchForm.campus"
                  placeholder="输入校区"
                  clearable
                  @clear="handleSearch"
                />
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="btn-search" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
            </el-form-item>
          </el-form>
          <el-button v-if="isAdmin" type="success" size="large" class="btn-add" @click="goToCreate">
            <el-icon><Plus /></el-icon>
            添加餐厅
          </el-button>
        </div>
      </el-card>

      <!-- 餐馆列表 -->
      <div v-loading="loading" class="restaurant-list">
        <template v-if="restaurants.length > 0">
          <RestaurantCard
            v-for="restaurant in restaurants"
            :key="restaurant.id"
            :restaurant="restaurant"
          />
        </template>
        <el-empty v-else description="暂无餐馆数据">
          <template #image>
            <el-icon :size="80" color="#fdba74"><Food /></el-icon>
          </template>
        </el-empty>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Food, Location, Plus } from '@element-plus/icons-vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import RestaurantCard from '@/components/restaurant/RestaurantCard.vue'
import { searchRestaurants } from '@/api/restaurant'
import { useUserStore } from '@/stores/user'
import type { RestaurantDTO, RestaurantSearchParams } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-restaurant-list')
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const restaurants = ref<RestaurantDTO[]>([])

const isAdmin = computed(() => userStore.isAdmin)

const searchForm = reactive<RestaurantSearchParams>({
  name: '',
  campus: '',
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await searchRestaurants(searchForm)
    restaurants.value = res.data
  } catch (error) {
    logger.error('搜索餐馆失败:', error)
  } finally {
    loading.value = false
  }
}

const goToCreate = () => {
  router.push('/admin/restaurants/new')
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
/* ========== 暗夜模式样式 ========== */
html.dark .page-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 50%, #1a1a2e 100%);
  border-color: rgba(249, 115, 22, 0.15);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.4);
}

html.dark .header-text h1 {
  color: #fafafa;
}

html.dark .header-text p {
  color: #a3a3a3;
}

html.dark .search-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 100%);
  border-color: rgba(249, 115, 22, 0.15);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

html.dark .search-input-wrapper {
  background: #2d2d44;
}

html.dark .search-input-wrapper:focus-within {
  background: #1a1a2e;
}

html.dark .search-icon {
  color: #737373;
}

html.dark .search-input-wrapper:focus-within .search-icon {
  color: #fb923c;
}

.restaurant-list-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding-bottom: 40px;
}

/* ========== 页面头部 ========== */
.page-header {
  background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 50%, #fef2f2 100%);
  border-radius: 24px;
  padding: 48px;
  box-shadow:
    0 4px 24px rgba(249, 115, 22, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(249, 115, 22, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-icon {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-gradient);
  border-radius: 20px;
  color: white;
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
  flex-shrink: 0;
}

.header-text h1 {
  margin: 0 0 8px 0;
  font-size: 36px;
  font-weight: 800;
  color: var(--neutral-800);
  letter-spacing: -1px;
}

.header-text p {
  margin: 0;
  font-size: 15px;
  color: var(--neutral-500);
}

/* ========== 搜索卡片 ========== */
.search-card {
  border-radius: 20px;
  border: 1px solid rgba(249, 115, 22, 0.08);
  box-shadow: 0 4px 20px rgba(249, 115, 22, 0.06);
}

.search-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 8px 0;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  flex: 1;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 搜索输入框包裹 */
.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: var(--neutral-50);
  border-radius: 14px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  min-width: 200px;
}

.search-input-wrapper:focus-within {
  background: white;
  border-color: #f97316;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

.search-icon {
  font-size: 18px;
  color: var(--neutral-400);
  transition: color 0.3s ease;
}

.search-input-wrapper:focus-within .search-icon {
  color: #f97316;
}

:deep(.el-input__inner) {
  border: none !important;
  background: transparent !important;
  padding: 6px 0 !important;
  font-size: 15px !important;
  color: var(--neutral-700);
  width: 160px !important;
}

:deep(.el-input__inner::placeholder) {
  color: var(--neutral-400);
}

/* 搜索按钮 */
.btn-search {
  height: 46px;
  padding: 0 24px;
  border-radius: 14px;
  font-weight: 600;
  font-size: 15px;
  background: var(--primary-gradient);
  border: none;
  box-shadow: 0 4px 16px rgba(249, 115, 22, 0.25);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-search:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.35);
}

/* 添加按钮 */
.btn-add {
  height: 46px;
  padding: 0 24px;
  border-radius: 14px;
  font-weight: 600;
  font-size: 15px;
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(34, 197, 94, 0.25);
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-add:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(34, 197, 94, 0.35);
}

/* ========== 餐馆列表 ========== */
.restaurant-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 24px;
  padding: 8px;
}

/* 空状态 */
:deep(.el-empty) {
  padding: 60px 20px;
}

:deep(.el-empty__description) {
  color: var(--neutral-500);
  font-size: 15px;
}

/* 加载动画 */
:deep(.el-loading-mask) {
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
}

:deep(.el-loading-spinner .path) {
  stroke: #f97316;
}

:deep(.el-loading-spinner .el-loading-text) {
  color: #f97316;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .restaurant-list-page {
    gap: 20px;
    padding-bottom: calc(80px + env(safe-area-inset-bottom));
  }

  .page-header {
    padding: 24px 20px;
    border-radius: var(--radius-xl);
  }

  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .header-icon {
    width: 56px;
    height: 56px;
  }

  .header-text h1 {
    font-size: 24px;
  }

  .header-text p {
    font-size: 13px;
  }

  /* 搜索栏优化 */
  .search-card {
    border-radius: var(--radius-xl);
  }

  .search-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }

  .search-form {
    flex-direction: column;
    width: 100%;
    gap: 10px;
  }

  .search-form :deep(.el-form-item) {
    width: 100%;
  }

  .search-input-wrapper {
    width: 100%;
    min-width: auto;
  }

  :deep(.el-input__inner) {
    width: 100% !important;
  }

  /* 按钮优化 */
  .btn-search,
  .btn-add {
    height: 42px;
    font-size: 14px;
    width: 100%;
    justify-content: center;
  }

  /* 列表优化 */
  .restaurant-list {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 0;
  }
}
</style>
