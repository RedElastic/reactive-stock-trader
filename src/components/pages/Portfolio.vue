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
