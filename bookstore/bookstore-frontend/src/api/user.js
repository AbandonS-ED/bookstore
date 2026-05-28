import api from './index'

export const userApi = {
  login(data) {
    return api.post('/user/login', data)
  },
  register(data) {
    return api.post('/user/register', data)
  },
  getUserInfo() {
    return api.get('/user/info')
  },
  updatePassword(data) {
    return api.put('/user/password', data)
  },
  updateProfile(data) {
    return api.put('/user/profile', data)
  }
}