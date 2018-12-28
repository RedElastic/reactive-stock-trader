<template>  
  <div>  
    <div class="row">
      <div class="col">  
        <h2 class="mb-3">Pending Orders</h2> 
      </div>
    </div>
    <div class="row">
      <div class="col-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">NASDAQ:AAPL</h5>
            <h6 class="card-subtitle mb-2 text-muted">Buy Pending</h6>
            <p class="card-text">
              <div class="row">
                <div class="col">
                  Equity name
                </div>
                <div class="col">
                  Apple Inc.
                </div>
              </div>
              <div class="row">
                <div class="col">
                  Trade date/time
                </div>
                <div class="col">
                  12/12/2018 12:13:12pm
                </div>
              </div>
              <div class="row">
                <div class="col">
                  Shares
                </div>
                <div class="col">
                  150
                </div>
              </div>
              <div class="row">
                <div class="col">
                  Limit Price
                </div>
                <div class="col">
                  15012
                </div>
              </div> 
              <div class="row">
                <div class="col">
                  Confirmation
                </div>
                <div class="col">
                  XKJNFKH123
                </div>
              </div> 
              <b-button size="sm" variant="danger" class="mt-3">
                Cancel
              </b-button>             
            </p>            
          </div>
        </div>
      </div>
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
