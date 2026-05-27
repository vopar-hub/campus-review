<template>
  <AppLayout>
    <div class="restaurant-detail-page" v-loading="loading">
      <template v-if="restaurant">
        <!-- 餐馆信息 -->
        <el-card class="restaurant-info-card">
          <div class="restaurant-header">
            <div class="cover-section">
              <div class="cover-image">
                <img
                  v-if="restaurant.coverImageUrl"
                  :src="restaurant.coverImageUrl"
                  :alt="restaurant.name"
                />
                <div v-else class="cover-placeholder">
                  <el-icon :size="64"><Food /></el-icon>
                </div>
              </div>
              <div class="cover-badge">
                <el-icon><Star /></el-icon>
              </div>
            </div>
            <div class="restaurant-details">
              <div class="name-section">
                <h1>{{ restaurant.name }}</h1>
              </div>
              <p class="location">
                <el-icon><Location /></el-icon>
                {{ restaurant.campus }} · {{ restaurant.address }}
              </p>
              <p class="description">{{ restaurant.description }}</p>
              <p class="create-time">
                <el-icon><Clock /></el-icon>
                {{ formattedTime }}
              </p>
            </div>
          </div>
        </el-card>

        <!-- 发布评价 -->
        <el-card class="review-form-card" v-if="userStore.isLoggedIn">
          <div class="card-header">
            <div class="header-icon-wrapper">
              <el-icon :size="20"><Edit /></el-icon>
            </div>
            <h3>发表评价</h3>
          </div>
          <el-form
            ref="formRef"
            :model="reviewForm"
            :rules="rules"
            label-width="0"
          >
            <el-form-item prop="rating">
              <div class="rating-label">评分</div>
              <StarRating v-model="reviewForm.rating" />
            </el-form-item>
            <el-form-item prop="content">
              <el-input
                v-model="reviewForm.content"
                type="textarea"
                :rows="4"
                placeholder="分享你的用餐体验..."
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleSubmit">
                提交评价
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-alert
          v-else
          title="登录后才能发表评价"
          type="info"
          :closable="false"
          show-icon
          class="login-alert"
        />

        <!-- 评价列表 -->
        <el-card class="reviews-card">
          <div class="card-header">
            <div class="header-icon-wrapper icon-mint">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </div>
            <h3>用户评价</h3>
          </div>
          <div v-loading="reviewsLoading" class="reviews-list">
            <template v-if="reviews.length > 0">
              <ReviewCard v-for="review in reviews" :key="review.id" :review="review" />
            </template>
            <el-empty v-else description="暂无评价">
              <template #image>
                <div class="empty-icon">
                  <el-icon :size="60"><ChatDotRound /></el-icon>
                </div>
              </template>
            </el-empty>
          </div>
        </el-card>
      </template>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Food, Location, Clock, Star, Edit, ChatDotRound } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import AppLayout from '@/components/layout/AppLayout.vue'
import StarRating from '@/components/review/StarRating.vue'
import ReviewCard from '@/components/review/ReviewCard.vue'
import { getRestaurant } from '@/api/restaurant'
import { getReviewsByRestaurant, createReview } from '@/api/review'
import { useUserStore } from '@/stores/user'
import type { RestaurantDTO, ReviewDTO } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-restaurant-detail')

dayjs.locale('zh-cn')

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const reviewsLoading = ref(false)
const submitting = ref(false)
const restaurant = ref<RestaurantDTO | null>(null)
const reviews = ref<ReviewDTO[]>([])

const formRef = ref<FormInstance>()

const reviewForm = reactive({
  rating: 5,
  content: '',
})

const rules: FormRules = {
  rating: [
    { required: true, message: '请选择评分', trigger: 'change' },
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, message: '评价内容至少 5 个字符', trigger: 'blur' },
  ],
}

const formattedTime = computed(() => {
  if (!restaurant.value) return ''
  return dayjs(restaurant.value.createdAt).format('YYYY-MM-DD HH:mm')
})

// 加载餐馆详情
const loadRestaurant = async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getRestaurant(id)
    restaurant.value = res.data
  } catch (error) {
    logger.error('加载餐馆详情失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载评价列表
const loadReviews = async () => {
  const id = Number(route.params.id)
  if (!id) return

  reviewsLoading.value = true
  try {
    const res = await getReviewsByRestaurant(id)
    reviews.value = res.data
  } catch (error) {
    logger.error('加载评价失败:', error)
  } finally {
    reviewsLoading.value = false
  }
}

// 提交评价
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await createReview({
        restaurantId: Number(route.params.id),
        rating: reviewForm.rating,
        content: reviewForm.content,
      })
      ElMessage.success('评价提交成功，等待审核后显示')
      reviewForm.content = ''
      reviewForm.rating = 5
      loadReviews()
    } catch (error: unknown) {
      logger.error('提交评价失败:', error)
      // 处理限流错误
      const apiError = error as { response?: { data?: { code?: number; message?: string } } }
      if (apiError.response?.data?.code === 42900) {
        ElMessage.error(apiError.response.data.message || '发送评价太频繁')
      }
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  loadRestaurant()
  loadReviews()
})
</script>

<style scoped>
.restaurant-detail-page {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding-bottom: 40px;
}

.restaurant-info-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--neutral-200);
}

.restaurant-header {
  display: flex;
  gap: 36px;
  padding: 8px 0;
}

.cover-section {
  position: relative;
  flex-shrink: 0;
}

