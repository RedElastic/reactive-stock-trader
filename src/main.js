import Vue from 'vue'
import VueRouter from 'vue-router';
import BootstrapVue from 'bootstrap-vue'
import App from './App.vue'
import Rollbar from 'vue-rollbar';

import Portfolio from './components/pages/Portfolio.vue'
import Home from './components/pages/Home.vue'
import Quote from './components/pages/Quote.vue'

import NewTrade from './components/pages/trades/New.vue'
import PendingTrades from './components/pages/trades/Pending.vue'
import CompletedTrades from './components/pages/trades/Completed.vue'

import NewTransfer from './components/pages/transfers/New.vue'
import PendingTransfers from './components/pages/transfers/Pending.vue'
import CompletedTransfers from './components/pages/transfers/Completed.vue'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.config.productionTip = false

Vue.use(VueRouter);
Vue.use(BootstrapVue);
Vue.use(Rollbar, {
     accessToken: "327c918327104609a437cf7dc5626754",
     captureUncaught: true,
     captureUnhandledRejections: true,
     enabled: true,
     source_map_enabled: true,
     environment: 'production',
     payload: {
       client: {
            javascript: {
               code_version: '1.0'
            }
       }
     }
});

const routes = [
  { path: '/portfolio', component: Portfolio },
  { path: '/quote', component: Quote },
  { path: '/trades/new', component: NewTrade },
  { path: '/trades/pending', component: PendingTrades },
  { path: '/trades/completed', component: CompletedTrades },
  { path: '/transfers/new', component: NewTransfer },
  { path: '/transfers/pending', component: PendingTransfers },
  { path: '/transfers/completed', component: CompletedTransfers },
  { path: '/', component: Home }
]

const router = new VueRouter({
  routes
})

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')