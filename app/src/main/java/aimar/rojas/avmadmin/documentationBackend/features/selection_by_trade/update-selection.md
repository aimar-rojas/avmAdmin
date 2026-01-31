# Update Selection - Actualizar Selection by Trade

## Descripción
Endpoint para actualizar una selección existente.

## Endpoint
```
PUT /api/v1/selections/:id
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Path Parameters
- `id` (uint): ID de la selección a actualizar (`selection_by_trade_id`).

## Request Body
Todos los campos son opcionales. Solo se actualizarán los campos que envíes.

```json
{
  "trade_id": 2,
  "selection_type_id": 3,
  "price": "20.00"
}
```

### Campos Opcionales
- `trade_id` (uint): Nuevo ID del trade asociado.
- `selection_type_id` (uint): Nuevo ID del tipo de selección.
- `price` (decimal|null): Nuevo precio. Puede ser un número o `null`.

## Response

### Success (200 OK)
```json
{
  "message": "Selection actualizado exitosamente",
  "selection": {
    "selection_by_trade_id": 1,
    "trade_id": 2,
    "selection_type_id": 3,
    "price": "20.00",
    "trade": {
      "trade_id": 2,
      "party_id": 1,
      "boss_id": 1,
      "shipment_id": 1,
      "trade_type": "SALE"
    },
    "selection_type": {
      "selection_type_id": 3,
      "name_selection": "Tipo B"
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

### Error (404 Not Found)
```json
{
  "error": "selection no encontrado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "ID de selection inválido"
}
```

### Error (500 Internal Server Error)
```json
{
  "error": "error al actualizar selection"
}
```
Este error puede ocurrir si:
- El nuevo `trade_id` no existe
- El nuevo `selection_type_id` no existe
- La nueva combinación de `trade_id` y `selection_type_id` ya existe (violación de índice único)

## Ejemplo de Uso

### Actualizar solo el precio
```bash
curl -X PUT http://localhost:5001/api/v1/selections/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "price": "20.00"
  }'
```

### Actualizar trade_id y selection_type_id
```bash
curl -X PUT http://localhost:5001/api/v1/selections/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "trade_id": 2,
    "selection_type_id": 3
  }'
```

### Actualizar múltiples campos
```bash
curl -X PUT http://localhost:5001/api/v1/selections/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "trade_id": 2,
    "selection_type_id": 3,
    "price": "20.00"
  }'
```

### Eliminar precio (establecer a null)
```bash
curl -X PUT http://localhost:5001/api/v1/selections/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "price": null
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');
const selectionId = 1;

// Actualizar solo algunos campos
const response = await fetch(`http://localhost:5001/api/v1/selections/${selectionId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    price: '20.00'
  })
});

const data = await response.json();
console.log('Selection actualizado:', data.selection);
```

## Notas
- La actualización es parcial: solo se actualizan los campos que envíes
- Si actualizas `trade_id` o `selection_type_id`, asegúrate de que los nuevos valores existan en la base de datos
- La combinación de `trade_id` y `selection_type_id` debe ser única (índice único compuesto)
- Para eliminar el precio, envía `null` en el campo `price`
- El selection actualizado incluye las relaciones (Trade y SelectionType) en la respuesta
- El precio debe ser un número decimal válido si se proporciona
