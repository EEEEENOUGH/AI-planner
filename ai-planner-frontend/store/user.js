import { defineStore } from 'pinia'
import { login, register, logout, getMyProfile } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => {
    const token = uni.getStorageSync('token') || ''
    const userInfo = uni.getStorageSync('userInfo') || null
    const storedLogin = uni.getStorageSync('isLogin')
    const isLogin = typeof storedLogin === 'boolean' ? storedLogin : !!token
    return {
      token,
      userInfo,
      isLogin
    }
  },
  actions: {
    // ── 内部：从接口响应中提取 token ──
    _extractToken(respData = {}) {
      const data = Object.prototype.hasOwnProperty.call(respData, 'data') &&
        Object.prototype.hasOwnProperty.call(respData, 'code')
        ? (respData.data || {})
        : (respData || {})
      return data.token || ''
    },

    // ── Token 持久化 ──
    setToken(token) {
      this.token = token || ''
      this.isLogin = !!this.token
      uni.setStorageSync('token', this.token)
      uni.setStorageSync('isLogin', this.isLogin)
    },

    // ── 用户信息持久化 ──
    setUserInfo(userInfo) {
      this.userInfo = userInfo || null
      uni.setStorageSync('userInfo', this.userInfo)
    },

    // ── 清除登录态 ──
    clearAuth() {
      this.token = ''
      this.userInfo = null
      this.isLogin = false
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.setStorageSync('isLogin', false)
    },

    // ── 同步本地存储到 store ──
    syncFromStorage() {
      this.token = uni.getStorageSync('token') || ''
      this.userInfo = uni.getStorageSync('userInfo') || null
      this.isLogin = !!this.token
    },

    // ── 拉取最新用户资料 ──
    async fetchMyProfile() {
      const profile = await getMyProfile()
      this.setUserInfo(profile || null)
      return profile
    },

    // ── 登录 ──
    async loginAction(loginName, password) {
      const respData = await login(loginName, password)
      const token = this._extractToken(respData)
      if (!token) throw new Error('登录失败：未获取到 token')
      this.setToken(token)
      await this.fetchMyProfile()
      return true
    },

    // ── 注册 ──
    async registerAction(loginName, password, nickname) {
      const respData = await register(loginName, password, nickname)
      const token = this._extractToken(respData)
      if (!token) throw new Error('注册失败：未获取到 token')
      this.setToken(token)
      await this.fetchMyProfile()
      return true
    },

    // ── 退出 ──
    async logoutAction() {
      try {
        await logout()
      } finally {
        this.clearAuth()
      }
    }
  }
})
