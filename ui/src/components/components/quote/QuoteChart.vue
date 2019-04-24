<template>
  <div class="row">    
    <div class="col">
      <b-nav
        class="small my-3"
        tabs
      >
        <b-nav-item
          id="1d"
          :active="getLastTimeframe === '1d'"
          @click="updateChart('1d')"
        >
          1 day
        </b-nav-item>
        <b-nav-item
          id="1m"
          :active="getLastTimeframe === '1m'"
          @click="updateChart('1m')"
        >
          1 month
        </b-nav-item>
        <b-nav-item
          id="3m"
          :active="getLastTimeframe === '3m'"
          @click="updateChart('3m')"
        >
          3 months
        </b-nav-item>
        <b-nav-item
          id="6m"
          :active="getLastTimeframe === '6m'"
          @click="updateChart('6m')"
        >
          6 months
        </b-nav-item>
        <b-nav-item
          id="1y"
          :active="getLastTimeframe === '1y'"
          @click="updateChart('1y')"
        >
          1 year
        </b-nav-item>
        <b-nav-item
          id="2y"
          :active="getLastTimeframe === '2y'"
          @click="updateChart('2y')"
        >
          2 years
        </b-nav-item>
        <b-nav-item
          id="5y"
          :active="getLastTimeframe === '5y'"
          @click="updateChart('5y')"
        >
          5 years
        </b-nav-item>
      </b-nav>
      <div
        :id="chartId"
        style="width: 100%; height: 300px;"
      />
    </div>
  </div>    
  <!-- /stocks -->
</template>

<script>
  import * as am4core from "@amcharts/amcharts4/core";
  import * as am4charts from "@amcharts/amcharts4/charts";
  import am4themes_animated from "@amcharts/amcharts4/themes/animated";
  import {IEX} from '@/common/http.js';
  import Vue from 'vue'

  am4core.useTheme(am4themes_animated);

  export default {
    name: 'QuoteChart',
    props: {
      symbol: {
        type: String,
        required: true
      }
    },
    data: function () {
      return {
        chartId: "chart_" + this.symbol,
        lastTimeframe: ""
      }
    },    
    computed: {
      getLastTimeframe: function () {
        return this.lastTimeframe;
      }
    },
    watch: {
      symbol: function () {        
        this.updateChart(this.lastTimeframe);      
      }
    },
    mounted() {
      this.updateChart("1m");
    },
    beforeDestroy() {
      if (this.chart) {
        this.chart.dispose();
      }
    },
    methods: {      
      iexQueryStr: function (symbol, timeframe) {
        if (timeframe === '1d') {
          return "/stock/" + symbol + "/chart/" + timeframe + "?filter=average,minute&chartSimplify=true";
        } else if (timeframe === '1m' || timeframe === '3m' || timeframe === '6m') {
          return "/stock/" + symbol + "/chart/" + timeframe + "?filter=close,date";
        } else {
          return "/stock/" + symbol + "/chart/" + timeframe + "?filter=close,date&chartSimplify=true";
        }
      },
      getChart: function (chartData, catXKey, catXLabel, valYKey, valYLabel) {
        let chart = am4core.create(this.chartId, am4charts.XYChart);

        chart.data = chartData;

        let categoryAxis = chart.xAxes.push(new am4charts.CategoryAxis());
        categoryAxis.dataFields.category = catXKey;
        categoryAxis.title.text = catXLabel;

        let valueAxis = chart.yAxes.push(new am4charts.ValueAxis());
        valueAxis.title.text = valYLabel;

        let series = chart.series.push(new am4charts.LineSeries());
        series.name = valYLabel;
        series.stroke = am4core.color("#7f8c8d");
        series.strokeWidth = 3;
        series.dataFields.valueY = valYKey;
        series.dataFields.categoryX = catXKey;
        series.tensionX = 0.8;
        series.tensionY = 1.0;

        return chart;
      },      
      updateChart: function (timeframe) {      
        IEX.get(this.iexQueryStr(this.symbol, timeframe), {timeout: 2000})
          .then(response => {                
            if (timeframe === "1d") {
              let chartData = response.data.filter(val => {
                return val.average > 0;
              });
              this.chart = this.getChart(chartData, "minute", "Minute", "average", "Average");
            } else {
              this.chart = this.getChart(response.data, "date", "Date", "close", "Close");
            }
            this.lastTimeframe = timeframe;
          })
          .catch(e => Vue.rollbar.error(e))
      }
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>