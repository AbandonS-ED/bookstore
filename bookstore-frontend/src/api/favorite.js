import api from './index'

export const favoriteApi = {
  add(bookId) {
    return api.post('/favorite/add', { bookId })
  },
  remove(bookId) {
    return api.delete(`/favorite/${bookId}`)
  },
  list(params) {
    return api.get('/favorite/list', { params })
  },
  ids() {
    return api.get('/favorite/ids')
  },
  check(bookId) {
    return api.get(`/favorite/check/${bookId}`)
  }
}
