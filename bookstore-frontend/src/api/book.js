import api from './index'

export const bookApi = {
  getList(params) {
    return api.get('/book/list', { params })
  },
  getDetail(id) {
    return api.get(`/book/${id}`)
  },
  getByCategory(categoryId, params) {
    return api.get(`/book/category/${categoryId}`, { params })
  },
  getRanking(params) {
    return api.get('/book/ranking', { params })
  },
  getComingSoon() {
    return api.get('/book/coming-soon')
  }
}