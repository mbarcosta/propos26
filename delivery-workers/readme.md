## Passo 1 — Criação do projeto Spring Boot dos Workers

O objetivo deste passo é criar o projeto base `delivery-workers`, que irá hospedar os workers responsáveis por executar tarefas externas do Camunda 7.

### 1. Criar o projeto no Eclipse

No Eclipse IDE, acessar:

File → New → Spring Starter Project

Se a opção `Spring Starter Project` não aparecer, instalar o Spring Tools no Eclipse.

Configurar o projeto com os seguintes dados:

Name: delivery-workers  
Type: Maven  
Java Version: 17 ou superior  
Packaging: Jar  
Group: br.edu.ifes  
Artifact: delivery-workers  
Package: br.edu.ifes.deliveryworkers  

Selecionar a dependência:

Spring Web

Finalizar a criação do projeto em:

Finish

### 2. Estrutura esperada do projeto

Após a criação, o projeto deverá ter uma estrutura semelhante a esta:

delivery-workers/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── br/
        │       └── edu/
        │           └── ifes/
        │               └── deliveryworkers/
        │                   └── DeliveryWorkersApplication.java
        └── resources/
            └── application.properties

### 3. Classe principal

A classe principal deve estar em:

src/main/java/br/edu/ifes/deliveryworkers/DeliveryWorkersApplication.java

Conteúdo esperado:

package br.edu.ifes.deliveryworkers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DeliveryWorkersApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryWorkersApplication.class, args);
    }
}

### 4. Adicionar dependência do cliente Camunda

Abrir o arquivo:

pom.xml

Adicionar a dependência abaixo dentro da seção `<dependencies>`:

<dependency>
    <groupId>org.camunda.bpm</groupId>
    <artifactId>camunda-external-task-client</artifactId>
    <version>7.23.0</version>
</dependency>

Depois de alterar o `pom.xml`, atualizar o projeto Maven:

Right click no projeto → Maven → Update Project

### 5. Configurar a porta da aplicação

Como o Camunda 7 normalmente utiliza a porta `8080`, o serviço dos workers deve usar outra porta.

Abrir o arquivo:

src/main/resources/application.properties

Adicionar:

server.port=8091

Com isso, o serviço Spring Boot será iniciado em:

http://localhost:8091

### 6. Executar o projeto

No Eclipse:

Right click no projeto → Run As → Spring Boot App

### 7. Verificar se a aplicação subiu corretamente

No console do Eclipse, deve aparecer algo semelhante a:

Tomcat initialized with port 8091 (http)
Tomcat started on port 8091 (http)
Started DeliveryWorkersApplication

Isso indica que a aplicação foi iniciada corretamente.

### 8. Testar no navegador

Acessar:

http://localhost:8091

O resultado esperado neste momento é uma página de erro 404, semelhante a:

Whitelabel Error Page
There was an unexpected error (type=Not Found, status=404)

Esse erro é esperado, pois ainda não foi criado nenhum endpoint REST na aplicação.

O importante é que o servidor esteja rodando na porta configurada.

### Resultado esperado do Passo 1

Ao final deste passo, deve-se ter:

- Projeto Spring Boot `delivery-workers` criado.
- Aplicação executando sem erros.
- Serviço rodando na porta `8091`.
- Dependência do cliente Camunda adicionada ao `pom.xml`.
- Ambiente base pronto para criação dos workers externos.

## Passo 2 — Configuração do cliente Camunda (External Task Client)

O objetivo deste passo é configurar o cliente que permitirá ao serviço Spring Boot se conectar ao Camunda 7 e consumir tarefas externas (External Tasks).

### 1. Configurar propriedades do cliente

Abrir o arquivo:

src/main/resources/application.properties

Adicionar as seguintes configurações:

camunda.bpm.client.base-url=http://localhost:8080/engine-rest  
camunda.bpm.client.worker-id=delivery-workers  
camunda.bpm.client.async-response-timeout=10000  

Explicação:

- base-url: endpoint REST do Camunda 7  
- worker-id: identificador único do worker  
- async-response-timeout: tempo de long polling (em ms)

---

### 2. Criar classe de configuração do cliente

### 2.1 Adicionar dependência do Camunda External Task Client

