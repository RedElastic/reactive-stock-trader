<template>
  <div class="container">
    <!-- navigation -->
    <MainNav />      
    <!-- route outlet -->
    <!-- component matched by the route will render here -->
    <RouterView />
    <!-- footer -->
    <MainFooter />  
  </div>
</template>

<script>
  import MainNav from '@/components/layout/MainNav.vue'
  import MainFooter from '@/components/layout/MainFooter.vue'

  export default {
    name: 'App',
    components: {
      MainNav,
      MainFooter    
    }
  }
</script>

<style>
</style>