<template>
  <div class="row mt-3">
    <div class="col-5">              
      <div class="card">
        <div class="card-body">
          <h5 class="card-title mb-3">{{ quote.companyName }}</h5>
          <h6 class="card-subtitle mb-2">{{ quote.symbol }} <span class="text-muted">({{ quote.primaryExchange }})</span></h6>
          <p class="card-text">
            <p v-bind:class="returnClass">{{ quote.latestPrice }} <span style="font-size:0.8em;" v-bind:class="returnClass">{{ quote.change }} {{ quote.changePercent }}%</span></p>            
            <div class="row">
              <div class="col-4">
                Value
              </div> 
              <div class="col-8">
                {{ this.equity.value }}
              </div>
            </div>
            <div class="row">
              <div class="col-4">
                Shares
              </div> 
              <div class="col-8">
                {{ this.equity.shares }}
              </div>
            </div>
            <div class="row mt-3">
              <div class="col-4">
                Return
              </div> 
              <div class="col-8">
                {{ this.equity.returnValue }} ({{ this.equity.returnPercent }})
              </div>
            </div>
            <div class="row">
              <div class="col-4">
                Return (24h)
              </div> 
              <div class="col-8">
                {{ this.equity.return24h }} ({{ this.equity.returnPercent24h }})
              </div>
            </div>
          </p>
          <a href="#" class="card-link">Buy</a>
          <a href="#" class="card-link">Sell</a>
          <a href="#" class="card-link">Transactions</a>
        </div>
      </div>
    </div>
    <div class="col-7">
      <quote-chart v-bind:symbol="this.equity.symbol"></quote-chart>
    </div>
  </div>    
  <!-- /stocks -->
</template>

<script>
  import QuoteChart from '@/components/components/quote/QuoteChart.vue'
  import {IEX} from '@/common/http.js'
  import Vue from 'vue'

  export default {
    name: 'EquityRow',
    props: ['equity'],
    components: {      
      QuoteChart
    },
    data: function () {
      return {
        quote: null
      }
    },
    computed: {
      returnClass: function() {
        if (this.quote.changePercent >= 0.0) {
          return 'positiveReturn';
        } else {
          return 'negativeReturn';
        }
      },
    },
    mounted() {
      IEX.get('/stock/' + this.equity.symbol + '/quote', {timeout: 2000})
        .then(response => {
          this.quote = response.data
        })
        .catch(e => Vue.rollbar.error(e))
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .positiveReturn {
    font-size: 1.2em;
    color: #27ae60;
  }

  .positiveReturnMuted {
    font-size: 0.8em;
    color: #2ecc71;
  }

  .negativeReturn {
    font-size: 1.2em;
    color: #c0392b;
  }

  .negativeReturnMuted {
    font-size: 0.8em;
    color: #e74c3c;
  }
</style>