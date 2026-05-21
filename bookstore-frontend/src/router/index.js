import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/user/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/categories',
    name: 'CategoryBrowse',
    component: () => import('@/views/user/CategoryBrowse.vue')
  },
  {
    path: '/ranking',
    name: 'Ranking',
    component: () => import('@/views/user/Ranking.vue')
  },
  {
    path: '/new-arrivals',
    name: 'NewArrivals',
    component: () => import('@/views/user/NewArrivals.vue')
  },
  {
    path: '/books',
    name: 'Books',
    component: () => import('@/views/user/Books.vue')
  },
  {
    path: '/about',
    name: 'About',
    component: () => import('@/views/user/About.vue')
  },
  {
    path: '/book/:id',
    name: 'BookDetail',
    component: () => import('@/views/user/BookDetail.vue')
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/user/Cart.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/confirm',
    name: 'OrderConfirm',
    component: () => import('@/views/user/OrderConfirm.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/success',
    name: 'OrderSuccess',
    component: () => import('@/views/user/OrderSuccess.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/orders',
    name: 'UserOrders',
    component: () => import('@/views/user/Orders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/User.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/user/OrderDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/settings',
    name: 'UserSettings',
    component: () => import('@/views/user/Settings.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/reviews',
    name: 'UserReviews',
    component: () => import('@/views/user/Reviews.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user/addresses',
    name: 'UserAddresses',
    component: () => import('@/views/user/Addresses.vue'),
    meta: { requiresAuth: true }
  },
  // 管理端
  {
    path: '/admin',
    component: () => import('@/views/admin/Admin.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    redirect: '/admin/books',
    children: [
      {
        path: 'books',
        name: 'AdminBooks',
        component: () => import('@/views/admin/AdminBooks.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/AdminCategories.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/AdminOrders.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/AdminUsers.vue')
      },
      {
        path: 'reviews',
        name: 'AdminReviews',
        component: () => import('@/views/admin/AdminReviews.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  }
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const isLoggedIn = userStore.isLoggedIn

  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  if (to.meta.guest && isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  if (to.meta.requiresAdmin) {
    try {
      await userStore.getUserInfo()
      if (userStore.userInfo?.role !== 'admin') {
        next({ name: 'Home' })
        return
      }
    } catch {
      next({ name: 'Login' })
      return
    }
  }

  next()
})

export default router