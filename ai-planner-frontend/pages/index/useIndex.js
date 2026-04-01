import { computed, reactive, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import {
  createTask,
  deleteTask,
  getCheckinSummary,
  getCurrentPlan,
  getTodayTasks,
  updateTask
} from '@/api/plan'

const DEFAULT_AI_RECOMMENDATION = {
  title: '今日建议',
  description: '先做最需要消化的内容，再用计时专注把今天的核心任务推进到完成。'
}

const readLocalActualMinutes = (taskId, durationMinutes) => {
  try {
    const saved = uni.getStorageSync(`timerRunning_${taskId}`)
    if (!saved || saved.taskId !== taskId) return null
    const usedSeconds = durationMinutes * 60 - saved.remainSeconds
    return Math.max(0, Math.floor(usedSeconds / 60))
  } catch (e) {
    return null
  }
}

const formatDuration = (durationMinutes) => {
  if (!durationMinutes) return '未设置时长'
  if (durationMinutes < 60) return `${durationMinutes}分钟`
  return durationMinutes % 60 === 0
    ? `${durationMinutes / 60}小时`
    : `${(durationMinutes / 60).toFixed(1)}小时`
}

export function useIndex() {
  const tasks = ref([])
  const planStages = ref([])
  const streakDays = ref(0)
  const aiRecommendation = ref(DEFAULT_AI_RECOMMENDATION)

  const taskFormVisible = ref(false)
  const deleteConfirmVisible = ref(false)
  const editingTask = ref(null)
  const formSaving = ref(false)
  const deletingTask = ref(false)
  const formStageId = ref(null)
  const taskForm = reactive({
    taskCoreGoal: '',
    taskDetail: '',
    durationMinutes: ''
  })

  const doneCount = computed(() => tasks.value.filter(task => task.done).length)
  const totalCount = computed(() => tasks.value.length)
  const progressRate = computed(() => {
    if (!totalCount.value) return 0
    return Math.round((doneCount.value / totalCount.value) * 100)
  })

  const mapTask = (task) => {
    const taskId = task.taskId ?? task.id
    const durationMinutes = task.durationMinutes || 0
    const serverActualDuration = task.actualDurationMinutes || 0
    const localActualDuration = readLocalActualMinutes(taskId, durationMinutes)
    const actualDurationMinutes =
      localActualDuration !== null && localActualDuration > serverActualDuration
        ? localActualDuration
        : serverActualDuration

    return {
      taskId,
      stageId: task.stageId,
      taskTitle: task.taskCoreGoal || '任务',
      taskDetail: task.taskDetail || '',
      done: task.taskStatus === 1,
      taskStatus: task.taskStatus ?? 0,
      durationMinutes,
      actualDurationMinutes
    }
  }

  const loadTodayTasks = async () => {
    try {
      const result = await getTodayTasks()
      tasks.value = (result || []).map(mapTask)
    } catch (e) {
      console.warn('[index] loadTodayTasks failed', e)
      tasks.value = []
    }
  }

  const loadPlan = async () => {
    try {
      const plan = await getCurrentPlan()
      planStages.value = plan?.stages || []
    } catch (e) {
      console.warn('[index] loadPlan failed', e)
      planStages.value = []
    }
  }

  const loadCheckinSummary = async () => {
    try {
      const summary = await getCheckinSummary()
      streakDays.value = summary?.currentStreakDays || 0
    } catch (e) {
      console.warn('[index] loadCheckinSummary failed', e)
      streakDays.value = 0
    }
  }

  const loadHomeData = () => {
    loadPlan()
    loadCheckinSummary()
    loadTodayTasks()
  }

  const resetTaskForm = () => {
    taskForm.taskCoreGoal = ''
    taskForm.taskDetail = ''
    taskForm.durationMinutes = ''
  }

  const openAddDirect = () => {
    editingTask.value = null
    resetTaskForm()
    formStageId.value = planStages.value[0]?.stageId || null
    taskFormVisible.value = true
  }

  const openEditTask = (task) => {
    editingTask.value = task
    taskForm.taskCoreGoal = task.taskTitle
    taskForm.taskDetail = task.taskDetail
    taskForm.durationMinutes = task.durationMinutes || ''
    formStageId.value = task.stageId || planStages.value[0]?.stageId || null
    taskFormVisible.value = true
  }

  const closeTaskForm = () => {
    taskFormVisible.value = false
    deleteConfirmVisible.value = false
    editingTask.value = null
  }

  const openDeleteConfirm = () => {
    if (!editingTask.value) return
    deleteConfirmVisible.value = true
  }

  const closeDeleteConfirm = () => {
    if (deletingTask.value) return
    deleteConfirmVisible.value = false
  }

  const submitTaskForm = async () => {
    if (formSaving.value) return
    if (!taskForm.taskCoreGoal.trim()) {
      uni.showToast({ title: '请输入任务目标', icon: 'none' })
      return
    }

    if (!editingTask.value && !formStageId.value) {
      uni.showToast({ title: '请选择所属阶段', icon: 'none' })
      return
    }

    formSaving.value = true
    try {
      const durationMinutes = taskForm.durationMinutes
        ? Number(taskForm.durationMinutes)
        : undefined

      if (editingTask.value) {
        await updateTask(editingTask.value.taskId, {
          stageId: formStageId.value || undefined,
          taskCoreGoal: taskForm.taskCoreGoal,
          taskDetail: taskForm.taskDetail,
          durationMinutes
        })
        await loadTodayTasks()
      } else {
        await createTask({
          stageId: formStageId.value,
          taskDate: new Date().toISOString().slice(0, 10),
          taskCoreGoal: taskForm.taskCoreGoal,
          taskDetail: taskForm.taskDetail,
          durationMinutes
        })
        await loadTodayTasks()
      }

      closeTaskForm()
      uni.showToast({ title: '保存成功', icon: 'success' })
    } catch (e) {
      if (!e?.msg) uni.showToast({ title: '保存失败', icon: 'none' })
    } finally {
      formSaving.value = false
    }
  }

  const confirmDeleteTask = async () => {
    if (!editingTask.value || deletingTask.value) return

    deletingTask.value = true
    try {
      await deleteTask(editingTask.value.taskId)
      await loadTodayTasks()
      closeTaskForm()
      uni.showToast({ title: '已删除', icon: 'success' })
    } catch (e) {
      if (!e?.msg) uni.showToast({ title: '删除失败', icon: 'none' })
    } finally {
      deletingTask.value = false
    }
  }

  const viewAiDetail = () => {
    uni.showToast({ title: '先完成今日任务，再继续加练', icon: 'none' })
  }

  const taskProgress = (task) => {
    if (!task.durationMinutes || task.durationMinutes <= 0) return 0
    if (task.done) return 1
    return Math.min((task.actualDurationMinutes || 0) / task.durationMinutes, 1)
  }

  const displayTasks = computed(() =>
    tasks.value.map(task => ({
      ...task,
      displayDuration: formatDuration(task.durationMinutes)
    }))
  )

  const goTimer = (task) => {
    if (!task.durationMinutes || task.durationMinutes <= 0) {
      uni.showToast({ title: '请先设置任务时长', icon: 'none' })
      return
    }

    uni.navigateTo({
      url: `/pages/timer/timer?taskId=${task.taskId}&taskTitle=${encodeURIComponent(task.taskTitle)}&durationMinutes=${task.durationMinutes}&actualDurationMinutes=${task.actualDurationMinutes || 0}`
    })
  }

  uni.$off('timerDone')
  uni.$on('timerDone', ({ taskId, completed, actualDurationMinutes }) => {
    const targetTask = tasks.value.find(item => item.taskId === taskId)
    if (!targetTask) return

    targetTask.actualDurationMinutes = actualDurationMinutes
    if (completed) {
      targetTask.done = true
      targetTask.taskStatus = 1
    }
  })

  onShow(() => {
    loadHomeData()
  })

  return {
    taskFormVisible,
    editingTask,
    formSaving,
    formStageId,
    taskForm,
    openAddDirect,
    openEditTask,
    closeTaskForm,
    submitTaskForm,
    planStages,
    deleteConfirmVisible,
    openDeleteConfirm,
    closeDeleteConfirm,
    confirmDeleteTask,
    doneCount,
    totalCount,
    progressRate,
    streakDays,
    aiRecommendation,
    viewAiDetail,
    displayTasks,
    goTimer,
    taskProgress
  }
}
