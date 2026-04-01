import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/user'
import { getMyStats } from '@/api/user'

export function useMy() {
  const userStore = useUserStore()

  const nickname          = computed(() => userStore.userInfo?.nickname          || '未设置昵称')
  const targetInstitution = computed(() => userStore.userInfo?.targetInstitution || '目标院校待设置')
  const avatarUrl         = computed(() => userStore.userInfo?.avatarUrl          || '/static/头像.jpg')

  const checkinCount = ref('--')
  const totalHours   = ref('--')
  const doneTasks    = ref('--')

  const loadStats = async () => {
    try {
      const stats = await getMyStats()
      if (stats) {
        checkinCount.value = stats.checkinDays ?? '--'
        totalHours.value   = stats.totalHours  ?? '--'
        doneTasks.value    = stats.doneTasks   ?? '--'
      }
    } catch (e) {
      console.warn('[my] loadStats failed', e)
    }
  }

  onShow(async () => {
    if (!userStore.token) return
    try { await userStore.fetchMyProfile() } catch (e) { console.warn('[my] fetchMyProfile failed', e) }
    loadStats()
  })

  const goProfile = () => uni.navigateTo({ url: '/pages/profile/profile' })
  const goArchive = () => uni.navigateTo({ url: '/pages/archive/archive' })
  const logout    = async () => {
    await userStore.logoutAction()
    uni.redirectTo({ url: '/pages/login/login' })
  }

  return { nickname, targetInstitution, avatarUrl, checkinCount, totalHours, doneTasks, goProfile, goArchive, logout }
}
