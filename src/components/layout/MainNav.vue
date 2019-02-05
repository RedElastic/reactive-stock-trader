<template>
  <BNavbar class="navbar sticky-top navbar-expand-lg navbar-light bg-light mb-3">
    <BNavbarBrand to="/">
      Reactive Stock Trader
    </BNavbarBrand>
    <BNavbarNav>
      <BNavItem to="/portfolio">
        Portfolio
      </BNavItem>
      <BNavItem to="/quote">
        Quote
      </BNavItem>
      <BNavItemDropdown text="Trading">
        <BDropdownItem to="/trades/new">
          Place New Order
        </BDropdownItem>
        <BDropdownItem to="/trades/pending">
          Pending Orders
        </BDropdownItem>
        <BDropdownItem to="/trades/completed">
          Completed Orders
        </BDropdownItem>
      </BNavItemDropdown>
      <BNavItemDropdown text="Transfers">
        <BDropdownItem to="/transfers/new">
          Place Wire Transfer
        </BDropdownItem>
        <BDropdownItem to="/transfers/pending">
          Pending Wires
        </BDropdownItem>
        <BDropdownItem to="/transfers/completed">
          Completed Wires
        </BDropdownItem>
      </BNavItemDropdown>
    </BNavbarNav>
    <BContainer>{{ shortName }}</BContainer>
  </BNavbar>
</template>

<script>
  import {activePortfolio} from '@/common/portfolio';
  
  export default {
    data() {
      return {
        portfolio: activePortfolio.state
      }
    },
    computed: {
      shortName() {
        return this.portfolio.name + ' (' + this.portfolio.id.substring(0,8) + ')';
      }      
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
