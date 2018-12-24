import Router from 'vue-router';

let routes=[
  {
    path: '/',
    component: require('@/components/pages/Portfolio.vue')
  }
];

export default new VueRouter({routes});