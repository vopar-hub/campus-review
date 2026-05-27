<template>
  <el-card class="restaurant-card" shadow="hover" @click="handleClick">
    <div class="restaurant-content">
      <!-- 封面图片 -->
      <div class="restaurant-cover">
        <div class="cover-gradient"></div>
        <img
          v-if="restaurant.coverImageUrl"
          :src="restaurant.coverImageUrl"
          :alt="restaurant.name"
          class="cover-image"
          loading="lazy"
        />
        <div v-else class="cover-placeholder">
          <div class="placeholder-icon">
            <el-icon :size="48"><Food /></el-icon>
          </div>
          <span class="placeholder-text">暂无图片</span>
        </div>

        <!-- 角落装饰 -->
        <div class="cover-deco">
          <div class="deco-circle deco-1"></div>
          <div class="deco-circle deco-2"></div>
        </div>
      </div>

      <!-- 餐馆信息 -->
      <div class="restaurant-info">
        <div class="info-header">
          <h3 class="restaurant-name">{{ restaurant.name }}</h3>
          <div class="campus-tag">
            <el-icon><Location /></el-icon>
            <span>{{ restaurant.campus || '主校区' }}</span>
          </div>
        </div>

        <p class="restaurant-description">
          {{ restaurant.description || '暂无描述' }}
        </p>

        <!-- 更新时间 -->
        <div class="info-footer">
          <div class="update-info">
            <el-icon class="update-icon"><Clock /></el-icon>
            <span class="update-label">收录时间</span>
          </div>
          <span class="update-time">{{ formattedTime }}</span>
        </div>
      </div>

      <!-- 悬停效果层 -->
      <div class="hover-overlay">
        <div class="overlay-content">
          <div class="overlay-icon-wrapper">
            <el-icon class="overlay-icon"><ArrowRight /></el-icon>
          </div>
          <span class="overlay-text">查看详情</span>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Food, Location, ArrowRight, Clock } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import type { RestaurantDTO } from '@/types'

dayjs.locale('zh-cn')

const props = defineProps<{
  restaurant: RestaurantDTO
}>()

const router = useRouter()

const formattedTime = computed(() => {
  return dayjs(props.restaurant.createdAt).format('YYYY.MM.DD')
})

const handleClick = () => {
  router.push(`/restaurants/${props.restaurant.id}`)
}
</script>

<style scoped>
.restaurant-card {
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-bounce);
  border-radius: var(--radius-xl);
  overflow: visible;
  border: 1px solid var(--neutral-200);
  background: white;
  position: relative;
}

.restaurant-card:hover {
  transform: translateY(-8px);
  border-color: var(--color-coral);
  box-shadow: 0 20px 50px -15px rgba(255, 107, 107, 0.25);
}

.restaurant-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  position: relative;
}

/* ========== 封面图片区域 ========== */
.restaurant-cover {
  position: relative;
  width: 100%;
  height: 200px;
  margin: -1px -1px 0 -1px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--neutral-100) 0%, var(--neutral-200) 100%);
}

.cover-gradient {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent 50%, rgba(0, 0, 0, 0.1) 100%);
  z-index: 1;
  pointer-events: none;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s var(--ease-bounce);
}

.restaurant-card:hover .cover-image {
  transform: scale(1.1);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  color: white;
}

.placeholder-icon {
  opacity: 0.9;
  margin-bottom: 8px;
}

.placeholder-text {
  font-size: 14px;
  opacity: 0.9;
  font-weight: 500;
}

.cover-deco {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 6px;
  z-index: 2;
}

.deco-circle {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  opacity: 0.6;
}

.deco-1 {
  background: var(--color-sunshine);
}

.deco-2 {
  background: var(--color-mint);
}

/* ========== 餐馆信息区域 ========== */
.restaurant-info {
  flex: 1;
  padding: 20px;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.restaurant-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--neutral-800);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color var(--duration-fast) var(--ease-smooth);
}