.cover-image {
  width: 260px;
  height: 260px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  background: linear-gradient(135deg, var(--neutral-100) 0%, var(--neutral-200) 100%);
  box-shadow: var(--shadow-lg);
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s var(--ease-bounce);
}

.cover-image:hover img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  color: white;
}

.cover-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  backdrop-filter: blur(10px);
  border-radius: 50%;
  color: var(--color-sunshine);
  box-shadow: var(--shadow-md);
  font-size: 22px;
}

.restaurant-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.name-section h1 {
  margin: 0 0 16px 0;
  font-size: 34px;
  font-weight: 800;
  color: var(--neutral-800);
  letter-spacing: -0.5px;
}

.location {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 15px;
  color: var(--color-mint);
  font-weight: 500;
}

.location .el-icon {
  flex-shrink: 0;
}

.description {
  margin: 0 0 16px 0;
  font-size: 15px;
  color: var(--neutral-600);
  line-height: 1.8;
}

.create-time {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 13px;
  color: var(--neutral-400);
}

.review-form-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--neutral-200);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.header-icon-wrapper {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-lg);
  color: white;
  box-shadow: var(--shadow-coral);
}

.header-icon-wrapper.icon-mint {
  background: var(--gradient-fresh);
  box-shadow: var(--shadow-mint);
}

.card-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--neutral-800);
}

.rating-label {
  font-size: 14px;
  color: var(--neutral-600);
  margin-bottom: 12px;
  margin-right: 16px;
  display: inline-block;
  vertical-align: middle;
  font-weight: 500;
}

.review-form-card :deep(.el-textarea__inner) {
  border-radius: var(--radius-lg);
  border: 2px solid var(--neutral-200);
  font-size: 15px;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.review-form-card :deep(.el-textarea__inner:focus) {
  border-color: var(--color-coral);
  box-shadow: 0 0 0 4px rgba(255, 107, 107, 0.1);
}

.review-form-card .el-button--primary {
  background: var(--gradient-primary);
  border: none;
  padding: 14px 36px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-coral);
  transition: all var(--duration-normal) var(--ease-bounce);
}

.review-form-card .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(255, 107, 107, 0.4);
}

.login-alert {
  border-radius: var(--radius-lg);
  border: none;
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.08) 0%, rgba(69, 183, 209, 0.05) 100%);
  border: 1px solid rgba(78, 205, 196, 0.2);
}

.reviews-card {
  border-radius: var(--radius-xl);
  border: 1px solid var(--neutral-200);
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.empty-icon {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-fresh);
  border-radius: 50%;
  color: white;
  margin: 0 auto;
  box-shadow: var(--shadow-mint);
}

/* ========== 暗夜模式样式 ========== */
html.dark .restaurant-info-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .cover-image {
  background: linear-gradient(135deg, var(--neutral-200) 0%, var(--neutral-300) 100%);
}

html.dark .cover-badge {
  background: var(--neutral-100);
}

html.dark .name-section h1 {
  color: var(--neutral-800);
}

html.dark .location {
  color: var(--color-mint);
}

html.dark .description {
  color: var(--neutral-600);
}

html.dark .create-time {
  color: var(--neutral-400);
}

html.dark .review-form-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .card-header h3 {
  color: var(--neutral-800);
}

html.dark .review-form-card :deep(.el-textarea__inner) {
  background: var(--neutral-200);
  border-color: rgba(255, 255, 255, 0.1);
  color: var(--neutral-700);
}

html.dark .login-alert {
  background: linear-gradient(135deg, rgba(78, 205, 196, 0.12) 0%, rgba(69, 183, 209, 0.08) 100%);
  border-color: rgba(78, 205, 196, 0.25);
}

html.dark .reviews-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

/* 加载动画 */
:deep(.el-loading-mask) {
  border-radius: var(--radius-xl);
  background: rgba(255, 255, 255, 0.9);
}

html.dark :deep(.el-loading-mask) {
  background: rgba(22, 27, 34, 0.9);
}

/* ========== 移动端响应式 ========== */
@media (max-width: 768px) {
  .restaurant-detail-page {
    gap: 20px;
    padding-bottom: calc(80px + env(safe-area-inset-bottom));
  }

  .restaurant-info-card {
    border-radius: var(--radius-lg);
  }

  .restaurant-header {
    flex-direction: column;
    gap: 20px;
  }

  .cover-section {
    width: 100%;
  }

  .cover-image {
    width: 100%;
    height: 200px;
  }

  .cover-badge {
    width: 40px;
    height: 40px;
    top: 12px;
    right: 12px;
  }

  .restaurant-details {
    text-align: center;
  }

  .name-section h1 {
    font-size: 24px;
  }

  .location {
    justify-content: center;
    font-size: 14px;
  }

  .description {
    font-size: 14px;
    line-height: 1.7;
  }

  .create-time {
    justify-content: center;
    font-size: 12px;
  }

  .review-form-card,
  .reviews-card {
    border-radius: var(--radius-lg);
  }

  .card-header {
    margin-bottom: 16px;
  }

  .card-header h3 {
    font-size: 18px;
  }

  .header-icon-wrapper {
    width: 36px;
    height: 36px;
  }

  .rating-label {
    font-size: 13px;
    margin-bottom: 8px;
  }

  .review-form-card :deep(.el-textarea__inner) {
    font-size: 14px;
  }

  .review-form-card .el-button--primary {
    width: 100%;
    padding: 12px 24px;
    font-size: 14px;
  }

  .reviews-list {
    gap: 12px;
  }
}
</style>
