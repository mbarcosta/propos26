@echo off
setlocal enabledelayedexpansion

REM === 1) Iniciar processo ===
curl.exe -s -X POST http://localhost:8080/engine-rest/process-definition/key/cotacao_delivery/start ^
  -H "Content-Type: application/json" ^
  -d "{ \"variables\": { \"tipoTransporte\": { \"value\": \"Carro\", \"type\": \"String\" }, \"distanciaKm\": { \"value\": 10.0, \"type\": \"Double\" } } }" ^
  > resposta-start.json

REM === 2) Extrair ID da instância ===
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content resposta-start.json -Raw | ConvertFrom-Json).id"') do set PID=%%i

echo ProcessInstanceId: %PID%

REM === 3) Aguardar o worker concluir ===
timeout /t 2 > nul

REM === 4) Consultar variável valorCotacao ===
curl.exe -s "http://localhost:8080/engine-rest/history/variable-instance?processInstanceId=%PID%&variableName=valorCotacao" ^
  > resposta-valor.json

REM === 5) Extrair valor da cotação ===
for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content resposta-valor.json -Raw | ConvertFrom-Json)[0].value"') do set VALOR=%%i

echo Valor da cotacao: %VALOR%

pause