<template>    
  <!-- transactions -->
  <div class="row mt-5">
    <div class="col">
      <h2 class="mb-3">Completed Transfers</h2> 
      <table class="table">
        <thead>
          <tr>
            <th scope="col">Date/Time</th>
            <th scope="col">Type</th>
            <th scope="col">Confirmation</th>
            <th scope="col">Account</th>
            <th scope="col">Amount (USD)</th>            
          </tr>
        </thead>
        <tbody style="font-size:0.8em;">
          <tr>
            <td scope="row">12/12/12 11:46:01am EST</td>
            <td>Deposit</td>
            <td>XKJNFKH123</td>            
            <td>Chequing</td>
            <td>123.12</td>
          </tr>
        </tbody>
      </table>       
    </div>      
  </div>
  <!-- /transactions -->         
</template>

<script>
  import PortfolioSummary from '@/components/components/portfolio/PortfolioSummary.vue'
  import PortfolioSwitcher from '@/components/components/portfolio/PortfolioSwitcher.vue'
  import CashSummary from '@/components/components/portfolio/CashSummary.vue'
  import EquityRow from '@/components/components/portfolio/EquityRow.vue'

  export default {
    name: 'Portfolio',
    components: {      
      PortfolioSummary,
      PortfolioSwitcher,
      CashSummary,
      EquityRow
    }    
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
