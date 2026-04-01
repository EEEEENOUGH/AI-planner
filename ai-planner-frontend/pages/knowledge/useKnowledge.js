import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getAiHistory } from '@/api/ai'

export function useKnowledge() {
  const subjects = ref(['高等数学', '英语', '政治', '专业课'])
  const currentSubject = ref(0)
  const inputText = ref('')
  const messages = ref([])
  const knowledgePoints = ref([
    { id: 1, number: 1, title: '极限的定义与性质', mastery: 85, color: 'linear-gradient(135deg, #9333EA, #4F46E5)' },
    { id: 2, number: 2, title: '导数与微分', mastery: 62, color: 'linear-gradient(135deg, #F97316, #DC2626)' },
    { id: 3, number: 3, title: '不定积分', mastery: 74, color: 'linear-gradient(135deg, #0EA5E9, #2563EB)' }
  ])

  // 加载最近2条真实AI历史消息
  const loadRecentMessages = async () => {
    try {
      const pageData = await getAiHistory(1, 1) // 取最新1条记录（含一问一答）
      const records = Array.isArray(pageData?.records) ? pageData.records : []
      if (records.length > 0) {
        const latest = records[0]
        const msgs = []
        if (latest.questionContent) msgs.push({ id: 1, type: 'user', content: latest.questionContent })
        if (latest.answerContent) msgs.push({ id: 2, type: 'ai', content: latest.answerContent })
        messages.value = msgs
      }
    } catch (e) {
      // 加载失败时保持空列表，不显示任何消息
      console.warn('加载知识页AI历史失败:', e)
    }
  }

  // 消息截断：超过 60 字显示省略号
  const truncate = (text, max = 60) => {
    if (!text) return ''
    return text.length > max ? text.slice(0, max) + '...' : text
  }

  // 每次页面显示时刷新（包括从 AI 页返回）
  onShow(() => {
    loadRecentMessages()
  })

  // 点击箭头或按下回车键时跳转到AI页，并携带输入内容
  const goToAi = () => {
    const msg = inputText.value.trim()
    if (msg) {
      uni.navigateTo({ url: `/pages/ai/ai?initMsg=${encodeURIComponent(msg)}` })
    } else {
      uni.navigateTo({ url: '/pages/ai/ai' })
    }
  }

  return {
    subjects,
    currentSubject,
    inputText,
    messages,
    knowledgePoints,
    truncate,
    goToAi
  }
}
