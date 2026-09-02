const state = {
  capabilities: [],
  requirements: [],
  gatewayBranches: [],
  derivedConfig: {
    variables: [],
    topics: [],
    messages: [],
    requiredConfigurations: []
  },
  bindings: {},
  inboundConfigs: {},
  outboundConfigs: {},
  flowConditions: {},
  integration: {},
  processConfig: { historyTimeToLive: '180' },
  status: 'DRAFT'
};

const referenceXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_Vinculacao" targetNamespace="http://propos26.local/bpmn">
  <bpmn:message id="Message_VinculacaoSolicitada" name="VINCULACAO_SOLICITADA" />
  <bpmn:message id="Message_DadosComplementares" name="DADOS_COMPLEMENTARES" />
  <bpmn:message id="Message_ConfirmacaoEstudante" name="CONFIRMACAO_ESTUDANTE" />
  <bpmn:message id="Message_ConfirmacaoCoordenador" name="CONFIRMACAO_COORDENADOR" />
  <bpmn:process id="vinculacao_orientacao" name="Vinculacao de Orientacao" isExecutable="true" camunda:historyTimeToLive="180">
    <bpmn:startEvent id="Start_Vinculacao" name="Receber solicitacao">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
      <bpmn:messageEventDefinition id="Start_Message" messageRef="Message_VinculacaoSolicitada" />
    </bpmn:startEvent>
    <bpmn:serviceTask id="Task_VerificarDados" name="Verificar dados" camunda:type="external" camunda:topic="VALIDATE_ADVISORSHIP_REQUEST">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:exclusiveGateway id="Gateway_DadosCompletos" name="Dados completos?">
      <bpmn:incoming>Flow_2</bpmn:incoming>
      <bpmn:outgoing>Flow_DadosNao</bpmn:outgoing>
      <bpmn:outgoing>Flow_DadosSim</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:sendTask id="Task_SolicitarDados" name="Solicitar dados" camunda:type="external" camunda:topic="SEND_EMAIL">
      <bpmn:incoming>Flow_DadosNao</bpmn:incoming>
      <bpmn:outgoing>Flow_3</bpmn:outgoing>
    </bpmn:sendTask>
    <bpmn:intermediateCatchEvent id="Catch_DadosComplementares" name="Aguardar resposta">
      <bpmn:incoming>Flow_3</bpmn:incoming>
      <bpmn:outgoing>Flow_4</bpmn:outgoing>
      <bpmn:messageEventDefinition id="Catch_Dados_Message" messageRef="Message_DadosComplementares" />
    </bpmn:intermediateCatchEvent>
    <bpmn:sendTask id="Task_SolicitarConfirmacaoEstudante" name="Solicitar confirmacao ao estudante" camunda:type="external" camunda:topic="SEND_EMAIL">
      <bpmn:incoming>Flow_DadosSim</bpmn:incoming>
      <bpmn:outgoing>Flow_5</bpmn:outgoing>
    </bpmn:sendTask>
    <bpmn:intermediateCatchEvent id="Catch_ConfirmacaoEstudante" name="Aguardar confirmacao estudante">
      <bpmn:incoming>Flow_5</bpmn:incoming>
      <bpmn:outgoing>Flow_6</bpmn:outgoing>
      <bpmn:messageEventDefinition id="Catch_Estudante_Message" messageRef="Message_ConfirmacaoEstudante" />
    </bpmn:intermediateCatchEvent>
    <bpmn:sendTask id="Task_SolicitarConfirmacaoCoordenador" name="Solicitar confirmacao ao coordenador" camunda:type="external" camunda:topic="SEND_EMAIL">
      <bpmn:incoming>Flow_6</bpmn:incoming>
      <bpmn:outgoing>Flow_7</bpmn:outgoing>
    </bpmn:sendTask>
    <bpmn:intermediateCatchEvent id="Catch_ConfirmacaoCoordenador" name="Aguardar confirmacao coordenador">
      <bpmn:incoming>Flow_7</bpmn:incoming>
      <bpmn:outgoing>Flow_8</bpmn:outgoing>
      <bpmn:messageEventDefinition id="Catch_Coordenador_Message" messageRef="Message_ConfirmacaoCoordenador" />
    </bpmn:intermediateCatchEvent>
    <bpmn:serviceTask id="Task_RegistrarOrientacao" name="Registrar orientacao" camunda:type="external" camunda:topic="REGISTER_ADVISORSHIP">
      <bpmn:incoming>Flow_8</bpmn:incoming>
      <bpmn:outgoing>Flow_9</bpmn:outgoing>
    </bpmn:serviceTask>
    <bpmn:endEvent id="End_Vinculacao" name="Fim">
      <bpmn:incoming>Flow_9</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="Start_Vinculacao" targetRef="Task_VerificarDados" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_VerificarDados" targetRef="Gateway_DadosCompletos" />
    <bpmn:sequenceFlow id="Flow_DadosNao" sourceRef="Gateway_DadosCompletos" targetRef="Task_SolicitarDados">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\${complete == false}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_DadosSim" sourceRef="Gateway_DadosCompletos" targetRef="Task_SolicitarConfirmacaoEstudante">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\${complete == true}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:sequenceFlow id="Flow_3" sourceRef="Task_SolicitarDados" targetRef="Catch_DadosComplementares" />
    <bpmn:sequenceFlow id="Flow_4" sourceRef="Catch_DadosComplementares" targetRef="Task_VerificarDados" />
    <bpmn:sequenceFlow id="Flow_5" sourceRef="Task_SolicitarConfirmacaoEstudante" targetRef="Catch_ConfirmacaoEstudante" />
    <bpmn:sequenceFlow id="Flow_6" sourceRef="Catch_ConfirmacaoEstudante" targetRef="Task_SolicitarConfirmacaoCoordenador" />
    <bpmn:sequenceFlow id="Flow_7" sourceRef="Task_SolicitarConfirmacaoCoordenador" targetRef="Catch_ConfirmacaoCoordenador" />
    <bpmn:sequenceFlow id="Flow_8" sourceRef="Catch_ConfirmacaoCoordenador" targetRef="Task_RegistrarOrientacao" />
    <bpmn:sequenceFlow id="Flow_9" sourceRef="Task_RegistrarOrientacao" targetRef="End_Vinculacao" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="vinculacao_orientacao">
    <bpmndi:BPMNShape id="Start_Vinculacao_di" bpmnElement="Start_Vinculacao"><dc:Bounds x="120" y="170" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Task_VerificarDados_di" bpmnElement="Task_VerificarDados"><dc:Bounds x="210" y="148" width="120" height="80" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Gateway_DadosCompletos_di" bpmnElement="Gateway_DadosCompletos" isMarkerVisible="true"><dc:Bounds x="390" y="163" width="50" height="50" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Task_SolicitarDados_di" bpmnElement="Task_SolicitarDados"><dc:Bounds x="520" y="270" width="120" height="80" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Catch_DadosComplementares_di" bpmnElement="Catch_DadosComplementares"><dc:Bounds x="720" y="292" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Task_SolicitarConfirmacaoEstudante_di" bpmnElement="Task_SolicitarConfirmacaoEstudante"><dc:Bounds x="520" y="148" width="150" height="80" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Catch_ConfirmacaoEstudante_di" bpmnElement="Catch_ConfirmacaoEstudante"><dc:Bounds x="735" y="170" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Task_SolicitarConfirmacaoCoordenador_di" bpmnElement="Task_SolicitarConfirmacaoCoordenador"><dc:Bounds x="840" y="148" width="160" height="80" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Catch_ConfirmacaoCoordenador_di" bpmnElement="Catch_ConfirmacaoCoordenador"><dc:Bounds x="1065" y="170" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="Task_RegistrarOrientacao_di" bpmnElement="Task_RegistrarOrientacao"><dc:Bounds x="1170" y="148" width="140" height="80" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="End_Vinculacao_di" bpmnElement="End_Vinculacao"><dc:Bounds x="1370" y="170" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1"><di:waypoint x="156" y="188" /><di:waypoint x="210" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2"><di:waypoint x="330" y="188" /><di:waypoint x="390" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_DadosNao_di" bpmnElement="Flow_DadosNao"><di:waypoint x="415" y="213" /><di:waypoint x="415" y="310" /><di:waypoint x="520" y="310" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_DadosSim_di" bpmnElement="Flow_DadosSim"><di:waypoint x="440" y="188" /><di:waypoint x="520" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_3_di" bpmnElement="Flow_3"><di:waypoint x="640" y="310" /><di:waypoint x="720" y="310" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_4_di" bpmnElement="Flow_4"><di:waypoint x="738" y="328" /><di:waypoint x="738" y="390" /><di:waypoint x="270" y="390" /><di:waypoint x="270" y="228" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_5_di" bpmnElement="Flow_5"><di:waypoint x="670" y="188" /><di:waypoint x="735" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_6_di" bpmnElement="Flow_6"><di:waypoint x="771" y="188" /><di:waypoint x="840" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_7_di" bpmnElement="Flow_7"><di:waypoint x="1000" y="188" /><di:waypoint x="1065" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_8_di" bpmnElement="Flow_8"><di:waypoint x="1101" y="188" /><di:waypoint x="1170" y="188" /></bpmndi:BPMNEdge>
    <bpmndi:BPMNEdge id="Flow_9_di" bpmnElement="Flow_9"><di:waypoint x="1310" y="188" /><di:waypoint x="1370" y="188" /></bpmndi:BPMNEdge>
  </bpmndi:BPMNPlane></bpmndi:BPMNDiagram>
