import { defineStore } from 'pinia'
import { orderApi } from '@/api/order'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [],
    currentOrder: null,
    loading: false
  }),

  actions: {
    async createOrder(orderData) {
      const res = await orderApi.create(orderData)
      return res.data
    },

    async getOrderList(params) {
      this.loading = true
      try {
        const res = await orderApi.getList(params)
        this.orders = res.data || []
        return res.data
      } finally {
        this.loading = false
      }
    },

    async getOrderDetail(id) {
      const res = await orderApi.getDetail(id)
      this.currentOrder = res.data
      return res.data
    },

    async payOrder(id) {
      await orderApi.pay(id)
      const order = this.orders.find(o => o.id === id)
      if (order) {
        order.payStatus = 'paid'
      }
    },

    async cancelOrder(id) {
      await orderApi.cancel(id)
      const order = this.orders.find(o => o.id === id)
      if (order) {
        order.status = 'cancelled'
      }
    },

    async confirmOrder(id) {
      await orderApi.confirm(id)
      const order = this.orders.find(o => o.id === id)
      if (order) {
        order.status = 'delivered'
      }
    },

    async payApplyOrder(id, paymentMethod) {
      const res = await orderApi.payApply(id, paymentMethod)
      return res.data
    }
  }
})