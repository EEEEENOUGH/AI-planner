import request from '@/utils/request'

// ═══════════════════════════════════════
//  计划（Plan）
// ═══════════════════════════════════════

/**
 * AI 生成阶段规划（读取当前档案自动构建 Prompt）
 */
export function generatePlan() {
  return request({ url: '/plan/generate', method: 'POST', timeout: 120000 })
}

/**
 * 查询当前用户最新启用的总计划及阶段
 */
export function getCurrentPlan() {
  return request({ url: '/plan/current', method: 'GET' })
}

/**
 * 手动修改阶段信息（modifySource 后端自动置为 1-人工）
 * @param {number} stageId
 * @param {{ stageName?, stageStartDate?, stageEndDate?, stageCoreTask? }} data
 */
export function updateStage(stageId, data) {
  return request({ url: `/plan/stage/${stageId}`, method: 'PUT', data })
}

/**
 * AI 生成某阶段的每日任务（调用后批量写入 t_study_plan_day_task）
 * @param {number} stageId
 */
export function generateStageTasks(stageId) {
  return request({
    url: `/plan/stage/${stageId}/generate-tasks`,
    method: 'POST',
    timeout: 120000
  })
}

/**
 * 自然语言 AI 调整计划
 * @param {string} instruction  用户的调整指令，如"把基础阶段延长两周"
 */
export function adjustPlanByAi(instruction) {
  return request({
    url: '/plan/ai-adjust',
    method: 'POST',
    data: { instruction },
    timeout: 120000
  })
}

// ═══════════════════════════════════════
//  任务（Task）
// ═══════════════════════════════════════

/**
 * 查询某阶段所有每日任务
 * @param {number} stageId
 */
export function getStageTaskList(stageId) {
  return request({ url: `/task/stage/${stageId}`, method: 'GET' })
}

/**
 * 查询今日任务
 */
export function getTodayTasks() {
  return request({ url: '/task/today', method: 'GET' })
}

/**
 * 新增每日任务
 * @param {{ stageId: number, taskDate: string, taskCoreGoal: string, taskDetail?: string, durationMinutes?: number }} data
 */
export function createTask(data) {
  return request({ url: '/task', method: 'POST', data })
}

/**
 * 更新任务内容（标记 modifySource=1 人工）
 * @param {number} taskId
 * @param {{ taskCoreGoal?: string, taskDetail?: string, durationMinutes?: number }} data
 */
export function updateTask(taskId, data) {
  return request({ url: `/task/${taskId}`, method: 'PUT', data })
}

/**
 * 更新任务状态
 * @param {number} taskId
 * @param {{ taskStatus: 0|1|2, actualDurationMinutes?: number }} data
 */
export function updateTaskStatus(taskId, data) {
  return request({ url: `/task/${taskId}/status`, method: 'PATCH', data })
}

/**
 * 删除任务
 * @param {number} taskId
 */
export function deleteTask(taskId) {
  return request({ url: `/task/${taskId}`, method: 'DELETE' })
}

// ═══════════════════════════════════════
//  打卡（Checkin）
// ═══════════════════════════════════════

/**
 * 今日打卡
 * @param {{ checkinNote?: string }} data
 */
export function checkin(data) {
  return request({ url: '/task/checkin', method: 'POST', data: data || {} })
}

/**
 * 查询首页打卡汇总
 */
export function getCheckinSummary() {
  return request({ url: '/task/checkin/summary', method: 'GET' })
}
