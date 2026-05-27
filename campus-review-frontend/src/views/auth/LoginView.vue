<template>
  <div class="login-view">
    <!-- 装饰性背景 -->
    <div class="decorative-background">
      <div class="decor-shape shape-1"></div>
      <div class="decor-shape shape-2"></div>
      <div class="decor-shape shape-3"></div>
      <div class="decor-shape shape-4"></div>
      <div class="decor-circle circle-1"></div>
      <div class="decor-circle circle-2"></div>
      <div class="decor-circle circle-3"></div>
    </div>

    <!-- 主内容 -->
    <div class="login-wrapper">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo-wrapper">
            <img src="/logo.png" alt="中原科技学院" class="brand-logo" />
          </div>
          <h1 class="brand-title">食品评价平台</h1>
          <p class="brand-subtitle">发现校园里的每一味美好</p>

          <div class="brand-features">
            <div class="feature-item feature-coral">
              <div class="feature-icon">
                <el-icon><Food /></el-icon>
              </div>
              <div class="feature-text">
                <strong>校园美食</strong>
                <span>探索校内特色餐馆</span>
              </div>
            </div>
            <div class="feature-item feature-mint">
              <div class="feature-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="feature-text">
                <strong>真实评价</strong>
                <span>师生真实用餐体验</span>
              </div>
            </div>
            <div class="feature-item feature-lavender">
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

      <!-- 右侧登录表单 -->
      <div class="login-form-section">
        <div class="form-container">
          <el-card class="login-card">
            <template #header>
              <div class="card-header">
                <div class="header-icon">
                  <el-icon><User /></el-icon>
                </div>
                <h2>欢迎回来</h2>
                <p>登录账号，继续探索美食之旅</p>
              </div>
            </template>

            <el-form
              ref="formRef"
              :model="loginForm"
              :rules="rules"
              label-width="0"
              size="large"
            >
              <el-form-item prop="account">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><User /></el-icon>
                  <el-input
                    v-model="loginForm.account"
                    placeholder="邮箱或学号"
                    clearable
                  />
                </div>
              </el-form-item>

              <el-form-item prop="password">
                <div class="input-wrapper">
                  <el-icon class="input-icon"><Lock /></el-icon>
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    show-password
                    @keyup.enter="handleLogin"
                  />
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="loading"
                  class="login-button"
                  @click="handleLogin"
                >
                  <span v-if="!loading">登录</span>
                  <span v-else>登录中...</span>
                </el-button>
              </el-form-item>

              <div class="form-footer">
                <span>还没有账号？</span>
                <router-link to="/register" class="register-link">立即注册</router-link>
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
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { Food, User, Lock, Star, Trophy } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getLogger } from '@/utils/logger'

const logger = getLogger('views-auth-login')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
})

const rules: FormRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 位', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')

      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } catch (error) {
      logger.error('登录失败:', error)
      // 错误信息由 axios 拦截器通过 ElMessage 显示，这里不需要重复提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-view {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
  background: var(--neutral-50);
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

.decor-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 12s ease-in-out infinite;
}

.shape-1 {
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(255, 107, 107, 0.15) 0%, transparent 70%);
  top: -300px;
  right: -200px;
}

.shape-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(78, 205, 196, 0.12) 0%, transparent 70%);
  bottom: -200px;
  left: -150px;
  animation-delay: 3s;
}

.shape-3 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.1) 0%, transparent 70%);
  top: 30%;
  right: 20%;
  animation-delay: 6s;
}

.shape-4 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(255, 230, 109, 0.12) 0%, transparent 70%);
  bottom: 20%;
  right: 40%;
  animation-delay: 9s;
}

.decor-circle {
  position: absolute;
  border-radius: 50%;
  border: 2px solid;
  animation: float 15s ease-in-out infinite;
}

.circle-1 {
  width: 200px;
  height: 200px;
  border-color: rgba(255, 107, 107, 0.15);
  top: 15%;
  left: 35%;
}

.circle-2 {
  width: 150px;
  height: 150px;
  border-color: rgba(78, 205, 196, 0.15);
  bottom: 25%;
  right: 15%;
  animation-delay: 5s;
}

.circle-3 {
  width: 100px;
  height: 100px;
  border-color: rgba(167, 139, 250, 0.15);
  top: 60%;
  left: 10%;
  animation-delay: 10s;
}

/* ========== 主布局 ========== */
.login-wrapper {
  display: flex;
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* ========== 左侧品牌区 ========== */
.login-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.02) 0%, rgba(78, 205, 196, 0.02) 100%);
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
  border-radius: var(--radius-2xl);
  box-shadow: 0 8px 40px rgba(255, 107, 107, 0.15);
  padding: 16px;
  transition: all var(--duration-normal) var(--ease-bounce);
}

.brand-logo-wrapper:hover {
  transform: scale(1.05) rotate(-3deg);
  box-shadow: 0 12px 50px rgba(255, 107, 107, 0.25);
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
  background: var(--gradient-rainbow);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradient-shift 4s ease infinite;
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
  gap: 16px;
  align-items: center;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  background: white;
  border-radius: var(--radius-xl);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: 2px solid transparent;
  transition: all var(--duration-normal) var(--ease-bounce);
  width: 100%;
  max-width: 360px;
}

