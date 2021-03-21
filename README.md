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

As of this date, 8 of 12 units of this series have been finished and are reflected in the code:

* Unit 1: [Introduction: event storming the 'stock trader' domain](https://developer.ibm.com/tutorials/reactive-in-practice-1/)
* Unit 2: [Prototyping the UI and UI integration patterns](https://developer.ibm.com/tutorials/reactive-in-practice-2/)
* Unit 3: [Translating the domain model to service APIs](https://developer.ibm.com/tutorials/reactive-in-practice-3/)
* Unit 4: [Concurrency, parallelism and asynchrony](https://developer.ibm.com/tutorials/reactive-in-practice-4/)
* Unit 5: [Event sourcing](https://developer.ibm.com/tutorials/reactive-in-practice-5/)
* Unit 6: [CQRS - Write side (commands and state)](https://developer.ibm.com/tutorials/reactive-in-practice-6/)
* Unit 7: [CQRS - Read side (queries and views)](https://developer.ibm.com/tutorials/reactive-in-practice-7/)
* Unit 8: [Integration patterns for transactions](https://developer.ibm.com/tutorials/reactive-in-practice-8/)

The remaining 4 units of this series are still under construction, so the topics reflected may not be in this repo yet:

* Unit 9: Microservice integration patterns
* Unit 10: Streaming data
* Unit 11: Deploying and monitoring reactive systems in the cloud
* Unit 12: Recap and conclusion

## Contributions

If you would like to contribute, fork this repo and issue a PR. All contributions are welcome!

# Installation

The following will help you get set up in the following contexts:

- Local development
- Deployment to local Kubernetes (using Minikube)
- Interactions (UI, command line)

## Local development

- Install Java 8 SDK
- [Install sbt](https://www.scala-sbt.org/1.x/docs/Setup.html) (`brew install sbt` on Mac)

Running Lagom in development mode is simple. Start by launching the backend services using `sbt`.

- `sbt runAll`

The BFF exposes an API to the frontend on port 9100.

## Deploying to Kubernetes

For instructions on how to deploy Reactive Stock Trader to Kubernetes, you can find the deployment instructions and Helm Charts for Kafka and Cassandra here: [https://github.com/RedElastic/reactive-stock-trader/tree/master/deploy](https://github.com/RedElastic/reactive-stock-trader/tree/master/deploy)

## Interacting with the UI

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

Reactive Stock Trader uses Rollbar for debugging purposes. In order to make use of Rollbar:

* copy `config.env` to `.env.local`
* sign up at Rollbar and create an access token
* change `VUE_APP_ROLLBAR_ACCESS_TOKEN` to your token in `.env.local`

Visit [Environment Variables and Modes](https://cli.vuejs.org/guide/mode-and-env.html) and [https://rollbar.com](Rollbar) for more details.

For additional Vue configuration information, see [Configuration Reference](https://cli.vuejs.org/config/).

## Interacting with the command line

If you would like to test the backend without installing the UI, you can use the following command line information to help.

The `jq` command line tool for JSON is very handy for pretty printing JSON responses, on Mac this can be installed with `brew install jq`.

### A short smoke test

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