</bpmn:definitions>`;

const modeler = new BpmnJS({ container: '#canvas' });
const xmlBox = document.getElementById('xml');

function project() {
  return {
    name: document.getElementById('projectName').value,
    key: document.getElementById('projectKey').value,
    version: document.getElementById('projectVersion').value,
    bpmnXml: xmlBox.value,
    requirements: state.requirements,
    derivedConfig: state.derivedConfig,
    bindings: state.bindings,
    inboundConfigs: state.inboundConfigs,
    outboundConfigs: state.outboundConfigs,
    flowConditions: state.flowConditions,
    processConfig: state.processConfig,
    integration: state.integration,
    status: state.status
  };
}

async function loadXml() {
  await modeler.importXML(xmlBox.value);
  modeler.get('canvas').zoom('fit-viewport');
}

async function saveXml() {
  const result = await modeler.saveXML({ format: true });
  xmlBox.value = result.xml;
  saveProject();
}

function setStatus(status) {
  state.status = status;
  document.getElementById('projectStatus').textContent = status;
}

function saveProject() {
  localStorage.setItem('propos26.ade.project', JSON.stringify(project()));
  renderProject();
}

function openProject() {
  const saved = localStorage.getItem('propos26.ade.project');
  if (!saved) {
    xmlBox.value = referenceXml;
    loadXml();
    return;
  }
  const data = JSON.parse(saved);
  document.getElementById('projectName').value = data.name || '';
  document.getElementById('projectKey').value = data.key || '';
  document.getElementById('projectVersion').value = data.version || '';
  xmlBox.value = data.bpmnXml || referenceXml;
  state.requirements = data.requirements || [];
  state.derivedConfig = data.derivedConfig || {
    variables: [],
    topics: [],
    messages: [],
    requiredConfigurations: []
  };
  state.bindings = data.bindings || {};
  state.inboundConfigs = data.inboundConfigs || {};
  state.outboundConfigs = data.outboundConfigs || {};
  state.flowConditions = data.flowConditions || {};
  state.processConfig = data.processConfig || { historyTimeToLive: '180' };
  state.integration = data.integration || {};
  document.getElementById('historyTimeToLive').value = state.processConfig.historyTimeToLive || '180';
  setStatus(data.status || 'DRAFT');
  loadXml();
  renderRequirements();
  renderDerivedConfig();
  renderIntegration();
  renderProject();
}

async function analyzeBpmn() {
  await saveXml();
  const xml = xmlBox.value;
  const parser = new DOMParser();
  const doc = parser.parseFromString(xml, 'text/xml');
  const elements = [];
  const selectors = [
    ['bpmn\\:serviceTask, serviceTask', 'SERVICE_CAPABILITY'],
    ['bpmn\\:sendTask, sendTask', 'OUTBOUND_COMMUNICATION'],
    ['bpmn\\:intermediateThrowEvent, intermediateThrowEvent', 'OUTBOUND_COMMUNICATION'],
    ['bpmn\\:endEvent, endEvent', 'OUTBOUND_COMMUNICATION'],
    ['bpmn\\:receiveTask, receiveTask', 'INBOUND_EVENT'],
    ['bpmn\\:intermediateCatchEvent, intermediateCatchEvent', 'INBOUND_EVENT,MESSAGE_DEFINITION,CORRELATION_DEFINITION']
  ];
  selectors.forEach(([selector, type]) => {
    doc.querySelectorAll(selector).forEach((node) => {
      if (type === 'OUTBOUND_COMMUNICATION' && !isOutboundNode(node)) {
        return;
      }
      elements.push({
        elementId: node.getAttribute('id'),
        name: node.getAttribute('name') || node.getAttribute('id'),
        type,
        bpmnType: node.localName,
        capabilityId: state.bindings[node.getAttribute('id')] || '',
        inboundConfig: state.inboundConfigs[node.getAttribute('id')] || defaultInboundConfig(node),
        outboundConfig: state.outboundConfigs[node.getAttribute('id')] || defaultOutboundConfig(node)
      });
    });
  });
  state.requirements = elements;
  state.requirements
    .filter((item) => item.type.includes('INBOUND_EVENT'))
    .forEach((item) => {
      state.inboundConfigs[item.elementId] = state.inboundConfigs[item.elementId] || item.inboundConfig;
    });
  state.requirements
    .filter((item) => item.type.includes('OUTBOUND_COMMUNICATION') && isExternalTaskCapable(item.bpmnType))
    .forEach((item) => {
      state.outboundConfigs[item.elementId] = state.outboundConfigs[item.elementId] || item.outboundConfig;
    });
  state.gatewayBranches = deriveGatewayBranches(doc);
  state.derivedConfig = deriveProjectConfiguration(doc, xml, state.requirements);
  renderRequirements();
  renderDerivedConfig();
  setStatus('DRAFT');
}

function deriveProjectConfiguration(doc, xml, requirements) {
  const configuredConditions = Object.values(state.flowConditions || {})
    .map((config) => config.condition || '')
    .filter(Boolean)
    .join('\n');
  const variables = deriveVariables(`${xml}\n${configuredConditions}`);
  const topics = Array.from(doc.querySelectorAll('[camunda\\:topic], [topic]'))
    .map((node) => ({
      elementId: node.getAttribute('id'),
      elementName: node.getAttribute('name') || node.getAttribute('id'),
      topic: node.getAttribute('camunda:topic') || node.getAttribute('topic')
    }))
    .filter((item) => item.topic);
  const messages = Array.from(doc.querySelectorAll('message, bpmn\\:message'))
    .map((node) => ({
      id: node.getAttribute('id'),
      name: node.getAttribute('name')
    }))
    .filter((item) => item.id || item.name);
  const requiredConfigurations = [];

  variables.forEach((variable) => {
    requiredConfigurations.push({
      key: variable.name,
      type: inferVariableConfigurationType(variable.name),
      reason: `Used in BPMN expression: ${variable.expression}`
    });
  });

  topics.forEach((topic) => {
    requiredConfigurations.push({
      key: topic.topic,
      type: 'CAPABILITY_BINDING',
      reason: `External Task topic used by ${topic.elementName}`
    });
  });

  messages.forEach((message) => {
    requiredConfigurations.push({
      key: message.name || message.id,
      type: 'MESSAGE_DEFINITION',
      reason: `BPMN message definition ${message.id}`
    });
  });

  requirements
    .filter((item) => item.type.includes('INBOUND_EVENT'))
    .forEach((item) => {
      requiredConfigurations.push({
        key: item.elementId,
        type: 'INBOUND_CORRELATION',
        reason: `Waiting element requires external event and correlation: ${item.name}`
      });
    });

  return {
    variables,
    topics,
    messages,
    requiredConfigurations: dedupeRequiredConfigurations(requiredConfigurations)
  };
}

function deriveVariables(xml) {
  const found = new Map();
  const pattern = /\$\{([^}]+)}/g;
  let match;
  while ((match = pattern.exec(xml)) !== null) {
    const expression = match[0];
    const names = extractVariableNames(match[1]);
    names.forEach((name) => {
      if (!found.has(name)) {
        found.set(name, {
          name,
          expression,
          source: inferVariableSource(name),
          required: true
        });
      }
    });
  }
  return Array.from(found.values());
}

function extractVariableNames(expressionBody) {
  const reserved = new Set(['true', 'false', 'null', 'and', 'or', 'not', 'eq', 'ne', 'gt', 'lt', 'ge', 'le']);
  const withoutStrings = expressionBody.replace(/"([^"\\]|\\.)*"|'([^'\\]|\\.)*'/g, '');
  return Array.from(withoutStrings.matchAll(/[A-Za-z_][A-Za-z0-9_]*/g))
    .filter((match) => withoutStrings.charAt(match.index - 1) !== '.')
    .map((match) => match[0])
    .filter((name) => !reserved.has(name))
    .filter((name) => !/^[A-Z][A-Za-z0-9_]*$/.test(name));
}

function inferVariableSource(name) {
  const lower = name.toLowerCase();
  if (lower.includes('coordinator') || lower.includes('coordenador') || lower.includes('program')) {
    return 'PROJECT_VARIABLE';
  }
  if (lower.includes('email') || lower.includes('student') || lower.includes('advisor') || lower.includes('orientador') || lower.includes('title') || lower.includes('research')) {
    return 'INBOUND_MAPPING';
  }
  if (lower.includes('correlation') || lower.includes('request')) {
    return 'GENERATED_OR_INBOUND_MAPPING';
  }
  return 'PROCESS_VARIABLE';
}

function inferVariableConfigurationType(name) {
  const source = inferVariableSource(name);
  if (source === 'PROJECT_VARIABLE') {
    return 'PROJECT_VARIABLE';
  }
  if (source === 'INBOUND_MAPPING' || source === 'GENERATED_OR_INBOUND_MAPPING') {
    return 'INBOUND_DATA_MAPPING';
  }
  return 'PROCESS_VARIABLE';
}

function dedupeRequiredConfigurations(items) {
  const found = new Map();
  items.forEach((item) => {
    const key = `${item.type}:${item.key}`;
    if (!found.has(key)) {
      found.set(key, item);
    }
  });
  return Array.from(found.values());
}

function deriveGatewayBranches(doc) {
  const elements = Array.from(doc.getElementsByTagName('*'));
  const byId = new Map(elements
    .filter((node) => node.getAttribute && node.getAttribute('id'))
    .map((node) => [node.getAttribute('id'), node]));
  const sequenceFlows = elements.filter((node) => node.localName === 'sequenceFlow');

  return elements
    .filter((node) => node.localName === 'exclusiveGateway')
    .flatMap((gateway) => {
      const gatewayId = gateway.getAttribute('id');
      const defaultFlow = gateway.getAttribute('default') || '';
      const outgoingIds = Array.from(gateway.childNodes)
        .filter((node) => node.nodeType === Node.ELEMENT_NODE && node.localName === 'outgoing')
        .map((node) => node.textContent.trim())
        .filter(Boolean);
      const outgoingFlows = outgoingIds.length
        ? outgoingIds.map((id) => sequenceFlows.find((flow) => flow.getAttribute('id') === id)).filter(Boolean)
        : sequenceFlows.filter((flow) => flow.getAttribute('sourceRef') === gatewayId);

      return outgoingFlows.map((flow) => {
        const flowId = flow.getAttribute('id');
        const target = byId.get(flow.getAttribute('targetRef'));
        const condition = Array.from(flow.childNodes)
          .find((node) => node.nodeType === Node.ELEMENT_NODE && node.localName === 'conditionExpression');
        const isDefault = flowId === defaultFlow;
        const detectedCondition = condition ? condition.textContent.trim() : '';
        const defaultCondition = isDefault ? '' : defaultEmailBodyConditionExpression(flow.getAttribute('name') || '');
        state.flowConditions[flowId] = state.flowConditions[flowId] || {
          condition: detectedCondition || defaultCondition,
          isDefault
        };
        if (!state.flowConditions[flowId].condition && defaultCondition) {
          state.flowConditions[flowId].condition = defaultCondition;
        }
        return {
          gatewayId,
          gatewayName: gateway.getAttribute('name') || gatewayId,
          flowId,
          flowName: flow.getAttribute('name') || '',
          targetId: flow.getAttribute('targetRef') || '',
          targetName: target ? target.getAttribute('name') || target.getAttribute('id') : flow.getAttribute('targetRef') || '',
          condition: state.flowConditions[flowId].condition || '',
          isDefault: Boolean(state.flowConditions[flowId].isDefault)
        };
      });
    });
}

function renderDerivedConfig() {
  const config = state.derivedConfig || {};
  const variables = config.variables || [];
  const topics = config.topics || [];
  const messages = config.messages || [];
  const required = config.requiredConfigurations || [];

  document.getElementById('derivedConfig').innerHTML = `
    <div class="item">
      <strong>Required Configurations</strong>
      ${renderTable(['type', 'key', 'reason'], required)}
    </div>
    <div class="item">
      <strong>Variables from Expressions</strong>
      ${renderTable(['name', 'source', 'expression'], variables)}
    </div>
    <div class="item">
      <strong>External Task Topics</strong>
      ${renderTable(['elementName', 'topic', 'elementId'], topics)}
    </div>
    <div class="item">
      <strong>BPMN Messages</strong>
      ${renderTable(['id', 'name'], messages)}
    </div>`;
}

function renderTable(columns, rows) {
  if (!rows.length) {
    return '<p><small>No items detected.</small></p>';
  }
  return `<table><thead><tr>${columns.map((column) => `<th>${column}</th>`).join('')}</tr></thead><tbody>${rows.map((row) => `<tr>${columns.map((column) => `<td>${row[column] || ''}</td>`).join('')}</tr>`).join('')}</tbody></table>`;
}

function renderRequirements() {
  const container = document.getElementById('requirements');
  const requirementHtml = state.requirements.map((item) => {
    if (item.type.includes('INBOUND_EVENT')) {
      const config = state.inboundConfigs[item.elementId] || item.inboundConfig || {};
      return `<div class="item">
        <strong>${item.name}</strong><br>
        <small>${item.elementId}</small>
        <p>${item.type}</p>
        <label>External Event</label>
        <input data-inbound="${item.elementId}" data-field="externalEvent" value="${config.externalEvent || ''}">
        <label>Camunda Message</label>
        <input data-inbound="${item.elementId}" data-field="camundaMessage" value="${config.camundaMessage || ''}">
        <label>Correlation Field</label>
        <input data-inbound="${item.elementId}" data-field="correlationField" value="${config.correlationField || 'correlationId'}">
        <label>Correlation Expression</label>
        <input data-inbound="${item.elementId}" data-field="correlationExpression" value="${config.correlationExpression || '${correlationId}'}">
        <label>Variable Mappings JSON</label>
        <textarea data-inbound="${item.elementId}" data-field="variableMappings">${config.variableMappings || defaultVariableMappings()}</textarea>
      </div>`;
    }

    const options = ['<option value="">Select capability</option>'].concat(state.capabilities.map((capability) => {
      const selected = item.capabilityId === capability.id ? 'selected' : '';
      return `<option value="${capability.id}" ${selected}>${capability.id} - ${capability.name}</option>`;
    })).join('');
    const hint = item.type === 'OUTBOUND_COMMUNICATION' && !isExternalTaskCapable(item.bpmnType)
      ? '<p><small>Message throw events are detected for configuration, but Camunda External Task execution should be modeled as a Send Task or Service Task.</small></p>'
      : '';
    return `<div class="item">
      <strong>${item.name}</strong><br>
      <small>${item.elementId}</small>
      <p>${item.type}</p>${hint}
      <select data-binding="${item.elementId}">${options}</select>
      ${renderOutboundConfigFields(item)}
    </div>`;
  }).join('');
  container.innerHTML = requirementHtml + renderGatewayBranchConditions();
  container.querySelectorAll('select[data-binding]').forEach((select) => {
    select.addEventListener('change', async () => {
      state.bindings[select.dataset.binding] = select.value;
      const requirement = state.requirements.find((item) => item.elementId === select.dataset.binding);
      if (requirement) {
        requirement.capabilityId = select.value;
      }
      await applyCapabilityToBpmnElement(select.dataset.binding, select.value);
      await saveXml();
    });
  });
  container.querySelectorAll('input[data-outbound], textarea[data-outbound]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.outbound;
      state.outboundConfigs[elementId] = state.outboundConfigs[elementId] || {
        bpmnElementId: elementId
      };
      state.outboundConfigs[elementId][input.dataset.field] = input.value;
      saveProject();
    });
  });
  container.querySelectorAll('input[data-flow-condition]').forEach((input) => {
    input.addEventListener('change', () => {
      const flowId = input.dataset.flowCondition;
      state.flowConditions[flowId] = state.flowConditions[flowId] || {};
      state.flowConditions[flowId].condition = input.value.trim();
      saveProject();
    });
  });
  container.querySelectorAll('input[data-flow-default]').forEach((input) => {
    input.addEventListener('change', () => {
      const gatewayId = input.dataset.gateway;
      const flowId = input.dataset.flowDefault;
      (state.gatewayBranches || [])
        .filter((branch) => branch.gatewayId === gatewayId)
        .forEach((branch) => {
          state.flowConditions[branch.flowId] = state.flowConditions[branch.flowId] || {};
          state.flowConditions[branch.flowId].isDefault = branch.flowId === flowId && input.checked;
        });
      renderRequirements();
      saveProject();
    });
  });
  container.querySelectorAll('input[data-inbound], textarea[data-inbound]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.inbound;
      state.inboundConfigs[elementId] = state.inboundConfigs[elementId] || {
        channel: 'EMAIL',
        action: 'CORRELATE_MESSAGE',
        bpmnElementId: elementId
      };
      state.inboundConfigs[elementId][input.dataset.field] = input.value.trim();
      if (input.dataset.field === 'camundaMessage') {
        updateBpmnMessageForEvent(elementId, input.value);
      }
      saveProject();
    });
  });
}

function renderGatewayBranchConditions() {
  const branches = state.gatewayBranches || [];
  if (!branches.length) {
    return '';
  }
  const grouped = branches.reduce((acc, branch) => {
    acc[branch.gatewayId] = acc[branch.gatewayId] || [];
    acc[branch.gatewayId].push(branch);
    return acc;
  }, {});

  return `
    <h3>Gateway Branch Conditions</h3>
    <p class="help-link"><a href="condition-expressions.html" target="_blank" rel="noopener">Abrir guia de expressoes de processamento de e-mails</a></p>
    ${Object.entries(grouped).map(([gatewayId, gatewayBranches]) => `
      <div class="item">
        <strong>${escapeHtml(gatewayBranches[0].gatewayName)}</strong><br>
        <small>${escapeHtml(gatewayId)}</small>
        ${gatewayBranches.map((branch) => {
          const config = state.flowConditions[branch.flowId] || {};
          const condition = config.condition || branch.condition || defaultEmailBodyConditionExpression(branch.flowName);
          const isDefault = Boolean(config.isDefault);
          return `
            <div class="branch-row">
              <strong>${escapeHtml(branch.flowName || branch.flowId)}</strong><br>
              <small>${escapeHtml(branch.flowId)} -> ${escapeHtml(branch.targetName || branch.targetId)}</small>
              <label>Condition Expression</label>
              <input data-flow-condition="${escapeAttribute(branch.flowId)}" value="${escapeAttribute(condition)}" ${isDefault ? 'disabled' : ''}>
              <label>
                <input type="checkbox" data-flow-default="${escapeAttribute(branch.flowId)}" data-gateway="${escapeAttribute(gatewayId)}" ${isDefault ? 'checked' : ''}>
                Default branch
              </label>
            </div>`;
        }).join('')}
      </div>`).join('')}`;
}

function renderOutboundConfigFields(item) {
  if (!item.type.includes('OUTBOUND_COMMUNICATION') || !isExternalTaskCapable(item.bpmnType)) {
    return '';
  }
  const config = state.outboundConfigs[item.elementId] || item.outboundConfig || {};
  return `
    <label>Email To</label>
    <input list="emailRecipientVariables" data-outbound="${item.elementId}" data-field="emailTo" value="${escapeAttribute(config.emailTo || '')}">
    <datalist id="emailRecipientVariables">
      ${emailRecipientVariableOptions().map((value) => `<option value="${escapeAttribute(value)}"></option>`).join('')}
    </datalist>
    <label>Email Subject</label>
    <input data-outbound="${item.elementId}" data-field="emailSubject" value="${escapeAttribute(config.emailSubject || '')}">
    <label>Email Body</label>
    <textarea data-outbound="${item.elementId}" data-field="emailBody">${escapeHtml(config.emailBody || '')}</textarea>`;
}

function defaultOutboundConfig(node) {
  const elementId = node.getAttribute('id');
  const name = node.getAttribute('name') || '';
  return {
    bpmnElementId: elementId,
    emailTo: defaultEmailTo(elementId, name),
    emailSubject: defaultEmailSubject(elementId, name),
    emailBody: defaultEmailBody(elementId, name)
  };
}

function defaultEmailTo(elementId, name = '') {
  if (elementId === 'Task_SolicitarConfirmacaoEstudante') {
    return '${studentEmail}';
  }
  if (elementId === 'Task_SolicitarConfirmacaoCoordenador') {
    return '${coordinatorEmail}';
  }
  if (matchesAny(`${elementId} ${name}`, 'Orientador', 'Orientacao')) {
    return '${requesterEmail}';
  }
  return '';
}

function emailRecipientVariableOptions() {
  return [
    '${requesterEmail}',
    '${studentEmail}',
    '${advisorEmail}',
    '${coordinatorEmail}',
    '${from}',
    '${emailTo}'
  ];
}

function defaultEmailSubject(elementId, name = '') {
  if (elementId === 'Task_SolicitarConfirmacaoEstudante') {
    return 'Confirmacao de vinculacao de orientacao [${correlationId}]';
  }
  if (elementId === 'Task_SolicitarConfirmacaoCoordenador') {
    return 'Confirmacao final de vinculacao de orientacao [${correlationId}]';
  }
  if (matchesAny(`${elementId} ${name}`, 'NaoConfirmadaEstudante', 'Nao Confirmacao Estudante', 'Não Confirmação Estudante')) {
    return 'Orientacao nao confirmada pelo estudante [${correlationId}]';
  }
  if (matchesAny(`${elementId} ${name}`, 'NaoAprovadaCoordenacao', 'Indeferimento Coordenador', 'Coordenador')) {
    return 'Orientacao nao aprovada pela coordenacao [${correlationId}]';
  }
  return '';
}

function defaultEmailBody(elementId, name = '') {
  if (elementId === 'Task_SolicitarConfirmacaoEstudante') {
    return 'Confirme a solicitacao de vinculacao de orientacao.\\n\\nEstudante: ${studentName}\\nOrientador: ${advisorName}\\nTitulo: ${title}\\nArea de pesquisa: ${researchArea}\\n\\nResponda este e-mail mantendo o identificador de correlacao.\\nCORRELATION-ID: ${correlationId}';
  }
  if (elementId === 'Task_SolicitarConfirmacaoCoordenador') {
    return 'Confirme a vinculacao de orientacao para registro final.\\n\\nCoordenador: ${coordinatorName}\\nPrograma: ${programName}\\nCampus: ${campus}\\nEstudante: ${studentName}\\nOrientador: ${advisorName}\\nTitulo: ${title}\\nArea de pesquisa: ${researchArea}\\n\\nResponda este e-mail mantendo o identificador de correlacao.\\nCORRELATION-ID: ${correlationId}';
  }
  if (matchesAny(`${elementId} ${name}`, 'NaoConfirmadaEstudante', 'Nao Confirmacao Estudante', 'Não Confirmação Estudante')) {
    return 'A solicitacao de vinculacao de orientacao nao foi confirmada pelo estudante.\\n\\nEstudante: ${studentName}\\nOrientador: ${advisorName}\\nTitulo: ${title}\\nArea de pesquisa: ${researchArea}\\n\\nO processo foi encerrado sem registro da vinculacao.\\nCORRELATION-ID: ${correlationId}';
  }
  if (matchesAny(`${elementId} ${name}`, 'NaoAprovadaCoordenacao', 'Indeferimento Coordenador', 'Coordenador')) {
    return 'A solicitacao de vinculacao de orientacao nao foi aprovada pela coordenacao.\\n\\nCoordenador: ${coordinatorName}\\nPrograma: ${programName}\\nCampus: ${campus}\\nEstudante: ${studentName}\\nOrientador: ${advisorName}\\nTitulo: ${title}\\nArea de pesquisa: ${researchArea}\\n\\nO processo foi encerrado sem registro da vinculacao.\\nCORRELATION-ID: ${correlationId}';
  }
  return '';
}

function matchesAny(value, ...needles) {
  const normalizedValue = normalizeSearchText(value);
  return needles.some((needle) => normalizedValue.includes(normalizeSearchText(needle)));
}

function normalizeSearchText(value) {
  return String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase();
}

function defaultInboundConfig(node) {
  const elementId = node.getAttribute('id');
  const name = node.getAttribute('name') || elementId;
  const eventName = normalizeEventName(name);
  return {
    channel: 'EMAIL',
    action: 'CORRELATE_MESSAGE',
    bpmnElementId: elementId,
    externalEvent: eventName,
    camundaMessage: eventName,
    correlationField: 'correlationId',
    correlationExpression: '${correlationId}',
    variableMappings: defaultVariableMappings()
  };
}

function defaultVariableMappings() {
  return JSON.stringify({
    from: '${email.from}',
    subject: '${email.subject}',
    body: '${email.body}',
    correlationId: '${correlationId}'
  }, null, 2);
}

async function updateBpmnMessageForEvent(elementId, messageName) {
  if (!messageName) {
    return;
  }
  await saveXml();
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const eventNode = doc.querySelector(`[id="${elementId}"]`);
  const messageEvent = eventNode ? eventNode.querySelector('messageEventDefinition, bpmn\\:messageEventDefinition') : null;
  if (!messageEvent) {
    return;
  }
  const messageNode = ensureMessageDefinition(doc, messageName);
  messageEvent.setAttribute('messageRef', messageNode.getAttribute('id'));
  consolidateMessageDefinitions(doc);
  xmlBox.value = new XMLSerializer().serializeToString(doc);
  await loadXml();
}

function ensureMessageDefinition(doc, messageName) {
  const existing = Array.from(doc.getElementsByTagName('*'))
    .find((node) => node.localName === 'message' && node.getAttribute('name') === messageName);
  if (existing) {
    return existing;
  }

  const definitions = doc.documentElement;
  const namespace = definitions.lookupNamespaceURI('bpmn') || definitions.namespaceURI;
  const prefix = definitions.lookupPrefix(namespace) || 'bpmn';
  const message = doc.createElementNS(namespace, `${prefix}:message`);
  message.setAttribute('id', uniqueXmlId(doc, `Message_${messageName}`));
  message.setAttribute('name', messageName);
  const firstProcess = Array.from(definitions.childNodes)
    .find((node) => node.nodeType === Node.ELEMENT_NODE && node.localName === 'process');
  definitions.insertBefore(message, firstProcess || definitions.firstChild);
  return message;
}

function uniqueXmlId(doc, base) {
  const normalized = base
    .replace(/[^A-Za-z0-9_]/g, '_')
    .replace(/^([^A-Za-z_])/, '_$1');
  let candidate = normalized;
  let index = 2;
  while (doc.querySelector(`[id="${candidate}"]`)) {
    candidate = `${normalized}_${index}`;
    index++;
  }
  return candidate;
}

function consolidateMessageDefinitions(doc) {
  const messagesByName = new Map();
  Array.from(doc.getElementsByTagName('*'))
    .filter((node) => node.localName === 'message')
    .forEach((message) => {
      const name = message.getAttribute('name');
      if (!name) {
        return;
      }
      if (!messagesByName.has(name)) {
        messagesByName.set(name, message);
        return;
      }
      const kept = messagesByName.get(name);
      const duplicateId = message.getAttribute('id');
      const keptId = kept.getAttribute('id');
      Array.from(doc.getElementsByTagName('*'))
        .filter((node) => node.localName === 'messageEventDefinition'
          && node.getAttribute('messageRef') === duplicateId)
        .forEach((definition) => definition.setAttribute('messageRef', keptId));
      message.parentNode.removeChild(message);
    });
}

function normalizeEventName(value) {
  return (value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toUpperCase() || 'INBOUND_EVENT';
}

function isOutboundNode(node) {
  if (node.localName === 'sendTask') {
    return true;
  }
  if ((node.localName === 'intermediateThrowEvent' || node.localName === 'endEvent') && node.querySelector('messageEventDefinition, bpmn\\:messageEventDefinition')) {
    return true;
  }
  return false;
}

function isExternalTaskCapable(bpmnType) {
  return bpmnType === 'sendTask' || bpmnType === 'serviceTask';
}

async function applyCapabilityToBpmnElement(elementId, capabilityId) {
  if (!capabilityId) {
    return;
  }
  await saveXml();
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const node = doc.querySelector(`[id="${elementId}"]`);
  if (!node || !isExternalTaskCapable(node.localName)) {
    return;
  }
  node.setAttribute('camunda:type', 'external');
  node.setAttribute('camunda:topic', capabilityId);
  xmlBox.value = new XMLSerializer().serializeToString(doc);
  await loadXml();
}

async function saveIntegration() {
  state.processConfig = {
    historyTimeToLive: document.getElementById('historyTimeToLive').value || '180'
  };
  state.integration = {
    channel: document.getElementById('integrationChannel').value,
    externalEvent: document.getElementById('externalEvent').value,
    subjectContains: document.getElementById('subjectContains').value,
    camundaMessage: document.getElementById('camundaMessage').value,
    correlationField: document.getElementById('correlationField').value
  };
  await updateProcessHistoryTimeToLive(state.processConfig.historyTimeToLive);
  renderIntegration();
  saveProject();
}

async function updateProcessHistoryTimeToLive(ttl) {
  await saveXml();
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const processNode = doc.querySelector('process, bpmn\\:process');
  if (processNode && ttl) {
    processNode.setAttribute('camunda:historyTimeToLive', ttl);
    xmlBox.value = new XMLSerializer().serializeToString(doc);
    await loadXml();
  }
}

function renderIntegration() {
  const integration = state.integration;
  document.getElementById('integrationSummary').innerHTML = `<pre>${JSON.stringify(integration, null, 2)}</pre>`;
}

function validateProject() {
  synchronizeEventDefinitionOrder();
  synchronizeGatewayBranchLabelsToConditions();
  synchronizeFlowConditionsToBpmn();
  const errors = [];
  if (!document.getElementById('projectKey').value) errors.push('Project key is required');
  if (!xmlBox.value.includes('<bpmn:definitions')) errors.push('BPMN XML is required');
  errors.push(...validateExclusiveGateways(xmlBox.value));
  if (state.requirements.length === 0) errors.push('Analyze BPMN before deployment');
  state.requirements
    .filter((item) => item.type.includes('SERVICE_CAPABILITY') || item.type.includes('OUTBOUND_COMMUNICATION'))
    .forEach((item) => {
      if (!state.bindings[item.elementId]) errors.push(`Capability binding missing: ${item.name}`);
      const outboundConfig = state.outboundConfigs[item.elementId];
      const usesGenericEmail = state.bindings[item.elementId] === 'SEND_EMAIL'
        && !isBuiltInSendEmailElement(item.elementId)
        && item.type.includes('OUTBOUND_COMMUNICATION');
      if (usesGenericEmail && (!outboundConfig || !outboundConfig.emailTo || !outboundConfig.emailTo.trim())) {
        errors.push(`SEND_EMAIL recipient missing: ${item.name}`);
      }
      if (usesGenericEmail && (!outboundConfig || !outboundConfig.emailSubject || !outboundConfig.emailSubject.trim())) {
        errors.push(`SEND_EMAIL subject missing: ${item.name}`);
      }
      if (usesGenericEmail && (!outboundConfig || !outboundConfig.emailBody || !outboundConfig.emailBody.trim())) {
        errors.push(`SEND_EMAIL body missing: ${item.name}`);
      }
      if (state.bindings[item.elementId] === 'SEND_EMAIL'
          && expectsEmailReply(item)
          && outboundConfig
          && !containsCorrelationToken(outboundConfig.emailSubject, outboundConfig.emailBody)) {
        errors.push(`Correlation token missing in email subject/body: ${item.name}. Include \${correlationId}.`);
      }
    });
  state.requirements
    .filter((item) => item.type.includes('INBOUND_EVENT'))
    .forEach((item) => {
      const config = state.inboundConfigs[item.elementId] || item.inboundConfig;
      if (!config || !config.externalEvent || !config.camundaMessage || !config.correlationField || !config.correlationExpression) {
        errors.push(`Inbound/correlation configuration missing: ${item.name}`);
      }
      if (config && config.variableMappings) {
        try {
          JSON.parse(config.variableMappings);
        } catch (error) {
          errors.push(`Variable mappings must be valid JSON: ${item.name}`);
        }
      }
    });
  if (!state.integration.correlationField) errors.push('Correlation field is required');

  if (errors.length) {
    setStatus('DRAFT');
    return { valid: false, errors };
  }
  setStatus('VALID');
  return { valid: true, errors: [] };
}

function synchronizeFlowConditionsToBpmn() {
  if (!xmlBox.value.includes('<bpmn:definitions') && !xmlBox.value.includes('<definitions')) {
    return;
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  applyFlowConditions(doc);
  xmlBox.value = new XMLSerializer().serializeToString(doc);
  saveProject();
}

function synchronizeEventDefinitionOrder() {
  if (!xmlBox.value.includes('<bpmn:definitions') && !xmlBox.value.includes('<definitions')) {
    return;
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const events = Array.from(doc.getElementsByTagName('*'))
    .filter((node) => node.localName === 'startEvent'
      || node.localName === 'intermediateCatchEvent'
      || node.localName === 'intermediateThrowEvent'
      || node.localName === 'endEvent');
  let changed = false;

  events.forEach((event) => {
    const definitionNodes = Array.from(event.childNodes)
      .filter((node) => node.nodeType === Node.ELEMENT_NODE
        && (node.localName === 'messageEventDefinition'
          || node.localName === 'timerEventDefinition'
          || node.localName === 'conditionalEventDefinition'
          || node.localName === 'signalEventDefinition'
          || node.localName === 'errorEventDefinition'
          || node.localName === 'escalationEventDefinition'
          || node.localName === 'eventDefinitionRef'));
    definitionNodes.forEach((definition) => {
      if (definition.nextSibling) {
        event.appendChild(definition);
        changed = true;
      }
    });
  });

  if (changed) {
    xmlBox.value = new XMLSerializer().serializeToString(doc);
    saveProject();
  }
}

function synchronizeGatewayBranchLabelsToConditions() {
  if (!xmlBox.value.includes('<bpmn:definitions') && !xmlBox.value.includes('<definitions')) {
    return;
  }

  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const root = doc.documentElement;
  const elementsByLocalName = (name) => Array.from(doc.getElementsByTagName('*'))
    .filter((node) => node.localName === name);
  const sequenceFlows = elementsByLocalName('sequenceFlow');
  let changed = false;

  elementsByLocalName('exclusiveGateway').forEach((gateway) => {
    const gatewayId = gateway.getAttribute('id');
    const defaultFlow = gateway.getAttribute('default');
    sequenceFlows
      .filter((flow) => flow.getAttribute('sourceRef') === gatewayId)
      .filter((flow) => flow.getAttribute('id') !== defaultFlow)
      .forEach((flow) => {
        const hasCondition = Array.from(flow.childNodes)
          .some((node) => node.nodeType === Node.ELEMENT_NODE && node.localName === 'conditionExpression');
        if (hasCondition) {
          return;
        }

        const label = flow.getAttribute('name');
        if (!label || !label.trim()) {
          return;
        }
        if (!label.trim().startsWith('${')) {
          return;
        }

        const bpmnPrefix = flow.prefix || gateway.prefix || 'bpmn';
        const conditionNodeName = flow.prefix ? `${flow.prefix}:conditionExpression` : 'conditionExpression';
        const condition = flow.namespaceURI
          ? doc.createElementNS(flow.namespaceURI, conditionNodeName)
          : doc.createElement(conditionNodeName);
        ensureXsiNamespace(root);
        condition.setAttributeNS('http://www.w3.org/2001/XMLSchema-instance', 'xsi:type', `${bpmnPrefix}:tFormalExpression`);
        condition.textContent = normalizeConditionExpression(label);
        flow.appendChild(condition);
        changed = true;
      });
  });

  if (changed) {
    xmlBox.value = new XMLSerializer().serializeToString(doc);
    saveProject();
  }
}

function ensureXsiNamespace(root) {
  if (!root.hasAttribute('xmlns:xsi')) {
    root.setAttribute('xmlns:xsi', 'http://www.w3.org/2001/XMLSchema-instance');
  }
}

function normalizeConditionExpression(label) {
  const value = label.trim();
  if (value.startsWith('${') && value.endsWith('}')) {
    return value;
  }
  if (/^(true|false)$/i.test(value)) {
    return '${' + value.toLowerCase() + '}';
  }
  return '${' + value + '}';
}

function defaultEmailBodyConditionExpression(branchText) {
  const text = (branchText || '').trim();
  if (!text || text.startsWith('${')) {
    return '';
  }
  return '${body != null && body.toLowerCase().contains("' + escapeJavaStringLiteral(text.toLowerCase()) + '")}';
}

function escapeJavaStringLiteral(value) {
  return String(value)
    .replace(/\\/g, '\\\\')
    .replace(/"/g, '\\"');
}

function validateExclusiveGateways(xml) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xml, 'text/xml');
  const errors = [];
  const elementsByLocalName = (name) => Array.from(doc.getElementsByTagName('*'))
    .filter((node) => node.localName === name);
  const sequenceFlows = elementsByLocalName('sequenceFlow');

  elementsByLocalName('exclusiveGateway').forEach((gateway) => {
    const gatewayId = gateway.getAttribute('id');
    const defaultFlow = gateway.getAttribute('default');
    const outgoingIds = Array.from(gateway.childNodes)
      .filter((node) => node.nodeType === Node.ELEMENT_NODE && node.localName === 'outgoing')
      .map((node) => node.textContent.trim())
      .filter(Boolean);
    const flowIds = outgoingIds.length
      ? outgoingIds
      : sequenceFlows
        .filter((flow) => flow.getAttribute('sourceRef') === gatewayId)
        .map((flow) => flow.getAttribute('id'))
        .filter(Boolean);

    if (flowIds.length <= 1) {
      return;
    }

    const unconditionalFlows = flowIds.map((flowId) => {
      if (flowId === defaultFlow) {
        return null;
      }
      const flow = sequenceFlows.find((item) => item.getAttribute('id') === flowId);
      const hasCondition = flow && Array.from(flow.childNodes)
        .some((node) => node.nodeType === Node.ELEMENT_NODE
          && node.localName === 'conditionExpression'
          && node.textContent.trim());
      if (hasCondition) {
        return null;
      }
      return {
        id: flowId,
        label: flow ? flow.getAttribute('name') : ''
      };
    }).filter(Boolean);

    if (!defaultFlow && unconditionalFlows.length > 0) {
      const flowDetails = unconditionalFlows
        .map((flow) => flow.label ? `${flow.id} (label: ${flow.label})` : flow.id)
        .join(', ');
      errors.push(`Exclusive gateway ${gatewayId} has outgoing flows without conditionExpression: ${flowDetails}. A branch label/name is not a BPMN condition.`);
      return;
    }

    unconditionalFlows.forEach((flow) => {
      errors.push(`Exclusive gateway ${gatewayId} outgoing flow ${flow.id} needs conditionExpression or must be the default flow`);
    });
  });
  return errors;
}

async function deploy() {
  const deployButton = document.getElementById('deploy');
  const startedAt = new Date();
  const progress = startDeploymentProgress(startedAt);
  setStatus('DEPLOYING');
  deployButton.disabled = true;
  renderDeploymentProgress(progress, 'Deploy em andamento: clique recebido');
  writeDeploymentResult({
    status: 'DEPLOYING',
    message: 'Deploy em andamento',
    stage: progress.stage,
    elapsedSeconds: 0,
    startedAt: startedAt.toISOString()
  });

  const controller = new AbortController();
  const requestTimeoutMs = 90000;
  const timeoutId = setTimeout(() => controller.abort(), requestTimeoutMs);
  const tickId = setInterval(() => {
    renderDeploymentProgress(progress);
  }, 1000);

  try {
    renderDeploymentProgress(progress, 'Salvando XML do modeler');
    await runStageWithTimeout(() => saveXml(), 10000, 'saveXml');

    renderDeploymentProgress(progress, 'Normalizando XML para regras do Camunda');
    await runStageWithTimeout(async () => {
      synchronizeEventDefinitionOrder();
      synchronizeGatewayBranchLabelsToConditions();
      await synchronizeAutomationConfigurationToBpmn(false);
    }, 10000, 'BPMN normalization');

    renderDeploymentProgress(progress, 'Validando projeto no ADE');
    const validation = validateProject();
    if (!validation.valid) {
      writeDeploymentResult({
        status: 'FAILED',
        message: 'Validation failed before deployment',
        stage: progress.stage,
        elapsedSeconds: elapsedSeconds(progress),
        errors: validation.errors
      });
      setStatus('FAILED');
      return;
    }

    const payload = {
      projectKey: document.getElementById('projectKey').value,
      version: document.getElementById('projectVersion').value,
      bpmnXml: xmlBox.value,
      integration: state.integration,
      inboundIntegrations: Object.values(state.inboundConfigs)
    };

    renderDeploymentProgress(progress, 'Enviando requisicao para /api/deployments');
    const response = await fetch('/api/deployments', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      signal: controller.signal,
      body: JSON.stringify(payload)
    });

    renderDeploymentProgress(progress, `Resposta HTTP recebida: ${response.status}`);
    const text = await response.text();
    const result = parseJsonOrFailure(text, response);
    result.stage = progress.stage;
    result.elapsedSeconds = elapsedSeconds(progress);
    writeDeploymentResult(result);
    setStatus(result.status || 'FAILED');
  } catch (error) {
    const message = error.name === 'AbortError'
      ? `Timeout apos ${Math.round(requestTimeoutMs / 1000)}s aguardando resposta de /api/deployments`
      : `Deployment request failed: ${error.message || error}`;
    const result = {
      status: 'FAILED',
      message,
      stage: progress.stage,
      elapsedSeconds: elapsedSeconds(progress)
    };
    renderDeploymentProgress(progress, message);
    writeDeploymentResult(result);
    setStatus('FAILED');
  } finally {
    clearTimeout(timeoutId);
    clearInterval(tickId);
    deployButton.disabled = false;
    renderDeploymentProgress(progress);
    saveProject();
  }
}

function startDeploymentProgress(startedAt) {
  return {
    startedAt,
    stage: 'Aguardando inicio',
    lastUpdate: '',
    history: []
  };
}

function renderDeploymentProgress(progress, stage) {
  if (stage) {
    progress.stage = stage;
    progress.lastUpdate = new Date().toISOString();
    progress.history.push({
      at: progress.lastUpdate,
      elapsedSeconds: elapsedSeconds(progress),
      stage
    });
  }

  const container = document.getElementById('deploymentProgress');
  if (!container) {
    return;
  }
  container.innerHTML = `
    <strong>Deploy em andamento</strong>
    <p>Tempo: ${elapsedSeconds(progress)}s</p>
    <p>Etapa atual: ${escapeHtml(progress.stage)}</p>
    <p>Ultima atualizacao: ${escapeHtml(progress.lastUpdate || '-')}</p>
  `;
}

function writeDeploymentResult(result) {
  document.getElementById('deploymentResult').textContent = JSON.stringify(result, null, 2);
}

function elapsedSeconds(progress) {
  return Math.round((Date.now() - progress.startedAt.getTime()) / 1000);
}

async function runStageWithTimeout(action, timeoutMs, label) {
  let timeoutId;
  const timeout = new Promise((_, reject) => {
    timeoutId = setTimeout(() => reject(new Error(`${label} did not finish within ${Math.round(timeoutMs / 1000)}s`)), timeoutMs);
  });
  try {
    return await Promise.race([action(), timeout]);
  } finally {
    clearTimeout(timeoutId);
  }
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function escapeAttribute(value) {
  return escapeHtml(value).replace(/`/g, '&#096;');
}

