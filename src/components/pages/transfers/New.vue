<template>    
  <div class="row">
    <div class="col">  

      <!-- overview -->
      <div class="row mt-3">
        <div class="col-7">
          <h2>Place New Wire Transfer</h2>
          <b-form @submit="onSubmit" @reset="onReset">
            <div class="row">
              <div class="col">
                <b-form-group label="Deposit / Withdrawl">
                  <b-form-radio-group id="depositWithdrawlGroup" v-model="selected" name="depositWithdrawlRadio">
                    <b-form-radio value="deposit">Deposit</b-form-radio>
                    <b-form-radio value="withdrawl">Withdrawl</b-form-radio>
                  </b-form-radio-group>
                </b-form-group>
              </div>
            </div>                        
            <div class="row">
              <div class="col">
                <b-form-group id="amountGroup" label="Amount (USD)" label-for="amount">
                  <b-form-input id="amount" type="text" required></b-form-input>
                </b-form-group>   
              </div>
              <div class="col">

              </div>
            </div> 
            <div class="row">
              <div class="col-6">
                <b-form-group id="accountGroup" label="Bank Account" label-for="accountSelect">
                <select class="custom-select my-1 mr-sm-2" id="accountSelect">
                  <option selected>Choose a bank account...</option>
                  <option value="1">Chequing</option>
                  <option value="2">Savings</option>
                </select>
                </b-form-group>
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
  export default {  
  } 
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
