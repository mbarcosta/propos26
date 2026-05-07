@echo off
setlocal enabledelayedexpansion

set TIPO=%1
set DIST=%2

if "%TIPO%"=="" set TIPO=Carro
if "%DIST%"=="" set DIST=10.0

echo Tipo: %TIPO%
echo Distancia: %DIST%

curl.exe -s -X POST http://localhost:8080/engine-rest/process-definition/key/cotacao_delivery/start ^
  -H "Content-Type: application/json" ^
  -d "{ \"variables\": { \"tipoTransporte\": { \"value\": \"%TIPO%\", \"type\": \"String\" }, \"distanciaKm\": { \"value\": %DIST%, \"type\": \"Double\" } } }" ^
  > resposta-start.json

for /f "delims=" %%i in ('powershell -NoProfile -Command "(Get-Content resposta-start.json -Raw | ConvertFrom-Json).id"') do set PID=%%i

echo ProcessInstanceId: !PID!

if "!PID!"=="" (
  echo Erro ao iniciar o processo. Verifique resposta-start.json
  pause
  exit /b
)

set VALOR=

for /l %%n in (1,1,10) do (
  curl.exe -s "http://localhost:8080/engine-rest/history/variable-instance?processInstanceId=!PID!&variableName=valorCotacao" > resposta-valor.json

  for /f "delims=" %%v in ('powershell -NoProfile -Command "$r = Get-Content resposta-valor.json -Raw | ConvertFrom-Json; if ($r.Count -gt 0) { $r[0].value }"') do set VALOR=%%v

  if not "!VALOR!"=="" goto encontrado

  timeout /t 1 > nul
)

:encontrado
echo Valor da cotacao: !VALOR!

pause