function isBuiltInSendEmailElement(elementId) {
  return [
    'Task_SolicitarDados',
    'Task_SolicitarConfirmacaoEstudante',
    'Task_SolicitarConfirmacaoCoordenador'
  ].includes(elementId);
}

function expectsEmailReply(item) {
  const value = normalizeSearchText(`${item.elementId} ${item.name}`);
  return value.includes('confirmacao')
    || value.includes('confirm')
    || value.includes('aprovacao')
    || value.includes('aprovar');
}

function containsCorrelationToken(subject, body) {
  return `${subject || ''}\n${body || ''}`.includes('${correlationId}');
}

function parseJsonOrFailure(text, response) {
  try {
    const result = JSON.parse(text);
    if (!response.ok && !result.status) {
      return {
        status: 'FAILED',
        message: text
      };
    }
    return result;
  } catch (error) {
    return {
      status: 'FAILED',
      message: text || `HTTP ${response.status}`
    };
  }
}

async function synchronizeAutomationConfigurationToBpmn(reloadCanvas = true) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  Object.entries(state.bindings).forEach(([elementId, capabilityId]) => {
    if (!capabilityId) {
      return;
    }
    const node = doc.querySelector(`[id="${elementId}"]`);
    if (node && isExternalTaskCapable(node.localName)) {
      node.setAttribute('camunda:type', 'external');
      node.setAttribute('camunda:topic', capabilityId);
    }
  });
  Object.entries(state.outboundConfigs || {}).forEach(([elementId, config]) => {
    const node = doc.querySelector(`[id="${elementId}"]`);
    if (node && isExternalTaskCapable(node.localName)) {
      applyOutboundInputParameters(doc, node, config);
    }
  });
  applyFlowConditions(doc);
  consolidateMessageDefinitions(doc);
  if (state.processConfig.historyTimeToLive) {
    const processNode = doc.querySelector('process, bpmn\\:process');
    if (processNode) {
      processNode.setAttribute('camunda:historyTimeToLive', state.processConfig.historyTimeToLive);
    }
  }
  xmlBox.value = new XMLSerializer().serializeToString(doc);
  if (reloadCanvas) {
    await loadXml();
  }
  saveProject();
}

