import api from './index'

export const communityApi = {
  list(keyword) {
    return api.get('/community/list', { params: { keyword } })
  },
  getById(id) {
    return api.get(`/community/${id}`)
  },
  add(data) {
    return api.post('/community/add', data)
  },
  update(data) {
    return api.put('/community/update', data)
  },
  delete(id) {
    return api.delete(`/community/${id}`)
  },
  toggleLike(id) {
    return api.post(`/community/like/${id}`)
  }
}