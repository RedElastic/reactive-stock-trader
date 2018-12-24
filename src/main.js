import Vue from 'vue'
import VueRouter from 'vue-router';
import App from './App.vue'
import Portfolio from './components/pages/Portfolio.vue'

Vue.config.productionTip = false

Vue.use( VueRouter );

const routes = [
  { path: '/', component: Portfolio }
]

const router = new VueRouter({
  routes
})

const app = new Vue({
  router,
  render: h => h(App)
}).$mount('#app')