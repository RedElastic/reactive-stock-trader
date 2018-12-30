<template>
  <div class="row mt-3">    
    <div class="col">
      <div :id="tickerId" style="width: 100%; height: 300px;"></div>
    </div>
  </div>    
  <!-- /stocks -->
</template>

<script>
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import am4themes_animated from "@amcharts/amcharts4/themes/animated";

  am4core.useTheme(am4themes_animated);

  export default {
    name: 'QuoteChart',
    props: ['ticker'],
    computed: {
      tickerId: function () {
        return "chartdiv_" + this.ticker;
      }
    },
    mounted() {
      // Create chart instance
      let chart = am4core.create("chartdiv_" + this.ticker, am4charts.XYChart);

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