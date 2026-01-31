# Create Party - Crear Party

## Descripción
Endpoint para crear un nuevo party (proveedor o comprador).

## Endpoint
```
POST /api/v1/parties
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
  "party_role": "producer",
  "alias_name": "Proveedor ABC",
  "first_name": "Juan",
  "last_name": "Pérez",
  "dni": "12345678",
  "ruc": "20123456789",
  "phone": "987654321"
}
```

### Campos Requeridos
- `party_role` (string): Rol del party. Debe ser `producer` (proveedor) o `buyer` (comprador).

### Campos Opcionales
- `alias_name` (string): Nombre alias o comercial.
- `first_name` (string): Nombre.
- `last_name` (string): Apellido.
- `dni` (string): Documento Nacional de Identidad.
- `ruc` (string): Registro Único de Contribuyente.
- `phone` (string): Teléfono.

## Response

### Success (201 Created)
```json
{
  "message": "Party creado exitosamente",
  "party": {
    "party_id": 1,
    "party_role": "producer",
    "alias_name": "Proveedor ABC",
    "first_name": "Juan",
    "last_name": "Pérez",
    "dni": "12345678",
    "ruc": "20123456789",
    "phone": "987654321",
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
  "error": "Key: 'CreatePartyRequest.PartyRole' Error:Field validation for 'PartyRole' failed on the 'oneof' tag"
}
```

## Ejemplo de Uso

### Crear un producer (proveedor)
```bash
curl -X POST http://localhost:5001/api/v1/parties \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_role": "producer",
    "alias_name": "Proveedor ABC",
    "first_name": "Juan",
    "last_name": "Pérez",
    "dni": "12345678",
    "ruc": "20123456789",
    "phone": "987654321"
  }'
```

### Crear un buyer (comprador)
```bash
curl -X POST http://localhost:5001/api/v1/parties \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_role": "buyer",
    "alias_name": "Cliente XYZ",
    "first_name": "María",
    "last_name": "González",
    "dni": "87654321",
    "ruc": "20987654321",
    "phone": "123456789"
  }'
```

### Crear un party con campos mínimos
```bash
curl -X POST http://localhost:5001/api/v1/parties \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_role": "producer"
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Crear un party
const response = await fetch('http://localhost:5001/api/v1/parties', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    party_role: 'producer',
    alias_name: 'Proveedor ABC',
    first_name: 'Juan',
    last_name: 'Pérez',
    dni: '12345678',
    ruc: '20123456789',
    phone: '987654321'
  })
});

const data = await response.json();
console.log('Party creado:', data.party);
```

## Notas
- `party_role` es obligatorio y debe ser exactamente `producer` o `buyer` (case-sensitive)
- Todos los demás campos son opcionales
- El party creado se retorna completo en la respuesta
- Los parties se pueden usar luego en la creación de trades (como proveedor o cliente)
