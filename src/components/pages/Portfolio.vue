<template>    
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Portfolio <small class="text-muted">Conservative 60/40 Split</small></h2>
          <portfolio-summary class="mb-3"></portfolio-summary>          
        </div>
        <div class="col-5">          
          <cash-summary></cash-summary>        
        </div>      
      </div>
      <!-- /overview -->

      <!-- equities -->
      <div class="row mt-3">
        <div class="col">
          <b-nav class="small" tabs>
            <b-nav-item active>1 day</b-nav-item>
            <b-nav-item>5 days</b-nav-item>
            <b-nav-item>6 months</b-nav-item>
            <b-nav-item>1 year</b-nav-item>
            <b-nav-item>5 years</b-nav-item>
            <b-nav-item>Max</b-nav-item>
          </b-nav>
        </div>
      </div>
      <equity-row ticker="aapl1"></equity-row>
      <equity-row ticker="aapl2"></equity-row>
      <equity-row ticker="aapl3"></equity-row>
      <!-- /equities -->      

    </div>
  </div>
</template>

<script>
  import PortfolioSummary from '@/components/components/portfolio/PortfolioSummary.vue'
  import CashSummary from '@/components/components/portfolio/CashSummary.vue'
  import EquityRow from '@/components/components/portfolio/EquityRow.vue'

  export default {
    name: 'Portfolio',
    components: {      
      PortfolioSummary,
      CashSummary,
      EquityRow
    }    
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
