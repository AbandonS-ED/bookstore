import api from '@/api'

export const communityApi = {
  list() {
    return api.get('/community/list')
  },

  add(data) {
    return api.post('/community/add', data)
  },

  like(id) {
    return api.post(`/community/like/${id}`)
  },

  delete(id) {
    return api.delete(`/community/${id}`)
  }
}
