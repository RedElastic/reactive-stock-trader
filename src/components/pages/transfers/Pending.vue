<template>  
  <div>  
    <div class="row">
      <div class="col">  
        <h2 class="mb-3">Pending Transfers</h2> 
      </div>
    </div>
    <div class="row">
      <div class="col-6">
        <div class="card">
          <div class="card-body">
            <h5 class="card-title">Deposit</h5>
            <p class="card-text">              
              <div class="row">
                <div class="col">
                  Status
                </div>
                <div class="col">
                  Request Submitted
                </div>
              </div>
              <div class="row">
                <div class="col">
                  Account
                </div>
                <div class="col">
                  Chequing
                </div>
              </div>
              <div class="row">
                <div class="col">
                  Date/time
                </div>
                <div class="col">
                  12/12/2018 12:13:12pm
                </div>
              </div>
              <div class="row">
                <div class="col">
                  Amount (USD)
                </div>
                <div class="col">
                  $150,000
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
