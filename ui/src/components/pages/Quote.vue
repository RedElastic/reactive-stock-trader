<template>    
  <div class="row">
    <div class="col">
      <!-- overview -->
      <div class="row mt-3">
        <div class="col-4">
          <h2>Get Quote</h2>
          <b-form @submit.prevent="getQuote">                                  
            <div class="row">
              <div class="col">
                <b-form-group
                  id="symbolGroup"
                  label="Symbol"
                  label-for="symbol"
                >
                  <b-form-input
                    id="symbol"
                    v-model="symbol"
                    type="text"
                    required
                  />
                </b-form-group>   
              </div>
            </div>            
            <div class="row">
              <div class="col">
                <b-button
                  type="submit"
                  variant="primary"
                  class="mr-3"
                >
                  Get Quote
                </b-button>
              </div>
            </div>
          </b-form>
        </div>   
        <div
          v-if="quote !== null"
          class="col-8"
        >
          <h3 class="mb-3">
            <span class="text-muted">
              Market Summary >
            </span>{{ quote.companyName }}
          </h3>      
          <p>
            {{ quote.symbol }} <span class="text-muted">
              ({{ quote.primaryExchange }})
            </span>
          </p>
          <p
            style="font-size:1.5em;"
            :class="returnClass"
          >
            {{ quote.latestPrice | toCurrency }} <span
              style="font-size:0.8em;"
              :class="returnClass"
            >
              {{ quote.change | toCurrency }} {{ quote.changePercent | iexPercent }}%
            </span>
          </p>
          <p class="small">
            Source: {{ quote.latestSource }}
          </p>
        </div>        
      </div>           
      <quote-chart
        v-if="quote !== null"
        :symbol="quote.symbol"
      />
      <div
        v-if="quote !== null"
        class="row mt-5"
      >
        <div class="col-6">          
          <div class="row">
            <div class="col">
              Open
            </div>
            <div class="col">
              {{ quote.open }}
            </div>
          </div>        
          <div class="row">
            <div class="col">
              High
            </div>
            <div class="col">
              {{ quote.high }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              Low
            </div>
            <div class="col">
              {{ quote.low }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              Mkt cap
            </div>
            <div class="col">
              {{ quote.marketCap }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              P/E ratio
            </div>
            <div class="col">
              {{ quote.peRatio }}
            </div>
          </div>
        </div>
        <div class="col-6">                  
          <div class="row">
            <div class="col">
              Close
            </div>
            <div class="col">
              {{ quote.close }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              52-wk high
            </div>
            <div class="col">
              {{ quote.week52High }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              52-wk low
            </div>
            <div class="col">
              {{ quote.week52Low }}
            </div>
          </div>
        </div>
      </div> 
      <!-- /overview -->         
    </div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import QuoteChart from '@/components/components/quote/QuoteChart.vue'
  import {IEX} from '@/common/http.js'

  export default {
    name: 'Quote',
    components: {      
      QuoteChart
    },
    data: function () {
      return {
        quote: null,
        symbol: null
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
      returnClassMuted: function() {
        if (this.quote.changePercent >= 0.0) {
          return 'positiveReturnMuted';
        } else {
          return 'negativeReturnMuted';
        }
      }
    },
    beforeDestroy() {
      this.quote = null
      this.symbol = null
    },
    methods: {
      getQuote() {
        if (this.symbol !== null) {
          IEX.get('/stock/' + this.symbol + '/quote', {timeout: 2000})
          .then(response => {
            this.quote = response.data
          })
          .catch(e => Vue.rollbar.error(e))
        }
      }
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .positiveReturn {
    font-size: 1.5em;
    color: #27ae60;
  }

  .positiveReturnMuted {
    font-size: 0.8em;
    color: #2ecc71;
  }

  .negativeReturn {
    font-size: 1.5em;
    color: #c0392b;
  }

  .negativeReturnMuted {
    font-size: 0.8em;
    color: #e74c3c;
  }
</style>