function applyFlowConditions(doc) {
  const elements = Array.from(doc.getElementsByTagName('*'));
  const sequenceFlows = elements.filter((node) => node.localName === 'sequenceFlow');
  const gateways = elements.filter((node) => node.localName === 'exclusiveGateway');

  gateways.forEach((gateway) => {
    const gatewayId = gateway.getAttribute('id');
    const branches = (state.gatewayBranches || [])
      .filter((branch) => branch.gatewayId === gatewayId);
    const defaultBranch = branches.find((branch) => {
      const config = state.flowConditions[branch.flowId] || {};
      return Boolean(config.isDefault);
    });

    if (defaultBranch) {
      gateway.setAttribute('default', defaultBranch.flowId);
    } else {
      gateway.removeAttribute('default');
    }

    branches.forEach((branch) => {
      const flow = sequenceFlows.find((item) => item.getAttribute('id') === branch.flowId);
      if (!flow) {
        return;
      }
      removeConditionExpression(flow);
      const config = state.flowConditions[branch.flowId] || {};
      const condition = (config.condition || '').trim();
      if (!condition || (defaultBranch && defaultBranch.flowId === branch.flowId)) {
        return;
      }
      const conditionNode = createConditionExpression(doc, flow);
      conditionNode.textContent = normalizeConditionExpression(condition);
      flow.appendChild(conditionNode);
    });
  });
}

