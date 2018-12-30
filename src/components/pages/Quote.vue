<template>    
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-4">
          <h2>Get Quote</h2>
          <b-form @submit="onSubmit" @reset="onReset">                                  
            <div class="row">
              <div class="col">
                <b-form-group id="tickerGroup" label="Ticker Symbol" label-for="ticker">
                  <b-form-input id="ticker" type="text" required></b-form-input>
                </b-form-group>   
              </div>
            </div>            
            <div class="row">
              <div class="col">
                <b-button type="submit" variant="primary" class="mr-3">Get Quote</b-button>
              </div>
            </div>
          </b-form>
        </div>   
        <div class="col-8">
          <h3><span class="text-muted">Market Summary > </span>{{ quote.companyName }}</h3>      
          <p>{{ quote.symbol }} <span class="text-muted">({{ quote.primaryExchange }})</span></p>
          <p>{{ quote.latestPrice }} {{ quote.change }} {{ quote.changePercent }}%</p>
          <p class="small">Source: {{ quote.latestSource }}</p>
        </div>        
      </div>     
      <div class="row mt-5">
        <div class="col">
          <b-nav class="small" tabs>
            <b-nav-item active>1 day</b-nav-item>
            <b-nav-item>5 days</b-nav-item>
            <b-nav-item>6 months</b-nav-item>
            <b-nav-item>1 year</b-nav-item>
            <b-nav-item>5 years</b-nav-item>
            <b-nav-item>Max</b-nav-item>
          </b-nav>
        </div>
      </div>
      <quote-chart></quote-chart>
      <div class="row mt-5">
        <div class="col-6">          
          <div class="row">
            <div class="col">Open</div>
            <div class="col">{{ quote.open }}</div>
          </div>        
          <div class="row">
            <div class="col">High</div>
            <div class="col">{{ quote.high }}</div>
          </div>
          <div class="row">
            <div class="col">Low</div>
            <div class="col">{{ quote.low }}</div>
          </div>
          <div class="row">
            <div class="col">Mkt cap</div>
            <div class="col">{{ quote.marketCap }}</div>
          </div>
          <div class="row">
            <div class="col">P/E ratio</div>
            <div class="col">{{ quote.peRatio }}</div>
          </div>
        </div>
        <div class="col-6">                  
          <div class="row">
            <div class="col">Close</div>
            <div class="col">{{ quote.close }}</div>
          </div>
          <div class="row">
            <div class="col">52-wk high</div>
            <div class="col">{{ quote.week52High }}</div>
          </div>
          <div class="row">
            <div class="col">52-wk low</div>
            <div class="col">{{ quote.week52Low }}</div>
          </div>
        </div>
      </div> 
      <!-- /overview -->         
    </div>
  </div>
</template>

<script>
  import QuoteChart from '@/components/components/quote/QuoteChart.vue'
  import {IEX} from '@/common/http.js';

  export default {
    name: 'Quote',
    components: {      
      QuoteChart
    },
    data: function () {
      return {
        quote: null
      }
    },
    created() {
      IEX.get('/stock/aapl/quote')
      .then(response => {
        this.quote = response.data
      })
      .catch(e => {
        this.errors.push(e)
      })
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
