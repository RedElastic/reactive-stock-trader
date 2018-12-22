<template>
  <div class="row">
    <div class="col">  
      <div class="row mt-3">
        <div class="col-7">
          <h2>
            Portfolio
            <small class="text-muted">Conservative 60/40 Split</small>
          </h2>
        </div>
        <div class="col-5">
          <portfolio-switcher></portfolio-switcher>
        </div>      
      </div>
      <div class="row">
        <div class="col-6">
          <portfolio-summary></portfolio-summary>
        </div> 
        <div class="col-6">
          <cash-summary></cash-summary>
        </div>    
      </div>   

      <div class="row mt-3">
        <div class="col">
          <h2>Equities</h2>
        </div>
      </div>

      <!-- stocks -->
      <div class="row mt-3">
        <div class="col-5">              
          <div class="card">
            <div class="card-body">
              <h5 class="card-title">Apple Inc.</h5>
              <h6 class="card-subtitle mb-2 text-muted">NASDAQ: AAPL</h6>
              <p class="card-text">
                151.32 USD −5.51 (3.51%)<br/>
                <small>Dec. 21 (3:49 pm EST)</small>
                <div class="row">
                  <div class="col-4">
                    Value
                  </div> 
                  <div class="col-8">
                    135,123 USD -20,123 (-12.03%)
                  </div>
                </div>
                <div class="row">
                  <div class="col-4">
                    Shares
                  </div> 
                  <div class="col-8">
                    135
                  </div>
                </div>
                <div class="row mt-3">
                  <div class="col-4">
                    24h gain/loss
                  </div> 
                  <div class="col-8">
                    -204 USD (-12.01%)
                  </div>
                </div>
                <div class="row">
                  <div class="col-4">
                    Total gain/loss
                  </div> 
                  <div class="col-8">
                    -10,123 USD (-23.01%)
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
          <div id="chartdiv" style="width: 100%; height: 300px;"></div>
        </div>
      </div>    
      <!-- /stocks -->

      <!-- transactions -->
      <div class="row mt-5">
        <div class="col">
          <h2>Transactions</h2> 
          <table class="table">
            <thead>
              <tr>
                <th scope="col">Date</th>
                <th scope="col">Time</th>
                <th scope="col">Type</th>
                <th scope="col">Asset</th>
                <th scope="col">Shares Bought</th>
                <th scope="col">Shares Sold</th>
                <th scope="col">Price</th>
                <th scope="col">Debit</th>
                <th scope="col">Credit</th>
                <th scope="col">Cash on Hand</th>
              </tr>
            </thead>
            <tbody style="font-size:0.8em;">
              <tr>
                <th scope="row">12/12/12</th>
                <td>11:46:01am EST</td>
                <td>Trade</td>
                <td>AAPL:NASDAQ</td>
                <td>&nbsp;</td>
                <td>50</td>
                <td>123.12 USD</td>
                <td>&nbsp;</td>
                <td>12,134</td>
                <td>134,123 USD</td>
              </tr>
              <tr>
                <th scope="row">12/12/12</th>
                <td>11:46:01am EST</td>
                <td>Wire In</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>15,000 USD</td>
                <td>&nbsp;</td>
                <td>134,123 USD</td>
              </tr>
              <tr>
                <th scope="row">12/12/12</th>
                <td>11:46:01am EST</td>
                <td>Wire Out</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>              
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>10,000 USD</td>
                <td>134,123 USD</td>
              </tr>
            </tbody>
          </table>       
        </div>      
      </div>
    </div>
  </div>
  <!-- /transactions -->
</template>

<script>
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import am4themes_animated from "@amcharts/amcharts4/themes/animated";

  import PortfolioSummary from './PortfolioSummary.vue'
  import PortfolioSwitcher from './PortfolioSwitcher.vue'
  import CashSummary from './CashSummary.vue'

  am4core.useTheme(am4themes_animated);

  export default {
    name: 'Portfolio',
    components: {      
      PortfolioSummary,
      PortfolioSwitcher,
      CashSummary
    },
    mounted() {
      // Create chart instance
      let chart = am4core.create("chartdiv", am4charts.XYChart);

      // Add data
      chart.data = [{
        "time": "9:00",
        "price": 135.01
      }, {
        "time": "9:15",
        "price": 135.50
      }, {
        "time": "9:30",
        "price": 136.25
      }, {
        "time": "9:45",
        "price": 136.24
      }, {
        "time": "10:00",
        "price": 135.01
      }, {
        "time": "10:15",
        "price": 135.50
      }, {
        "time": "10:30",
        "price": 135.24
      }, {
        "time": "10:45",
        "price": 135.23
      }, {
        "time": "11:00",
        "price": 132.25
      }, {
        "time": "11:15",
        "price": 132.26
      }, {
        "time": "11:30",
        "price": 132.27
      }, {
        "time": "11:45",
        "price": 132.75
      }, {
        "time": "12:00",
        "price": 133.21
      }, {
        "time": "12:15",
        "price": 133.75
      }, {
        "time": "12:30",
        "price": 134.00
      }, {
        "time": "12:45",
        "price": 134.11
      }, {
        "time": "1:00",
        "price": 134.12
      }, {
        "time": "1:15",
        "price": 134.07
      }, {
        "time": "1:30",
        "price": 134.05
      }, {
        "time": "1:45",
        "price": 134.04
      }, {
        "time": "2:00",
        "price": 134.04
      }, {
        "time": "2:15",
        "price": 134.01
      }, {
        "time": "2:30",
        "price": 133.25
      }, {
        "time": "2:45",
        "price": 133.21
      }, {
        "time": "3:00",
        "price": 132.76
      }, {
        "time": "3:15",
        "price": 132.25
      }, {
        "time": "3:30",
        "price": 131.98
      }, {
        "time": "3:45",
        "price": 132.02
      }, {
        "time": "4:00",
        "price": 132.03
      }, {
        "time": "4:15",
        "price": 132.05
      }, {
        "time": "4:30",
        "price": 132.04
      }, {
        "time": "4:45",
        "price": 132.01
      }];

      // Create axes
      let categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
      categoryAxis.dataFields.category = "time";
      categoryAxis.title.text = "24hr";

      let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
      valueAxis.title.text = "Price (USD)";

      let series = chart.series.push(new am4charts.LineSeries());
      series.name = "Price";
      series.stroke = am4core.color("#CDA2AB");
      series.strokeWidth = 3;
      series.dataFields.valueY = "price";
      series.dataFields.categoryX = "time";
      series.tensionX = 0.8;
      series.tensionY = 1.0;

      this.chart = chart;
    },
    beforeDestroy() {
      if (this.chart) {
        this.chart.dispose();
      }
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
