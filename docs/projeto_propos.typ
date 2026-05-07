
// Projeto Propos - Documento Completo

#import "@preview/calmly-touying:0.2.0": *

#show: calmly.with(
  config-info(
    title: [Projeto Propos],
    subtitle: [Arquitetura, Implantação e Demonstração],
    author: [Prof. Mateus Costa],
    date: datetime.today(),
    institution: [Ifes – Campus Serra],
  ),
)

// ================= MOTIVAÇÃO =================

#section-slide[Motivação]

#highlight-box(title: "Problema")[
Implantação de processos lenta e manual
]

- integração difícil
- pouco escalável


#section-slide[Objetivo]

#highlight-box(title: "Objetivo")[
Automatizar e facilitar criação de processos
]


// ================= ARQUITETURA =================

#section-slide[Arquitetura]

Camunda → GMS → CIR → Email


#section-slide[Componentes]

- Camunda: processos
- GMS: integração
- CIR: e-mail
- Workers: execução


// ================= IMPLANTAÇÃO =================

#section-slide[Camunda]

```bash
docker run -d -p 8080:8080 camunda/camunda-bpm-platform:run-latest
```

Abrir:
http://localhost:8080


#section-slide[CIR]

```bash
docker run -d -p 8082:8082 cir-service
```


// ================= BINDING =================

#section-slide[Binding]

```json
{
  "bindingId": "ppcomp-main",
  "email": "ppcomp.propos@gmail.com"
}
```


// ================= DEMO =================

#section-slide[Demo - Objetivo]

Criar processo → email → reply → fim


#section-slide[Passo 1]

```bash
docker ps
```


#section-slide[Passo 2]

Abrir Camunda:
http://localhost:8080


#section-slide[Passo 3]

Mostrar BPMN


#section-slide[Passo 4]

Enviar email:

Para: ppcomp.propos@gmail.com  
Assunto: Defesa


#section-slide[Passo 5]

```bash
curl -X POST "http://localhost:8082/api/cir/execute?bindingId=ppcomp-main"
```


#section-slide[Passo 6]

Ver instância criada


#section-slide[Passo 7]

Responder email:

Re: Defesa [DEF-123]


#section-slide[Passo 8]

Executar polling novamente


#section-slide[Passo 9]

Ver processo finalizado


#section-slide[Resumo]

Email → Evento → Processo → Reply → Fim
