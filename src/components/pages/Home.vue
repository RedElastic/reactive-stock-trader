<template>    
  <div class="row">
    <div class="col">  

      <!-- equities -->
      <div class="row mt-3">
        <div class="col-6">
          <h2>Create a new portfolio</h2>
          <form>
            
            <label for="nickname">Portfolio nickname</label>
            <input type="text" class="form-control" id="nickname" aria-describedby="nicknameHelp">
            <small id="nicknameHelp" class="form-text text-muted">e.g, 75/25 Portfolio Split</small>

            <button type="submit" class="btn btn-primary mt-3">Create</button>
          </form>
        </div>
        <div class="col-6">
          <h2>Choose a portfolio</h2>
          <portfolio-switcher></portfolio-switcher>
        </div>
      </div>
      <!-- /equities -->

    </div>
  </div>
</template>

<script>
  import PortfolioSwitcher from '@/components/components/portfolio/PortfolioSwitcher.vue'

  export default {
    name: 'Home',
    components: {      
      PortfolioSwitcher
    }    
  } 
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
