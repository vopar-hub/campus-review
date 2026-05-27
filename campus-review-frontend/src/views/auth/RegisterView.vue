<template>
  <div class="register-view">
    <!-- 装饰性背景 -->
    <div class="decorative-background">
      <div class="decor-gradient decor-1"></div>
      <div class="decor-gradient decor-2"></div>
      <div class="decor-gradient decor-3"></div>
      <div class="decor-circle decor-4"></div>
      <div class="decor-circle decor-5"></div>
    </div>

    <!-- 主内容 -->
    <div class="register-wrapper">
      <!-- 左侧品牌区 -->
      <div class="register-brand">
        <div class="brand-content">
          <div class="brand-logo-wrapper">
            <img src="/logo.png" alt="中原科技学院" class="brand-logo" />
          </div>
          <h1 class="brand-title">食品评价平台</h1>
          <p class="brand-subtitle">发现校园里的每一味美好</p>

          <div class="brand-features">
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Food /></el-icon>
              </div>
              <div class="feature-text">
                <strong>校园美食</strong>
                <span>探索校内特色餐馆</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="feature-text">
                <strong>真实评价</strong>
                <span>师生真实用餐体验</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <el-icon><Trophy /></el-icon>
              </div>
              <div class="feature-text">
                <strong>排行榜单</strong>
                <span>发现最受欢迎美食</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="register-form-section">
        <div class="form-container">
          <el-card class="register-card">
            <template #header>
              <div class="card-header">
                <h2>创建账号</h2>
                <p>加入我们，开启美食探索之旅</p>
              </div>
            </template>

            <el-form
              ref="formRef"
              :model="registerForm"
              :rules="rules"
              label-width="0"
              size="large"
            >
              <el-form-item prop="nickname">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><User /></el-icon>
                  <el-input
                    v-model="registerForm.nickname"
                    placeholder="昵称"
                    clearable
                  />
                </div>
              </el-form-item>

              <el-form-item prop="email">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Message /></el-icon>
                  <el-input
                    v-model="registerForm.email"
                    placeholder="校园邮箱"
                    clearable
                  />
                </div>
              </el-form-item>

              <el-form-item prop="studentNo">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Ticket /></el-icon>
                  <el-input
                    v-model="registerForm.studentNo"
                    placeholder="学号"
                    clearable
                  />
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="registerForm.password"
                    type="password"
                    placeholder="密码（至少 6 位）"
                    show-password
                  />
                </div>
              </el-form-item>

              <el-form-item prop="confirmPassword">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="registerForm.confirmPassword"
                    type="password"
                    placeholder="确认密码"
                    show-password
                  />
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="loading"
                  class="register-button"
                  @click="handleRegister"
                >
                  <span v-if="!loading">注册</span>
                  <span v-else>注册中...</span>
                </el-button>
              </el-form-item>

              <div class="form-footer">
                <span>已有账号？</span>
                <router-link to="/login" class="login-link">立即登录</router-link>
              </div>
            </el-form>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Food, User, Message, Ticket, Lock, Star, Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-auth-register')

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  nickname: '',
  email: '',
  studentNo: '',
  password: '',
  confirmPassword: '',
})

