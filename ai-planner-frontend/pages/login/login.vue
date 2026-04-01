<template>
  <view class="page">
    <view class="status-bar"></view>
    <view class="content">

      <!-- Logo Area -->
      <view class="logo-section">
        <view class="logo-wrap">
          <image src="/static/logo.png" class="logo" mode="aspectFit" />
        </view>
        <text class="app-name">考研智划</text>
        <text class="app-slogan">AI 驱动的考研备考助手</text>
      </view>

      <!-- Tab Switch -->
      <view class="tab-switch">
        <view :class="['tab-btn', !isRegisterMode ? 'tab-active' : '']" @tap="switchToLogin">
          <text class="tab-label">登录</text>
        </view>
        <view :class="['tab-btn', isRegisterMode ? 'tab-active' : '']" @tap="switchToRegister">
          <text class="tab-label">注册</text>
        </view>
      </view>

      <!-- Login Form -->
      <view v-if="!isRegisterMode" class="form">
        <view class="input-group">
          <view class="input-label-row">
            <text class="input-icon">📱</text>
            <text class="input-label">账号</text>
          </view>
          <view class="input-wrap">
            <input v-model="loginName" class="input" placeholder="请输入账号" placeholder-class="input-placeholder" type="text" />
          </view>
        </view>
        <view class="input-group">
          <view class="input-label-row">
            <text class="input-icon">🔒</text>
            <text class="input-label">密码</text>
          </view>
          <view class="input-wrap">
            <input v-model="password" class="input" placeholder="请输入密码" placeholder-class="input-placeholder" password />
          </view>
        </view>
        <view class="submit-btn" :class="{ loading }" @tap="submit">
          <text class="submit-text">{{ loading ? '请稍候...' : '立即登录' }}</text>
        </view>
      </view>

      <!-- Register Form -->
      <view v-if="isRegisterMode" class="form">
        <view class="input-group">
          <view class="input-label-row">
            <text class="input-icon">👤</text>
            <text class="input-label">昵称</text>
          </view>
          <view class="input-wrap">
            <input v-model="nickname" class="input" placeholder="昵称（可选）" placeholder-class="input-placeholder" type="text" />
          </view>
        </view>
        <view class="input-group">
          <view class="input-label-row">
            <text class="input-icon">📱</text>
            <text class="input-label">账号</text>
          </view>
          <view class="input-wrap">
            <input v-model="loginName" class="input" placeholder="请输入账号" placeholder-class="input-placeholder" type="text" />
          </view>
        </view>
        <view class="input-group">
          <view class="input-label-row">
            <text class="input-icon">🔒</text>
            <text class="input-label">设置密码</text>
          </view>
          <view class="input-wrap">
            <input v-model="password" class="input" placeholder="请输入密码" placeholder-class="input-placeholder" password />
          </view>
        </view>
        <view class="input-group">
          <view class="input-label-row">
            <text class="input-icon">🔒</text>
            <text class="input-label">确认密码</text>
          </view>
          <view class="input-wrap">
            <input v-model="confirmPassword" class="input" placeholder="再次输入密码" placeholder-class="input-placeholder" password />
          </view>
        </view>
        <view class="submit-btn" :class="{ loading }" @tap="submit">
          <text class="submit-text">{{ loading ? '请稍候...' : '注册并登录' }}</text>
        </view>
      </view>

      <view class="footer-tip">
        <text class="tip-text">{{ isRegisterMode ? '已有账号？' : '还没有账号？' }}</text>
        <text class="tip-link" @tap="isRegisterMode ? switchToLogin() : switchToRegister()">{{ isRegisterMode ? '去登录' : '去注册' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { useLogin } from './useLogin'
const { isRegisterMode, loading, loginName, password, confirmPassword, nickname, switchToLogin, switchToRegister, submit } = useLogin()
</script>

<style scoped lang="scss">
@import './login.scss';

.footer-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
}
.tip-text { font-size: 24rpx; color: #888888; }
.tip-link  { font-size: 24rpx; color: #D9F02C; font-weight: 700; }
</style>
