import api from '@/api'

export const reviewApi = {
  getStats() {
    return api.get('/review/stats')
  },

  getBookReviews(bookId) {
    return api.get(`/review/book/${bookId}`)
  },

  getMyReviews() {
    return api.get('/review/my')
  },

  add(data) {
    return api.post('/review/add', data)
  },

  delete(id) {
    return api.delete(`/review/${id}`)
  }
}