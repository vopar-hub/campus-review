<template>
  <AppLayout>
    <div class="admin-user-page" v-loading="loading">
      <div class="page-header">
        <div class="header-left">
          <el-icon :size="28" class="header-icon"><User /></el-icon>
          <h1>用户管理</h1>
        </div>
      </div>

      <!-- 搜索栏 -->
      <el-card class="search-card">
        <div class="search-form">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名、邮箱或学号"
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

      <!-- 用户列表 -->
      <el-card class="users-card">
        <el-table :data="filteredUsers" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="nickname" label="昵称" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="180" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.roles?.includes('ADMIN') ? 'danger' : 'primary'" size="small">
                {{ row.roles?.includes('ADMIN') ? '管理员' : '用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.banned ? 'danger' : 'success'" size="small">
                {{ row.banned ? '封禁' : '正常' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="注册时间" width="180">
            <template #default="{ row }">
              {{ formattedTime(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.banned"
                type="success"
                size="small"
                @click="handleUnban(row)"
              >
                解封
              </el-button>
              <el-button
                v-else
                type="warning"
                size="small"
                @click="handleBan(row)"
                :disabled="row.roles?.includes('ADMIN')"
              >
                封禁
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="users.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无用户数据" />
        </div>
      </el-card>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Search, Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import AppLayout from '@/components/layout/AppLayout.vue'
import { getUserList, banUser, unbanUser } from '@/api/admin'
import { getLogger } from '@/utils/logger'
import type { UserDTO } from '@/types'

dayjs.locale('zh-cn')

const logger = getLogger('views-admin-user-manage')

const loading = ref(false)
const users = ref<UserDTO[]>([])
const searchKeyword = ref('')

const formattedTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList()
    users.value = res.data || []
  } catch (error) {
    logger.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 过滤用户列表
const filteredUsers = computed(() => {
  if (!searchKeyword.value) {
    return users.value
  }
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(user =>
    user.nickname?.toLowerCase().includes(keyword) ||
    user.email?.toLowerCase().includes(keyword) ||
    user.studentNo?.toLowerCase().includes(keyword)
  )
})

const handleSearch = () => {
  // 使用 computed 自动过滤，无需额外操作
}

const resetSearch = () => {
  searchKeyword.value = ''
}

const handleBan = async (user: UserDTO) => {
  try {
    await ElMessageBox.confirm(`确定要封禁用户 "${user.nickname}" 吗？`, '封禁用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await banUser(user.id!)
    ElMessage.success('用户已封禁')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('封禁用户失败:', error)
    }
  }
}

const handleUnban = async (user: UserDTO) => {
  try {
    await ElMessageBox.confirm(`确定要解封用户 "${user.nickname}" 吗？`, '解封用户', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })

    await unbanUser(user.id!)
    ElMessage.success('用户已解封')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      logger.error('解封用户失败:', error)
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-user-page {
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

.users-card {
  border-radius: 16px;
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
html.dark .admin-user-page .page-header h1 {
  color: #fafafa;
}

html.dark .search-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 100%);
  border-color: rgba(249, 115, 22, 0.15);
}

html.dark .users-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 100%);
  border-color: rgba(249, 115, 22, 0.15);
}
</style>
