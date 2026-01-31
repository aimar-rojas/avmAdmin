# Update Party - Actualizar Party

## Descripción
Endpoint para actualizar un party existente.

## Endpoint
```
PUT /api/v1/parties/:id
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Path Parameters
- `id` (uint): ID del party a actualizar.

## Request Body
Todos los campos son opcionales. Solo se actualizarán los campos que envíes.

```json
{
  "party_role": "buyer",
  "alias_name": "Nuevo Nombre Comercial",
  "first_name": "Pedro",
  "last_name": "Sánchez",
  "dni": "11223344",
  "ruc": "20112233445",
  "phone": "555555555"
}
```

### Campos Opcionales
- `party_role` (string): Nuevo rol. Debe ser `producer` o `buyer`.
- `alias_name` (string): Nuevo nombre alias o comercial.
- `first_name` (string): Nuevo nombre.
- `last_name` (string): Nuevo apellido.
- `dni` (string): Nuevo DNI.
- `ruc` (string): Nuevo RUC.
- `phone` (string): Nuevo teléfono.

## Response

### Success (200 OK)
```json
{
  "message": "Party actualizado exitosamente",
  "party": {
    "party_id": 1,
    "party_role": "buyer",
    "alias_name": "Nuevo Nombre Comercial",
    "first_name": "Pedro",
    "last_name": "Sánchez",
    "dni": "11223344",
    "ruc": "20112233445",
    "phone": "555555555",
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
  "error": "party no encontrado"
}
```

### Error (400 Bad Request)
```json
{
  "error": "ID de party inválido"
}
```

## Ejemplo de Uso

### Actualizar solo el rol
```bash
curl -X PUT http://localhost:5001/api/v1/parties/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_role": "buyer"
  }'
```

### Actualizar nombre y teléfono
```bash
curl -X PUT http://localhost:5001/api/v1/parties/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Pedro",
    "phone": "555555555"
  }'
```

### Actualizar múltiples campos
```bash
curl -X PUT http://localhost:5001/api/v1/parties/1 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "party_role": "buyer",
    "alias_name": "Nuevo Nombre Comercial",
    "first_name": "Pedro",
    "last_name": "Sánchez",
    "dni": "11223344",
    "ruc": "20112233445",
    "phone": "555555555"
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');
const partyId = 1;

// Actualizar solo algunos campos
const response = await fetch(`http://localhost:5001/api/v1/parties/${partyId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    first_name: 'Pedro',
    phone: '555555555'
  })
});

const data = await response.json();
console.log('Party actualizado:', data.party);
```

## Notas
- La actualización es parcial: solo se actualizan los campos que envíes
- `party_role` debe ser exactamente `producer` o `buyer` si se proporciona
- Todos los campos son opcionales en la actualización
- El party actualizado se retorna completo en la respuesta
