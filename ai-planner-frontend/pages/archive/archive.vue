<template>
  <view class="page">
    <view class="banner">
      <view class="banner-left">
        <text class="banner-num">{{ daysUntilExam }}</text>
        <text class="banner-unit">天</text>
      </view>
      <view class="banner-right">
        <text class="banner-label">距离考研还有</text>
        <text class="banner-info">{{ form.targetInstitution || '——' }} · {{ form.targetMajor || '——' }}</text>
      </view>
      <text class="banner-emoji">🎯</text>
    </view>

    <view class="card">
      <view class="card-header"><view class="card-dot" /><text class="card-title">备考基本信息</text></view>
      <view class="field">
        <view class="label-row"><text class="label">目标院校</text><text class="req">*</text></view>
        <input class="fi" v-model="form.targetInstitution" placeholder="如：北京大学" placeholder-class="ph" />
      </view>
      <view class="field">
        <view class="label-row"><text class="label">目标专业</text><text class="req">*</text></view>
        <input class="fi" v-model="form.targetMajor" placeholder="如：计算机科学与技术" placeholder-class="ph" />
      </view>
      <view class="field">
        <view class="label-row"><text class="label">预计考试日期</text><text class="req">*</text></view>
        <input class="fi" v-model="form.examDate" placeholder="格式：2026-12-27" placeholder-class="ph" />
      </view>
      <view class="field">
        <text class="label">考试科目</text>
        <input class="fi" v-model="form.examSubjects" placeholder="如：政治,英语一,数学一,408" placeholder-class="ph" />
        <text class="hint">多科目用逗号分隔</text>
      </view>
      <view class="field">
        <text class="label">每日复习时长</text>
        <view class="duration-row">
          <u-number-box v-model="form.dailyStudyDuration" :min="1" :max="16" :step="1" input-width="80" />
          <text class="duration-label">小时 / 天</text>
        </view>
      </view>
    </view>

    <view class="card">
      <view class="card-header"><view class="card-dot dot-amber" /><text class="card-title">科目掌握情况</text></view>
      <view class="field">
        <text class="label">薄弱与擅长科目说明</text>
        <u-textarea v-model="form.subjectMastery" placeholder="如：数学基础薄弱，英语阅读较好，写作需加强。" :autoHeight="true" :height="120" border="none" maxlength="500" count />
        <text class="hint">AI 将根据此描述调整复习计划侧重点</text>
      </view>
    </view>

    <view class="card">
      <view class="card-header"><view class="card-dot dot-green" /><text class="card-title">档案名称（选填）</text></view>
      <input class="fi" v-model="form.archiveName" placeholder="默认：我的考研档案" placeholder-class="ph" />
    </view>

    <view class="submit-btn" :class="{ loading: saving }" @click="submit">
      <text class="submit-text">{{ saving ? '保存中...' : (hasArchive ? '保存修改' : '创建档案') }}</text>
    </view>
  </view>
</template>

<script setup>
import { useArchive } from './useArchive'
const { saving, hasArchive, form, daysUntilExam, submit } = useArchive()
</script>

<style scoped lang="scss">
@import './archive.scss';
</style>
