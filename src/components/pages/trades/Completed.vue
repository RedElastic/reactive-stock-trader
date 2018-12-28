<template>  
  <div class="row mt-5">
    <div class="col">
      <h2 class="mb-3">All Transactions</h2> 
      <table class="table">
        <thead>
          <tr>
            <th scope="col">Date</th>
            <th scope="col">Time</th>
            <th scope="col">Confirmation</th>
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
          <tr>
            <td scope="row">12/12/12</td>
            <td>11:46:01am EST</td>
            <td>XKJNFKH123</td>
            <td>Trade</td>
            <td>AAPL:NASDAQ</td>
            <td>&nbsp;</td>
            <td>50</td>
            <td>123.12 USD</td>
            <td>&nbsp;</td>
            <td>12,134</td>
            <td>134,123 USD</td>
          </tr>
        </tbody>
      </table>       
    </div>      
  </div>         
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
