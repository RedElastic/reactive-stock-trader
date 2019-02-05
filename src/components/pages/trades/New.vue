<template>    
  <div class="row">
    <div class="col">
      <BForm
        @submit="handleSubmit"
        @reset="handleReset"
      >
        <!-- overview -->
        <div class="row mt-3">
          <div class="col-7">
            <h2>Place New Order</h2>          
            <div class="row">
              <div class="col">
                <BFormGroup label="Buy / sell">
                  <BFormRadioGroup
                    id="order"
                    v-model="form.order"
                    name="order"
                  >
                    <BFormRadio value="buy">
                      Buy
                    </BFormRadio>
                    <BFormRadio value="sell">
                      Sell
                    </BFormRadio>
                  </BFormRadioGroup>
                </BFormGroup>
              </div>
            </div>                        
            <div class="row">
              <div class="col">
                <BFormGroup
                  id="stymbolGroup"
                  label="Ticker Symbol"
                  label-for="symbol"
                >
                  <BFormInput
                    id="symbol"
                    v-model="form.symbol"
                    type="text"
                    required
                    @input="debounceSymbolInput()"
                  />
                </BFormGroup>   
              </div>
              <div class="col">
                <BFormGroup
                  id="sharesGroup"
                  label="Number of shares"
                  label-for="shares"
                >
                  <BFormInput
                    id="shares"
                    v-model.number="form.shares"
                    type="number"
                    required
                  />
                </BFormGroup>            
              </div>                          
            </div>
            <div class="row">
              <div class="col">
                <BFormGroup label="Order Type">
                  <BFormRadioGroup
                    id="orderType"
                    v-model="form.orderType"
                    name="orderType"
                  >
                    <BFormRadio value="market">
                      Market
                    </BFormRadio>
                    <BFormRadio value="limit">
                      Limit
                    </BFormRadio>
                    <BFormRadio value="stop">
                      Stop
                    </BFormRadio>
                    <BFormRadio value="stopLimit">
                      Stop limit
                    </BFormRadio>
                  </BFormRadioGroup>
                </BFormGroup>
              </div>             
            </div>
            <div
              v-if="stopOrder || limitOrder"
              class="row"
            >
              <div
                v-if="stopOrder"
                class="col-6"
              >
                <BFormGroup
                  id="stopPriceGroup"
                  label="Stop price"
                  label-for="stopPrice"
                >
                  <BFormInput
                    id="stopPrice"
                    v-model.number="form.stopPrice"
                    type="number"
                    required
                  />
                </BFormGroup>            
              </div>
              <div
                v-if="limitOrder"
                class="col-6"
              >
                <BFormGroup
                  id="limitPriceGroup"
                  label="Limit price"
                  label-for="limitPrice"
                >
                  <BFormInput
                    id="limitPrice"
                    v-model.number="form.limitPrice"
                    type="number"
                    required
                  />
                </BFormGroup>            
              </div>                                                                
            </div>            
          </div>          
          <div class="col-5">          
            <div class="row">
              <div class="col">
                <h4 class="mt-5">
                  Order Summary
                </h4>
              </div>
            </div>
            <div class="row">
              <div class="col">
                Symbol
              </div>
              <div class="col">
                {{ filteredCompany.symbol }}
              </div>
            </div>
            <div class="row">
              <div class="col">
                Company
              </div>
              <div class="col">
                {{ filteredCompany.name }}
              </div>
            </div>
            <div class="row">
              <div class="col">
                Latest Price
              </div>
              <div class="col">
                {{ quote.latestPrice | toCurrency }}
              </div>
            </div>
            <div class="row">
              <div class="col">
                Number of Shares
              </div>
              <div class="col">
                {{ form.shares }}
              </div>
            </div>
            <div class="row">
              <div class="col">
                Order Type
              </div>
              <div class="col">
                {{ formatOrderType() }}
              </div>
            </div>          
            <div
              v-if="stopOrder"
              class="row"
            >
              <div class="col">
                Stop price
              </div>
              <div class="col">
                {{ form.stopPrice | toCurrency }}
              </div>
            </div>
            <div
              v-if="limitOrder"
              class="row"
            >
              <div class="col">
                Limit price
              </div>
              <div class="col">
                {{ form.limitPrice | toCurrency }}
              </div>
            </div>          
            <div class="row mt-3">
              <div class="col">
                <BButton
                  type="submit"
                  variant="primary"
                  class="mr-3"
                >
                  Place Order
                </BButton>
                <BButton
                  type="reset"
                  variant="danger"
                >
                  Reset
                </BButton>
              </div>
            </div>                    
          </div>
        </div> 
      <!-- /overview -->  
      </BForm>       
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
          } else if (this.form.order === "sell") {
            if (this.form.orderType === "market") {
              return "Sell"
            } else if (this.form.orderType === "limit") {
              return "Sell Limit"
            } else if (this.form.orderType === "stop") {
              return "Stop Loss"
            } else if (this.form.orderType === "stopLimit") {
              return "Sell Stop-Limit"
            }
          } else {
            return "";
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
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
