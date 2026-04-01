<template>
  <view class="tabbar-wrapper">
    <view class="tabbar">
      <view
        v-for="(item, index) in list"
        :key="index"
        class="tab-item"
        @tap="switchTab(index, item.pagePath)"
      >
        <view :class="['tab-inner', current === index ? 'active' : 'inactive']">
          <image
            :src="current === index ? item.activeIcon : item.icon"
            class="tab-icon"
            mode="aspectFit"
          />
          <text class="tab-text">{{ item.text }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  props: {
    current: { type: Number, default: 0 }
  },
  data() {
    return {
      list: [
        { text: '今日',   icon: '/static/icons/home.svg',        activeIcon: '/static/icons/home-active.svg',        pagePath: '/pages/index/index' },
        { text: '规划',   icon: '/static/icons/plan.svg',        activeIcon: '/static/icons/plan-active.svg',        pagePath: '/pages/plan/plan' },
        { text: '答疑',   icon: '/static/icons/knowledge.svg',   activeIcon: '/static/icons/knowledge-active.svg',   pagePath: '/pages/knowledge/knowledge' },
        { text: '进度',   icon: '/static/icons/progress.svg',    activeIcon: '/static/icons/progress-active.svg',    pagePath: '/pages/progress/progress' },
        { text: '我的',   icon: '/static/icons/my.svg',          activeIcon: '/static/icons/my-active.svg',          pagePath: '/pages/my/my' }
      ]
    }
  },
  methods: {
    switchTab(index, path) {
      if (this.current !== index) uni.switchTab({ url: path })
    }
  }
}
</script>

<style scoped>
.tabbar-wrapper {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom);
  background: transparent;
  pointer-events: none;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  box-sizing: border-box;
}

.tabbar {
  pointer-events: auto;
  width: calc(100% - 80rpx);
  height: 112rpx;
  margin: 0 40rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 12rpx;
  border-radius: 56rpx;
  background-color: #161618;
  border: 1rpx solid rgba(255,255,255,0.08);
  box-shadow: 0 8rpx 40rpx rgba(0,0,0,0.6);
}

.tab-item {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

/* 未选中状态：保持不变 */
.tab-inner.inactive {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}

.tab-inner.inactive .tab-icon {
  width: 44rpx;
  height: 44rpx;
  opacity: 0.35;
}

.tab-inner.inactive .tab-text {
  font-size: 20rpx;
  color: #555558;
  font-family: 'PingFang SC', sans-serif;
  font-weight: 400;
}

/* 选中状态：修改为垂直排列，统一尺寸 */
.tab-inner.active {
  display: flex;
  flex-direction: column; /* 核心修改：改为垂直排列 */
  align-items: center;
  justify-content: center;
  background-color: #D9F02C;
  padding: 12rpx 36rpx; /* 调整内边距，形成垂直的圆角矩形 */
  border-radius: 36rpx;
  gap: 6rpx;
  box-shadow: 0 4rpx 16rpx rgba(217,240,44,0.3);
}

.tab-inner.active .tab-icon {
  width: 44rpx; /* 核心修改：大小和未选中状态保持一致，防止跳动 */
  height: 44rpx;
  opacity: 1;
}

.tab-inner.active .tab-text {
  font-size: 20rpx; /* 核心修改：字号和未选中状态保持一致 */
  font-weight: 700;
  color: #000000;
  font-family: 'PingFang SC', sans-serif;
}
</style>