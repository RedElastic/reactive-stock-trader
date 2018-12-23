<template>
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>
            Portfolio
            <small class="text-muted">Conservative 60/40 Split</small>
          </h2>
          <portfolio-summary class="mb-3"></portfolio-summary>
          <portfolio-switcher></portfolio-switcher>
        </div>
        <div class="col-5">          
          <cash-summary></cash-summary>
        </div>      
      </div> 
      <!-- /overview -->

      <!-- equities -->
      <div class="row mt-3">
        <div class="col">
          <h2>Equities</h2>
        </div>
      </div>
      <equity-row ticker="aapl1"></equity-row>
      <equity-row ticker="aapl2"></equity-row>
      <equity-row ticker="aapl3"></equity-row>
      <!-- /equities -->

      <!-- transactions -->
      <div class="row mt-5">
        <div class="col">
          <h2>Transactions</h2> 
          <table class="table">
            <thead>
              <tr>
                <th scope="col">Date</th>
                <th scope="col">Time</th>
                <th scope="col">Type</th>
                <th scope="col">Asset</th>
                <th scope="col">Shares Bought</th>
                <th scope="col">Shares Sold</th>
                <th scope="col">Price</th>
                <th scope="col">Debit</th>
                <th scope="col">Credit</th>
                <th scope="col">Cash on Hand</th>
              </tr>
            </thead>
            <tbody style="font-size:0.8em;">
              <transaction-row></transaction-row>
              <transaction-row></transaction-row>
              <transaction-row></transaction-row>
            </tbody>
          </table>       
        </div>      
      </div>
      <!-- /transactions -->

    </div>
  </div>
</template>

<script>
  import PortfolioSummary from './PortfolioSummary.vue'
  import PortfolioSwitcher from './PortfolioSwitcher.vue'
  import CashSummary from './CashSummary.vue'
  import EquityRow from './EquityRow.vue'
  import TransactionRow from './TransactionRow.vue'

  export default {
    name: 'Portfolio',
    components: {      
      PortfolioSummary,
      PortfolioSwitcher,
      CashSummary,
      EquityRow,
      TransactionRow
    }    
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
