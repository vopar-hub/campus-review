<template>
  <AppLayout>
    <div class="home-page">
      <!-- Hero 区域 -->
      <section class="hero-section">
        <!-- 动态背景 -->
        <div class="hero-background">
          <div class="bg-shape shape-1"></div>
          <div class="bg-shape shape-2"></div>
          <div class="bg-shape shape-3"></div>
          <div class="bg-shape shape-4"></div>
          <div class="bg-shape shape-5"></div>
        </div>

        <div class="hero-content">
          <!-- 徽章 -->
          <div class="hero-badge animate-bounce-in">
            <div class="badge-icon-wrapper">
              <el-icon class="badge-icon"><Star /></el-icon>
            </div>
            <span>校园美食推荐平台</span>
          </div>

          <!-- 主标题 -->
          <h1 class="hero-title animate-slide-up">
            <span class="title-line">发现</span>
            <span class="title-line title-gradient">校园美食</span>
          </h1>

          <p class="hero-subtitle animate-slide-up delay-100">
            探索校内最受欢迎的美味餐馆，分享属于你的美食体验
          </p>

          <!-- 行动按钮 -->
          <div class="hero-actions animate-slide-up delay-200">
            <button class="btn-primary btn-explore" @click="$router.push('/restaurants')">
              <div class="btn-icon-wrapper">
                <el-icon class="btn-icon"><Food /></el-icon>
              </div>
              <span>浏览餐馆</span>
            </button>
            <button class="btn-secondary btn-ranking" @click="$router.push('/rankings')">
              <div class="btn-icon-wrapper">
                <el-icon class="btn-icon"><Trophy /></el-icon>
              </div>
              <span>查看排行榜</span>
            </button>
          </div>

          <!-- 统计卡片 -->
          <div class="hero-stats animate-slide-up delay-300">
            <div class="stat-item stat-coral">
              <div class="stat-icon">
                <el-icon><Shop /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">50+</div>
                <div class="stat-label">校内餐馆</div>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item stat-mint">
              <div class="stat-icon">
                <el-icon><ChatDotRound /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">1000+</div>
                <div class="stat-label">真实评价</div>
              </div>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item stat-lavender">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">5000+</div>
                <div class="stat-label">在校师生</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 热门餐馆 -->
      <section class="hot-restaurants">
        <div class="section-header">
          <div class="header-left">
            <div class="section-badge">
              <div class="badge-icon-mini">
                <el-icon><Burger /></el-icon>
              </div>
              <span>人气热门</span>
            </div>
            <h2>热门餐馆</h2>
            <p>同学们最喜欢的美食聚集地，每一家都是精挑细选</p>
          </div>
          <router-link to="/rankings" class="more-link">
            <span>查看全部</span>
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>

        <div v-loading="loading" class="restaurant-grid">
          <template v-if="hotRestaurants.length > 0">
            <RestaurantCard
              v-for="(item, index) in hotRestaurants"
              :key="item.restaurantId"
              :restaurant="item.restaurant || {
                id: item.restaurantId,
                name: item.restaurantName,
                campus: '',
                address: '',
                description: '',
                coverImageUrl: '',
                createdAt: ''
              }"
              :style="{ 'animation-delay': `${index * 100}ms` }"
              class="animate-bounce-in"
            />
          </template>
          <el-empty v-else description="暂无数据">
            <template #image>
              <div class="empty-icon">
                <el-icon :size="80"><Food /></el-icon>
              </div>
            </template>
          </el-empty>
        </div>
      </section>

    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ArrowRight, Food, Trophy, Star, Burger, Shop, ChatDotRound, User } from '@element-plus/icons-vue'
import AppLayout from '@/components/layout/AppLayout.vue'
import RestaurantCard from '@/components/restaurant/RestaurantCard.vue'
import { getHotRestaurants } from '@/api/ranking'
import { getRestaurant } from '@/api/restaurant'
import type { HotRestaurantRankItemDTO, RestaurantDTO } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-home')

interface HotRestaurantItem extends HotRestaurantRankItemDTO {
  restaurant?: RestaurantDTO
}

const loading = ref(false)
const hotRestaurants = ref<HotRestaurantItem[]>([])

