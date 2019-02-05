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
              <div class="col">
                <b-form-group label="Deposit / Withdrawl">
                  <b-form-radio-group
                    id="depositWithdrawlGroup"
                    v-model="form.depositWithdrawl"
                    :options="options.depositWithdrawl"
                    name="depositWithdrawlRadio"
                  />
                </b-form-group>
              </div>
            </div>                        
            <div class="row">
              <div class="col">
                <b-form-group
                  id="amountGroup"
                  label="Amount (USD)"
                  label-for="amount"
                >
                  <b-form-input
                    id="amount"
                    v-model="form.amount"
                    type="text"
                    required
                  />
                </b-form-group>   
              </div>
              <div class="col" />
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
                  variant="primary"
                  class="mr-3"
                >
                  Transfer
                </b-button>
                <b-button
                  type="reset"
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
              $15200.12
            </div>
          </div>
          <div class="row">
            <div class="col">
              Transfer amount
            </div>
            <div class="col">
              -$323.12
            </div>
          </div>
          <div class="row">
            <div class="col">
              Cash on Hand (after)
            </div>
            <div class="col">
              $15200.12
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

export default { 
  data() {
    return {
      submitted: false,
      form: {
        
      },
      options: {
        depositWithdrawl: [
          { value: 'deposit', text: 'Deposit' },
          { value: 'withdrawl', text: 'Withdrawl' }
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
  },
  mounted() {    
  },
  methods: {
    onSubmit() {
      submitTransfer(this.form);
    },
    onReset() {
      this.form = {
      }
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
