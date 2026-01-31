# Update Shipment - Actualizar Shipment

## Descripción
Endpoint para actualizar un shipment existente.

## Endpoint
```
PUT /api/v1/shipments/:id
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Path Parameters
- `id` (uint): ID del shipment a actualizar.

## Request Body
Todos los campos son opcionales. Solo se actualizarán los campos que envíes.

```json
{
  "start_date": "2024-01-02",
  "end_date": "2024-01-20",
  "status": "CLOSED"
}
```

### Campos Opcionales
- `start_date` (string): Nueva fecha de inicio. Formato: `YYYY-MM-DD`.
- `end_date` (string|null): Nueva fecha de fin. Formato: `YYYY-MM-DD`. Puede ser `null` o cadena vacía `""` para eliminar.
- `status` (string): Nuevo estado. Debe ser `OPEN` o `CLOSED`.

## Response

### Success (200 OK)
```json
{
  "message": "Shipment actualizado exitosamente",
  "shipment": {
    "shipment_id": 1,
    "start_date": "2024-01-02",
    "end_date": "2024-01-20",
    "status": "CLOSED",
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-26T10:30:00Z"
  }
}
```

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (404 Not Found)
```json
{
  "error": "shipment no encontrado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "ID de shipment inválido"
}
```

```json
{
  "error": "formato de start_date inválido"
}
```

## Ejemplo de Uso

### Actualizar solo el estado
```bash
curl -X PUT http://localhost:5001/api/v1/shipments/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CLOSED"
  }'
```

### Actualizar fecha de fin
```bash
curl -X PUT http://localhost:5001/api/v1/shipments/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "end_date": "2024-01-20"
  }'
```

### Actualizar múltiples campos
```bash
curl -X PUT http://localhost:5001/api/v1/shipments/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "start_date": "2024-01-02",
    "end_date": "2024-01-20",
    "status": "CLOSED"
  }'
```

### Eliminar end_date (establecer a null)
```bash
curl -X PUT http://localhost:5001/api/v1/shipments/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "end_date": ""
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');
const shipmentId = 1;

// Actualizar solo algunos campos
const response = await fetch(`http://localhost:5001/api/v1/shipments/${shipmentId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    status: 'CLOSED',
    end_date: '2024-01-20'
  })
});

const data = await response.json();
console.log('Shipment actualizado:', data.shipment);
```

## Notas
- La actualización es parcial: solo se actualizan los campos que envíes
- Para eliminar `end_date`, envía una cadena vacía `""`
- `status` debe ser exactamente `OPEN` o `CLOSED` si se proporciona
- Las fechas deben estar en formato `YYYY-MM-DD` (ISO 8601 date format)
- El shipment actualizado se retorna completo en la respuesta
