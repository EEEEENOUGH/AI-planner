<template>
  <view class="timer-page">
    <view class="page-glow glow-left" />
    <view class="page-glow glow-right" />

    <view class="top-bar">
      <view class="back-btn" @click="handleBack">
        <text class="back-icon">‹</text>
      </view>
      <text class="top-title">专注计时</text>
      <view class="top-placeholder" />
    </view>

    <view class="task-card">
      <text class="task-tag">当前任务</text>
      <text class="task-name">{{ taskTitle }}</text>
      <text class="task-note">{{ statusText }}</text>
    </view>

    <view class="ring-panel">
      <view class="ring-wrap">
        <svg class="ring-svg" viewBox="0 0 280 280" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="ringGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#7EA2FF" />
              <stop offset="100%" stop-color="#9FD4B3" />
            </linearGradient>
          </defs>
          <circle
            cx="140"
            cy="140"
            :r="RADIUS"
            fill="none"
            stroke="rgba(255,255,255,0.06)"
            stroke-width="12"
          />
          <circle
            cx="140"
            cy="140"
            :r="RADIUS"
            fill="none"
            stroke="url(#ringGrad)"
            stroke-width="12"
            stroke-linecap="round"
            :stroke-dasharray="CIRCUMFERENCE"
            :stroke-dashoffset="strokeDashoffset"
            transform="rotate(-90 140 140)"
            class="progress-arc"
          />
        </svg>

        <view class="ring-center" v-if="!isFinished">
          <text class="time-sub">剩余时间</text>
          <text class="time-label">{{ remainLabel }}</text>
          <text class="progress-text">已完成 {{ progressPercent }}%</text>
        </view>

        <view class="ring-center" v-else>
          <view class="done-icon-wrap">
            <text class="done-icon">✓</text>
          </view>
          <text class="done-label">任务完成!</text>
        </view>
      </view>
    </view>

    <view class="action-area" v-if="!isFinished">
      <view class="main-btn" :class="{ 'main-btn-secondary': isRunning }" @click="toggleTimer">
        <text class="main-btn-text">{{ isRunning ? '我要歇一会~' : 'CONTINUE!' }}</text>
      </view>
    </view>

    <view class="action-area" v-else>
      <view class="main-btn" @click="finishAndBack">
        <text class="main-btn-text">返回首页</text>
      </view>
    </view>

    <view class="modal-mask" v-if="exitConfirmVisible" @click.self="cancelExit">
      <view class="exit-modal">
        <text class="exit-title">先暂停保存吗？</text>
        <text class="exit-desc">退出后会保留当前进度，下次可以继续本次专注。</text>
        <view class="exit-btns">
          <view class="exit-btn exit-btn-cancel" @click="cancelExit">
            <text class="exit-btn-text">继续计时</text>
          </view>
          <view class="exit-btn exit-btn-confirm" @click="confirmExit">
            <text class="exit-btn-text">{{ saving ? '保存中...' : '保存退出' }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useTimer } from './useTimer'

const {
  taskTitle,
  remainLabel,
  progress,
  isRunning,
  isFinished,
  saving,
  exitConfirmVisible,
  RADIUS,
  CIRCUMFERENCE,
  strokeDashoffset,
  init,
  toggleTimer,
  handleBack,
  confirmExit,
  cancelExit,
  finishAndBack
} = useTimer()

const progressPercent = computed(() => Math.round(progress.value * 100))
const statusText = computed(() => {
  if (isFinished.value) return '很好，今天这项已经完成了。'
  return isRunning.value ? '保持节奏，按自己的呼吸继续。' : '准备好了就开始，不必急。'
})

onLoad((options) => {
  init(options)
})
</script>

<style scoped lang="scss">
@import './timer.scss';
</style>