function removeConditionExpression(flow) {
  Array.from(flow.childNodes)
    .filter((node) => node.nodeType === Node.ELEMENT_NODE && node.localName === 'conditionExpression')
    .forEach((node) => node.parentNode.removeChild(node));
}

function createConditionExpression(doc, flow) {
  const namespace = flow.namespaceURI || 'http://www.omg.org/spec/BPMN/20100524/MODEL';
  const prefix = flow.prefix || doc.documentElement.lookupPrefix(namespace) || 'bpmn';
  ensureXsiNamespace(doc.documentElement);
  const condition = doc.createElementNS(namespace, `${prefix}:conditionExpression`);
  condition.setAttributeNS('http://www.w3.org/2001/XMLSchema-instance', 'xsi:type', `${prefix}:tFormalExpression`);
  return condition;
}

function applyOutboundInputParameters(doc, node, config) {
  const values = {
    outboundEmailTo: config.emailTo || '',
    outboundEmailSubject: config.emailSubject || '',
    outboundEmailBody: config.emailBody || ''
  };
  const hasAnyValue = Object.values(values).some((value) => value.trim());
  removeManagedInputParameters(node, Object.keys(values));
  if (!hasAnyValue) {
    removeEmptyInputOutput(node);
    return;
  }

  const extensionElements = ensureExtensionElements(doc, node);
  const inputOutput = ensureInputOutput(doc, extensionElements);
  Object.entries(values)
    .filter(([, value]) => value.trim())
    .forEach(([name, value]) => {
      const input = createCamundaElement(doc, 'inputParameter');
      input.setAttribute('name', name);
      input.textContent = value;
      inputOutput.appendChild(input);
    });
}

