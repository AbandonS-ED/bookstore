<template>
  <div class="settings-page">
    <div class="page-container">
      <div class="page-header">
        <h1>账户设置</h1>
      </div>

      <div class="settings-card">
        <div class="settings-section">
          <h2>个人信息</h2>
          <el-form :model="profileForm" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <div class="settings-section">
          <h2>修改密码</h2>
          <el-form :model="passwordForm" :rules="passwordRules" label-width="100px" class="password-form">
            <el-form-item label="当前密码" prop="currentPassword">
              <el-input v-model="passwordForm.currentPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { userApi } from '@/api/user'

const userStore = useUserStore()

const profileForm = ref({
  username: '',
  email: '',
  phone: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileLoading = ref(false)
const passwordLoading = ref(false)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const fetchUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo()
    const user = res.data
    profileForm.value = {
      username: user.username || '',
      email: user.email || '',
      phone: user.phone || ''
    }
  } catch (error) {
    console.error('Failed to fetch user info:', error)
  }
}

const handleUpdateProfile = async () => {
  profileLoading.value = true
  try {
    await userApi.updateProfile(profileForm.value)
    ElMessage.success('个人信息更新成功')
    if (userStore.userInfo) {
      userStore.userInfo.username = profileForm.value.username
    }
  } catch (error) {
    ElMessage.error('更新失败')
  } finally {
    profileLoading.value = false
  }
}

const handleChangePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  passwordLoading.value = true
  try {
    await userApi.updatePassword({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.value = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    }
  } catch (error) {
    ElMessage.error('密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.settings-page {
  padding: var(--space-8) 0 var(--space-16);
}

.page-header {
  margin-bottom: var(--space-8);
}

.page-header h1 {
  font-family: var(--font-display);
  font-size: var(--text-2xl);
  color: var(--color-text);
}

.settings-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  max-width: 600px;
}

.settings-section {
  padding: var(--space-8);
  border-bottom: 1px solid var(--color-divider);
}

.settings-section:last-child {
  border-bottom: none;
}

.settings-section h2 {
  font-family: var(--font-display);
  font-size: var(--text-lg);
  color: var(--color-text);
  margin-bottom: var(--space-6);
}

.profile-form,
.password-form {
  max-width: 400px;
}

@media (max-width: 768px) {
  .profile-form,
  .password-form {
    max-width: 100%;
  }
}
</style>