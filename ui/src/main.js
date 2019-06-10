import Vue from 'vue'
import VueRouter from 'vue-router';
import BootstrapVue from 'bootstrap-vue'
import App from './App.vue'
import Rollbar from 'vue-rollbar';
import moment from 'moment'

import Portfolio from './components/pages/Portfolio.vue'
import Home from './components/pages/Home.vue'
import Quote from './components/pages/Quote.vue'

import NewTrade from './components/pages/trades/New.vue'

import NewTransfer from './components/pages/transfers/New.vue'

import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'

Vue.config.productionTip = false

Vue.use(VueRouter);
Vue.use(BootstrapVue);
Vue.use(Rollbar, {
     accessToken: process.env.VUE_APP_ROLLBAR_ACCESS_TOKEN,
     captureUncaught: true,
     captureUnhandledRejections: true,
     enabled: true,
     source_map_enabled: true,
     environment: process.env.NODE_ENV,
     payload: {
       client: {
            javascript: {
               code_version: '1.0'
            }
       }
     }
});

Vue.filter('toCurrency', function (value) {
    var v = value;
    if (typeof value !== "number") {
        v = parseInt(value);
    }
    var formatter = new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
        minimumFractionDigits: 2
    });
    return formatter.format(value);
});

Vue.filter('iexPercent', function (value) {
    if (typeof value !== "number") {
        return value;
    }
    return (value * 100).toFixed(2);
});

Vue.filter('formatDate', function(value) {
  if (value) {
    return moment.unix(String(value)).format('MM/DD/YYYY hh:mm')
  }
});

Vue.filter('shortUUID', function(value) {
  if (typeof value === "string") {
    return value.substring(0,8);
  }
});

const routes = [
  { path: '/portfolio', component: Portfolio },
  { path: '/quote', component: Quote },
  { path: '/trades/new', component: NewTrade },
  { path: '/transfers/new', component: NewTransfer },
  { path: '/', component: Home }
]

const router = new VueRouter({
  routes
})

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')