<template>
  <view class="page">
    <view class="status-bar"></view>

    <view class="content">
      <!-- Header -->
      <view class="header">
        <text class="title">知识复习</text>
        <text class="subtitle">AI 答疑与知识梳理</text>
      </view>

      <!-- Subject Selector -->
      <scroll-view scroll-x class="subject-scroll" :show-scrollbar="false">
        <view class="subject-selector">
          <view
            v-for="(subject, index) in subjects"
            :key="index"
            :class="['subject-item', { active: currentSubject === index }]"
            @tap="currentSubject = index"
          >
            <text class="subject-text">{{ subject }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- AI 答疑 -->
      <view class="section">
        <text class="section-title">AI 答疑</text>
        <view class="chat-card" @tap="goToAi">
          <view v-if="messages.length === 0" class="chat-empty">
            <text class="chat-empty-text">点击开始与 AI 对话 →</text>
          </view>
          <view v-for="msg in messages" :key="msg.id" :class="['message-row', msg.type]">
            <view v-if="msg.type === 'ai'" class="ai-avatar">
              <text class="ai-avatar-text">AI</text>
            </view>
            <view class="bubble">
              <text class="msg-text">{{ truncate(msg.content) }}</text>
            </view>
          </view>
        </view>
        <view class="chat-input-row">
          <view class="chat-input-wrap">
            <input class="chat-input" v-model="inputText" placeholder="向 AI 提问..." placeholder-class="chat-placeholder" confirm-type="send" @confirm="goToAi" />
          </view>
          <view class="chat-send-btn" @tap="goToAi">
            <text class="chat-send-text">→</text>
          </view>
        </view>
      </view>

      <!-- 知识点梳理 -->
      <view class="section">
        <text class="section-title">知识点梳理</text>
        <view class="knowledge-list">
          <view v-for="point in knowledgePoints" :key="point.id" class="knowledge-card">
            <view class="kp-icon" :style="{ background: point.color }">
              <text class="kp-number">{{ point.number }}</text>
            </view>
            <view class="kp-content">
              <text class="kp-title">{{ point.title }}</text>
              <view class="mastery-row">
                <view class="mastery-bar-bg">
                  <view
                    class="mastery-bar-fill"
                    :class="point.mastery >= 80 ? 'fill-good' : 'fill-weak'"
                    :style="{ width: point.mastery + '%' }"
                  ></view>
                </view>
                <text :class="['kp-mastery', point.mastery >= 80 ? 'good' : 'weak']">
                  {{ point.mastery }}%
                </text>
              </view>
              <text v-if="point.mastery < 80" class="kp-hint">需要加强 ⚡</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <custom-tabbar :current="2"></custom-tabbar>
  </view>
</template>

<script setup>
import { useKnowledge } from './useKnowledge'
const {
  subjects,
  currentSubject,
  inputText,
  messages,
  knowledgePoints,
  truncate,
  goToAi
} = useKnowledge()
</script>

<style scoped lang="scss">
@import './knowledge.scss';
</style>
