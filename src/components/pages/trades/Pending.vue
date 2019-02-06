<template>  
  <div>  
    <div class="row">
      <div class="col">  
        <h2 class="mb-3">
          Pending Orders
        </h2> 
      </div>
    </div>
    <div class="row">
      <div
        v-for="(order, index) in orders.pending" 
        :key="order.symbol"
        class="col-6"
        :order="order"
        :index="index"
      >
        <div class="card mb-4">
          <div class="card-body">
            <h5 class="card-title">
              {{ order.symbol }}
            </h5>
            <h6 class="card-subtitle mb-2 text-muted">
              {{ order.status }}
            </h6>
            <p class="card-text" />
            <div class="row">
              <div class="col">
                Confirmation
              </div>
              <div class="col">
                {{ order.id }}
              </div>
            </div> 
            <div class="row">
              <div class="col">
                Equity name
              </div>
              <div class="col">
                {{ order.company }}
              </div>
            </div>
            <div class="row">
              <div class="col">
                Trade date/time
              </div>
              <div class="col">
                {{ order.timestamp | formatDate }}
              </div>
            </div>
            <div class="row">
              <div class="col">
                Shares
              </div>
              <div class="col">
                {{ order.shares }}
              </div>
            </div>
            <div
              v-if="order.limit"
              class="row"
            >
              <div class="col">
                Limit Price
              </div>
              <div class="col">
                {{ order.limit | toCurrency }}
              </div>
            </div> 
            <div
              v-if="order.stop"
              class="row"
            >
              <div class="col">
                Stop Price
              </div>
              <div class="col">
                {{ order.stop | toCurrency }}
              </div>
            </div>             
            <b-button
              size="sm"
              variant="danger"
              class="mt-3"
            >
              Cancel
            </b-button>                     
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  

  export default {      
    data: function () {
      return {
        orders: {
          "pending": [
              {
                  "id": "XKJNFKH123",
                  "symbol": "AAPL",
                  "company": "Apple Inc.",
                  "exchange": "Nasdaq",
                  "status": "Buy pending",
                  "timestamp": 1318781876,
                  "shares": 150,
                  "limit": 150,
                  "stop": 150
              },               
              {
                  "id": "JDHBFJHSDF",
                  "symbol": "MSFT",
                  "company": "Microsoft",
                  "exchange": "Nasdaq",
                  "status": "Sell pending",
                  "timestamp": 1318781876,
                  "shares": 100
              },               
              {
                  "id": "JDHBFJHSDF",
                  "symbol": "IBM",
                  "company": "International Business Machines",
                  "exchange": "Nasdaq",
                  "status": "Sell pending",
                  "timestamp": 1318781876,
                  "shares": 100
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
