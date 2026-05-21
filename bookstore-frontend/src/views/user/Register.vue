<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-brand">
        <div class="brand-content">
          <div class="brand-icon">
            <span class="icon-book">📖</span>
          </div>
          <h1 class="brand-title">书斋</h1>
          <p class="brand-subtitle">书斋</p>
          <p class="brand-desc">在快时代，做一家慢书店</p>
          <div class="brand-divider"></div>
          <div class="brand-features">
            <span>128,000+ 图书</span>
            <span class="dot">·</span>
            <span>56,000+ 读者</span>
            <span class="dot">·</span>
            <span>98.6% 好评</span>
          </div>
        </div>
        <div class="brand-pattern"></div>
        <div class="brand-light"></div>
      </div>

      <div class="auth-form-wrapper">
        <div class="auth-form-container">
          <div class="auth-header">
            <h2>注册账号</h2>
            <p>创建新账号，开始探索书海</p>
          </div>

          <form @submit.prevent="handleRegister" class="auth-form">
            <div class="form-item">
              <label>用户名</label>
              <input
                v-model="formData.username"
                type="text"
                placeholder="3-20位字母或数字"
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
                  placeholder="6-20位密码"
                  class="input"
                  :class="{ 'input-error': errors.password }"
                />
                <button type="button" class="toggle-pwd" @click="showPassword = !showPassword">
                  {{ showPassword ? '🙈' : '👁️' }}
                </button>
              </div>
              <span v-if="errors.password" class="error-msg">{{ errors.password }}</span>
            </div>

            <div class="form-item">
              <label>确认密码</label>
              <input
                v-model="formData.confirmPassword"
                type="password"
                placeholder="再次输入密码"
                class="input"
                :class="{ 'input-error': errors.confirmPassword }"
              />
              <span v-if="errors.confirmPassword" class="error-msg">{{ errors.confirmPassword }}</span>
            </div>

            <div class="form-item">
              <label>邮箱 <span class="optional">(选填)</span></label>
              <input
                v-model="formData.email"
                type="email"
                placeholder="用于找回密码"
                class="input"
                :class="{ 'input-error': errors.email }"
              />
              <span v-if="errors.email" class="error-msg">{{ errors.email }}</span>
            </div>

            <div class="form-item">
              <label>手机号 <span class="optional">(选填)</span></label>
              <input
                v-model="formData.phone"
                type="tel"
                placeholder="用于接收订单通知"
                class="input"
              />
            </div>

            <button type="submit" class="btn-submit" :disabled="loading">
              <span v-if="loading" class="loading-spinner"></span>
              <span v-else>注 册</span>
            </button>
          </form>

          <div class="auth-footer">
            <p>
              已有账号？
              <router-link to="/login" class="link">立即登录</router-link>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formData = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: ''
})

const errors = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const showPassword = ref(false)
const loading = ref(false)

const validate = () => {
  let valid = true
  Object.keys(errors).forEach(key => errors[key] = '')

  if (!formData.username.trim()) {
    errors.username = '请输入用户名'
    valid = false
  } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(formData.username)) {
    errors.username = '用户名3-20位字母、数字或下划线'
    valid = false
  }

  if (!formData.password) {
    errors.password = '请输入密码'
    valid = false
  } else if (formData.password.length < 6 || formData.password.length > 20) {
    errors.password = '密码6-20位'
    valid = false
  }

  if (formData.password !== formData.confirmPassword) {
    errors.confirmPassword = '两次密码输入不一致'
    valid = false
  }

  if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
    errors.email = '邮箱格式不正确'
    valid = false
  }

  return valid
}