Para que o serviço Spring Boot possa se comunicar com o Camunda 7, é necessário adicionar a dependência do cliente de External Tasks.

No arquivo `pom.xml`, incluir dentro da seção `<dependencies>`:

```xml
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.2</version>
</dependency>

<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>4.0.5</version>
</dependency>
<dependency>
    <groupId>org.camunda.bpm</groupId>
    <artifactId>camunda-external-task-client</artifactId>
    <version>7.23.0</version>
</dependency>
```

### 2.2 Criar a classe
Criar o pacote:

br.edu.ifes.deliveryworkers.config

Criar a classe:

CamundaClientConfig.java

Conteúdo:

```java
package br.edu.ifes.deliveryworkers.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaClientConfig {

    @Bean
    public ExternalTaskClient externalTaskClient(
            @Value("${camunda.bpm.client.base-url}") String baseUrl,
            @Value("${camunda.bpm.client.worker-id}") String workerId,
            @Value("${camunda.bpm.client.async-response-timeout}") long asyncResponseTimeout
    ) {
        return ExternalTaskClient.create()
                .baseUrl(baseUrl)
                .workerId(workerId)
                .asyncResponseTimeout(asyncResponseTimeout)
                .build();
    }
}
```
## 2.3 — Criar o pacote dos workers

Criar o seguinte pacote dentro de `src/main/java`:

br.edu.ifes.deliveryworkers.workers

Neste pacote serão criadas duas classes:

- MotoWorker
- CarroWorker

Cada classe será responsável por escutar um tópico diferente do Camunda.

A relação será:

MotoWorker  → topic: cotacao-moto  
CarroWorker → topic: cotacao-carro

---

## 2.4 — Criar o Worker da Moto

Criar a classe:

MotoWorker.java

No pacote:

br.edu.ifes.deliveryworkers.workers

Conteúdo da classe:

```java
package br.edu.ifes.deliveryworkers.workers;

import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class MotoWorker {

    private final ExternalTaskClient client;

    public MotoWorker(ExternalTaskClient client) {
        this.client = client;
    }

    @PostConstruct
    public void subscribe() {
        client.subscribe("cotacao-moto")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {

                    Double distanciaKm = externalTask.getVariable("distanciaKm");

                    double valorCotacao = 20 + distanciaKm * 0.5;

                    externalTaskService.complete(
                            externalTask,
                            Map.of("valorCotacao", valorCotacao)
                    );

                    System.out.println("Worker Moto executado.");
                    System.out.println("Distância: " + distanciaKm + " km");
                    System.out.println("Valor da cotação: R$ " + valorCotacao);
                })
                .open();
    }
}
```

### 2.5 Verificar se os workers foram inicializados corretamente

Após criar as classes `MotoWorker` e `CarroWorker`, executar novamente a aplicação Spring Boot no Eclipse:

Right click no projeto → Run As → Spring Boot App

Antes disso, garantir que o Camunda 7 esteja rodando em:

http://localhost:8080

E que o serviço dos workers esteja configurado para usar a URL REST do Camunda:

```properties
camunda.bpm.client.base-url=http://localhost:8080/engine-rest
```

### 2.6 Teste rápido de inicialização dos workers (opcional, recomendado)

Para verificar se os workers foram realmente inicializados pelo Spring Boot, pode-se adicionar temporariamente mensagens de log simples nas classes `MotoWorker` e `CarroWorker`.

Por exemplo, no arquivo `MotoWorker.java`, dentro do método `subscribe()`, (no seu início) adicionar a linha abaixo logo antes da chamada `.open()`:

```java
System.out.println("Worker Moto ativo");
```


Após executar novamente a aplicação:

Right click no projeto → Run As → Spring Boot App

Verificar no console do Eclipse se as mensagens aparecem.

Se aparecerem, significa que:

- os beans foram carregados corretamente pelo Spring;
- o cliente Camunda foi inicializado;
- os workers estão ativos e prontos para consumir tarefas externas.

Após a validação, essas mensagens podem ser removidas.

## Passo 3 — Criação do modelo BPMN

O objetivo deste passo é criar um modelo de processo BPMN que:

