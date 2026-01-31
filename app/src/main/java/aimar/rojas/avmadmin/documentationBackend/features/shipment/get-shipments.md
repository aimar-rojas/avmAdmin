# Get Shipments - Obtener Shipments

## Descripción
Endpoint para obtener todos los shipments (envíos) del sistema con paginación y filtros opcionales por status y fechas.

## Endpoint
```
GET /api/v1/shipments
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Query Parameters

### Parámetros Opcionales
- `page` (int): Número de página. Por defecto: 1
- `limit` (int): Cantidad de registros por página. Por defecto: 50, máximo: 200
- `status` (string): Filtro por estado. Valores válidos: `OPEN` o `CLOSED`
- `start_date` (string): Filtro por fecha de inicio. Formato: `YYYY-MM-DD` (ej: `2024-01-01`)
- `end_date` (string): Filtro por fecha de fin. Formato: `YYYY-MM-DD` (ej: `2024-12-31`)

## Ejemplos de Uso

### Obtener todos los shipments (sin filtro)
```
GET /api/v1/shipments?page=1&limit=50
```
Trae los primeros 50 shipments.

### Obtener solo shipments abiertos (OPEN)
```
GET /api/v1/shipments?page=1&limit=50&status=OPEN
```
Trae los primeros 50 shipments con estado OPEN.

### Obtener solo shipments cerrados (CLOSED)
```
GET /api/v1/shipments?page=1&limit=50&status=CLOSED
```
Trae los primeros 50 shipments con estado CLOSED.

### Filtrar por rango de fechas
```
GET /api/v1/shipments?start_date=2024-01-01&end_date=2024-12-31
```
Trae shipments cuya fecha de inicio esté entre el 1 de enero y el 31 de diciembre de 2024.

### Combinar filtros
```
GET /api/v1/shipments?status=OPEN&start_date=2024-01-01&end_date=2024-12-31&page=1&limit=50
```
Trae shipments abiertos dentro del rango de fechas especificado.

### Paginación
```
GET /api/v1/shipments?page=2&limit=50
```
Trae los registros 51-100 (segunda página con 50 registros por página).

## Response

### Success (200 OK)
```json
{
  "shipments": [
    {
      "shipment_id": 1,
      "start_date": "2024-01-01",
      "end_date": null,
      "status": "OPEN",
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2024-01-01T00:00:00Z"
    },
    {
      "shipment_id": 2,
      "start_date": "2024-01-15",
      "end_date": "2024-01-20",
      "status": "CLOSED",
      "created_at": "2024-01-15T00:00:00Z",
      "updated_at": "2024-01-20T00:00:00Z"
    }
  ],
  "total": 150,
  "page": 1,
  "limit": 50,
  "total_pages": 3,
  "has_next": true,
  "has_previous": false
}
```

#### Campos de Respuesta
- `shipments` (array): Lista de shipments.
- `total` (int64): Total de registros que cumplen los filtros.
- `page` (int): Página actual.
- `limit` (int): Cantidad de registros por página.
- `total_pages` (int): Total de páginas disponibles.
- `has_next` (boolean): Indica si hay una página siguiente.
- `has_previous` (boolean): Indica si hay una página anterior.

#### Campos del Shipment
- `shipment_id` (uint): ID único del shipment.
- `start_date` (string): Fecha de inicio del shipment (formato: YYYY-MM-DD).
- `end_date` (string|null): Fecha de fin del shipment (opcional, formato: YYYY-MM-DD).
- `status` (string): Estado del shipment: `OPEN` (abierto) o `CLOSED` (cerrado).

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "status debe ser 'OPEN' o 'CLOSED'"
}
```

```json
{
  "error": "formato de start_date inválido. Use formato YYYY-MM-DD (ej: 2024-01-01)"
}
```

## Ejemplo de Uso

### cURL - Obtener todos los shipments
```bash
curl -X GET "http://localhost:5001/api/v1/shipments?page=1&limit=50" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Obtener solo shipments abiertos
```bash
curl -X GET "http://localhost:5001/api/v1/shipments?status=OPEN" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Filtrar por rango de fechas
```bash
curl -X GET "http://localhost:5001/api/v1/shipments?start_date=2024-01-01&end_date=2024-12-31" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Obtener todos los shipments
const response = await fetch('http://localhost:5001/api/v1/shipments?page=1&limit=50', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
console.log(`Total de shipments: ${data.total}`);

// Obtener solo shipments abiertos
const openShipments = await fetch('http://localhost:5001/api/v1/shipments?status=OPEN', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Filtrar por rango de fechas
const filteredShipments = await fetch('http://localhost:5001/api/v1/shipments?start_date=2024-01-01&end_date=2024-12-31', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## Notas
- **Este endpoint trae TODOS los shipments del sistema**, no solo los del usuario autenticado
- Los resultados están ordenados por fecha descendente (los más recientes primero)
- Los filtros pueden combinarse:
  - `status`: Filtra por estado (OPEN o CLOSED)
  - `start_date`: Filtra shipments con fecha de inicio mayor o igual a la fecha especificada
  - `end_date`: Filtra shipments con fecha de inicio menor o igual a la fecha especificada
- La paginación funciona con `limit` y `offset` calculado automáticamente desde `page`
- Por defecto trae 50 registros por página
- El máximo permitido es 200 registros por página
- Las fechas deben estar en formato `YYYY-MM-DD` (ISO 8601 date format)
