<template>
  <view class="page">
    <view class="status-bar"></view>

    <view class="content">
      <!-- Header -->
      <view class="header">
        <text class="title">学习进度</text>
        <text class="subtitle">数据统计与分析</text>
      </view>

      <!-- Overall Card -->
      <view class="overall-card">
        <view class="overall-top">
          <text class="card-label">总体完成度</text>
          <view class="value-row">
            <text class="overall-value">{{ overallProgress }}</text>
            <text class="overall-unit">%</text>
          </view>
        </view>
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value">{{ completedTasks }}</text>
            <text class="stat-label">已完成任务</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ studyDays }}</text>
            <text class="stat-label">学习天数</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-value">{{ streakDays }}</text>
            <text class="stat-label">连续天数</text>
          </view>
        </view>
      </view>

      <!-- Subject Progress -->
      <view class="section">
        <text class="section-title">科目进度</text>
        <view class="subject-list">
          <view v-for="subject in subjects" :key="subject.id" class="subject-card">
            <view class="subject-header">
              <view class="subject-name-row">
                <view class="subject-dot" :class="subject.progress >= 75 ? 'dot-good' : 'dot-normal'"></view>
                <text class="subject-name">{{ subject.name }}</text>
              </view>
              <text :class="['subject-percent', subject.progress >= 75 ? 'good' : 'normal']">
                {{ subject.progress }}%
              </text>
            </view>
            <view class="progress-bar">
              <view
                :class="['progress-fill', subject.progress >= 75 ? 'bg-good' : 'bg-normal']"
                :style="{ width: subject.progress + '%' }"
              ></view>
            </view>
            <text class="subject-info">已完成 {{ subject.completed }}/{{ subject.total }} 任务</text>
          </view>
        </view>
      </view>
    </view>

    <custom-tabbar :current="3"></custom-tabbar>
  </view>
</template>

<script setup>
import { useProgress } from './useProgress'
const {
  overallProgress,
  completedTasks,
  studyDays,
  streakDays,
  subjects
} = useProgress()
</script>

<style scoped lang="scss">
@import './progress.scss';
</style>
