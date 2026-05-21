import axios from 'axios'

const request = axios.create({
  baseURL: '/admin-api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const adminApi = {
  // 书籍管理
  getBookList(params) {
    return request.get('/book/list', { params })
  },
  addBook(data) {
    return request.post('/book/add', data)
  },
  updateBook(data) {
    return request.put('/book/update', data)
  },
  deleteBook(id) {
    return request.delete(`/book/${id}`)
  },
  updateBookStatus(id, status) {
    return request.put(`/book/${id}/status`, null, { params: { status } })
  },

  // 分类管理
  addCategory(data) {
    return request.post('/category/add', data)
  },
  updateCategory(data) {
    return request.put('/category/update', data)
  },
  deleteCategory(id) {
    return request.delete(`/category/${id}`)
  },

  // 订单管理
  getOrderList(params) {
    return request.get('/order/list', { params })
  },
  shipOrder(id, expressNo) {
    return request.put(`/order/${id}/ship`, { expressNo })
  },
  deliverOrder(id) {
    return request.put(`/order/${id}/deliver`)
  },

  // 用户管理
  getUserList(params) {
    return request.get('/user/list', { params })
  },
  disableUser(id) {
    return request.put(`/user/${id}/disable`)
  },
  enableUser(id) {
    return request.put(`/user/${id}/enable`)
  },

  // 评论管理
  getReviewList(params) {
    return request.get('/review/list', { params })
  },
  deleteReview(id) {
    return request.delete(`/review/${id}`)
  },
  hideReview(id) {
    return request.put(`/review/${id}/hide`)
  }
}

export default request