const handleRegister = async () => {
  if (!validate()) return

  loading.value = true
  try {
    const { confirmPassword, ...registerData } = formData
    await userStore.register(registerData)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
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
  padding-top: calc(var(--space-10) + var(--header-height));
  margin-top: calc(-1 * var(--header-height));
}

.auth-container {
  width: 100%;
  max-width: 960px;
  display: flex;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: var(--shadow-deeper);
  background: var(--color-bg-card);
}

.auth-brand {
  flex: 1.1;
  background: linear-gradient(160deg, var(--color-primary-abyss) 0%, var(--color-primary-dark) 40%, var(--color-primary-mid) 100%);
  color: var(--color-bg-warm);
  padding: var(--space-12) var(--space-10);
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  animation: fadeInUp 0.6s ease;
}

.icon-book {
  font-size: 40px;
  display: inline-block;
  margin-bottom: var(--space-4);
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.brand-title {
  font-family: var(--font-display);
  font-size: var(--text-5xl);
  font-weight: 900;
  letter-spacing: 6px;
  color: var(--color-bg-warm);
  margin-bottom: var(--space-2);
}

.brand-subtitle {
  font-family: var(--font-accent);
  font-size: var(--text-xs);
  letter-spacing: 5px;
  color: rgba(237, 230, 214, 0.35);
  margin-bottom: var(--space-5);
  text-transform: uppercase;
}

.brand-desc {
  font-size: var(--text-lg);
  color: rgba(237, 230, 214, 0.55);
  font-style: italic;
  font-family: var(--font-display);
}

.brand-divider {
  width: 40px;
  height: 2px;
  background: var(--color-accent);
  margin: var(--space-6) 0;
  opacity: 0.6;
  border-radius: 1px;
}

.brand-features {
  font-size: var(--text-sm);
  color: rgba(237, 230, 214, 0.4);
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.dot {
  opacity: 0.5;
}

.brand-pattern {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 80%, rgba(192, 154, 75, 0.08) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 20%, rgba(192, 154, 75, 0.04) 0%, transparent 50%);
  z-index: 1;
}

.brand-light {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(ellipse at 30% 60%, rgba(192, 154, 75, 0.03) 0%, transparent 50%);
  z-index: 0;
  animation: glow 8s ease-in-out infinite alternate;
}

@keyframes glow {
  0% { transform: translate(0, 0); }
  100% { transform: translate(-10%, 5%); }
}

.auth-form-wrapper {
  flex: 1;
  padding: var(--space-10) var(--space-10);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-card);
}

.auth-form-container {
  width: 100%;
  max-width: 340px;
  animation: fadeIn 0.5s ease;
}

.auth-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.auth-header h2 {
  font-family: var(--font-display);
  font-size: var(--text-3xl);
  font-weight: 700;
  color: var(--color-text);
  margin-bottom: var(--space-2);
}

.auth-header p {
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
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

.optional {
  font-weight: 400;
  color: var(--color-text-muted);
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
  padding: 4px;
  transition: opacity var(--transition-fast);
  line-height: 1;
}

.toggle-pwd:hover { opacity: 1; }

.error-msg {
  font-size: var(--text-xs);
  color: var(--color-accent-muted);
}

.btn-submit {
  width: 100%;
  padding: var(--space-4);
  background: linear-gradient(135deg, var(--color-accent) 0%, var(--color-accent-muted) 100%);
  color: var(--color-primary-abyss);
  font-size: var(--text-base);
  font-weight: 700;
  font-family: var(--font-body);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  box-shadow: 0 2px 12px rgba(192, 154, 75, 0.2);
  letter-spacing: 0.08em;
  margin-top: var(--space-2);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(192, 154, 75, 0.3);
  letter-spacing: 0.12em;
}

.btn-submit:active:not(:disabled) { transform: scale(0.98); }

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid rgba(46, 31, 21, 0.2);
  border-top-color: var(--color-primary-abyss);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.auth-footer {
  text-align: center;
  margin-top: var(--space-5);
  font-size: var(--text-sm);
  color: var(--color-text-muted);
}

.link {
  color: var(--color-accent-muted);
  font-weight: 500;
  transition: color var(--transition-fast);
}

.link:hover {
  color: var(--color-accent);
  text-decoration: underline;
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@media (max-width: 768px) {
  .auth-container { flex-direction: column; max-width: 440px; }
  .auth-brand { padding: var(--space-8); }
  .brand-title { font-size: var(--text-4xl); }
  .brand-features { font-size: var(--text-xs); }
  .auth-form-wrapper { padding: var(--space-8); }
}
</style>
