# Create Shipment - Crear Shipment

## Descripción
Endpoint para crear un nuevo shipment (envío).

## Endpoint
```
POST /api/v1/shipments
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Request Body
```json
{
  "start_date": "2024-01-01",
  "end_date": "2024-01-15",
  "status": "OPEN"
}
```

### Campos Requeridos
- `start_date` (string): Fecha de inicio del shipment. Formato: `YYYY-MM-DD` (ej: `2024-01-01`).
- `status` (string): Estado del shipment. Debe ser `OPEN` (abierto) o `CLOSED` (cerrado).

### Campos Opcionales
- `end_date` (string|null): Fecha de fin del shipment. Formato: `YYYY-MM-DD`. Si no se proporciona, será `null`.

## Response

### Success (201 Created)
```json
{
  "message": "Shipment creado exitosamente",
  "shipment": {
    "shipment_id": 1,
    "start_date": "2024-01-01",
    "end_date": "2024-01-15",
    "status": "OPEN",
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:00:00Z"
  }
}
```

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "formato de start_date inválido. Use formato YYYY-MM-DD (ej: 2024-01-01)"
}
```

```json
{
  "error": "Key: 'CreateShipmentRequest.Status' Error:Field validation for 'Status' failed on the 'oneof' tag"
}
```

## Ejemplo de Uso

### Crear un shipment sin fecha de fin
```bash
curl -X POST http://localhost:5001/api/v1/shipments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "start_date": "2024-01-01",
    "status": "OPEN"
  }'
```

### Crear un shipment con fecha de fin
```bash
curl -X POST http://localhost:5001/api/v1/shipments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "start_date": "2024-01-01",
    "end_date": "2024-01-15",
    "status": "OPEN"
  }'
```

### Crear un shipment cerrado
```bash
curl -X POST http://localhost:5001/api/v1/shipments \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "start_date": "2024-01-01",
    "end_date": "2024-01-15",
    "status": "CLOSED"
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Crear un shipment
const response = await fetch('http://localhost:5001/api/v1/shipments', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    start_date: '2024-01-01',
    end_date: '2024-01-15',
    status: 'OPEN'
  })
});

const data = await response.json();
console.log('Shipment creado:', data.shipment);
```

## Notas
- `start_date` es obligatorio y debe estar en formato `YYYY-MM-DD`
- `end_date` es opcional. Si no se proporciona, será `null`
- `status` debe ser exactamente `OPEN` o `CLOSED` (case-sensitive)
- Las fechas se almacenan como tipo `date` en la base de datos (sin hora)
- El shipment creado se retorna completo en la respuesta
