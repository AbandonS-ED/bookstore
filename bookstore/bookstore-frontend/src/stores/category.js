import { defineStore } from 'pinia'
import { categoryApi } from '@/api/category'

export const useCategoryStore = defineStore('category', {
  state: () => ({
    categories: [],
    selectedCategoryId: null
  }),

  actions: {
    async getCategories() {
      const res = await categoryApi.getList()
      this.categories = res.data || []
    },

    selectCategory(id) {
      this.selectedCategoryId = id
    },

    clearSelection() {
      this.selectedCategoryId = null
    }
  }
})