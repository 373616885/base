// loadingMixin.js
export default {
  data() {
    return {
      showLoading: false
    }
  },
  methods: {
    showLoadingFun() {
      this.showLoading = true
    },
    hideLoadingFun() {
      this.showLoading = false
    }
  }
}
