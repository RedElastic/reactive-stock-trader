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
          <b-form @submit.prevent="loadPortfolio">
            <b-form-group
              id="symbolGroup"
              label="Search for portfolio by name"
              label-for="symbol"
            >
              <b-form-input
                id="name"
                v-model="search.name"
                type="text"
              />
            </b-form-group>
            <b-form-group
              id="symbolGroup"
              label="Search for portfolio by ID"
              label-for="symbol"
            >
              <b-form-input
                id="portfolioId"
                v-model="search.portfolioId"
                type="text"
                required
              />
            </b-form-group>
            <b-button
              type="submit"
              variant="primary"
              class="mr-3"
            >
              Lookup Portfolio
            </b-button>
          </b-form>
        </div>
      </div>
      <!-- /equities -->
    </div>
  </div>
</template>

<script>
  import * as portfolio from '@/common/portfolio';
  export default {  
    data() {
      return {
        open: this.emptyOpenForm(),
        search: this.emptySearchForm()
      }
    },
    methods: {
      openPortfolio() {
        portfolio.open(this.open);
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
      loadPortfolio() {
        portfolio.getPortfolio({
          portfolioId: this.search.portfolioId
        });
      }
    }
  } 
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
