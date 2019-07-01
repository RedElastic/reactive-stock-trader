<template>    
  <div>
    <div class="row">
      <div class="col-12">
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
            <div id="cashOnHandCard" class="card" v-bind:class="{ itemhighlight: cashOnHandHighlight == true }">
              <div class="card-body">          
                <h4>Active Portfolio</h4>          
                <div class="row">
                  <div class="col">
                    Cash on hand
                  </div>
                  <div id="cashOnHand" class="col">
                    {{ cashOnHand | toCurrency }}
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
                    New cash on hand
                  </div>
                  <div class="col">
                    {{ afterTransfer | toCurrency }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>        
      </div>
    </div>
    <div class="row mt-5">
      <div class="col-12">
        <h2 class="mb-3">
          Transfer History
        </h2> 
        <table id="xfer" class="table">
          <thead>
            <tr>
              <th scope="col">
                Transfer ID
              </th>
              <th scope="col">
                Status
              </th>
              <th scope="col">
                Date/Time
              </th>
              <th scope="col">
                Source account
              </th>
              <th scope="col">
                Destination account
              </th>
              <th scope="col">
                Amount (USD)
              </th>            
            </tr>
          </thead>
          <tbody style="font-size:0.8em;">
            <tr v-for="transfer in transfers" :key="transfer.id">
              <td>{{ transfer.id }}</td>
              <td>{{ transfer.status }}</td>
              <td scope="row">
                {{ transfer.dateTime }}
              </td>
              <td>{{ transfer.source }}</td>                          
              <td>{{ transfer.destination }}</td>
              <td>{{ transfer.amount | toCurrency }}</td>
            </tr>
          </tbody>
        </table>       
      </div>      
    </div>
  </div>
</template>

<script>
import {submitTransfer} from '@/common/transfers';
import {activePortfolio} from '@/common/portfolio';
import {getPortfolio} from '@/common/portfolio';
import {getDetails} from '@/common/portfolio';
import {getAllTransfersFor} from '@/common/transfers';
import {wsBaseURL} from '@/common/config';

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
      transfers: [],
      cashOnHand: null,
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
      showDismissibleAlert: false,
      cashOnHandHighlight: false
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
        ? this.cashOnHand - this.form.amount
        : this.form.depositWithdrawl === 'portfolioDeposit' 
        ? this.cashOnHand + this.form.amount        
        : null;
    }
  },
  mounted() {       
    getAllTransfersFor(this.portfolioId).then(transfers => {          
      let t = transfers.map(transfer => ({
        id: transfer.id,
        status: transfer.status,
        dateTime: transfer.dateTime,
        source: transfer.source,
        destination: transfer.destination,
        amount: transfer.amount
      }));
      this.transfers = t;
    });
    this.connect();
    this.updateCashOnHand();
  },
  methods: {
    updateCashOnHand() {
      getDetails().then(details => {
       this.cashOnHand = details.funds;
      });
    },
    resetHighlight() {
      setTimeout(() => this.cashOnHandHighlight = false, 5000);
    },
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
    },
    connect() {
      this.socket = new WebSocket(wsBaseURL + "/api/transfer/stream");
      this.socket.onopen = () => {
        this.socket.onmessage = (e) => {
          let event = JSON.parse(e.data);
          var index = -1;
          
          // determine if we're updating a row (initiated) or adding a new row (completed)
          for (var i = 0; i < this.transfers.length; i++) {
            if (this.transfers[i].id === event.id) {
              index = i;
              break;
            }
          }
          
          if (index === -1) {
            // unshift is similar to push, but prepends
            this.transfers.unshift({
              id: event.id,
              status: event.status,
              dateTime: event.dateTime,
              source: event.sourceId,
              destination: event.destinationId,
              amount: event.amount
            });
          } else {
            let t = {
              id: event.id,
              status: event.status,
              dateTime: event.dateTime,
              source: event.sourceId,
              destination: event.destinationId,
              amount: event.amount
            };
            this.transfers.splice(index, 1, t);
            this.updateCashOnHand();
            // flash the 'cash on hand' card to let users know it's been updated
            this.cashOnHandHighlight = true;
            this.resetHighlight();
          }
        };
      };
    },
    disconnect() {
      this.socket.close();
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  @keyframes yellowfade {
      from { background: yellow; }
      to { background: transparent; }
  }

  .itemhighlight {
      animation-name: yellowfade;
      animation-duration: 2.5s;
  }
</style>
