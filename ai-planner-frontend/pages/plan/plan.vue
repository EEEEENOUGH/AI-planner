<template>
  <view class="page">
    <view class="status-bar"></view>

    <view class="content">
      <!-- Header -->
      <view class="header">
        <text class="header__title">学习计划</text>
        <view class="ai-btn" @tap="handleGenerate">
          <text class="ai-icon">✨</text>
          <text class="ai-btn__text">AI 调整</text>
        </view>
      </view>

      <!-- 有计划：阶段导航 + 详情 -->
      <template v-if="plan && plan.stages && plan.stages.length">

        <!-- 阶段分段控制器 -->
        <view class="segment-wrap">
          <view class="segment">
            <view
              v-for="(stage, index) in plan.stages"
              :key="stage.stageId"
              class="segment__item"
              :class="{ 'segment__item--active': currentPhase === index }"
              @tap="selectPhase(index)"
            >
              <text class="segment__label">{{ stage.stageName.slice(0, 4) }}</text>
            </view>
          </view>
        </view>

        <!-- 阶段详情 -->
        <view class="stage-detail" v-if="currentStage">

          <!-- 阶段进度卡 -->
          <view class="card">
            <view class="card__top-row">
              <view class="card__name-col">
                <text class="card__stage-name">{{ currentStage.stageName }}</text>
                <view class="badge-row">
                  <view class="badge">{{ currentTypeLabel }}</view>
                  <view class="badge" :class="'badge--' + currentStatusKey">{{ currentStatusLabel }}</view>
                </view>
              </view>
              <text class="card__pct">{{ stagePct }}<text class="card__pct-unit">%</text></text>
            </view>
            <view class="progress-track">
              <view class="progress-fill" :style="{ width: stagePct + '%' }" />
            </view>
            <view class="card__date-row">
              <text class="card__date">{{ currentStage.stageStartDate }} — {{ currentStage.stageEndDate }}</text>
              <text class="card__days">{{ passedDays }}/{{ currentStage.stagePlannedDays }} 天</text>
            </view>
          </view>

          <!-- 阶段目标（折叠式）-->
          <view class="card" v-if="currentStage.stageCoreTask">
            <view class="card__label--collapse-row" @tap="coreTaskCollapsed = !coreTaskCollapsed">
              <text class="card__label--title">阶段目标</text>
              <text class="card__collapse-icon">{{ coreTaskCollapsed ? '▶' : '▼' }}</text>
            </view>
            <text class="core-task-text" v-if="!coreTaskCollapsed">{{ currentStage.stageCoreTask }}</text>
          </view>

          <!-- 本周计划（折叠式大卡片）-->
          <view class="card week-plan-card">
            <view class="week-plan__header" @tap="weekPlanCollapsed = !weekPlanCollapsed">
              <view class="week-plan__header-left">
                <text class="week-plan__title">本周计划</text>
                <text class="week-plan__stat">{{ doneCount }}/{{ dailyTasks.length }} 已完成</text>
              </view>
              <view class="week-plan__header-right">
                <view class="gen-btn" @tap.stop="generateStageTasks(currentStage.stageId)">
                  <text class="gen-btn__text">生成任务</text>
                </view>
                <text class="week-plan__collapse-icon">{{ weekPlanCollapsed ? '▶' : '▼' }}</text>
              </view>
            </view>

            <view v-if="!weekPlanCollapsed">
              <!-- 加载中 -->
              <view class="loading-wrap" v-if="stageDetailLoading">
                <u-loading-icon mode="circle" color="#D9F02C" />
                <text class="loading-text">加载中...</text>
              </view>

              <!-- 按天分组 -->
              <view v-else-if="dailyTasks.length" class="week-plan__days">
                <view class="day-card" v-for="(group, date) in groupedTasks" :key="date">
                  <view class="day-card__header" @tap="toggleDayCollapse(date)">
                    <view class="day-card__header-left">
                      <text class="day-card__title">{{ formatDayLabel(date) }}</text>
                      <text class="day-card__date-sub">{{ date.slice(5).replace('-', '月') }}日</text>
                    </view>
                    <view class="day-card__header-right">
                      <view
                        class="day-badge"
                        :class="group.filter(t=>t.done).length === group.length ? 'day-badge--done' : 'day-badge--pending'"
                      >
                        <text class="day-badge__text">{{ group.filter(t=>t.done).length }}/{{ group.length }}</text>
                      </view>
                      <text class="day-card__collapse-icon">{{ collapsedDays[date] ? '▶' : '▼' }}</text>
                    </view>
                  </view>

                  <view v-if="!collapsedDays[date]" class="day-card__tasks">
                    <view
                      class="task-item"
                      v-for="item in group"
                      :key="item.taskId"
                      :class="{ 'task-item--done': item.done }"
                      @tap="toggleDailyTask(item)"
                    >
                      <view class="task-item__check" :class="{ 'task-item__check--checked': item.done }">
                        <text class="task-item__tick" v-if="item.done">✓</text>
                      </view>
                      <view class="task-item__body">
                        <text class="task-item__title" :class="{ 'task-item__title--done': item.done }">
                          {{ item.taskTitle }}
                        </text>
                        <text class="task-item__detail" v-if="item.taskDetail">{{ item.taskDetail }}</text>
                      </view>
                      <view class="task-tag" :class="item.done ? 'task-tag--done' : 'task-tag--todo'">
                        <text class="task-tag__text">{{ item.done ? '完成' : '待做' }}</text>
                      </view>
                    </view>
                  </view>
                </view>
              </view>

              <!-- 空状态 -->
              <view class="empty-tasks" v-else>
                <text class="empty-tasks__text">暂无本周任务，点击「生成任务」开始</text>
              </view>
            </view>
          </view>

        </view>
      </template>

      <!-- 无计划：本周计划 + 空状态 -->
      <template v-else>
        <view class="section" v-if="schedule.length">
          <view class="section__header">
            <text class="section__title">本周计划</text>
            <text class="section__week">{{ weekRange }}</text>
          </view>
          <view class="schedule-list">
            <view v-for="day in schedule" :key="day.date" class="day-section">
              <view class="day-section__header">
                <text class="day-section__title">{{ day.title }}</text>
                <view :class="['day-badge', day.progress === 100 ? 'day-badge--done' : 'day-badge--pending']">
                  <text class="day-badge__text">{{ day.progress }}%</text>
                </view>
              </view>
              <view v-for="item in day.items" :key="item.id" class="schedule-item">
                <view class="schedule-item__time-col">
                  <text class="schedule-item__time">{{ item.time }}</text>
                </view>
                <view class="schedule-item__content">
                  <text class="schedule-item__title">{{ item.title }}</text>
                  <text class="schedule-item__duration">{{ item.duration }}</text>
                </view>
                <view
                  class="schedule-item__check"
                  :class="{ 'schedule-item__check--checked': item.completed }"
                  @tap="toggleWeekTask(item)"
                >
                  <text class="schedule-item__tick">✓</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <view class="empty-wrap" v-if="!loading">
          <view class="empty-card">
            <text class="empty-card__title">还没有学习规划</text>
            <text class="empty-card__desc">点击右上角「AI 生成」，让 AI 为你量身定制专属备考规划</text>
          </view>
        </view>
      </template>

    </view>

    <custom-tabbar :current="1" :isDark="isDark"></custom-tabbar>

    <!-- 自定义确认弹窗 -->
    <view class="modal-mask" v-if="showConfirmModal" @tap.self="onModalCancel">
      <view class="modal-box">
        <text class="modal-box__title">重新生成规划</text>
        <text class="modal-box__desc">将停用当前规划并生成新规划，确定继续？</text>
        <view class="modal-box__actions">
          <view class="modal-box__btn modal-box__btn--cancel" @tap="onModalCancel">
            <text class="modal-box__btn-text">取消</text>
          </view>
          <view class="modal-box__btn modal-box__btn--confirm" @tap="onModalConfirm">
            <text class="modal-box__btn-text modal-box__btn-text--confirm">确定</text>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup>
