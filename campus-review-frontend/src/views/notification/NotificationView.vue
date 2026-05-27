<template>
  <AppLayout>
    <div class="notification-page">
      <div class="page-header">
        <h1>
          <el-icon><Bell /></el-icon>
          消息通知
        </h1>
        <el-button v-if="messages.length > 0" @click="markAllAsRead">
          全部标记为已读
        </el-button>
      </div>

      <div v-loading="loading" class="message-list">
        <template v-if="messages.length > 0">
          <el-card
            v-for="message in messages"
            :key="message.id"
            class="message-card"
            :class="{ 'is-read': message.read }"
            shadow="hover"
          >
            <div class="message-content">
              <div class="message-header">
                <h3>{{ message.title }}</h3>
                <el-tag :type="message.read ? 'info' : 'primary'" size="small">
                  {{ message.read ? '已读' : '未读' }}
                </el-tag>
              </div>
              <p class="message-body">{{ message.content }}</p>
              <div class="message-footer">
                <span class="message-time">{{ formattedTime(message.createdAt) }}</span>
                <el-button
                  v-if="!message.read"
                  type="primary"
                  size="small"
                  @click="markAsRead(message.id)"
                >
                  标记为已读
                </el-button>
              </div>
            </div>
          </el-card>
        </template>
        <el-empty v-else description="暂无消息" />
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import relativeTime from 'dayjs/plugin/relativeTime'
import AppLayout from '@/components/layout/AppLayout.vue'
import { getInbox, markAsRead as markAsReadApi } from '@/api/notification'
import type { MessageDTO } from '@/types'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-notification')

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const messages = ref<MessageDTO[]>([])

const formattedTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadMessages = async () => {
  loading.value = true
  try {
    const res = await getInbox()
    messages.value = res.data
  } catch (error) {
    logger.error('加载消息失败:', error)
  } finally {
    loading.value = false
  }
}

const markAsRead = async (id: number) => {
  try {
    await markAsReadApi(id)
    const message = messages.value.find(m => m.id === id)
    if (message) {
      message.read = true
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    logger.error('标记已读失败:', error)
  }
}

const markAllAsRead = async () => {
  const unreadMessages = messages.value.filter(m => !m.read)
  try {
    await Promise.all(unreadMessages.map(m => markAsReadApi(m.id)))
    messages.value.forEach(m => m.read = true)
    ElMessage.success('全部标记为已读')
  } catch (error) {
    logger.error('批量标记失败:', error)
  }
}

onMounted(() => {
  loadMessages()
})
</script>

<style scoped>
.notification-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h1 {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0;
    font-size: 28px;
    color: #333;
  }
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-card {
  transition: all 0.3s;

  &.is-read {
    opacity: 0.7;
  }

  &:hover {
    transform: translateX(4px);
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-size: 16px;
    color: #333;
  }
}

.message-body {
  margin: 0;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.message-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.message-time {
  font-size: 12px;
  color: #999;
}
</style>
