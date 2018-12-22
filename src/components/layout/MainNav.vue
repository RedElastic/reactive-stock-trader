<template>
  <nav class="navbar sticky-top navbar-expand-lg navbar-light bg-light mb-3">
    <a class="navbar-brand" href="#">Reactive Stock Trader</a>
    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
      <span class="navbar-toggler-icon"></span>
    </button>
    <div class="collapse navbar-collapse" id="navbarSupportedContent">
      <ul class="navbar-nav mr-auto">
        <li class="nav-item">
          <a id="nav-link-portfolio" class="nav-link" href="#">Portfolio</a>
        </li>
        <li class="nav-item">
          <a id="nav-link-trades" class="nav-link" href="#">Trades</a>
        </li>
        <li class="nav-item">
          <a id="nav-link-transfers" class="nav-link" href="#">Transfers</a>
        </li>          
      </ul>        
      <a href="#" class="mr-3 align-middle">Create portfolio</a>    
    </div>
  </nav>
</template>

<script>
export default {
  components: {
      
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
