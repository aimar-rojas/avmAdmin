# Get Selections - Obtener Selections by Trade

## Descripción
Endpoint para obtener todas las selecciones por trade con filtros opcionales por `trade_id` y `selection_type_id`.

## Endpoint
```
GET /api/v1/selections
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Query Parameters

### Parámetros Opcionales
- `trade_id` (uint): Filtro por ID de trade.
- `selection_type_id` (uint): Filtro por ID de tipo de selección.

**Nota:** Los filtros se pueden usar individualmente o combinados. Si no se especifica ningún filtro, trae todas las selecciones.

## Ejemplos de Uso

### Obtener todas las selecciones (sin filtro)
```
GET /api/v1/selections
```
Trae todas las selecciones del sistema.

### Filtrar por trade_id
```
GET /api/v1/selections?trade_id=1
```
Trae todas las selecciones asociadas al trade con ID 1.

### Filtrar por selection_type_id
```
GET /api/v1/selections?selection_type_id=2
```
Trae todas las selecciones del tipo de selección con ID 2.

### Combinar filtros
```
GET /api/v1/selections?trade_id=1&selection_type_id=2
```
Trae las selecciones que pertenecen al trade 1 y son del tipo de selección 2.

## Response

### Success (200 OK)
```json
{
  "selections": [
    {
      "selection_by_trade_id": 1,
      "trade_id": 1,
      "selection_type_id": 2,
      "price": "15.50",
      "trade": {
        "trade_id": 1,
        "party_id": 1,
        "boss_id": 1,
        "shipment_id": 1,
        "trade_type": "PURCHASE",
        "start_datetime": "2024-01-01T10:00:00Z"
      },
      "selection_type": {
        "selection_type_id": 2,
        "name_selection": "Tipo A"
      },
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2024-01-01T00:00:00Z"
    }
  ],
  "total": 1
}
```

#### Campos de Respuesta
- `selections` (array): Lista de selecciones que cumplen los filtros.
- `total` (int): Total de selecciones retornadas.

#### Campos del Selection
- `selection_by_trade_id` (uint): ID único de la selección.
- `trade_id` (uint): ID del trade asociado.
- `selection_type_id` (uint): ID del tipo de selección.
- `price` (decimal|null): Precio de la selección (opcional).
- `trade` (object): Información del trade asociado (si se incluye la relación).
- `selection_type` (object): Información del tipo de selección (si se incluye la relación).

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

## Ejemplo de Uso

### cURL - Obtener todas las selecciones
```bash
curl -X GET "http://localhost:5001/api/v1/selections" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Filtrar por trade_id
```bash
curl -X GET "http://localhost:5001/api/v1/selections?trade_id=1" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Filtrar por selection_type_id
```bash
curl -X GET "http://localhost:5001/api/v1/selections?selection_type_id=2" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Combinar filtros
```bash
curl -X GET "http://localhost:5001/api/v1/selections?trade_id=1&selection_type_id=2" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Obtener todas las selecciones
const response = await fetch('http://localhost:5001/api/v1/selections', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();

// Filtrar por trade_id
const byTrade = await fetch('http://localhost:5001/api/v1/selections?trade_id=1', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Combinar filtros
const filtered = await fetch('http://localhost:5001/api/v1/selections?trade_id=1&selection_type_id=2', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## Notas
- **Este endpoint NO tiene paginación** ya que se espera que haya pocos registros
- **Este endpoint trae TODAS las selecciones del sistema**, no solo las del usuario autenticado
- Los resultados están ordenados por `selection_by_trade_id` ascendente
- Los filtros se pueden usar individualmente o combinados
- Las relaciones (Trade y SelectionType) se incluyen automáticamente en la respuesta
- Si `price` es `null`, significa que no se ha establecido un precio para esa selección