function removeManagedInputParameters(node, names) {
  node.querySelectorAll('inputParameter, camunda\\:inputParameter').forEach((input) => {
    if (names.includes(input.getAttribute('name'))) {
      input.parentNode.removeChild(input);
    }
  });
}

function removeEmptyInputOutput(node) {
  node.querySelectorAll('inputOutput, camunda\\:inputOutput').forEach((inputOutput) => {
    const hasChildren = Array.from(inputOutput.childNodes)
      .some((child) => child.nodeType === Node.ELEMENT_NODE);
    if (!hasChildren) {
      const extensionElements = inputOutput.parentNode;
      extensionElements.removeChild(inputOutput);
      const hasExtensionChildren = Array.from(extensionElements.childNodes)
        .some((child) => child.nodeType === Node.ELEMENT_NODE);
      if (!hasExtensionChildren) {
        extensionElements.parentNode.removeChild(extensionElements);
      }
    }
  });
}

function ensureExtensionElements(doc, node) {
  const existing = Array.from(node.childNodes)
    .find((child) => child.nodeType === Node.ELEMENT_NODE && child.localName === 'extensionElements');
  if (existing) {
    return existing;
  }
  const extensionElements = createBpmnElement(doc, 'extensionElements');
  const firstElement = Array.from(node.childNodes)
    .find((child) => child.nodeType === Node.ELEMENT_NODE);
  node.insertBefore(extensionElements, firstElement || null);
  return extensionElements;
}

