# Introduction

**Reactive in practice: A complete guide to event-driven systems development** in Java is a 12 part series that takes learners through the entire spectrum of a real-world event-sourced project from inception to deployment. This reference architecture is to support the series, originally published on IBM Developer:

- [Reactive in Practice on IBM Developer](https://developer.ibm.com/technologies/reactive-systems/)

Complete examples of this material are very sparse in the industry because they are so time intensive to create. However, with the support of IBM and the IBM Developer portal, we've brought this material to life. We hope it inspires the community and showcases best practices of these technologies. Special thanks to Lightbend for peer review assistance.

## -- UNDER CONSTRUCTION --

As of this date, 4 of 12 units of this series have been finished and are reflected in the code:

* Unit 1: [Introduction: event storming the 'stock trader' domain](https://developer.ibm.com/tutorials/reactive-in-practice-1/)
* Unit 2: [Prototyping the UI and UI integration patterns](https://developer.ibm.com/tutorials/reactive-in-practice-2/)
* Unit 3: [Translating the domain model to service APIs](https://developer.ibm.com/tutorials/reactive-in-practice-3/)
* Unit 4: [Concurrency, parallelism and asynchrony](https://developer.ibm.com/tutorials/reactive-in-practice-4/)

The remaining 8 units of this series are still under construction, so the topics reflected may not be in this repo yet:

* Unit 5: Persistence: aggregates, state, and event sourcing in Lagom
* Unit 6: Advanced persistence in Lagom: sharding, passivation, and activation
* Unit 7: Reactive integration patterns: circuit breakers, timeouts, and fan-outs
* Unit 8: Read-side processing and consistency basics
* Unit 9: Transactions: advanced consistency and resilience across asynchronous boundaries
* Unit 10: Streaming data
* Unit 11: Deploying and monitoring reactive systems in the cloud
* Unit 12: Recap and conclusion

## -- CONTRIBUTIONS --

If you would like to contribute, or have questions, you can reach out to us on Gitter:

* [https://gitter.im/reactive-in-practice](https://gitter.im/reactive-in-practice)

This will be a fast-moving code base until approximately May, 2019. We recommend reaching out to us before submitting a PR to make sure your change will align with the learning content in the series.

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
