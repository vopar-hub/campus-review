<template>
  <el-card class="review-card" shadow="hover">
    <div class="review-header">
      <div class="user-info">
        <div class="user-avatar">
          <el-icon :size="20"><User /></el-icon>
        </div>
        <div class="user-details">
          <span class="user-name">用户 #{{ review.userId }}</span>
          <span class="review-time">{{ formattedTime }}</span>
        </div>
      </div>
      <el-tag :type="statusType" size="small" round class="status-tag">{{ statusText }}</el-tag>
    </div>

    <div class="review-content">
      <div class="rating-section">
        <StarRating :model-value="review.rating" :read-only="true" :show-score="true" />
      </div>
      <p class="review-text">{{ review.content }}</p>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { User } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import type { ReviewDTO } from '@/types'
import StarRating from './StarRating.vue'

dayjs.locale('zh-cn')

const props = defineProps<{
  review: ReviewDTO
}>()

const formattedTime = computed(() => {
  return dayjs(props.review.createdAt).format('YYYY-MM-DD HH:mm')
})

const statusType = computed(() => {
  const statusMap: Record<string, 'success' | 'warning' | 'danger'> = {
    APPROVED: 'success',
    PENDING: 'warning',
    REJECTED: 'danger',
  }
  return statusMap[props.review.status] || 'info'
})

const statusText = computed(() => {
  const statusMap: Record<string, string> = {
    APPROVED: '已通过',
    PENDING: '待审核',
    REJECTED: '已驳回',
  }
  return statusMap[props.review.status] || props.review.status
})
</script>

<style scoped>
.review-card {
  border-radius: var(--radius-xl);
  transition: all var(--duration-normal) var(--ease-bounce);
  border: 1px solid var(--neutral-200);
  background: white;
  overflow: hidden;
}

.review-card:hover {
  transform: translateY(-4px);
  border-color: var(--color-coral);
  box-shadow: 0 16px 40px -10px rgba(255, 107, 107, 0.2);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--neutral-200);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: 50%;
  color: white;
  box-shadow: var(--shadow-coral);
  transition: all var(--duration-normal) var(--ease-bounce);
}

.review-card:hover .user-avatar {
  transform: scale(1.1);
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 600;
  color: var(--neutral-800);
  font-size: 15px;
}

.review-time {
  font-size: 12px;
  color: var(--neutral-400);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-tag {
  border-radius: var(--radius-full);
  font-weight: 600;
  font-size: 12px;
  padding: 6px 14px;
  border: none;
}

:deep(.el-tag--success) {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.15) 0%, rgba(69, 183, 209, 0.1) 100%);
  color: var(--color-mint-dark);
}

:deep(.el-tag--warning) {
  background: linear-gradient(135deg, rgba(255, 230, 109, 0.2) 0%, rgba(255, 107, 107, 0.1) 100%);
  color: #D97706;
}

:deep(.el-tag--danger) {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.15) 0%, rgba(185, 28, 28, 0.1) 100%);
  color: var(--color-error);
}

.review-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.rating-section {
  display: flex;
  align-items: center;
}

.review-text {
  margin: 0;
  font-size: 15px;
  color: var(--neutral-600);
  line-height: 1.8;
  padding: 4px 0;
}

/* ========== 暗夜模式样式 ========== */
html.dark .review-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .review-card:hover {
  border-color: var(--color-coral);
  box-shadow: 0 16px 40px -10px rgba(255, 107, 107, 0.25);
}

html.dark .review-header {
  border-bottom-color: rgba(255, 107, 107, 0.15);
}

html.dark .user-name {
  color: var(--neutral-800);
}

html.dark .review-text {
  color: var(--neutral-600);
}

html.dark :deep(.el-tag--success) {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.2) 0%, rgba(69, 183, 209, 0.15) 100%);
  color: var(--color-mint);
}

html.dark :deep(.el-tag--warning) {
  background: linear-gradient(135deg, rgba(255, 230, 109, 0.25) 0%, rgba(255, 107, 107, 0.15) 100%);
  color: var(--color-sunshine);
}

html.dark :deep(.el-tag--danger) {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(185, 28, 28, 0.15) 100%);
  color: #F87171;
}
</style>
