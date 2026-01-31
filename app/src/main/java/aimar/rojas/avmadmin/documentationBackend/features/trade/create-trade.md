# Create Trade - Crear Trade (Compra o Venta)

## Descripción
Endpoint para crear un nuevo trade (transacción). Puede ser tanto una **PURCHASE** (compra) como una **SALE** (venta).

## Endpoint
```
POST /api/v1/trades
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
  "party_id": 1,
  "shipment_id": 1,
  "trade_type": "PURCHASE",
  "start_datetime": "2024-01-01T10:00:00Z",
  "end_datetime": "2024-01-01T18:00:00Z",
  "discount_weight_per_tray": "5.50",
  "variety_avocado": "Hass"
}
```

### Campos Requeridos
- `party_id` (uint): ID de la parte involucrada (proveedor para compras, cliente para ventas).
- `shipment_id` (uint): ID del envío asociado.
- `trade_type` (string): Tipo de trade. Debe ser `PURCHASE` (compra) o `SALE` (venta).
- `start_datetime` (string): Fecha y hora de inicio del trade. Formato RFC3339 (ej: `2024-01-01T10:00:00Z`).
- `discount_weight_per_tray` (decimal): Descuento de peso por bandeja. Formato numérico con decimales.
- `variety_avocado` (string): Variedad de aguacate.

### Campos Opcionales
- `end_datetime` (string|null): Fecha y hora de fin del trade. Formato RFC3339. Si no se proporciona, será `null`.

## Response

### Success (201 Created)
```json
{
  "message": "Trade creado exitosamente",
  "trade": {
    "trade_id": 1,
    "party_id": 1,
    "boss_id": 1,
    "shipment_id": 1,
    "trade_type": "PURCHASE",
    "start_datetime": "2024-01-01T10:00:00Z",
    "end_datetime": "2024-01-01T18:00:00Z",
    "discount_weight_per_tray": "5.50",
    "variety_avocado": "Hass",
    "party": {
      "party_id": 1,
      "party_role": "SUPPLIER",
      "alias_name": "Proveedor ABC"
    },
    "boss": {
      "boss_id": 1,
      "name_boss": "Mi Negocio",
      "boss_type": "RETAIL"
    },
    "shipment": {
      "shipment_id": 1,
      "start_date": "2024-01-01",
      "status": "OPEN"
    },
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:00:00Z"
  }
}
```

**Nota:** El campo `boss_id` se asigna automáticamente basado en el usuario autenticado.

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (404 Not Found)
```json
{
  "error": "no se encontraron datos de boss para este usuario"
}
```

### Error (400 Bad Request)
```json
{
  "error": "formato de start_datetime inválido. Use formato RFC3339 (ej: 2024-01-01T10:00:00Z)"
}
```

```json
{
  "error": "Key: 'CreateTradeRequest.TradeType' Error:Field validation for 'TradeType' failed on the 'oneof' tag"
}
```

## Ejemplo de Uso

### Crear una compra (PURCHASE)
```bash
curl -X POST http://localhost:5001/api/v1/trades \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_id": 1,
    "shipment_id": 1,
    "trade_type": "PURCHASE",
    "start_datetime": "2024-01-01T10:00:00Z",
    "end_datetime": "2024-01-01T18:00:00Z",
    "discount_weight_per_tray": "5.50",
    "variety_avocado": "Hass"
  }'
```

### Crear una venta (SALE)
```bash
curl -X POST http://localhost:5001/api/v1/trades \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_id": 2,
    "shipment_id": 1,
    "trade_type": "SALE",
    "start_datetime": "2024-01-01T14:00:00Z",
    "discount_weight_per_tray": "3.25",
    "variety_avocado": "Fuerte"
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Crear una compra
const purchaseResponse = await fetch('http://localhost:5001/api/v1/trades', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    party_id: 1,
    shipment_id: 1,
    trade_type: 'PURCHASE',
    start_datetime: '2024-01-01T10:00:00Z',
    end_datetime: '2024-01-01T18:00:00Z',
    discount_weight_per_tray: '5.50',
    variety_avocado: 'Hass'
  })
});

const purchaseData = await purchaseResponse.json();

// Crear una venta
const saleResponse = await fetch('http://localhost:5001/api/v1/trades', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    party_id: 2,
    shipment_id: 1,
    trade_type: 'SALE',
    start_datetime: '2024-01-01T14:00:00Z',
    discount_weight_per_tray: '3.25',
    variety_avocado: 'Fuerte'
  })
});

const saleData = await saleResponse.json();
```

## Notas
- El `boss_id` se asigna automáticamente basado en el usuario autenticado
- El usuario debe tener datos de Boss completados para poder crear trades
- `trade_type` debe ser exactamente `PURCHASE` o `SALE` (case-sensitive)
- Las fechas deben estar en formato RFC3339 (ISO 8601)
- `end_datetime` es opcional y puede ser `null`
- `variety_avocado` es obligatorio y debe especificar la variedad de aguacate
- El trade creado incluye todas las relaciones (Party, Boss, Shipment) en la respuesta
