import Vue from 'vue'
import VueRouter from 'vue-router';
import BootstrapVue from 'bootstrap-vue'
import App from './App.vue'

import Portfolio from './components/pages/Portfolio.vue'
import Trades from './components/pages/Trades.vue'
import Transfers from './components/pages/Transfers.vue'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.config.productionTip = false

Vue.use(VueRouter);
Vue.use(BootstrapVue);

const routes = [
  { path: '/', component: Portfolio },
  { path: '/trades', component: Trades },
  { path: '/transfers', component: Transfers }
]

const router = new VueRouter({
  routes
})

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')