import { ref, computed, onMounted } from 'vue'
import { getMyArchive, saveArchive } from '@/api/archive'

export function useArchive() {
  const saving = ref(false)
  const hasArchive = ref(false)
  const form = ref({
    targetInstitution: '',
    targetMajor: '',
    examDate: '',
    examSubjects: '',
    subjectMastery: '',
    dailyStudyDuration: 6,
    archiveName: ''
  })

  const daysUntilExam = computed(() => {
    if (!form.value.examDate) return '—'
    try {
      const target = new Date(form.value.examDate)
      const today = new Date(); today.setHours(0, 0, 0, 0)
      const diff = Math.floor((target - today) / 86400000)
      return diff > 0 ? diff : 0
    } catch { return '—' }
  })

  const loadArchive = async () => {
    try {
      const data = await getMyArchive()
      if (data) {
        hasArchive.value = true
        Object.assign(form.value, {
          targetInstitution:  data.targetInstitution  || '',
          targetMajor:        data.targetMajor        || '',
          examDate:           data.examDate           || '',
          examSubjects:       data.examSubjects       || '',
          subjectMastery:     data.subjectMastery     || '',
          dailyStudyDuration: data.dailyStudyDuration || 6,
          archiveName:        data.archiveName        || ''
        })
      }
    } catch (e) {
      console.warn('[archive] loadArchive failed', e)
    }
  }

  onMounted(() => { loadArchive() })

  const validateForm = () => {
    if (!form.value.targetInstitution.trim()) { uni.showToast({ title: '请填写目标院校', icon: 'none' }); return false }
    if (!form.value.targetMajor.trim())        { uni.showToast({ title: '请填写目标专业', icon: 'none' }); return false }
    if (!form.value.examDate.trim())           { uni.showToast({ title: '请填写考试日期', icon: 'none' }); return false }
    if (!/^\d{4}-\d{2}-\d{2}$/.test(form.value.examDate.trim())) {
      uni.showToast({ title: '日期格式应为 yyyy-MM-dd', icon: 'none' }); return false
    }
    return true
  }

  const buildArchivePayload = () => ({
    archiveName:        form.value.archiveName.trim(),
    targetInstitution:  form.value.targetInstitution.trim(),
    targetMajor:        form.value.targetMajor.trim(),
    examDate:           form.value.examDate.trim(),
    examSubjects:       form.value.examSubjects.trim(),
    dailyStudyDuration: form.value.dailyStudyDuration,
    subjectMastery:     form.value.subjectMastery.trim()
  })

  const submit = async () => {
    if (!validateForm() || saving.value) return
    saving.value = true
    try {
      await saveArchive(buildArchivePayload())
      hasArchive.value = true
      uni.showToast({ title: '保存成功', icon: 'success' })
    } catch (e) {
      if (!e?.msg) uni.showToast({ title: '保存失败，请重试', icon: 'none' })
    } finally {
      saving.value = false
    }
  }

  return { saving, hasArchive, form, daysUntilExam, submit }
}
