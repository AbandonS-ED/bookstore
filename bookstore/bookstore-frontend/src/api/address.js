import api from '@/api'

export const addressApi = {
  getList() {
    return api.get('/address/list')
  },

  add(data) {
    return api.post('/address/add', data)
  },

  update(data) {
    return api.put('/address/update', data)
  },

  delete(id) {
    return api.delete(`/address/${id}`)
  },

  setDefault(id) {
    return api.put(`/address/${id}/default`)
  }
}