// 自定义验证器：密码匹配
const validateConfirmPassword = (_rule: any, value: string, callback: any) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 50, message: '昵称长度 1-50 个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.register({
        nickname: registerForm.nickname,
        email: registerForm.email,
        studentNo: registerForm.studentNo,
        password: registerForm.password,
      })
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (error) {
      logger.error('注册失败:', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
/* ========== 暗夜模式样式 ========== */
html.dark .register-view {
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a2e 50%, #0f0f0f 100%);
}

html.dark .register-brand {
  background: linear-gradient(135deg, #16161a 0%, #1a1a2e 100%);
}

html.dark .brand-logo-wrapper {
  background: #2d2d44;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4), 0 2px 8px rgba(255, 255, 255, 0.05);
}

html.dark .brand-subtitle {
  color: #a3a3a3;
}

html.dark .feature-item {
  background: #1a1a2e;
  border-color: rgba(249, 115, 22, 0.15);
}

html.dark .feature-item:hover {
  border-color: rgba(249, 115, 22, 0.3);
}

html.dark .feature-text strong {
  color: #d4d4d4;
}

html.dark .feature-text span {
  color: #737373;
}

html.dark .register-form-section {
  background: #0f0f0f;
  border-left-color: rgba(249, 115, 22, 0.15);
}

html.dark .register-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16161a 100%);
  border-color: rgba(249, 115, 22, 0.15);
}

html.dark .card-header h2 {
  color: #fafafa;
}

html.dark .card-header p {
  color: #737373;
}

html.dark .input-wrapper {
  background: #2d2d44;
}

html.dark .input-wrapper:focus-within {
  background: #1a1a2e;
}

html.dark .input-icon {
  color: #737373;
}

html.dark .input-wrapper:focus-within .input-icon {
  color: #fb923c;
}

html.dark .form-footer {
  color: #737373;
}

html.dark .register-button:disabled {
  opacity: 0.6;
}

.register-view {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #fafafa 0%, #fff7ed 50%, #ffffff 100%);
}

/* ========== 装饰性背景 ========== */
.decorative-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
}

.decor-gradient {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}

.decor-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.15) 0%, transparent 70%);
  top: -200px;
  right: -100px;
}

.decor-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(244, 63, 94, 0.1) 0%, transparent 70%);
  bottom: -150px;
  left: -100px;
  animation-delay: 2s;
}

.decor-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(251, 146, 60, 0.12) 0%, transparent 70%);
  top: 40%;
  right: 30%;
  animation-delay: 4s;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(249, 115, 22, 0.1);
  animation: float 10s ease-in-out infinite;
}

.decor-4 {
  width: 200px;
  height: 200px;
  top: 20%;
  left: 40%;
  animation-delay: 1s;
}

.decor-5 {
  width: 150px;
  height: 150px;
  bottom: 30%;
  right: 20%;
  animation-delay: 3s;
}

/* ========== 主布局 ========== */
.register-wrapper {
  display: flex;
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* ========== 左侧品牌区 ========== */
.register-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.02) 0%, rgba(244, 63, 94, 0.02) 100%);
}

.brand-content {
  max-width: 480px;
  text-align: center;
}

.brand-logo-wrapper {
  width: 120px;
  height: 120px;
  margin: 0 auto 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 28px;
  box-shadow:
    0 8px 32px rgba(249, 115, 22, 0.15),
    0 2px 8px rgba(0, 0, 0, 0.05);
  padding: 16px;
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.brand-logo-wrapper:hover {
  transform: scale(1.05) rotate(-3deg);
}

.brand-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.brand-title {
  margin: 0 0 12px 0;
  font-size: 36px;
  font-weight: 800;
  /* 蓝黄渐变 */
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 30%, #fbbf24 70%, #f59e0b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 2px;
}

.brand-subtitle {
  margin: 0 0 48px 0;
  font-size: 16px;
  color: var(--neutral-500);
  font-weight: 400;
}

/* 特性列表 */
.brand-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(249, 115, 22, 0.08);
  border: 1px solid rgba(249, 115, 22, 0.05);
  transition: all 0.3s ease;
  width: 100%;
  max-width: 360px;
}

.feature-item:hover {
  transform: translateX(8px);
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.12);
  border-color: rgba(249, 115, 22, 0.2);
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary-gradient);
  border-radius: 14px;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.3);
}

.feature-icon .el-icon {
  font-size: 24px;
}

