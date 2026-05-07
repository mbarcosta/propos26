@echo off

curl -X POST http://localhost:8080/engine-rest/process-definition/key/cotacao_delivery/start ^
  -H "Content-Type: application/json" ^
  -d "{ \"variables\": { \"tipoTransporte\": { \"value\": \"Moto\", \"type\": \"String\" }, \"distanciaKm\": { \"value\": 10.0, \"type\": \"Double\" } } }"

pause