import { ref } from 'vue'

export function useProgress() {
  const overallProgress = ref(68)
  const completedTasks = ref(156)
  const studyDays = ref(45)
  const streakDays = ref(12)
  const subjects = ref([
    { id: 1, name: '高等数学', progress: 78, completed: 42, total: 54 },
    { id: 2, name: '英语', progress: 65, completed: 35, total: 54 },
    { id: 3, name: '政治', progress: 82, completed: 41, total: 50 },
    { id: 4, name: '专业课', progress: 55, completed: 28, total: 51 }
  ])

  return {
    overallProgress,
    completedTasks,
    studyDays,
    streakDays,
    subjects
  }
}
