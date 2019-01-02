<template>    
  <div class="row">
    <div class="col">
      <b-form @submit="handleSubmit" @reset="handleReset">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Place New Order</h2>          
            <div class="row">
              <div class="col">
                <b-form-group label="Buy / sell">
                  <b-form-radio-group id="order" v-model="form.order" name="order">
                    <b-form-radio value="buy">Buy</b-form-radio>
                    <b-form-radio value="sell">Sell</b-form-radio>
                  </b-form-radio-group>
                </b-form-group>
              </div>
            </div>                        
            <div class="row">
              <div class="col">
                <b-form-group id="stymbolGroup" label="Ticker Symbol" label-for="symbol">
                  <b-form-input id="symbol" type="text" v-on:input="debounceSymbolInput()" v-model="form.symbol" required></b-form-input>
                </b-form-group>   
              </div>
              <div class="col">
                <b-form-group id="sharesGroup" label="Number of shares" label-for="shares">
                  <b-form-input id="shares" type="number" v-model.number="form.shares" required></b-form-input>
                </b-form-group>            
              </div>                          
            </div>
            <div class="row">
              <div class="col">
                <b-form-group label="Order Type">
                  <b-form-radio-group id="orderType" v-model="form.orderType" name="orderType">
                    <b-form-radio value="market">Market</b-form-radio>
                    <b-form-radio value="limit">Limit</b-form-radio>
                    <b-form-radio value="stop">Stop</b-form-radio>
                    <b-form-radio value="stopLimit">Stop limit</b-form-radio>
                  </b-form-radio-group>
                </b-form-group>
              </div>             
            </div>
            <div class="row" v-if="this.stopOrder || this.limitOrder">
              <div class="col-6" v-if="this.limitOrder">
                <b-form-group id="limitPriceGroup" label="Limit price" label-for="limitPrice">
                  <b-form-input id="limitPrice" type="number" v-model.number="form.limitPrice" required></b-form-input>
                </b-form-group>            
              </div>                          
              <div class="col-6" v-if="this.stopOrder">
                <b-form-group id="stopPriceGroup" label="Stop price" label-for="stopPrice">
                  <b-form-input id="stopPrice" type="number" v-model.number="form.stopPrice" required></b-form-input>
                </b-form-group>            
              </div>                          
            </div>            
        </div>          
        <div class="col-5">          
          <div class="row">
            <div class="col">
              <h4 class="mt-5">Order Summary</h4>
            </div>
          </div>
          <div class="row">
            <div class="col">Symbol</div>
            <div class="col">{{ this.filteredCompany.symbol }}</div>
          </div>
          <div class="row">
            <div class="col">Company</div>
            <div class="col">{{ this.filteredCompany.name }}</div>
          </div>
          <div class="row">
            <div class="col">Latest Price</div>
            <div class="col">{{ quote.latestPrice | toCurrency }} </div>
          </div>
          <div class="row">
            <div class="col">Order Type</div>
            <div class="col">{{ this.formatOrderType() }}</div>
          </div>
          <div class="row">
            <div class="col">Number of Shares</div>
            <div class="col">{{ form.shares }}</div>
          </div>
          <div class="row" v-if="this.stopOrder">
            <div class="col">Stop price</div>
            <div class="col">{{ form.stopPrice | toCurrency }}</div>
          </div>
          <div class="row" v-if="this.limitOrder">
            <div class="col">Limit price</div>
            <div class="col">{{ form.limitPrice | toCurrency }}</div>
          </div>          
          <div class="row mt-3">
            <div class="col">
              <b-button type="submit" variant="primary" class="mr-3">Place Order</b-button>
              <b-button type="reset" variant="danger">Reset</b-button>
            </div>
          </div>                    
        </div>
      </div> 
      <!-- /overview -->  
      </b-form>       
    </div>
  </div>
</template>

<script>
  import {IEX} from '@/common/http.js'
  import Vue from 'vue'
  import _ from 'lodash'

  export default { 
    data() {
      return {
        submitted: false,
        form: {
          order: '',  
          symbol: '',
          shares: null,
          orderType: '',
          limitPrice: null,
          stopPrice: null
        },
        quote: {
          symbol: '',
          latestPrice: null
        },
        companies: null
      }
    },
    methods: {
      formatOrderType () {
        if (this.form.order !== null && this.form.orderType !== null) {
          if (this.form.order === "buy") {
            if (this.form.orderType === "market") {
              return "Buy"
            } else if (this.form.orderType === "limit") {
              return "Buy Limit"
            } else if (this.form.orderType === "stop") {
              return "Buy On Stop"
            } else if (this.form.orderType === "stopLimit") {
              return "Buy Stop-Limit"
            }
          } else {
            if (this.form.orderType === "market") {
              return "Sell"
            } else if (this.form.orderType === "limit") {
              return "Sell Limit"
            } else if (this.form.orderType === "stop") {
              return "Stop Loss"
            } else if (this.form.orderType === "stopLimit") {
              return "Sell Stop-Limit"
            }
          }
        }
      },
      handleSubmit() {
        this.submitted = true
      },
      handleReset() {
        this.form = {
          order: '',  
          symbol: '',
          shares: null,
          orderType: '',
          limitPrice: null,
          stopPrice: null
        }
        this.quote = {
          symbol: '',
          latestPrice: null
        }
      },
      debounceSymbolInput: _.debounce(function() { // only execute once every 1s max
        IEX.get('/stock/' + this.form.symbol + '/quote?filter=symbol,latestPrice', {timeout: 2000})
          .then(response => {
            this.quote = response.data
          })
          .catch(e => Vue.rollbar.error(e))
        }, 
      1000) 
    },
    computed: {
      filteredCompany() {
        if (this.companies !== null) {
          let idx = this.companies.findIndex(c => c.symbol === this.form.symbol)          
          if (idx > -1) {
            return {
              symbol: this.companies[idx].symbol,
              name: this.companies[idx].name
            }
          } else {
            return {
              symbol: "",
              name: ""
            } 
          }
        } else { 
          return {
            symbol: "",
            name: ""
          } 
        }
      },
      stopOrder() {
        return (this.form.orderType === "stop" || this.form.orderType === "stopLimit")
      },
      limitOrder() {
        return (this.form.orderType === "limit" || this.form.orderType === "stopLimit")
      }
    },
    mounted() {
      IEX.get('/ref-data/symbols?filter=symbol,name')
      .then(response => {
        this.companies = response.data
      })
      .catch(e => Vue.rollbar.error(e))
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
