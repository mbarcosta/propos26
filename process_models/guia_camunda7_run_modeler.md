# Guia de criação, deployment, teste e execução de processos BPMN com Camunda 7 Run + Camunda Modeler

Este guia descreve um fluxo simples e validado para modelar, publicar e testar processos BPMN usando:

- **Camunda Modeler**
- **Camunda 7 Run em Docker**
- **REST API do Camunda**
- **PowerShell/Windows com `curl.exe`**

O objetivo é manter um caminho curto e controlado para:

1. criar um modelo BPMN;
2. fazer o deployment;
3. iniciar o processo por mensagem;
4. validar uma **Service Task externa**;
5. preparar a integração com serviços externos como o CIR.

---

## 1. Ambiente adotado

Neste guia, assume-se o seguinte cenário já instalado e funcionando:

- Docker disponível localmente;
- Camunda Modeler instalado;
- Camunda 7 Run executando em Docker;
- acesso local ao Camunda em `http://localhost:8080`;
- uso do PowerShell no Windows.

A imagem Docker oficial do Camunda expõe a landing page em `http://localhost:8080/camunda-welcome/index.html`, a REST API em `http://localhost:8080/engine-rest`, e no modo `run` os Web Apps, REST API e Swagger UI vêm habilitados por padrão. Quando não há configuração explícita de banco, o `run` usa H2 embutido. ([github.com](https://github.com/camunda/docker-camunda-bpm-platform/blob/7.24/README.md))

---

## 2. Subindo o Camunda 7 Run em Docker

Para este fluxo de testes, o Camunda foi executado localmente com Docker.

### 2.1. Subida básica

```bash
docker run -d --name camunda7 -p 8080:8080 camunda/camunda-bpm-platform:run-latest
```

A documentação oficial do Docker image mostra que as imagens podem ser usadas para demonstração e testes, e que a distribuição `run` é suportada como uma das variantes publicadas. ([github.com](https://github.com/camunda/docker-camunda-bpm-platform/blob/7.24/README.md))

### 2.2. Verificando se o container está em execução

```bash
docker ps
```

### 2.3. Verificando a versão do engine

```bash
curl.exe http://localhost:8080/engine-rest/version
```

No ambiente validado, a resposta foi:

```json
{"version":"7.24.0"}
```

### 2.4. URLs úteis

- Landing page: `http://localhost:8080/camunda-welcome/index.html`
- Web apps: `http://localhost:8080/camunda`
- REST API: `http://localhost:8080/engine-rest`

As credenciais padrão dos Web Apps são `demo/demo`. A REST API, por padrão, não exige autenticação. ([github.com](https://github.com/camunda/docker-camunda-bpm-platform/blob/7.24/README.md))

---

## 3. Observação importante sobre a distribuição `run`

Na distribuição `run`, os componentes Web Apps, REST API e Swagger UI já vêm habilitados por padrão. Se for necessário habilitar seletivamente, os parâmetros devem ser passados ao script `./camunda.sh`, e não soltos ao final do `docker run`. ([github.com](https://github.com/camunda/docker-camunda-bpm-platform/blob/7.24/README.md))

Exemplo oficial de habilitação seletiva:

```bash
docker run camunda/camunda-bpm-platform:run ./camunda.sh --webapps
```

e

```bash
docker run camunda/camunda-bpm-platform:run ./camunda.sh --rest --swaggerui
```

No fluxo deste guia, isso **não é necessário**. ([github.com](https://github.com/camunda/docker-camunda-bpm-platform/blob/7.24/README.md))

---

## 4. Criando o processo no Camunda Modeler

Para o primeiro teste, recomenda-se um processo **mínimo e simples**, sem pool, contendo:

- **Message Start Event**
- **Service Task**
- **End Event**

Fluxo:

```text
Message Start Event -> Service Task -> End Event
```

Esse desenho é suficiente para validar:

- deployment;
- início por mensagem;
- parada em tarefa externa;
- consumo posterior por um worker.

---

## 5. Configuração do processo

Ao criar o processo no Modeler, configure:

### 5.1. Processo executável

Selecione o processo e marque como executável.

O processo BPMN precisa ser executável para rodar no engine.

### 5.2. History Cleanup / Time to live (TTL)

No painel de propriedades do processo, configure:

```text
History Cleanup -> Time to live = 180
```

Esse campo é importante porque o History Cleanup do Camunda remove dados históricos com base em configurações de time-to-live. Na prática, processos sem TTL podem gerar erro de parse/deployment dependendo da configuração do engine. A documentação oficial explica que o cleanup histórico se baseia em configurações de TTL. ([docs.camunda.org](https://docs.camunda.org/manual/latest/user-guide/process-engine/history/history-cleanup/))

Neste guia, foi usado:

```text
180
```

Se preferir editar no XML, o processo pode conter:

```xml
camunda:historyTimeToLive="180"
```

Exemplo:

```xml
<bpmn:process id="Process_Exemplo" isExecutable="true" camunda:historyTimeToLive="180">
```

Para isso, o XML precisa declarar o namespace Camunda:

```xml
xmlns:camunda="http://camunda.org/schema/1.0/bpmn"
```

---

## 6. Configuração do Message Start Event

O evento inicial deve ser do tipo **Message Start Event**.

Além disso, ele precisa estar associado a uma mensagem BPMN válida. Não basta apenas mudar o ícone para envelope; é necessário criar a mensagem e vinculá-la ao evento.

### 6.1. No Modeler

Selecione o Start Event e:

1. defina o tipo como **Message Start Event**;
2. na propriedade **Message**, crie uma nova mensagem;
3. use o nome:

```text
VINCULACAO_RECEBIDA
```

### 6.2. Efeito esperado no XML

O Modeler deve gerar algo como:

```xml
<bpmn:message id="Message_1" name="VINCULACAO_RECEBIDA" />
```

e o evento inicial deve referenciá-la:

```xml
<bpmn:startEvent id="StartEvent_1">
  <bpmn:messageEventDefinition messageRef="Message_1" />
</bpmn:startEvent>
```

Sem `messageRef`, o deployment falha com erro de parse.

---

## 7. Configuração da Service Task

Para este fluxo, a Service Task foi configurada como **External Task**, que é o caminho mais simples para integração com um sistema externo.

### 7.1. No Modeler

Selecione a Service Task e configure:

- **Implementation**: `External`
- **Topic**: `vinculacao`

Pronto. Não é necessário mais nada para o primeiro teste.

---

## 8. Salvando o arquivo BPMN

Salve o arquivo, por exemplo, como:

```text
teste_v1.bpmn
```

Sugestão de organização no repositório:

```text
process-models/
└── vinculacao/
    ├── teste_v1.bpmn
    └── README.md
```

---

## 9. Fazendo o deployment do BPMN

No PowerShell/Windows, use `curl.exe`, não apenas `curl`, para evitar conflito com `Invoke-WebRequest`.

### 9.1. Deployment

```bash
curl.exe -X POST http://localhost:8080/engine-rest/deployment/create -F "deployment-name=teste" -F "data=@teste_v1.bpmn"
```

### 9.2. Resposta esperada

Uma resposta com `id`, `name`, `deploymentTime` e `deployedProcessDefinitions`.

Exemplo simplificado:

```json
{
  "id": "...",
  "name": "teste",
  "deployedProcessDefinitions": {
    "Process_...": {
      "key": "Process_...",
      "version": 1,
      "resource": "teste_v1.bpmn",
      "historyTimeToLive": 180
    }
  }
}
```

Se isso ocorrer, o deployment foi feito com sucesso.

---

## 10. Iniciando o processo por mensagem

Como o processo começa com **Message Start Event**, ele deve ser iniciado por correlação de mensagem, via REST.

### 10.1. Comando

No PowerShell, funcionou corretamente com JSON entre aspas simples externas:

```bash
curl.exe -X POST http://localhost:8080/engine-rest/message -H "Content-Type: application/json" -d '{\"messageName\":\"VINCULACAO_RECEBIDA\"}'
```

### 10.2. Observação prática sobre PowerShell

No Windows/PowerShell, o escaping de JSON com `curl.exe` pode gerar erros estranhos. Quando o payload começar a crescer, prefira usar arquivo `.json` com `-d @arquivo.json`.

Exemplo:

**msg.json**
```json
{
  "messageName": "VINCULACAO_RECEBIDA"
}
```

Comando:

```bash
curl.exe -X POST http://localhost:8080/engine-rest/message -H "Content-Type: application/json" -d @msg.json
```

---

## 11. Verificando se a External Task foi criada

Depois de iniciar o processo por mensagem, o engine deve executar até a Service Task e parar nela, aguardando um worker externo.

### 11.1. Consultando external tasks

```bash
curl.exe http://localhost:8080/engine-rest/external-task
```

### 11.2. Resposta esperada

Uma lista contendo uma tarefa com:

```json
"topicName":"vinculacao"
```

Exemplo simplificado:

```json
[
  {
    "id": "...",
    "topicName": "vinculacao",
    "workerId": null,
    "lockExpirationTime": null
  }
]
```

Isso prova que:

- o processo iniciou;
- a Service Task externa foi alcançada;
- o engine está aguardando consumo por worker.

---

## 12. Fetch and Lock da External Task

O passo seguinte é um worker externo fazer o `fetchAndLock` da task.

### 12.1. Payload sugerido

**fetch.json**
```json
{
  "workerId": "worker1",
  "maxTasks": 1,
  "topics": [
    {
      "topicName": "vinculacao",
      "lockDuration": 10000
    }
  ]
}
```

### 12.2. Comando

```bash
curl.exe -X POST http://localhost:8080/engine-rest/external-task/fetchAndLock -H "Content-Type: application/json" -d @fetch.json
```

### 12.3. Resultado esperado

Uma lista com a task, agora contendo:

- `workerId = "worker1"`
- `lockExpirationTime` preenchido

Exemplo simplificado:

```json
[
  {
    "id": "...",
    "topicName": "vinculacao",
    "workerId": "worker1",
    "lockExpirationTime": "..."
  }
]
```

---

## 13. Concluindo a External Task

Após o `fetchAndLock`, o worker conclui a task.

### 13.1. Payload sugerido

**complete.json**
```json
{
  "workerId": "worker1"
}
```

### 13.2. Comando

Substitua `{TASK_ID}` pelo `id` retornado no passo anterior:

```bash
curl.exe -X POST http://localhost:8080/engine-rest/external-task/{TASK_ID}/complete -H "Content-Type: application/json" -d @complete.json
```

### 13.3. Verificação

Depois, execute novamente:

```bash
curl.exe http://localhost:8080/engine-rest/external-task
```

Se o processo tiver terminado, o esperado é:

```json
[]
```

---

## 14. Fluxo completo validado

Com isso, o fluxo validado fica:

```text
Message -> Processo inicia -> Service Task externa -> Fetch and Lock -> Complete -> Processo termina
```

Esse é o ciclo mínimo necessário para provar a integração BPMN básica.

---

## 15. Problemas mais comuns encontrados

### 15.1. `docker ps` não mostra o container

Se o container não aparece em `docker ps`, ele provavelmente encerrou logo após subir.

Verifique:

```bash
docker ps -a
docker logs camunda7
```

### 15.2. Usar `curl` em vez de `curl.exe` no PowerShell

No PowerShell, `curl` costuma ser alias de `Invoke-WebRequest`. Isso quebra comandos com `-X` e `-F`.

Use sempre:

```bash
curl.exe
```

### 15.3. Erros de escaping no JSON

Se aparecerem erros como:

- `InvalidRequestException`
- `bad range specification in URL`
- `URL rejected`

o problema costuma ser escape do JSON no PowerShell.

Use uma destas estratégias:

1. JSON com aspas simples externas:
   ```bash
   -d '{\"messageName\":\"VINCULACAO_RECEBIDA\"}'
   ```

2. melhor ainda: arquivo `.json` com `-d @arquivo.json`

### 15.4. Erro de TTL

Se o deployment falhar reclamando de `History Time To Live (TTL) cannot be null`, configure no processo:

```text
History Cleanup -> Time to live = 180
```

ou no XML:

```xml
camunda:historyTimeToLive="180"
```

### 15.5. Erro de `messageRef`

Se o deployment falhar com algo como:

- `attribute 'messageRef' is required`
- `Invalid 'messageRef': no message with id 'null' found`

então o Start Event foi marcado como Message, mas a mensagem BPMN não foi criada ou vinculada corretamente.

Corrija criando a mensagem e associando-a ao evento.

---

## 16. Verificação visual pelo Cockpit

Depois do deployment e da execução, é possível acompanhar pelo Cockpit.

Acesse:

```text
http://localhost:8080/camunda
```

No Cockpit, é possível verificar:

- processo implantado;
- instâncias iniciadas;
- ponto atual de execução;
- término do processo.

---

## 17. Sugestão de uso com integração CIR

Para integração com o CIR, o uso natural é:

```text
E-mail -> CIR identifica evento -> CIR chama /engine-rest/message -> Camunda inicia processo
```

e, em um segundo momento:

```text
Camunda para em External Task -> CIR (ou outro serviço) faz fetchAndLock -> executa lógica -> complete
```

Isso separa claramente:

- **detecção do evento**;
- **orquestração do processo**;
- **execução do trabalho externo**.

---

## 18. Checklist curto de modelagem

Antes de fazer o deployment, confirme:

- processo marcado como executável;
- `History Cleanup -> Time to live` preenchido;
- Start Event do tipo **Message**;
- mensagem BPMN criada e vinculada ao Start Event;
- Service Task configurada como **External**;
- tópico da Service Task definido como `vinculacao`;
- fluxo indo do Start Event para a Service Task e depois para o End Event.

---

## 19. Comandos principais reunidos

### Subir o Camunda

```bash
docker run -d --name camunda7 -p 8080:8080 camunda/camunda-bpm-platform:run-latest
```

### Ver versão

```bash
curl.exe http://localhost:8080/engine-rest/version
```

### Fazer deployment

```bash
curl.exe -X POST http://localhost:8080/engine-rest/deployment/create -F "deployment-name=teste" -F "data=@teste_v1.bpmn"
```

### Iniciar por mensagem

```bash
curl.exe -X POST http://localhost:8080/engine-rest/message -H "Content-Type: application/json" -d '{\"messageName\":\"VINCULACAO_RECEBIDA\"}'
```

### Consultar external tasks

```bash
curl.exe http://localhost:8080/engine-rest/external-task
```

### Fetch and lock

```bash
curl.exe -X POST http://localhost:8080/engine-rest/external-task/fetchAndLock -H "Content-Type: application/json" -d @fetch.json
```

### Complete

```bash
curl.exe -X POST http://localhost:8080/engine-rest/external-task/{TASK_ID}/complete -H "Content-Type: application/json" -d @complete.json
```

---

## 20. Conclusão

Com esse fluxo, fica estabelecido um caminho simples e reproduzível para:

- modelar um processo no Camunda Modeler;
- publicar no Camunda 7 Run;
- iniciar por mensagem;
- validar uma Service Task externa;
- preparar a integração com sistemas externos, como o CIR.

Esse guia cobre o caso mínimo necessário para sair da modelagem e chegar à execução controlada do processo.
