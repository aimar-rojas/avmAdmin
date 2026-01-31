# Update Profile - Actualizar Perfil de Boss

## Descripción
Endpoint para actualizar los datos del Boss asociado al usuario autenticado.

## Endpoint
```
PUT /api/v1/profile
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
  "name_boss": "Nuevo Nombre del Boss",
  "boss_type": "Nuevo Tipo de Boss"
}
```

### Campos Requeridos
- `name_boss` (string): Nombre del negocio/boss.
- `boss_type` (string): Tipo de negocio.

## Response

### Success (200 OK)
```json
{
  "message": "Perfil actualizado exitosamente",
  "boss": {
    "boss_id": 1,
    "name_boss": "Nuevo Nombre del Boss",
    "boss_type": "Nuevo Tipo de Boss",
    "user_id": 1,
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
  "error": "no se encontraron datos de boss para este usuario"
}
```

### Error (400 Bad Request)
```json
{
  "error": "Key: 'UpdateProfileRequest.NameBoss' Error:Field validation for 'NameBoss' failed on the 'required' tag"
}
```

## Ejemplo de Uso

### cURL
```bash
curl -X PUT http://localhost:5001/api/v1/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "name_boss": "Nuevo Nombre del Boss",
    "boss_type": "Nuevo Tipo de Boss"
  }'
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:5001/api/v1/profile', {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name_boss: 'Nuevo Nombre del Boss',
    boss_type: 'Nuevo Tipo de Boss'
  })
});

const data = await response.json();
```

## Notas
- El usuario debe tener datos de Boss existentes antes de poder actualizarlos
- Si el usuario no tiene datos de Boss, debe crearlos primero (normalmente se hace en el flujo de registro)
- Todos los campos son requeridos en la actualización
