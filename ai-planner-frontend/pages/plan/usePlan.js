import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { generatePlan, getCurrentPlan, getStageTaskList, updateTaskStatus } from '@/api/plan'

export function usePlan() {
  const plan = ref(null)
  const loading = ref(false)
  const generating = ref(false)
  const isDark = ref(true)

  const currentPhase = ref(0)

  const currentStage = computed(() => {
    if (!plan.value?.stages?.length) return null
    return plan.value.stages[currentPhase.value] || null
  })

  const selectPhase = (index) => {
    if (currentPhase.value === index) return
    currentPhase.value = index
    loadDailyTasks()
  }

  // ── 无计划时：本周计划占位数据 ──
  const weekRange = ref('')
  const schedule = ref([])

  const calculateWeekRange = () => {
    const today = new Date()
    const dayOfWeek = today.getDay() || 7
    const startOfWeek = new Date(today)
    startOfWeek.setDate(today.getDate() - dayOfWeek + 1)
    const endOfWeek = new Date(startOfWeek)
    endOfWeek.setDate(startOfWeek.getDate() + 6)
    const fmt = (d) => `${String(d.getMonth() + 1).padStart(2, '0')}月${String(d.getDate()).padStart(2, '0')}日`
    weekRange.value = `${fmt(startOfWeek)} - ${fmt(endOfWeek)}`
  }

  const generateWeeklySchedule = () => {
    const today = new Date()
    const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const titles = ['高等数学 - 极限与连续', '英语词汇背诵', '政治 - 马克思主义原理']
    const times = ['09:00', '14:00', '19:00']
    const durations = ['2小时', '1.5小时', '1.5小时']
    return Array.from({ length: 7 }, (_, i) => {
      const date = new Date(today)
      date.setDate(today.getDate() + i)
      const dateStr = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
      const count = Math.floor(Math.random() * 3) + 2
      const items = Array.from({ length: count }, (_, j) => ({
        id: i * 10 + j,
        time: times[j],
        title: titles[j],
        duration: durations[j],
        completed: Math.random() > 0.5
      }))
      const done = items.filter(t => t.completed).length
      return {
        date: dateStr,
        title: `${days[i]} · ${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`,
        progress: Math.round((done / items.length) * 100),
        items
      }
    })
  }

  const toggleWeekTask = (item) => {
    item.completed = !item.completed
    for (const day of schedule.value) {
      if (day.items.find(t => t.id === item.id)) {
        const done = day.items.filter(t => t.completed).length
        day.progress = Math.round((done / day.items.length) * 100)
        break
      }
    }
  }

  // ── 阶段详情 ──
  const stageDetailLoading = ref(false)
  const dailyTasks = ref([])

  const stageTypeLabel = t => ({ 1: '基础', 2: '强化', 3: '突破', 4: '冲刺' }[t] || '阶段')

  const stageStatusClass = (stage) => {
    if (!stage?.stageStartDate) return 'upcoming'
    const today = new Date().toISOString().slice(0, 10)
    if (today < stage.stageStartDate) return 'upcoming'
    if (today > stage.stageEndDate) return 'done'
    return 'active'
  }

  const currentTypeLabel = computed(() => stageTypeLabel(currentStage.value?.stageType))
  const currentStatusKey = computed(() => stageStatusClass(currentStage.value))
  const currentStatusLabel = computed(() => {
    return { upcoming: '未开始', active: '进行中', done: '已完成' }[currentStatusKey.value]
  })

  const passedDays = computed(() => {
    const stage = currentStage.value
    if (!stage?.stageStartDate) return 0
    const s = new Date(stage.stageStartDate).getTime()
    const now = Date.now()
    if (now < s) return 0
    return Math.min(Math.floor((now - s) / 86400000), stage.stagePlannedDays || 0)
  })

  const stagePct = computed(() => {
    const total = currentStage.value?.stagePlannedDays || 0
    return total ? Math.round((passedDays.value / total) * 100) : 0
  })

  const loadDailyTasks = async () => {
    const stage = currentStage.value
    if (!stage?.stageId) { dailyTasks.value = []; return }
    stageDetailLoading.value = true
    try {
      const list = await getStageTaskList(stage.stageId)
      dailyTasks.value = (list || []).map(t => ({
        taskId: t.taskId,
        date: t.taskDate || '',
        taskTitle: t.taskCoreGoal || '任务',
        taskDetail: t.taskDetail || '',
        done: t.taskStatus === 1,
        taskStatus: t.taskStatus
      }))
    } catch (e) {
      console.warn('[plan] loadDailyTasks failed', e)
    } finally {
      stageDetailLoading.value = false
    }
  }

  const groupedTasks = computed(() => {
    const groups = {}
    dailyTasks.value.forEach(t => {
      if (!groups[t.date]) groups[t.date] = []
      groups[t.date].push(t)
    })
    return Object.fromEntries(Object.entries(groups).sort(([a], [b]) => b.localeCompare(a)))
  })

  const doneCount = computed(() => dailyTasks.value.filter(t => t.done).length)

  const toggleDailyTask = async (item) => {
    const newStatus = item.done ? 0 : 1
    try {
      await updateTaskStatus(item.taskId, { taskStatus: newStatus })
      item.done = !item.done
      item.taskStatus = newStatus
      if (item.done) uni.showToast({ title: '已完成 🎉', icon: 'none', duration: 1200 })
    } catch (e) {
      if (!e?.msg) uni.showToast({ title: '操作失败，请重试', icon: 'none' })
    }
  }

  const formatDayLabel = (date) => {
    const today = new Date().toISOString().slice(0, 10)
    const yesterday = new Date(Date.now() - 86400000).toISOString().slice(0, 10)
    if (date === today) return '今天'
    if (date === yesterday) return '昨天'
    return date.slice(5).replace('-', '月') + '日'
  }

  // ── 加载计划 ──
  const loadPlan = async () => {
    loading.value = true
    try {
      const data = await getCurrentPlan()
      plan.value = data || null
      if (plan.value?.stages?.length) {
        currentPhase.value = 0
        await loadDailyTasks()
      }
    } catch (e) {
      console.warn('[plan] loadPlan failed', e)
    } finally {
      loading.value = false
    }
  }

  onShow(() => {
    isDark.value = (getApp().globalData.theme || 'dark') === 'dark'
    calculateWeekRange()
    schedule.value = generateWeeklySchedule()
    loadPlan()
  })

  // ── AI 生成 ──
  const handleGenerate = async (confirmFn) => {
    if (generating.value) return
    if (plan.value) {
      const confirmed = confirmFn
        ? await confirmFn()
        : await new Promise(resolve =>
            uni.showModal({
              title: '重新生成规划', content: '将停用当前规划并生成新规划，确定继续？',
              success: res => resolve(res.confirm)
            })
          )
      if (!confirmed) return
    }
    generating.value = true
    uni.showLoading({ title: 'AI 规划生成中...', mask: true })
    try {
      const data = await generatePlan()
      plan.value = data
      currentPhase.value = 0
      await loadDailyTasks()
      uni.showToast({ title: '规划生成成功', icon: 'success' })
    } catch (e) {
      if (!e?.msg) uni.showToast({ title: '生成失败，请稍后重试', icon: 'none' })
    } finally {
      generating.value = false
      uni.hideLoading()
    }
  }

  // ── 生成阶段任务（待实现）──
  const generateStageTasks = () => {
    uni.showToast({ title: '功能开发中', icon: 'none' })
  }

  return {
    plan, loading, isDark,
    currentPhase, currentStage,
    weekRange, schedule,
    currentTypeLabel, currentStatusKey, currentStatusLabel,
    passedDays, stagePct,
    stageDetailLoading, dailyTasks, groupedTasks, doneCount,
    selectPhase, toggleWeekTask, toggleDailyTask,
    formatDayLabel, handleGenerate, generateStageTasks
  }
}
