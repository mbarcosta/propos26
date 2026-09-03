# E07 - Contextual Capabilities

O Wizard nao mostra inicialmente o catalogo completo de capabilities. Para Service Tasks e Send Tasks, ele calcula uma lista de funcionalidades compativeis por regra deterministica.

Sinais usados:

- tipo da etapa do Wizard;
- nome do elemento BPMN;
- id tecnico do elemento;
- tipo da capability;
- interface disponivel;
- contrato da capability;
- capability ja vinculada.

Exemplos:

- uma Send Task prioriza `SEND_EMAIL`;
- nomes com cancelamento priorizam capabilities `CANCEL_*`;
- nomes relacionados a orientacao priorizam capabilities de `ADVISORSHIP`;
- nomes relacionados a professor priorizam `PROFESSOR`;
- nomes relacionados a defesa ou dissertacao priorizam `DEFENSE` e `DISSERTATION_DOCUMENT`.

O usuario ainda pode abrir `Mostrar todas as capabilities` e pesquisar no catalogo completo.

As sugestoes nao usam IA generativa e nao alteram silenciosamente a automacao. A capability so e aplicada quando o usuario escolhe a opcao.