- receba os parâmetros de entrada (`tipoTransporte` e `distanciaKm`);
- utilize um gateway XOR para decidir o fluxo;
- acione o worker correto (Moto ou Carro);
- finalize o processo.

---

### 3.1 Criar o modelo no Camunda Modeler

Abrir o Camunda Modeler e criar um novo diagrama BPMN.

Salvar o arquivo com o nome:

cotacao-delivery.bpmn

Definir o **Process ID** como:

cotacao_delivery

---

### 3.2 Estrutura do processo

Construir o seguinte fluxo:

Start Event  
→ Gateway XOR (decisão pelo tipo de transporte)  
→ Service Task (Moto)  
→ End Event  

e

Start Event  
→ Gateway XOR  
→ Service Task (Carro)  
→ End Event  

---

### 3.3 Configurar as condições dos fluxos de saída do Gateway XOR

No Camunda Modeler, as condições do gateway não são configuradas diretamente no gateway, mas sim nos **Sequence Flows** que saem dele.

Ou seja:

- clique na seta que liga o Gateway XOR à tarefa da Moto;
- configure a condição desse fluxo;
- depois clique na seta que liga o Gateway XOR à tarefa do Carro;
- configure a condição desse outro fluxo.

#### Fluxo para Moto

Selecionar o **Sequence Flow** que sai do Gateway XOR e aponta para a tarefa da Moto.

No painel lateral direito, localizar a seção:

Condition

Configurar:

```text
Type: Expression
Expression: ${tipoTransporte == "Moto"}
```

Importante:
 
- As expressões são avaliadas pelo Camunda. Por isso o tipo deve ser Expression, porque a condição será avaliada diretamente como uma expressão de variável do processo.
- O valor de `tipoTransporte` virá da chamada REST.

---

### 3.4 Configurar a tarefa da Moto

Selecionar a Service Task da Moto.
No painel lateral direito, localizar a seção Implementation e
configurar:

- Type: External
- Topic: cotacao-moto

Isso faz com que o Camunda gere uma External Task com esse tópico, que será consumida pelo `MotoWorker`.

---

### 3.5 Configurar a tarefa do Carro

Selecionar a Service Task do Carro. No painel lateral direito, localizar a seção Implementation e
configurar: 

- Type: External
- Topic: cotacao-carro

Isso conecta a tarefa ao `CarroWorker`.

---

### 3.6 Variáveis utilizadas no processo (como definir e usar)

No Camunda 7, variáveis de processo **não são declaradas explicitamente no BPMN** como em linguagens de programação.

Elas são:

- **criadas dinamicamente** na execução do processo;
- **recebidas via REST** na inicialização;
- **consumidas nas expressões do BPMN** (ex: XOR);
- **produzidas pelos workers**.

Neste exemplo, utilizaremos três variáveis:

Entrada:

- tipoTransporte (String)
- distanciaKm (Double)

Saída:

- valorCotacao (Double)

---

#### Como a variável `tipoTransporte` é usada

- Ela é enviada na chamada REST que inicia o processo;
- É utilizada no Gateway XOR para decidir o fluxo;
- Não precisa ser declarada no Modeler.

Exemplo de uso no BPMN:

```text
${tipoTransporte == "Moto"}
```

### 3.7 Deploy do processo no Camunda

Antes de fazer o deploy, configurar o **History Time To Live** do processo.

No Camunda Modeler:

1. Clicar em uma área vazia do diagrama, para selecionar o processo como um todo.
2. No painel lateral direito, localizar a seção:

General

3. Verificar se o **Process ID** está como:

cotacao_delivery

4. Localizar o campo:

History Time To Live

5. Preencher com:

180

Esse valor indica que o histórico das instâncias do processo será mantido por 180 dias.

Essa configuração evita erros de deploy relacionados à ausência de TTL no Camunda 7.


Após salvar o BPMN, realizar o deploy:

- Via modeler:

Uma forma mais simples de realizar o deploy é utilizando o próprio Camunda Modeler.

Passos:

1. Certificar-se de que o Camunda 7 está em execução em:

http://localhost:8080

2. No Camunda Modeler, com o diagrama aberto, clicar no botão de **Deploy** (ícone de foguete) localizado na barra inferior direita.

3. Na janela de configuração, informar:

