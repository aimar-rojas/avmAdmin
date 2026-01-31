# Update Trade - Actualizar Trade

## Descripción
Endpoint para actualizar un trade existente. Solo se pueden actualizar trades que pertenezcan al usuario autenticado.

## Endpoint
```
PUT /api/v1/trades/:id
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Path Parameters
- `id` (uint): ID del trade a actualizar.

## Request Body
Todos los campos son opcionales. Solo se actualizarán los campos que envíes.

```json
{
  "party_id": 2,
  "shipment_id": 1,
  "trade_type": "SALE",
  "start_datetime": "2024-01-02T10:00:00Z",
  "end_datetime": "2024-01-02T18:00:00Z",
  "discount_weight_per_tray": "6.00",
  "variety_avocado": "Fuerte"
}
```

### Campos Opcionales
- `party_id` (uint): Nuevo ID de la parte involucrada.
- `shipment_id` (uint): Nuevo ID del envío asociado.
- `trade_type` (string): Nuevo tipo de trade. Debe ser `PURCHASE` o `SALE`.
- `start_datetime` (string): Nueva fecha y hora de inicio. Formato RFC3339.
- `end_datetime` (string|null): Nueva fecha y hora de fin. Formato RFC3339. Puede ser `null` o cadena vacía para eliminar.
- `discount_weight_per_tray` (decimal): Nuevo descuento de peso por bandeja.
- `variety_avocado` (string): Nueva variedad de aguacate.

## Response

### Success (200 OK)
```json
{
  "message": "Trade actualizado exitosamente",
  "trade": {
    "trade_id": 1,
    "party_id": 2,
    "boss_id": 1,
    "shipment_id": 1,
    "trade_type": "SALE",
    "start_datetime": "2024-01-02T10:00:00Z",
    "end_datetime": "2024-01-02T18:00:00Z",
    "discount_weight_per_tray": "6.00",
    "variety_avocado": "Fuerte",
    "party": {
      "party_id": 2,
      "party_role": "CUSTOMER",
      "alias_name": "Cliente XYZ"
    },
    "boss": {
      "boss_id": 1,
      "name_boss": "Mi Negocio"
    },
    "shipment": {
      "shipment_id": 1,
      "status": "OPEN"
    },
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

### Error (403 Forbidden)
```json
{
  "error": "no tienes permiso para actualizar este trade"
}
```
Este error ocurre cuando intentas actualizar un trade que no pertenece a tu usuario.

### Error (404 Not Found)
```json
{
  "error": "trade no encontrado"
}
```

```json
{
  "error": "no se encontraron datos de boss para este usuario"
}
```

### Error (400 Bad Request)
```json
{
  "error": "ID de trade inválido"
}
```

```json
{
  "error": "formato de start_datetime inválido"
}
```

## Ejemplo de Uso

### Actualizar solo el tipo de trade
```bash
curl -X PUT http://localhost:5001/api/v1/trades/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "trade_type": "SALE"
  }'
```

### Actualizar múltiples campos
```bash
curl -X PUT http://localhost:5001/api/v1/trades/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_id": 2,
    "trade_type": "SALE",
    "start_datetime": "2024-01-02T10:00:00Z",
    "discount_weight_per_tray": "6.00",
    "variety_avocado": "Fuerte"
  }'
```

### Eliminar end_datetime (establecer a null)
```bash
curl -X PUT http://localhost:5001/api/v1/trades/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "end_datetime": ""
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');
const tradeId = 1;

// Actualizar solo algunos campos
const response = await fetch(`http://localhost:5001/api/v1/trades/${tradeId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    trade_type: 'SALE',
    discount_weight_per_tray: '6.00',
    variety_avocado: 'Fuerte'
  })
});

const data = await response.json();
```

## Notas
- Solo puedes actualizar trades que pertenezcan a tu usuario (basado en `boss_id`)
- La actualización es parcial: solo se actualizan los campos que envíes
- Para eliminar `end_datetime`, envía una cadena vacía `""`
- `trade_type` debe ser exactamente `PURCHASE` o `SALE` si se proporciona
- Las fechas deben estar en formato RFC3339 (ISO 8601)
- `variety_avocado` es opcional en la actualización
- El trade actualizado incluye todas las relaciones (Party, Boss, Shipment) en la respuesta
