<template>    
  <div class="row">
    <div class="col">
      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Place New Wire Transfer</h2>
          <b-form
            @submit.prevent="onSubmit"
            @reset="onReset"
          >
            <div class="row">
              <div class="col-8">
                <b-form-group
                  id="amountGroup"
                  label="Transfer amount (USD)"
                  label-for="amount"
                >
                  <b-form-input
                    id="amount"
                    v-model.number="form.amount"
                    type="number"
                    min="0"
                    step="0.01"
                    required
                  />
                </b-form-group>   
              </div>
            </div>
            <div class="row">
              <div class="col-3">
                <b-form-group label="">
                  <b-form-radio-group
                    v-model="form.depositWithdrawl"
                    :options="options.fromTo"
                    name="depositWithdrawl"
                  />
                </b-form-group>
              </div>
              <div class="col-5">
                current portfolio ({{ portfolioId | shortUUID }})
              </div>
            </div>
            <div class="row">
              <div class="col-6">
                {{ resultingAction }}
              </div>
            </div>
            <div class="row">
              <div class="col-6">
                <b-form-select
                  id="accountGroup"
                  v-model="form.accountType"
                  label="Bank Account"
                  :options="options.accountType"
                  label-for="accountSelect"
                />
              </div>
            </div>         
            <div class="row">
              <div class="col">
                <b-form-group
                  id="accoundIdGroup"
                  label="Account ID"
                  label-for="accountId"
                >
                  <b-form-input
                    id="accountId"
                    v-model="form.accountId"
                    type="text"
                    required
                  />
                </b-form-group>   
              </div>
              <div class="col" />
            </div> 
            <div class="row">
              <div class="col mt-3">
                <b-button
                  type="submit"
                  :disabled="submitted"
                  variant="primary"
                  class="mr-3"
                >
                  Transfer
                </b-button>
                <b-button
                  type="reset"
                  :disabled="submitted"
                  variant="danger"
                >
                  Reset
                </b-button>
              </div>
            </div>
          </b-form>
        </div>          
        <div class="col-5">          
          <div class="row">
            <div class="col">
              <h4 class="mt-5">
                Summary
              </h4>
            </div>
          </div>          
          <div class="row">
            <div class="col">
              Cash on Hand (current)
            </div>
            <div class="col">
              {{ portfolio.cashOnHand | toCurrency }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              Transfer amount
            </div>
            <div class="col">
              {{ form.amount | toCurrency }}
            </div>
          </div>
          <div class="row">
            <div class="col">
              Cash on Hand (after)
            </div>
            <div class="col">
              {{ afterTransfer | toCurrency }}
            </div>
          </div>
        </div>
      </div> 
      <!-- /overview -->         
    </div>
  </div>
</template>

<script>
import {submitTransfer} from '@/common/transfers';
import {activePortfolio} from '@/common/portfolio';
import {getPortfolio} from '@/common/portfolio';

const emptyForm = {
  amount: null,
  depositWithdrawl: null,
  accountType: null,
  accountId: null
};

export default { 
  data() {
    return {
      submitted: false,
      form: Object.assign({}, emptyForm),
      portfolio: {
        cashOnHand: null
      },
      options: {
        fromTo: [
          { value: 'portfolioWithdrawl', text: 'From' },
          { value: 'portfolioDeposit', text: 'To' }
        ],
        accountType: [
          { value: null, text: 'Choose an account...' },
          { value: 'savings', text: 'Savings account' },
          { value: 'portfolio', text: 'Another portfolio' }
        ]
      }
    }
  },
  computed: {
    portfolioId() {
      return activePortfolio.id;
    },
    resultingAction() {
      return this.form.depositWithdrawl === 'portfolioWithdrawl' ? 'To'
        : this.form.depositWithdrawl === 'portfolioDeposit' ? 'From'
        : "(From/To)";
    },
    afterTransfer() {
      return this.form.depositWithdrawl === 'portfolioWithdrawl' 
        ? this.portfolio.cashOnHand - this.form.amount
        : this.form.depositWithdrawl === 'portfolioDeposit' 
        ? this.portfolio.cashOnHand + this.form.amount        
        : null;
    }
  },
  mounted() {   
     getPortfolio()
       .then(portfolio => {
         this.portfolio.cashOnHand = portfolio.funds;
       });
  },
  methods: {
    onSubmit() {
      this.submitted = true;
      submitTransfer(this.form)
        .then(() => {
          this.submitted = false;
          this.onReset();
        });      
    },
    onReset() {
      Object.assign(this.form, emptyForm);
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
