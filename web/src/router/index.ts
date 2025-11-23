import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/login',
            component: () => import('@/views/auth/Login.vue')
        },
        {
            path: '/dashboard',
            component: () => import('@/views/dashboard/Dashboard.vue')
        },
        {
            path: '/materials',
            component: () => import('@/views/materials/Materials.vue')
        },
        {
            path: '/mix/recipes',
            component: () => import('@/views/mix/MixRecipes.vue')
        },
        {
            path: '/tasks',
            component: () => import('@/views/tasks/Tasks.vue')
        },
        {
            path: '/',
            redirect: '/dashboard'
        }
    ]
})

router.beforeEach((to, _from, next) => {
    const userStore = useUserStore()
    if (to.path !== '/login' && !userStore.token) {
        next('/login')
    } else {
        next()
    }
})

export default router