.restaurant-card:hover .restaurant-name {
  color: var(--color-coral);
}

.campus-tag {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--color-mint);
  background: rgba(78, 205, 196, 0.1);
  padding: 5px 12px;
  border-radius: var(--radius-full);
  width: fit-content;
  border: 1px solid rgba(78, 205, 196, 0.2);
  font-weight: 500;
  transition: all var(--duration-fast) var(--ease-smooth);
}

.campus-tag .el-icon {
  font-size: 12px;
}

.restaurant-card:hover .campus-tag {
  background: rgba(78, 205, 196, 0.15);
  border-color: rgba(78, 205, 196, 0.3);
}

.restaurant-description {
  margin: 0;
  font-size: 14px;
  color: var(--neutral-500);
  line-height: 1.7;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  flex: 1;
}

.info-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14px;
  border-top: 1px solid var(--neutral-200);
  gap: 12px;
}

.update-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.update-icon {
  font-size: 14px;
  color: var(--neutral-400);
}

.update-label {
  font-size: 12px;
  color: var(--neutral-400);
  font-weight: 500;
}

.update-time {
  font-size: 12px;
  color: var(--color-coral);
  font-weight: 600;
  background: rgba(255, 107, 107, 0.08);
  padding: 4px 12px;
  border-radius: var(--radius-full);
  border: 1px solid rgba(255, 107, 107, 0.15);
}

/* ========== 悬停效果层 ========== */
.hover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 70px;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 107, 107, 0.05) 100%);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding-bottom: 16px;
  opacity: 0;
  transform: translateY(10px);
  transition: all var(--duration-normal) var(--ease-bounce);
  pointer-events: none;
}

.restaurant-card:hover .hover-overlay {
  opacity: 1;
  transform: translateY(0);
}

.overlay-content {
  display: flex;
  align-items: center;
  gap: 8px;
  background: white;
  padding: 10px 20px;
  border-radius: var(--radius-full);
  box-shadow: 0 8px 24px rgba(255, 107, 107, 0.2);
  border: 1px solid rgba(255, 107, 107, 0.2);
}

.overlay-icon-wrapper {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 50%;
  color: white;
}

.overlay-icon {
  font-size: 14px;
  animation: slideRight 0.6s ease infinite;
}

.overlay-text {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-coral);
}

@keyframes slideRight {
  0%, 100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(4px);
  }
}

/* ========== 暗夜模式样式 ========== */
html.dark .restaurant-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .restaurant-card:hover {
  border-color: var(--color-coral);
  box-shadow: 0 20px 50px -15px rgba(255, 107, 107, 0.3);
}

html.dark .restaurant-cover {
  background: linear-gradient(135deg, var(--neutral-200) 0%, var(--neutral-300) 100%);
}

html.dark .restaurant-name {
  color: var(--neutral-800);
}

html.dark .restaurant-card:hover .restaurant-name {
  color: var(--color-coral);
}

html.dark .restaurant-description {
  color: var(--neutral-500);
}

html.dark .info-footer {
  border-top-color: rgba(255, 107, 107, 0.15);
}

html.dark .overlay-content {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.3);
}

/* ========== 移动端响应式 ========== */
@media (max-width: 768px) {
  .restaurant-card {
    border-radius: var(--radius-lg);
  }

  .restaurant-cover {
    height: 160px;
  }

  .restaurant-info {
    padding: 16px;
    gap: 10px;
  }

  .restaurant-name {
    font-size: 16px;
  }

  .restaurant-description {
    font-size: 13px;
    line-height: 1.6;
  }

  .campus-tag {
    font-size: 11px;
    padding: 4px 10px;
  }

  .info-footer {
    padding-top: 10px;
  }

  .update-label {
    font-size: 11px;
  }

  .update-time {
    font-size: 11px;
    padding: 3px 10px;
  }

  /* 移动端隐藏悬停效果 */
  .hover-overlay {
    display: none;
  }
}
</style>
