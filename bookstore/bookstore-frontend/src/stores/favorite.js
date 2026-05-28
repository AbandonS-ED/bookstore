import { defineStore } from 'pinia'
import { favoriteApi } from '@/api/favorite'
import { useUserStore } from '@/stores/user'

export const useFavoriteStore = defineStore('favorite', {
  state: () => ({
    favoriteIds: []
  }),

  getters: {
    isFavorited: (state) => {
      return (bookId) => state.favoriteIds.includes(String(bookId))
    },
    totalCount: (state) => state.favoriteIds.length
  },

  actions: {
    async fetchFavoriteIds() {
      const userStore = useUserStore()
      if (!userStore.isLoggedIn) {
        this.favoriteIds = []
        return
      }
      try {
        const res = await favoriteApi.ids()
        this.favoriteIds = res.data || []
      } catch {
        this.favoriteIds = []
      }
    },

    async toggleFavorite(bookId) {
      const id = String(bookId)
      if (this.favoriteIds.includes(id)) {
        await this.removeFavorite(id)
      } else {
        await this.addFavorite(id)
      }
    },

    async addFavorite(bookId) {
      await favoriteApi.add(bookId)
      if (!this.favoriteIds.includes(String(bookId))) {
        this.favoriteIds.push(String(bookId))
      }
    },

    async removeFavorite(bookId) {
      await favoriteApi.remove(bookId)
      this.favoriteIds = this.favoriteIds.filter(id => id !== String(bookId))
    }
  }
})