.feature-item:hover {
  transform: translateX(8px);
}

.feature-coral:hover {
  border-color: rgba(255, 107, 107, 0.3);
  box-shadow: 0 8px 30px rgba(255, 107, 107, 0.15);
}

.feature-mint:hover {
  border-color: rgba(78, 205, 196, 0.3);
  box-shadow: 0 8px 30px rgba(78, 205, 196, 0.15);
}

.feature-lavender:hover {
  border-color: rgba(167, 139, 250, 0.3);
  box-shadow: 0 8px 30px rgba(167, 139, 250, 0.15);
}

.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-lg);
  color: white;
  flex-shrink: 0;
}

.feature-coral .feature-icon {
  background: var(--gradient-primary);
  box-shadow: var(--shadow-coral);
}

.feature-mint .feature-icon {
  background: var(--gradient-fresh);
  box-shadow: var(--shadow-mint);
}

.feature-lavender .feature-icon {
  background: var(--gradient-dreamy);
  box-shadow: var(--shadow-lavender);
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

/* ========== 右侧登录表单 ========== */
.login-form-section {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: white;
  border-left: 1px solid var(--neutral-200);
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.login-card {
  border-radius: var(--radius-2xl);
  border: 1px solid var(--neutral-200);
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.06);
}

:deep(.el-card__header) {
  padding: 32px 32px 24px;
  border-bottom: none;
  background: transparent;
}

.card-header {
  text-align: center;
}

.header-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gradient-primary);
  border-radius: var(--radius-xl);
  color: white;
  font-size: 28px;
  box-shadow: var(--shadow-coral);
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
  margin-bottom: 20px;
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: var(--neutral-100);
  border-radius: var(--radius-lg);
  border: 2px solid transparent;
  transition: all var(--duration-normal) var(--ease-smooth);
}

.input-wrapper:focus-within {
  background: white;
  border-color: var(--color-coral);
  box-shadow: 0 0 0 4px rgba(255, 107, 107, 0.1);
}

.input-icon {
  font-size: 20px;
  color: var(--neutral-400);
  transition: color var(--duration-fast) var(--ease-smooth);
}

.input-wrapper:focus-within .input-icon {
  color: var(--color-coral);
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

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 54px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-lg);
  background: var(--gradient-primary);
  border: none;
  margin-top: 8px;
  transition: all var(--duration-normal) var(--ease-bounce);
  box-shadow: var(--shadow-coral);
}

.login-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 16px 40px rgba(255, 107, 107, 0.4);
}

.login-button:active:not(:disabled) {
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
  color: var(--color-coral);
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color var(--duration-fast) var(--ease-smooth);
}

.form-footer a:hover {
  color: var(--color-coral-dark);
  text-decoration: underline;
}

.register-link {
  margin-left: 4px !important;
}

/* ========== 暗夜模式 ========== */
html.dark .login-view {
  background: var(--neutral-50);
}

html.dark .login-brand {
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.03) 0%, rgba(78, 205, 196, 0.03) 100%);
}

html.dark .brand-logo-wrapper {
  background: var(--neutral-100);
  box-shadow: 0 8px 40px rgba(255, 107, 107, 0.2);
}

html.dark .brand-subtitle {
  color: var(--neutral-500);
}

html.dark .feature-item {
  background: var(--neutral-100);
}

html.dark .feature-text strong {
  color: var(--neutral-800);
}

html.dark .login-form-section {
  background: var(--neutral-100);
  border-left-color: rgba(255, 107, 107, 0.15);
}

html.dark .login-card {
  background: var(--neutral-100);
  border-color: rgba(255, 107, 107, 0.15);
}

html.dark .card-header h2 {
  color: var(--neutral-800);
}

html.dark .input-wrapper {
  background: var(--neutral-200);
}

html.dark .input-wrapper:focus-within {
  background: var(--neutral-100);
}

/* ========== 响应式设计 ========== */
@media (max-width: 968px) {
  .login-view {
    flex-direction: column;
  }

  .login-brand {
    padding: 40px 24px;
    min-height: auto;
  }

  .brand-features {
    display: none;
  }

  .login-form-section {
    width: 100%;
    border-left: none;
    border-top: 1px solid var(--neutral-200);
    padding: 40px 24px;
  }
}

/* 移动端进一步优化 */
@media (max-width: 768px) {
  .login-view {
    min-height: 100vh;
    padding-bottom: calc(60px + env(safe-area-inset-bottom));
  }

  .login-brand {
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

  .login-form-section {
    padding: 24px 16px;
    flex: 1;
  }

  .form-container {
    max-width: 100%;
  }

  .login-card {
    border-radius: var(--radius-xl);
  }

  :deep(.el-card__header) {
    padding: 24px 20px 16px;
  }

  .header-icon {
    width: 52px;
    height: 52px;
    font-size: 24px;
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

  .login-button {
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
  stroke: white;
}

:deep(.el-loading-spinner .el-loading-text) {
  color: white;
}
</style>