function ensureInputOutput(doc, extensionElements) {
  const existing = Array.from(extensionElements.childNodes)
    .find((child) => child.nodeType === Node.ELEMENT_NODE && child.localName === 'inputOutput');
  if (existing) {
    return existing;
  }
  const inputOutput = createCamundaElement(doc, 'inputOutput');
  extensionElements.appendChild(inputOutput);
  return inputOutput;
}

function createBpmnElement(doc, localName) {
  const definitions = doc.documentElement;
  const namespace = definitions.lookupNamespaceURI('bpmn') || 'http://www.omg.org/spec/BPMN/20100524/MODEL';
  const prefix = definitions.lookupPrefix(namespace) || 'bpmn';
  return doc.createElementNS(namespace, `${prefix}:${localName}`);
}

function createCamundaElement(doc, localName) {
  const definitions = doc.documentElement;
  const namespace = definitions.lookupNamespaceURI('camunda') || 'http://camunda.org/schema/1.0/bpmn';
  if (!definitions.lookupPrefix(namespace)) {
    definitions.setAttribute('xmlns:camunda', namespace);
  }
  return doc.createElementNS(namespace, `camunda:${localName}`);
}

function renderProject() {
  document.getElementById('projectSummary').innerHTML = `<pre>${JSON.stringify(project(), null, 2)}</pre>`;
}

