import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

// 关闭生产提示
Vue.config.productionTip = false

// 使用Element UI
Vue.use(ElementUI)

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
