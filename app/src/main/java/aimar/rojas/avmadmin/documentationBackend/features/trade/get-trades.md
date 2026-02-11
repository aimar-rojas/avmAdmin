# Get Trades - Obtener Trades (Compras y Ventas)

## Descripción
Endpoint para obtener los trades (transacciones) de un shipment específico con paginación y filtros opcionales por tipo. Este endpoint sirve tanto para obtener **PURCHASE** (compras) como **SALE** (ventas) de un envío determinado.

## Endpoint
```
GET /api/v1/trades
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Query Parameters

### Parámetros Requeridos
- `shipment_id` (uint): ID del shipment (envío) del cual se desean obtener los trades

### Parámetros Opcionales
- `page` (int): Número de página. Por defecto: 1
- `limit` (int): Cantidad de registros por página. Por defecto: 50, máximo: 200
- `trade_type` (string): Filtro por tipo de trade. Valores válidos: `PURCHASE` o `SALE`

## Ejemplos de Uso

### Obtener todos los trades de un shipment (sin filtro por tipo)
```
GET /api/v1/trades?shipment_id=1&page=1&limit=50
```
Trae los primeros 50 trades (tanto compras como ventas) del shipment con ID 1.

### Obtener solo compras (PURCHASE) de un shipment
```
GET /api/v1/trades?shipment_id=1&page=1&limit=50&trade_type=PURCHASE
```
Trae los primeros 50 trades de tipo PURCHASE (compras) del shipment con ID 1.

### Obtener solo ventas (SALE) de un shipment
```
GET /api/v1/trades?shipment_id=1&page=1&limit=50&trade_type=SALE
```
Trae los primeros 50 trades de tipo SALE (ventas) del shipment con ID 1.

### Paginación
```
GET /api/v1/trades?shipment_id=1&page=2&limit=50
```
Trae los registros 51-100 (segunda página con 50 registros por página) del shipment con ID 1.

## Response

### Success (200 OK)
```json
{
  "trades": [
    {
      "trade_id": 1,
      "party_id": 1,
      "boss_id": 1,
      "shipment_id": 1,
      "trade_type": "PURCHASE",
      "start_datetime": "2024-01-01T10:00:00Z",
      "end_datetime": null,
      "discount_weight_per_tray": "5.50",
      "variety_avocado": "Hass",
      "party": {
        "party_id": 1,
        "party_role": "SUPPLIER",
        "alias_name": "Proveedor ABC",
        "first_name": "Juan",
        "last_name": "Pérez",
        "dni": "12345678",
        "ruc": "20123456789",
        "phone": "987654321"
      },
      "boss": {
        "boss_id": 1,
        "name_boss": "Mi Negocio",
        "boss_type": "RETAIL",
        "user_id": 1
      },
      "shipment": {
        "shipment_id": 1,
        "start_date": "2024-01-01",
        "end_date": null,
        "status": "OPEN"
      },
      "created_at": "2024-01-01T00:00:00Z",
      "updated_at": "2024-01-01T00:00:00Z"
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
- `trades` (array): Lista de trades con sus relaciones (Party, Boss, Shipment).
- `total` (int64): Total de registros que cumplen los filtros.
- `page` (int): Página actual.
- `limit` (int): Cantidad de registros por página.
- `total_pages` (int): Total de páginas disponibles.
- `has_next` (boolean): Indica si hay una página siguiente.
- `has_previous` (boolean): Indica si hay una página anterior.

#### Campos del Trade
- `trade_id` (uint): ID único del trade.
- `party_id` (uint): ID de la parte involucrada (proveedor/cliente).
- `boss_id` (uint): ID del negocio que realiza el trade.
- `shipment_id` (uint): ID del envío asociado.
- `trade_type` (string): Tipo de trade: `PURCHASE` (compra) o `SALE` (venta).
- `start_datetime` (string): Fecha y hora de inicio del trade (formato RFC3339).
- `end_datetime` (string|null): Fecha y hora de fin del trade (opcional).
- `discount_weight_per_tray` (decimal): Descuento de peso por bandeja.
- `variety_avocado` (string): Variedad de aguacate.

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "shipment_id es requerido"
}
```

O

```json
{
  "error": "trade_type debe ser 'PURCHASE' o 'SALE'"
}
```

## Ejemplo de Uso

### cURL - Obtener todos los trades de un shipment
```bash
curl -X GET "http://localhost:5001/api/v1/trades?shipment_id=1&page=1&limit=50" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Obtener solo compras de un shipment
```bash
curl -X GET "http://localhost:5001/api/v1/trades?shipment_id=1&page=1&limit=50&trade_type=PURCHASE" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Obtener solo ventas de un shipment
```bash
curl -X GET "http://localhost:5001/api/v1/trades?shipment_id=1&page=1&limit=50&trade_type=SALE" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');
const shipmentId = 1; // ID del shipment

// Obtener todos los trades de un shipment
const response = await fetch(`http://localhost:5001/api/v1/trades?shipment_id=${shipmentId}&page=1&limit=50`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
console.log(`Total de trades: ${data.total}`);
console.log(`Página ${data.page} de ${data.total_pages}`);

// Obtener solo compras de un shipment
const purchasesResponse = await fetch(`http://localhost:5001/api/v1/trades?shipment_id=${shipmentId}&trade_type=PURCHASE`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Obtener solo ventas de un shipment
const salesResponse = await fetch(`http://localhost:5001/api/v1/trades?shipment_id=${shipmentId}&trade_type=SALE`, {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## Notas
- **Este endpoint trae los trades de un shipment específico**, identificado por el parámetro requerido `shipment_id`
- Los resultados están ordenados por fecha descendente (los más recientes primero)
- El parámetro `shipment_id` es **requerido** y debe ser un ID válido de shipment
- El filtro `trade_type` es opcional:
  - Sin `trade_type`: Trae tanto compras como ventas del shipment
  - Con `trade_type=PURCHASE`: Trae solo compras del shipment
  - Con `trade_type=SALE`: Trae solo ventas del shipment
- La paginación funciona con `limit` y `offset` calculado automáticamente desde `page`
- Por defecto trae 50 registros por página
- El máximo permitido es 200 registros por página
- Las relaciones (Party, Boss, Shipment) se incluyen automáticamente en la respuesta