async function init() {
  state.capabilities = await fetch('/api/capabilities').then((response) => response.json());
  const runtime = await fetch('/api/runtime').then((response) => response.json());
  document.getElementById('runtime').textContent = `Camunda: ${runtime.camundaBaseUrl}`;
  openProject();
}

document.querySelectorAll('button[data-tab]').forEach((button) => {
  button.addEventListener('click', () => {
    document.querySelectorAll('section').forEach((section) => section.classList.remove('active'));
    document.getElementById(button.dataset.tab).classList.add('active');
  });
});

document.getElementById('loadReference').addEventListener('click', () => {
  if (!confirm('Load the reference BPMN and replace the current XML in the editor?')) {
    return;
  }
  xmlBox.value = referenceXml;
  loadXml();
  saveProject();
  setStatus('DRAFT');
});
document.getElementById('saveProject').addEventListener('click', saveProject);
document.getElementById('loadXml').addEventListener('click', loadXml);
document.getElementById('saveXml').addEventListener('click', saveXml);
document.getElementById('exportXml').addEventListener('click', saveXml);
document.getElementById('analyze').addEventListener('click', analyzeBpmn);
document.getElementById('saveIntegration').addEventListener('click', saveIntegration);
document.getElementById('runCir').addEventListener('click', async () => {
  const bindingId = document.getElementById('executionBindingId').value || 'ppcomp-main';
  const response = await fetch('/api/execution/cir', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ bindingId })
  });
  const text = await response.text();
  try {
    document.getElementById('executionInfo').textContent = JSON.stringify(JSON.parse(text), null, 2);
  } catch (error) {
    document.getElementById('executionInfo').textContent = text;
  }
});
document.getElementById('validate').addEventListener('click', () => {
  document.getElementById('deploymentResult').textContent = JSON.stringify(validateProject(), null, 2);
});
document.getElementById('deploy').addEventListener('click', deploy);

init();
