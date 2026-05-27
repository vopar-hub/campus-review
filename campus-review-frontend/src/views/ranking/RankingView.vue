<template>
  <AppLayout>
    <div class="ranking-page">
      <!-- 页面头部 -->
      <div class="page-header">
        <div class="header-decoration">
          <div class="deco deco-1"></div>
          <div class="deco deco-2"></div>
          <div class="deco deco-3"></div>
        </div>
        <div class="header-content">
          <div class="header-icon">
            <el-icon :size="40"><Trophy /></el-icon>
          </div>
          <div class="header-text">
            <h1>热门餐馆排行榜</h1>
            <p>发现校内最受欢迎的美食地标</p>
          </div>
        </div>
      </div>

      <!-- 榜单选择 -->
      <el-card class="filter-card">
        <div class="filter-content">
          <div class="filter-left">
            <el-form :inline="true">
              <el-form-item label="显示榜单">
                <el-select v-model="topN" @change="loadRankings" size="large" class="top-select">
                  <el-option label="Top 10" :value="10" />
                  <el-option label="Top 20" :value="20" />
                  <el-option label="Top 50" :value="50" />
                  <el-option label="Top 100" :value="100" />
                </el-select>
              </el-form-item>
            </el-form>
          </div>
          <div class="filter-decoration">
            <div class="filter-dot dot-1"></div>
            <div class="filter-dot dot-2"></div>
            <div class="filter-dot dot-3"></div>
          </div>
        </div>
      </el-card>

      <!-- 排行榜列表 -->
      <div v-loading="loading" class="ranking-list">
        <template v-if="rankings.length > 0">
          <div
            v-for="(item, index) in rankings"
            :key="item.restaurantId"
            class="ranking-item"
            :class="'rank-' + (index + 1)"
            @click="goToRestaurant(item.restaurantId)"
            :style="{ 'animation-delay': `${index * 50}ms` }"
          >
            <div class="rank-badge" :class="getRankClass(item.rank)">
              <span class="rank-number">{{ item.rank }}</span>
            </div>
            <div class="restaurant-info">
              <h3>{{ item.restaurantName }}</h3>
              <p class="restaurant-desc">人气餐馆</p>
            </div>
            <div class="rating-section">
              <el-rate
                :model-value="item.avgRating"
                :max="5"
                disabled
                show-score
                text-color="#ff9900"
                :score-template="item.avgRating.toFixed(1)"
              />
              <span class="rating-value">{{ item.avgRating.toFixed(1) }}</span>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无排行数据">
          <template #image>
            <div class="empty-icon">
              <el-icon :size="80"><Trophy /></el-icon>
            </div>
          </template>
        </el-empty>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Trophy } from '@element-plus/icons-vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import { getHotRestaurants } from '@/api/ranking'
import type { HotRestaurantRankItemDTO } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-ranking')

const router = useRouter()

const loading = ref(false)
const topN = ref(10)
const rankings = ref<HotRestaurantRankItemDTO[]>([])

const loadRankings = async () => {
  loading.value = true
  try {
    const res = await getHotRestaurants(topN.value)
    rankings.value = res.data
  } catch (error) {
    logger.error('加载排行榜失败:', error)
    // 错误信息由 axios 拦截器通过 ElMessage 显示
  } finally {
    loading.value = false
  }
}

const getRankClass = (rank: number) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return ''
}

const goToRestaurant = (restaurantId: number) => {
  router.push(`/restaurants/${restaurantId}`)
}

onMounted(() => {
  loadRankings()
})
</script>

<style scoped>
.ranking-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
  padding-bottom: 40px;
}

/* ========== 页面头部 ========== */
.page-header {
  position: relative;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.08) 0%, rgba(78, 205, 196, 0.08) 50%, rgba(167, 139, 250, 0.08) 100%);
  border-radius: var(--radius-2xl);
  padding: 48px;
  overflow: hidden;
  border: 1px solid var(--neutral-200);
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.deco {
  position: absolute;
  border-radius: 50%;
}

.deco-1 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.1) 0%, transparent 70%);
  top: -150px;
  right: -50px;
}

.deco-2 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(78, 205, 196, 0.1) 0%, transparent 70%);
  bottom: -100px;
  left: -50px;
}

.deco-3 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.1) 0%, transparent 70%);
  top: 50%;
  right: 20%;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
  z-index: 1;
}

.header-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-warm);
  border-radius: var(--radius-xl);
  color: white;
  box-shadow: 0 8px 30px rgba(255, 230, 109, 0.4);
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

/* ========== 筛选卡片 ========== */
.filter-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--neutral-200);
  box-shadow: var(--shadow-md);
}

.filter-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.filter-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-content :deep(.el-form-item) {
  margin-bottom: 0;
}

.filter-content :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--neutral-700);
  font-size: 15px;
}

.top-select {
  width: 140px;
}

:deep(.el-select .el-input__inner) {
  border-radius: var(--radius-lg);
  border: 2px solid var(--neutral-200);
  font-weight: 500;
}

:deep(.el-select .el-input__inner:focus) {
  border-color: var(--color-coral);
  box-shadow: 0 0 0 4px rgba(255, 107, 107, 0.1);
}

.filter-decoration {
  display: flex;
  gap: 8px;
  align-items: center;
}

.filter-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot-1 {
  background: var(--gradient-primary);
  animation: pulse 2s ease-in-out infinite;
}

.dot-2 {
  background: var(--gradient-fresh);
  animation: pulse 2s ease-in-out infinite 0.3s;
}

.dot-3 {
  background: var(--gradient-dreamy);
  animation: pulse 2s ease-in-out infinite 0.6s;
}

