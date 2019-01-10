# Command line use of API

Create a new portfolio named "piggy bank savings":
`PID=$(curl -X POST http:/localhost:9000/portfolio -F name="piggy bank savings"); echo $PID`

Place an order:
`curl -X POST http://localhost:9000/portfolio/$PID/order -F symbol=RHT -F shares=10`

View the portfolio
`curl http://localhost:9000/portfolio/$PID`