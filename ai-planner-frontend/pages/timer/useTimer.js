import { ref, computed, onUnmounted } from 'vue'
import { onShow, onHide } from '@dcloudio/uni-app'
import { updateTaskStatus } from '@/api/plan'

export function useTimer() {
  const taskId = ref(null)
  const taskTitle = ref('')
  const durationMinutes = ref(0)
  const actualDurationMinutes = ref(0)

  const totalSeconds = computed(() => durationMinutes.value * 60)
  const remainSeconds = ref(0)
  const isRunning = ref(false)
  const isFinished = ref(false)
  const exitConfirmVisible = ref(false)
  const saving = ref(false)

  let timer = null

  const progress = computed(() => {
    if (totalSeconds.value <= 0) return 0
    return Math.max(0, Math.min(1 - (remainSeconds.value / totalSeconds.value), 1))
  })

  const remainLabel = computed(() => {
    const s = Math.max(remainSeconds.value, 0)
    const m = Math.floor(s / 60)
    const sec = s % 60
    return `${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
  })

  const RADIUS = 120
  const CIRCUMFERENCE = 2 * Math.PI * RADIUS
  const strokeDashoffset = computed(() => CIRCUMFERENCE * (1 - progress.value))

  const _stateKey = () => `timerRunning_${taskId.value}`

  const _persistState = () => {
    if (!taskId.value) return
    uni.setStorageSync(_stateKey(), {
      taskId: taskId.value,
      remainSeconds: remainSeconds.value,
      wasRunning: isRunning.value,
      savedAt: Date.now()
    })
  }

  const _loadPersistedState = () => {
    if (!taskId.value) return null
    try { return uni.getStorageSync(_stateKey()) || null } catch (e) { return null }
  }

  const _clearPersistedState = () => {
    if (!taskId.value) return
    try { uni.removeStorageSync(_stateKey()) } catch (e) {}
  }

  const init = (options) => {
    taskId.value = Number(options.taskId)
    taskTitle.value = decodeURIComponent(options.taskTitle || '')
    durationMinutes.value = Number(options.durationMinutes) || 25
    actualDurationMinutes.value = Number(options.actualDurationMinutes) || 0

    const backendRemain = Math.max(totalSeconds.value - (actualDurationMinutes.value * 60), 0)
    const saved = _loadPersistedState()

    if (saved && saved.taskId === taskId.value) {
      const storedTotalUsed = totalSeconds.value - saved.remainSeconds
      if (saved.remainSeconds > totalSeconds.value || storedTotalUsed < 0) {
        _clearPersistedState()
        remainSeconds.value = backendRemain
      } else if (saved.wasRunning && saved.savedAt) {
        const passedSec = Math.floor((Date.now() - saved.savedAt) / 1000)
        remainSeconds.value = Math.max(saved.remainSeconds - passedSec, 0)
        if (backendRemain < remainSeconds.value - 60) {
          remainSeconds.value = backendRemain
        }
      } else {
        remainSeconds.value = saved.remainSeconds
        if (backendRemain < remainSeconds.value - 60) {
          remainSeconds.value = backendRemain
        }
      }
    } else {
      remainSeconds.value = backendRemain
    }

    remainSeconds.value = Math.min(remainSeconds.value, totalSeconds.value)

    if (remainSeconds.value <= 0) {
      isFinished.value = true
      remainSeconds.value = 0
    }
  }

  const toggleTimer = () => {
    if (isFinished.value) return
    isRunning.value ? pauseTimer() : startTimer()
  }

  const startTimer = () => {
    if (isRunning.value || isFinished.value) return
    isRunning.value = true
    timer = setInterval(() => {
      if (remainSeconds.value <= 0) {
        clearInterval(timer)
        timer = null
        isRunning.value = false
        isFinished.value = true
        _clearPersistedState()
        saveProgress(true)
        return
      }
      remainSeconds.value--
      if (remainSeconds.value % 5 === 0) _persistState()
    }, 1000)
  }

  const pauseTimer = () => {
    clearInterval(timer)
    timer = null
    isRunning.value = false
    _persistState()
  }

  const saveProgress = async (completed = false) => {
    if (saving.value) return null
    saving.value = true
    try {
      const usedSeconds = totalSeconds.value - remainSeconds.value
      const totalActual = Math.floor(usedSeconds / 60)
      const newStatus = completed ? 1 : 0
      await updateTaskStatus(taskId.value, {
        taskStatus: newStatus,
        actualDurationMinutes: totalActual
      })
      return { completed, actualDurationMinutes: totalActual }
    } catch (e) {
      if (!e?.msg) uni.showToast({ title: '保存失败', icon: 'none' })
      return null
    } finally {
      saving.value = false
    }
  }

  const handleBack = () => {
    if (isFinished.value) { goBack(true); return }
    const usedSeconds = totalSeconds.value - remainSeconds.value
    if (!isRunning.value && usedSeconds === 0) { goBack(false); return }
    pauseTimer()
    exitConfirmVisible.value = true
  }

  const confirmExit = async () => {
    exitConfirmVisible.value = false
    _persistState()
    const result = await saveProgress(false)
    goBack(false, result?.actualDurationMinutes)
  }

  const cancelExit = () => {
    exitConfirmVisible.value = false
    if (!isFinished.value) startTimer()
  }

  const goBack = (completed, savedActual) => {
    const usedSeconds = totalSeconds.value - remainSeconds.value
    const actual = savedActual ?? Math.floor(usedSeconds / 60)
    uni.navigateBack({
      delta: 1,
      success: () => uni.$emit('timerDone', { taskId: taskId.value, completed, actualDurationMinutes: actual })
    })
  }

  const finishAndBack = async () => {
    _clearPersistedState()
    const result = await saveProgress(true)
    goBack(true, result?.actualDurationMinutes)
  }

  onHide(() => {
    if (isRunning.value) {
      _persistState()
      clearInterval(timer)
      timer = null
      isRunning.value = false
    } else if (totalSeconds.value - remainSeconds.value > 0) {
      _persistState()
    }
  })

  onShow(() => {
    if (!taskId.value) return
    const saved = _loadPersistedState()
    if (!saved || saved.taskId !== taskId.value) return

    if (saved.wasRunning && saved.savedAt) {
      const passedSec = Math.floor((Date.now() - saved.savedAt) / 1000)
      remainSeconds.value = Math.max(saved.remainSeconds - passedSec, 0)

      if (remainSeconds.value <= 0) {
        isFinished.value = true
        _clearPersistedState()
        saveProgress(true)
      } else {
        startTimer()
      }
    } else {
      remainSeconds.value = saved.remainSeconds
    }
  })

  onUnmounted(() => { clearInterval(timer); timer = null })

  return {
    taskTitle, durationMinutes, remainLabel, progress, isRunning, isFinished, saving,
    exitConfirmVisible, RADIUS, CIRCUMFERENCE, strokeDashoffset,
    init, toggleTimer, handleBack, confirmExit, cancelExit, finishAndBack
  }
}