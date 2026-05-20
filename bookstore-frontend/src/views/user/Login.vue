<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-icon">📖</div>
          <h1 class="brand-title">书斋</h1>
          <p class="brand-subtitle">SHUZHAI BOOKS</p>
          <p class="brand-desc">用阅读点亮时光</p>
        </div>
        <div class="brand-pattern"></div>
      </div>

      <div class="auth-form-wrapper">
        <div class="auth-form-container">
          <div class="auth-header">
            <h2>欢迎回来</h2>
            <p>请输入您的账号信息</p>
          </div>

          <form @submit.prevent="handleLogin" class="auth-form">
            <div class="form-item">
              <label>用户名</label>
              <input
                v-model="formData.username"
                type="text"
                placeholder="请输入用户名"
                class="input"
                :class="{ 'input-error': errors.username }"
              />
              <span v-if="errors.username" class="error-msg">{{ errors.username }}</span>
            </div>

            <div class="form-item">
              <label>密码</label>
              <div class="password-input">
                <input
                  v-model="formData.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  class="input"
                  :class="{ 'input-error': errors.password }"
                />
                <button type="button" class="toggle-pwd" @click="showPassword = !showPassword">
                  {{ showPassword ? '🙈' : '👁️' }}
                </button>
              </div>
              <span v-if="errors.password" class="error-msg">{{ errors.password }}</span>
            </div>

            <div class="form-options">
              <label class="remember-me">
                <input type="checkbox" v-model="rememberMe" />
                <span>记住我</span>
              </label>
              <a href="#" class="forgot-pwd">忘记密码？</a>
            </div>

            <button type="submit" class="btn-submit" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>登 录</span>
            </button>
          </form>

          <div class="auth-footer">
            <p>
              还没有账号？
              <router-link to="/register" class="link">立即注册</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formData = reactive({ username: '', password: '' })
const errors = reactive({ username: '', password: '' })
const showPassword = ref(false)
const rememberMe = ref(false)
const loading = ref(false)

const validate = () => {
  let valid = true
  errors.username = ''
  errors.password = ''
  if (!formData.username.trim()) { errors.username = '请输入用户名'; valid = false }
  if (!formData.password) { errors.password = '请输入密码'; valid = false }
  return valid
}

const handleLogin = async () => {
  if (!validate()) return
  loading.value = true
  try {
    await userStore.login(formData)
    await userStore.getUserInfo()
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - var(--header-height));
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg);
  padding: var(--space-6);
  margin-top: calc(-1 * var(--header-height));
}

.auth-container {
  width: 100%;
  max-width: 900px;
  display: flex;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-xl);
  background: var(--color-bg-card);
}

.auth-brand {
  flex: 1;
  background: linear-gradient(135deg, var(--color-primary-abyss) 0%, var(--color-primary-dark) 100%);
  color: var(--color-bg-warm);
  padding: var(--space-12);
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  animation: fadeInUp 0.6s ease;
}

.brand-icon {
  font-size: 48px;
  margin-bottom: var(--space-4);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.brand-title {
  font-family: var(--font-display);
  font-size: var(--text-5xl);
  font-weight: 900;
  letter-spacing: 8px;
  color: var(--color-bg-warm);
  margin-bottom: var(--space-2);
}

.brand-subtitle {
  font-size: var(--text-xs);
  letter-spacing: 4px;
  color: rgba(237,230,214,0.35);
  margin-bottom: var(--space-4);
}

.brand-desc {
  font-size: var(--text-lg);
  color: rgba(237,230,214,0.6);
  font-style: italic;
}

.brand-pattern {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 30% 70%, rgba(192,154,75,0.08) 0%, transparent 60%),
    radial-gradient(ellipse at 70% 30%, rgba(192,154,75,0.05) 0%, transparent 50%);
}

.auth-form-wrapper {
  flex: 1;
  padding: var(--space-12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-form-container {
  width: 100%;
  max-width: 320px;
  animation: fadeIn 0.5s ease;
}

.auth-header {
  text-align: center;
  margin-bottom: var(--space-8);
}

.auth-header h2 {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  color: var(--color-text);
  margin-bottom: var(--space-2);
}

.auth-header p {
  font-size: var(--text-sm);
  color: var(--color-text-light);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.form-item label {
  font-size: var(--text-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
}

.password-input {
  position: relative;
}

.password-input .input {
  padding-right: 48px;
}

.toggle-pwd {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  font-size: 18px;
  opacity: 0.6;
  cursor: pointer;
  transition: opacity var(--transition-fast);
}

.toggle-pwd:hover { opacity: 1; }

.error-msg {
  font-size: var(--text-xs);
  color: var(--color-accent);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: var(--text-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.remember-me input { accent-color: var(--color-accent); }

.forgot-pwd {
  font-size: var(--text-sm);
  color: var(--color-accent-muted);
}

.forgot-pwd:hover { text-decoration: underline; }

.btn-submit {
  width: 100%;
  padding: var(--space-4);
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%);
  color: var(--color-primary-abyss);
  font-size: var(--text-base);
  font-weight: 700;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  box-shadow: 0 2px 12px rgba(192,154,75,0.2);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(192,154,75,0.3);
  letter-spacing: 0.04em;
}

.btn-submit:active:not(:disabled) { transform: scale(0.98); }

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(46,31,21,0.2);
  border-top-color: var(--color-primary-abyss);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.auth-footer {
  text-align: center;
  margin-top: var(--space-6);
  font-size: var(--text-sm);
  color: var(--color-text-light);
}

.link {
  color: var(--color-accent-muted);
  font-weight: 500;
}

.link:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .auth-container { flex-direction: column; max-width: 420px; }
  .auth-brand { padding: var(--space-8); }
  .brand-title { font-size: var(--text-4xl); }
  .auth-form-wrapper { padding: var(--space-8); }
}
</style>
