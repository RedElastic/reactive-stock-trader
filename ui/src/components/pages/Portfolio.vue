<template>    
  <div class="row">
    <div class="col">
      <div class="row mt-3">
        <div class="col-7">
          <h2>
            Portfolio <small class="text-muted">
              {{ portfolioName }}
            </small>
          </h2>
          <div class="row">
            <div class="col">
              Account: {{ portfolioId }}
            </div>
          </div>
        </div>
      </div>
      <div class="row mt-3">
        <div class="col-7">
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
      <equity-row
        v-for="(equity, index) in portfolio.equities"
        :key="equity.symbol"
        :equity="equity"
        :index="index"
      />
    </div>
  </div>
</template>

<script>
  import EquityRow from '@/components/components/portfolio/EquityRow.vue';
  import * as portfolioService from '@/common/portfolio';
  
  const dummyPortfolioData = {
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
  
  const emptyPortfolio = {
    "name": "",    
    "value": null,
    "returnValue": null,
    "returnPercent": null,
    "return24h": null,
    "returnPercent24h": null,
    "cashOnHand": null,
    "equities": [],
    "version":1.0
  }

  export default {
    name: 'Portfolio',
    components: {      
      EquityRow
    },
    data: function () {
      return {
        portfolio: Object.assign({}, emptyPortfolio)
      }
    },
    computed: {
      portfolioId() {
        return portfolioService.activePortfolio.id;
      },
      portfolioName() {
        return portfolioService.activePortfolio.name;
      }
    },
    mounted() {
      portfolioService.getDetails()
        .then(details => {          
          this.portfolio.name = details.name;
          this.portfolio.cashOnHand = details.funds;
          let equities = details.holdings.map(holding => ({
            symbol: holding.symbol,
            shares: holding.shareCount,
            value: holding.marketValue,
            returnValue: null,
            returnPercent: null,
            return24h: null,
            returnPercent24h: null
          }));
          this.portfolio.equities = equities;
          let equitiesValue = equities.reduce((acc,eq) => acc + eq.value, 0);
          this.portfolio.value = this.portfolio.cashOnHand + equitiesValue;
          this.portfolio.returnValue = null;
          this.portfolio.returnPercent = null;
          this.portfolio.returnPercent = null;
          this.portfolio.returnPercent24h = null;
        });
      
    },
    methods: {
      update() {
                
      }
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
