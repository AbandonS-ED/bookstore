import api from '@/api'

export const communityApi = {
  list() {
    return api.get('/community/posts')
  },

  add(data) {
    return api.post('/community/posts', data)
  },

  like(id) {
    return api.post(`/community/posts/${id}/like`)
  },

  unlike(id) {
    return api.delete(`/community/posts/${id}/like`)
  },

  delete(id) {
    return api.delete(`/community/posts/${id}`)
  }
}
