export const formatPrice = (price) => {
  if (price == null) return '¥0.00'
  return `¥${parseFloat(price).toFixed(2)}`
}

export const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
}

export const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const d = new Date(dateTime)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export const formatOrderStatus = (status) => {
  const statusMap = {
    created: '待付款',
    paying: '支付中',
    paid: '待发货',
    shipped: '待收货',
    delivered: '已收货',
    completed: '已完成',
    cancelled: '已取消',
    refunding: '退款审核',
    after_sale: '售后处理',
    expired: '已过期',
    refunded: '已退款'
  }
  return statusMap[status] || status
}

export const formatPayStatus = (status) => {
  const statusMap = {
    unpaid: '未支付',
    paid: '已支付'
  }
  return statusMap[status] || status
}