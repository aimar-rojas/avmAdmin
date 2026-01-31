# Create Selection - Crear Selection by Trade

## Descripción
Endpoint para crear una nueva selección asociada a un trade.

## Endpoint
```
POST /api/v1/selections
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
  "trade_id": 1,
  "selection_type_id": 2,
  "price": "15.50"
}
```

### Campos Requeridos
- `trade_id` (uint): ID del trade al que pertenece la selección.
- `selection_type_id` (uint): ID del tipo de selección.

### Campos Opcionales
- `price` (decimal|null): Precio de la selección. Si no se proporciona, será `null`.

## Response

### Success (201 Created)
```json
{
  "message": "Selection creado exitosamente",
  "selection": {
    "selection_by_trade_id": 1,
    "trade_id": 1,
    "selection_type_id": 2,
    "price": "15.50",
    "trade": {
      "trade_id": 1,
      "party_id": 1,
      "boss_id": 1,
      "shipment_id": 1,
      "trade_type": "PURCHASE"
    },
    "selection_type": {
      "selection_type_id": 2,
      "name_selection": "Tipo A"
    },
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
  "error": "Key: 'CreateSelectionRequest.TradeID' Error:Field validation for 'TradeID' failed on the 'required' tag"
}
```

### Error (500 Internal Server Error)
```json
{
  "error": "error al crear selection"
}
```
Este error puede ocurrir si:
- El `trade_id` no existe
- El `selection_type_id` no existe
- Ya existe una combinación única de `trade_id` y `selection_type_id` (violación de índice único)

## Ejemplo de Uso

### Crear una selección con precio
```bash
curl -X POST http://localhost:5001/api/v1/selections \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "trade_id": 1,
    "selection_type_id": 2,
    "price": "15.50"
  }'
```

### Crear una selección sin precio
```bash
curl -X POST http://localhost:5001/api/v1/selections \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "trade_id": 1,
    "selection_type_id": 2
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Crear una selección
const response = await fetch('http://localhost:5001/api/v1/selections', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    trade_id: 1,
    selection_type_id: 2,
    price: '15.50'
  })
});

const data = await response.json();
console.log('Selection creado:', data.selection);
```

## Notas
- `trade_id` y `selection_type_id` son obligatorios
- `price` es opcional y puede ser `null`
- La combinación de `trade_id` y `selection_type_id` debe ser única (índice único compuesto)
- El trade y selection_type deben existir en la base de datos antes de crear la selección
- El selection creado incluye las relaciones (Trade y SelectionType) en la respuesta
- El precio debe ser un número decimal válido (formato: "15.50")
