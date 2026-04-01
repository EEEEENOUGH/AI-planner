<template>
  <view class="page">
    <view class="status-bar"></view>
    <view class="content">

      <!-- Header -->
      <view class="header">
        <view class="avatar-section">
          <view class="avatar-wrap">
            <u-avatar :src="avatarUrl" size="80" />
          </view>
          <view class="user-info">
            <text class="user-name">{{ nickname }}</text>
            <view class="user-tag">
              <text class="tag-text">🎯 {{ targetInstitution }}</text>
            </view>
            <text class="user-motto">考研冲刺中 🔥</text>
          </view>
        </view>
      </view>

      <!-- Stats Bar -->
      <view class="stats-bar">
        <view class="stat-cell">
          <text class="stat-num">{{ checkinCount }}</text>
          <text class="stat-lbl">坚持天数</text>
        </view>
        <view class="stat-cell">
          <text class="stat-num">{{ totalHours }}</text>
          <text class="stat-lbl">学习时长</text>
        </view>
        <view class="stat-cell">
          <text class="stat-num">{{ doneTasks }}</text>
          <text class="stat-lbl">完成任务</text>
        </view>
      </view>

      <!-- 设置与工具 -->
      <view class="section">
        <text class="section-title">设置与工具</text>
        <view class="menu-card">
          <view class="menu-row menu-row-border" @click="goProfile">
            <view class="menu-left">
              <view class="menu-icon-wrap" style="background: rgba(217,240,44,0.08)"><text class="menu-icon">👤</text></view>
              <text class="menu-label">修改资料</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-row menu-row-border" @click="goArchive">
            <view class="menu-left">
              <view class="menu-icon-wrap" style="background: rgba(59,130,246,0.08)"><text class="menu-icon">📋</text></view>
              <text class="menu-label">备考档案</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
          <view class="menu-row" @click="uni.showToast({ title: '敬请期待', icon: 'none' })">
            <view class="menu-left">
              <view class="menu-icon-wrap" style="background: rgba(124,58,237,0.08)"><text class="menu-icon">⚙️</text></view>
              <text class="menu-label">系统设置</text>
            </view>
            <text class="menu-arrow">›</text>
          </view>
        </view>
      </view>

      <!-- 退出按钮 -->
      <view class="logout-btn" @click="logout">
        <text class="logout-text">退出登录</text>
      </view>

      <text class="version-text">AI考研规划 v1.0.0</text>
    </view>
    <custom-tabbar :current="4"></custom-tabbar>
  </view>
</template>

<script setup>
import { useMy } from './useMy'
const { nickname, targetInstitution, avatarUrl, checkinCount, totalHours, doneTasks, goProfile, goArchive, logout } = useMy()
</script>

<style scoped lang="scss">
@import './my.scss';
</style>