// 加载热门餐馆
const loadHotRestaurants = async () => {
  loading.value = true
  try {
    const res = await getHotRestaurants(6)
    hotRestaurants.value = res.data

    // 获取每个餐馆的详细信息
    for (const item of hotRestaurants.value) {
      try {
        const restaurantRes = await getRestaurant(item.restaurantId)
        item.restaurant = restaurantRes.data
      } catch (error) {
        logger.error(`获取餐馆 ${item.restaurantId} 详情失败:`, error)
      }
    }
  } catch (error) {
    logger.error('加载热门餐馆失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHotRestaurants()
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 64px;
  padding-bottom: 40px;
}

/* ========== Hero 区域 ========== */
.hero-section {
  position: relative;
  background: linear-gradient(180deg, var(--neutral-50) 0%, rgba(255, 107, 107, 0.03) 50%, var(--neutral-50) 100%);
  border-radius: var(--radius-2xl);
  padding: 80px 60px;
  text-align: center;
  overflow: hidden;
  border: 1px solid var(--neutral-200);
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.bg-shape {
  position: absolute;
  border-radius: 50%;
  animation: float 10s ease-in-out infinite;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.12) 0%, transparent 70%);
  top: -250px;
  right: -150px;
  animation-delay: 0s;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(78, 205, 196, 0.1) 0%, transparent 70%);
  bottom: -150px;
  left: -100px;
  animation-delay: 2s;
}

.shape-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.1) 0%, transparent 70%);
  top: 30%;
  right: 20%;
  animation-delay: 4s;
}

.shape-4 {
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 230, 109, 0.12) 0%, transparent 70%);
  top: 20%;
  left: 15%;
  animation-delay: 6s;
}

.shape-5 {
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(249, 168, 212, 0.1) 0%, transparent 70%);
  bottom: 30%;
  right: 30%;
  animation-delay: 8s;
}

.hero-content {
  position: relative;
  z-index: 1;
  max-width: 800px;
  margin: 0 auto;
}

/* ========== 徽章 ========== */
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px 8px 8px;
  background: white;
  border-radius: var(--radius-full);
  font-size: 14px;
  font-weight: 600;
  color: var(--color-coral);
  margin-bottom: 32px;
  border: 2px solid rgba(255, 107, 107, 0.15);
  box-shadow: 0 4px 20px rgba(255, 107, 107, 0.1);
  transition: all var(--duration-normal) var(--ease-bounce);
}

.hero-badge:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px rgba(255, 107, 107, 0.2);
}

.badge-icon-wrapper {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 50%;
  color: white;
}

.badge-icon {
  animation: pulse 2s ease-in-out infinite;
}

/* ========== 主标题 ========== */
.hero-title {
  margin: 0 0 20px 0;
  font-size: 72px;
  font-weight: 800;
  letter-spacing: -3px;
  line-height: 1.1;
  color: var(--neutral-800);
}

.title-line {
  display: block;
}

.title-gradient {
  background: var(--gradient-rainbow);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradient-shift 4s ease infinite;
}

.hero-subtitle {
  margin: 0 0 48px 0;
  font-size: 18px;
  color: var(--neutral-500);
  font-weight: 400;
  line-height: 1.7;
}

/* ========== 行动按钮 ========== */
.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 56px;
}

.btn-primary,
.btn-secondary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-bounce);
  border: none;
}

.btn-icon-wrapper {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.2);
}

.btn-explore {
  background: var(--gradient-primary);
  color: white;
  box-shadow: var(--shadow-coral);
}

.btn-explore:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(255, 107, 107, 0.4);
}

.btn-ranking {
  background: white;
  color: var(--color-mint);
  border: 2px solid rgba(78, 205, 196, 0.3);
  box-shadow: 0 4px 20px rgba(78, 205, 196, 0.1);
}

.btn-ranking:hover {
  background: var(--gradient-fresh);
  color: white;
  border-color: transparent;
  transform: translateY(-4px);
  box-shadow: var(--shadow-mint);
}

.btn-ranking:hover .btn-icon-wrapper {
  background: rgba(255, 255, 255, 0.3);
}

/* ========== 统计卡片 ========== */
.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
  padding: 28px 40px;
  background: white;
  border-radius: var(--radius-2xl);
  border: 1px solid var(--neutral-200);
  max-width: 600px;
  margin: 0 auto;
  box-shadow: var(--shadow-lg);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  font-size: 22px;
  color: white;
}

.stat-coral .stat-icon {
  background: var(--gradient-primary);
  box-shadow: var(--shadow-coral);
}

.stat-mint .stat-icon {
  background: var(--gradient-fresh);
  box-shadow: var(--shadow-mint);
}

.stat-lavender .stat-icon {
  background: var(--gradient-dreamy);
  box-shadow: var(--shadow-lavender);
}

