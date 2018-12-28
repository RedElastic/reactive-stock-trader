<template>    
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Place New Order</h2>
          <b-form @submit="onSubmit" @reset="onReset">
            <div class="row">
              <div class="col">
                <b-form-group label="Buy / sell">
                  <b-form-radio-group id="buySellRadio" v-model="selected" name="buySellRadio">
                    <b-form-radio value="first">Buy</b-form-radio>
                    <b-form-radio value="second">Sell</b-form-radio>
                  </b-form-radio-group>
                </b-form-group>
              </div>
            </div>                        
            <div class="row">
              <div class="col">
                <b-form-group id="tickerGroup" label="Ticker Symbol" label-for="ticker">
                  <b-form-input id="ticker" type="text" required></b-form-input>
                </b-form-group>   
              </div>
              <div class="col">

              </div>
            </div>
            <div class="row">
              <div class="col-6">
                <b-form-group id="numberOfShares" label="Number of shares" label-for="numberOfShares">
                  <b-form-input id="numberOfShares" type="text" required></b-form-input>
                </b-form-group>            
              </div>                          
            </div>
            <div class="row">
              <div class="col">
                <b-form-group label="Order Type">
                  <b-form-radio-group id="orderType" v-model="selected" name="orderTypeRadio">
                    <b-form-radio value="first">Market</b-form-radio>
                    <b-form-radio value="second">Limit</b-form-radio>
                  </b-form-radio-group>
                </b-form-group>
              </div>
              <div class="col">
                <b-form-group id="numberOfShares" label="Limit price" label-for="numberOfShares">
                  <b-form-input id="numberOfShares" type="text" required></b-form-input>
                </b-form-group>    
              </div>              
            </div>
            <div class="row">
              <div class="col">
                <b-button type="submit" variant="primary" class="mr-3">Place Order</b-button>
                <b-button type="reset" variant="danger">Reset</b-button>
              </div>
            </div>
          </b-form>
        </div>          
        <div class="col-5">          
          <div class="row">
            <div class="col">
              <h4 class="mt-5">Order Summary</h4>
            </div>
          </div>
          <div class="row">
            <div class="col">Ticker</div>
            <div class="col">NASDAQ:AAPL</div>
          </div>
          <div class="row">
            <div class="col">Order Type</div>
            <div class="col">Buy (Limit)</div>
          </div>
          <div class="row">
            <div class="col">Number of Shares</div>
            <div class="col">100</div>
          </div>
          <div class="row">
            <div class="col">Limit Price</div>
            <div class="col">$150</div>
          </div>
          <div class="row mt-3">
            <div class="col">Latest Price</div>
            <div class="col">$152.12 (12:43:12pm EST)</div>
          </div>
          <div class="row">
            <div class="col">Estimated Total</div>
            <div class="col">$15200.12</div>
          </div>
          <div class="row mt-3">
            <div class="col">Cash on Hand</div>
            <div class="col">$15200.12</div>
          </div>
        </div>
      </div> 
      <!-- /overview -->         
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
