import { defineStore } from 'pinia'
import { cartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),

  getters: {
    totalAmount: (state) => {
      return state.items.reduce((sum, item) => {
        return sum + parseFloat(item.price) * item.quantity
      }, 0)
    },
    totalCount: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    }
  },

  actions: {
    async getCartList() {
      this.loading = true
      try {
        const res = await cartApi.getList()
        this.items = res.data || []
      } finally {
        this.loading = false
      }
    },

    async addToCart(bookId, quantity = 1) {
      await cartApi.add({ bookId, quantity })
      await this.getCartList()
    },

    async updateQuantity(bookId, quantity) {
      const item = this.items.find(i => i.bookId === bookId)
      if (!item) return
      await cartApi.update({ id: item.id, quantity })
      item.quantity = quantity
    },

    async removeItem(id) {
      await cartApi.remove(id)
      this.items = this.items.filter(i => i.id !== id)
    },

    async clearCart() {
      await cartApi.clear()
      this.items = []
    }
  }
})