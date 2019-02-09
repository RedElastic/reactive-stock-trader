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
        v-for="(order, index) in orders" 
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
import * as portfolio from '@/common/portfolio';

export default {  
  data() {
    return {
      orders: [        
      ]
    };
  },
  mounted() {
    portfolio.getPortfolio({ includeOrders: true })
      .then(summary => {
        this.orders = summary.pendingOrders.map(order => {
          const line = {
            id: order.orderId,
            symbol: order.symbol,
            price: order.price
          };
          if (order.tradeType === 'BUY') {
            line.sharesBought = order.shares;
          } else {
            line.sharesSold = order.shares;
          }
          return line;
        });
      });
  }
} 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>