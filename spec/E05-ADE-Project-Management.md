# E05 — Gestão de Projetos de Automação no ADE

## 1. Objetivo

Criar uma gestão real de projetos no **Automation Development Environment — ADE**.

O ADE não deve mais iniciar implicitamente com o projeto de Vinculação de Orientação.

Deve iniciar como um ambiente em que o usuário cria ou abre projetos de automação.

## 2. Menu de projeto

Adicionar menu:

```text
Project
  New Project
  Open Project
  Save Project
  Close Project
  Delete Project
```

Opcional:

```text
Save As
```

## 3. Tela inicial

Quando nenhum projeto estiver aberto:

```text
propos26 Automation Development Environment
Integrated with PPG Management

[ New Automation Project ]
[ Open Existing Project ]
```

Não abrir automaticamente qualquer processo.

## 4. Novo projeto

Solicitar:

```text
Name
Key
Version
Description
```

Exemplo:

```text
Name: Cancelamento de Orientação
Key: cancelamento-orientacao
Version: 1.0
```

Abrir um projeto vazio ou BPMN mínimo.

## 5. Modelo persistente

Um projeto não é apenas BPMN.

Persistir:

```text
AutomationProject
│
├── metadata
├── BPMN model
├── Automation Requirements
├── Capability Bindings
├── Variable Mappings
├── Inbound Integrations
├── Outbound Integrations
├── Correlation Definitions
├── Generated Components
├── Deployment Configuration
└── Deployment History
```

Documentar o formato.

## 6. Salvar

`Save Project` deve salvar:

- BPMN;
- metadados;
- bindings;
- mappings;
- integrações;
- correlações;
- referências a workers/serviços;
- deployment configuration.

Fechar e reabrir deve restaurar o projeto integralmente.

## 7. Fechar

`Close Project` deve:

1. verificar alterações não salvas;
2. oferecer salvamento;
3. fechar;
4. retornar à tela inicial.

## 8. Abrir

Mostrar projetos existentes:

```text
Vinculação de Orientação
Cancelamento de Orientação
Defesa de Mestrado
```

Exibir:

```text
name
version
lastModified
deploymentStatus
```

## 9. Excluir

`Delete Project` deve:

1. pedir confirmação;
2. remover artefatos do projeto do ADE;
3. não excluir dados do PPG Management;
4. não excluir definições Camunda automaticamente.

Diferenciar:

```text
Delete Project
```

de:

```text
Undeploy Automation
```

## 10. Projeto ativo

Mostrar claramente:

```text
Project: Cancelamento de Orientação
Version: 1.0
Status: DRAFT
```

## 11. Organização da interface

Manter/evoluir:

```text
BPMN
Automation
Capabilities
Integrations
Deployment
Execution
```

Tudo deve operar sobre o projeto ativo.

## 12. Estados

Usar, por exemplo:

```text
DRAFT
CONFIGURED
VALID
DEPLOYED
FAILED
```

Não confundir estado do projeto com estado de uma instância Camunda.

## 13. Versionamento

Preparar:

```text
projectId
version
```

Sem necessidade de Git/branching nesta sprint.

## 14. Autosave

Pode existir autosave, mas manter `Save Project` explícito.

Exibir:

```text
Saved
Unsaved changes
```

## 15. Instâncias de teste

Na área `Execution`, permitir visualizar instâncias do projeto e cancelar uma instância em execução via API Camunda sem excluir a definição.

Exemplo:

```text
Instance   State
#123       WAITING

[ View ] [ Cancel ]
```

## 16. Projeto versus deployment

Modelar claramente:

```text
Automation Project
        ↓
Automation Deployment
        ↓
Camunda / Services / Integrations
```

Fechar/excluir projeto não equivale a encerrar instâncias.

## 17. Caso de validação

Usar:

```text
Cancelamento de Orientação
```

para testar:

```text
New Project
    ↓
model BPMN
    ↓
configure capabilities
    ↓
Save
    ↓
Close
    ↓
Open
    ↓
verify restored configuration
    ↓
Deploy
```

## 18. Segundo caso

Abrir novamente:

```text
Vinculação de Orientação
```

para demonstrar coexistência de múltiplos projetos.

## 19. Critérios de aceitação

A sprint termina quando:

1. o ADE não iniciar automaticamente com Vinculação de Orientação;
2. existir tela inicial;
3. for possível criar projeto;
4. salvar;
5. fechar;
6. abrir;
7. excluir;
8. múltiplos projetos coexistirem;
9. BPMN/configurações forem restaurados;
10. bindings forem persistidos;
11. mappings forem persistidos;
12. integrações/correlações forem persistidas;
13. deployment config for persistida;
14. projeto ativo for claramente identificado;
15. instâncias de teste puderem ser canceladas sem apagar definição;
16. Cancelamento de Orientação puder ser criado sem conhecimento prévio no ADE.

## 20. Relatório

Criar:

```text
docs/sprints/E05-report.md
```

Documentar modelo de persistência, interface, operações, armazenamento, alterações não salvas, teste com múltiplos projetos, limitações e recomendações.
