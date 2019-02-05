<template>    
  <div class="row">
    <div class="col">
      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>
            Portfolio <small class="text-muted">
              {{ portfolio.name }}
            </small>
          </h2>
          <div class="row">
            <div class="col">
              <div class="row">
                <div class="col-6">
                  Value (USD)
                </div> 
                <div class="col-6">
                  {{ portfolio.value | toCurrency }}
                </div>
              </div>        
              <div class="row">
                <div class="col-6">
                  Return
                </div> 
                <div class="col-6">
                  {{ portfolio.returnValue | toCurrency }} ({{ portfolio.returnPercent | iexPercent }})
                </div>
              </div>
              <div class="row">
                <div class="col-6">
                  Return (24h)
                </div> 
                <div class="col-6">
                  {{ portfolio.return24h | toCurrency }} ({{ portfolio.returnPercent24h | iexPercent }})
                </div>
              </div>
            </div> 
          </div>
        </div>
        <div class="col-5">          
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">
                Cash on Hand
              </h5>
              <h6 class="card-subtitle mb-2 text-muted">
                {{ portfolio.cashOnHand | toCurrency }}
              </h6>
              <p class="card-text">
                <router-link to="/transfers">
                  Transfer
                </router-link>
              </p>            
            </div>
          </div>     
        </div>      
      </div>
      <!-- /overview -->

      <equity-row
        v-for="(equity, index) in portfolio.equities"
        :key="equity.symbol"
        :equity="equity"
        :index="index"
      />
      <!-- /equities -->
    </div>
  </div>
</template>

<script>
  import EquityRow from '@/components/components/portfolio/EquityRow.vue'

  export default {
    name: 'Portfolio',
    components: {      
      EquityRow
    },
    data: function () {
      return {
        portfolio: {
          "name": "Conservative 59/41 Split",
          "value": 135122,
          "returnValue": 22132,
          "returnPercent": 0.1203,
          "return24h": -234,
          "returnPercent24h": -0.0122,
          "cashOnHand": 1245,
          "equities": [
              {
                  "symbol": "AAPL",
                  "shares": 135,
                  "value": 135123,
                  "returnValue": -20123,
                  "returnPercent": -0.1234,
                  "return24h": -20123,
                  "returnPercent24h": -0.1234
              },
              {
                  "symbol": "MSFT",
                  "shares": 135,
                  "value": 135123,
                  "returnValue": -20123,
                  "returnPercent": -0.1234,
                  "return24h": -20123,
                  "returnPercent24h": -0.1234
              },
              {
                  "symbol": "GOOGL",
                  "shares": 135,
                  "value": 135123,
                  "returnValue": -20123,
                  "returnPercent": -0.1234,
                  "return24h": -20123,
                  "returnPercent24h": -0.1234
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