.stat-info {
  text-align: left;
}

.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: var(--neutral-800);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--neutral-500);
  font-weight: 500;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: linear-gradient(180deg, transparent 0%, var(--neutral-300) 50%, transparent 100%);
}

/* ========== 热门餐馆区域 ========== */
.hot-restaurants {
  padding: 0 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 36px;
  gap: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px 6px 6px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.08) 0%, rgba(78, 205, 196, 0.08) 100%);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 600;
  color: var(--color-coral);
  width: fit-content;
  border: 1px solid rgba(255, 107, 107, 0.15);
}

.badge-icon-mini {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 50%;
  color: white;
  font-size: 14px;
}

.header-left h2 {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  color: var(--neutral-800);
  letter-spacing: -1px;
}

.header-left p {
  margin: 0;
  font-size: 15px;
  color: var(--neutral-500);
  line-height: 1.6;
}

.more-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: white;
  text-decoration: none;
  font-weight: 600;
  font-size: 14px;
  padding: 12px 24px;
  border-radius: var(--radius-lg);
  background: var(--gradient-fresh);
  border: none;
  transition: all var(--duration-normal) var(--ease-bounce);
  white-space: nowrap;
  box-shadow: var(--shadow-mint);
}

.more-link:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(78, 205, 196, 0.4);
}

.more-link .el-icon {
  transition: transform var(--duration-fast) var(--ease-smooth);
}

.more-link:hover .el-icon {
  transform: translateX(4px);
}

.restaurant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 28px;
}

.empty-icon {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 50%;
  color: white;
  margin: 0 auto;
  box-shadow: var(--shadow-coral);
}

/* ========== 动画类 ========== */
.animate-bounce-in {
  animation: bounce-in 0.6s var(--ease-bounce) both;
}

.animate-slide-up {
  animation: slide-up 0.6s var(--ease-smooth) both;
}

.delay-100 { animation-delay: 100ms; }
.delay-200 { animation-delay: 200ms; }
.delay-300 { animation-delay: 300ms; }

/* ========== 暗夜模式 ========== */
html.dark .hero-section {
  background: linear-gradient(180deg, var(--neutral-50) 0%, rgba(255, 107, 107, 0.05) 50%, var(--neutral-50) 100%);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .hero-title {
  color: var(--neutral-800);
}

html.dark .hero-badge {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.2);
}

html.dark .btn-ranking {
  background: var(--neutral-100);
  border-color: rgba(78, 205, 196, 0.3);
}

html.dark .btn-ranking:hover {
  background: var(--gradient-fresh);
}

html.dark .hero-stats {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .stat-value {
  color: var(--neutral-800);
}

html.dark .header-left h2 {
  color: var(--neutral-800);
}

html.dark .section-badge {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.12) 0%, rgba(78, 205, 196, 0.12) 100%);
  border-color: rgba(255, 107, 107, 0.2);
}

/* ========== 响应式设计 ========== */
@media (max-width: 768px) {
  .home-page {
    gap: 40px;
  }

  .hero-section {
    padding: 32px 16px;
    border-radius: var(--radius-xl);
  }

  .hero-title {
    font-size: 32px;
    letter-spacing: -1px;
  }

  .hero-subtitle {
    font-size: 14px;
    margin-bottom: 32px;
  }

  /* 移动端按钮优化 */
  .hero-actions {
    flex-direction: column;
    gap: 12px;
    margin-bottom: 40px;
  }

  .btn-primary,
  .btn-secondary {
    padding: 12px 24px;
    font-size: 14px;
    width: 100%;
    justify-content: center;
  }

  .btn-icon-wrapper {
    width: 32px;
    height: 32px;
  }

  /* 移动端统计卡片优化 */
  .hero-stats {
    flex-direction: column;
    gap: 16px;
    padding: 20px 24px;
  }

  .stat-divider {
    width: 60px;
    height: 1px;
    background: linear-gradient(90deg, transparent 0%, var(--neutral-300) 50%, transparent 100%);
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 18px;
  }

  .stat-value {
    font-size: 22px;
  }

  .stat-label {
    font-size: 12px;
  }

  /* 移动端热门餐馆区域 */
  .section-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-left h2 {
    font-size: 26px;
  }

  .header-left p {
    font-size: 14px;
  }

  .more-link {
    width: 100%;
    justify-content: center;
  }

  .restaurant-grid {
    grid-template-columns: 1fr;
    gap: 16px;
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
