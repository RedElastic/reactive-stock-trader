<template>  
  <div class="row mt-5">
    <div class="col">
      <h2 class="mb-3">
        All Transactions
      </h2> 
      <table class="table">
        <thead>
          <tr>
            <th scope="col">
              Date
            </th>
            <th scope="col">
              Time
            </th>
            <th scope="col">
              Confirmation
            </th>
            <th scope="col">
              Type
            </th>
            <th scope="col">
              Asset
            </th>
            <th scope="col">
              Shares Bought
            </th>
            <th scope="col">
              Shares Sold
            </th>
            <th scope="col">
              Share Price
            </th>
            <th scope="col">
              Debit
            </th>
            <th scope="col">
              Credit
            </th>
            <th scope="col">
              Cash on Hand
            </th>
          </tr>
        </thead>
        <tbody style="font-size:0.8em;">
          <tr 
            v-for="order in orders"
            :key="order.id"
          >
            <td scope="row">
              {{ order.date }}
            </td>
            <td>{{ order.time }}</td>
            <td>{{ order.id | shortUUID }}</td>
            <td>Trade</td>
            <td>{{ order.symbol }}</td>
            <td>{{ order.sharesBought }}</td>
            <td>{{ order.sharesSold }}</td>
            <td>{{ order.price | toCurrency }}</td>
            <td>{{ order.debit }}</td>
            <td>{{ order.credit }}</td>
            <td>{{ order.cashOnHand }}</td>
          </tr>
        </tbody>
      </table>       
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
          this.orders = summary.completedOrders.map(order => {
            let line = {
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
