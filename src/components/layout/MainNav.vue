<template>
  <b-navbar class="navbar sticky-top navbar-expand-lg navbar-light bg-light mb-3">
    <b-navbar-brand href="/">Reactive Stock Trader</b-navbar-brand>
    <b-navbar-nav>
      <b-nav-item to="/">Portfolio</b-nav-item>
      <b-nav-item to="trades">Trades</b-nav-item>
      <b-nav-item to="transfers">Transfers</b-nav-item>
    </b-navbar-nav>

    <!-- Right aligned nav items -->
    <b-navbar-nav class="ml-auto">
      <b-nav-item href="/portfolio/new">Create Portfolio</b-nav-item>
    </b-navbar-nav>
  </b-navbar>
</template>

<script>
  export default {
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
