import api from './index'

export const categoryApi = {
  getList() {
    return api.get('/category/list')
  },
  getTree(parentId) {
    return api.get('/category/tree', { params: { parentId } })
  }
}