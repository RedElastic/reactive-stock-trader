# Reactive in Practice: An Introduction

**Reactive in practice: A complete guide to event-driven systems development** in Java is a 12 part series that takes learners through the entire spectrum of a real-world event-sourced project from inception to deployment. This reference architecture is to support the series, originally published on IBM Developer:

- [Reactive in Practice on IBM Developer](https://developer.ibm.com/technologies/reactive-systems/)

Complete examples of this material are very sparse in the industry because they are so time intensive to create. However, with the support of IBM and the IBM Developer portal, we've brought this material to life. We hope it inspires the community and showcases best practices of these technologies. Special thanks to Lightbend for peer review assistance.

The main technologies showcased: 

* Java
* Lagom
* Play
* Kafka
* Cassandra

## Published units of learning materials

This reference architecture is meant to enhance the learning experience of
Reactive in Practice, a 12 part learning series published by IBM. Please visit
the learning materials below to learn about CQRS and event sourcing using Lagom
and Vue.

* Unit 1: [Introduction: event storming the 'stock trader' domain](https://developer.ibm.com/tutorials/reactive-in-practice-1/)
* Unit 2: [Prototyping the UI and UI integration patterns](https://developer.ibm.com/tutorials/reactive-in-practice-2/)
* Unit 3: [Translating the domain model to service APIs](https://developer.ibm.com/tutorials/reactive-in-practice-3/)
* Unit 4: [Concurrency, parallelism and asynchrony](https://developer.ibm.com/tutorials/reactive-in-practice-4/)
* Unit 5: [Event sourcing](https://developer.ibm.com/tutorials/reactive-in-practice-5/)
* Unit 6: [CQRS - Write side (commands and state)](https://developer.ibm.com/tutorials/reactive-in-practice-6/)
* Unit 7: [CQRS - Read side (queries and views)](https://developer.ibm.com/tutorials/reactive-in-practice-7/)
* Unit 8: [Integration patterns for transactions](https://developer.ibm.com/tutorials/reactive-in-practice-8/)
* Unit 9: [Reactive integration patterns](https://developer.ibm.com/tutorials/reactive-in-practice-9/)
* Unit 10: [Streaming data](https://developer.ibm.com/tutorials/reactive-in-practice-10/)
* Unit 11: [Deploying and monitoring reactive systems in the cloud](https://developer.ibm.com/tutorials/reactive-in-practice-11/)
* Unit 12: [Recap and conclusion](https://developer.ibm.com/tutorials/reactive-in-practice-12/)

## Contributions

If you would like to contribute, fork this repo and issue a PR. All contributions are welcome!

# Installation

The following will help you get set up in the following contexts:

- Local development
- Deployment to local Kubernetes (using Minikube)
- Interactions (UI, command line)

## Local development and running locally

Most of your interaction with Lagom will be via the command line. Please
complete the following steps.

1. Install Java 8 SDK
	- [Install sbt](https://www.scala-sbt.org/1.x/docs/Setup.html) (`brew install sbt` on Mac)
1. Sign up for [IEX Cloud](https://iexcloud.io) and generate an API token
	- IEX Cloud is used for stock quotes and historical stock data
	- Update `quote.iex.token="YOUR_TOKEN_HERE"` with your IEX public API key in
	  `broker-impl/src/main/resources/application.conf` and
`application.prod.conf`
1. Running Lagom in development mode is simple. Start by launching the backend services using `sbt`.
	- `sbt runAll`

The BFF ("backend for frontend") exposes an API on port 9100.

### Testing the backend with CURL

Let's ensure Reactive Stock trader is running properly before wiring up the UI.
Do do this, we'll use `curl` and `jq` from the command line.

The `jq` command line tool for JSON is very handy for pretty printing JSON responses, on Mac this can be installed with `brew install jq`.

The following `curl` commands will create a new portfolio and then place a few
orders to ensure that all microservices are functioning correctly.

```bash
# open a new portfolio
PID=$(curl -X POST http:/localhost:9100/api/portfolio -F name="piggy bank savings" | jq -r .portfolioId); echo $PID

# check the portfolio (you should see a lack of funds)
curl http://localhost:9100/api/portfolio/$PID | jq .

# transfer funds into the portfolio
curl -X POST http://localhost:9100/api/transfer -F amount=20000 -F sourceType=savings -F sourceId=123 -F destinationType=portfolio -F destinationId=$PID

# check the portfolio (you should see new funds)
curl http://localhost:9100/api/portfolio/$PID | jq .

# purchase shares in IBM
curl -X POST http://localhost:9100/api/portfolio/$PID/order -F symbol=IBM -F shares=10 -F order=buy

# you should see less funds and now hold shares of IBM
curl http://localhost:9100/api/portfolio/$PID | jq .
```
## Configuring and launching the UI

The UI is developed in Vue.js. You'll need to have [Node.js and npm installed](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm) and then follow the instructions below.

Project setup and launching for development: 

```
npm install
npm run serve
```

This will launch the UI on [localhost:8080](localhost:8080) for development. You can then use the UI to interact with the Lagom system.

Testing / debugging:

- Run your tests: `npm run test`
- Lints and fixes files: `npm run lint`

### Sign up for Rollbar and configure the API key

The Vue UI uses Rollbar for debugging purposes. You will need to create a
Rollbar account and then set up your API key as follows.

1. sign up at Rollbar and create an access token
1. obtain an access token
1. edit `ui/.env` and ensure `VUE_APP_ROLLBAR_ACCESS_TOKEN` is set to your token

Visit [Environment Variables and Modes](https://cli.vuejs.org/guide/mode-and-env.html) and [https://rollbar.com](Rollbar) for more details.

For additional Vue configuration information, see [Configuration Reference](https://cli.vuejs.org/config/).

## Deploying to Kubernetes

For instructions on how to deploy Reactive Stock Trader to Kubernetes, you can find the deployment instructions and Helm Charts for Kafka and Cassandra here: [https://github.com/RedElastic/reactive-stock-trader/tree/master/deploy](https://github.com/RedElastic/reactive-stock-trader/tree/master/deploy)