import { ref } from 'vue'
import { usePlan } from './usePlan'

const coreTaskCollapsed = ref(false)
const weekPlanCollapsed = ref(false)
const collapsedDays = ref({})
const showConfirmModal = ref(false)
let confirmResolve = null

const toggleDayCollapse = (date) => {
  collapsedDays.value[date] = !collapsedDays.value[date]
}

const showCustomConfirm = () => {
  showConfirmModal.value = true
  return new Promise(resolve => { confirmResolve = resolve })
}

const onModalConfirm = () => {
  showConfirmModal.value = false
  if (confirmResolve) { confirmResolve(true); confirmResolve = null }
}

const onModalCancel = () => {
  showConfirmModal.value = false
  if (confirmResolve) { confirmResolve(false); confirmResolve = null }
}

const {
  plan, loading, isDark,
  currentPhase, currentStage,
  weekRange, schedule,
  currentTypeLabel, currentStatusKey, currentStatusLabel,
  passedDays, stagePct,
  stageDetailLoading, dailyTasks, groupedTasks, doneCount,
  selectPhase, toggleWeekTask, toggleDailyTask,
  formatDayLabel, generateStageTasks,
  handleGenerate: _handleGenerate
} = usePlan()

const handleGenerate = () => _handleGenerate(showCustomConfirm)
</script>

<style lang="scss">
@import "./plan.scss";
</style>