.feature-text {
  text-align: left;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feature-text strong {
  font-size: 16px;
  color: var(--neutral-800);
  font-weight: 600;
}

.feature-text span {
  font-size: 13px;
  color: var(--neutral-400);
}

/* ========== 右侧注册表单 ========== */
.register-form-section {
  width: 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  border-left: 1px solid rgba(249, 115, 22, 0.08);
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.register-card {
  border-radius: 24px;
  border: 1px solid rgba(249, 115, 22, 0.08);
  box-shadow: 0 8px 32px rgba(249, 115, 22, 0.08);
}

:deep(.el-card__header) {
  padding: 32px 32px 24px;
  border-bottom: none;
  background: transparent;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--neutral-800);
}

.card-header p {
  margin: 0;
  font-size: 14px;
  color: var(--neutral-400);
}

:deep(.el-card__body) {
  padding: 16px 32px 32px;
}

/* 输入框样式 */
:deep(.el-form-item) {
  margin-bottom: 16px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: var(--neutral-50);
  border-radius: 16px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.input-wrapper:focus-within {
  background: white;
  border-color: #f97316;
  box-shadow: 0 0 0 4px rgba(249, 115, 22, 0.1);
}

.input-icon {
  font-size: 20px;
  color: var(--neutral-400);
  transition: color 0.3s ease;
}

.input-wrapper:focus-within .input-icon {
  color: #f97316;
}

:deep(.el-input__inner) {
  border: none !important;
  background: transparent !important;
  padding: 8px 0 !important;
  font-size: 15px !important;
  color: var(--neutral-700);
}

:deep(.el-input__inner::placeholder) {
  color: var(--neutral-400);
}

:deep(.el-input__clear) {
  color: var(--neutral-300);
}

/* 注册按钮 */
.register-button {
  width: 100%;
  height: 54px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 16px;
  background: var(--primary-gradient);
  border: none;
  margin-top: 8px;
  transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 8px 24px rgba(249, 115, 22, 0.3);
}

.register-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 12px 32px rgba(249, 115, 22, 0.4);
}

.register-button:active:not(:disabled) {
  transform: translateY(-1px);
}

/* 页脚链接 */
.form-footer {
  text-align: center;
  margin-top: 24px;
  color: var(--neutral-500);
  font-size: 14px;
}

.form-footer a {
  color: #f97316;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.3s ease;
}

.form-footer a:hover {
  color: #ea580c;
  text-decoration: underline;
}

.login-link {
  margin-left: 4px !important;
}

/* 响应式设计 */
@media (max-width: 968px) {
  .register-view {
    flex-direction: column;
  }

  .register-brand {
    padding: 40px 24px;
    min-height: auto;
  }

  .brand-features {
    display: none;
  }

  .register-form-section {
    width: 100%;
    border-left: none;
    border-top: 1px solid rgba(249, 115, 22, 0.08);
    padding: 40px 24px;
  }
}

/* 移动端进一步优化 */
@media (max-width: 768px) {
  .register-view {
    min-height: 100vh;
    padding-bottom: calc(60px + env(safe-area-inset-bottom));
  }

  .register-brand {
    padding: 32px 20px;
  }

  .brand-logo-wrapper {
    width: 80px;
    height: 80px;
    margin-bottom: 20px;
  }

  .brand-title {
    font-size: 26px;
  }

  .brand-subtitle {
    font-size: 14px;
    margin-bottom: 0;
  }

  .register-form-section {
    padding: 24px 16px;
    flex: 1;
  }

  .form-container {
    max-width: 100%;
  }

  .register-card {
    border-radius: var(--radius-xl);
  }

  :deep(.el-card__header) {
    padding: 24px 20px 16px;
  }

  .card-header h2 {
    font-size: 22px;
  }

  .card-header p {
    font-size: 13px;
  }

  :deep(.el-card__body) {
    padding: 12px 20px 24px;
  }

  .input-wrapper {
    padding: 12px 14px;
    gap: 10px;
  }

  .input-icon {
    font-size: 18px;
  }

  :deep(.el-input__inner) {
    font-size: 14px !important;
  }

  .register-button {
    height: 48px;
    font-size: 15px;
  }

  .form-footer {
    margin-top: 20px;
    font-size: 13px;
  }
}

/* 加载动画 */
:deep(.el-loading-spinner .path) {
  stroke: #f97316;
}

:deep(.el-loading-spinner .el-loading-text) {
  color: #f97316;
}
</style>
