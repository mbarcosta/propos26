# E02 APIs

## ADE

```http
GET /api/health
GET /api/runtime
GET /api/capabilities
POST /api/deployments
```

## CIR

```http
GET /api/cir/health
POST /api/cir/execute?bindingId={bindingId}
GET /api/cir/routes
POST /api/cir/routes
```

## GMS

```http
GET /api/health
POST /api/bindings/{bindingId}/poll
POST /api/bindings/{bindingId}/messages/processed
```

## PPG Management

```http
GET /api/health
GET /api/students
GET /api/students/{id}
POST /api/students
GET /api/professors
GET /api/professors/{id}
POST /api/professors
GET /api/advisorships
GET /api/advisorships/{id}
POST /api/advisorships
```