/* ========== 排行榜列表 ========== */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-bounce);
  border: 2px solid transparent;
  animation: slide-up 0.5s var(--ease-smooth) both;
}

.ranking-item:hover {
  transform: translateX(8px);
  border-color: var(--color-coral);
  box-shadow: 0 12px 30px rgba(255, 107, 107, 0.15);
}

.ranking-item.rank-1 {
  background: linear-gradient(135deg, rgba(255, 230, 109, 0.08) 0%, rgba(255, 107, 107, 0.05) 100%);
  border-color: rgba(255, 230, 109, 0.3);
}

.ranking-item.rank-2 {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.08) 0%, rgba(69, 183, 209, 0.05) 100%);
  border-color: rgba(78, 205, 196, 0.3);
}

.ranking-item.rank-3 {
  background: linear-gradient(135deg, rgba(167, 139, 250, 0.08) 0%, rgba(139, 92, 246, 0.05) 100%);
  border-color: rgba(167, 139, 250, 0.3);
}

.rank-badge {
  flex-shrink: 0;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 24px;
  font-weight: 800;
  background: var(--neutral-100);
  color: var(--neutral-500);
  position: relative;
  overflow: hidden;
}

.rank-badge::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.4) 0%, transparent 70%);
}

.rank-gold {
  background: var(--gradient-warm);
  color: white;
  box-shadow: 0 8px 24px rgba(255, 230, 109, 0.5);
}

.rank-silver {
  background: var(--gradient-fresh);
  color: white;
  box-shadow: 0 8px 24px rgba(78, 205, 196, 0.5);
}

.rank-bronze {
  background: var(--gradient-dreamy);
  color: white;
  box-shadow: 0 8px 24px rgba(167, 139, 250, 0.5);
}

.rank-number {
  position: relative;
  z-index: 1;
}

.restaurant-info {
  flex: 1;
  min-width: 0;
}

.restaurant-info h3 {
  margin: 0 0 6px 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--neutral-800);
  transition: color var(--duration-fast) var(--ease-smooth);
}

.ranking-item:hover .restaurant-info h3 {
  color: var(--color-coral);
}

.restaurant-desc {
  margin: 0;
  font-size: 13px;
  color: var(--neutral-400);
  font-weight: 500;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rating-value {
  font-size: 22px;
  font-weight: 800;
  background: var(--gradient-warm);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  min-width: 50px;
  text-align: right;
}

.empty-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-warm);
  border-radius: 50%;
  color: white;
  margin: 0 auto;
  box-shadow: 0 8px 30px rgba(255, 230, 109, 0.4);
}

/* ========== 暗夜模式 ========== */
html.dark .page-header {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(78, 205, 196, 0.1) 50%, rgba(167, 139, 250, 0.1) 100%);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .header-text h1 {
  color: var(--neutral-800);
}

html.dark .header-text p {
  color: var(--neutral-500);
}

html.dark .filter-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .ranking-item {
  background: var(--neutral-100);
}

html.dark .ranking-item:hover {
  border-color: var(--color-coral);
}

html.dark .ranking-item.rank-1 {
  background: linear-gradient(135deg, rgba(255, 230, 109, 0.12) 0%, rgba(255, 107, 107, 0.08) 100%);
}

html.dark .ranking-item.rank-2 {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.12) 0%, rgba(69, 183, 209, 0.08) 100%);
}

html.dark .ranking-item.rank-3 {
  background: linear-gradient(135deg, rgba(167, 139, 250, 0.12) 0%, rgba(139, 92, 246, 0.08) 100%);
}

html.dark .rank-badge {
  background: var(--neutral-200);
  color: var(--neutral-500);
}

html.dark .restaurant-info h3 {
  color: var(--neutral-800);
}

html.dark .ranking-item:hover .restaurant-info h3 {
  color: var(--color-coral);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .ranking-page {
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
    width: 60px;
    height: 60px;
  }

  .header-icon .el-icon {
    font-size: 28px;
  }

  .header-text h1 {
    font-size: 24px;
  }

  .header-text p {
    font-size: 13px;
  }

  .filter-card {
    border-radius: var(--radius-xl);
  }

  .filter-content {
    flex-direction: column;
    gap: 12px;
  }

  .filter-left {
    width: 100%;
  }

  .filter-content :deep(.el-form-item) {
    width: 100%;
  }

  .filter-content :deep(.el-form-item__label) {
    font-size: 14px;
  }

  .top-select {
    width: 100%;
  }

  .filter-decoration {
    display: none;
  }

  .ranking-item {
    padding: 16px;
    gap: 12px;
    border-radius: var(--radius-lg);
  }

  .rank-badge {
    width: 44px;
    height: 44px;
    font-size: 18px;
  }

  .restaurant-info h3 {
    font-size: 15px;
  }

  .restaurant-desc {
    font-size: 12px;
  }

  .rating-section {
    flex-direction: column;
    align-items: flex-end;
    gap: 4px;
  }

  .rating-value {
    font-size: 18px;
    min-width: auto;
  }

  :deep(.el-rate) {
    height: 16px;
  }

  :deep(.el-rate__icon) {
    font-size: 14px;
  }
}

/* ========== 加载动画 ========== */
:deep(.el-loading-mask) {
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.9);
}

html.dark :deep(.el-loading-mask) {
  background: rgba(22, 27, 34, 0.9);
}

:deep(.el-loading-spinner .path) {
  stroke: var(--color-coral);
}

:deep(.el-loading-spinner .el-loading-text) {
  color: var(--color-coral);
  font-weight: 600;
}
</style>
