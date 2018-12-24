<template>
  <div class="container">
    <!-- navigation -->
    <main-nav></main-nav>      
    <!-- route outlet -->
    <!-- component matched by the route will render here -->
    <router-view></router-view>
    <!-- footer -->
    <main-footer></main-footer>  
  </div>
</template>

<script>
  import MainNav from '@/components/layout/MainNav.vue'
  import MainFooter from '@/components/layout/MainFooter.vue'

  export default {
    name: 'app',
    components: {
      MainNav,
      MainFooter    
    }
  }
</script>

<style>
</style>