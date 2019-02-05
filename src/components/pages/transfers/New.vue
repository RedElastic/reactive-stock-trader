<template>    
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Place New Wire Transfer</h2>
          <b-form @submit.prevent="onSubmit" @reset="onReset">
            <div class="row">
              <div class="col">
                <b-form-group label="Deposit / Withdrawl">
                  <b-form-radio-group id="depositWithdrawlGroup" v-model="form.depositWithdrawl" :options="options.depositWithdrawl" name="depositWithdrawlRadio">                  
                  </b-form-radio-group>
                </b-form-group>
              </div>
            </div>                        
            <div class="row">
              <div class="col">
                <b-form-group id="amountGroup" label="Amount (USD)" label-for="amount">
                  <b-form-input id="amount" type="text" v-model="form.amount" required></b-form-input>
                </b-form-group>   
              </div>
              <div class="col">

              </div>
            </div> 
            <div class="row">
              <div class="col-6">
                <b-form-select id="accountGroup" label="Bank Account"
                    :options="options.accountType"
                    v-model="form.accountType" label-for="accountSelect">                
                </b-form-select>
              </div>
            </div>         
            <div class="row">
              <div class="col">
                <b-form-group id="accoundIdGroup" label="Account ID" label-for="accountId">
                  <b-form-input id="accountId" type="text" v-model="form.accountId" required></b-form-input>
                </b-form-group>   
              </div>
              <div class="col">

              </div>
            </div> 
            <div class="row">
              <div class="col mt-3">
                <b-button type="submit" variant="primary" class="mr-3">Transfer</b-button>
                <b-button type="reset" variant="danger">Reset</b-button>
              </div>
            </div>
          </b-form>
        </div>          
        <div class="col-5">          
          <div class="row">
            <div class="col">
              <h4 class="mt-5">Summary</h4>
            </div>
          </div>          
          <div class="row">
            <div class="col">Cash on Hand (current)</div>
            <div class="col">$15200.12</div>
          </div>
          <div class="row">
            <div class="col">Transfer amount</div>
            <div class="col">-$323.12</div>
          </div>
          <div class="row">
            <div class="col">Cash on Hand (after)</div>
            <div class="col">$15200.12</div>
          </div>
        </div>
      </div> 
      <!-- /overview -->         
    </div>
  </div>
</template>

<script>
import {submitTransfer} from '@/common/transfers.js'

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
  methods: {
    onSubmit() {
      submitTransfer(this.form);
    },
    onReset() {
      this.form = {
      }
    }
  },
  computed: {

  },
  mounted() {
    
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
