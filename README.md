# Install

Install Java 8 SDK
Install SBT (`brew install sbt` on Mac)
`sbt runAll`

The front end API is exposed on port 9000. 

The `jq` command line tool for JSON is very handy for pretty printing JSON responses, on Mac this can be installed with `brew install jq`. 

# Command line use of API

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