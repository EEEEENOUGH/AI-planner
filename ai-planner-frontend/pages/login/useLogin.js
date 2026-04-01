import { ref } from 'vue'
import { useUserStore } from '@/store/user'

export function useLogin() {
  const userStore = useUserStore()

  const isRegisterMode  = ref(false)
  const loading         = ref(false)
  const loginName       = ref('')
  const password        = ref('')
  const confirmPassword = ref('')
  const nickname        = ref('')

  const switchToLogin    = () => { isRegisterMode.value = false; password.value = ''; confirmPassword.value = '' }
  const switchToRegister = () => { isRegisterMode.value = true;  password.value = ''; confirmPassword.value = '' }

  const validate = () => {
    if (!loginName.value.trim())  { uni.showToast({ title: '账号不能为空', icon: 'none' }); return false }
    if (!password.value.trim())   { uni.showToast({ title: '密码不能为空', icon: 'none' }); return false }
    if (isRegisterMode.value) {
      if (!confirmPassword.value.trim()) { uni.showToast({ title: '确认密码不能为空', icon: 'none' }); return false }
      if (password.value !== confirmPassword.value) { uni.showToast({ title: '两次密码输入不一致', icon: 'none' }); return false }
    }
    return true
  }

  const submit = async () => {
    if (!validate() || loading.value) return
    loading.value = true
    try {
      const success = isRegisterMode.value
        ? await userStore.registerAction(loginName.value.trim(), password.value, nickname.value.trim())
        : await userStore.loginAction(loginName.value.trim(), password.value)
      if (success) {
        uni.showToast({ title: isRegisterMode.value ? '注册成功' : '登录成功', icon: 'success' })
        setTimeout(() => { uni.switchTab({ url: '/pages/index/index' }) }, 900)
      }
    } catch (e) {
      if (e?.msg) return
      uni.showToast({ title: '操作失败，请稍后重试', icon: 'none' })
    } finally {
      loading.value = false
    }
  }

  return { isRegisterMode, loading, loginName, password, confirmPassword, nickname, switchToLogin, switchToRegister, submit }
}
