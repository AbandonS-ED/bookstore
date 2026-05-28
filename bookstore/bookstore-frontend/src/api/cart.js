import api from './index'

export const cartApi = {
  getList() {
    return api.get('/cart/list')
  },
  add(data) {
    return api.post('/cart/add', data)
  },
  update(data) {
    return api.put('/cart/update', data)
  },
  remove(id) {
    return api.delete(`/cart/${id}`)
  },
  clear() {
    return api.delete('/cart/clear')
  }
}