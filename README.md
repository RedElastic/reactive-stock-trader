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


# Command line use of API

The `jq` command line tool for JSON is very handy for pretty printing JSON responses, on Mac this can be installed with `brew install jq`.

Create a new portfolio named "piggy bank savings":
`PID=$(curl -X POST http:/localhost:9000/api/portfolio -F name="piggy bank savings"); echo $PID`

Place an order:
`curl -X POST http://localhost:9000/api/portfolio/$PID/order -F symbol=RHT -F shares=10 -F order=buy`

View the portfolio
`curl http://localhost:9000/portfolio/$PID`

Transfer funds into the portfolio
`curl -X POST http://localhost:9000/api/transfer -F amount=20000 -F sourceType=savings -F sourceId=123 -F destinationType=portfolio -F destinationId=$PID`

```
PID=$(curl -X POST http:/localhost:9000/api/portfolio -F name="piggy bank savings"); echo $PID

curl -X POST http://localhost:9000/api/transfer -F amount=20000 -F sourceType=savings -F sourceId=123 -F destinationType=portfolio -F destinationId=$PID
sleep 2
curl http://localhost:9000/api/portfolio/$PID | jq .

curl -X POST http://localhost:9000/api/portfolio/$PID/order -F symbol=RHT -F shares=10 -F order=buy

curl http://localhost:9000/api/portfolio/$PID | jq .
```