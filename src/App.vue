<template>
  <div class="container">
    <main-nav></main-nav>  
    <portfolio></portfolio>
    <main-footer></main-footer>  
  </div>
</template>

<script>
  import MainNav from './components/layout/MainNav.vue'
  import MainFooter from './components/layout/MainFooter.vue'
  import Portfolio from './components/portfolio/Portfolio.vue'

  export default {
    name: 'app',
    components: {
      MainNav,
      Portfolio,
      MainFooter    
    }
  }
</script>

<style>
</style>