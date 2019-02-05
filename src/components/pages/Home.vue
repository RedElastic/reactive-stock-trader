<template>    
  <div class="row">
    <div class="col">
      <!-- equities -->
      <div class="row mt-3">
        <div class="col-6">
          <h2>Create a new portfolio</h2>
          <BForm
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
          </BForm>
        </div>
        <div class="col-6">
          <h2>Choose a portfolio</h2>        
          <BForm @submit.prevent="loadPortfolio">
            <BFormGroup
              id="symbolGroup"
              label="Search for portfolio by name"
              label-for="symbol"
            >
              <BFormInput
                id="name"
                v-model="search.name"
                type="text"
              />
            </BFormGroup>
            <BFormGroup
              id="symbolGroup"
              label="Search for portfolio by ID"
              label-for="symbol"
            >
              <BFormInput
                id="portfolioId"
                v-model="search.portfolioId"
                type="text"
                required
              />
            </BFormGroup>
            <BButton
              type="submit"
              variant="primary"
              class="mr-3"
            >
              Lookup Portfolio
            </BButton>
          </BForm>
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
        portfolio.load(this.search.portfolioId);
      }
    }
  } 
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
