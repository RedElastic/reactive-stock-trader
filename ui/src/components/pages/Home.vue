<template>    
  <div class="row">
    <div class="col-6">
      <!-- equities -->
      <div class="row mt-3">
        <div class="col-12">
          <h3>Create a new portfolio</h3>
          <b-form
            @submit.prevent="openPortfolio"
            @reset="resetOpenPortfolio"
          >            
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
      </div>
      <div class="row mt-5">
        <div class="col-12" v-if="portfolios.length > 0">
          <h3 class="mb-3">Switch portfolio</h3>        
          <p v-for="portfolio in portfolios" :key="portfolio.id">
            <button v-on:click="setActivePortfolio(portfolio.id, portfolio.name)" v-if="getActivePortfolio() !== portfolio.id">Select</button>
            <button v-on:click="setActivePortfolio(portfolio.id, portfolio.name)" v-else disabled>Select</button>
            &nbsp;<b>{{ portfolio.name }}</b><br>({{ portfolio.id }})
          </p>
        </div>
      </div>
      <!-- /equities -->
    </div>
    <div class="col-6">
      <div class="card">
        <div class="card-body">
          <h2 class="mt-1">Introduction</h2>
          <p>Welcome to Reactive Stock Trader! This is a reference application intended to help developers learn the concepts of event sourcing and CQRS in a real application deployed to the cloud.</p>

          <p>Before working with this applicationm, we recommend reviewing the IBM Developer series <a href="https://developer.ibm.com/series/reactive-in-practice/">Reactive in Practice</a>.</p>

          <h3>Getting Started</h3>
          <ol>
            <li>Create a new portfolio on this page by entering an 'account name' and clicking create.</li>
            <li><a href="/#/transfers/new">Wire cash</a> from a fake checking account to your portfolio. You can wire in an unlimited amount, which is pretty awesome.</li>
            <li><a href="/#/trades/new">Perform some trades</a> and begin to build up your portfolio.</li>
            <li><a href="/#/portfolio">Visit your portfolio</a> and check out those incredible returns.</li>
          </ol>

          <p>Once you've completed these steps, follow along with the Reactive in Practice series, this demo application, and the <a href="https://github.com/RedElastic/reactive-stock-trader">code on Github</a>.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import * as portfolioService from '@/common/portfolio';
  export default {  
    data() {
      return {
        open: this.emptyOpenForm(),
        portfolios: []
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
        portfolioService.open(this.open)
          .then(response => {
            this.open.name = "";
            this.setActivePortfolio(response.id, response.name);
            this.portfolios.push(response);
          });
      },
      resetOpenPortfolio() {
        this.open = this.emptyOpenForm();
      },
      emptyOpenForm() {
        return {
          name: null
        };
      },
      setActivePortfolio(id, name) {
        portfolioService.setActivePortfolio(id, name);
      },
      getActivePortfolio() {
        return portfolioService.activePortfolio.id;
      }
    }
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
