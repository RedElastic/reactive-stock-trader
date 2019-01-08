# Command line use of API

Create a new portfolio named "piggy bank savings":
`PID=$(curl -X POST http:/localhost:9000/portfolio --data '{ "name" : "piggy bank savings" }' -H "Content-Type: application/json"); echo $PID`

Place an order:
`curl -X POST http://localhost:9000/portfolio/$PID/order -F symbol=RHT -F shares=10`

View the portfolio
`curl http://localhost:9000/portfolio/$PID`