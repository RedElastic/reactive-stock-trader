# Install and Run

## The backend
- Install Java 8 SDK
- [Install SBT](https://www.scala-sbt.org/1.x/docs/Setup.html) (`brew install sbt` on Mac)
- `sbt runAll`

The BFF exposes an API to the front end on port 9000. 

## The UI
Install Node.js and NPM, then 
```shell
cd ui
npm install
npm run serve
```

The UI will be served by default on [localhost:8080](http://localhost:8080).

# Command line use of API

The `jq` command line tool for JSON is very handy for pretty printing JSON responses, on Mac this can be installed with `brew install jq`.

Create a new portfolio named "piggy bank savings":
`PID=$(curl -X POST http:/localhost:9000/api/portfolio -F name="piggy bank savings" | jq -r .portfolioId); echo $PID`

Place an order:
`curl -X POST http://localhost:9000/api/portfolio/$PID/order -F symbol=RHT -F shares=10 -F order=buy`

View the portfolio
`curl http://localhost:9000/api/portfolio/$PID | jq .`

Transfer funds into the portfolio
`curl -X POST http://localhost:9000/api/transfer -F amount=20000 -F sourceType=savings -F sourceId=123 -F destinationType=portfolio -F destinationId=$PID`

```
PID=$(curl -X POST http:/localhost:9000/api/portfolio -F name="piggy bank savings" | jq -r .portfolioId); echo $PID

curl -X POST http://localhost:9000/api/transfer -F amount=20000 -F sourceType=savings -F sourceId=123 -F destinationType=portfolio -F destinationId=$PID

curl http://localhost:9000/api/portfolio/$PID | jq .

curl -X POST http://localhost:9000/api/portfolio/$PID/order -F symbol=IBM -F shares=10 -F order=buy

curl http://localhost:9000/api/portfolio/$PID | jq .
```

# UI: Current Functionality

The reactive stock trader is a work in progress. At this point the following functionality is available through the UI, organized by the associated navigation item:
- 'Reactive Stock Trader':
  * Create a new portfolio
  * Load an existing portfolio (search by portfolio ID and click 'Lookup portfolio' to load)
  * The name and short ID for the active portfolio is displayed in the top navigation bar
- 'Portfolio':
  * View the current state of the active portfolio ('Portfolio' item in navigation. Note that this view does not presently auto update
- 'Quote':
  * view stock quote data
- 'Trading':
  * 'Place New Order':
    - Only market buy and sell orders are currently implemented
- 'Transfers':
  * 'Place Wire Transfer':
    - Transfers can be made between the active portfolio and another portfolio or a 'savings account'.
    - With the savings account option the ID is ignored, and all transfers will succeed. No account balance is tracked so the savings accounts are a free source (and sink) for money.