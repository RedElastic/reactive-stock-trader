<template>
  <div>
    <form class="form-inline">
      <label for="nickname">Search for portfolio</label>
      <input type="text" class="form-control" id="nickname" aria-describedby="nicknameHelp">
      <small id="nicknameHelp" class="form-text text-muted">e.g, 75/25 Portfolio Split</small>
      <button type="submit" class="btn btn-primary my-1">Switch portfolio</button>
    </form>

    <form class="form-inline">
      <label for="nickname">Select portfolio by ID</label>
      <input type="text" class="form-control" id="nickname" aria-describedby="nicknameHelp">
      <small id="nicknameHelp" class="form-text text-muted">if you remember the ID of your portfolio you can find by ID</small>
      <button type="submit" class="btn btn-primary my-1">Switch portfolio</button>
    </form>
  </div>
</template>

<script>  
  export default {

  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
