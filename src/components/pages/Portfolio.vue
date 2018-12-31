<template>    
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Portfolio <small class="text-muted">{{ this.portfolio.name }}</small></h2>
          <div class="row">
            <div class="col">
              <div class="row">
                <div class="col-6">
                  Value (USD)
                </div> 
                <div class="col-6">
                  {{ this.portfolio.value }}
                </div>
              </div>        
              <div class="row">
                <div class="col-6">
                  Return
                </div> 
                <div class="col-6">
                  {{ this.portfolio.returnValue }} ({{ this.portfolio.returnPercent }})
                </div>
              </div>
              <div class="row">
                <div class="col-6">
                  Return (24h)
                </div> 
                <div class="col-6">
                  {{ this.portfolio.return24h }} ({{ this.portfolio.returnPercent24h }})
                </div>
              </div>
            </div> 
          </div>
        </div>
        <div class="col-5">          
          <cash-summary></cash-summary>        
        </div>      
      </div>
      <!-- /overview -->

      <equity-row
        v-for="(equity, index) in this.portfolio.equities"
        v-bind:equity="equity"
        v-bind:index="index"
        v-bind:key="equity.symbol">
      </equity-row>
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
    },
    data: function () {
      return {
        portfolio: {
          "name": "Conservative 59/41 Split",
          "value": 135122,
          "returnValue": 22132,
          "returnPercent": 12.03,
          "return24h": -234,
          "returnPercent24h": -0.12,
          "cashOnHand": 1245,
          "equities": [
              {
                  "symbol": "AAPL",
                  "shares": 135,
                  "value": 135123,
                  "returnValue": -20123,
                  "returnPercent": -12.03,
                  "return24h": -20123,
                  "returnPercent24h": -12.03
              },
              {
                  "symbol": "MSFT",
                  "shares": 135,
                  "value": 135123,
                  "returnValue": -20123,
                  "returnPercent": -12.03,
                  "return24h": -20123,
                  "returnPercent24h": -12.03
              },
              {
                  "symbol": "GOOGL",
                  "shares": 135,
                  "value": 135123,
                  "returnValue": -20123,
                  "returnPercent": -12.03,
                  "return24h": -20123,
                  "returnPercent24h": -12.03
              }
          ],
          "version":1.0
        }
      }
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
