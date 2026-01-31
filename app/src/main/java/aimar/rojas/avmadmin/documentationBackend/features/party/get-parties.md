# Get Parties - Obtener Parties

## Descripción
Endpoint para obtener todos los parties (proveedores y compradores) del sistema con filtros opcionales. **No incluye paginación** ya que se espera que haya pocos registros en este negocio.

## Endpoint
```
GET /api/v1/parties
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Query Parameters

### Parámetros Opcionales (todos los filtros se pueden combinar)
- `party_role` (string): Filtro por rol. Valores válidos: `producer` (proveedor) o `buyer` (comprador).
- `first_name` (string): Búsqueda parcial en el nombre (case-insensitive).
- `last_name` (string): Búsqueda parcial en el apellido (case-insensitive).
- `dni` (string): Búsqueda parcial en el DNI.
- `ruc` (string): Búsqueda parcial en el RUC.
- `phone` (string): Búsqueda parcial en el teléfono.

## Ejemplos de Uso

### Obtener todos los parties (sin filtro)
```
GET /api/v1/parties
```
Trae todos los parties del sistema.

### Filtrar por rol (producer)
```
GET /api/v1/parties?party_role=producer
```
Trae solo los parties con rol de proveedor.

### Filtrar por rol (buyer)
```
GET /api/v1/parties?party_role=buyer
```
Trae solo los parties con rol de comprador.

### Buscar por nombre
```
GET /api/v1/parties?first_name=Juan
```
Trae parties cuyo nombre contenga "Juan" (búsqueda parcial, case-insensitive).

### Buscar por apellido
```
GET /api/v1/parties?last_name=Pérez
```
Trae parties cuyo apellido contenga "Pérez" (búsqueda parcial, case-insensitive).

### Buscar por DNI
```
GET /api/v1/parties?dni=12345678
```
Trae parties cuyo DNI contenga "12345678" (búsqueda parcial).

### Buscar por RUC
```
GET /api/v1/parties?ruc=20123456789
```
Trae parties cuyo RUC contenga "20123456789" (búsqueda parcial).

### Buscar por teléfono
```
GET /api/v1/parties?phone=987654321
```
Trae parties cuyo teléfono contenga "987654321" (búsqueda parcial).

### Combinar múltiples filtros
```
GET /api/v1/parties?party_role=producer&first_name=Juan&dni=1234
```
Trae producers cuyo nombre contenga "Juan" y DNI contenga "1234".

## Response

### Success (200 OK)
```json
{
  "parties": [
    {
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
    },
    {
      "party_id": 2,
      "party_role": "buyer",
      "alias_name": "Cliente XYZ",
      "first_name": "María",
      "last_name": "González",
      "dni": "87654321",
      "ruc": "20987654321",
      "phone": "123456789",
      "created_at": "2024-01-02T00:00:00Z",
      "updated_at": "2024-01-02T00:00:00Z"
    }
  ],
  "total": 2
}
```

#### Campos de Respuesta
- `parties` (array): Lista de parties que cumplen los filtros.
- `total` (int): Total de parties retornados.

#### Campos del Party
- `party_id` (uint): ID único del party.
- `party_role` (string): Rol del party: `producer` (proveedor) o `buyer` (comprador).
- `alias_name` (string): Nombre alias o comercial (opcional).
- `first_name` (string): Nombre (opcional).
- `last_name` (string): Apellido (opcional).
- `dni` (string): Documento Nacional de Identidad (opcional).
- `ruc` (string): Registro Único de Contribuyente (opcional).
- `phone` (string): Teléfono (opcional).

### Error (401 Unauthorized)
```json
{
  "error": "usuario no autenticado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "party_role debe ser 'producer' o 'buyer'"
}
```

## Ejemplo de Uso

### cURL - Obtener todos los parties
```bash
curl -X GET "http://localhost:5001/api/v1/parties" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Filtrar por rol
```bash
curl -X GET "http://localhost:5001/api/v1/parties?party_role=producer" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### cURL - Buscar por nombre y DNI
```bash
curl -X GET "http://localhost:5001/api/v1/parties?first_name=Juan&dni=1234" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

// Obtener todos los parties
const response = await fetch('http://localhost:5001/api/v1/parties', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
console.log(`Total de parties: ${data.total}`);

// Filtrar por rol
const producers = await fetch('http://localhost:5001/api/v1/parties?party_role=producer', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

// Buscar por nombre
const searchByName = await fetch('http://localhost:5001/api/v1/parties?first_name=Juan', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

## Notas
- **Este endpoint NO tiene paginación** ya que se espera que haya pocos parties en el sistema
- **Este endpoint trae TODOS los parties del sistema**, no solo los del usuario autenticado
- Los resultados están ordenados por `party_id` ascendente
- Los filtros de texto (first_name, last_name, dni, ruc, phone) son búsquedas parciales (LIKE)
- Los filtros de texto son case-insensitive para nombres y apellidos
- Todos los filtros se pueden combinar para hacer búsquedas más específicas
- `party_role` debe ser exactamente `producer` o `buyer` (case-insensitive en la búsqueda)
