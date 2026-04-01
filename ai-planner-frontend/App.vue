<script>
export default {
  globalData: {
    theme: 'dark'
  },
  onLaunch: function() {
    console.log('App Launch')
  },
  onShow: function() {
    console.log('App Show')
  },
  onHide: function() {
    console.log('App Hide')
  },
  methods: {
    toggleTheme() {
      const next = this.globalData.theme === 'dark' ? 'light' : 'dark'
      this.globalData.theme = next
      return next
    }
  }
}
</script>

<style lang="scss">
/* 每个页面公共css */
@import "uview-plus/index.scss";
</style>
