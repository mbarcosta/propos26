# CPF Service

Serviço REST para validação de CPF desenvolvido em Java com Spring Boot e empacotado com Docker.

Este documento descreve **passo a passo**, desde a criação do projeto no Eclipse até a execução do serviço em um servidor.

---

# 1. Pré-requisitos

- Eclipse IDE (com suporte a Maven)
- Java 17 (ou compatível)
- Maven
- Docker instalado
- Conta no Docker Hub

---

# 2. Criação do projeto no Eclipse

1. No Eclipse, selecione:
   File → New → Maven Project

2. Escolha:
   - Group Id: br.ifes
   - Artifact Id: cpf-service

3. Estrutura gerada:

   cpf-service/
     ├── pom.xml
     └── src/

---

# 3. Implementação do serviço

O projeto foi desenvolvido utilizando Spring Boot, com um endpoint REST para validação de CPF.

Endpoint disponível:

GET /cpf/{cpf}

Exemplo:

GET /cpf/12345678909

Resposta:

true

ou

false

---

# 4. Build da aplicação

Na raiz do projeto:

mvn clean package

Resultado:

target/cpf-service-0.0.1-SNAPSHOT.jar

---

# 5. Execução local (sem Docker)

java -jar target/cpf-service-0.0.1-SNAPSHOT.jar

Teste:

curl http://localhost:8080/cpf/12345678909

---

# 6. Criação do Dockerfile

Na raiz do projeto, criar o arquivo Dockerfile:

FROM eclipse-temurin:17-jre  
WORKDIR /app  
COPY target/*.jar app.jar  
EXPOSE 8080  
ENTRYPOINT ["java", "-jar", "app.jar"]

---

# 7. Build da imagem Docker

Na raiz do projeto:

docker build -t cpf-service:1.0 .

---

# 8. Execução local com Docker

docker run -d --name cpf-service -p 8080:8080 cpf-service:1.0

Teste:

curl http://localhost:8080/cpf/12345678909

---

# 9. Publicação no Docker Hub

## 9.1 Login

docker login

## 9.2 Tag da imagem

docker tag cpf-service:1.0 SEU_USUARIO/cpf-service:1.0

## 9.3 Push

docker push SEU_USUARIO/cpf-service:1.0

---

# 10. Execução no servidor

No servidor (com Docker instalado):

## 10.1 Baixar a imagem

docker pull SEU_USUARIO/cpf-service:1.0

## 10.2 Executar o serviço

docker run -d --name cpf-service -p 80:8080 SEU_USUARIO/cpf-service:1.0

---

# 11. Teste no servidor

curl http://IP_DO_SERVIDOR/cpf/12345678909

ou via navegador:

http://IP_DO_SERVIDOR/cpf/12345678909

---

# 12. Observações

- O serviço é stateless (não mantém estado)
- Não utiliza banco de dados
- Pode ser facilmente escalado
- Serve como base para outros serviços do projeto

---

# 13. Boas práticas adotadas

- Uso de Docker para empacotamento
- Versionamento de imagens (1.0, 1.1, etc.)
- Separação entre build e execução
- Estrutura Maven padrão

---

