import axios from 'axios'
import { ElMessage } from 'element-plus'

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
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
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

  // 库存管理
  getInventoryList(params) {
    return request.get('/book/list', { params })
  },
  updateBookStock(id, stock) {
    return request.put(`/book/${id}/stock`, { stock })
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
  updateCategoryStatus(id, status) {
    return request.put(`/category/${id}/status`, null, { params: { status } })
  },

  // 订单管理
  getOrderList(params) {
    return request.get('/order/list', { params })
  },
  getRevenueStats(params) {
    return request.get('/order/stats/revenue', { params })
  },
  shipOrder(id, expressNo) {
    return request.put(`/order/${id}/ship`, { expressNo })
  },
  deliverOrder(id) {
    return request.put(`/order/${id}/deliver`)
  },
  refundOrder(id) {
    return request.put(`/order/${id}/refund`)
  },
  approveRefund(id) {
    return request.put(`/order/${id}/approve-refund`)
  },
  rejectRefund(id) {
    return request.put(`/order/${id}/reject-refund`)
  },
  approveAfterSale(id) {
    return request.put(`/order/${id}/approve-after-sale`)
  },
  rejectAfterSale(id) {
    return request.put(`/order/${id}/reject-after-sale`)
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
  updateUserRole(id, role) {
    return request.put(`/user/${id}/role`, { role })
  },
  deleteUser(id) {
    return request.delete(`/user/${id}`)
  },

  // 社区管理
  getCommunityList(params) {
    return request.get('/community/list', { params })
  },
  updateCommunity(data) {
    return request.put('/community/update', data)
  },
  deleteCommunity(id) {
    return request.delete(`/community/${id}`)
  },

  // 评价管理
  getReviewList(params) {
    return request.get('/review/list', { params })
  },
  deleteReview(id) {
    return request.delete(`/review/${id}`)
  },
  hideReview(id) {
    return request.put(`/review/${id}/hide`)
  },
  showReview(id) {
    return request.put(`/review/${id}/show`)
  }
}

export default request