```text
Endpoint: http://localhost:8080/engine-rest
Deployment Name: cotacao-delivery
```

-  via terminal:

curl -X POST http://localhost:8080/engine-rest/deployment/create \
  -F "deployment-name=cotacao-delivery" \
  -F "enable-duplicate-filtering=true" \
  -F "data=@cotacao-delivery.bpmn"

---

### 3.8 Verificar deploy

Acessar:

http://localhost:8080/camunda/app/cockpit

Verificar se o processo `cotacao_delivery` aparece na lista.

---

### 3.9 Resultado esperado

Ao final deste passo:

- O processo BPMN está criado;
- O gateway XOR decide o fluxo com base no tipo de transporte;
- Cada Service Task está vinculada a um tópico;
- Os workers estão prontos para serem acionados automaticamente pelo Camunda.

---

### Liçao Aprendida

Neste modelo:

- O BPMN controla o fluxo (decisão XOR);
- Os workers executam a lógica  das tarefas (cálculo);
- Não há acoplamento direto entre processo e código;
- A comunicação ocorre exclusivamente via tópicos (External Tasks).


## Passo 4 — Entender a arquitetura e executar o processo

O objetivo deste passo é:

- entender como o Camunda 7 se comunica com os workers;
- compreender quem chama quem;
- executar o processo na prática;
- validar o funcionamento completo.

---

### 4.1 Quem chama quem?

O Camunda **não chama diretamente** o serviço `delivery-workers`.

Ou seja, o Camunda não faz uma requisição HTTP para:

http://localhost:8091/moto  
http://localhost:8091/carro  

Na verdade, acontece o contrário:

👉 o serviço `delivery-workers` é que consulta o Camunda.

Esse modelo é chamado de:

PULL (o worker busca tarefas)

---

### 4.2 Papel do Camunda

O Camunda executa o fluxo BPMN.

Quando o processo chega em uma Service Task configurada como:

Type: External  
Topic: cotacao-moto  

o Camunda cria uma **External Task** com o tópico:

cotacao-moto  

Essa tarefa fica armazenada no engine aguardando um worker.

O mesmo ocorre para:

Type: External  
Topic: cotacao-carro  

---

### 4.3 Papel do serviço delivery-workers

A aplicação `delivery-workers` roda separadamente do Camunda.

Ela deve ser iniciada no Eclipse:

Right click → Run As → Spring Boot App  

Ela roda, por exemplo, em:

http://localhost:8091  

⚠️ Importante:

O Camunda **não chama essa URL**.

O que acontece é:

- o worker usa `ExternalTaskClient`
- esse client consulta o Camunda via REST:

http://localhost:8080/engine-rest  

---

### 4.4 Como o worker se conecta ao BPMN

A conexão ocorre exclusivamente pelo **nome do tópico**.

No BPMN:

Service Task Moto  
Topic: cotacao-moto  

No Java:

```java
client.subscribe("cotacao-moto");
```

### 4.5 Teste completo de execução (passo a passo)

A partir deste ponto, já é possível testar o funcionamento completo do processo com os workers.

---

#### 4.5.1 Garantir que tudo está rodando

Antes de iniciar o teste, verificar:

- Camunda 7 rodando (Docker):
  http://localhost:8080

- REST API disponível:
  http://localhost:8080/engine-rest

- Aplicação `delivery-workers` rodando no Eclipse:
  http://localhost:8091

- Sem erros no console do Spring Boot

---

#### 4.5.2 Executar o processo (Moto)

No terminal (PowerShell, CMD ou bash), executar:

```
curl -X POST http://localhost:8080/engine-rest/process-definition/key/cotacao_delivery/start -H "Content-Type: application/json" -d "{ \"variables\": { \"tipoTransporte\": { \"value\": \"Moto\", \"type\": \"String\" }, \"distanciaKm\": { \"value\": 10.0, \"type\": \"Double\" } } }"
```
No Linux/Mac (bash):

``` curl -X POST http://localhost:8080/engine-rest/process-definition/key/cotacao_delivery/start   -H "Content-Type: application/json" -d '{ "variables": {"tipoTransporte": { "value": "Moto", "type": "String" },"distanciaKm": { "value": 10.0, "type": "Double" } }}'



```