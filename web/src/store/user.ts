import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const roles = ref<string[]>(JSON.parse(localStorage.getItem('roles') || '[]'))
    const info = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

    function setToken(newToken: string) {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    function setRoles(newRoles: string[]) {
        roles.value = newRoles
        localStorage.setItem('roles', JSON.stringify(newRoles))
    }

    function setInfo(newInfo: any) {
        info.value = newInfo
        localStorage.setItem('userInfo', JSON.stringify(newInfo))
    }

    function logout() {
        token.value = ''
        roles.value = []
        info.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('roles')
        localStorage.removeItem('userInfo')
    }

    return { token, roles, info, setToken, setRoles, setInfo, logout }
})
