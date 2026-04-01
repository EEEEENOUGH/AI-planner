export const baseURL = 'http://localhost:8080/api/v1'

function isObject(val) {
  return val !== null && typeof val === 'object'
}

function getPayload(data) {
  if (!isObject(data)) return null
  if (!('code' in data)) return null
  return data
}

export function request(options) {
  const {
    url,
    method = 'GET',
    data = {},
    header = {},
    timeout = 120000
  } = options || {}

  if (!url) {
    return Promise.reject(new Error('request: url is required'))
  }

  const token = uni.getStorageSync('token') || ''

  return new Promise((resolve, reject) => {
    uni.request({
      url: `${baseURL}${url}`,
      method,
      data,
      header: {
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header
      },
      timeout,
      success: (res) => {
        const payload = getPayload(res.data)
        if (payload) {
          // debug：定位登录/注册不跳转问题的关键
          console.log('[request] url=', url, 'payload=', payload)

          if (payload.code === 200 || payload.code === 0) {
            resolve(payload.data)
            return
          }

          if (payload.code === 401) {
            console.warn('[request] auth expired, payload=', payload)
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
            uni.setStorageSync('isLogin', false)
            uni.reLaunch({ url: '/pages/login/login' })
            reject(payload)
            return
          }

          console.warn('[request] request failed, payload=', payload)
          uni.showToast({
            title: payload.msg || '请求失败',
            icon: 'none'
          })
          reject(payload)
          return
        }
        resolve(res.data)
      },
      fail: (err) => {
        console.error('[request] network fail, url=', url, 'err=', err)
        uni.showToast({
          title: '网络错误',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default request

