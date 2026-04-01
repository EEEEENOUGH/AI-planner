import request from '@/utils/request'

/**
 * 登录
 * @param {string} loginName
 * @param {string} password
 */
export function login(loginName, password) {
  return request({
    url: '/auth/login',
    method: 'POST',
    data: { loginName, password }
  })
}

/**
 * 注册
 * @param {string} loginName
 * @param {string} password
 * @param {string} nickname
 */
export function register(loginName, password, nickname) {
  return request({
    url: '/auth/register',
    method: 'POST',
    data: { loginName, password, nickname }
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'POST'
  })
}

/**
 * 获取当前用户资料
 */
export function getMyProfile() {
  return request({
    url: '/user/me',
    method: 'GET'
  })
}

/**
 * 修改用户资料
 * @param {{ nickname?, phone?, gender? }} data
 */
export function updateMyProfile(data) {
  return request({
    url: '/user/profile',
    method: 'PUT',
    data
  })
}

/**
 * 获取用户学习统计数据（打卡天数、完成任务数、累计时长）
 */
export function getMyStats() {
  return request({
    url: '/user/stats',
    method: 'GET'
  })
}
