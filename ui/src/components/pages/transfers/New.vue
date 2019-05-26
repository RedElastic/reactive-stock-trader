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
              <div class="col-8">
                <b-form-group label="">
                  <b-form-radio-group
                    v-model="form.depositWithdrawl"
                    :options="options.fromTo"
                    name="depositWithdrawl"
                  />
                </b-form-group>
              </div>
            </div>
            <div class="row">
              <div class="col-6">
                <b-form-group
                  id="accountGroup"
                  label="External Account"
                  label-for="accountType"
                >
                  <b-form-select
                    id="accountType"
                    v-model="form.accountType"
                    :options="options.accountType"
                    label-for="accountGroup"
                    required
                  />
                </b-form-group>
              </div>
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
          <b-alert class="mb-5" v-model="showDismissibleAlert" variant="primary" dismissible>
            Wire transfer has been initiated! Your cash will be available shortly. Please view pending wires for up-to-date transfer status.
          </b-alert>          
          <div class="card">
            <div class="card-body">          
              <h4>Active Portfolio</h4>          
              <div class="row">
                <div class="col">
                  Cash on hand
                </div>
                <div class="col">
                  {{ portfolio.cashOnHand | toCurrency }}
                </div>
              </div>
            </div>
          </div>

          <div class="card mt-5">
            <div class="card-body">          
              <h4>Transaction</h4>          
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
import {getDetails} from '@/common/portfolio';

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
          { value: 'portfolioWithdrawl', text: 'Withdrawal (out)' },
          { value: 'portfolioDeposit', text: 'Deposit (in)' }
        ],
        accountType: [
          { value: null, text: 'Choose an account...' },
          { value: 'savings', text: 'Savings account' },
          { value: 'portfolio', text: 'Another portfolio' }
        ]
      },
      showDismissibleAlert: false
    }
  },
  computed: {
    portfolioId() {
      return activePortfolio.id;
    },
    resultingAction() {
      return this.form.depositWithdrawl === 'portfolioWithdrawl' ? 'Destination'
        : this.form.depositWithdrawl === 'portfolioDeposit' ? 'Source'
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
     getDetails()
       .then(details => {
         this.portfolio.cashOnHand = details.funds;
       });
  },
  methods: {
    onSubmit() {
      this.submitted = true;
      this.showDismissibleAlert = true;
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
