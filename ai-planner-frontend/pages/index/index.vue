<template>
  <view class="page">
    <view class="status-bar"></view>

    <view class="content">
      <view class="header">
        <text class="title">考研复习</text>
        <text class="subtitle">AI智能规划助手</text>
      </view>

      <view class="metrics">
        <view class="metric-card">
          <text class="label">今日进度</text>
          <view class="value-row">
            <text class="value lime">{{ progressRate }}</text>
            <text class="unit lime">%</text>
          </view>
          <text class="desc">{{ doneCount }}/{{ totalCount }} 任务完成</text>
        </view>
        <view class="metric-card">
          <text class="label">连续学习</text>
          <view class="value-row">
            <text class="value">{{ streakDays }}</text>
            <text class="unit">天</text>
          </view>
          <text class="desc">保持节奏</text>
        </view>
      </view>

      <view class="section">
        <text class="section-title">AI 建议</text>
        <view class="ai-card">
          <text class="ai-title">{{ aiRecommendation.title }}</text>
          <text class="ai-desc">{{ aiRecommendation.description }}</text>
          <view class="ai-btn" @click="viewAiDetail">
            <text class="btn-text">查看详情</text>
          </view>
        </view>
      </view>

      <view class="section">
        <view class="section-header">
          <text class="section-title">今日任务</text>
          <text class="add-btn" @click="openAddDirect">+</text>
        </view>

        <view class="tasks-card">
          <view v-for="task in displayTasks" :key="task.taskId" class="task-item">
            <!-- 进度圆圈（点击打开编辑）-->
            <view class="task-ring-wrap" @click="openEditTask(task)">
              <svg class="task-ring-svg" viewBox="0 0 40 40" xmlns="http://www.w3.org/2000/svg">
                <circle cx="20" cy="20" r="16" fill="none" stroke="rgba(255,255,255,0.08)" stroke-width="3"/>
                <circle
                  cx="20" cy="20" r="16"
                  fill="none"
                  :stroke="task.done ? '#D9F02C' : 'rgba(217,240,44,0.5)'"
                  stroke-width="3"
                  stroke-linecap="round"
                  :stroke-dasharray="100.53"
                  :stroke-dashoffset="task.done ? 0 : (100.53 * (1 - taskProgress(task)))"
                  transform="rotate(-90 20 20)"
                  class="task-ring-arc"
                />
              </svg>
              <view class="task-ring-center">
                <text class="task-ring-check" v-if="task.done">✓</text>
                <text class="task-ring-pct" v-else-if="taskProgress(task) > 0">{{ Math.round(taskProgress(task) * 100) }}</text>
              </view>
            </view>

            <!-- 内容区（点击打开编辑）-->
            <view class="task-content" @click="openEditTask(task)">
              <text :class="['task-title', task.done ? 'completed' : '']">{{ task.taskTitle }}</text>
              <text class="task-time">{{ task.displayDuration }}
                <text v-if="task.actualDurationMinutes > 0" class="task-actual"> · 已专注 {{ task.actualDurationMinutes }} min</text>
              </text>
            </view>

            <!-- 箭头（点击进入计时器）-->
            <view class="task-arrow-btn" @click.stop="goTimer(task)">
              <text class="task-arrow">›</text>
            </view>
          </view>

          <view v-if="displayTasks.length === 0" class="empty-state">
            <text class="empty-text">今天还没有任务，点击右上角 '+' 新增吧</text>
          </view>
        </view>
      </view>
    </view>

    <custom-tabbar :current="0"></custom-tabbar>

    <view class="modal-mask" v-if="taskFormVisible" @click.self="closeTaskForm">
      <view class="modal-panel">
        <view class="modal-header">
          <text class="modal-title">{{ editingTask ? '编辑任务' : '新增任务' }}</text>
          <text class="modal-close" @click="closeTaskForm">✕</text>
        </view>
        
        <view class="form-field form-field-stage">
          <text class="form-label">所属阶段 <text class="form-required">*</text></text>
          <scroll-view class="stage-tabs-scroll" scroll-x>
            <view class="stage-tabs">
              <view
                v-for="s in planStages" :key="s.stageId" class="stage-tab"
                :class="{ 'stage-tab-active': formStageId === s.stageId }"
                @click="formStageId = s.stageId"
              >
                <text class="stage-tab-text">{{ s.stageName.slice(0, 4) }}</text>
              </view>
            </view>
          </scroll-view>
        </view>

        <view class="form-field form-field-goal">
          <text class="form-label">任务目标 <text class="form-required">*</text></text>
          <view class="form-input-wrap">
            <input class="form-input" v-model="taskForm.taskCoreGoal" placeholder="简短描述今日核心目标" placeholder-class="placeholder-style" maxlength="100" />
          </view>
        </view>

        <view class="form-field form-field-detail">
          <text class="form-label">任务详情</text>
          <view class="form-textarea-wrap">
            <textarea class="form-textarea" v-model="taskForm.taskDetail" placeholder="补充说明（可选）" placeholder-class="placeholder-style" maxlength="300" auto-height />
          </view>
        </view>

        <view class="form-field form-field-duration">
          <text class="form-label">计划时长（分钟）</text>
          <view class="form-input-wrap">
            <input class="form-input" v-model="taskForm.durationMinutes" type="number" placeholder="如：60" placeholder-class="placeholder-style" />
          </view>
        </view>

        <view class="form-btns">
          <view class="form-btn form-btn-cancel" @click="closeTaskForm">
            <text class="form-btn-text">取消</text>
          </view>
          <view v-if="editingTask" class="form-btn form-btn-delete" @click="openDeleteConfirm">
            <text class="form-btn-text">删除</text>
          </view>
          <view class="form-btn form-btn-save" :class="{ 'btn-disabled': formSaving }" @click="submitTaskForm">
            <text class="form-btn-text">{{ formSaving ? '保存中...' : '保存' }}</text>
          </view>
          
        </view>
      </view>
    </view>
    <view class="modal-mask delete-modal-mask" v-if="deleteConfirmVisible" @click.self="closeDeleteConfirm">
      <view class="modal-panel delete-modal-panel">
        <view class="modal-header">
          <text class="modal-title">删除任务</text>
          <text class="modal-close" @click="closeDeleteConfirm">✕</text>
        </view>

        <view class="delete-modal-body">
          <text class="delete-modal-text">确定要删除「{{ editingTask ? editingTask.taskTitle : '' }}」吗？</text>
        </view>

        <view class="form-btns">
          <view class="form-btn form-btn-cancel" @click="closeDeleteConfirm">
            <text class="form-btn-text">取消</text>
          </view>
          <view class="form-btn form-btn-save form-btn-delete-confirm" @click="confirmDeleteTask">
            <text class="form-btn-text">确认删除</text>
          </view>
        </view>
      </view>
    </view>
    
  </view>
</template>

<script setup>
import { useIndex } from './useIndex'
const {
  taskFormVisible, editingTask, formSaving, formStageId, taskForm,
  openAddDirect, openEditTask, closeTaskForm, submitTaskForm, planStages,
  deleteConfirmVisible, openDeleteConfirm, closeDeleteConfirm, confirmDeleteTask,
  doneCount, totalCount, progressRate,
  streakDays, aiRecommendation, viewAiDetail, displayTasks,
  goTimer, taskProgress
} = useIndex()
</script>

<style scoped lang="scss">
@import './index.scss';
</style>