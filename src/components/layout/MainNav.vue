<template>
  <b-navbar class="navbar sticky-top navbar-expand-lg navbar-light bg-light mb-3">
    <b-navbar-brand to="/">
      Reactive Stock Trader
    </b-navbar-brand>
    <b-navbar-nav>
      <b-nav-item to="/portfolio">
        Portfolio
      </b-nav-item>
      <b-nav-item to="/quote">
        Quote
      </b-nav-item>
      <b-nav-item-dropdown text="Trading">
        <b-dropdown-item to="/trades/new">
          Place New Order
        </b-dropdown-item>
        <b-dropdown-item to="/trades/pending">
          Pending Orders
        </b-dropdown-item>
        <b-dropdown-item to="/trades/completed">
          Completed Orders
        </b-dropdown-item>
      </b-nav-item-dropdown>
      <b-nav-item-dropdown text="Transfers">
        <b-dropdown-item to="/transfers/new">
          Place Wire Transfer
        </b-dropdown-item>
        <b-dropdown-item to="/transfers/pending">
          Pending Wires
        </b-dropdown-item>
        <b-dropdown-item to="/transfers/completed">
          Completed Wires
        </b-dropdown-item>
      </b-nav-item-dropdown>
    </b-navbar-nav>
    <b-container>Active Portfolio: {{ portfolio.name }} ({{ portfolio.id | shortUUID }})</b-container>
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
