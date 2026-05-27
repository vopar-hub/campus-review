<template>
  <AppLayout>
    <div class="profile-page">
      <div class="page-header">
        <h1>个人中心</h1>
      </div>

      <div v-loading="loading" class="profile-content">
        <template v-if="userStore.user">
          <!-- 用户信息卡片 -->
          <el-card class="user-info-card">
            <div class="user-header">
              <el-avatar :size="80">
                <User />
              </el-avatar>
              <div class="user-details">
                <h2>{{ userStore.user.nickname }}</h2>
                <p class="user-email">
                  <el-icon><Message /></el-icon>
                  {{ userStore.user.email }}
                </p>
                <p class="user-student">
                  <el-icon><Ticket /></el-icon>
                  学号：{{ userStore.user.studentNo }}
                </p>
                <div class="user-roles">
                  <el-tag
                    v-for="role in userStore.user.roles"
                    :key="role"
                    :type="role === 'ADMIN' ? 'danger' : 'primary'"
                  >
                    {{ role }}
                  </el-tag>
                </div>
              </div>
            </div>

            <el-divider />

            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-label">注册时间</span>
                <span class="stat-value">{{ formattedRegisterTime }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">账号状态</span>
                <el-tag :type="userStore.user.banned ? 'danger' : 'success'">
                  {{ userStore.user.banned ? '已封禁' : '正常' }}
                </el-tag>
              </div>
            </div>
          </el-card>

          <!-- 我的评价 -->
          <el-card class="my-reviews-card">
            <h3>我的评价</h3>
            <div v-loading="reviewsLoading" class="reviews-list">
              <template v-if="myReviews.length > 0">
                <ReviewCard v-for="review in myReviews" :key="review.id" :review="review" />
              </template>
              <el-empty v-else description="暂无评价" />
            </div>
          </el-card>
        </template>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { User, Message, Ticket } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import AppLayout from '@/components/layout/AppLayout.vue'
import ReviewCard from '@/components/review/ReviewCard.vue'
import { useUserStore } from '@/stores/user'
import { getMyReviews } from '@/api/review'
import type { ReviewDTO } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-user-profile')

dayjs.locale('zh-cn')

const userStore = useUserStore()

const loading = ref(false)
const reviewsLoading = ref(false)
const myReviews = ref<ReviewDTO[]>([])

const formattedRegisterTime = computed(() => {
  if (!userStore.user) return ''
  return dayjs(userStore.user.createdAt).format('YYYY-MM-DD HH:mm:ss')
})

// 加载我的评价
const loadMyReviews = async () => {
  reviewsLoading.value = true
  try {
    const res = await getMyReviews()
    myReviews.value = res.data
  } catch (error) {
    logger.error('加载我的评价失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

onMounted(() => {
  loadMyReviews()
})
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  h1 {
    margin: 0;
    font-size: 28px;
    color: #333;
  }
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.user-info-card {
  .user-header {
    display: flex;
    gap: 24px;
    align-items: center;
  }

  .user-details {
    flex: 1;

    h2 {
      margin: 0 0 12px 0;
      font-size: 24px;
      color: #333;
    }
  }

  .user-email,
  .user-student {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 8px 0;
    font-size: 14px;
    color: #666;
  }

  .user-roles {
    display: flex;
    gap: 8px;
    margin-top: 12px;
  }

  .user-stats {
    display: flex;
    gap: 32px;
    padding-top: 16px;

    .stat-item {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .stat-label {
        font-size: 12px;
        color: #999;
      }

      .stat-value {
        font-size: 16px;
        color: #333;
        font-weight: 500;
      }
    }
  }
}

.my-reviews-card {
  h3 {
    margin: 0 0 20px 0;
    font-size: 18px;
    color: #333;
  }
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>
