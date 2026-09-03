const state = {
  projectId: '',
  description: '',
  activeProjectOpen: false,
  dirty: false,
  lastSavedAt: '',
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
  variableMappings: {},
  capabilityImplementations: {},
  inboundConfigs: {},
  outboundConfigs: {},
  flowConditions: {},
  integration: {},
  processConfig: { historyTimeToLive: '180' },
  wizardSession: null,
  status: 'DRAFT'
};
const PROJECTS_STORAGE_KEY = 'propos26.ade.projects';
const ACTIVE_PROJECT_STORAGE_KEY = 'propos26.ade.activeProjectId';
const PROJECT_SEED_STORAGE_KEY = 'propos26.ade.projects.seeded';
const LEGACY_CAPABILITY_ALIASES = {
  VALIDATE_ADVISORSHIP_REQUEST: 'CHECK_ADVISORSHIP',
  REGISTER_ADVISORSHIP: 'CREATE_ADVISORSHIP',
  VALIDATE_REQUEST: 'CHECK_ADVISORSHIP'
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

function emptyBpmnXml(projectKey = 'automation_process', projectName = 'Automation Process') {
  const processId = normalizeXmlId(projectKey || 'automation_process');
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="Definitions_${processId}" targetNamespace="http://propos26.local/bpmn">
  <bpmn:process id="${processId}" name="${escapeXml(projectName)}" isExecutable="true" camunda:historyTimeToLive="180">
    <bpmn:startEvent id="StartEvent_1" name="Start">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:endEvent id="EndEvent_1" name="End">
      <bpmn:incoming>Flow_1</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="EndEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="${processId}">
    <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1"><dc:Bounds x="160" y="160" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1"><dc:Bounds x="320" y="160" width="36" height="36" /></bpmndi:BPMNShape>
    <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1"><di:waypoint x="196" y="178" /><di:waypoint x="320" y="178" /></bpmndi:BPMNEdge>
  </bpmndi:BPMNPlane></bpmndi:BPMNDiagram>
</bpmn:definitions>`;
}

function project() {
  const inboundIntegrations = Object.values(state.inboundConfigs || {});
  const outboundIntegrations = Object.values(state.outboundConfigs || {});
  const capabilityBindings = Object.entries(state.bindings || {}).map(([bpmnElementId, capabilityId]) => ({
    bpmnElementId,
    capabilityId,
    bindingType: 'CAPABILITY',
    variableMappings: state.variableMappings[bpmnElementId] || {},
    implementation: state.capabilityImplementations[bpmnElementId] || {}
  }));
  const correlationDefinitions = inboundIntegrations.map((config) => ({
    bpmnElementId: config.bpmnElementId,
    externalEvent: config.externalEvent,
    camundaMessage: config.camundaMessage,
    correlationField: config.correlationField,
    correlationExpression: config.correlationExpression
  }));
  return {
    projectId: state.projectId,
    name: document.getElementById('projectName').value,
    key: document.getElementById('projectKey').value,
    version: document.getElementById('projectVersion').value,
    description: document.getElementById('projectDescription').value,
    bpmnXml: xmlBox.value,
    metadata: {
      projectId: state.projectId,
      name: document.getElementById('projectName').value,
      key: document.getElementById('projectKey').value,
      version: document.getElementById('projectVersion').value,
      description: document.getElementById('projectDescription').value
    },
    requirements: state.requirements,
    automationRequirements: state.requirements,
    derivedConfig: state.derivedConfig,
    bindings: state.bindings,
    capabilityBindings,
    capabilityImplementations: state.capabilityImplementations,
    variableMappings: deriveVariableMappingsSnapshot(),
    inboundConfigs: state.inboundConfigs,
    inboundIntegrations,
    outboundConfigs: state.outboundConfigs,
    outboundIntegrations,
    correlationDefinitions,
    generatedComponents: state.generatedComponents || [],
    deploymentConfiguration: {
      processConfig: state.processConfig,
      integration: state.integration
    },
    deploymentHistory: state.deploymentHistory || [],
    wizardSession: state.wizardSession,
    flowConditions: state.flowConditions,
    processConfig: state.processConfig,
    integration: state.integration,
    status: state.status,
    lastModified: state.lastSavedAt || new Date().toISOString()
  };
}

async function loadXml() {
  await modeler.importXML(xmlBox.value);
  modeler.get('canvas').zoom('fit-viewport');
}

async function saveXml() {
  if (!state.activeProjectOpen) {
    return;
  }
  const result = await modeler.saveXML({ format: true });
  xmlBox.value = result.xml;
  markDirty();
}

function setStatus(status) {
  state.status = status;
  document.getElementById('projectStatus').textContent = status;
  markDirty(false);
}

function savedProjects() {
  return JSON.parse(localStorage.getItem(PROJECTS_STORAGE_KEY) || '{}');
}

function writeProjects(projects) {
  localStorage.setItem(PROJECTS_STORAGE_KEY, JSON.stringify(projects));
}

function seedExampleProjects() {
  if (localStorage.getItem(PROJECT_SEED_STORAGE_KEY)) {
    return;
  }
  const projects = savedProjects();
  const now = new Date().toISOString();
  const vinculationId = 'vinculacao-orientacao-reference';
  const defenseId = 'defesa-de-mestrado-example';
  if (!projects[vinculationId]) {
    projects[vinculationId] = {
      projectId: vinculationId,
      name: 'Vinculacao de Orientacao',
      key: 'vinculacao-orientacao',
      version: '1.0',
      description: 'Reference automation project for ADE validation.',
      bpmnXml: referenceXml,
      requirements: [],
      derivedConfig: { variables: [], topics: [], messages: [], requiredConfigurations: [] },
      bindings: {},
      variableMappings: {},
      capabilityImplementations: {},
      inboundConfigs: {},
      outboundConfigs: {},
      flowConditions: {},
      processConfig: { historyTimeToLive: '180' },
      integration: {},
      generatedComponents: [],
      deploymentHistory: [],
      wizardSession: null,
      status: 'DRAFT',
      lastModified: now
    };
  }
  if (!projects[defenseId]) {
    projects[defenseId] = {
      projectId: defenseId,
      name: 'Defesa de Mestrado',
      key: 'defesa-de-mestrado',
      version: '1.0',
      description: 'Empty example project showing coexistence of multiple automations.',
      bpmnXml: emptyBpmnXml('defesa-de-mestrado', 'Defesa de Mestrado'),
      requirements: [],
      derivedConfig: { variables: [], topics: [], messages: [], requiredConfigurations: [] },
      bindings: {},
      variableMappings: {},
      capabilityImplementations: {},
      inboundConfigs: {},
      outboundConfigs: {},
      flowConditions: {},
      processConfig: { historyTimeToLive: '180' },
      integration: {},
      generatedComponents: [],
      deploymentHistory: [],
      wizardSession: null,
      status: 'DRAFT',
      lastModified: now
    };
  }
  writeProjects(projects);
  localStorage.setItem(PROJECT_SEED_STORAGE_KEY, 'true');
}

function saveProject() {
  if (!state.activeProjectOpen) {
    return;
  }
  const projects = savedProjects();
  state.lastSavedAt = new Date().toISOString();
  const data = project();
  data.lastModified = state.lastSavedAt;
  projects[state.projectId] = data;
  writeProjects(projects);
  localStorage.setItem(ACTIVE_PROJECT_STORAGE_KEY, state.projectId);
  state.dirty = false;
  updateProjectShell();
  renderProject();
  renderProjectList();
}

function openInitialProject() {
  localStorage.removeItem(ACTIVE_PROJECT_STORAGE_KEY);
  closeProject(false);
}

async function openProject(projectId) {
  if (!confirmUnsavedChanges()) {
    return;
  }
  const data = savedProjects()[projectId];
  if (!data) {
    return;
  }
  hydrateProject(data);
  renderRequirements();
  renderDerivedConfig();
  renderIntegration();
  renderProject();
  renderProjectList();
  updateProjectShell();
  showTab('bpmn');
  try {
    await loadXml();
  } catch (error) {
    document.getElementById('deploymentResult').textContent = `Could not render BPMN: ${error.message || error}`;
  }
}

function showProjectBrowser() {
  if (!confirmUnsavedChanges()) {
    return;
  }
  document.body.classList.add('project-browser');
  showTab('projects');
  renderProjectList();
}

function hydrateProject(data) {
  state.projectId = data.projectId || data.metadata?.projectId || createProjectId(data.key || 'automation');
  state.description = data.description || data.metadata?.description || '';
  state.activeProjectOpen = true;
  state.dirty = false;
  state.lastSavedAt = data.lastModified || '';
  state.generatedComponents = data.generatedComponents || [];
  state.deploymentHistory = data.deploymentHistory || [];
  state.wizardSession = data.wizardSession || null;
  document.getElementById('projectName').value = data.name || '';
  document.getElementById('projectKey').value = data.key || '';
  document.getElementById('projectVersion').value = data.version || '';
  document.getElementById('projectDescription').value = state.description;
  xmlBox.value = data.bpmnXml || referenceXml;
  state.requirements = data.requirements || [];
  state.derivedConfig = data.derivedConfig || {
    variables: [],
    topics: [],
    messages: [],
    requiredConfigurations: []
  };
  state.bindings = migrateCapabilityBindings(data.bindings || {});
  state.variableMappings = data.variableMappings && !Array.isArray(data.variableMappings) ? data.variableMappings : {};
  state.capabilityImplementations = data.capabilityImplementations || deriveCapabilityImplementations(data.capabilityBindings || []);
  state.inboundConfigs = data.inboundConfigs || {};
  state.outboundConfigs = data.outboundConfigs || {};
  state.flowConditions = data.flowConditions || {};
  state.processConfig = data.processConfig || { historyTimeToLive: '180' };
  state.integration = data.integration || {};
  document.getElementById('historyTimeToLive').value = state.processConfig.historyTimeToLive || '180';
  setStatus(data.status || 'DRAFT');
  state.dirty = false;
}

function closeProject(checkUnsaved = true) {
  if (checkUnsaved && !confirmUnsavedChanges()) {
    return;
  }
  document.body.classList.remove('project-browser');
  state.projectId = '';
  state.description = '';
  state.activeProjectOpen = false;
  state.dirty = false;
  state.lastSavedAt = '';
  state.requirements = [];
  state.gatewayBranches = [];
  state.derivedConfig = { variables: [], topics: [], messages: [], requiredConfigurations: [] };
  state.bindings = {};
  state.variableMappings = {};
  state.capabilityImplementations = {};
  state.inboundConfigs = {};
  state.outboundConfigs = {};
  state.flowConditions = {};
  state.integration = {};
  state.processConfig = { historyTimeToLive: '180' };
  state.generatedComponents = [];
  state.deploymentHistory = [];
  state.wizardSession = null;
  document.getElementById('projectName').value = '';
  document.getElementById('projectKey').value = '';
  document.getElementById('projectVersion').value = '';
  document.getElementById('projectDescription').value = '';
  xmlBox.value = '';
  localStorage.removeItem(ACTIVE_PROJECT_STORAGE_KEY);
  setStatus('DRAFT');
  state.dirty = false;
  renderRequirements();
  renderDerivedConfig();
  renderIntegration();
  renderProject();
  renderProjectList();
  updateProjectShell();
  document.querySelectorAll('section').forEach((section) => section.classList.remove('active'));
  document.getElementById('projects').classList.add('active');
}

function newProject(useReference = false) {
  if (!confirmUnsavedChanges()) {
    return;
  }
  const name = prompt('Project name', useReference ? 'Vinculacao de Orientacao' : 'Cancelamento de Orientacao');
  if (!name) {
    return;
  }
  const key = slugify(name);
  const version = '1.0';
  const description = '';
  const data = {
    projectId: createProjectId(key),
    name,
    key,
    version,
    description,
    bpmnXml: useReference ? referenceXml : emptyBpmnXml(key, name),
    requirements: [],
    derivedConfig: { variables: [], topics: [], messages: [], requiredConfigurations: [] },
    bindings: {},
    variableMappings: {},
    capabilityImplementations: {},
    inboundConfigs: {},
    outboundConfigs: {},
    flowConditions: {},
    processConfig: { historyTimeToLive: '180' },
    integration: {},
    generatedComponents: [],
    deploymentHistory: [],
    wizardSession: null,
    status: 'DRAFT',
    lastModified: new Date().toISOString()
  };
  hydrateProject(data);
  loadXml();
  markDirty();
  renderProject();
  renderProjectList();
  updateProjectShell();
  showTab('bpmn');
}

function saveProjectAs() {
  if (!state.activeProjectOpen) {
    return;
  }
  const current = project();
  const name = prompt('Save As - Name', `${current.name} Copy`);
  if (!name) {
    return;
  }
  const key = prompt('Save As - Key', slugify(name));
  if (!key) {
    return;
  }
  state.projectId = createProjectId(key);
  document.getElementById('projectName').value = name;
  document.getElementById('projectKey').value = key;
  saveProject();
}

function deleteProject(projectId = state.projectId) {
  if (!projectId) {
    return;
  }
  const projects = savedProjects();
  const data = projects[projectId];
  if (!data || !confirm(`Delete project "${data.name}" from ADE? Camunda definitions and PPG Management data will not be deleted.`)) {
    return;
  }
  delete projects[projectId];
  writeProjects(projects);
  if (state.projectId === projectId) {
    closeProject(false);
  } else {
    renderProjectList();
  }
}

function confirmUnsavedChanges() {
  if (!state.activeProjectOpen || !state.dirty) {
    return true;
  }
  if (confirm('The open project has unsaved changes. Save it before continuing?')) {
    saveProject();
  }
  state.dirty = false;
  updateProjectShell();
  return true;
}

function markDirty(updateShell = true) {
  if (!state.activeProjectOpen) {
    return;
  }
  if (state.status === 'READY_FOR_DEPLOYMENT') {
    state.status = 'CONFIGURATION_REVIEW_REQUIRED';
    document.getElementById('projectStatus').textContent = state.status;
  }
  state.dirty = true;
  if (updateShell) {
    updateProjectShell();
  }
}

function updateProjectShell() {
  const hasProject = state.activeProjectOpen;
  document.body.classList.toggle('no-project', !hasProject);
  if (hasProject) {
    document.body.classList.remove('project-browser');
  }
  document.querySelectorAll('[data-requires-project]').forEach((element) => {
    element.disabled = !hasProject;
  });
  document.getElementById('activeProjectName').textContent = hasProject ? document.getElementById('projectName').value : 'No project open';
  document.getElementById('activeProjectVersion').textContent = hasProject ? document.getElementById('projectVersion').value : '-';
  document.getElementById('saveState').textContent = hasProject ? (state.dirty ? 'Unsaved changes' : 'Saved') : '-';
}

function showTab(tabId) {
  if (!state.activeProjectOpen && tabId !== 'projects') {
    tabId = 'projects';
  }
  if (tabId === 'projects') {
    document.body.classList.add('project-browser');
  }
  document.querySelectorAll('section').forEach((section) => section.classList.remove('active'));
  const section = document.getElementById(tabId);
  if (section) {
    section.classList.add('active');
  }
}

function renderProjectList() {
  const projects = Object.values(savedProjects()).sort((left, right) => (right.lastModified || '').localeCompare(left.lastModified || ''));
  const container = document.getElementById('projectList');
  if (!container) {
    return;
  }
  if (!projects.length) {
    container.innerHTML = '<p><small>No saved projects.</small></p>';
    return;
  }
  container.innerHTML = `
    <div class="project-list">
      <div class="project-row header">
        <span>Name</span>
        <span>Version</span>
        <span>Last Modified</span>
        <span>Deployment Status</span>
        <span>Action</span>
      </div>
      ${projects.map((item) => `
        <div class="project-row">
          <strong>${escapeHtml(item.name || '')}</strong>
          <span>${escapeHtml(item.version || '')}</span>
          <span>${escapeHtml(item.lastModified || '')}</span>
          <span>${escapeHtml(item.status || 'DRAFT')}</span>
          <button type="button" data-open-project="${escapeAttribute(item.projectId)}">Open</button>
        </div>`).join('')}
    </div>`;
  container.querySelectorAll('button[data-open-project]').forEach((button) => {
    button.addEventListener('click', (event) => {
      event.stopPropagation();
      openProject(button.dataset.openProject);
    });
  });
}

function renderCapabilities() {
  const container = document.getElementById('capabilityList');
  if (!container) {
    return;
  }
  container.innerHTML = (state.capabilities || []).map((capability) => `
    <div class="item">
      <strong>${escapeHtml(capability.id)} - ${escapeHtml(capability.name)}</strong>
      <p>${escapeHtml(capability.description || '')}</p>
      ${renderCapabilityContract(capability)}
      ${renderCapabilityImplementation(capability)}
    </div>`).join('');
}

function duplicateProject(projectId) {
  const source = savedProjects()[projectId];
  if (!source) {
    return;
  }
  hydrateProject({
    ...source,
    projectId: createProjectId(source.key || 'automation'),
    name: `${source.name} Copy`,
    key: `${source.key || 'automation'}-copy`,
    status: 'DRAFT',
    deploymentHistory: []
  });
  saveProject();
  showTab('bpmn');
}

function createProjectId(key) {
  return `${slugify(key || 'automation')}-${Date.now()}`;
}

function slugify(value) {
  return (value || 'automation')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '') || 'automation';
}

function normalizeXmlId(value) {
  return slugify(value).replace(/-/g, '_');
}

function escapeXml(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function deriveVariableMappingsSnapshot() {
  const inboundMappings = Object.fromEntries(Object.entries(state.inboundConfigs || {}).map(([key, config]) => [key, {
    inbound: config.variableMappings || ''
  }]));
  return {
    ...inboundMappings,
    ...(state.variableMappings || {})
  };
}

function deriveCapabilityImplementations(capabilityBindings) {
  return Object.fromEntries((capabilityBindings || [])
    .filter((binding) => binding.bpmnElementId && binding.implementation)
    .map((binding) => [binding.bpmnElementId, binding.implementation]));
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
        capabilityId: capabilityBindingForNode(node),
        inboundConfig: state.inboundConfigs[node.getAttribute('id')] || defaultInboundConfig(node),
        outboundConfig: state.outboundConfigs[node.getAttribute('id')] || defaultOutboundConfig(node)
      });
    });
  });
  elements
    .filter((item) => item.capabilityId && !state.bindings[item.elementId])
    .forEach((item) => {
      state.bindings[item.elementId] = item.capabilityId;
      applyDeterministicMappingDefaults(item.elementId, findCapability(item.capabilityId));
    });
  elements
    .filter((item) => item.capabilityId)
    .forEach((item) => applyDeterministicMappingDefaults(item.elementId, findCapability(item.capabilityId)));
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
  alignLegacyGatewayConditionsWithProducedData();
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

function alignLegacyGatewayConditionsWithProducedData() {
  const gateways = new Set((state.gatewayBranches || []).map((branch) => branch.gatewayId));
  gateways.forEach((gatewayId) => {
    const context = buildProcessDataContext(gatewayId);
    const preferred = preferredGatewayVariable(context);
    if (!preferred || preferred.type !== 'Boolean') {
      return;
    }
    (state.gatewayBranches || [])
      .filter((branch) => branch.gatewayId === gatewayId)
      .forEach((branch) => {
        const config = state.flowConditions[branch.flowId] || {};
        const visual = config.visual || inferVisualCondition(config.condition, context);
        if (!visual || visual.variable !== 'complete') {
          return;
        }
        const migratedVisual = {
          ...visual,
          variable: preferred.name
        };
        state.flowConditions[branch.flowId] = {
          ...config,
          visual: migratedVisual,
          condition: buildConditionExpression(migratedVisual, preferred.type)
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

function findCapability(capabilityId) {
  return (state.capabilities || []).find((capability) => capability.id === resolveCapabilityId(capabilityId));
}

function migrateCapabilityBindings(bindings) {
  return Object.fromEntries(Object.entries(bindings || {}).map(([elementId, capabilityId]) => [elementId, resolveCapabilityId(capabilityId)]));
}

function resolveCapabilityId(capabilityId) {
  return LEGACY_CAPABILITY_ALIASES[capabilityId] || capabilityId || '';
}

function capabilityBindingForNode(node) {
  const elementId = node.getAttribute('id');
  const current = resolveCapabilityId(state.bindings[elementId] || '');
  if (current) {
    return current;
  }
  const topic = node.getAttribute('camunda:topic') || node.getAttribute('topic') || '';
  const resolvedTopic = resolveCapabilityId(topic);
  return findCapability(resolvedTopic) ? resolvedTopic : '';
}

function legacyCapabilityNotice(step) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const node = doc.querySelector(`[id="${step.elementId}"]`);
  const topic = node?.getAttribute('camunda:topic') || node?.getAttribute('topic') || '';
  const resolvedTopic = resolveCapabilityId(topic);
  if (!topic || topic === resolvedTopic) {
    return '';
  }
  return `<div class="wizard-message warning">Esta tarefa usava o topico legado ${escapeHtml(topic)}. No catalogo atual do PPG Management, a verificacao equivalente e ${escapeHtml(resolvedTopic)}.</div>`;
}

function renderCapabilityContract(capability) {
  if (!capability) {
    return '';
  }
  return `
    <table>
      <tbody>
        <tr><th>Name</th><td>${escapeHtml(capability.name || '')}</td></tr>
        <tr><th>Description</th><td>${escapeHtml(capability.description || '')}</td></tr>
        <tr><th>Provider</th><td>${escapeHtml(capability.provider || '')}</td></tr>
        <tr><th>Interface</th><td>${escapeHtml(capability.interfaceType || '')}</td></tr>
        <tr><th>Endpoint/Topic</th><td>${escapeHtml(capability.endpoint || '')}</td></tr>
      </tbody>
    </table>
    <strong>Input Parameters</strong>
    ${renderParameterTable(capability.inputParameters || [])}
    <strong>Output Parameters</strong>
    ${renderParameterTable(capability.outputParameters || [])}`;
}

function renderCapabilityImplementation(capability) {
  if (!capability) {
    return '';
  }
  return `
    <strong>Implementation</strong>
    <table>
      <tbody>
        <tr><th>Capability</th><td>${escapeHtml(capability.id || '')}</td></tr>
        <tr><th>Provider</th><td>${escapeHtml(capability.provider || '')}</td></tr>
        <tr><th>Implementation Type</th><td>${escapeHtml(capability.implementationType || '')}</td></tr>
        <tr><th>Implementation</th><td>${escapeHtml(capability.implementation || '')}</td></tr>
        <tr><th>Deployment</th><td>${escapeHtml(capability.deployment || '')}</td></tr>
        <tr><th>Status</th><td>${escapeHtml(capability.status || '')}</td></tr>
      </tbody>
    </table>`;
}

function renderParameterTable(parameters) {
  if (!parameters.length) {
    return '<p><small>No parameters.</small></p>';
  }
  return renderTable(['name', 'type', 'contentType'], parameters.map((parameter) => ({
    name: parameter.name || '',
    type: parameter.type || '',
    contentType: parameter.contentType || ''
  })));
}

function renderCapabilityMappingFields(elementId, capability) {
  if (!capability) {
    return '';
  }
  const mappings = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
  const inputFields = (capability.inputParameters || []).map((parameter) => `
    <label>${escapeHtml(parameter.name)} : ${escapeHtml(parameter.type || '')}</label>
    <input data-capability-mapping="${escapeAttribute(elementId)}" data-direction="inputs" data-param="${escapeAttribute(parameter.name)}" value="${escapeAttribute((mappings.inputs || {})[parameter.name] || '')}" placeholder="\${processVariable}">`).join('');
  const outputFields = (capability.outputParameters || []).map((parameter) => `
    <label>${escapeHtml(parameter.name)} -> process variable</label>
    <input data-capability-mapping="${escapeAttribute(elementId)}" data-direction="outputs" data-param="${escapeAttribute(parameter.name)}" value="${escapeAttribute((mappings.outputs || {})[parameter.name] || '')}" placeholder="\${resultVariable}">`).join('');
  return `
    <strong>Variable Mappings</strong>
    <div class="branch-row">
      <strong>Inputs</strong>
      ${inputFields || '<p><small>No inputs.</small></p>'}
      <strong>Outputs</strong>
      ${outputFields || '<p><small>No outputs.</small></p>'}
    </div>`;
}

function renderRequirementImplementation(elementId, capability) {
  if (!capability) {
    return '';
  }
  const implementation = state.capabilityImplementations[elementId] || capability;
  const generated = implementation.generatedCode ? '<button type="button" data-view-code="' + escapeAttribute(elementId) + '">View Code</button>' : '';
  return `
    ${renderCapabilityImplementation(implementation)}
    <div class="row">
      <button type="button" data-generate-worker="${escapeAttribute(elementId)}">Generate/Customize Worker</button>
      ${generated}
    </div>`;
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

    const selectedCapabilityId = state.bindings[item.elementId] || item.capabilityId || '';
    const selectedCapability = findCapability(selectedCapabilityId);
    const options = ['<option value="">Select capability</option>'].concat(state.capabilities.map((capability) => {
      const selected = selectedCapabilityId === capability.id ? 'selected' : '';
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
      ${selectedCapability ? renderCapabilityContract(selectedCapability) : ''}
      ${renderCapabilityMappingFields(item.elementId, selectedCapability)}
      ${renderRequirementImplementation(item.elementId, selectedCapability)}
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
      renderRequirements();
    });
  });
  container.querySelectorAll('input[data-capability-mapping]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.capabilityMapping;
      state.variableMappings[elementId] = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
      state.variableMappings[elementId][input.dataset.direction] = state.variableMappings[elementId][input.dataset.direction] || {};
      state.variableMappings[elementId][input.dataset.direction][input.dataset.param] = input.value.trim();
      saveProject();
    });
  });
  container.querySelectorAll('button[data-generate-worker]').forEach((button) => {
    button.addEventListener('click', () => {
      generateWorker(button.dataset.generateWorker);
    });
  });
  container.querySelectorAll('button[data-view-code]').forEach((button) => {
    button.addEventListener('click', () => {
      viewGeneratedCode(button.dataset.viewCode);
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
    variableMappings: defaultVariableMappings(downstreamRequiredProcessVariables(elementId))
  };
}

function defaultVariableMappings(additionalVariables = []) {
  const mappings = {
    from: '${email.from}',
    subject: '${email.subject}',
    body: '${email.body}',
    correlationId: '${correlationId}'
  };
  additionalVariables.forEach((name) => {
    if (name && !mappings[name]) {
      mappings[name] = '${payload.' + name + '}';
    }
  });
  return JSON.stringify(mappings, null, 2);
}

function downstreamRequiredProcessVariables(elementId) {
  if (!xmlBox.value) {
    return [];
  }
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const elements = Array.from(doc.getElementsByTagName('*'));
  const byId = new Map(elements
    .filter((node) => node.getAttribute && node.getAttribute('id'))
    .map((node) => [node.getAttribute('id'), node]));
  const queue = [elementId];
  const visited = new Set();
  const required = new Set();
  while (queue.length && required.size === 0) {
    const current = queue.shift();
    if (!current || visited.has(current)) {
      continue;
    }
    visited.add(current);
    elements
      .filter((node) => node.localName === 'sequenceFlow' && node.getAttribute('sourceRef') === current)
      .forEach((flow) => {
        const targetId = flow.getAttribute('targetRef');
        const target = byId.get(targetId);
        if (!target) {
          return;
        }
        const capability = findCapability(capabilityBindingForNode(target));
        if (capability) {
          (capability.inputParameters || []).forEach((parameter) => required.add(parameter.name));
          return;
        }
        queue.push(targetId);
      });
  }
  return Array.from(required);
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

function generateWorker(elementId) {
  const capabilityId = state.bindings[elementId];
  const capability = findCapability(capabilityId);
  if (!capability) {
    return;
  }
  const component = generatedWorkerComponent(elementId, capability);
  state.capabilityImplementations[elementId] = {
    ...capability,
    implementationType: capability.interfaceType && capability.interfaceType.startsWith('REST')
      ? 'GENERATED_WORKER'
      : capability.implementationType,
    implementation: component.name,
    deployment: component.files['Dockerfile'] ? 'Dockerfile generated in project metadata' : capability.deployment,
    generatedCode: component.files
  };
  state.generatedComponents = (state.generatedComponents || []).filter((item) => item.bpmnElementId !== elementId);
  state.generatedComponents.push(component);
  saveProject();
  renderRequirements();
  viewGeneratedCode(elementId);
}

function generatedWorkerComponent(elementId, capability) {
  const className = `${toPascalCase(capability.id)}Worker`;
  const clientName = `${toPascalCase(capability.id)}Client`;
  const packageName = `br.ifes.propos.generated.${slugify(document.getElementById('projectKey').value).replace(/-/g, '')}`;
  const mappings = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
  return {
    bpmnElementId: elementId,
    capabilityId: capability.id,
    name: `${slugify(capability.id)}-worker`,
    implementationType: 'GENERATED_WORKER',
    files: {
      'pom.xml': generatedWorkerPom(),
      'src/main/java/WorkerApplication.java': generatedMainClass(packageName),
      [`src/main/java/${className}.java`]: generatedWorkerClass(packageName, className, clientName, capability, mappings),
      [`src/main/java/${clientName}.java`]: generatedRestClientClass(packageName, clientName, capability),
      'src/main/java/dto/CapabilityRequest.java': generatedDto(packageName, 'CapabilityRequest', capability.inputParameters || []),
      'src/main/java/dto/CapabilityResponse.java': generatedDto(packageName, 'CapabilityResponse', capability.outputParameters || []),
      'src/main/resources/application.properties': generatedWorkerProperties(capability),
      'Dockerfile': generatedDockerfile(),
      'README.md': generatedWorkerReadme(capability),
      'service-definition.yaml': generatedServiceDefinition(capability)
    }
  };
}

function generatedWorkerPom() {
  return `<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>br.ifes.propos.generated</groupId>
  <artifactId>generated-worker</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <properties>
    <java.version>17</java.version>
  </properties>
</project>
`;
}

function viewGeneratedCode(elementId) {
  const implementation = state.capabilityImplementations[elementId];
  const code = implementation && implementation.generatedCode;
  const container = document.getElementById('generatedCode');
  if (!container || !code) {
    return;
  }
  container.textContent = Object.entries(code)
    .map(([fileName, content]) => `===== ${fileName} =====\n${content}`)
    .join('\n\n');
}

function toPascalCase(value) {
  return String(value || 'Generated')
    .toLowerCase()
    .split(/[^a-z0-9]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join('');
}

function generatedMainClass(packageName) {
  return `package ${packageName};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
`;
}

function generatedWorkerClass(packageName, className, clientName, capability, mappings) {
  return `package ${packageName};

import java.util.HashMap;
import java.util.Map;

public class ${className} {
    private final ${clientName} client;

    public ${className}(${clientName} client) {
        this.client = client;
    }

    public Map<String, Object> execute(Map<String, Object> camundaVariables) {
        Map<String, Object> request = new HashMap<>();
${Object.entries(mappings.inputs || {}).map(([name, expression]) => `        request.put("${name}", camundaVariables.get("${stripExpression(expression)}"));`).join('\n') || '        // Map Camunda variables into request parameters here.'}

        Map<String, Object> response = client.invoke(request);
        Map<String, Object> outputs = new HashMap<>();
${Object.entries(mappings.outputs || {}).map(([name, expression]) => `        outputs.put("${stripExpression(expression)}", response.get("${name}"));`).join('\n') || '        // Map response fields back to Camunda variables here.'}
        return outputs;
    }
}
`;
}

function generatedRestClientClass(packageName, clientName, capability) {
  return `package ${packageName};

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ${clientName} {
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> invoke(Map<String, Object> request) {
        // ${capability.endpoint}
        // Build REST request, including multipart/form-data when required, then parse the response.
        return Map.of();
    }
}
`;
}

function generatedDto(packageName, className, parameters) {
  return `package ${packageName}.dto;

public class ${className} {
${parameters.map((parameter) => `    public Object ${parameter.name};`).join('\n') || '    public Object value;'}
}
`;
}

function generatedWorkerProperties(capability) {
  return `camunda.bpm.client.base-url=http://camunda:8080/engine-rest
worker.topic=${capability.id}
ppg.management.base-url=http://ppg-management-service:8081
`;
}

function generatedDockerfile() {
  return `FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/generated-worker.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
`;
}

function generatedWorkerReadme(capability) {
  return `# ${capability.name} Worker

Generated by ADE for capability ${capability.id}.

The worker receives Camunda variables, calls ${capability.provider} through ${capability.endpoint}, maps outputs, and completes the External Task.
`;
}

function generatedServiceDefinition(capability) {
  return `capability: ${capability.id}
provider: ${capability.provider}
interface: ${capability.interfaceType}
endpoint: ${capability.endpoint}
implementationType: GENERATED_WORKER
`;
}

function stripExpression(expression) {
  return String(expression || '')
    .replace(/^\$\{/, '')
    .replace(/\}$/, '')
    .trim();
}

async function startWizard(mode = 'run') {
  if (!state.activeProjectOpen) {
    return;
  }
  await analyzeBpmnForWizard();
  const steps = buildWizardSteps();
  if (!steps.length) {
    document.getElementById('deploymentResult').textContent = 'No configurable BPMN elements were detected.';
    return;
  }
  const currentStep = mode === 'resume' && state.wizardSession
    ? Math.min(state.wizardSession.currentStep || 0, steps.length - 1)
    : 0;
  state.wizardSession = {
    projectId: state.projectId,
    currentStep,
    status: 'RUNNING',
    startedAt: state.wizardSession?.startedAt || new Date().toISOString(),
    lastUpdatedAt: new Date().toISOString(),
    lastRunAt: new Date().toISOString(),
    steps,
    stepStatuses: {}
  };
  updateWizardStepStatuses();
  saveProject();
  openWizardModal();
}

async function analyzeBpmnForWizard() {
  await analyzeBpmn();
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  state.gatewayBranches = deriveGatewayBranches(doc);
}

function buildWizardSteps() {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const nodes = Array.from(doc.getElementsByTagName('*'))
    .filter((node) => isWizardRelevantBpmnElement(node))
    .map((node, index) => {
      const requirement = state.requirements.find((item) => item.elementId === node.getAttribute('id'));
      return {
        id: `step-${node.getAttribute('id')}`,
        elementId: node.getAttribute('id'),
        name: node.getAttribute('name') || node.getAttribute('id'),
        bpmnType: node.localName,
        kind: wizardStepKind(node),
        requirementTypes: requirement ? requirement.type.split(',') : wizardRequirementTypes(node),
        order: index
      };
    });
  nodes.push({
    id: 'global-validation',
    elementId: '',
    name: 'Global Validation',
    bpmnType: 'project',
    kind: 'GLOBAL_VALIDATION',
    requirementTypes: ['GLOBAL_VALIDATION'],
    order: nodes.length
  });
  return nodes;
}

function isWizardRelevantBpmnElement(node) {
  return ['startEvent', 'serviceTask', 'sendTask', 'receiveTask', 'intermediateCatchEvent', 'intermediateThrowEvent', 'exclusiveGateway', 'endEvent']
    .includes(node.localName)
    && node.getAttribute('id')
    && (node.localName !== 'endEvent' || isOutboundNode(node));
}

function wizardStepKind(node) {
  if (node.localName === 'serviceTask') return 'SERVICE_TASK';
  if (node.localName === 'sendTask' || node.localName === 'intermediateThrowEvent') return 'OUTBOUND_COMMUNICATION';
  if (node.localName === 'receiveTask' || node.localName === 'intermediateCatchEvent') return 'INBOUND_EVENT';
  if (node.localName === 'startEvent') return hasMessageDefinition(node) ? 'START_MESSAGE_EVENT' : 'VALIDATION';
  if (node.localName === 'exclusiveGateway') return 'GATEWAY';
  return 'VALIDATION';
}

function wizardRequirementTypes(node) {
  if (node.localName === 'serviceTask') return ['CAPABILITY_BINDING', 'VARIABLE_MAPPING'];
  if (node.localName === 'sendTask' || node.localName === 'intermediateThrowEvent') return ['OUTBOUND_COMMUNICATION', 'CAPABILITY_BINDING'];
  if (node.localName === 'receiveTask' || node.localName === 'intermediateCatchEvent') return ['INBOUND_EVENT', 'MESSAGE_DEFINITION', 'CORRELATION_DEFINITION'];
  if (node.localName === 'exclusiveGateway') return ['CONDITION_VALIDATION'];
  if (node.localName === 'startEvent' && hasMessageDefinition(node)) return ['INBOUND_EVENT', 'MESSAGE_DEFINITION', 'CORRELATION_DEFINITION'];
  return ['VALIDATION'];
}

function hasMessageDefinition(node) {
  return Boolean(node.querySelector('messageEventDefinition, bpmn\\:messageEventDefinition'));
}

function openWizardModal() {
  document.getElementById('wizardOverlay').classList.add('open');
  renderWizard();
  document.getElementById('wizardNext').focus();
}

function closeWizardModal() {
  document.getElementById('wizardOverlay').classList.remove('open');
}

function cancelWizard() {
  if (state.wizardSession) {
    state.wizardSession.status = 'CANCELLED';
    state.wizardSession.lastUpdatedAt = new Date().toISOString();
    saveProject();
  }
  closeWizardModal();
}

function renderWizard() {
  if (!state.wizardSession) {
    return;
  }
  updateWizardStepStatuses();
  const steps = state.wizardSession.steps;
  const index = state.wizardSession.currentStep || 0;
  const step = steps[index];
  document.getElementById('wizardProjectName').textContent = document.getElementById('projectName').value;
  document.getElementById('wizardProgress').innerHTML = renderWizardProgress(steps, index);
  document.getElementById('wizardContent').innerHTML = renderWizardStep(step);
  document.getElementById('wizardFooterStatus').textContent = `Etapa ${index + 1} de ${steps.length}`;
  document.getElementById('wizardBack').disabled = index === 0;
  document.getElementById('wizardNext').style.display = step.kind === 'GLOBAL_VALIDATION' ? 'none' : '';
  document.getElementById('wizardFinish').style.display = step.kind === 'GLOBAL_VALIDATION' ? '' : 'none';
  document.getElementById('wizardNext').disabled = hasBlockingStepErrors(step);
  document.getElementById('wizardFinish').disabled = !globalWizardValidation().ready;
  bindWizardContentEvents();
}

function renderWizardProgress(steps, currentIndex) {
  return `<div class="wizard-step-nav">${steps.map((step, index) => {
    const status = wizardStepStatus(step);
    return `<button type="button" class="wizard-step-button ${index === currentIndex ? 'current' : ''} ${status.toLowerCase()}" data-wizard-step-index="${index}">
      <span class="wizard-step-symbol">${wizardStepSymbol(status, index === currentIndex)}</span>
      <span><strong>${escapeHtml(step.name)}</strong><br><small>${escapeHtml(status)} · ${escapeHtml(step.bpmnType)}</small></span>
    </button>`;
  }).join('')}</div>`;
}

function wizardStepSymbol(status, current) {
  if (current) return '>';
  if (status === 'CONFIGURED') return '✓';
  if (status === 'ERROR') return '!';
  if (status === 'WARNING') return '~';
  return '○';
}

function wizardStepStatus(step) {
  const issues = validateWizardStep(step);
  if (issues.some((issue) => issue.level === 'ERROR')) return 'ERROR';
  if (issues.some((issue) => issue.level === 'WARNING')) return 'WARNING';
  if (step.kind === 'GLOBAL_VALIDATION') return globalWizardValidation().ready ? 'CONFIGURED' : 'ERROR';
  return 'CONFIGURED';
}

function updateWizardStepStatuses() {
  if (!state.wizardSession) {
    return;
  }
  state.wizardSession.stepStatuses = Object.fromEntries(state.wizardSession.steps.map((step) => [step.id, wizardStepStatus(step)]));
  state.wizardSession.lastUpdatedAt = new Date().toISOString();
}

function renderWizardStep(step) {
  if (step.kind === 'GLOBAL_VALIDATION') {
    return renderGlobalValidationStep();
  }
  const issues = validateWizardStep(step);
  return `
    <div class="wizard-card">
      <h3>${escapeHtml(step.name)}</h3>
      <p class="wizard-step-meta">${escapeHtml(step.bpmnType)} · ${escapeHtml(step.elementId)}</p>
      <p class="wizard-diagnostic">Requirements: ${step.requirementTypes.map(escapeHtml).join(', ')}</p>
      ${renderWizardIssues(issues)}
    </div>
    ${renderElementSummary(step)}
    ${renderWizardConfigurator(step)}`;
}

function renderElementSummary(step) {
  return `
    <div class="wizard-card wizard-grid">
      <div>
        <strong>BPMN</strong>
        <div class="wizard-message info">INFO: ID defined as ${escapeHtml(step.elementId)}</div>
        <div class="wizard-message info">INFO: Name defined as ${escapeHtml(step.name)}</div>
      </div>
      <div>
        <strong>Configuration Status</strong>
        <div class="wizard-message ${wizardStepStatus(step).toLowerCase()}">${escapeHtml(wizardStepStatus(step))}</div>
      </div>
    </div>`;
}

function renderWizardConfigurator(step) {
  if (step.kind === 'SERVICE_TASK') return renderServiceTaskWizard(step);
  if (step.kind === 'OUTBOUND_COMMUNICATION') return renderOutboundWizard(step);
  if (step.kind === 'INBOUND_EVENT' || step.kind === 'START_MESSAGE_EVENT') return renderInboundWizard(step);
  if (step.kind === 'GATEWAY') return renderGatewayWizard(step);
  return '<div class="wizard-card"><p>No manual configuration is required for this element.</p></div>';
}

function renderServiceTaskWizard(step) {
  const capabilityId = state.bindings[step.elementId] || '';
  const capability = findCapability(capabilityId);
  const suggestion = suggestCapability(step);
  return `
    <div class="wizard-card">
      <strong>Suggested Capability</strong>
      <p>${suggestion ? escapeHtml(suggestion.id) : 'No deterministic suggestion available.'}</p>
    </div>
    ${renderWizardCapabilityPicker(step, capabilityId)}
    ${capability ? `<div class="wizard-card">${renderCapabilityContract(capability)}${renderCapabilityMappingEditor(step.elementId, capability)}${renderRequirementImplementation(step.elementId, capability)}</div>` : ''}`;
}

function renderOutboundWizard(step) {
  if (!state.bindings[step.elementId]) {
    state.bindings[step.elementId] = 'SEND_EMAIL';
  }
  const config = state.outboundConfigs[step.elementId] || {};
  const capability = findCapability(state.bindings[step.elementId]);
  return `
    ${renderWizardCapabilityPicker(step, state.bindings[step.elementId])}
    <div class="wizard-card">
      <strong>Outbound Communication</strong>
      <label>To</label>
      <input data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="emailTo" value="${escapeAttribute(config.emailTo || '')}" placeholder="\${student.email}">
      <label>Subject</label>
      <input data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="emailSubject" value="${escapeAttribute(config.emailSubject || '')}">
      <label>Body / Template</label>
      <textarea data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="emailBody">${escapeHtml(config.emailBody || '')}</textarea>
      <label>Expected Response</label>
      <input data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="expectedResponse" value="${escapeAttribute(config.expectedResponse || '')}">
    </div>
    ${capability ? `<div class="wizard-card">${renderCapabilityContract(capability)}${renderCapabilityMappingEditor(step.elementId, capability)}</div>` : ''}`;
}

function renderInboundWizard(step) {
  const config = state.inboundConfigs[step.elementId] || defaultWizardInboundConfig(step);
  state.inboundConfigs[step.elementId] = config;
  return `
    <div class="wizard-card wizard-grid">
      <div>
        <strong>Inbound Message</strong>
        <label>Inbound Channel</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="channel" value="${escapeAttribute(config.channel || 'EMAIL')}">
        <label>Provider</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="provider" value="${escapeAttribute(config.provider || 'GMS')}">
        <label>Router</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="router" value="${escapeAttribute(config.router || 'CIR')}">
      </div>
      <div>
        <strong>Correlation</strong>
        <label>External Event</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="externalEvent" value="${escapeAttribute(config.externalEvent || '')}">
        <label>Message Name</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="camundaMessage" value="${escapeAttribute(config.camundaMessage || '')}">
        <label>Correlation Key</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="correlationField" value="${escapeAttribute(config.correlationField || 'requestId')}">
        <label>Target Process Variable</label>
        <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="correlationExpression" value="${escapeAttribute(config.correlationExpression || '${requestId}')}">
      </div>
    </div>
    <div class="wizard-card">
      <label>Initial Variable Mappings JSON</label>
      <textarea data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="variableMappings">${escapeHtml(config.variableMappings || defaultVariableMappings())}</textarea>
    </div>`;
}

function renderGatewayWizard(step) {
  const branches = (state.gatewayBranches || []).filter((branch) => branch.gatewayId === step.elementId);
  return `
    <div class="wizard-card">
      <strong>Exclusive Gateway Conditions</strong>
      ${branches.map((branch) => {
        const config = state.flowConditions[branch.flowId] || {};
        const isDefault = Boolean(config.isDefault);
        return `
          <div class="wizard-mapping-row">
            <strong>${escapeHtml(branch.flowName || branch.flowId)}</strong>
            <span>${isDefault ? 'default' : 'if'}</span>
            <input data-wizard-flow-condition="${escapeAttribute(branch.flowId)}" value="${escapeAttribute(config.condition || '')}" ${isDefault ? 'disabled' : ''}>
          </div>
          <label><input type="checkbox" data-wizard-flow-default="${escapeAttribute(branch.flowId)}" data-gateway="${escapeAttribute(step.elementId)}" ${isDefault ? 'checked' : ''}> Default branch</label>`;
      }).join('')}
    </div>`;
}

function renderWizardCapabilityPicker(step, selectedCapabilityId) {
  const filter = normalizeSearchText(step.name);
  const ranked = [...state.capabilities].sort((left, right) => capabilityScore(right, filter) - capabilityScore(left, filter));
  return `
    <div class="wizard-card">
      <strong>Capability</strong>
      <label>Search capability</label>
      <input data-wizard-capability-filter="${escapeAttribute(step.elementId)}" value="">
      <div class="wizard-capability-list">
        ${ranked.map((capability) => `
          <button type="button" class="wizard-capability-option ${capability.id === selectedCapabilityId ? 'selected' : ''}" data-wizard-capability="${escapeAttribute(step.elementId)}" data-capability-id="${escapeAttribute(capability.id)}">
            <strong>${escapeHtml(capability.id)}</strong>
            <span>${escapeHtml(capability.provider)} · ${escapeHtml(capability.interfaceType)} · ${escapeHtml(capability.status)}</span>
            <small>${escapeHtml(capability.description || '')}</small>
          </button>`).join('')}
      </div>
    </div>`;
}

function renderCapabilityMappingEditor(elementId, capability) {
  return `
    <strong>Variable Mapping</strong>
    <div>
      ${(capability.inputParameters || []).map((parameter) => renderWizardMappingRow(elementId, 'inputs', parameter, '<-')).join('')}
      ${(capability.outputParameters || []).map((parameter) => renderWizardMappingRow(elementId, 'outputs', parameter, '->')).join('')}
    </div>`;
}

function renderWizardMappingRow(elementId, direction, parameter, arrow) {
  const mappings = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
  const value = (mappings[direction] || {})[parameter.name] || '';
  return `
    <div class="wizard-mapping-row">
      <span>${escapeHtml(parameter.name)} <small>${escapeHtml(parameter.type || '')}</small></span>
      <span>${arrow}</span>
      <input data-wizard-mapping="${escapeAttribute(elementId)}" data-direction="${direction}" data-param="${escapeAttribute(parameter.name)}" value="${escapeAttribute(value)}" placeholder="${direction === 'inputs' ? '${processVariable}' : 'processVariable'}">
    </div>`;
}

function renderWizardIssues(issues) {
  if (!issues.length) {
    return '<div class="wizard-message info">INFO: No blocking issues detected.</div>';
  }
  return issues.map((issue) => `<div class="wizard-message ${issue.level.toLowerCase()}">${escapeHtml(issue.level)}: ${escapeHtml(issue.message)}</div>`).join('');
}

function validateWizardStep(step) {
  if (!step) return [{ level: 'ERROR', message: 'Wizard step is missing.' }];
  if (step.kind === 'GLOBAL_VALIDATION') return globalWizardValidation().issues;
  const issues = [];
  if (!step.elementId) issues.push({ level: 'ERROR', message: 'BPMN id is required.' });
  if (!step.name) issues.push({ level: 'WARNING', message: 'BPMN name is not defined.' });
  if (step.kind === 'SERVICE_TASK') validateCapabilityAndMappings(step, issues);
  if (step.kind === 'OUTBOUND_COMMUNICATION') validateOutboundWizardStep(step, issues);
  if (step.kind === 'INBOUND_EVENT' || step.kind === 'START_MESSAGE_EVENT') validateInboundWizardStep(step, issues);
  if (step.kind === 'GATEWAY') validateGatewayWizardStep(step, issues);
  return issues;
}

function validateCapabilityAndMappings(step, issues) {
  const capability = findCapability(state.bindings[step.elementId]);
  if (!capability) {
    issues.push({ level: 'ERROR', message: 'Capability not configured.' });
    return;
  }
  const mappings = state.variableMappings[step.elementId] || { inputs: {}, outputs: {} };
  (capability.inputParameters || []).forEach((parameter) => {
    if (!((mappings.inputs || {})[parameter.name] || '').trim()) {
      issues.push({ level: 'ERROR', message: `Required input "${parameter.name}" is not mapped.` });
    }
  });
  (capability.outputParameters || []).forEach((parameter) => {
    if (!((mappings.outputs || {})[parameter.name] || '').trim()) {
      issues.push({ level: 'WARNING', message: `No output variable was defined for "${parameter.name}".` });
    }
  });
}

function validateOutboundWizardStep(step, issues) {
  if (!state.bindings[step.elementId]) {
    issues.push({ level: 'ERROR', message: 'Outbound capability not configured.' });
  }
  const config = state.outboundConfigs[step.elementId] || {};
  if (!config.emailTo) issues.push({ level: 'ERROR', message: 'Recipient is required.' });
  if (!config.emailSubject) issues.push({ level: 'ERROR', message: 'Subject is required.' });
  if (!config.emailBody) issues.push({ level: 'WARNING', message: 'Body/template is empty.' });
}

function validateInboundWizardStep(step, issues) {
  const config = state.inboundConfigs[step.elementId] || {};
  ['channel', 'externalEvent', 'camundaMessage', 'correlationField', 'correlationExpression'].forEach((field) => {
    if (!config[field]) {
      issues.push({ level: 'ERROR', message: `${field} is required.` });
    }
  });
  if (config.variableMappings) {
    try {
      JSON.parse(config.variableMappings);
    } catch (error) {
      issues.push({ level: 'ERROR', message: 'Initial variable mappings must be valid JSON.' });
    }
  }
}

function validateGatewayWizardStep(step, issues) {
  const branches = (state.gatewayBranches || []).filter((branch) => branch.gatewayId === step.elementId);
  if (branches.length <= 1) return;
  const hasDefault = branches.some((branch) => state.flowConditions[branch.flowId]?.isDefault);
  branches.forEach((branch) => {
    const config = state.flowConditions[branch.flowId] || {};
    if (!config.isDefault && !config.condition) {
      issues.push({ level: hasDefault ? 'WARNING' : 'ERROR', message: `Branch ${branch.flowId} needs a condition or default marker.` });
    }
  });
}

function hasBlockingStepErrors(step) {
  return validateWizardStep(step).some((issue) => issue.level === 'ERROR');
}

function globalWizardValidation() {
  const steps = state.wizardSession?.steps || [];
  const issues = steps
    .filter((step) => step.kind !== 'GLOBAL_VALIDATION')
    .flatMap((step) => validateWizardStep(step).map((issue) => ({ ...issue, stepId: step.id, elementId: step.elementId })));
  const validation = validateProject();
  (validation.errors || []).forEach((message) => issues.push({ level: 'ERROR', message }));
  return {
    ready: !issues.some((issue) => issue.level === 'ERROR'),
    errors: issues.filter((issue) => issue.level === 'ERROR').length,
    warnings: issues.filter((issue) => issue.level === 'WARNING').length,
    issues
  };
}

function renderGlobalValidationStep() {
  const summary = globalWizardValidation();
  const steps = state.wizardSession?.steps || [];
  const serviceTasks = steps.filter((step) => step.kind === 'SERVICE_TASK');
  const inboundEvents = steps.filter((step) => step.kind === 'INBOUND_EVENT' || step.kind === 'START_MESSAGE_EVENT');
  const outbound = steps.filter((step) => step.kind === 'OUTBOUND_COMMUNICATION');
  return `
    <div class="wizard-card">
      <h3>Automation Validation</h3>
      <div class="wizard-message ${summary.ready ? 'info' : 'error'}">${summary.ready ? 'READY' : 'NOT READY'}: ${summary.errors} errors, ${summary.warnings} warnings</div>
      <table>
        <tbody>
          <tr><th>BPMN</th><td>${xmlBox.value.includes('<bpmn:definitions') ? 'Valid XML present' : 'BPMN XML missing'}</td></tr>
          <tr><th>Service Tasks</th><td>${countConfigured(serviceTasks)} / ${serviceTasks.length} configured</td></tr>
          <tr><th>Inbound Events</th><td>${countConfigured(inboundEvents)} / ${inboundEvents.length} configured</td></tr>
          <tr><th>Outbound Communications</th><td>${countConfigured(outbound)} / ${outbound.length} configured</td></tr>
          <tr><th>External Services</th><td>Camunda, PPG Management, GMS and CIR configured as runtime dependencies. Availability is runtime, not design validity.</td></tr>
          <tr><th>Deployment Readiness</th><td>${summary.ready ? 'READY' : 'NOT READY'}</td></tr>
        </tbody>
      </table>
      ${renderWizardIssues(summary.issues)}
    </div>`;
}

function countConfigured(steps) {
  return steps.filter((step) => !validateWizardStep(step).some((issue) => issue.level === 'ERROR')).length;
}

function suggestCapability(step) {
  const text = normalizeSearchText(`${step.name} ${step.elementId}`);
  let best = null;
  let bestScore = 0;
  (state.capabilities || []).forEach((capability) => {
    const score = capabilityScore(capability, text);
    if (score > bestScore) {
      best = capability;
      bestScore = score;
    }
  });
  return bestScore > 0 ? best : null;
}

function capabilityScore(capability, text) {
  const haystack = normalizeSearchText(`${capability.id} ${capability.name} ${capability.description} ${capability.type}`);
  return text.split(/\s+/).filter((token) => token.length > 2 && haystack.includes(token)).length;
}

function defaultWizardInboundConfig(step) {
  const event = slugify(step.name).replace(/-/g, '_').toUpperCase();
  return {
    channel: 'EMAIL',
    provider: 'GMS',
    router: 'CIR',
    action: 'CORRELATE_MESSAGE',
    bpmnElementId: step.elementId,
    externalEvent: event,
    camundaMessage: event,
    correlationField: 'requestId',
    correlationExpression: '${requestId}',
    variableMappings: defaultVariableMappings(downstreamRequiredProcessVariables(step.elementId))
  };
}

async function wizardSelectCapability(elementId, capabilityId) {
  state.bindings[elementId] = capabilityId;
  const requirement = state.requirements.find((item) => item.elementId === elementId);
  if (requirement) requirement.capabilityId = capabilityId;
  await applyCapabilityToBpmnElement(elementId, capabilityId);
  applyDeterministicMappingDefaults(elementId, findCapability(capabilityId));
  saveProject();
  renderRequirements();
  renderWizard();
}

function applyDeterministicMappingDefaults(elementId, capability) {
  if (!capability) return;
  state.variableMappings[elementId] = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
  (capability.inputParameters || []).forEach((parameter) => {
    state.variableMappings[elementId].inputs[parameter.name] = state.variableMappings[elementId].inputs[parameter.name] || '${' + parameter.name + '}';
  });
  (capability.outputParameters || []).forEach((parameter) => {
    state.variableMappings[elementId].outputs[parameter.name] = state.variableMappings[elementId].outputs[parameter.name] || defaultOutputVariableName(elementId, parameter);
  });
}

function bindWizardContentEvents() {
  document.querySelectorAll('button[data-wizard-step-index]').forEach((button) => {
    button.addEventListener('click', () => {
      state.wizardSession.currentStep = Number(button.dataset.wizardStepIndex);
      saveProject();
      renderWizard();
    });
  });
  document.querySelectorAll('button[data-wizard-capability]').forEach((button) => {
    button.addEventListener('click', () => wizardSelectCapability(button.dataset.wizardCapability, button.dataset.capabilityId));
  });
  document.querySelectorAll('input[data-wizard-capability-filter]').forEach((input) => {
    input.addEventListener('input', () => {
      const term = normalizeSearchText(input.value);
      input.closest('.wizard-card').querySelectorAll('.wizard-capability-option').forEach((button) => {
        button.style.display = normalizeSearchText(button.textContent).includes(term) ? '' : 'none';
      });
    });
  });
  document.querySelectorAll('button[data-generate-worker]').forEach((button) => {
    button.addEventListener('click', () => {
      generateWorker(button.dataset.generateWorker);
    });
  });
  document.querySelectorAll('button[data-view-code]').forEach((button) => {
    button.addEventListener('click', () => {
      viewGeneratedCode(button.dataset.viewCode);
    });
  });
  document.querySelectorAll('input[data-wizard-mapping]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardMapping;
      state.variableMappings[elementId] = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
      state.variableMappings[elementId][input.dataset.direction] = state.variableMappings[elementId][input.dataset.direction] || {};
      state.variableMappings[elementId][input.dataset.direction][input.dataset.param] = input.value.trim();
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('input[data-wizard-outbound], textarea[data-wizard-outbound]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardOutbound;
      state.outboundConfigs[elementId] = state.outboundConfigs[elementId] || { bpmnElementId: elementId };
      state.outboundConfigs[elementId][input.dataset.field] = input.value.trim();
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('input[data-wizard-inbound], textarea[data-wizard-inbound]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardInbound;
      state.inboundConfigs[elementId] = state.inboundConfigs[elementId] || { bpmnElementId: elementId };
      state.inboundConfigs[elementId][input.dataset.field] = input.value.trim();
      if (input.dataset.field === 'camundaMessage') updateBpmnMessageForEvent(elementId, input.value);
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('input[data-wizard-flow-condition]').forEach((input) => {
    input.addEventListener('change', () => {
      state.flowConditions[input.dataset.wizardFlowCondition] = state.flowConditions[input.dataset.wizardFlowCondition] || {};
      state.flowConditions[input.dataset.wizardFlowCondition].condition = input.value.trim();
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('input[data-wizard-flow-default]').forEach((input) => {
    input.addEventListener('change', () => {
      (state.gatewayBranches || [])
        .filter((branch) => branch.gatewayId === input.dataset.gateway)
        .forEach((branch) => {
          state.flowConditions[branch.flowId] = state.flowConditions[branch.flowId] || {};
          state.flowConditions[branch.flowId].isDefault = branch.flowId === input.dataset.wizardFlowDefault && input.checked;
        });
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
}

function wizardNext() {
  const step = state.wizardSession.steps[state.wizardSession.currentStep];
  if (hasBlockingStepErrors(step)) return;
  state.wizardSession.currentStep = Math.min(state.wizardSession.currentStep + 1, state.wizardSession.steps.length - 1);
  saveProject();
  renderWizard();
}

function wizardBack() {
  state.wizardSession.currentStep = Math.max((state.wizardSession.currentStep || 0) - 1, 0);
  saveProject();
  renderWizard();
}

function finishWizard() {
  const summary = globalWizardValidation();
  if (!summary.ready) return;
  state.wizardSession.status = 'FINISHED';
  state.wizardSession.lastConfiguredElement = state.wizardSession.steps[state.wizardSession.currentStep]?.elementId || '';
  state.wizardSession.lastUpdatedAt = new Date().toISOString();
  setStatus('READY_FOR_DEPLOYMENT');
  saveProject();
  renderRequirements();
  closeWizardModal();
}

function renderWizardStep(step) {
  if (step.kind === 'GLOBAL_VALIDATION') {
    return renderGlobalValidationStep();
  }
  const issues = validateWizardStep(step);
  return `
    <div class="wizard-guidance">
      ${renderTutorialIntro(step)}
      ${renderWizardIssues(issues)}
      ${renderWizardConfigurator(step)}
      ${renderExecutionSummary(step)}
      ${renderTechnicalDetails(step)}
    </div>`;
}

function renderTutorialIntro(step) {
  if (step.kind === 'START_MESSAGE_EVENT') {
    return `
      <div class="wizard-card">
        <h3>Configuração do início do processo - ${escapeHtml(step.name)}</h3>
        <p>Este processo começa quando uma solicitação externa é recebida. Precisamos definir de onde ela chega, como será reconhecida e quais informações ficarão disponíveis para as próximas etapas.</p>
      </div>`;
  }
  if (step.kind === 'SERVICE_TASK') {
    return `
      <div class="wizard-card">
        <h3>Configuração da tarefa "${escapeHtml(step.name)}"</h3>
        <p>Esta é uma tarefa automatizada. Precisamos definir qual funcionalidade disponível no ambiente realizará esse trabalho e quais dados ela receberá e produzirá.</p>
      </div>`;
  }
  if (step.kind === 'OUTBOUND_COMMUNICATION') {
    return `
      <div class="wizard-card">
        <h3>Envio de e-mail - ${escapeHtml(step.name)}</h3>
        <p>Esta tarefa enviará uma mensagem durante a execução do processo. Vamos definir o destinatário, o assunto, a mensagem e, quando houver resposta esperada, o evento correspondente.</p>
      </div>`;
  }
  if (step.kind === 'INBOUND_EVENT') {
    return `
      <div class="wizard-card">
        <h3>Configuração da espera por mensagem - ${escapeHtml(step.name)}</h3>
        <p>Neste ponto o processo ficará aguardando uma mensagem. Precisamos definir qual mensagem é esperada e como o ADE identificará a instância correta quando ela chegar.</p>
      </div>`;
  }
  if (step.kind === 'GATEWAY') {
    return `
      <div class="wizard-card">
        <h3>Configuração da decisão "${escapeHtml(step.name)}"</h3>
        <p>Este ponto decide qual caminho o processo seguirá. Escolha um dado produzido anteriormente e defina quando cada caminho deve ser utilizado.</p>
        ${renderPreviousProducerHint(step)}
      </div>`;
  }
  return `
    <div class="wizard-card">
      <h3>Revisão do elemento "${escapeHtml(step.name)}"</h3>
      <p>Este elemento participa do processo e será validado para garantir que possui informações suficientes.</p>
    </div>`;
}

function renderTechnicalDetails(step) {
  const capability = findCapability(state.bindings[step.elementId]);
  const inbound = state.inboundConfigs[step.elementId] || {};
  return `
    <details class="technical-details">
      <summary>Ver detalhes técnicos</summary>
      <table>
        <tbody>
          <tr><th>BPMN Type</th><td>${escapeHtml(step.bpmnType)}</td></tr>
          <tr><th>BPMN ID</th><td>${escapeHtml(step.elementId)}</td></tr>
          <tr><th>Automation Requirements</th><td>${step.requirementTypes.map(escapeHtml).join(', ')}</td></tr>
          <tr><th>Capability ID</th><td>${escapeHtml(capability?.id || '')}</td></tr>
          <tr><th>Provider</th><td>${escapeHtml(capability?.provider || inbound.provider || '')}</td></tr>
          <tr><th>Endpoint</th><td>${escapeHtml(capability?.endpoint || '')}</td></tr>
          <tr><th>Router</th><td>${escapeHtml(inbound.router || 'CIR')}</td></tr>
          <tr><th>Message Name</th><td>${escapeHtml(inbound.camundaMessage || '')}</td></tr>
          <tr><th>Implementation Type</th><td>${escapeHtml(capability?.implementationType || '')}</td></tr>
        </tbody>
      </table>
    </details>`;
}

function renderWizardIssues(issues) {
  if (!issues.length) {
    return '<div class="wizard-message info">OK: Esta etapa está configurada corretamente.</div>';
  }
  return issues.map((issue) => `<div class="wizard-message ${issue.level.toLowerCase()}">${escapeHtml(localizeIssueLevel(issue.level))}: ${escapeHtml(issue.message)}</div>`).join('');
}

function localizeIssueLevel(level) {
  if (level === 'ERROR') return 'Ação necessária';
  if (level === 'WARNING') return 'Atenção';
  return 'Informação';
}

function renderServiceTaskWizard(step) {
  const capabilityId = state.bindings[step.elementId] || '';
  const capability = findCapability(capabilityId);
  const suggestion = suggestCapability(step);
  const context = buildProcessDataContext(step.elementId);
  const legacyNotice = legacyCapabilityNotice(step);
  return `
    <div class="wizard-card">
      <h3>O que esta tarefa deve fazer?</h3>
      <p>O ADE comparou o nome da tarefa, o tipo BPMN e os contratos das capabilities para priorizar funcionalidades compatíveis.</p>
      ${suggestion ? `<div class="wizard-message info">Sugestão determinística: ${escapeHtml(suggestion.id)} - ${escapeHtml(suggestion.name)}</div>` : ''}
      ${legacyNotice}
      ${renderWizardCapabilityPicker(step, capabilityId)}
    </div>
    <div class="wizard-card">
      <h3>Dados disponíveis antes desta tarefa</h3>
      ${renderProcessDataContext(context)}
    </div>
    ${capability ? `
      <div class="wizard-card">
        <h3>${escapeHtml(capability.name)}</h3>
        <p>${escapeHtml(capability.description || '')}</p>
        ${renderCapabilityNeedReturn(capability)}
        ${renderCapabilityMappingEditor(step.elementId, capability, context)}
        ${renderRequirementImplementation(step.elementId, capability)}
      </div>` : ''}`;
}

function renderOutboundWizard(step) {
  if (!state.bindings[step.elementId]) {
    state.bindings[step.elementId] = 'SEND_EMAIL';
  }
  const config = state.outboundConfigs[step.elementId] || {};
  const capability = findCapability(state.bindings[step.elementId]);
  const context = buildProcessDataContext(step.elementId);
  return `
    <div class="wizard-card">
      <h3>Como a mensagem será enviada?</h3>
      <p>Nesta distribuição, a capability SEND_EMAIL representa o envio de mensagens por worker de automação.</p>
      ${renderWizardCapabilityPicker(step, state.bindings[step.elementId])}
    </div>
    <div class="wizard-card">
      <h3>Mensagem</h3>
      <label>Destinatário</label>
      ${renderValueSourcePicker('emailTo', step.elementId, config.emailTo || '', context, 'String', 'data-wizard-outbound')}
      <label>Assunto</label>
      <input data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="emailSubject" value="${escapeAttribute(config.emailSubject || '')}">
      <label>Mensagem</label>
      <textarea data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="emailBody">${escapeHtml(config.emailBody || '')}</textarea>
      <label>Resposta esperada</label>
      <input data-wizard-outbound="${escapeAttribute(step.elementId)}" data-field="expectedResponse" value="${escapeAttribute(config.expectedResponse || '')}">
    </div>
    <div class="wizard-card">
      <h3>Dados disponíveis para este e-mail</h3>
      ${renderProcessDataContext(context)}
    </div>
    ${capability ? `<div class="wizard-card">${renderCapabilityNeedReturn(capability)}${renderCapabilityMappingEditor(step.elementId, capability, context)}</div>` : ''}`;
}

function renderInboundWizard(step) {
  const config = state.inboundConfigs[step.elementId] || defaultWizardInboundConfig(step);
  state.inboundConfigs[step.elementId] = config;
  const context = buildProcessDataContext(step.elementId);
  return `
    <div class="wizard-card">
      <h3>Como a solicitação chega?</h3>
      <p>O GMS recebe a mensagem e o CIR reconhece o evento externo. Depois disso, o CIR inicia ou correlaciona a mensagem com uma instância do processo no Camunda.</p>
      <label>Canal de recebimento</label>
      <select data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="channel">
        <option value="EMAIL" ${config.channel === 'EMAIL' ? 'selected' : ''}>E-mail</option>
      </select>
      <label>Qual evento externo representa esta mensagem?</label>
      <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="externalEvent" value="${escapeAttribute(config.externalEvent || '')}">
      <label>Nome da mensagem no processo</label>
      <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="camundaMessage" value="${escapeAttribute(config.camundaMessage || '')}">
    </div>
    <div class="wizard-card">
      <h3>Como o ADE reconhecerá a resposta correta?</h3>
      <p>Durante a execução podem existir várias solicitações aguardando respostas. O sistema precisa de uma informação que permita descobrir a qual solicitação cada resposta pertence.</p>
      <label>Identificador da solicitação</label>
      ${renderVariablePicker('correlationField', step.elementId, config.correlationField || 'requestId', context, 'String', 'data-wizard-inbound')}
      <small>Termo técnico: Correlation Key</small>
      <label>Expressão técnica gerada</label>
      <input data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="correlationExpression" value="${escapeAttribute(config.correlationExpression || toExpression(config.correlationField || 'requestId'))}">
    </div>
    <div class="wizard-card">
      <h3>Quais dados ficarão disponíveis?</h3>
      <p>Informe os dados extraídos da mensagem. Cada chave do JSON vira um dado disponível para as próximas etapas.</p>
      <textarea data-wizard-inbound="${escapeAttribute(step.elementId)}" data-field="variableMappings">${escapeHtml(config.variableMappings || defaultVariableMappings())}</textarea>
      <h3>Dados que esta etapa produzirÃ¡</h3>
      ${renderProcessDataContext(buildProducedDataContext(step))}
      <h3>Dados disponÃ­veis antes desta etapa</h3>
      ${renderProcessDataContext(context)}
    </div>`;
}

function renderGatewayWizard(step) {
  const context = buildProcessDataContext(step.elementId);
  const branches = (state.gatewayBranches || []).filter((branch) => branch.gatewayId === step.elementId);
  const preferred = preferredGatewayVariable(context);
  return `
    <div class="wizard-card">
      <h3>Dados disponíveis para esta decisão</h3>
      ${renderProcessDataContext(context)}
      ${preferred ? `<div class="wizard-message info">A melhor opção para esta decisão parece ser ${escapeHtml(preferred.name)}:${escapeHtml(preferred.type)}, porque ela foi produzida antes deste gateway.</div>` : ''}
    </div>
    <div class="wizard-card">
      <h3>Condições dos caminhos</h3>
      ${branches.map((branch) => renderGatewayBranchBuilder(step, branch, context, preferred)).join('')}
    </div>`;
}

function renderGatewayBranchBuilder(step, branch, context, preferred) {
  const config = state.flowConditions[branch.flowId] || {};
  const visual = config.visual || inferVisualCondition(config.condition, context) || {
    variable: preferred?.name || '',
    operator: preferred?.type === 'Boolean' ? 'isTrue' : 'equals',
    value: preferred?.type === 'Boolean' ? 'true' : ''
  };
  const variable = context.variables.find((item) => item.name === visual.variable) || preferred || { type: 'String' };
  const isDefault = Boolean(config.isDefault);
  return `
    <div class="branch-row">
      <strong>Caminho "${escapeHtml(branch.flowName || branch.targetName || branch.flowId)}"</strong>
      <div class="wizard-choice">
        <div>
          <label>Dado do processo</label>
          ${renderVariablePicker(`flow-variable-${branch.flowId}`, branch.flowId, visual.variable, context, '', 'data-wizard-condition-var')}
        </div>
        <div>
          <label>Operador</label>
          <select data-wizard-condition-operator="${escapeAttribute(branch.flowId)}">
            ${conditionOperators(variable.type).map((operator) => `<option value="${operator.value}" ${visual.operator === operator.value ? 'selected' : ''}>${escapeHtml(operator.label)}</option>`).join('')}
          </select>
        </div>
        <div>
          <label>Valor</label>
          ${renderConditionValueControl(branch.flowId, variable.type, visual)}
        </div>
      </div>
      <label><input type="checkbox" data-wizard-flow-default="${escapeAttribute(branch.flowId)}" data-gateway="${escapeAttribute(step.elementId)}" ${isDefault ? 'checked' : ''}> Usar este caminho como padrão</label>
      <small>Expressão técnica: ${escapeHtml(config.condition || buildConditionExpression(visual, variable.type))}</small>
    </div>`;
}

function renderGlobalValidationStep() {
  const summary = globalWizardValidation();
  const heading = summary.ready ? 'Processo pronto para implantação' : `${summary.errors} configurações precisam de atenção`;
  return `
    <div class="wizard-card">
      <h3>${escapeHtml(heading)}</h3>
      <div class="wizard-message ${summary.ready ? 'info' : 'error'}">${summary.ready ? 'OK: O projeto pode seguir para implantação.' : 'Revise os itens abaixo antes de finalizar.'}</div>
      ${renderReadinessChecklist(summary)}
      ${summary.issues.length ? renderActionableGlobalIssues(summary.issues) : ''}
      <details class="technical-details">
        <summary>Ver detalhes técnicos</summary>
        <pre>${JSON.stringify(summary, null, 2)}</pre>
      </details>
    </div>`;
}

function renderReadinessChecklist(summary) {
  return `
    <ul>
      <li>${summary.checks.start ? 'OK' : 'Pendente'} - O início do processo está configurado.</li>
      <li>${summary.checks.capabilities ? 'OK' : 'Pendente'} - Todas as tarefas automatizadas possuem funcionalidades associadas.</li>
      <li>${summary.checks.mappings ? 'OK' : 'Pendente'} - Os dados obrigatórios estão mapeados.</li>
      <li>${summary.checks.gateways ? 'OK' : 'Pendente'} - As decisões possuem condições válidas.</li>
      <li>${summary.checks.messages ? 'OK' : 'Pendente'} - As mensagens possuem regras de correlação.</li>
      <li>Info - Camunda, PPG Management, GMS e CIR são verificados em tempo de execução pelo compose.</li>
    </ul>`;
}

function renderActionableGlobalIssues(issues) {
  return `<ol>${issues.map((issue) => `<li><strong>${escapeHtml(issue.elementName || issue.elementId || 'Projeto')}</strong> - ${escapeHtml(issue.message)} ${issue.stepIndex >= 0 ? `<button type="button" data-wizard-step-index="${issue.stepIndex}">Revisar</button>` : ''}</li>`).join('')}</ol>`;
}

function globalWizardValidation() {
  const steps = state.wizardSession?.steps || [];
  const issues = steps
    .filter((step) => step.kind !== 'GLOBAL_VALIDATION')
    .flatMap((step, stepIndex) => validateWizardStep(step).map((issue) => ({
      ...issue,
      stepId: step.id,
      stepIndex,
      elementId: step.elementId,
      elementName: step.name
    })));
  const checks = {
    start: !steps.some((step) => step.kind === 'START_MESSAGE_EVENT' && hasBlockingStepErrors(step)),
    capabilities: !steps.some((step) => ['SERVICE_TASK', 'OUTBOUND_COMMUNICATION'].includes(step.kind) && !state.bindings[step.elementId]),
    mappings: !issues.some((issue) => issue.message.includes('associado') || issue.message.includes('mapeado')),
    gateways: !steps.some((step) => step.kind === 'GATEWAY' && hasBlockingStepErrors(step)),
    messages: !steps.some((step) => ['INBOUND_EVENT', 'START_MESSAGE_EVENT'].includes(step.kind) && hasBlockingStepErrors(step))
  };
  return {
    ready: !issues.some((issue) => issue.level === 'ERROR'),
    errors: issues.filter((issue) => issue.level === 'ERROR').length,
    warnings: issues.filter((issue) => issue.level === 'WARNING').length,
    checks,
    issues
  };
}

function validateCapabilityAndMappings(step, issues) {
  const capability = findCapability(state.bindings[step.elementId]);
  if (!capability) {
    issues.push({ level: 'ERROR', message: 'Escolha qual funcionalidade executará esta tarefa.' });
    return;
  }
  const context = buildProcessDataContext(step.elementId);
  const mappings = state.variableMappings[step.elementId] || { inputs: {}, outputs: {} };
  (capability.inputParameters || []).forEach((parameter) => {
    const value = ((mappings.inputs || {})[parameter.name] || '').trim();
    if (!value) {
      issues.push({ level: 'ERROR', message: `O dado obrigatório ${parameter.name} ainda não foi associado a nenhum dado do processo.` });
      return;
    }
    const variableName = stripExpression(value);
    if (!context.variables.some((item) => item.name === variableName)) {
      issues.push({ level: 'WARNING', message: `O dado ${variableName} não foi encontrado entre os dados disponíveis antes desta tarefa.` });
    }
  });
  (capability.outputParameters || []).forEach((parameter) => {
    if (!((mappings.outputs || {})[parameter.name] || '').trim()) {
      issues.push({ level: 'WARNING', message: `O resultado ${parameter.name} não será salvo para etapas posteriores.` });
    }
  });
}

function validateOutboundWizardStep(step, issues) {
  if (!state.bindings[step.elementId]) {
    issues.push({ level: 'ERROR', message: 'Escolha a funcionalidade responsável pelo envio da mensagem.' });
  }
  const config = state.outboundConfigs[step.elementId] || {};
  if (!config.emailTo) issues.push({ level: 'ERROR', message: 'Defina quem receberá o e-mail.' });
  if (!config.emailSubject) issues.push({ level: 'ERROR', message: 'Defina o assunto do e-mail.' });
  if (!config.emailBody) issues.push({ level: 'WARNING', message: 'A mensagem está vazia.' });
}

function validateInboundWizardStep(step, issues) {
  const config = state.inboundConfigs[step.elementId] || {};
  if (!config.channel) issues.push({ level: 'ERROR', message: 'Escolha o canal de recebimento da mensagem.' });
  if (!config.externalEvent) issues.push({ level: 'ERROR', message: 'Informe o evento externo reconhecido pelo CIR.' });
  if (!config.camundaMessage) issues.push({ level: 'ERROR', message: 'Informe o nome da mensagem no processo.' });
  if (!config.correlationField) issues.push({ level: 'ERROR', message: 'Escolha o identificador usado para reconhecer a solicitação correta.' });
  if (!config.correlationExpression) issues.push({ level: 'ERROR', message: 'Informe a expressão técnica de correlação.' });
  if (config.variableMappings) {
    try {
      JSON.parse(config.variableMappings);
    } catch (error) {
      issues.push({ level: 'ERROR', message: 'Os dados extraídos da mensagem precisam estar em JSON válido.' });
    }
  }
}

function validateGatewayWizardStep(step, issues) {
  const branches = (state.gatewayBranches || []).filter((branch) => branch.gatewayId === step.elementId);
  if (branches.length <= 1) return;
  const seen = new Set();
  branches.forEach((branch) => {
    const config = state.flowConditions[branch.flowId] || {};
    if (config.isDefault) return;
    if (!config.condition) {
      issues.push({ level: 'ERROR', message: `O caminho "${branch.flowName || branch.targetName || branch.flowId}" ainda não possui condição.` });
      return;
    }
    if (seen.has(config.condition)) {
      issues.push({ level: 'WARNING', message: `A condição do caminho "${branch.flowName || branch.flowId}" é igual a outra condição deste gateway.` });
    }
    seen.add(config.condition);
    const visual = config.visual || inferVisualCondition(config.condition, buildProcessDataContext(step.elementId));
    if (visual && visual.variable && !buildProcessDataContext(step.elementId).variables.some((item) => item.name === visual.variable)) {
      issues.push({ level: 'ERROR', message: `A variável ${visual.variable} não está disponível antes desta decisão.` });
    }
  });
}

function buildProcessDataContext(elementId) {
  const steps = state.wizardSession?.steps || buildWizardSteps();
  const variables = new Map();
  const stopIndex = steps.findIndex((step) => step.elementId === elementId);
  const priorSteps = stopIndex >= 0 ? steps.slice(0, stopIndex) : steps;
  priorSteps.forEach((step) => addProducedVariablesForStep(variables, step));
  return {
    elementId,
    variables: Array.from(variables.values()).sort(variablePrioritySort)
  };
}

function addProducedVariablesForStep(variables, step) {
  const add = (descriptor) => {
    if (!descriptor.name || variables.has(descriptor.name)) return;
    variables.set(descriptor.name, descriptor);
  };
  const inbound = state.inboundConfigs[step.elementId]
    || (['START_MESSAGE_EVENT', 'INBOUND_EVENT'].includes(step.kind) ? defaultWizardInboundConfig(step) : null);
  if (inbound) {
    parseVariableMappings(inbound.variableMappings).forEach((mapping) => add({
      name: mapping.name,
      type: inferVariableType(mapping.name),
      origin: step.name,
      producerElement: step.elementId,
      description: `Dado recebido em "${step.name}"`,
      availability: 'AVAILABLE'
    }));
    if (inbound.correlationField) {
      add({
        name: inbound.correlationField,
        type: 'String',
        origin: step.name,
        producerElement: step.elementId,
        description: 'Identificador usado para correlacionar mensagens com a instância correta.',
        availability: 'AVAILABLE'
      });
    }
  }
  const capability = findCapability(state.bindings[step.elementId]);
  const mappings = state.variableMappings[step.elementId] || {};
  if (capability && mappings.outputs) {
    (capability.outputParameters || []).forEach((parameter) => {
      const outputName = mappings.outputs[parameter.name];
      if (outputName) {
        add({
          name: stripExpression(outputName),
          type: normalizeCapabilityType(parameter.type),
          origin: step.name,
          producerElement: step.elementId,
          description: `Resultado ${parameter.name} produzido por ${capability.name}.`,
          availability: 'AVAILABLE'
        });
      }
    });
  }
}

function parseVariableMappings(json) {
  try {
    const parsed = JSON.parse(json || '{}');
    return Object.keys(parsed).map((name) => ({ name, expression: parsed[name] }));
  } catch (error) {
    return [];
  }
}

function renderProcessDataContext(context) {
  if (!context.variables.length) {
    return '<p><small>Nenhum dado anterior foi identificado ainda.</small></p>';
  }
  return `<div class="wizard-context-list">${context.variables.map((variable) => `
    <div class="wizard-context-item">
      <strong>${escapeHtml(variable.name)}</strong>
      <span>${escapeHtml(variable.type)}</span>
      <span>${escapeHtml(variable.origin)}<br><small>${escapeHtml(variable.description || '')}</small></span>
    </div>`).join('')}</div>`;
}

function renderVariablePicker(field, elementId, selectedValue, context, preferredType, attributeName) {
  const selected = stripExpression(selectedValue);
  const options = sortedVariablesForType(context.variables, preferredType);
  return `
    <select ${attributeName}="${escapeAttribute(elementId)}" data-field="${escapeAttribute(field)}">
      <option value="">Selecionar dado do processo</option>
      ${options.map((variable) => `<option value="${escapeAttribute(variable.name)}" ${selected === variable.name ? 'selected' : ''}>${escapeHtml(variable.name)} (${escapeHtml(variable.type)}) - ${escapeHtml(variable.origin)}</option>`).join('')}
    </select>`;
}

function renderValueSourcePicker(field, elementId, value, context, preferredType, attributeName) {
  const isExpression = String(value || '').startsWith('${');
  return `
    <div class="wizard-choice">
      <div>
        <label>Fonte do valor</label>
        <select data-value-source="${escapeAttribute(field)}" data-value-element="${escapeAttribute(elementId)}" data-preferred-type="${escapeAttribute(preferredType || '')}">
          <option value="variable" ${isExpression ? 'selected' : ''}>Dado do processo</option>
          <option value="literal" ${!isExpression ? 'selected' : ''}>Valor fixo</option>
        </select>
      </div>
      <div style="grid-column: span 2;">
        ${isExpression
          ? renderVariablePicker(field, elementId, value, context, preferredType, attributeName)
          : `<input ${attributeName}="${escapeAttribute(elementId)}" data-field="${escapeAttribute(field)}" value="${escapeAttribute(value || '')}">`}
      </div>
    </div>`;
}

function renderCapabilityNeedReturn(capability) {
  return `
    <div class="wizard-grid">
      <div><strong>O que ela precisa?</strong>${renderFriendlyParameterList(capability.inputParameters || [])}</div>
      <div><strong>O que ela devolve?</strong>${renderFriendlyParameterList(capability.outputParameters || [])}</div>
    </div>`;
}

function renderFriendlyParameterList(parameters) {
  if (!parameters.length) return '<p><small>Nenhum dado.</small></p>';
  return `<ul>${parameters.map((parameter) => `<li>${escapeHtml(friendlyParameterName(parameter.name))} <small>${escapeHtml(parameter.name)}:${escapeHtml(parameter.type || '')}</small></li>`).join('')}</ul>`;
}

function friendlyParameterName(name) {
  return String(name || '')
    .replace(/Id$/, ' identifier')
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .toLowerCase();
}

function renderCapabilityMappingEditor(elementId, capability, context = buildProcessDataContext(elementId)) {
  return `
    <h3>Como os dados serão usados?</h3>
    <div>
      ${(capability.inputParameters || []).map((parameter) => renderTutorialInputMapping(elementId, parameter, context)).join('')}
      <h3>Quais resultados você deseja disponibilizar para as próximas etapas?</h3>
      ${(capability.outputParameters || []).map((parameter) => renderTutorialOutputMapping(elementId, parameter)).join('')}
    </div>`;
}

function renderTutorialInputMapping(elementId, parameter, context) {
  const mappings = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
  const value = (mappings.inputs || {})[parameter.name] || '';
  return `
    <div class="wizard-mapping-row">
      <span>Qual dado do processo será usado como ${escapeHtml(friendlyParameterName(parameter.name))}?</span>
      <span>&lt;-</span>
      ${renderVariablePicker(parameter.name, elementId, value, context, normalizeCapabilityType(parameter.type), 'data-wizard-mapping-input')}
    </div>`;
}

function renderTutorialOutputMapping(elementId, parameter) {
  const mappings = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
  const value = (mappings.outputs || {})[parameter.name] || '';
  return `
    <div class="wizard-mapping-row">
      <span>${escapeHtml(friendlyParameterName(parameter.name))}</span>
      <span>-&gt;</span>
      <input data-wizard-mapping-output="${escapeAttribute(elementId)}" data-param="${escapeAttribute(parameter.name)}" value="${escapeAttribute(value)}" placeholder="${escapeAttribute(defaultOutputVariableName(elementId, parameter))}">
    </div>`;
}

function bindWizardContentEvents() {
  document.querySelectorAll('button[data-wizard-step-index]').forEach((button) => {
    button.addEventListener('click', () => {
      state.wizardSession.currentStep = Number(button.dataset.wizardStepIndex);
      saveProject();
      renderWizard();
    });
  });
  document.querySelectorAll('button[data-wizard-capability]').forEach((button) => {
    button.addEventListener('click', () => wizardSelectCapability(button.dataset.wizardCapability, button.dataset.capabilityId));
  });
  document.querySelectorAll('input[data-wizard-capability-filter]').forEach((input) => {
    input.addEventListener('input', () => {
      const term = normalizeSearchText(input.value);
      input.closest('.wizard-card').querySelectorAll('.wizard-capability-option').forEach((button) => {
        button.style.display = normalizeSearchText(button.textContent).includes(term) ? '' : 'none';
      });
    });
  });
  document.querySelectorAll('select[data-wizard-mapping-input]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardMappingInput;
      state.variableMappings[elementId] = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
      state.variableMappings[elementId].inputs[input.dataset.field] = toExpression(input.value);
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('input[data-wizard-mapping-output]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardMappingOutput;
      state.variableMappings[elementId] = state.variableMappings[elementId] || { inputs: {}, outputs: {} };
      state.variableMappings[elementId].outputs[input.dataset.param] = input.value.trim();
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('select[data-wizard-outbound], input[data-wizard-outbound], textarea[data-wizard-outbound]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardOutbound;
      state.outboundConfigs[elementId] = state.outboundConfigs[elementId] || { bpmnElementId: elementId };
      state.outboundConfigs[elementId][input.dataset.field] = input.tagName === 'SELECT' && input.dataset.field === 'emailTo'
        ? toExpression(input.value)
        : input.value.trim();
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('select[data-value-source]').forEach((input) => {
    input.addEventListener('change', () => {
      const field = input.dataset.valueSource;
      const elementId = input.dataset.valueElement;
      const preferredType = input.dataset.preferredType || '';
      state.outboundConfigs[elementId] = state.outboundConfigs[elementId] || { bpmnElementId: elementId };
      if (input.value === 'variable') {
        const context = buildProcessDataContext(elementId);
        const variable = sortedVariablesForType(context.variables, preferredType)[0];
        state.outboundConfigs[elementId][field] = variable ? toExpression(variable.name) : '';
      } else {
        state.outboundConfigs[elementId][field] = stripExpression(state.outboundConfigs[elementId][field] || '');
      }
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('select[data-wizard-inbound], input[data-wizard-inbound], textarea[data-wizard-inbound]').forEach((input) => {
    input.addEventListener('change', () => {
      const elementId = input.dataset.wizardInbound;
      state.inboundConfigs[elementId] = state.inboundConfigs[elementId] || { bpmnElementId: elementId };
      state.inboundConfigs[elementId][input.dataset.field] = input.dataset.field === 'correlationField'
        ? stripExpression(input.value)
        : input.value.trim();
      if (input.dataset.field === 'correlationField') {
        state.inboundConfigs[elementId].correlationExpression = toExpression(input.value);
      }
      if (input.dataset.field === 'camundaMessage') updateBpmnMessageForEvent(elementId, input.value);
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('select[data-wizard-condition-var], select[data-wizard-condition-operator], select[data-wizard-condition-value], input[data-wizard-condition-value]').forEach((input) => {
    input.addEventListener('change', () => updateVisualGatewayCondition(input));
  });
  document.querySelectorAll('input[data-wizard-flow-default]').forEach((input) => {
    input.addEventListener('change', () => {
      (state.gatewayBranches || [])
        .filter((branch) => branch.gatewayId === input.dataset.gateway)
        .forEach((branch) => {
          state.flowConditions[branch.flowId] = state.flowConditions[branch.flowId] || {};
          state.flowConditions[branch.flowId].isDefault = branch.flowId === input.dataset.wizardFlowDefault && input.checked;
        });
      saveProject();
      renderRequirements();
      renderWizard();
    });
  });
  document.querySelectorAll('button[data-generate-worker]').forEach((button) => {
    button.addEventListener('click', () => generateWorker(button.dataset.generateWorker));
  });
  document.querySelectorAll('button[data-view-code]').forEach((button) => {
    button.addEventListener('click', () => viewGeneratedCode(button.dataset.viewCode));
  });
}

function renderWizardCapabilityPicker(step, selectedCapabilityId) {
  const recommended = recommendedCapabilities(step);
  const all = state.capabilities.filter((capability) => !recommended.some((item) => item.id === capability.id));
  return `
    <strong>Funcionalidades compatíveis</strong>
    <div class="wizard-capability-list">
      ${recommended.map((capability) => renderCapabilityOption(step, capability, selectedCapabilityId)).join('') || '<p><small>Nenhuma sugestão segura foi encontrada.</small></p>'}
    </div>
    <details class="technical-details">
      <summary>Mostrar todas as capabilities</summary>
      <label>Buscar capability</label>
      <input data-wizard-capability-filter="${escapeAttribute(step.elementId)}" value="">
      <div class="wizard-capability-list">
        ${all.map((capability) => renderCapabilityOption(step, capability, selectedCapabilityId)).join('')}
      </div>
    </details>`;
}

function renderCapabilityOption(step, capability, selectedCapabilityId) {
  return `
    <button type="button" class="wizard-capability-option ${capability.id === selectedCapabilityId ? 'selected' : ''}" data-wizard-capability="${escapeAttribute(step.elementId)}" data-capability-id="${escapeAttribute(capability.id)}">
      <strong>${escapeHtml(capability.id)}</strong>
      <span>${escapeHtml(capability.provider)} - ${escapeHtml(capability.interfaceType)} - ${escapeHtml(capability.status)}</span>
      <small>${escapeHtml(capability.description || '')}</small>
    </button>`;
}

function recommendedCapabilities(step) {
  const text = normalizeSearchText(`${step.name} ${step.elementId}`);
  return [...state.capabilities]
    .map((capability) => ({ capability, score: capabilityCompatibilityScore(capability, step, text) }))
    .filter((item) => item.score > 0)
    .sort((left, right) => right.score - left.score)
    .slice(0, 6)
    .map((item) => item.capability);
}

function capabilityCompatibilityScore(capability, step, text) {
  let score = capabilityScore(capability, text);
  if (step.kind === 'OUTBOUND_COMMUNICATION' && capability.id === 'SEND_EMAIL') score += 10;
  if (step.kind === 'SERVICE_TASK' && capability.interfaceType && capability.interfaceType.startsWith('REST')) score += 2;
  if ((text.includes('verificar') || text.includes('validar') || text.includes('check')) && capability.id.includes('CHECK')) score += 7;
  if ((text.includes('verificar') || text.includes('validar') || text.includes('validate')) && capability.id.includes('VALIDATE')) score += 5;
  if (text.includes('cancel') && capability.id.includes('CANCEL')) score += 6;
  if (text.includes('orientacao') && capability.type === 'ADVISORSHIP') score += 4;
  if (text.includes('student') || text.includes('estudante')) {
    if (capability.type === 'STUDENT' || capability.id.includes('STUDENT')) score += 4;
  }
  if (text.includes('professor') || text.includes('orientador')) {
    if (capability.type === 'PROFESSOR' || capability.id.includes('PROFESSOR')) score += 4;
  }
  if (text.includes('defesa') || text.includes('dissertation')) {
    if (capability.type === 'DEFENSE' || capability.type === 'DISSERTATION_DOCUMENT') score += 4;
  }
  return score;
}

function updateVisualGatewayCondition(input) {
  const flowId = input.dataset.wizardConditionVar || input.dataset.wizardConditionOperator || input.dataset.wizardConditionValue;
  const row = input.closest('.branch-row');
  const variable = row.querySelector('select[data-wizard-condition-var]')?.value || '';
  const operator = row.querySelector('select[data-wizard-condition-operator]')?.value || 'equals';
  const valueElement = row.querySelector('select[data-wizard-condition-value], input[data-wizard-condition-value]');
  const value = valueElement ? valueElement.value : '';
  const context = buildProcessDataContext(state.wizardSession.steps[state.wizardSession.currentStep].elementId);
  const descriptor = context.variables.find((item) => item.name === variable) || { type: 'String' };
  const visual = { variable, operator, value };
  state.flowConditions[flowId] = state.flowConditions[flowId] || {};
  state.flowConditions[flowId].visual = visual;
  state.flowConditions[flowId].condition = buildConditionExpression(visual, descriptor.type);
  saveProject();
  renderRequirements();
  renderWizard();
}

function conditionOperators(type) {
  if (type === 'Boolean') {
    return [
      { value: 'isTrue', label: 'for verdadeiro' },
      { value: 'isFalse', label: 'for falso' },
      { value: 'equals', label: 'for igual a' }
    ];
  }
  if (type === 'Number' || type === 'Long' || type === 'Integer') {
    return [
      { value: 'equals', label: 'igual' },
      { value: 'gt', label: 'maior' },
      { value: 'lt', label: 'menor' },
      { value: 'gte', label: 'maior ou igual' },
      { value: 'lte', label: 'menor ou igual' }
    ];
  }
  return [
    { value: 'equals', label: 'é igual a' },
    { value: 'notEquals', label: 'é diferente de' },
    { value: 'empty', label: 'está vazio' },
    { value: 'notEmpty', label: 'não está vazio' }
  ];
}

function renderConditionValueControl(flowId, type, visual) {
  if (type === 'Boolean') {
    return `
      <select data-wizard-condition-value="${escapeAttribute(flowId)}">
        <option value="true" ${visual.value === 'true' ? 'selected' : ''}>verdadeiro</option>
        <option value="false" ${visual.value === 'false' ? 'selected' : ''}>falso</option>
      </select>`;
  }
  return `<input data-wizard-condition-value="${escapeAttribute(flowId)}" value="${escapeAttribute(visual.value || '')}">`;
}

function buildConditionExpression(visual, type) {
  if (!visual.variable) return '';
  if (type === 'Boolean') {
    if (visual.operator === 'isFalse') return '${' + visual.variable + ' == false}';
    if (visual.operator === 'equals') return '${' + visual.variable + ' == ' + (visual.value || 'true') + '}';
    return '${' + visual.variable + ' == true}';
  }
  if (visual.operator === 'empty') return '${' + visual.variable + ' == null || ' + visual.variable + ' == ""}';
  if (visual.operator === 'notEmpty') return '${' + visual.variable + ' != null && ' + visual.variable + ' != ""}';
  const quoted = type === 'Number' || type === 'Long' || type === 'Integer' ? visual.value : '"' + escapeJavaStringLiteral(visual.value || '') + '"';
  const operators = { equals: '==', notEquals: '!=', gt: '>', lt: '<', gte: '>=', lte: '<=' };
  return '${' + visual.variable + ' ' + (operators[visual.operator] || '==') + ' ' + quoted + '}';
}

function inferVisualCondition(expression, context) {
  const match = String(expression || '').match(/\$\{\s*([A-Za-z_][A-Za-z0-9_]*)\s*(==|!=|>=|<=|>|<)\s*(true|false|"[^"]*"|[0-9.]+)\s*}/);
  if (!match) return null;
  const variable = context.variables.find((item) => item.name === match[1]);
  return {
    variable: match[1],
    operator: match[2] === '==' ? 'equals' : match[2] === '!=' ? 'notEquals' : match[2] === '>' ? 'gt' : match[2] === '<' ? 'lt' : match[2] === '>=' ? 'gte' : 'lte',
    value: match[3].replace(/^"|"$/g, ''),
    type: variable?.type || inferVariableType(match[1])
  };
}

function preferredGatewayVariable(context) {
  return context.variables.find((variable) => variable.type === 'Boolean') || context.variables[0];
}

function renderPreviousProducerHint(step) {
  const context = buildProcessDataContext(step.elementId);
  const preferred = preferredGatewayVariable(context);
  if (!preferred) return '';
  return `<div class="wizard-message info">A etapa anterior ou uma etapa já executada produziu ${escapeHtml(preferred.name)}:${escapeHtml(preferred.type)}. Esse dado pode ser usado para configurar esta decisão.</div>`;
}

function renderExecutionSummary(step) {
  if (step.kind === 'START_MESSAGE_EVENT') {
    const config = state.inboundConfigs[step.elementId] || {};
    return `
      <div class="wizard-card">
        <h3>O que acontecerá durante a execução</h3>
        <ol>
          <li>O GMS receberá a mensagem pelo canal ${escapeHtml(config.channel || 'EMAIL')}.</li>
          <li>O CIR reconhecerá o evento ${escapeHtml(config.externalEvent || '')}.</li>
          <li>Uma nova instância será iniciada ou uma mensagem será correlacionada no Camunda.</li>
          <li>Os dados extraídos ficarão disponíveis para as próximas etapas.</li>
        </ol>
      </div>`;
  }
  if (step.kind === 'SERVICE_TASK') {
    const capability = findCapability(state.bindings[step.elementId]);
    return `
      <div class="wizard-card">
        <h3>O que acontecerá durante a execução</h3>
        <ol>
          <li>Os dados mapeados serão lidos da instância do processo.</li>
          <li>${escapeHtml(capability?.provider || 'O serviço configurado')} será acionado.</li>
          <li>Os resultados mapeados ficarão disponíveis para as próximas etapas.</li>
        </ol>
      </div>`;
  }
  if (step.kind === 'GATEWAY') {
    const branches = (state.gatewayBranches || []).filter((branch) => branch.gatewayId === step.elementId);
    return `
      <div class="wizard-card">
        <h3>O que acontecerá durante a execução</h3>
        <ul>${branches.map((branch) => `<li>${escapeHtml(state.flowConditions[branch.flowId]?.condition || 'sem condição')} - ${escapeHtml(branch.flowName || branch.targetName || branch.flowId)}</li>`).join('')}</ul>
      </div>`;
  }
  return '';
}

function sortedVariablesForType(variables, preferredType) {
  return [...variables].sort((left, right) => {
    const leftType = preferredType && left.type === preferredType ? 0 : 1;
    const rightType = preferredType && right.type === preferredType ? 0 : 1;
    return leftType - rightType || variablePrioritySort(left, right);
  });
}

function variablePrioritySort(left, right) {
  const typeOrder = { Boolean: 0, Long: 1, Integer: 1, Number: 1, String: 2, File: 3 };
  return (typeOrder[left.type] ?? 9) - (typeOrder[right.type] ?? 9) || left.name.localeCompare(right.name);
}

function normalizeCapabilityType(type) {
  if (!type) return 'String';
  if (['Long', 'Integer', 'Double', 'Float', 'BigDecimal'].includes(type)) return type === 'Long' || type === 'Integer' ? type : 'Number';
  if (type.includes('Boolean')) return 'Boolean';
  if (type.includes('File')) return 'File';
  return 'String';
}

function inferVariableType(name) {
  const lower = normalizeSearchText(name);
  if (lower.startsWith('is') || lower.includes('valid') || lower.includes('complete') || lower.includes('completo') || lower.includes('aprovado')) return 'Boolean';
  if (lower.endsWith('id') || lower.includes('count') || lower.includes('quantidade')) return 'Long';
  if (lower.includes('file')) return 'File';
  return 'String';
}

function toExpression(value) {
  const cleaned = stripExpression(value);
  return cleaned ? '${' + cleaned + '}' : '';
}

function buildProducedDataContext(step) {
  const variables = new Map();
  addProducedVariablesForStep(variables, step);
  return {
    elementId: step.elementId,
    variables: Array.from(variables.values()).sort(variablePrioritySort)
  };
}

function defaultOutputVariableName(elementId, parameter) {
  if (normalizeCapabilityType(parameter.type) === 'Boolean') {
    const nextGatewayName = findNextGatewayName(elementId);
    if (nextGatewayName) {
      return toCamelCase(nextGatewayName.replace(/[?]/g, ''));
    }
  }
  return stripExpression(parameter.name);
}

function findNextGatewayName(elementId) {
  const parser = new DOMParser();
  const doc = parser.parseFromString(xmlBox.value, 'text/xml');
  const elements = Array.from(doc.getElementsByTagName('*'));
  const outgoing = elements.find((node) => node.localName === 'sequenceFlow' && node.getAttribute('sourceRef') === elementId);
  if (!outgoing) return '';
  const targetId = outgoing.getAttribute('targetRef');
  const target = elements.find((node) => node.getAttribute && node.getAttribute('id') === targetId);
  return target && target.localName === 'exclusiveGateway' ? target.getAttribute('name') || targetId : '';
}

function toCamelCase(value) {
  const parts = slugify(value).split('-').filter(Boolean);
  return parts.map((part, index) => index === 0 ? part : part.charAt(0).toUpperCase() + part.slice(1)).join('');
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

function activeProcessDefinitionKey() {
  return document.getElementById('projectKey').value || '';
}

async function refreshInstances() {
  if (!state.activeProjectOpen) {
    return;
  }
  const processDefinitionKey = activeProcessDefinitionKey();
  if (!processDefinitionKey) {
    document.getElementById('instanceList').innerHTML = '<p><small>Project key is required to list instances.</small></p>';
    return;
  }
  const response = await fetch(`/api/execution/instances?processDefinitionKey=${encodeURIComponent(processDefinitionKey)}`);
  const data = await response.json();
  renderInstances(data);
}

function renderInstances(data) {
  const container = document.getElementById('instanceList');
  if (!container) {
    return;
  }
  if (!Array.isArray(data)) {
    container.innerHTML = `<pre>${JSON.stringify(data, null, 2)}</pre>`;
    return;
  }
  if (!data.length) {
    container.innerHTML = '<p><small>No running instances for this project.</small></p>';
    return;
  }
  container.innerHTML = `
    <table>
      <thead>
        <tr>
          <th>Instance</th>
          <th>State</th>
          <th>Definition</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        ${data.map((item) => `
          <tr>
            <td>${escapeHtml(item.id || '')}</td>
            <td>${item.suspended ? 'SUSPENDED' : 'RUNNING'}</td>
            <td>${escapeHtml(item.processDefinitionId || item.definitionId || '')}</td>
            <td>
              <button data-view-instance="${escapeAttribute(item.id || '')}">View</button>
              <button data-cancel-instance="${escapeAttribute(item.id || '')}">Cancel</button>
            </td>
          </tr>`).join('')}
      </tbody>
    </table>`;
}

async function cancelInstance(instanceId) {
  if (!instanceId || !confirm(`Cancel running instance ${instanceId}? This will not delete the Camunda process definition.`)) {
    return;
  }
  const response = await fetch(`/api/execution/instances/${encodeURIComponent(instanceId)}/cancel`, {
    method: 'POST'
  });
  const data = await response.json();
  document.getElementById('executionInfo').textContent = JSON.stringify(data, null, 2);
  await refreshInstances();
}

function renderProject() {
  const container = document.getElementById('projectSummary');
  if (!container) {
    return;
  }
  container.innerHTML = state.activeProjectOpen
    ? `<pre>${JSON.stringify(project(), null, 2)}</pre>`
    : '<p><small>No project open.</small></p>';
}

async function init() {
  seedExampleProjects();
  state.capabilities = await fetch('/api/capabilities').then((response) => response.json());
  renderCapabilities();
  const runtime = await fetch('/api/runtime').then((response) => response.json());
  document.getElementById('runtime').textContent = `Camunda: ${runtime.camundaBaseUrl}`;
  openInitialProject();
}

document.querySelectorAll('button[data-tab]').forEach((button) => {
  button.addEventListener('click', () => {
    showTab(button.dataset.tab);
  });
});

document.getElementById('projectMenuToggle').addEventListener('click', (event) => {
  event.stopPropagation();
  document.getElementById('projectMenu').classList.toggle('open');
});

document.addEventListener('click', (event) => {
  if (!event.target.closest('#projectMenu')) {
    document.getElementById('projectMenu').classList.remove('open');
  }
});

document.addEventListener('click', (event) => {
  const target = event.target;
  if (!target.matches('button')) {
    return;
  }
  if (target.dataset.viewInstance) {
    document.getElementById('executionInfo').textContent = JSON.stringify({ instanceId: target.dataset.viewInstance }, null, 2);
  }
  if (target.dataset.cancelInstance) {
    cancelInstance(target.dataset.cancelInstance);
  }
});

document.querySelectorAll('[data-project-field]').forEach((input) => {
  input.addEventListener('input', () => {
    markDirty();
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
document.getElementById('menuNewProject').addEventListener('click', () => newProject(false));
document.getElementById('menuOpenProject').addEventListener('click', showProjectBrowser);
document.getElementById('menuSaveProject').addEventListener('click', saveProject);
document.getElementById('menuCloseProject').addEventListener('click', () => closeProject(true));
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
document.getElementById('refreshInstances').addEventListener('click', refreshInstances);
document.getElementById('runWizard').addEventListener('click', () => startWizard('run'));
document.getElementById('resumeWizard').addEventListener('click', () => startWizard('resume'));
document.getElementById('reviewWizard').addEventListener('click', () => startWizard('review'));
document.getElementById('wizardBack').addEventListener('click', wizardBack);
document.getElementById('wizardNext').addEventListener('click', wizardNext);
document.getElementById('wizardFinish').addEventListener('click', finishWizard);
document.getElementById('wizardCancel').addEventListener('click', cancelWizard);
document.getElementById('wizardClose').addEventListener('click', cancelWizard);
document.addEventListener('keydown', (event) => {
  if (event.key === 'Escape' && document.getElementById('wizardOverlay').classList.contains('open')) {
    cancelWizard();
  }
});

init();
