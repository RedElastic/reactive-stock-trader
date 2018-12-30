<template>
  <div class="row mt-3">    
    <div class="col">
      <b-nav class="small my-3" tabs>
        <b-nav-item>1 day</b-nav-item>
        <b-nav-item>5 days</b-nav-item>
        <b-nav-item active>1 month</b-nav-item>
        <b-nav-item>6 months</b-nav-item>
        <b-nav-item>1 year</b-nav-item>
        <b-nav-item>5 years</b-nav-item>
        <b-nav-item>Max</b-nav-item>
      </b-nav>
      <div :id="this.chartId" style="width: 100%; height: 300px;"></div>
    </div>
  </div>    
  <!-- /stocks -->
</template>

<script>
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import am4themes_animated from "@amcharts/amcharts4/themes/animated";
  import {IEX} from '@/common/http.js';

  am4core.useTheme(am4themes_animated);

  export default {
    name: 'QuoteChart',
    props: ['symbol'],
    data: function () {
      return {
        chartId: "chart_" + this.symbol
      }
    },
    watch: {
      // whenever question changes, this function will run
      symbol: function (newSymbol, oldSymbol) {
        if (this.chart) {
          IEX.get('/stock/' + newSymbol + '/chart?filter=close,date')
            .then(response => {
              this.chart.data = response.data
            })
            .catch(e => {
              this.errors.push(e)
            }
          )          
        }
      }
    },
    mounted() {
      // Create chart instance
      let chart = am4core.create(this.chartId, am4charts.XYChart);

      IEX.get('/stock/' + this.symbol + '/chart?filter=close,date')
        .then(response => {
          chart.data = response.data
        })
        .catch(e => {
          this.errors.push(e)
        }
      )

      // Create axes
      let categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
      categoryAxis.dataFields.category = "date";
      categoryAxis.title.text = "Date";

      let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
      valueAxis.title.text = "Close";

      let series = chart.series.push(new am4charts.LineSeries());
      series.name = "Close";
      series.stroke = am4core.color("#CDA2AB");
      series.strokeWidth = 3;
      series.dataFields.valueY = "close";
      series.dataFields.categoryX = "date";
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