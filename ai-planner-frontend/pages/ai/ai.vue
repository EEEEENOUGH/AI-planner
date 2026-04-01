<template>
  <view class="page">
    <scroll-view
      class="chat-scroll"
      scroll-y
      :scroll-with-animation="false"
      :scroll-into-view="scrollIntoViewId"
      :upper-threshold="40"
      @scrolltoupper="loadMoreHistory"
    >
      <view class="history-status" v-if="loadingMoreHistory || historyFinished">
        <text class="history-status-text">{{ loadingMoreHistory ? '正在加载更早记录...' : '已加载全部历史' }}</text>
      </view>

      <view class="msg-row" v-for="msg in messages" :key="msg.msgId" :class="{ mine: msg.role === 'user' }">
        <view v-if="msg.role === 'assistant'" class="avatar ai-avatar"><text class="avatar-text">AI</text></view>
        <view class="bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
          <text class="msg-text">{{ formatMarkdown(msg.content) }}</text>
        </view>
        <view v-if="msg.role === 'user'" class="avatar user-avatar"><text class="avatar-text">我</text></view>
      </view>

      <view id="chat-bottom" class="chat-bottom-anchor" />
    </scroll-view>

    <view class="composer">
      <view class="input-wrap">
        <u-textarea
          v-model="inputText"
          :autoHeight="true"
          border="none"
          maxlength="1000"
          placeholder="与AI探讨你的考研规划吧！"
          placeholder-style="color: #555558; font-size: 26rpx;"
          :disabled="sending"
          @confirm="send"
        />
      </view>
      <view class="send-btn" :class="{ 'send-disabled': sending || inputText.trim().length === 0 }" @click="send">
        <text class="send-text">{{ sending ? '…' : '发送' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { useAi } from './useAi'
const {
  messages, inputText, sending, scrollIntoViewId,
  loadingMoreHistory, historyFinished,
  loadMoreHistory, formatMarkdown, send
} = useAi()
</script>

<style scoped lang="scss">
@import './ai.scss';
</style>
