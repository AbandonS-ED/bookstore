import { defineStore } from 'pinia'
import { reviewApi } from '@/api/review'

export const useReviewStore = defineStore('review', {
  state: () => ({
    reviews: []
  }),

  actions: {
    async getBookReviews(bookId) {
      const res = await reviewApi.getBookReviews(bookId)
      this.reviews = res.data || []
      return this.reviews
    },

    async addReview(reviewData) {
      return await reviewApi.add(reviewData)
    },

    async deleteReview(id) {
      await reviewApi.delete(id)
      this.reviews = this.reviews.filter(r => r.id !== id)
    }
  }
})