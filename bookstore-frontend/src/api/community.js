import api from '@/api'

export const communityApi = {
  list(keyword) {
    return api.get('/community/list', { params: { keyword } })
  },

  add(data) {
    return api.post('/community/add', data)
  },

  update(data) {
    return api.put('/community/update', data)
  },

  like(id) {
    return api.post(`/community/like/${id}`)
  },

  delete(id) {
    return api.delete(`/community/${id}`)
  }
}
