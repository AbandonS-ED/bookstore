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
  }
}

export default request