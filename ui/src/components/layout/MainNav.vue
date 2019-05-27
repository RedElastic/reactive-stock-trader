<template>
  <b-navbar class="navbar sticky-top navbar-expand-lg navbar-light bg-light mb-3">
    <b-navbar-brand to="/">
      Reactive Stock Trader
    </b-navbar-brand>
    <b-navbar-nav>
      <b-nav-item to="/portfolio">Portfolio</b-nav-item>
      <b-nav-item to="/quote">Quote</b-nav-item>
      <b-nav-item to="/trades/new">Trades</b-nav-item>
      <b-nav-item to="/transfers/new">Wire Transfers</b-nav-item>
    </b-navbar-nav>
    
    <b-navbar-nav v-if="portfolio.id" class="ml-auto">
      Active Portfolio:&nbsp;<b>{{ portfolio.name }}</b>&nbsp;({{ portfolio.id | shortUUID }})
    </b-navbar-nav>
  </b-navbar>
</template>

<script>
  import {activePortfolio} from '@/common/portfolio';
  
  export default {
    data() {
      return {
        portfolio: activePortfolio.state
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
