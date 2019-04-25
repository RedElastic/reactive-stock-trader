<template>    
  <div class="row">
    <div class="col">
      <!-- equities -->
      <div class="row mt-3">
        <div class="col-6">
          <h2>Create a new portfolio</h2>
          <b-form
            @submit.prevent="openPortfolio"
            @reset="resetOpenPortfolio"
          >            
            <label for="nickname">
              Portfolio nickname
            </label>
            <input
              id="nickname"
              v-model="open.name"
              type="text"
              class="form-control"
              aria-describedby="nicknameHelp"
            >
            <small
              id="nicknameHelp"
              class="form-text text-muted"
            >
              Give your portfolio a meaningful name, e.g, "75/25 Portfolio Split". In the future you can find your portfolio by this name or by the portfolio ID.
            </small>
            <button
              type="submit"
              class="btn btn-primary mt-3"
            >
              Create
            </button>
          </b-form>
        </div>
        <div class="col-6">
          <h2>Choose a portfolio</h2>        
          <p v-for="portfolio in portfolios" :key="portfolio.id">
            <button v-on:click="setActivePortfolio(portfolio.id, portfolio.name)">Select</button>&nbsp;<b>{{ portfolio.name }}</b>&nbsp;({{ portfolio.id }})
          </p>
        </div>
      </div>
      <!-- /equities -->
    </div>
  </div>
</template>

<script>
  import * as portfolioService from '@/common/portfolio';
  export default {  
    data() {
      return {
        open: this.emptyOpenForm(),
        search: this.emptySearchForm(),
        portfolios: null
      }
    },
    mounted() {
      portfolioService.getAllPortfolios()
        .then(portfolios => {          
          let p = portfolios.map(portfolio => ({
            id: portfolio.portfolioId.id,
            name: portfolio.name
          }));
          this.portfolios = p;
        });
    },
    methods: {
      openPortfolio() {
        portfolioService.open(this.open);
      },
      resetOpenPortfolio() {
        this.open = this.emptyOpenForm();
      },
      emptyOpenForm() {
        return {
          name: null
        };
      },
      emptySearchForm() {
        return {
          name: null,
          portfolioId: null
        };
      },
      setActivePortfolio(id, name) {
        portfolioService.setActivePortfolio(id, name);
      }
    }
  } 
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
