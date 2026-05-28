import { defineStore } from 'pinia'
import { userApi } from '@/api/user'
import { setToken, removeToken, setUserInfo, getUsername, isAuthenticated } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: getUsername() || '',
    userInfo: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 'admin'
  },

  actions: {
    async login(loginData) {
      const res = await userApi.login(loginData)
      this.token = res.data.token
      this.username = res.data.username
      setToken(res.data.token)
      setUserInfo(res.data.username)
      return res
    },

    async register(registerData) {
      return await userApi.register(registerData)
    },

    async getUserInfo() {
      if (!this.token) return null
      try {
        const res = await userApi.getUserInfo()
        this.userInfo = res.data
        return res.data
      } catch (error) {
        this.logout()
        throw error
      }
    },

    async updatePassword(passwordData) {
      return await userApi.updatePassword(passwordData)
    },

    async updateProfile(profileData) {
      return await userApi.updateProfile(profileData)
    },

    logout() {
      this.token = ''
      this.username = ''
      this.userInfo = null
      removeToken()
    }
  }
})