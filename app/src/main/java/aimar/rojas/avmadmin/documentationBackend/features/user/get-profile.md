# Get Profile - Obtener Perfil de Boss

## Descripción
Endpoint para obtener los datos completos del Boss asociado al usuario autenticado.

## Endpoint
```
GET /api/v1/profile
```

## Autenticación
Requiere autenticación (endpoint protegido)

### Header Requerido
```
Authorization: Bearer <token>
```

## Response

### Success (200 OK)
```json
{
  "message": "Datos de boss obtenidos exitosamente",
  "boss": {
    "boss_id": 1,
    "name_boss": "Nombre del Boss",
    "boss_type": "Tipo de Boss",
    "user_id": 1,
    "created_at": "2024-01-01T00:00:00Z",
    "updated_at": "2024-01-01T00:00:00Z"
  }
}
```

#### Campos de Respuesta
- `boss_id` (uint): ID único del registro de Boss.
- `name_boss` (string): Nombre del negocio/boss.
- `boss_type` (string): Tipo de negocio.
- `user_id` (uint): ID del usuario asociado.
- `created_at` (string): Fecha de creación.
- `updated_at` (string): Fecha de última actualización.

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

## Ejemplo de Uso

### cURL
```bash
curl -X GET http://localhost:5001/api/v1/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### JavaScript (Fetch)
```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:5001/api/v1/profile', {
  method: 'GET',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();
```

## Notas
- Este endpoint solo retorna datos si el usuario tiene un perfil de Boss completado
- Si el usuario no tiene datos de Boss, debe usar `PUT /api/v1/profile` para crearlos primero
- Los datos del usuario básico ya se obtienen en el endpoint `/login`, este endpoint es específico para datos de Boss
